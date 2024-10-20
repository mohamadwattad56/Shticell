package utils;

import chat.chat.ChatManager;
import jakarta.servlet.ServletContext;
import chat.users.UserManager;
import jakarta.servlet.http.HttpServletRequest;

import static constant.Constant.INT_PARAMETER_ERROR;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";

    // Synchronization locks for the manager attributes
    private static final Object userManagerLock = new Object();
    private static final Object chatManagerLock = new Object();
    private static final String CHAT_MANAGER_ATTRIBUTE_NAME = "chatManager";




    // Fetch the UserManager and initialize it if it doesn't exist
    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }



    public static ChatManager getChatManager(ServletContext servletContext) {
        synchronized (chatManagerLock) {
            if (servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(CHAT_MANAGER_ATTRIBUTE_NAME, new ChatManager());
            }
        }
        return (ChatManager) servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME);
    }

    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return INT_PARAMETER_ERROR;
    }






    // Uncommented parts for future use if needed

    //    // Fetch the SheetCellManager and initialize it if it doesn't exist
    //    public static SheetCellManager getSheetCellManager(ServletContext servletContext) {
    //        synchronized (sheetCellManagerLock) {
    //            if (servletContext.getAttribute(SHEETCELL_MANAGER_ATTRIBUTE_NAME) == null) {
    //                servletContext.setAttribute(SHEETCELL_MANAGER_ATTRIBUTE_NAME, new SheetCellManager());
    //            }
    //        }
    //        return (SheetCellManager) servletContext.getAttribute(SHEETCELL_MANAGER_ATTRIBUTE_NAME);
    //    }
    //
    //    // Fetch the EngineManager and initialize it if it doesn't exist
    //    public static EngineManager getEngineManager(ServletContext servletContext) {
    //        synchronized (engineManagerLock) {
    //            if (servletContext.getAttribute(ENGINE_MANAGER_ATTRIBUTE_NAME) == null) {
    //                servletContext.setAttribute(ENGINE_MANAGER_ATTRIBUTE_NAME, new EngineManager());
    //            }
    //        }
    //        return (EngineManager) servletContext.getAttribute(ENGINE_MANAGER_ATTRIBUTE_NAME);
    //    }
    //
    //    // Utility to get an integer parameter from the request, returning a default error value if the parameter is invalid
    //    public static int getIntParameter(HttpServletRequest request, String name) {
    //        String value = request.getParameter(name);
    //        if (value != null) {
    //            try {
    //                return Integer.parseInt(value);
    //            } catch (NumberFormatException numberFormatException) {
    //                // Log the exception if necessary, keeping it silent for now
    //            }
    //        }
    //        return INT_PARAMETER_ERROR;  // Error constant for invalid integers
    //    }





}
