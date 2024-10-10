package servlets;

import dto.SpreadsheetManagerDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Spreadsheet.impl.SpreadsheetManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/uploadFile")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Initialize or retrieve the stored spreadsheet managers
            Map<String, SpreadsheetManager> spreadsheetManagerMap = (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");
            if (spreadsheetManagerMap == null) {
                spreadsheetManagerMap = new HashMap<>();
                getServletContext().setAttribute("spreadsheetManagerMap", spreadsheetManagerMap);
            }

            // Initialize or retrieve the uploader map
            Map<String, String> uploaderMap = (Map<String, String>) getServletContext().getAttribute("uploaderMap");
            if (uploaderMap == null) {
                uploaderMap = new HashMap<>();
                getServletContext().setAttribute("uploaderMap", uploaderMap);
            }

            // Get the file path and uploader name from the request
            String filePath = request.getParameter("filePath");
            String uploaderName = request.getParameter("uploaderName");
            if (filePath == null || filePath.isEmpty() || uploaderName == null || uploaderName.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File path or uploader name is missing.");
                return;
            }

            // Load the spreadsheet using the SpreadsheetManager class
            SpreadsheetManager spreadsheetManager = new SpreadsheetManager();
            spreadsheetManager.loadSpreadsheet(filePath);  // Load spreadsheet from file path

            // Store the SpreadsheetManager in the global map
            String sheetName = spreadsheetManager.getSpreadsheetName();
            spreadsheetManagerMap.put(sheetName, spreadsheetManager);
            getServletContext().setAttribute("spreadsheetManagerMap", spreadsheetManagerMap);

            // Store the uploader name in the uploaderMap
            uploaderMap.put(sheetName, uploaderName);
            getServletContext().setAttribute("uploaderMap", uploaderMap);

            // Convert the SpreadsheetManager to DTO for easier transmission
            SpreadsheetManagerDTO spreadsheetManagerDTO = spreadsheetManager.toDTO(uploaderName);

            // Respond with success and return the DTO (or just confirmation)
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("File uploaded successfully with sheet name: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing file: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}

