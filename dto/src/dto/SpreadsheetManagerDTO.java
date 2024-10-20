package dto;
import cell.impl.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SpreadsheetManagerDTO {
    private final SpreadsheetDTO spreadsheetDTO;
    private final List<VersionDTO> versionHistory;
    private final boolean isSheetLoaded;
    private  int currentVersion;
    private final String uploaderName;
    private String currentUserName;





    //ctor
    public SpreadsheetManagerDTO(SpreadsheetDTO spreadsheetDTO, List<VersionDTO> versionHistory, boolean isSheetLoaded, int currentVersion, String uploaderName) {
        this.spreadsheetDTO = spreadsheetDTO;
        this.versionHistory = versionHistory;
        this.isSheetLoaded = isSheetLoaded;
        this.currentVersion = currentVersion;
        this.uploaderName = uploaderName;
    }

    //setters
    public void setCellTextColor(String cellId, Color color) {
        // Find the matching cell in the DTO and update its text color
        for (CellDTO cell : spreadsheetDTO.getCells()) {
            if (cell.getCellId().equals(cellId)) {
                cell.setTextColor(toRgbCode(color)); // Update the text color in the DTO
            }
        }
    }

    public void setCellBackgroundColor(String cellId, Color color) {
        // Find the matching cell in the DTO and update its background color
        for (CellDTO cell : spreadsheetDTO.getCells()) {
            if (cell.getCellId().equals(cellId)) {
                cell.setBackgroundColor(toRgbCode(color)); // Update the background color in the DTO
            }
        }
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }
    //getters

    public String getCurrentUserName() {
        return currentUserName;
    }

    public SpreadsheetDTO getSpreadsheetDTO() {
        return spreadsheetDTO;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getCellTextColor(String cellId) {
        return spreadsheetDTO.getCellTextColor(cellId);
    }

    public String getCellBackgroundColor(String cellId) {
        return spreadsheetDTO.getCellBackgroundColor(cellId);
    }

    public CellDTO getCellDTO(String cellId) {
        for (CellDTO cell : spreadsheetDTO.getCells()) {
            if (cell.getCellId().equals(cellId)) {
                return cell;
            }
        }
        return new CellDTO(cellId, "", "EMPTY", CellType.EMPTY, 0, new ArrayList<>(), new ArrayList<>(), "black", "white");
    }

    public int getNumOfRows() {
        return spreadsheetDTO.getRows();
    }

    public int getNumOfColumns() {
        return spreadsheetDTO.getColumns();
    }

    public Set<String> getAllRangeNames() {
        return spreadsheetDTO.getAllRangeNames();
    }

    //Functions
    public boolean addRange(String rangeName, String fromCellId, String toCellId) {
        // Check if the range already exists
        if (spreadsheetDTO.rangeExists(rangeName)) {
            return false; // Range with the same name already exists
        }

        // Add the range in the DTO
        spreadsheetDTO.addRange(rangeName, fromCellId, toCellId);
        return true;
    }

    public boolean deleteRange(String rangeName) {
        if (spreadsheetDTO.rangeExists(rangeName)) {
            // Ensure no cells depend on this range before deleting
            if (isRangeUsedInCells(rangeName)) {
                throw new IllegalArgumentException("Range '" + rangeName + "' is used by one or more cells and cannot be deleted.");
            }

            // Remove the range
            spreadsheetDTO.removeRange(rangeName);
            return true;
        }
        return false; // Range not found
    }

    public Set<String> getRangeCells(String rangeName) {
        return spreadsheetDTO.getCellsInRange(rangeName);
    }

    private boolean isRangeUsedInCells(String rangeName) {
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

    private String toRgbCode(Color color) {
        return String.format("#%02X%02X%02X",
                 (color.getRed() * 255),
                 (color.getGreen() * 255),
                 (color.getBlue() * 255));
    }

    public void setInitialCellsModifiers(String uploaderName) {
        for (CellDTO cell : spreadsheetDTO.getCells()) {
            if (!cell.getSourceValue().equals("EMPTY")) {
                cell.setLastModifiedBy(uploaderName);
            }
        }
    }

    @Override
    public SpreadsheetManagerDTO clone() {
        // Clone the spreadsheetDTO (assuming SpreadsheetDTO also has a proper clone or copy constructor)
        SpreadsheetDTO clonedSpreadsheetDTO = new SpreadsheetDTO(
                this.spreadsheetDTO.getSheetName(),
                new ArrayList<>(this.spreadsheetDTO.getCells()),  // Deep copy of the list of cells
                this.spreadsheetDTO.getRows(),
                this.spreadsheetDTO.getColumns(),
                this.spreadsheetDTO.getColumnWidth(),
                this.spreadsheetDTO.getRowHeight(),
                new HashMap<>(this.spreadsheetDTO.getRanges())     // Deep copy of ranges map
        );

        // Clone the version history (deep copy of each VersionDTO if needed)
        List<VersionDTO> clonedVersionHistory = new ArrayList<>();
        for (VersionDTO version : this.versionHistory) {
            clonedVersionHistory.add(new VersionDTO(
                    version.getVersionNumber(),
                    version.getSpreadsheetDTO(),  // Ensure that the spreadsheetDTO in VersionDTO is deeply copied if necessary
                    version.getChangedCellsCount()
            ));
        }

        // Create and return the deep copy of SpreadsheetManagerDTO
        return new SpreadsheetManagerDTO(
                clonedSpreadsheetDTO,
                clonedVersionHistory,
                this.isSheetLoaded,
                this.currentVersion,
                this.uploaderName
        );
    }


}