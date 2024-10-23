package servlets;

import Spreadsheet.impl.SpreadsheetManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static constant.Constant.SHEET_NAME;

@WebServlet("/addRange")
public class AddRangeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rangeName = request.getParameter("rangeName");
        String fromCellId = request.getParameter("fromCellId");
        String toCellId = request.getParameter("toCellId");
        String sheetName = request.getParameter(SHEET_NAME);

        if (rangeName == null || fromCellId == null || toCellId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        // Retrieve spreadsheet manager (from the session or context)
        Map<String, SpreadsheetManager> spreadsheetManagerMap = (Map<String, SpreadsheetManager>) getServletContext().getAttribute("spreadsheetManagerMap");
        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);  // Adjust as needed\
        synchronized (spreadsheetManager) {
            boolean added = spreadsheetManager.addRange(rangeName, fromCellId, toCellId);
            if (added) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Range added successfully");
            } else {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Range name already exists");
            }
        }
    }
}

