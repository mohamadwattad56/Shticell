package utils;

import constant.Constant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {
    // Retrieves the username from the session
    public static String getUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constant.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }
}