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

@WebServlet("/getCellUpdate")
public class GetCellUpdateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the cell identifier and sheet name from the request parameters
        String cellId = request.getParameter("cellId");
        String sheetName = request.getParameter("sheetName");
        String userName = request.getParameter("userName"); // User who is viewing the sheet

        // Fetch the spreadsheet manager map from the servlet context
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");

        // Retrieve the correct SpreadsheetManager instance for the specified sheet
        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager != null) {
            try {
                // Generate a CellUpdateDTO containing the updated cell and its dependents
                CellUpdateDTO cellUpdateDTO = spreadsheetManager.generateCellUpdateDTO(cellId, userName);

                // Use GsonBuilder to handle special floating-point values like NaN
                Gson gson = new GsonBuilder()
                        .serializeSpecialFloatingPointValues() // Allow NaN, Infinity, etc.
                        .create();

                // Convert the DTO to JSON and send it in the response
                String jsonResponse = gson.toJson(cellUpdateDTO);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error generating cell update: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Spreadsheet not found.");
        }
    }
}
