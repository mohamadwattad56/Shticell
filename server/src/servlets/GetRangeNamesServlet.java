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
import java.util.Set;

import static constant.Constant.SHEET_NAME;

@WebServlet("/getRangeNames")
public class GetRangeNamesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sheetName = request.getParameter(SHEET_NAME);

        if (sheetName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing sheet name");
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

        // Get range names from the spreadsheet manager
        Set<String> rangeNames = spreadsheetManager.getRangeNames();  // Assume this method is available

        // Send the list as a JSON response
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(rangeNames);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
