package servlets;


import dto.PermissionRequestDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Spreadsheet.impl.SpreadsheetManager;
import java.io.IOException;
import java.util.Map;

@WebServlet("/acknowledgePermission")
public class AcknowledgePermissionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sheetName = request.getParameter("sheetName");
        String username = request.getParameter("username");
        String decision = request.getParameter("decision");

        if (sheetName == null || username == null || decision == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        // Retrieve the spreadsheet manager
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found");
            return;
        }

        // Grant or deny the permission based on the decision
        if ("approve".equalsIgnoreCase(decision)) {
            // Assuming you store the permission type in pending requests
            PermissionRequestDTO requestDTO = spreadsheetManager.getPendingRequests().stream()
                    .filter(req -> req.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (requestDTO != null) {
                // Here, use the fully qualified name for Permission enum
                SpreadsheetManager.Permission permission = SpreadsheetManager.Permission.valueOf(requestDTO.getPermissionType().toUpperCase());
                spreadsheetManager.approveRequest(username, permission);
            }
        } else {
            // Simply remove the pending request without granting permission
            spreadsheetManager.getPendingRequests().removeIf(req -> req.getUsername().equals(username));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Permission " + decision + " successfully.");
    }
}


