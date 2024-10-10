package dashboard.mainDashboardController;

import com.google.gson.Gson;
import dashboard.dashboardCommands.DashboardCommandsController;
import dashboard.dashboardHeader.DashboardHeaderController;
import dashboard.dashboardTables.DashboardTablesController;
import dto.SpreadsheetManagerDTO;
import dto.VersionDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

    @FXML
    public void initialize() {
        try {
            // Load the header
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/dashboard/dashboardHeader/dashHeader.fxml"));
            VBox header = headerLoader.load();  // Load the header FXML
            dashboardHeaderController = headerLoader.getController();  // Get the controller
            mainLayout.setTop(header);  // Set the header in the top region

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

            // Now, inject the appController into the necessary subcomponents if needed
            dashboardHeaderController.setMainDashboardController(this);
            dashboardTablesController.setMainDashboardController(this);
            dashboardCommandsController.setMainController(this);

            System.out.println("Header, Tables, and Commands loaded correctly!");
            System.out.println("AppController initialized: " + (appController != null));

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void handleSheetSelection(String sheetName) throws UnsupportedEncodingException {
        fetchSheetAndDisplay(sheetName, dashboardHeaderController.getDashUserName());  // Fetch and display the sheet
    }

    private void fetchSheetAndDisplay(String sheetName, String userName) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();

        // Include the current user's name as a query parameter
        String url = String.format(
                "http://localhost:8080/server_Web/getSpreadsheet?sheetName=%s&userName=%s",
                URLEncoder.encode(sheetName, "UTF-8"),
                URLEncoder.encode(userName, "UTF-8")
        );

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
                    Gson gson = new Gson();
                    SpreadsheetManagerDTO spreadsheetManagerDTO = gson.fromJson(responseData, SpreadsheetManagerDTO.class);

                    // Clear tableView2 (permissions table)
                    dashboardTablesController.clearTable2();

                    // Get uploader name from the DTO
                    String uploader = spreadsheetManagerDTO.getUploaderName();

                    // Add permission details to tableView2
                    dashboardTablesController.addPermission(
                            uploader,          // Username (uploader)
                            "Owner",           // Permission type
                            "Approved"         // Status
                    );

                    // Now display the sheet
                   // displaySheet(spreadsheetManagerDTO);
                });
            }
        });
    }




    // Sends the selected sheet's DTO to the controller to be displayed like in Stage 2
    public void sendSelectedSheetToController(String sheetName) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:8080/server_Web/getSpreadsheet?sheetName=" + sheetName)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to send sheet to controller: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Platform.runLater(() -> {
                    Gson gson = new Gson();
                    SpreadsheetManagerDTO spreadsheetManagerDTO = gson.fromJson(responseData, SpreadsheetManagerDTO.class);

                    // Send the DTO to the SpreadsheetController (like in Stage 2)
                   // spreadsheetController.loadSpreadsheet(spreadsheetManagerDTO);
                });
            }
        });
    }


    public void loadSheetFromServer(String sheetName, String uploaderName) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String userName = this.dashboardHeaderController.getDashUserName();
        // Add uploaderName as a query parameter in the request
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
                    System.out.println("Response from server: " + responseData);  // For debugging

                    Gson gson = new Gson();
                    SpreadsheetManagerDTO spreadsheetManagerDTO = gson.fromJson(responseData, SpreadsheetManagerDTO.class);

                    // Now that we have the SpreadsheetManagerDTO, pass it to the controller to display
                    displaySheet(spreadsheetManagerDTO);
                });
            }
        });
    }


    private void displaySheet(SpreadsheetManagerDTO spreadsheetManagerDTO) {
        try {
            System.out.println("Displaying sheet: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());  // For debugging

            // Create a new stage to display the sheet in a separate window
            Stage sheetStage = new Stage();
            sheetStage.setTitle("View Sheet: " + spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName());

            // Load the appController layout (MainLayout.fxml)
            FXMLLoader appLoader = new FXMLLoader(getClass().getResource("/gridPageController/mainController/MainLayout.fxml"));
            StackPane appPane = appLoader.load();  // Load the FXML
            appController = appLoader.getController();  // Get the appController
            appController.setMainDashboardController(this);  // Pass MainDashboardController to AppController


            // Set the spreadsheet data in the appController to display
            appController.getSpreadsheetController().loadSpreadsheetFromDTO(spreadsheetManagerDTO);

            // Set the event handler to stop polling when the window is closed
            sheetStage.setOnCloseRequest(event -> {
                appController.getSpreadsheetController().stopPollingForNewVersions(); // Stop polling when the window is closed
            });

            // Create a new scene and set it to the stage
            Scene scene = new Scene(appPane);
            sheetStage.setScene(scene);

            // Show the new window
            sheetStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
