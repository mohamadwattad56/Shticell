package dto;

public class PermissionRequestDTO {
    private String username;
    private String permissionType;
    private boolean isApproved;
    private String sheetName;  // Add this field

    public PermissionRequestDTO(String username, String permissionType, boolean isApproved, String sheetName) {
        this.username = username;
        this.permissionType = permissionType;
        this.isApproved = isApproved;
        this.sheetName = sheetName;

    }
    public String getSheetName() {  // Add getter for sheetName
        return sheetName;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPermissionType() {
        return permissionType;
    }
    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }
    public boolean isApproved() {
        return isApproved;
    }
    public void setApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

}
