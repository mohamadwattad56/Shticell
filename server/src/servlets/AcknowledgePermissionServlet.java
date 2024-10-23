package servlets;

import dto.PermissionRequestDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Spreadsheet.impl.SpreadsheetManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static constant.Constant.*;

@WebServlet("/acknowledgePermission")
public class AcknowledgePermissionServlet extends HttpServlet {

    // Add lock management for each sheet
    private final Map<String, ReadWriteLock> sheetLocks = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String sheetName = request.getParameter(SHEET_NAME);
        String username = request.getParameter(USERNAME);
        String decision = request.getParameter("decision");

        if (sheetName == null || username == null || decision == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        Map<String, SpreadsheetManager> spreadsheetManagerMap =
                (Map<String, SpreadsheetManager>) getServletContext().getAttribute(SPREADSHEET_MAP);

        SpreadsheetManager spreadsheetManager = spreadsheetManagerMap.get(sheetName);
        if (spreadsheetManager == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Spreadsheet not found");
            return;
        }

        // Retrieve lock for the specific sheet
        ReadWriteLock lock = sheetLocks.computeIfAbsent(sheetName, k -> new ReentrantReadWriteLock());

        // Acquire write lock for modification
        lock.writeLock().lock();
        try {
            if ("approve".equalsIgnoreCase(decision)) {
                PermissionRequestDTO requestDTO = spreadsheetManager.getPendingRequests().stream()
                        .filter(req -> req.getUsername().equals(username))
                        .findFirst()
                        .orElse(null);

                if (requestDTO != null) {
                    SpreadsheetManager.Permission permission = SpreadsheetManager.Permission.valueOf(requestDTO.getPermissionType().toUpperCase());
                    spreadsheetManager.approveRequest(username, permission);
                }
            } else if ("deny".equalsIgnoreCase(decision)) {
                spreadsheetManager.denyRequest(username);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Permission " + decision + " successfully.");
        } finally {
            lock.writeLock().unlock();  // Release lock
        }
    }
}

