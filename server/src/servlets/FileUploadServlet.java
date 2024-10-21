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

import static constant.Constant.SPREADSHEET_MAP;
import static constant.Constant.UPLOADER_MAP;

@WebServlet("/uploadFile")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Synchronize on the servlet context to prevent concurrent modification of the maps
            synchronized (getServletContext()) {
                // Initialize or retrieve the stored spreadsheet managers
                Map<String, SpreadsheetManager> spreadsheetManagerMap = (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);
                if (spreadsheetManagerMap == null) {
                    spreadsheetManagerMap = new HashMap<>();
                    getServletContext().setAttribute(SPREADSHEET_MAP, spreadsheetManagerMap);
                }

                // Initialize or retrieve the uploader map
                Map<String, String> uploaderMap = (Map<String, String>) getServletContext().getAttribute(UPLOADER_MAP);
                if (uploaderMap == null) {
                    uploaderMap = new HashMap<>();
                    getServletContext().setAttribute(UPLOADER_MAP, uploaderMap);
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
                try {
                    // Validate and load the spreadsheet (checks from Stage 1 and 2 happen here)
                    spreadsheetManager.loadSpreadsheet(filePath, uploaderName);
                    spreadsheetManager.initializeOwner(uploaderName);

                    // Check for unique sheet name
                    String sheetName = spreadsheetManager.getSpreadsheetName();
                    if (spreadsheetManagerMap.containsKey(sheetName)) {
                        // If sheet name already exists, send an error message
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sheet name '" + sheetName + "' already exists. Please choose a unique sheet.");
                        return;
                    }

                    // Store the SpreadsheetManager in the global map
                    spreadsheetManagerMap.put(sheetName, spreadsheetManager);
                    getServletContext().setAttribute(SPREADSHEET_MAP, spreadsheetManagerMap);

                    // Store the uploader name in the uploaderMap
                    uploaderMap.put(sheetName, uploaderName);
                    getServletContext().setAttribute(UPLOADER_MAP, uploaderMap);

                    // Convert the SpreadsheetManager to DTO for easier transmission
                    SpreadsheetManagerDTO spreadsheetManagerDTO = spreadsheetManager.toDTO(uploaderName);
                    spreadsheetManagerDTO.setInitialCellsModifiers(uploaderName);

                    // Respond with success and return the DTO (or just confirmation)
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("File uploaded successfully with sheet name: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());

                } catch (IllegalArgumentException e) {
                    // Handle validation exceptions from the loadSpreadsheet method
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error in uploaded file: " + e.getMessage());
                }
            }  // End of synchronized block
        } catch (Exception e) {
            // Handle any other exceptions
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing file: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}



