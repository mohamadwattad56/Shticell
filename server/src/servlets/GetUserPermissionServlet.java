package servlets;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Spreadsheet.impl.SpreadsheetManager;
import java.io.IOException;
import java.util.Map;

@WebServlet("/getUserPermission")
public class GetUserPermissionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String sheetName = request.getParameter("sheetName");
        String username = request.getParameter("username");

        if (sheetName == null || username == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        // Get the spreadsheet manager and user permission
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");
        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found.");
            return;
        }

        // Retrieve the user's permission
        String permission = spreadsheetManager.getUserPermission(username).name();
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(permission);
    }
}
