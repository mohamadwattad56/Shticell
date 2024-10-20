package servlets;
import Spreadsheet.impl.SpreadsheetManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.CellUpdateDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import static constant.Constant.*;

@WebServlet("/updateCellValue")
public class UpdateCellValueServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the query parameters from the request
        String cellIdentifier = request.getParameter(CELL_ID);
        String newValue = request.getParameter("newValue");
        String oldValue = request.getParameter("oldValue");
        String sheetName = request.getParameter(SHEET_NAME);
        String versionNumberStr = request.getParameter("versionNumber");
        String userName = request.getParameter(USERNAME);  // Retrieve the userName who made the change

        // Check if any required parameter is missing
        if (cellIdentifier == null || newValue == null || oldValue == null || sheetName == null || versionNumberStr == null || userName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        // Parse the version number from the request
        int userVersion;
        try {
            userVersion = Integer.parseInt(versionNumberStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid version number format.");
            return;
        }

        // Retrieve the spreadsheet manager from ServletContext
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager != null) {
            int latestVersion = spreadsheetManager.getCurrentVersion();

            // Check if the user's version is outdated
            if (userVersion < latestVersion) {
                // Version conflict, reject the update
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Version conflict: Please update to the latest version before editing.");
                return;
            }

            try {
                // Update the cell value and pass the current user who made the change
                spreadsheetManager.updateCellValue(cellIdentifier, newValue, oldValue, userName, true);

                spreadsheetManager.setLastModifiedBy(cellIdentifier,userName);
                // Generate the updated cell DTO with the last modified user
                CellUpdateDTO cellUpdateDTO = spreadsheetManager.generateCellUpdateDTO(cellIdentifier, userName);

                // Convert response to JSON and send it back to the client
                Gson gson = new GsonBuilder()
                        .serializeSpecialFloatingPointValues() // This allows NaN and other special values
                        .create();

                String jsonResponse = gson.toJson(cellUpdateDTO);
                response.setContentType("application/json");
                response.getWriter().write(jsonResponse);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Error: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }
    }
}
