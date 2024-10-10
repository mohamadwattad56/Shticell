package Spreadsheet.impl;

import Spreadsheet.loader.STLBoundaries;
import Spreadsheet.loader.STLCell;
import Spreadsheet.loader.STLRange;
import Spreadsheet.loader.STLSheet;
import cell.api.Cell;
import cell.impl.*;
import dto.*;
import exception.CircularDependencyException;
import exception.OutOfBoundsException;
import exception.RangeDoesNotExist;
import function.api.Functions;
import function.impl.*;
import jakarta.xml.bind.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Spreadsheet implements Serializable {
    private static final String CIRCULAR_DEPENDENCY_ERROR = "Circular dependency or missing reference detected.";
    private static final String OUT_OF_BOUNDS_ERROR = "Cell %s is out of bounds.";;

    private Map<String, CellImpl> cells = new HashMap<>();
    private Map<String, Integer> cellLastModifiedVersion = new HashMap<>();
    public STLSheet sheet;
    private int numRows;
    private int numCols;
    private double rowHeight;
    private double columnWidth;
    private Map<String, Set<String>> ranges = new HashMap<>();


    public void initializeGrid(STLSheet sheet) {
        int rows = sheet.getSTLLayout().getRows();
        int columns = sheet.getSTLLayout().getColumns();
        int rowHeight = sheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        int columnWidth = sheet.getSTLLayout().getSTLSize().getColumnWidthUnits();

        clearSpreadsheet();


        this.numRows = rows;
        this.numCols = columns;
        this.rowHeight = rowHeight;
        this.columnWidth = columnWidth;
    }
    public int getNumRows() {
        return numRows;
    }
    public int getNumCols() {
        return numCols;
    }

    public double getRowHeight() {
        return rowHeight;
    }
    public double getColumnWidth() {
        return columnWidth;
    }

    public Map<String, Set<String>> getRanges() {
        return ranges;
    }



    public void loadFromXml(String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            sheet = (STLSheet) unmarshaller.unmarshal(new File(filePath));

            initializeGrid(sheet);  // Initialize the grid structure based on the XML

            // Load ranges into the ranges map (store only the cell IDs)
            if (sheet.getSTLRanges() != null) {
                Set<String> rangeNames = new HashSet<>(); // Set to track unique range names
                for (STLRange range : sheet.getSTLRanges().getSTLRange()) {
                    String rangeName = range.getName();

                    // Check for duplicate range names
                    if (!rangeNames.add(rangeName)) {
                        throw new IllegalArgumentException("The range name '" + rangeName + "' appears more than once in the XML file.");
                    }

                    STLBoundaries boundaries = range.getSTLBoundaries();
                    String fromCellId = boundaries.getFrom();
                    String toCellId = boundaries.getTo();

                    Set<String> cellIdsInRange = getCellIdsInRange(fromCellId, toCellId);
                    ranges.put(rangeName, cellIdsInRange);  // Only store the cell IDs in the ranges map
                }
            }

            // Load cells and handle functions
            List<STLCell> functionCells = new ArrayList<>();
            for (STLCell cell : sheet.getSTLCells().getSTLCell()) {
                if (!isFunction(cell.getSTLOriginalValue())) {
                    String cellId = cell.getCellId();
                    setCellValue(cellId, cell.getSTLOriginalValue());
                } else {
                    functionCells.add(cell);  // Add function cells to process later
                }
            }

            // Initialize all remaining empty cells to "EMPTY"
            int numRows = sheet.getSTLLayout().getRows();
            int numColumns = sheet.getSTLLayout().getColumns();

            for (int row = 1; row <= numRows; row++) {
                for (int col = 1; col <= numColumns; col++) {
                    String cellId = getCellIdFromCoordinates(row, col);
                    if (!cells.containsKey(cellId)) {
                        // Initialize any missing cells with "EMPTY"
                        setCellValue(cellId, "EMPTY",0);
                    }
                }
            }

            // Resolve the function cells
            boolean progress;
            while (!functionCells.isEmpty()) {
                progress = false;
                Iterator<STLCell> iterator = functionCells.iterator();

                while (iterator.hasNext()) {
                    STLCell cell = iterator.next();
                    String cellId = cell.getCellId();
                    String originalValue = cell.getSTLOriginalValue();
                    try {
                        setCellValue(cellId, originalValue);  // This will now dynamically fetch any ranges needed
                        iterator.remove();
                        progress = true;
                    }
                    catch (RangeDoesNotExist e) {
                        throw new RangeDoesNotExist("Error calculating the cell " + cellId +" :" + e.getMessage());
                    }
                    catch (Exception e) {
                        // Skip this cell if its dependencies aren't ready
                    }
                }

                if (!progress) {
                    break;
                }
            }

        } catch (JAXBException e) {
            throw new RuntimeException("Error parsing the XML file: " + e.getMessage(), e);
        }
    }

    public String getCellIdFromCoordinates(int row, int column) {
        char columnLetter = (char) ('A' + (column - 1));
        return String.valueOf(columnLetter) + row;
    }

    public Set<String> getCellIdsInRange(String fromCellId, String toCellId) {
        Set<String> cellIds = new HashSet<>();

        // Extract row and column information from the provided cell IDs
        int fromRow = extractRow(fromCellId);
        int fromCol = extractCol(fromCellId);  // Column is extracted as an integer (A=1, B=2, etc.)
        int toRow = extractRow(toCellId);
        int toCol = extractCol(toCellId);

        // Ensure we process the range in the correct order (smallest to largest)
        int startRow = Math.min(fromRow, toRow);
        int endRow = Math.max(fromRow, toRow);
        int startCol = Math.min(fromCol, toCol);
        int endCol = Math.max(fromCol, toCol);

        // Iterate over the rows and columns within the range
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                // Convert the column index back to a letter (A, B, C, etc.)
                String columnName = Character.toString((char) ('A' + col - 1));
                String cellId = generateCellId(row, columnName);  // Generate the cell ID using the existing method

                // Ensure the cell is within bounds before adding it to the set
                if (isWithinBounds(cellId)) {
                    cellIds.add(cellId);
                } else {
                    throw new OutOfBoundsException(String.format("Cell %s is out of bounds.", cellId));
                }
            }
        }

        return cellIds;
    }

    public Set<CellImpl> getCellsInRange(String rangeName) {
        Set<String> cellIds = ranges.get(rangeName);
        if (cellIds == null) {
            throw new RangeDoesNotExist("Range with name '" + rangeName + "' does not exist.");
        }

        Set<CellImpl> cellsInRange = new HashSet<>();
        for (String cellId : cellIds) {
            cellsInRange.add(getCellById(cellId));  // Fetch each CellImpl by its ID
        }

        return cellsInRange;
    }

    private String generateCellId(int row, String column) {
        // Assume column is already a letter (e.g., "A", "B", etc.)
        return column.toUpperCase() + row;
    }


    // Cell Management Methods
    public void clearSpreadsheet() {
        cells.clear();
        ranges.clear();
        cellLastModifiedVersion.clear();
    }

    public void setCellValue(String cellId, Object value) {
        cellLastModifiedVersion.put(cellId, 1);
        setCellValue(cellId, value, -1);
    }

    public int setCellValue(String cellId, Object value, int currentVersion) {
        if (!isWithinBounds(cellId)) {
            throw new OutOfBoundsException(String.format(OUT_OF_BOUNDS_ERROR, cellId));
        }

        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
           value = "EMPTY";
        }

        // Fetch the existing cell if it exists, so we can retain its colors
        CellImpl existingCell = cells.get(cellId);
        String existingTextColor = (existingCell != null) ? existingCell.getTextColor() : "black";  // default to black
        String existingBackgroundColor = (existingCell != null) ? existingCell.getBackgroundColor() : "white";  // default to white

        // Create a new cell based on the value
        CellImpl tempCell = createCellBasedOnValue(value);

        // Retain the existing colors in the new cell
        tempCell.setTextColor(existingTextColor);
        tempCell.setBackgroundColor(existingBackgroundColor);

        // Process the cell change (updating the spreadsheet data)
        int affectedCellsCount = processCellChange(cellId, tempCell, value, currentVersion);

        return affectedCellsCount;
    }



    private int processCellChange(String cellId, CellImpl tempCell, Object value, int currentVersion) {
        int affectedCellsCount = 0;
        try {
            System.out.println("Processing cell change for: " + cellId + " with value: " + value);

            cells.put(cellId, tempCell);
            detectCircularDependency(cellId, new HashSet<>());

            if (!validChange(cellId, tempCell, value)) {
                throw new IllegalArgumentException("Changing the value of " + cellId + " would invalidate dependent cells.");
            }

            cells.put(cellId, tempCell);
            affectedCellsCount++;

            recalculate();

            if (currentVersion != -1) {
                cellLastModifiedVersion.put(cellId, currentVersion);
                affectedCellsCount += updateDependentCells(cellId, currentVersion);
            }

            System.out.println("Successfully processed cell change for " + cellId);

        } catch (Exception e) {
            System.out.println("Error processing cell change for " + cellId + ": " + e.getMessage());
            revertCellChange(cellId, currentVersion);
            throw new IllegalArgumentException("Error calculating cell " + cellId + ": " + e.getMessage());
        }

        return affectedCellsCount;
    }


    private boolean validChange(String cellId, CellImpl cell, Object newValue) {
        Object originalSourceValue = cell != null ? cell.getSourceValue() : null;
        Object originalEffectiveValue = cell != null ? cell.getEffectiveValue() : null;

        try {
            CellImpl tempCell = createCellBasedOnValue(newValue);
            tempCell.setSourceValue(newValue);

            if (cell != null) {
          /*      cell.setSourceValue(newValue);
            } else {*/
                cell = tempCell;
            }

            detectCircularDependency(cellId, new HashSet<>());
           // cell.evaluate();

            for (Map.Entry<String, CellImpl> entry : cells.entrySet()) {
                CellImpl dependentCell = entry.getValue();
                if (dependentCell instanceof FunctionCell && ((FunctionCell) dependentCell).dependsOn(cellId)) {
                    try {
                        dependentCell.evaluate();
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            return true;
        } catch (CircularDependencyException e) {
            throw new CircularDependencyException(e.getMessage());
        } /*finally {
            if (cell != null) {
                cell.setSourceValue(originalSourceValue);
                cell.setEffectiveValue(originalEffectiveValue);
            }
        }*/
    }

    CellImpl createCellBasedOnValue(Object value) {
        if (value instanceof Number) {
            return new NumericCell(value);
        } else if (value instanceof String) {
            if (isFunction((String) value)) {
                return new FunctionCell(value, parseFunction((String) value), this, parseArguments((String) value));
            } else if (((String) value).equalsIgnoreCase("TRUE") || ((String) value).equalsIgnoreCase("FALSE")) {
                return new BooleanCell(value);
            } else {
                return new StringCell(value);
            }
        } else {
            throw new IllegalArgumentException("Unsupported value type.");
        }
    }


    private void handleCircularDependency(String cellId, int currentVersion) {
        if (currentVersion == -1) {
            cells.remove(cellId);
        }
        throw new CircularDependencyException(CIRCULAR_DEPENDENCY_ERROR);
    }

    private void revertCellChange(String cellId, int currentVersion) {
        if (currentVersion == -1) {
            cells.remove(cellId);
        }
    }

    private int updateDependentCells(String cellId, int currentVersion) {
        int affectedCellsCount = 0;
        for (Map.Entry<String, CellImpl> entry : cells.entrySet()) {
            String dependentCellId = entry.getKey();
            CellImpl dependentCell = entry.getValue();
            if (dependentCell instanceof FunctionCell && ((FunctionCell) dependentCell).dependsOn(cellId)) {
                cellLastModifiedVersion.put(dependentCellId, currentVersion);
                affectedCellsCount++;
                affectedCellsCount += updateDependentCells(dependentCellId, currentVersion);
            }
        }
        return affectedCellsCount;
    }

    private void detectCircularDependency(String cellId, Set<String> visitedCells) throws CircularDependencyException {
        if (visitedCells.contains(cellId)) {
            throw new CircularDependencyException("Circular dependency detected involving cell " + cellId);
        }

        visitedCells.add(cellId);
        CellImpl cell = cells.get(cellId);

        if (cell instanceof FunctionCell) {
            FunctionCell functionCell = (FunctionCell) cell;
            for (Cell arg : functionCell.getArgs()) {
                if (arg instanceof StringCell) {
                    String referencedCellId = ((StringCell) arg).getSourceValue().toString();
                    detectCircularDependency(referencedCellId, visitedCells);
                }
            }
        }

        visitedCells.remove(cellId);
    }


    boolean isWithinBounds(String cellId) {
        int row = extractRow(cellId);
        int col = extractCol(cellId);
        return row > 0 && row <= sheet.getSTLLayout().getRows() && col > 0 && col <= sheet.getSTLLayout().getColumns();
    }

    private int extractRow(String cellId) {
        String rowPart = cellId.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowPart);
    }

    private int extractCol(String cellId) {
        String colPart = cellId.replaceAll("[0-9]", "").toUpperCase();
        return colPart.chars().reduce(0, (acc, ch) -> acc * 26 + (ch - 'A' + 1));
    }

    // Utility Methods
    public void recalculate() {
        Set<String> evaluatedCells = new HashSet<>();
        cells.values().forEach(cell -> {
            if (!evaluatedCells.contains(cell)) {
                cell.evaluate();
                evaluatedCells.add(cell.getSourceValue().toString());
            }
        });
    }

    public CellImpl resolveReference(String cellReference) {
        if(extractCol(cellReference)<numCols && extractRow(cellReference)<numRows){
            Optional<CellImpl> cell = Optional.ofNullable(cells.get(cellReference.toUpperCase()));

            if (cell.isPresent()) {
                return cell.get();
            } else {
                // If the referenced cell is empty, return a special cell indicating it's empty
                return new StringCell("EMPTY");
            }
        }else{
            return new StringCell("!UNKNOWN!");
        }
    }


    public boolean isFunction(String value) {
        return value.startsWith("{") && value.endsWith("}");
    }

    public Functions parseFunction(String value) {
        int startIndex = value.indexOf('{') + 1; // Move past the '{'
        int endIndex = value.indexOf(','); // The position of the comma

        // Extract and trim the function name
        String functionName = value.substring(startIndex, endIndex).trim().toUpperCase();
        return switch (functionName.trim()) {
            case "PLUS" -> new PLUS();
            case "MINUS" -> new MINUS();
            case "TIMES" -> new TIMES();
            case "DIVIDE" -> new DIVIDE();
            case "MOD" -> new MOD();
            case "POW" -> new POW();
            case "ABS" -> new ABS();
            case "CONCAT" -> new CONCAT();
            case "SUB" -> new SUB();
            case "REF" -> new REF(this);
            case "AND" -> new AND();
            case "AVERAGE" -> new AVERAGE(this);
            case "BIGGER" -> new BIGGER();
            case "LESS" -> new LESS();
            case "NOT" -> new NOT();
            case "IF" -> new IF();
            case "OR" -> new OR();
            case "PERCENT" -> new PERCENT();
            case "SUM" -> new SUM(this);
            case "EQUAL" -> new EQUAL();
            default -> throw new IllegalArgumentException("Unknown function: " + functionName);
        };
    }


    public Cell[] parseArguments(String value1) {
        String value = value1.trim();
        int firstCommaIndex = value.indexOf(',');
        if (firstCommaIndex == -1) {
            throw new IllegalArgumentException("Function must have arguments.");
        }

        String argsString = value.substring(firstCommaIndex + 1, value.length() - 1);
        if (argsString.isEmpty()) {
            return new Cell[0];
        }

        List<String> argStrings = splitArguments(argsString);
        Cell[] args = new Cell[argStrings.size()];
        for (int i = 0; i < argStrings.size(); i++) {
            String arg = argStrings.get(i);
            args[i] = parseArgument(arg);
        }

        return args;
    }

    private List<String> splitArguments(String argsString) {
        List<String> arguments = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        int nestedLevel = 0;

        for (char c : argsString.toCharArray()) {
            if (c == '{') nestedLevel++;
            if (c == '}') nestedLevel--;

            if (c == ',' && nestedLevel == 0) {
                arguments.add(currentArg.toString());
                currentArg.setLength(0);
            } else {
                currentArg.append(c);
            }
        }

        if (!currentArg.isEmpty()) {
            arguments.add(currentArg.toString());
        }

        return arguments;
    }

    private Cell parseArgument(String arg) {
        if (arg.matches("-?\\d+(\\.\\d+)?")) {
            return new NumericCell(Double.parseDouble(arg));
        } else if (arg.equalsIgnoreCase("TRUE") || arg.equalsIgnoreCase("FALSE")) {
            return new BooleanCell(Boolean.parseBoolean(arg));
        } else if (arg.trim().startsWith("{")) {
            return new FunctionCell(arg, parseFunction(arg), this, parseArguments(arg));
        } else if (isCellReference(arg)) {
            return new StringCell(arg);
        } else {
            return new StringCell(arg);
        }
    }

    private boolean isCellReference(String arg) {
        return arg.toUpperCase().trim().matches("[A-Z]{1,3}[0-9]+");
    }



    // Data Transfer Methods
    public SpreadsheetDTO toDTO() {
        // Proceed with converting cells to DTOs
        List<CellDTO> cellDTOs = cells.entrySet().stream()
                .map(entry -> {
                    String cellId = entry.getKey();
                    CellImpl cell = entry.getValue();
                    List<String> dependencies = findDependencies(cellId);
                    List<String> dependents = findDependents(cellId);
                    int lastModifiedVersion = getLastModifiedVersion(cellId);
                    return new CellDTO(
                            cellId,
                            cell.getSourceValue(),
                            cell.getEffectiveValue(),
                            cell.getType(),
                            lastModifiedVersion,
                            dependencies,
                            dependents,
                            cell.getTextColor(),
                            cell.getBackgroundColor()
                    );
                })
                .collect(Collectors.toList());

        // Debug: Deep copying ranges
        Map<String, Set<String>> copiedRanges = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : ranges.entrySet()) {
            copiedRanges.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        // Final return, include the deep-copied ranges
        return new SpreadsheetDTO(
                sheet != null ? sheet.getName() : "Unnamed Sheet",
                cellDTOs,
                sheet.getSTLLayout().getRows(),
                sheet.getSTLLayout().getColumns(),
                sheet.getSTLLayout().getSTLSize().getColumnWidthUnits(),
                sheet.getSTLLayout().getSTLSize().getRowsHeightUnits(),
                copiedRanges
        );
    }



    public CellDTO getCellDTO(String cellId) {
        CellImpl cell = cells.get(cellId);
        if (isWithinBounds(cellId)) {
                if (cell != null) {
                List<String> dependencies = findDependencies(cellId);
                List<String> dependents = findDependents(cellId);
                int lastModifiedVersion = getLastModifiedVersion(cellId);
                return new CellDTO(
                        cellId,
                        cell.getSourceValue(),
                        cell.getEffectiveValue(),
                        cell.getType(),
                        lastModifiedVersion,
                        dependencies,
                        dependents,
                        cell.getTextColor(),
                        cell.getBackgroundColor()
                );
            } else {
                return new CellDTO(
                        cellId,
                        "",
                        "EMPTY",
                        CellType.EMPTY,
                        0,
                        new ArrayList<>(),
                        new ArrayList<>()

                );
            }
        } else {
            throw new OutOfBoundsException(String.format(OUT_OF_BOUNDS_ERROR, cellId));
        }
    }




    public List<String> findDependencies(String cellId) {
        // Create a set to avoid processing the same cell multiple times
        Set<String> visited = new HashSet<>();
        return findDependenciesHelper(cellId, visited);
    }

    private List<String> findDependenciesHelper(String cellId, Set<String> visited) throws CircularDependencyException {
        if (visited.contains(cellId)) {
            throw new CircularDependencyException("Circular dependency detected at cell: " + cellId);
        }

        visited.add(cellId);
        CellImpl cell = cells.get(cellId);

        if (!(cell instanceof FunctionCell)) {
            return Collections.emptyList();
        }

        List<String> dependencies = new ArrayList<>();
        String functionDefinition = cell.getSourceValue().toString();

        // Check if the function references a range (e.g., {SUM, grades})
        if (isRangeBasedFunction(functionDefinition)) {
            String rangeName = extractRangeFromFunction(functionDefinition);
            Set<String> cellsInRange = getCellsInRangeByName(rangeName);

            for (String rangeCellId : cellsInRange) {
                if (!dependencies.contains(rangeCellId)) {
                    dependencies.add(rangeCellId);
                    dependencies.addAll(findDependenciesHelper(rangeCellId, visited));
                }
            }
        } else {
            // Handle cell references and nested functions
            List<String> args = splitArguments(functionDefinition.substring(functionDefinition.indexOf(',') + 1, functionDefinition.length() - 1));

            for (String arg : args) {
                arg = arg.trim();
                if (isCellReference(arg)) {
                    String referencedCellId = arg;
                    if (!dependencies.contains(referencedCellId)) {
                        dependencies.add(referencedCellId);
                        dependencies.addAll(findDependenciesHelper(referencedCellId, visited));
                    }
                } else if (arg.startsWith("{")) {
                    dependencies.addAll(processNestedFunction(arg, visited));
                }
            }
        }

        return dependencies;
    }

    // Helper to detect if a function is range-based (e.g., SUM, AVERAGE)
    private boolean isRangeBasedFunction(String functionDefinition) {
        return functionDefinition.startsWith("{SUM") || functionDefinition.startsWith("{AVERAGE");
    }

    // Extract range name from function definition (e.g., {SUM, grades} -> "grades")
    private String extractRangeFromFunction(String functionDefinition) {
        return functionDefinition.substring(functionDefinition.indexOf(',') + 1, functionDefinition.length() - 1).trim();
    }

    // Get cells in range by range name (e.g., "grades" -> {C3, C4, C5})
    private Set<String> getCellsInRangeByName(String rangeName) {
        return ranges.get(rangeName); // Assumes the range is already stored in a map
    }

    private List<String> processNestedFunction(String functionDefinition, Set<String> visited) {
        List<String> dependencies = new ArrayList<>();

        // Extract the arguments from the nested function
        List<String> args = splitArguments(functionDefinition.substring(functionDefinition.indexOf(',') + 1, functionDefinition.length() - 1));

        // Process each argument in the nested function
        for (String nestedArg : args) {
            nestedArg = nestedArg.trim();
            if (isCellReference(nestedArg)) {
                String referencedNestedCellId = nestedArg;
                if (!dependencies.contains(referencedNestedCellId)) {
                    dependencies.add(referencedNestedCellId);
                    // Recursively find dependencies for the referenced cell in the nested function
                    dependencies.addAll(findDependenciesHelper(referencedNestedCellId, visited));
                }
            } else if (nestedArg.startsWith("{")) {
                // If it's another nested function, process it recursively
                dependencies.addAll(processNestedFunction(nestedArg, visited));
            }
        }

        return dependencies;
    }


    public List<String> findDependents(String cellId) {
        List<String> dependents = new ArrayList<>();

        // Find direct cell dependents
        dependents.addAll(cells.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof FunctionCell)
                .filter(entry -> entry.getValue().dependsOn(cellId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

        // Check for cells that depend on ranges that include cellId
        for (Map.Entry<String, CellImpl> entry : cells.entrySet()) {
            if (entry.getValue() instanceof FunctionCell) {
                String functionDefinition = entry.getValue().getSourceValue().toString();
                if (isRangeBasedFunction(functionDefinition)) {
                    String rangeName = extractRangeFromFunction(functionDefinition);
                    Set<String> cellsInRange = getCellsInRangeByName(rangeName);

                    // If cellId is part of the range, add the dependent
                    if (cellsInRange.contains(cellId)) {
                        dependents.add(entry.getKey());
                    }
                }
            }
        }

        return dependents;
    }




    private int getLastModifiedVersion(String cellId) {
        return cellLastModifiedVersion.getOrDefault(cellId, 0);
    }



    public Spreadsheet deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (Spreadsheet) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error during deep copy", e);
        }
    }

    // Existing method to add ranges
    public void addRange(String rangeName, String fromCellId, String toCellId) {
        Set<String> cellIdsInRange = getCellIdsInRange(fromCellId, toCellId);
        ranges.put(rangeName, cellIdsInRange);
    }

    public CellImpl getCellById(String cellId) {
        // Assuming you have a map called cells in your Spreadsheet class that stores cell data
        if (cells.containsKey(cellId)) {
            return cells.get(cellId);
        } else {
            throw new IllegalArgumentException("Cell with ID " + cellId + " does not exist.");
        }
    }

    // Remove a range
    public void removeRange(String rangeName) {
        ranges.remove(rangeName);
    }

    // Check if a range exists
    public boolean rangeExists(String rangeName) {
        return ranges.containsKey(rangeName);
    }

    // Get all range names
    public Set<String> getAllRangeNames() {
        return ranges.keySet();
    }

    public Set<String> getAllRangeCellsId(String range) {
        return ranges.get(range);
    }


    public void setCellTextColor(String cellId, Color color) {
        CellImpl cell = cells.get(cellId);
        String colorCode = toRgbCode(color); // Convert Color object to RGB String
        cell.setTextColor(colorCode);
    }

    public void setCellBackgroundColor(String cellId, Color color) {
        CellImpl cell = cells.get(cellId);
        String colorCode = toRgbCode(color); // Convert Color object to RGB String
        cell.setBackgroundColor(colorCode);
    }

    // Helper method to convert Color to RGB code (e.g., #FF0000 for red)
    private String toRgbCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }



    public String getCellTextColor(String cellId) {
        CellImpl cell = cells.get(cellId);
        return cell != null && cell.getTextColor() != null ? cell.getTextColor() : "#000000"; // Default to black
    }

    public String getCellBackgroundColor(String cellId) {
        CellImpl cell = cells.get(cellId);
        return cell != null && cell.getBackgroundColor() != null ? cell.getBackgroundColor() : "#FFFFFF"; // Default to white
    }


}
