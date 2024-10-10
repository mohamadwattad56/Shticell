package servlets;

import dto.PermissionRequestDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Spreadsheet.impl.SpreadsheetManager;
import java.io.IOException;
import java.util.Map;

@WebServlet("/requestPermission")
public class RequestPermissionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sheetName = request.getParameter("sheetName");
        String username = request.getParameter("username");
        String permissionType = request.getParameter("permissionType");

        if (sheetName == null || username == null || permissionType == null) {
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

        // Create a new permission request and add it to the sheet manager
        PermissionRequestDTO requestDTO = new PermissionRequestDTO(username, permissionType, false, spreadsheetManager.getSpreadsheetName());
        spreadsheetManager.addPendingRequest(requestDTO);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Permission request submitted.");
    }
}
