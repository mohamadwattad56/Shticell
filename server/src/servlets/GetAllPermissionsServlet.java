package servlets;


import Spreadsheet.impl.SpreadsheetManager;
import com.google.gson.Gson;
import dto.PermissionRequestDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/getAllPermissions")
public class GetAllPermissionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sheetName = request.getParameter("sheetName");

        if (sheetName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing sheet name");
            return;
        }

        // Retrieve the spreadsheet manager map
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found");
            return;
        }

        // Get all permissions for the sheet (including the owner, approved, and pending requests)
        List<PermissionRequestDTO> allPermissions = spreadsheetManager.getProcessedRequests();  // Add this method in SpreadsheetManager
        allPermissions.addAll(spreadsheetManager.getPendingRequests());

        // Send the list as a JSON response
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(allPermissions);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
