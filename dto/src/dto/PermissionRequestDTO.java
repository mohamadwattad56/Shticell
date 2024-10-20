package dto;

public class PermissionRequestDTO implements Cloneable {
    private String username;
    private final String permissionType;
    private RequestStatus requestStatus;
    private final String sheetName;

    public enum RequestStatus {
        PENDING, APPROVED,DENIED
    }

    //ctor
    public PermissionRequestDTO(String username, String permissionType, RequestStatus isApproved, String sheetName) {
        this.username = username;
        this.permissionType = permissionType;
        this.requestStatus = isApproved;
        this.sheetName = sheetName;

    }

    //getters
    public String getSheetName() {  // Add getter for sheetName
        return sheetName;
    }
    public String getUsername() {
        return username;
    }
    public String getPermissionType() {
        return permissionType;
    }
    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    //setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setRequestStatus(RequestStatus isApproved) {
        this.requestStatus = isApproved;
    }

    //functions
    @Override
    public PermissionRequestDTO clone() {
        try {
            return (PermissionRequestDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }

}
