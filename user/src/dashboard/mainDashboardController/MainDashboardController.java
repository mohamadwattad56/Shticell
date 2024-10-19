package dashboard.mainDashboardController;

import com.google.gson.Gson;
import dashboard.chat.main.ChatAppMainController;
import dashboard.dashboardCommands.DashboardCommandsController;
import dashboard.dashboardHeader.DashboardHeaderController;
import dashboard.dashboardTables.DashboardTablesController;
import dto.PermissionRequestDTO;
import dto.SpreadsheetManagerDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import okhttp3.*;
import gridPageController.mainController.appController;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainDashboardController {

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

    public void saveCurrentDashboardState() {
        dashboardState = new BorderPane();
        dashboardState.setTop(this.getMainLayout().getTop());
        dashboardState.setBottom(getMainLayout().getBottom());
        dashboardState.setLeft(getMainLayout().getLeft());
        dashboardState.setRight(getMainLayout().getRight());
        dashboardState.setCenter(getMainLayout().getCenter());  // Save the current center layout (dashboard)
    }


    /*@FXML
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

            System.out.println("Header, Tables, and Commands loaded correctly!");
            System.out.println("AppController initialized: " + (appController != null));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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
        fetchSheetPermissions(sheetName,uploader);
    }

    public void fetchSheetPermissions(String sheetName, String uploaderName) {
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
                String responseData = response.body().string();

                // Parse the response into PermissionRequestDTO objects
                Gson gson = new Gson();
                PermissionRequestDTO[] permissionInfoArray = gson.fromJson(responseData, PermissionRequestDTO[].class);

                // Update the TableView on the JavaFX thread and pass uploaderName to ensure owner is included
                Platform.runLater(() -> populatePermissionsTable(Arrays.asList(permissionInfoArray), uploaderName));
            }
        });
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
