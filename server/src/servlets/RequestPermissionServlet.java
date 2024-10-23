package servlets;

import dto.PermissionRequestDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Spreadsheet.impl.SpreadsheetManager;
import java.io.IOException;
import java.util.Map;
import static constant.Constant.*;

@WebServlet("/requestPermission")
public class RequestPermissionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sheetName = request.getParameter(SHEET_NAME);
        String username = request.getParameter(USERNAME);
        String permissionType = request.getParameter("permissionType");

        if (sheetName == null || username == null || permissionType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        // Retrieve the spreadsheet manager
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found");
            return;
        }

        // Check if the user already has permission
        boolean alreadyHasPermission = spreadsheetManager.getProcessedRequests().stream()
                .anyMatch(req -> req.getUsername().equals(username) && req.getPermissionType().equalsIgnoreCase(permissionType));

        if (alreadyHasPermission) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Owner already Approved/Denied requested permission.");
            return;
        }

        boolean requestIsPending = spreadsheetManager.getPendingRequests().stream()
                .anyMatch(req -> req.getUsername().equals(username) && req.getPermissionType().equalsIgnoreCase(permissionType));

        if (requestIsPending) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User already requested permission.");
            return;
        }

        // Create a new pending permission request and add it to the spreadsheet manager
        PermissionRequestDTO requestDTO = new PermissionRequestDTO(username, permissionType, PermissionRequestDTO.RequestStatus.PENDING, spreadsheetManager.getSpreadsheetName());
        spreadsheetManager.addPendingRequest(requestDTO);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Permission request submitted.");
    }
}
