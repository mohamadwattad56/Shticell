package servlets;
import Spreadsheet.impl.SpreadsheetManager;
import cell.impl.CellImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import static constant.Constant.SHEET_NAME;
import static constant.Constant.SPREADSHEET_MAP;

@WebServlet("/calculateLivePreview")
public class LivePreviewServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Get the function string from the request body
            String functionString = request.getReader().readLine();

            // Fetch the SpreadsheetManager from the global map (instead of session)
            Map<String, SpreadsheetManager> spreadsheetManagerMap =
                    (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

            String sheetName = request.getParameter(SHEET_NAME);  // Get the sheet name from request
            SpreadsheetManager manager = spreadsheetManagerMap.get(sheetName); // Retrieve the manager by sheet name

            if (manager == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No SpreadsheetManager found for the given sheet.");
                return;
            }

            // Create and evaluate the function
            CellImpl cell = manager.createCellBasedOnValue(functionString);
            Object result = cell.evaluate();

            // Send the result back to the client
            response.setContentType("text/plain");
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error calculating live preview: " + e.getMessage());
        }
    }
}
