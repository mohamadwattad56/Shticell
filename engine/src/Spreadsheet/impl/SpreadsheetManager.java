package Spreadsheet.impl;
import Spreadsheet.api.Engine;
import cell.impl.*;
import dto.*;
import exception.CircularDependencyException;
import exception.EmptyCellException;
import exception.OutOfBoundsException;
import exception.RangeDoesNotExist;
import java.util.*;
import java.util.stream.Collectors;

public class SpreadsheetManager implements Engine, Cloneable {
    private final Spreadsheet currentSpreadsheet;
    private final List<Version> versionHistory = new ArrayList<>();
    private boolean isSheetLoaded = false;
    private int currentVersion = 1;
    private final Map<String, Permission> userPermissions = new HashMap<>();  // Map of usernames to their permissions
    private final List<PermissionRequestDTO> processedRequests = new ArrayList<>();  // Processed (approved/denied) requests
    private final List<PermissionRequestDTO> pendingRequests = new ArrayList<>();  // Pending permission requests

    public Set<String> getRangeNames() {
        return currentSpreadsheet.getAllRangeNames();
    }

    public enum Permission {
        OWNER, READER, WRITER, NONE

    }

    //Ctor
    private SpreadsheetManager(Spreadsheet clonedSpreadsheet, List<Version> clonedVersionHistory, boolean isSheetLoaded, int currentVersion, Map<String, Permission> clonedUserPermissions, List<PermissionRequestDTO> clonedProcessedRequests, List<PermissionRequestDTO> clonedPendingRequests) {
        this.currentSpreadsheet = clonedSpreadsheet;  // Assign the cloned Spreadsheet
        this.versionHistory.addAll(clonedVersionHistory);
        this.isSheetLoaded = isSheetLoaded;
        this.currentVersion = currentVersion;
        this.userPermissions.putAll(clonedUserPermissions);
        this.processedRequests.addAll(clonedProcessedRequests);
        this.pendingRequests.addAll(clonedPendingRequests);
    }

    //Setters
    public void setLastModifiedBy(String cellId, String lastModifiedBy) {
        this.currentSpreadsheet.getCellById(cellId).setLastModifiedBy(lastModifiedBy);
    }

    //Getters
    public Permission getUserPermission(String username) {
        return userPermissions.getOrDefault(username, Permission.NONE);
    }

    public List<PermissionRequestDTO> getProcessedRequests() {
        return new ArrayList<>(processedRequests);  // Return a copy of the processed requests
    }

    public List<PermissionRequestDTO> getPendingRequests() {
        return new ArrayList<>(pendingRequests);  // Return a copy of the list
    }

