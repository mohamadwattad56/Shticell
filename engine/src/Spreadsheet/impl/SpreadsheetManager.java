package Spreadsheet.impl;

import Spreadsheet.api.Engine;
import cell.impl.*;
import dto.*;
import exception.CircularDependencyException;
import exception.EmptyCellException;
import exception.OutOfBoundsException;
import exception.RangeDoesNotExist;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class SpreadsheetManager implements Engine {
    private final Spreadsheet currentSpreadsheet;
    private final List<Version> versionHistory = new ArrayList<>();
    private boolean isSheetLoaded = false;
    private int currentVersion = 1;



    public SpreadsheetManager() {
        this.currentSpreadsheet = new Spreadsheet();
    }

    public int getNumOfRows() {
        return currentSpreadsheet.getNumRows();
    }
    public int getNumOfColumns() {
        return currentSpreadsheet.getNumCols();
    }
    public double getRowHeight() {
        return currentSpreadsheet.getRowHeight();
    }
    public double getColumnWidth() {
        return currentSpreadsheet.getColumnWidth();
    }

    public CellUpdateDTO generateCellUpdateDTO(String cellId, String modifiedBy) {
        CellDTO updatedCell = getCellDTO(cellId);

        List<String> dependentIds = updatedCell.getDependents();
        List<CellDTO> dependentCells = dependentIds.stream()
                .map(this::getCellDTO)
                .collect(Collectors.toList());

        List<String> dependencyIds = updatedCell.getDependencies();
        List<CellDTO> dependencyCells = dependencyIds.stream()
                .map(this::getCellDTO)
                .collect(Collectors.toList());

        return new CellUpdateDTO(updatedCell, dependentCells, dependencyCells, modifiedBy);
    }



    public CellImpl createCellBasedOnValue(Object value) {
        return currentSpreadsheet.createCellBasedOnValue(value);
    }

    public SpreadsheetManagerDTO toDTO(String uploader) {
        // Convert the current spreadsheet to a DTO
        SpreadsheetDTO spreadsheetDTO = currentSpreadsheet.toDTO();

        // Convert version history to DTO
        List<VersionDTO> versionDTOList = versionHistory.stream()
                .map(Version::toDTO)
                .collect(Collectors.toList());

        return new SpreadsheetManagerDTO(
                spreadsheetDTO,
                versionDTOList,
                isSheetLoaded,
                currentVersion,
                uploader
        );
    }


 /*   public void setRowHeight(double rowHeight) {
        currentSpreadsheet.setRowHeight(rowHeight);
    }

    public void setColumnWidth(double columnWidth) {
        currentSpreadsheet.setColumnWidth(columnWidth);
    }*/


    public void loadSpreadsheet(String filePath) {
        try {
            // Load spreadsheet from XML
            currentSpreadsheet.loadFromXml(filePath);
            isSheetLoaded = true; // Mark as loaded
            currentVersion = 1;
            versionHistory.clear();
            versionHistory.add(new Version(currentVersion, currentSpreadsheet.deepCopy(), 0)); // Save initial version

            // Check for unique range names
            checkUniqueRangeNames();

            // Check that all ranges are in bounds
            checkRangesWithinBounds();

            // Check that functional cells (SUM, AVERAGE) reference valid ranges
            checkFunctionRangeValidity();

        } catch (RangeDoesNotExist e) {
            throw new RangeDoesNotExist(e.getMessage());
        }
        catch (Exception e) {
            isSheetLoaded = false; // Set to false if there was an error
            throw new IllegalArgumentException("Error loading spreadsheet: " + e.getMessage(), e);
        }
    }

        // 1. Check for unique range names
        private void checkUniqueRangeNames() {
            Set<String> rangeNames = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : currentSpreadsheet.getRanges().entrySet()) {
                String rangeName = entry.getKey();
                if (!rangeNames.add(rangeName)) {
                    throw new IllegalArgumentException("Range name " + rangeName + " is not unique.");
                }
            }
        }


        // 2. Check that all ranges are within bounds
        private void checkRangesWithinBounds() {
            for (Map.Entry<String, Set<String>> entry : currentSpreadsheet.getRanges().entrySet()) {
                for (String cellId : entry.getValue()) {
                    if (!isWithinBounds(cellId)) {
                        throw new OutOfBoundsException(String.format("Range '%s' includes out-of-bounds cell: %s", entry.getKey(), cellId));
                    }
                }
            }
        }

        // 3. Check that functional cells (SUM, AVERAGE) reference valid ranges
        private void checkFunctionRangeValidity() {
            // Iterate through the list of CellDTO objects from the SpreadsheetDTO
            for (CellDTO cellDTO : currentSpreadsheet.toDTO().getCells()) {
                // Check if the cell is a function cell
                if (cellDTO.getType() == CellType.FUNCTION) {
                    String functionName = cellDTO.getSourceValue().toString().toUpperCase();

                    // If the function is SUM or AVERAGE, ensure the referenced range exists
                    if (functionName.startsWith("{SUM") || functionName.startsWith("{AVERAGE")) {
                        String[] arguments = extractArguments(functionName);

                        for (String arg : arguments) {
                            if (isRange(arg)) {
                                if (!currentSpreadsheet.getAllRangeNames().contains(arg)) {
                                    throw new IllegalArgumentException("Function '" + functionName + "' references non-existent range: '" + arg + "'");
                                }
                            }
                        }
                    }
                }
            }
        }

        // Helper method to extract arguments from a function string
        private String[] extractArguments(String functionValue) {
            String argsString = functionValue.substring(functionValue.indexOf(',') + 1, functionValue.length() - 1);
            return argsString.split(",");
        }

        // Helper method to check if a string is a valid range reference (e.g., A1:B3)
        private boolean isRange(String value) {
            return value.matches("[A-Z]+[0-9]+:[A-Z]+[0-9]+");
        }

        // Helper method to check if a cell is within bounds
        private boolean isWithinBounds(String cellId) {
            return currentSpreadsheet.isWithinBounds(cellId); // Call to Spreadsheet class to check bounds
        }

        public boolean isSheetLoaded() {
            return isSheetLoaded;
        }

    public void updateCellValue(String cellId, String newValue, String oldValue, String modifiedBy, Boolean flag) {
        try {
            System.out.println("Updating cell " + cellId + " from " + oldValue + " to " + newValue);

            int changedCells = currentSpreadsheet.setCellValue(cellId, newValue, currentVersion);

            // Save the last modified user
            if (flag) {
                currentVersion++;
                versionHistory.add(new Version(currentVersion, currentSpreadsheet.deepCopy(), changedCells));
            }

            // Create a new CellUpdateDTO to include the last modified user
            CellUpdateDTO cellUpdateDTO = generateCellUpdateDTO(cellId, modifiedBy);

        } catch (IllegalArgumentException | CircularDependencyException e) {
            currentSpreadsheet.setCellValue(cellId, oldValue, currentVersion);
            throw new IllegalArgumentException(e.getMessage());
        } catch (EmptyCellException e) {
            throw new EmptyCellException(e.getMessage());
        }
    }




   /* private void propagateToDependents(String cellId) {
            CellDTO cell = currentSpreadsheet.getCellDTO(cellId);
            for (String dependentId : cell.getDependents()) {
                CellDTO dependentCell = currentSpreadsheet.getCellDTO(dependentId);
                String updatedValue = dependentCell.getSourceValue().toString();
                updateCellValue(dependentId, updatedValue, updatedValue,false);  // This will recursively update all dependents
            }
        }*/



        public CellDTO getCellDTO(String cellId) {
            return currentSpreadsheet.getCellDTO(cellId);
        }

        public boolean isFunction(String value) {
            return value.trim().startsWith("{") && value.trim().endsWith("}");
        }

        public List<String> getVersionHistory() {
            List<String> history = new ArrayList<>();
            for (Version version : versionHistory) {
                history.add("Version " + version.getVersionNumber() + ": " + version.getChangedCellsCount() + " cells changed.");
            }
            return history;
        }

    public VersionDTO getVersionDTO(int versionNumber) {
        if (versionNumber > versionHistory.size() || versionNumber <= 0) {
            throw new IllegalArgumentException("Invalid version number.");
        }

        Version selectedVersion = versionHistory.stream()
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst()
                .orElse(null);

        if (selectedVersion != null) {
            return selectedVersion.toDTO();
        } else {
            throw new IllegalArgumentException("Invalid version number.");
        }
    }


    public CellDTO getDisplayCellValue(String cellId) {
        try {
            return currentSpreadsheet.getCellDTO(cellId);
        } catch (OutOfBoundsException e) {
            throw new OutOfBoundsException(e.getMessage());
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean addRange(String rangeName, String fromCellId, String toCellId) {
        if (currentSpreadsheet.rangeExists(rangeName)) {
            return false; // Range with the same name already exists
        }

        // Directly call the addRange method in Spreadsheet
        currentSpreadsheet.addRange(rangeName, fromCellId, toCellId); // Corrected: passing fromCellId and toCellId
        return true;
    }


    public boolean deleteRange(String rangeName) {
        if (currentSpreadsheet.rangeExists(rangeName)) {
            // Check if any cell uses the range before deletion
            if (isRangeUsedInCells(rangeName)) {
                throw new IllegalArgumentException("Range '" + rangeName + "' is used by one or more cells and cannot be deleted.");
            }

            // If no cells use the range, delete it
            currentSpreadsheet.removeRange(rangeName);
            return true;
        }
        return false; // Range not found
    }

    private boolean isRangeUsedInCells(String rangeName) {
        // Get all the cells from the spreadsheet
        SpreadsheetDTO spreadsheetDTO = currentSpreadsheet.toDTO();
        List<CellDTO> cells = spreadsheetDTO.getCells();  // Get all cells in the DTO format

        // Loop through each cell to check if it references the range
        for (CellDTO cellDTO : cells) {
            // Check if the cell contains a function and uses the range
            String sourceValue = cellDTO.getSourceValue().toString();

            // If the sourceValue represents a function (SUM or AVERAGE), check if it references the range
            if (sourceValue.startsWith("{SUM,") || sourceValue.startsWith("{AVERAGE,")) {
                String referencedRange = extractRangeFromFunction(sourceValue);

                if (referencedRange != null && referencedRange.equals(rangeName)) {
                    return true;  // Range is used in this function
                }
            }
        }
        return false;  // Range is not used in any cells
    }

    private String extractRangeFromFunction(String sourceValue) {
        // Extract the range name from the function string
        if (sourceValue.contains(",")) {
            return sourceValue.substring(sourceValue.indexOf(',') + 1, sourceValue.length() - 1).trim();
        }
        return null;
    }

    // Fetch all range names
    public Set<String> getAllRangeNames() {
        return currentSpreadsheet.getAllRangeNames();
    }

    public Set<String> getRangeCells(String rangeName) {
        return currentSpreadsheet.getAllRangeCellsId(rangeName);
    }

    public void setCellTextColor(String cellId, Color color) {
        currentSpreadsheet.setCellTextColor(cellId, color); // Pass the Color object
    }

    public void setCellBackgroundColor(String cellId, Color color) {
        currentSpreadsheet.setCellBackgroundColor(cellId, color);
    }

    public String getCellTextColor(String cellId) {
        return currentSpreadsheet.getCellTextColor(cellId);
    }

    public String getCellBackgroundColor(String cellId) {
        return currentSpreadsheet.getCellBackgroundColor(cellId);
    }

    public String getSpreadsheetName() {
        return currentSpreadsheet.sheet.getName();
    }

    public int getCurrentVersion() {
        return currentVersion;
    }
}
