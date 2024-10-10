/*
package Permission;

import dto.PermissionRequestDTO;

import java.util.*;

public class PermissionManager {
    private static final Map<String, List<PermissionRequestDTO>> permissionRequests = new HashMap<>();

    // Add a new permission request for a specific sheet
    public static void addPermissionRequest(String sheetName, PermissionRequestDTO request) {
        permissionRequests.computeIfAbsent(sheetName, k -> new ArrayList<>()).add(request);
    }

    // Get all pending permission requests for a specific sheet
    public static List<PermissionRequestDTO> getPendingRequests(String sheetName) {
        return permissionRequests.getOrDefault(sheetName, new ArrayList<>());
    }

    // Approve or deny a permission request
    public static void acknowledgePermission(String sheetName, String username, String decision) {
        List<PermissionRequestDTO> requests = permissionRequests.get(sheetName);
        if (requests != null) {
            for (PermissionRequestDTO request : requests) {
                if (request.getUsername().equals(username)) {
                    request.setApproved("approve".equalsIgnoreCase(decision));
                }
            }
        }
    }

    // Get permission status for a specific user
    public static PermissionRequestDTO getUserPermission(String sheetName, String username) {
        List<PermissionRequestDTO> requests = permissionRequests.get(sheetName);
        if (requests != null) {
            for (PermissionRequestDTO request : requests) {
                if (request.getUsername().equals(username)) {
                    return request;
                }
            }
        }
        return null;
    }
}
*/