    public CellDTO getCellDTO(String cellId) {
        return currentSpreadsheet.getCellDTO(cellId);
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
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String getSpreadsheetName() {
        return currentSpreadsheet.sheet.getName();
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    //Functions
    @Override
    public SpreadsheetManager clone() {
        // Create deep copies of the necessary fields
        Spreadsheet clonedSpreadsheet = this.currentSpreadsheet.clone();
        List<Version> clonedVersionHistory = new ArrayList<>();
        for (Version version : this.versionHistory) {
            clonedVersionHistory.add(version.clone());
        }

        Map<String, Permission> clonedUserPermissions = new HashMap<>(this.userPermissions);

        List<PermissionRequestDTO> clonedProcessedRequests = new ArrayList<>();
        for (PermissionRequestDTO request : this.processedRequests) {
            clonedProcessedRequests.add(request.clone());
        }

        List<PermissionRequestDTO> clonedPendingRequests = new ArrayList<>();
        for (PermissionRequestDTO request : this.pendingRequests) {
            clonedPendingRequests.add(request.clone());
        }

        // Return a new instance using the private constructor
        return new SpreadsheetManager(
                clonedSpreadsheet,
                clonedVersionHistory,
                this.isSheetLoaded,
                this.currentVersion,
                clonedUserPermissions,
                clonedProcessedRequests,
                clonedPendingRequests
        );

    }

    public void initializeOwner(String ownerUsername) {
        userPermissions.put(ownerUsername, Permission.OWNER);
    }

    public void addPendingRequest(PermissionRequestDTO request) {
        pendingRequests.add(request);
    }

    public void approveRequest(String username, Permission permission) {
        for (PermissionRequestDTO request : pendingRequests) {
            if (request.getUsername().equals(username)) {
                request.setRequestStatus(PermissionRequestDTO.RequestStatus.APPROVED);  // Mark as approved
                userPermissions.put(username, permission);  // Grant permission
                processedRequests.add(request);  // Move to processed list
                pendingRequests.remove(request);  // Remove from pending list
                break;
            }
        }
    }

    public void denyRequest(String username) {
        for (PermissionRequestDTO request : pendingRequests) {
            if (request.getUsername().equals(username)) {
                request.setRequestStatus(PermissionRequestDTO.RequestStatus.DENIED);  // Mark as approved
                processedRequests.add(request);  // Move to processed list
                pendingRequests.remove(request);  // Remove from pending list
                break;
            }
        }
    }

    public SpreadsheetManager() {
        this.currentSpreadsheet = new Spreadsheet();

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

    public void loadSpreadsheet(String filePath, String uploaderName) {
        try {
            // Load spreadsheet from XML
            currentSpreadsheet.loadFromXml(filePath, uploaderName);
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
        } catch (Exception e) {
            isSheetLoaded = false; // Set to false if there was an error
            throw new IllegalArgumentException("Error loading spreadsheet: " + e.getMessage(), e);
        }
    }

    public boolean addRange(String rangeName, String fromCellId, String toCellId) {
        if (currentSpreadsheet.getAllRangeNames().contains(rangeName)) {
            return false; // Range with the same name already exists
        }

        Set<String> cellsInRange = new HashSet<>(calculateRangeCells(fromCellId, toCellId));
        currentSpreadsheet.getRanges().put(rangeName, cellsInRange);
        return true;
    }

    public boolean deleteRange(String rangeName) {
        if (currentSpreadsheet.getAllRangeNames().contains(rangeName)) {
            // Ensure no cells depend on this range before deleting
            if (currentSpreadsheet.isRangeUsedInCells(rangeName)) {
                throw new IllegalArgumentException("Range '" + rangeName + "' is used by one or more cells and cannot be deleted.");
            }

            // Remove the range
            currentSpreadsheet.removeRange(rangeName);
            return true;
        }
        return false; // Range not found
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

    private String generateCellId(int row, String column) {
        return column.toUpperCase() + row;
    }

    private int extractRow(String cellId) {
        String rowPart = cellId.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowPart);
    }

    private int extractCol(String cellId) {
        String colPart = cellId.replaceAll("[0-9]", "").toUpperCase();
        return colPart.chars().reduce(0, (acc, ch) -> acc * 26 + (ch - 'A' + 1));
    }

    private void checkUniqueRangeNames() {
        Set<String> rangeNames = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : currentSpreadsheet.getRanges().entrySet()) {
            String rangeName = entry.getKey();
            if (!rangeNames.add(rangeName)) {
                throw new IllegalArgumentException("Range name " + rangeName + " is not unique.");
            }
        }
    }

    private void checkRangesWithinBounds() {
        for (Map.Entry<String, Set<String>> entry : currentSpreadsheet.getRanges().entrySet()) {
            for (String cellId : entry.getValue()) {
                if (!isWithinBounds(cellId)) {
                    throw new OutOfBoundsException(String.format("Range '%s' includes out-of-bounds cell: %s", entry.getKey(), cellId));
                }
            }
        }
    }

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

    private String[] extractArguments(String functionValue) {
        String argsString = functionValue.substring(functionValue.indexOf(',') + 1, functionValue.length() - 1);
        return argsString.split(",");
    }

    private boolean isRange(String value) {
        return value.matches("[A-Z]+[0-9]+:[A-Z]+[0-9]+");
    }

    public Set<String> getCellIdsInRange(String rangeName){
       return currentSpreadsheet.getCellIdsInRange(rangeName);
    }

    private boolean isWithinBounds(String cellId) {
        return currentSpreadsheet.isWithinBounds(cellId); // Call to Spreadsheet class to check bounds
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


        } catch (IllegalArgumentException | CircularDependencyException e) {
            currentSpreadsheet.setCellValue(cellId, oldValue, currentVersion);
            throw new IllegalArgumentException(e.getMessage());
        } catch (EmptyCellException e) {
            throw new EmptyCellException(e.getMessage());
        }
    }
}
