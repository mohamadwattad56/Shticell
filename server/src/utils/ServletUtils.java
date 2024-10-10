package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import users.UserManager;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String SHEETCELL_MANAGER_ATTRIBUTE_NAME = "sheetCellManager";
    private static final String ENGINE_MANAGER_ATTRIBUTE_NAME = "engineManager";
    private static final String FILE_MANAGER_ATTRIBUTE_NAME = "fileManager";  // Add FileManager attribute name

    // Synchronization locks for the manager attributes
    private static final Object userManagerLock = new Object();
    private static final Object sheetCellManagerLock = new Object();
    private static final Object engineManagerLock = new Object();
    private static final Object fileManagerLock = new Object();  // Lock for FileManager

    // Fetch the UserManager and initialize it if it doesn't exist
    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    // Fetch the FileManager and initialize it if it doesn't exist
    public static FileManager getFileManager(ServletContext servletContext) {
        synchronized (fileManagerLock) {
            if (servletContext.getAttribute(FILE_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(FILE_MANAGER_ATTRIBUTE_NAME, new FileManager());
            }
        }
        return (FileManager) servletContext.getAttribute(FILE_MANAGER_ATTRIBUTE_NAME);
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
