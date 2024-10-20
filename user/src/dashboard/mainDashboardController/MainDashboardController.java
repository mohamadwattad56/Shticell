package dashboard.mainDashboardController;
import com.google.gson.Gson;
import dashboard.dashboardCommands.DashboardCommandsController;
import dashboard.dashboardHeader.DashboardHeaderController;
import dashboard.dashboardTables.DashboardTablesController;
import dto.PermissionRequestDTO;
import dto.SpreadsheetManagerDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import okhttp3.*;
import gridPageController.mainController.appController;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import static httputils.Constants.*;

public class MainDashboardController {

    private Timeline fetchTimeline;

    @FXML
    private BorderPane mainLayout;  // Root layout from the FXML

    @FXML
    private HBox centerHBox;  // Container for tables and commands

    @FXML
    private appController appController;  // Declare appController as a field

    private DashboardHeaderController dashboardHeaderController;

    private DashboardTablesController dashboardTablesController;

    private DashboardCommandsController dashboardCommandsController;

    @FXML
    public void initialize() {
        try {
            // Load the header
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource(DASHBOARD_HEADER_FXML_RESOURCE_LOCATION));
            VBox header = headerLoader.load();  // Load the header FXML
            dashboardHeaderController = headerLoader.getController();  // Get the controller
            mainLayout.setTop(header);  // Set the header in the top region

            // Set the mainDashboardController in dashboardHeaderController
            dashboardHeaderController.setMainDashboardController(this);

            // Now that the mainDashboardController is set, start fetching files
            dashboardHeaderController.startFetchingFiles();  // Call this after the controller is set

            // Load the tables
            FXMLLoader tablesLoader = new FXMLLoader(getClass().getResource(DASHBOARD_TABLES_FXML_RESOURCE_LOCATION));
            VBox tables = tablesLoader.load();  // Load the tables FXML
            dashboardTablesController = tablesLoader.getController();  // Get the controller
            centerHBox.getChildren().add(tables);  // Add the tables to the centerHBox

            // Load the commands
            FXMLLoader commandsLoader = new FXMLLoader(getClass().getResource(DASHBOARD_COMMANDS_FXML_RESOURCE_LOCATION));
            VBox commands = commandsLoader.load();  // Load the commands FXML
            dashboardCommandsController = commandsLoader.getController();  // Get the controller
            centerHBox.getChildren().add(commands);  // Add the commands to the centerHBox


            // Load the appController (Main layout for the grid page)
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource(SHEET_MAIN_LAYOUT_FXML_RESOURCE_LOCATION));
            StackPane appPane = appLoader.load();  // Load the main layout FXML
            appController = appLoader.getController();  // Get the appController

            // Inject the appController into necessary subcomponents if needed
            appController.setMainDashboardController(this);
            dashboardTablesController.setMainDashboardController(this);
            dashboardCommandsController.setMainController(this);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public appController getAppController() {
        return appController;
    }

    public BorderPane getMainLayout() {
        return mainLayout;
    }

    public void setDashUserName(String dashUserName) {
        if (dashboardHeaderController != null) {
            dashboardHeaderController.setDashUserName(dashUserName);
        }
    }

    public void handleSheetSelection(String sheetName, String uploader) throws UnsupportedEncodingException {
        // Initialize or restart the periodic fetch
        startPeriodicFetch(sheetName, uploader);
    }

    private void startPeriodicFetch(String sheetName, String uploaderName) {
        // Stop any existing fetch timeline to avoid multiple fetching loops
        if (fetchTimeline != null) {
            fetchTimeline.stop();
        }

        // Create a new timeline to fetch data every 200ms
        fetchTimeline = new Timeline(new KeyFrame(Duration.millis(200), event -> fetchSheetPermissions(sheetName, uploaderName)));

        fetchTimeline.setCycleCount(Timeline.INDEFINITE);  // Run indefinitely
        fetchTimeline.play();  // Start fetching
    }

