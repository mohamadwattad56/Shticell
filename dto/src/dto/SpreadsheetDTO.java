package dto;
import exception.OutOfBoundsException;
import java.util.*;

public class SpreadsheetDTO {
    private final String sheetName;
    private final List<CellDTO> cells;
    private final int rows;
    private final int columns;
    private final int columnWidthUnits;
    private final int rowsHeightUnits;
    private final Map<String, Set<String>> ranges;

    //ctor
    public SpreadsheetDTO(String sheetName, List<CellDTO> cells, int rows, int columns, int columnWidthUnits, int rowsHeightUnits, Map<String, Set<String>> ranges) {
        this.sheetName = sheetName;
        this.cells = cells;
        this.rows = rows;
        this.columns = columns;
        this.columnWidthUnits = columnWidthUnits;
        this.rowsHeightUnits = rowsHeightUnits;
        this.ranges = ranges != null ? ranges : new HashMap<>();
    }

    //getters
    public Map<String, Set<String>> getRanges() {
        return ranges;
    }

    public String getSheetName() {
        return sheetName;
    }

    public List<CellDTO> getCells() {
        return cells;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getRowHeight() {
        return rowsHeightUnits;
    }

    public int getColumnWidth() {
        return columnWidthUnits;
    }

    public String getCellTextColor(String cellId) {
        for (CellDTO cell : cells) {
            if (cell.getCellId().equals(cellId)) {
                return cell.getTextColor() != null ? cell.getTextColor() : "#000000"; // Default to black
            }
        }
        return "#000000"; // Default to black if no cell found
    }

    public String getCellBackgroundColor(String cellId) {
        for (CellDTO cell : cells) {
            if (cell.getCellId().equals(cellId)) {
                return cell.getBackgroundColor() != null ? cell.getBackgroundColor() : "#FFFFFF"; // Default to white
            }
        }
        return "#FFFFFF"; // Default to white if no cell found
    }

    public Set<String> getAllRangeNames() {
        return ranges.keySet();
    }

    public Set<String> getCellsInRange(String rangeName) {
        return ranges.getOrDefault(rangeName, new HashSet<>());
    }

    //Functions
    public void addRange(String rangeName, String fromCellId, String toCellId) {
        Set<String> cellsInRange = new HashSet<>(calculateRangeCells(fromCellId, toCellId));
        ranges.put(rangeName, cellsInRange);
    }

    public void removeRange(String rangeName) {
        ranges.remove(rangeName);
    }

    public boolean rangeExists(String rangeName) {
        return ranges.containsKey(rangeName);
    }

    public Set<String> calculateRangeCells(String fromCellId, String toCellId) {
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

    boolean isWithinBounds(String cellId) {
        int row = extractRow(cellId);
        int col = extractCol(cellId);
        return row > 0 && row <= rows && col > 0 && col <= columns;
    }

    private int extractRow(String cellId) {
        String rowPart = cellId.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowPart);
    }

    private int extractCol(String cellId) {
        String colPart = cellId.replaceAll("[0-9]", "").toUpperCase();
        return colPart.chars().reduce(0, (acc, ch) -> acc * 26 + (ch - 'A' + 1));
    }

    private String generateCellId(int row, String column) {
        return column.toUpperCase() + row;
    }

}
