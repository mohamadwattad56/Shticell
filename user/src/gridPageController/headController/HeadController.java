package gridPageController.headController;
import com.google.gson.Gson;
import dashboard.mainDashboardController.MainDashboardController;
import dto.CellDTO;
import dto.VersionDTO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import gridPageController.mainController.appController;
import javafx.util.Duration;
import okhttp3.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;


public class HeadController {

    private appController appController;


    @FXML
    private ComboBox<String> skinSelector;
    @FXML
    private Button versionButton;
    @FXML
    private TextField selectedCellIdField;
    @FXML
    private TextField originalCellValueField;
    @FXML
    private Button updateValueButton;
    @FXML
    private Label lastUpdateCellVersionField;
    @FXML
    private Button versionSelectorButton;
    @FXML
    private Label modifiedBy;
    @FXML
    private Button backButton;


    @FXML
    private void initialize() {
        // Set event handler for value updates
        updateValueButton.setOnAction(event -> {
            try {
                handleUpdateValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        versionSelectorButton.setOnAction(event -> handleVersionSelector());

        // Add listener for skin change
        skinSelector.setOnAction(event -> {
            String selectedSkin = skinSelector.getValue();
            appController.changeSkin(selectedSkin);  // Call the method in appController to change the skin
        });

        backButton.setOnAction(event -> handleBackButton());

    }


    @FXML
    private void handleBackButton() {
        try {
            // Load the main dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/mainDashboardController/mainDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the controller and set the username (you may want to pass the stored username)
            MainDashboardController dashboardController = loader.getController();
            if (dashboardController != null) {
                dashboardController.setDashUserName(appController.getMainDashboardController().getDashboardHeaderController().getDashUserName());  // Restore the username
            } else {
                System.out.println("Dashboard Controller is null!");
            }

            // Create a new scene for the dashboard
            Scene dashboardScene = new Scene(dashboardRoot);
            Stage currentStage = (Stage) backButton.getScene().getWindow();

            // Set the dashboard scene on the current stage
            currentStage.setScene(dashboardScene);

            // Show the dashboard again
            currentStage.show();

        } catch (IOException e) {
            appController.showError("Error", "Failed to navigate back to the dashboard.");
        }
    }


    private void handleUpdateValue() {
        String cellIdentifier = selectedCellIdField.getText();
        String newValue = originalCellValueField.getText();
        String oldValue = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellIdentifier).getSourceValue().toString();

        try {
            // Call the updateCellValue method in SpreadsheetController and handle the future
            appController.getSpreadsheetController()
                    .updateCellValue(cellIdentifier, newValue, oldValue)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            // Handle the exception if present
                            Platform.runLater(() -> appController.showError("Update Error", ex.getMessage()));
                        } else {
                            // Update UI only if the operation succeeded
                            Platform.runLater(() -> {
                                String tmp = "By user: " + appController.getMainDashboardController().getDashboardHeaderController().getDashUserName();
                                lastUpdateCellVersionField.setText("Last update cell version: " + appController.getSpreadsheetController().getSpreadsheet().getCurrentVersion());
                                modifiedBy.setText(tmp);
                            });
                        }
                        return null;  // Return null since we don't need a result here
                    });

        } catch (Exception ignored) {

        }
    }


    public Label getModifiedBy() {
        return modifiedBy;
    }

    public void setMainController(appController appController) {
        this.appController = appController;
    }

    public TextField getSelectedCellIdField(){
        return selectedCellIdField;
    }

    public TextField getOriginalCellValueField(){
        return originalCellValueField;
    }

    public Label getLastUpdateCellVersionField(){
        return lastUpdateCellVersionField;
    }

    public void disableEditing() {

        Platform.runLater(() -> {
            if (originalCellValueField != null && updateValueButton != null) {
                originalCellValueField.setEditable(false);  // Make the field non-editable but still visible
                updateValueButton.setDisable(true);  // Disable the update button
            } else {
                System.out.println("Fields are null, check initialization!");
            }
        });
    }






    @FXML
    private void handleVersionSelector() {
        // Send a request to the server to get version history
        OkHttpClient client = new OkHttpClient();

        try {
            // Construct the URL with URL-encoded sheet name
            String url = String.format(
                    "http://localhost:8080/server_Web/getVersionHistory?sheetName=%s",
                    URLEncoder.encode(this.appController.getSpreadsheetController().getSpreadsheet().getSpreadsheetDTO().getSheetName(), StandardCharsets.UTF_8)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failed to fetch version history: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    assert response.body() != null;
                    String responseData = response.body().string();

                    Platform.runLater(() -> {
                        // Parse the version history from server response
                        Gson gson = new Gson();
                        List<String> versionHistory = gson.fromJson(responseData, List.class);

                        // Now that we have the version history, display the popup on the client
                        showVersionSelectorPopup(versionHistory);
                    });
                }
            });
        } catch (Exception e) {
            System.out.println("Error encoding URL: " + e.getMessage());
        }
    }
    private void showVersionSelectorPopup(List<String> versionHistory) {
        Stage versionPopup = new Stage();
        versionPopup.setTitle("Select Version");

        VBox popupLayout = new VBox(10);
        popupLayout.setPadding(new Insets(20));

        Label instructionLabel = new Label("Select a version to view:");
        instructionLabel.setStyle("-fx-font-size: 14px;");

        ComboBox<String> versionComboBox = new ComboBox<>();
        versionComboBox.setPromptText("Select version");
        versionComboBox.getItems().addAll(versionHistory);

        Button okButton = new Button("Load Version");
        okButton.setDisable(true);  // Initially disable the button
        okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        versionComboBox.setOnAction(event -> {
            if (versionComboBox.getValue() != null) {
                okButton.setDisable(false);
            }
        });

        // Send a request to load the selected version
        okButton.setOnAction(event -> {
            String selectedVersion = versionComboBox.getValue();
            int versionNumber = extractVersionNumber(selectedVersion);
            loadVersionFromServer(versionNumber);
            versionPopup.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        cancelButton.setOnAction(event -> versionPopup.close());

        HBox buttonBox = new HBox(10, cancelButton, okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        popupLayout.getChildren().addAll(instructionLabel, versionComboBox, buttonBox);

        Scene popupScene = new Scene(popupLayout, 300, 200);
        versionPopup.setScene(popupScene);
        versionPopup.showAndWait();
    }


    private void loadVersionFromServer(int versionNumber) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Construct the URL with both version number and sheet name as query parameters
            String url = String.format(
                    "http://localhost:8080/server_Web/getVersion?versionNumber=%d&sheetName=%s",
                    versionNumber,
                    URLEncoder.encode(this.appController.getSpreadsheetController().getSpreadsheet().getSpreadsheetDTO().getSheetName(), StandardCharsets.UTF_8)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failed to fetch version: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    Platform.runLater(() -> {
                        Gson gson = new Gson();
                        VersionDTO selectedVersion = gson.fromJson(responseData, VersionDTO.class);

                        // Display the version using the existing method
                        showVersionSpreadsheet(selectedVersion);
                    });
                }
            });
        } catch (Exception e) {
            System.out.println("Error encoding URL: " + e.getMessage());
        }
    }


    public void showNewVersionIndicator(int latestVersion) {
        // Make the button visible
        versionButton.setVisible(true);  // Show the button

        // Set the button text to indicate the new version
        versionButton.setText("New Version Available: " + latestVersion);

        // Create a timeline for highlighting the button
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> versionButton.setStyle("-fx-background-color: #ffcc00;")),  // Highlight (yellow)
                new KeyFrame(Duration.seconds(1.0), e -> versionButton.setStyle("")),  // Remove highlight (default)
                new KeyFrame(Duration.seconds(1.5), e -> versionButton.setStyle("-fx-background-color: #ffcc00;")),  // Highlight again
                new KeyFrame(Duration.seconds(2.0), e -> versionButton.setStyle(""))   // Remove highlight
        );

        // Set the cycle count (how many times the highlight will blink)
        timeline.setCycleCount(4);  // Blink 4 times

        // Start the timeline
        timeline.play();

        // Add an action to load the new version when clicked
        versionButton.setOnAction(event -> {
            try {
                // Fetch the user's permission for the sheet before reloading
                String sheetName = this.appController.getSpreadsheetController().getSpreadsheet().getSpreadsheetDTO().getSheetName();
                String uploaderName = this.appController.getSpreadsheetController().getSpreadsheet().getUploaderName();
                String currentUsername = this.appController.getMainDashboardController().getDashboardHeaderController().getDashUserName();

                // Fetch user permission
                appController.getMainDashboardController().getDashboardCommandsController().fetchUserPermission(sheetName, currentUsername, permission -> {
                    if (permission.equals("NONE")) {
                        this.appController.showError("No permissions.","You do not have permission to view this sheet.");
                    } else {
                        // Reload the sheet with the latest version based on permission
                        try {
                            appController.getMainDashboardController().loadSheetFromServer(sheetName, uploaderName, permission);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        // If the user is a reader, disable editing features
                        if (permission.equals("READER")) {
                            appController.getMainDashboardController().disableEditingFeatures();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }










    // Helper method to extract version number from the version string
    private int extractVersionNumber(String versionText) {
        // Assuming the format is "Version X: ..." (for example, "Version 5: 3 cells changed.")
        String[] parts = versionText.split(" ");

        // Remove the colon at the end of the number (e.g., "5:")
        String versionNumberWithColon = parts[1];  // "5:"
        String versionNumber = versionNumberWithColon.replace(":", "");  // Remove the colon

        return Integer.parseInt(versionNumber);  // Convert the cleaned version number to integer
    }

    // Method to display the selected version of the spreadsheet in a read-only grid
    private void showVersionSpreadsheet(VersionDTO versionDTO) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Version " + versionDTO.getVersionNumber());

        VBox popupLayout = new VBox(10);
        popupLayout.setPadding(new Insets(20));
        popupLayout.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #d3d3d3; -fx-border-width: 2; -fx-border-radius: 8;");

        GridPane versionGrid = new GridPane();
        versionGrid.setGridLinesVisible(true);
        versionGrid.setHgap(2);
        versionGrid.setVgap(2);
        versionGrid.setStyle("-fx-padding: 10;");

        int numberOfRows = versionDTO.getSpreadsheetDTO().getRows();
        int numberOfColumns = versionDTO.getSpreadsheetDTO().getColumns();
        double columnWidth = 80; // Consistent width for all columns
        double rowHeight = 30;   // Consistent height for all rows

        // Add column letters at the top (A, B, C, ...)
        for (int col = 1; col <= numberOfColumns; col++) {
            Label columnLabel = new Label(String.valueOf((char) ('A' + (col - 1))));
            columnLabel.setAlignment(Pos.CENTER);
            columnLabel.setMaxWidth(Double.MAX_VALUE);
            columnLabel.setMaxHeight(Double.MAX_VALUE);
            columnLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5px; -fx-border-color: #a9a9a9;");
            versionGrid.add(columnLabel, col, 0);  // Add to the top row (row 0)

            // Set consistent column width
            ColumnConstraints colConstraints = new ColumnConstraints(columnWidth);
            colConstraints.setHgrow(Priority.SOMETIMES); // Ensure the column grows to fill space
            versionGrid.getColumnConstraints().add(colConstraints);
        }

        // Add row numbers on the left (1, 2, 3, ...)
        for (int row = 1; row <= numberOfRows; row++) {
            Label rowLabel = new Label(String.valueOf(row));
            rowLabel.setAlignment(Pos.CENTER);
            rowLabel.setMaxWidth(Double.MAX_VALUE);
            rowLabel.setMaxHeight(Double.MAX_VALUE);
            rowLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5px; -fx-border-color: #a9a9a9;");
            versionGrid.add(rowLabel, 0, row);  // Add to the leftmost column (column 0)

            // Set consistent row height
            RowConstraints rowConstraints = new RowConstraints(rowHeight);
            rowConstraints.setVgrow(Priority.ALWAYS); // Ensure the row grows to fill space
            versionGrid.getRowConstraints().add(rowConstraints);
        }

        // Populate the grid with cell values from the versionDTO
        for (CellDTO cellDTO : versionDTO.getCells()) {
            String cellId = cellDTO.getCellId();
            int rowIndex = extractRow(cellId);
            int columnIndex = extractCol(cellId);

            // Use the textColor and backgroundColor from CellDTO
            String textColor = cellDTO.getTextColor(); // Assuming you added getTextColor() to CellDTO
            String backgroundColor = cellDTO.getBackgroundColor(); // Assuming you added getBackgroundColor() to CellDTO

            Label cellLabel = new Label(cellDTO.getEffectiveValue().toString().equals("EMPTY") ? "" : cellDTO.getEffectiveValue().toString());
            cellLabel.setAlignment(Pos.CENTER);
            cellLabel.setStyle("-fx-border-color: #a9a9a9; -fx-padding: 5px; -fx-border-width: 1;" +
                    "-fx-text-fill: " + textColor + ";" +      // Apply text color
                    "-fx-background-color: " + backgroundColor + ";"); // Apply background color
            cellLabel.setPrefWidth(columnWidth);
            cellLabel.setPrefHeight(rowHeight);

            versionGrid.add(cellLabel, columnIndex, rowIndex);
        }

        // Create a ScrollPane with smooth scrolling
        ScrollPane scrollPane = new ScrollPane(versionGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);  // Ensure the ScrollPane adjusts to content height
        scrollPane.setPannable(true); // Enable panning with mouse drag
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Dynamically calculate the grid's height based on content
        double gridHeight = (numberOfRows + 1) * rowHeight; // Include header row
        double maxGridHeight = 800;  // Set a maximum height for the grid within the window

        // Dynamically set window size based on the content size
        double windowWidth = Math.max(1200, (numberOfColumns + 1) * columnWidth + 40); // Adjust for column count
        double windowHeight = Math.max(800, gridHeight + 100); // Adjust for grid height

        // Ensure the ScrollPane adapts height to content, with a maximum value
        scrollPane.setPrefHeight(Math.max(gridHeight, maxGridHeight));

        popupLayout.getChildren().add(scrollPane);

        Scene popupScene = new Scene(popupLayout, windowWidth, windowHeight);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    // Helper methods to extract row and column from cellId
    private int extractRow(String cellId) {
        return Integer.parseInt(cellId.replaceAll("[^0-9]", ""));
    }

    private int extractCol(String cellId) {
        return cellId.replaceAll("[0-9]", "").charAt(0) - 'A' + 1;
    }


}
