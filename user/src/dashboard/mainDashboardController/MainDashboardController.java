package dashboard.mainDashboardController;

import com.google.gson.Gson;
import dashboard.dashboardCommands.DashboardCommandsController;
import dashboard.dashboardHeader.DashboardHeaderController;
import dashboard.dashboardTables.DashboardTablesController;
import dto.PermissionRequestDTO;
import dto.SpreadsheetManagerDTO;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;



public class MainDashboardController {
    private Timeline fetchTimeline;

    @FXML
    private BorderPane mainLayout;  // Root layout from the FXML

    @FXML
    private HBox centerHBox;  // Container for tables and commands

    private DashboardHeaderController dashboardHeaderController;
    private DashboardTablesController dashboardTablesController;
    private DashboardCommandsController dashboardCommandsController;

    @FXML
    private appController appController;  // Declare appController as a field
    private BorderPane dashboardState;  // Store the current state of the dashboard

    // Method to handle when the dashboard is closed
    public void onDashboardClose() {

    }

    @FXML
    public void initialize() {
        try {
            // Load the header
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/dashboard/dashboardHeader/dashHeader.fxml"));
            VBox header = headerLoader.load();  // Load the header FXML
            dashboardHeaderController = headerLoader.getController();  // Get the controller
            mainLayout.setTop(header);  // Set the header in the top region

            // Set the mainDashboardController in dashboardHeaderController
            dashboardHeaderController.setMainDashboardController(this);

            // Now that the mainDashboardController is set, start fetching files
            dashboardHeaderController.startFetchingFiles();  // Call this after the controller is set

            // Load the tables
            FXMLLoader tablesLoader = new FXMLLoader(getClass().getResource("/dashboard/dashboardTables/dashTables.fxml"));
            VBox tables = tablesLoader.load();  // Load the tables FXML
            dashboardTablesController = tablesLoader.getController();  // Get the controller
            centerHBox.getChildren().add(tables);  // Add the tables to the centerHBox

            // Load the commands
            FXMLLoader commandsLoader = new FXMLLoader(getClass().getResource("/dashboard/dashboardCommands/dashCommands.fxml"));
            VBox commands = commandsLoader.load();  // Load the commands FXML
            dashboardCommandsController = commandsLoader.getController();  // Get the controller
            centerHBox.getChildren().add(commands);  // Add the commands to the centerHBox


            // Load the appController (Main layout for the grid page)
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource("/gridPageController/mainController/MainLayout.fxml"));
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
    public HBox getCenterHBox() {
        return centerHBox;
    }

    public BorderPane getMainLayout() {
        return mainLayout;
    }

    // Method to set the dashboard username
    public void setDashUserName(String dashUserName) {
        if (dashboardHeaderController != null) {
            dashboardHeaderController.setDashUserName(dashUserName);
        } else {
            System.out.println("DashboardHeaderController is null!");
        }
    }


    public void setDashboardCommandsController(DashboardCommandsController dashboardCommandsController) {
        this.dashboardCommandsController = dashboardCommandsController;
    }
    public void setDashboardHeaderController(DashboardHeaderController dashboardHeaderController) {
        this.dashboardHeaderController = dashboardHeaderController;
    }


    // Called when a sheet is selected in the table
    public void handleSheetSelection(String sheetName, String uploader) throws UnsupportedEncodingException {
        // Initialize or restart the periodic fetch
        startPeriodicFetch(sheetName, uploader);
    }
    private void stopPeriodicFetch() {
        if (fetchTimeline != null && fetchTimeline.getStatus() == Animation.Status.RUNNING) {
            fetchTimeline.stop();  // Stop the timeline when it's no longer needed
        }
    }

    private void startPeriodicFetch(String sheetName, String uploaderName) {
        // Stop any existing fetch timeline to avoid multiple fetching loops
        if (fetchTimeline != null) {
            fetchTimeline.stop();
            System.out.println("Stopping existing timeline...");
        }

        // Create a new timeline to fetch data every 200ms
        fetchTimeline = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            System.out.println("Fetching sheet permissions for sheet: " + sheetName + ", uploader: " + uploaderName);
            fetchSheetPermissions(sheetName, uploaderName);
        }));

        fetchTimeline.setCycleCount(Timeline.INDEFINITE);  // Run indefinitely
        fetchTimeline.play();  // Start fetching
        System.out.println("Timeline started for periodic fetch.");
    }



    public void fetchSheetPermissions(String sheetName, String uploaderName) {
        System.out.println("Inside fetchSheetPermissions for: " + sheetName + " by " + uploaderName);

        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8080/server_Web/getAllPermissions?sheetName=" + sheetName + "&uploaderName=" + uploaderName;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch sheet permissions: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    System.out.println("Response Data: " + responseData);  // Log response data

                    Gson gson = new Gson();
                    PermissionRequestDTO[] permissionInfoArray = gson.fromJson(responseData, PermissionRequestDTO[].class);

                    // Log permissions
                    System.out.println("Permissions Parsed: " + Arrays.toString(permissionInfoArray));

                    Platform.runLater(() -> {
                        System.out.println("Updating permissions table and tableView1...");
                        updatePermissionsTable(Arrays.asList(permissionInfoArray), uploaderName);
                        updateTableView1Permissions(Arrays.asList(permissionInfoArray), uploaderName, sheetName);
                        System.out.println("Tables updated successfully.");
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
        newDataList.add(0, ownerRow);  // Add the owner to the beginning of the list

        // Compare the new data with the current data in the table
        if (!tableView2.getItems().equals(newDataList)) {
            // If the data is different, update the table
            tableView2.getItems().setAll(newDataList);
        }
    }

    private void updateTableView1Permissions(List<PermissionRequestDTO> permissions, String uploaderName, String sheetName) {
        TableView<DashboardTablesController.SheetRowData> tableView1 = this.dashboardTablesController.getTableView1();  // Get TableView1
        ObservableList<DashboardTablesController.SheetRowData> currentItems = tableView1.getItems();  // Current items in the table

        // Create the new data based on the permissions
        List<DashboardTablesController.SheetRowData> updatedData = permissions.stream()
                .map(permission -> {
                    String permissionType = permission.getRequestStatus().name();
                    return new DashboardTablesController.SheetRowData(
                            uploaderName,  // Use the actual uploader for the sheet
                            permission.getSheetName(),  // Correct sheet name
                            "Sheet Size Placeholder",  // You can replace this with the actual sheet size if available
                            permissionType);  // Permission type
                })
                .collect(Collectors.toList());

        // Stop periodic fetch while updating the table to prevent conflicts
        stopPeriodicFetch();

        // Update the current data without adding duplicates or incorrect entries
        for (DashboardTablesController.SheetRowData newData : updatedData) {
            boolean exists = currentItems.stream()
                    .anyMatch(existingData ->
                            existingData.getSheetName().equals(newData.getSheetName()) &&
                                    existingData.getUploader().equals(newData.getUploader()) &&
                                        existingData.getPermission().equals(newData.getPermission()));

            // If the row doesn't exist, add it
            if (!exists) {
                currentItems.add(newData);
            }
        }

        // Start periodic fetch again after updating
        startPeriodicFetch(sheetName, uploaderName);
    }







    // Modified method to populate tableView2
    private void populatePermissionsTable(List<PermissionRequestDTO> permissions, String uploaderName) {
        TableView<DashboardTablesController.PermissionRowData> tableView2 = this.dashboardTablesController.getTableView2();

        // Only add columns once if they haven't been set
        if (tableView2.getColumns().isEmpty()) {
            TableColumn<DashboardTablesController.PermissionRowData, String> usernameCol = new TableColumn<>("Username");
            TableColumn<DashboardTablesController.PermissionRowData, String> permissionTypeCol = new TableColumn<>("Permission Type");
            TableColumn<DashboardTablesController.PermissionRowData, String> approvalCol = new TableColumn<>("Approved");

            usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
            permissionTypeCol.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
            approvalCol.setCellValueFactory(new PropertyValueFactory<>("status"));

            tableView2.getColumns().addAll(usernameCol, permissionTypeCol, approvalCol);
        }

        // Convert PermissionRequestDTO to PermissionRowData for display
        List<DashboardTablesController.PermissionRowData> rowDataList = permissions.stream()
                .map(permission -> new DashboardTablesController.PermissionRowData(
                        permission.getUsername(),
                        permission.getPermissionType(),
                        permission.getRequestStatus().name())
                )
                .collect(Collectors.toList());

        DashboardTablesController.PermissionRowData ownerRow = new DashboardTablesController.PermissionRowData(uploaderName, "OWNER", "APPROVED");

        // Add the ownerRow as the first row
        rowDataList.add(0, ownerRow);

        // Only update the table if the data has changed
        if (!tableView2.getItems().equals(rowDataList)) {
            tableView2.getItems().setAll(rowDataList);
        }
    }






    public void loadSheetFromServer(String sheetName, String uploaderName, String userPermission) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String userName = this.dashboardHeaderController.getDashUserName();
        String url = String.format("http://localhost:8080/server_Web/getSpreadsheet?sheetName=%s&uploaderName=%s&userName=%s",
                URLEncoder.encode(sheetName, "UTF-8"),
                URLEncoder.encode(uploaderName, "UTF-8"),
                URLEncoder.encode(userName, "UTF-8"));

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch spreadsheet: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
            System.out.println("Displaying sheet: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());

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
                System.out.println("Disabling editing features for READER permission.");
                disableEditingFeatures();
            } else {
                System.out.println("Permission allows editing: " + userPermission);
            }

            // Replace the dashboard content with the sheet layout
            mainLayout.setCenter(appPane);  // Replace the center with the sheet
            mainLayout.setTop(null);  // Make sure the top region is null (to prevent any header overlap)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    public void disableEditingFeatures() {
        System.out.println("Disabling editing features for READER permission");

        // Ensure this runs on the JavaFX Application thread
        Platform.runLater(() -> {
            System.out.println("Inside Platform.runLater for disableEditingFeatures");
/*
           getAppController().getSpreadsheetController().disableEditing();
*/
            getAppController().getHeadController().disableEditing();
        });
    }




    public void setDashboardTablesController(DashboardTablesController dashboardTablesController) {
        this.dashboardTablesController = dashboardTablesController;
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
