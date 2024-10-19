package dto;

public class PermissionRequestDTO implements Cloneable {
    private String username;
    private String permissionType;
    private RequestStatus requestStatus;
    private final String sheetName;

    public enum RequestStatus {
        PENDING, APPROVED,DENIED
    }

    @Override
    public PermissionRequestDTO clone() {
        try {
            return (PermissionRequestDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
    public PermissionRequestDTO(String username, String permissionType, RequestStatus isApproved, String sheetName) {
        this.username = username;
        this.permissionType = permissionType;
        this.requestStatus = isApproved;
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
    public RequestStatus getRequestStatus() {
        return requestStatus;
    }
    public void setRequestStatus(RequestStatus isApproved) {
        this.requestStatus = isApproved;
    }

}
