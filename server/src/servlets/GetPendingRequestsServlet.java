package servlets;
import Spreadsheet.impl.SpreadsheetManager;
import com.google.gson.Gson;
import dto.PermissionRequestDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static constant.Constant.SHEET_NAME;
import static constant.Constant.SPREADSHEET_MAP;

@WebServlet("/getPendingRequests")
public class GetPendingRequestsServlet extends HttpServlet {

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
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);

        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found");
            return;
        }

        List<PermissionRequestDTO> pendingRequests = spreadsheetManager.getPendingRequests();

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(pendingRequests);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
