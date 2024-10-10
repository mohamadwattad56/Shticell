package servlets;

import Spreadsheet.impl.SpreadsheetManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/getLatestVersion")
public class GetLatestVersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sheetName = request.getParameter("sheetName");

        // Retrieve the spreadsheet manager map
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager != null) {
            int latestVersion = spreadsheetManager.getCurrentVersion();  // Get the current/latest version
            response.setContentType("text/plain");
            response.getWriter().write(String.valueOf(latestVersion));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }
    }
}
