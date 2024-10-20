package servlets;
import Spreadsheet.impl.SpreadsheetManager;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static constant.Constant.SHEET_NAME;
import static constant.Constant.SPREADSHEET_MAP;

@WebServlet("/getVersionHistory")
public class VersionHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get spreadsheet name or ID from the request
        String sheetName = request.getParameter(SHEET_NAME);

        // Retrieve the spreadsheet map from ServletContext (as SpreadsheetManager)
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager != null) {
            // Get the version history from the spreadsheet manager
            List<String> versionHistory = spreadsheetManager.getVersionHistory();

            // Convert the version history to JSON
            Gson gson = new Gson();
            String jsonVersionHistory = gson.toJson(versionHistory);

            // Set response type to JSON and send the data
            response.setContentType("application/json");
            response.getWriter().write(jsonVersionHistory);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }
    }
}
