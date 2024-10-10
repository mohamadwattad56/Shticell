package servlets;

import com.google.gson.Gson;
import dto.SpreadsheetManagerDTO;
import Spreadsheet.impl.SpreadsheetManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/getUploadedFiles")
public class GetUploadedFilesServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve the spreadsheet manager map (now storing SpreadsheetManager objects)
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");

        // Retrieve the uploader map
        Map<String, String> uploaderMap = (Map<String, String>) getServletContext().getAttribute("uploaderMap");

        if (spreadsheetManagerMap == null || spreadsheetManagerMap.isEmpty()) {
            response.getWriter().write("{}");  // Return an empty JSON object if no files
            return;
        }

        // Convert each SpreadsheetManager to SpreadsheetManagerDTO before sending
        Map<String, SpreadsheetManagerDTO> spreadsheetManagerDTOMap = new HashMap<>();
        spreadsheetManagerMap.forEach((sheetName, spreadsheetManager) -> {
            String uploaderName = uploaderMap != null ? uploaderMap.get(sheetName) : "";  // Get the uploader name from the map
            spreadsheetManagerDTOMap.put(sheetName, spreadsheetManager.toDTO(uploaderName));
        });

        // Use GSON to serialize the map into JSON format
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(spreadsheetManagerDTOMap);

        // Set response headers and send the response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
