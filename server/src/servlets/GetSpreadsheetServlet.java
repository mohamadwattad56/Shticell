package servlets;

import com.google.gson.GsonBuilder;
import dto.SpreadsheetManagerDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import Spreadsheet.impl.SpreadsheetManager;

import java.io.IOException;
import java.util.Map;

import static constant.Constant.*;

@WebServlet("/getSpreadsheet")
public class GetSpreadsheetServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sheetName = request.getParameter(SHEET_NAME);
        String userName = request.getParameter(USERNAME); // The user requesting the sheet

        // Retrieve the spreadsheet manager map
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        // Retrieve the uploader map
        Map<String, String> uploaderMap = (Map<String, String>) getServletContext().getAttribute(UPLOADER_MAP);

        if (spreadsheetManagerMap == null || spreadsheetManagerMap.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No sheets found.");
            return;
        }

        // Find the relevant SpreadsheetManager
        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found.");
            return;
        }

        // Retrieve the uploader name from the uploaderMap
        String uploaderName = uploaderMap != null ? uploaderMap.get(sheetName) : "";

        // Convert SpreadsheetManager to SpreadsheetManagerDTO and pass uploaderName
        SpreadsheetManagerDTO spreadsheetManagerDTO = spreadsheetManager.toDTO(uploaderName);
        spreadsheetManagerDTO.setCurrentUserName(userName);

        // Send the SpreadsheetManagerDTO as JSON
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues() // Allow NaN, Infinity, etc.
                .create();
        String jsonResponse = gson.toJson(spreadsheetManagerDTO);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}

