package httputils;


import com.google.gson.Gson;

public class Constants {

    // Global constants
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String DEFAULT_USER = "<Anonymous>";
    public static final int REFRESH_RATE = 2000;  // Adjust if necessary
    public static final String DATE_TIME_FORMAT = "%tH:%tM:%tS | %.10s: %s%n";

    // FXML file locations for UI components
    public static final String DASHBOARD_HEADER_FXML_RESOURCE_LOCATION = "/dashboard/dashboardHeader/dashHeader.fxml";
    public static final String DASHBOARD_TABLES_FXML_RESOURCE_LOCATION = "/dashboard/dashboardTables/dashTables.fxml";
    public static final String DASHBOARD_COMMANDS_FXML_RESOURCE_LOCATION = "/dashboard/dashboardCommands/dashCommands.fxml";
    public static final String SHEET_MAIN_LAYOUT_FXML_RESOURCE_LOCATION = "/gridPageController/mainController/MainLayout.fxml";


    // Server resources locations (URLs)
    public static final String BASE_DOMAIN = "localhost";
    private static final String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private static final String CONTEXT_PATH = "/server_Web";  // Your application context path
    private static final String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    // API Endpoints
    public static final String LOGIN_PAGE = FULL_SERVER_PATH + "/login";  // Login endpoint
    public static final String UPLOAD_FILE = FULL_SERVER_PATH + "/getUploadedFiles";
    public static final String USERS_LIST = FULL_SERVER_PATH + "/userslist";  // Endpoint to fetch user list
    public static final String REMOVE_USER = FULL_SERVER_PATH + "/removeuser";  // Endpoint to fetch user list

    public static final String REQUEST_PERMISSION = FULL_SERVER_PATH + "/requestPermission";  // Logout endpoint
    public static final String USER_PERMISSION = FULL_SERVER_PATH + "/getUserPermission";
    public static final String PENDING_REQUESTS = FULL_SERVER_PATH + "/getPendingRequests";
    public static final String PERMISSION_ACK = FULL_SERVER_PATH + "/acknowledgePermission";
    public static final String GET_PERMISSIONS = FULL_SERVER_PATH + "/getAllPermissions";
    public static final String CELL_UPDATE = FULL_SERVER_PATH + "/updateCellValue";

    // GSON instance for JSON parsing
    public static final Gson GSON_INSTANCE = new Gson();



    //Chat
    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/sendChat";
    // fxml locations
    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";

}