package httputils;


import com.google.gson.Gson;

public class Constants {

    // Global constants
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String DEFAULT_USER = "<Anonymous>";
    public static final int REFRESH_RATE = 2000;  // Adjust if necessary
    public static final String DATE_TIME_FORMAT = "%tH:%tM:%tS | %.10s: %s%n";

    // FXML file locations for UI components
    public static final String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/components/main/app-main.fxml";
    public static final String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/login.fxml";
    public static final String DASHBOARD_FXML_RESOURCE_LOCATION = "/components/dashboard/dashboard.fxml";

    // Server resources locations (URLs)
    public static final String BASE_DOMAIN = "localhost";
    private static final String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private static final String CONTEXT_PATH = "/server_Web";  // Your application context path
    private static final String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    // API Endpoints
    public static final String LOGIN_PAGE = FULL_SERVER_PATH + "/login";  // Login endpoint
    public static final String UPLOAD_FILE = FULL_SERVER_PATH + "/getUploadedFiles";
    public static final String USERS_LIST = FULL_SERVER_PATH + "/userslist";  // Endpoint to fetch user list
    public static final String LOGOUT = FULL_SERVER_PATH + "/logout";  // Logout endpoint
    public static final String SHEET_DATA = FULL_SERVER_PATH + "/sheet";  // Endpoint for sheet data
    public static final String SHEET_UPDATE = FULL_SERVER_PATH + "/sheet/update";  // Endpoint for updating sheet

    // GSON instance for JSON parsing
    public static final Gson GSON_INSTANCE = new Gson();



    //Chat
    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/sendChat";
    // fxml locations
    public final static String CHAT_ROOM_FXML_RESOURCE_LOCATION = "/chat/client/component/chatroom/chat-room-main.fxml";
    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";

}