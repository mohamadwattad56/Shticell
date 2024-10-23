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
import static constant.Constant.SPREADSHEET_MAP;

@WebServlet("/deleteRange")
public class DeleteRangeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rangeName = request.getParameter("rangeName");
        String sheetName = request.getParameter(SHEET_NAME);
        if (rangeName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        // Retrieve spreadsheet manager (from the session or context)
        Map<String, SpreadsheetManager> spreadsheetManagerMap = (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);
        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);  // Adjust as needed

        try{
            boolean deleted = spreadsheetManager.deleteRange(rangeName);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Range deleted successfully");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Range not found");
            }
        }catch (Exception e){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