    public void fetchSheetPermissions(String sheetName, String uploaderName) {

        OkHttpClient client = new OkHttpClient();
        String url = GET_PERMISSIONS + "?sheetName=" + sheetName + "&uploaderName=" + uploaderName;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch sheet permissions: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    assert response.body() != null;
                    String responseData = response.body().string();

                    Gson gson = new Gson();
                    PermissionRequestDTO[] permissionInfoArray = gson.fromJson(responseData, PermissionRequestDTO[].class);

                    // Log permissions

                    Platform.runLater(() -> {
                        updatePermissionsTable(Arrays.asList(permissionInfoArray), uploaderName);
                        updateTableView1Permissions(Arrays.asList(permissionInfoArray), sheetName);
                    });
                } catch (Exception e) {
                    e.printStackTrace();  // Catch any exceptions
                    System.out.println("Error in onResponse: " + e.getMessage());
                }
            }
        });
    }

    private void updatePermissionsTable(List<PermissionRequestDTO> permissions, String uploaderName) {
        TableView<DashboardTablesController.PermissionRowData> tableView2 = this.dashboardTablesController.getTableView2(); // Assuming you have this method to get tableView2

        // Set up the columns (if not done already)
        TableColumn<DashboardTablesController.PermissionRowData, String> usernameCol = new TableColumn<>("Username");
        TableColumn<DashboardTablesController.PermissionRowData, String> permissionTypeCol = new TableColumn<>("Permission Type");
        TableColumn<DashboardTablesController.PermissionRowData, String> approvalCol = new TableColumn<>("Approved");

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        permissionTypeCol.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        approvalCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Create a list to hold the new data
        List<DashboardTablesController.PermissionRowData> newDataList = permissions.stream()
                .map(permission -> new DashboardTablesController.PermissionRowData(
                        permission.getUsername(),
                        permission.getPermissionType(),
                        permission.getRequestStatus().name())
                ).collect(Collectors.toList());

        // Ensure the owner is always in the list
        DashboardTablesController.PermissionRowData ownerRow = new DashboardTablesController.PermissionRowData(uploaderName, "OWNER", "Approved");
        newDataList.addFirst(ownerRow);  // Add the owner to the beginning of the list

        // Compare the new data with the current data in the table
        if (!tableView2.getItems().equals(newDataList)) {
            // If the data is different, update the table
            tableView2.getItems().setAll(newDataList);
        }
    }

    private void updateTableView1Permissions(List<PermissionRequestDTO> permissions, String sheetName) {
        String currentUser = this.dashboardHeaderController.getDashUserName();  // Get the current user's username
        TableView<DashboardTablesController.SheetRowData> tableView1 = this.dashboardTablesController.getTableView1();  // Get TableView1

        // Find the current user's permission for the selected sheet
        for (PermissionRequestDTO permission : permissions) {
            if (permission.getUsername().equals(currentUser)) {
                // Find the corresponding row in tableView1 for the current user and update the permission
                for (DashboardTablesController.SheetRowData rowData : tableView1.getItems()) {
                    if (rowData.getSheetName().equals(sheetName)) {  // Compare sheet names
                        // Update the permission in the row if it's changed
                        if (!rowData.getPermission().equals(permission.getPermissionType()) && permission.getRequestStatus().equals(PermissionRequestDTO.RequestStatus.APPROVED)) {

                            rowData.setPermission(permission.getPermissionType());
                            tableView1.refresh();  // Refresh the table to show the updated permission
                        }
                        break;
                    }
                }
                break;
            }
        }
    }
















    public void loadSheetFromServer(String sheetName, String uploaderName, String userPermission) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String userName = this.dashboardHeaderController.getDashUserName();
        String url = String.format("http://localhost:8080/server_Web/getSpreadsheet?sheetName=%s&uploaderName=%s&userName=%s",
                URLEncoder.encode(sheetName, StandardCharsets.UTF_8),
                URLEncoder.encode(uploaderName, StandardCharsets.UTF_8),
                URLEncoder.encode(userName, StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch spreadsheet: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String responseData = response.body().string();
                Platform.runLater(() -> {
                    try {
                        Gson gson = new Gson();
                        SpreadsheetManagerDTO spreadsheetManagerDTO = gson.fromJson(responseData, SpreadsheetManagerDTO.class);

                        // Now that we have the SpreadsheetManagerDTO, pass it to displaySheet with the user's permission
                        displaySheet(spreadsheetManagerDTO, userPermission);  // Pass the permission

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }





    /*private void displaySheet(SpreadsheetManagerDTO spreadsheetManagerDTO, String userPermission) {
        try {
            System.out.println("Displaying sheet: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());

            // Create a new stage to display the sheet in a separate window
            Stage sheetStage = new Stage();
            sheetStage.setTitle("View Sheet: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());

            // Load the appController layout (MainLayout.fxml)
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource("/gridPageController/mainController/MainLayout.fxml"));
            StackPane appPane = appLoader.load();
            appController = appLoader.getController();
            appController.setMainDashboardController(this);

            // Set the spreadsheet data in the appController to display
            appController.getSpreadsheetController().loadSpreadsheetFromDTO(spreadsheetManagerDTO, userPermission);

            // Disable editing features if the user is not a WRITER
            if (userPermission.equals("READER")) {
                System.out.println("Disabling editing features for READER permission.");
                disableEditingFeatures();
            } else {
                System.out.println("Permission allows editing: " + userPermission);
            }

            // Set the event handler to stop polling when the window is closed
            sheetStage.setOnCloseRequest(event -> {
                appController.getSpreadsheetController().stopPollingForNewVersions();
            });

            // Create a new scene and set it to the stage
            Scene scene = new Scene(appPane);
            sheetStage.setScene(scene);

            // Show the new window
            sheetStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void displaySheet(SpreadsheetManagerDTO spreadsheetManagerDTO, String userPermission) {
        try {

            // Clear both the top (header) and center content before loading the sheet
            mainLayout.setTop(null);  // Remove the dashboard header before loading the sheet header
            mainLayout.setCenter(null);  // Clear any existing content in the center

            // Load the appController layout (MainLayout.fxml)
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource("/gridPageController/mainController/MainLayout.fxml"));
            StackPane appPane = appLoader.load();
            appController = appLoader.getController();
            appController.setMainDashboardController(this);

            // Set the spreadsheet data in the appController to display
            appController.getSpreadsheetController().loadSpreadsheetFromDTO(spreadsheetManagerDTO, userPermission);

            // Disable editing features if the user is not a WRITER
            if (userPermission.equals("READER")) {
                disableEditingFeatures();
            }

            // Replace the dashboard content with the sheet layout
            mainLayout.setCenter(appPane);  // Replace the center with the sheet
            mainLayout.setTop(null);  // Make sure the top region is null (to prevent any header overlap)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    public void disableEditingFeatures() {

        // Ensure this runs on the JavaFX Application thread
        Platform.runLater(() -> {
/*
           getAppController().getSpreadsheetController().disableEditing();
*/
            getAppController().getHeadController().disableEditing();
        });
    }


    public DashboardTablesController getDashboardTablesController() {
        return dashboardTablesController;
    }
    public DashboardCommandsController getDashboardCommandsController() {
        return dashboardCommandsController;
    }
    public DashboardHeaderController getDashboardHeaderController() {
        return dashboardHeaderController;
    }
}
