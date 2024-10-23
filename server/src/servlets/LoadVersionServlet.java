package servlets;

import Spreadsheet.impl.SpreadsheetManager;
import com.google.gson.Gson;
import dto.VersionDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

import static constant.Constant.SHEET_NAME;
import static constant.Constant.SPREADSHEET_MAP;

@WebServlet("/getVersion")
public class LoadVersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Retrieve the sheet name and version number from the request
        String sheetName = request.getParameter(SHEET_NAME);
        String versionNumberStr = request.getParameter("versionNumber");

        // Basic input validation
        if (sheetName == null || sheetName.isEmpty() || versionNumberStr == null || versionNumberStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing sheetName or versionNumber.");
            return;
        }

        int versionNumber;
        try {
            versionNumber = Integer.parseInt(versionNumberStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid version number format.");
            return;
        }

        // Retrieve the spreadsheet map from the context (as SpreadsheetManager)
        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        if (spreadsheetManagerMap == null || spreadsheetManagerMap.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("No spreadsheets found.");
            return;
        }

        // Fetch the specific spreadsheet manager
        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);
        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found");
            return;
        }
        synchronized (spreadsheetManager) {
            // Get the specific version from the spreadsheet manager
            VersionDTO versionDTO = spreadsheetManager.getVersionDTO(versionNumber);

            if (versionDTO != null) {
                // Convert the versionDTO to JSON and send it in the response
                Gson gson = new Gson();
                String jsonVersionDTO = gson.toJson(versionDTO);

                response.setContentType("application/json");
                response.getWriter().write(jsonVersionDTO);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Version not found for the spreadsheet.");
            }
        }
    }
}

