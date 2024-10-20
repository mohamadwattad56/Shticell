package dashboard.dashboardHeader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dashboard.dashboardTables.DashboardTablesController;
import dashboard.mainDashboardController.MainDashboardController;
import dto.SpreadsheetManagerDTO;
import httputils.HttpClientUtil;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import static httputils.Constants.UPLOAD_FILE;

public class DashboardHeaderController {

    @FXML
    private Button loadFileButton;

    @FXML
    private TextField filePathField;

    @FXML
    private Label dashUserName;

    @FXML
    private Label successHintLabel;

    private MainDashboardController mainDashboardController;  // Reference to MainDashboardController

    private static final String UPLOAD_URL = "http://localhost:8080/server_Web/uploadFile";  // Your upload servlet URL

    public void initialize() {
        loadFileButton.setOnAction(event -> handleLoadFile());
    }

    public void startFetchingFiles() {
        Timeline fetchFilesTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> fetchUploadedFiles()));
        fetchFilesTimeline.setCycleCount(Timeline.INDEFINITE); // Run indefinitely every 2 seconds
        fetchFilesTimeline.play();
    }

    private void fetchUploadedFiles() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(UPLOAD_FILE)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch uploaded files: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String responseData = response.body().string();
                Platform.runLater(() -> {
                    Gson gson = new Gson();

                    // Save currently selected row
                    DashboardTablesController.SheetRowData selectedRow = mainDashboardController.getDashboardTablesController().getSelectedSheet();
                    String selectedSheetName = selectedRow != null ? selectedRow.getSheetName() : null;

                    // Convert the response to a map of sheet name -> SpreadsheetManagerDTO
                    Type type = new TypeToken<Map<String, SpreadsheetManagerDTO>>() {}.getType();
                    Map<String, SpreadsheetManagerDTO> spreadsheetManagerMap = gson.fromJson(responseData, type);

                    if (spreadsheetManagerMap.isEmpty()) {
                        return;
                    }

                    // Check if there are actual changes in the data
                    ObservableList<DashboardTablesController.SheetRowData> currentData = mainDashboardController.getDashboardTablesController().getSheetData();
                    boolean hasChanges = false;

                    // Compare current data with the new data
                    for (SpreadsheetManagerDTO dto : spreadsheetManagerMap.values()) {
                        String sheetName = dto.getSpreadsheetDTO().getSheetName();
                        String uploader = dto.getUploaderName();  // Assuming you added this field to SpreadsheetManagerDTO
                        String sheetSize = dto.getSpreadsheetDTO().getRows() + "x" + dto.getSpreadsheetDTO().getColumns();

                        // Check if the sheet already exists in the current data
                        boolean found = false;
                        for (DashboardTablesController.SheetRowData existingRow : currentData) {
                            if (existingRow.getSheetName().equals(sheetName)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            hasChanges = true;
                            mainDashboardController.getDashboardTablesController().addSheet(uploader, sheetName, sheetSize);
                        }
                    }

                    // If no changes were detected, skip the refresh
                    if (!hasChanges) {
                        return;
                    }

                    // Restore the selection after refreshing the table
                    if (selectedSheetName != null) {
                        mainDashboardController.getDashboardTablesController().selectSheetByName(selectedSheetName);
                    }
                });
            }
        });
    }

    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            uploadFileToServer(selectedFile); // Upload the file to the server
        }
    }

    private void uploadFileToServer(File file) {
        String uploaderName = dashUserName.getText();  // The uploader's name
        String filePath = file.getAbsolutePath();  // Get the absolute file path

        // Use the new uploadFileAsync method
        HttpClientUtil.uploadFileAsync(UPLOAD_URL, file, filePath, uploaderName, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                  //  progressBar.setProgress(0.0);
                    System.out.println("Upload failed: " + e.getMessage());
                    // Show error message to the user (you can replace this with a proper UI message)
                    showError("Upload Error", "Failed to upload the file: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                Platform.runLater(() -> {
                //    progressBar.setProgress(1.0);
                    if (response.isSuccessful()) {
                        showSuccessHint();

                        if (mainDashboardController != null) {
                            fetchUploadedFiles();  // Fetch updated files after successful upload
                        }
                    } else {
                        // Extract the error message from the response body
                        String responseBody;
                        try {
                            assert response.body() != null;
                            responseBody = response.body().string();
                            String errorMessage = extractErrorMessage(responseBody);  // Extract the message
                            System.out.println("Upload failed: " + errorMessage);
                            showError("Upload Error", "Failed to upload the file: " + errorMessage);
                        } catch (IOException e) {
                            System.out.println("Error reading response body: " + e.getMessage());
                            showError("Upload Error", "Error reading server response.");
                        }
                    }
                });
            }

        });
    }

    private String extractErrorMessage(String htmlResponse) {
        String errorMessage = "Unknown error occurred";

        // Extract the message between <p><b>Message</b> and </p>
        String messageTag = "<p><b>Message</b>";
        int messageIndex = htmlResponse.indexOf(messageTag);
        if (messageIndex != -1) {
            int startIndex = htmlResponse.indexOf("</b>", messageIndex) + 4;
            int endIndex = htmlResponse.indexOf("</p>", startIndex);
            if (endIndex != -1) {
                errorMessage = htmlResponse.substring(startIndex, endIndex).trim();
            }
        }

        return errorMessage;
    }


    public void showError(String title, String message) {
        // Display an error message in the UI (e.g., using an alert dialog)
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessHint() {
        // Get the success label from the FXML
        Label successHintLabel = this.mainDashboardController.getDashboardHeaderController().getSuccessHintLabel();

        // Set the message and make it visible
        successHintLabel.setText("File uploaded successfully!");
        successHintLabel.setVisible(true);

        // Fade the label out after a few seconds
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(3), successHintLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> successHintLabel.setVisible(false));  // Hide it after fading out

        fadeTransition.play();
    }

    private Label getSuccessHintLabel() {
        return successHintLabel;
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setDashUserName(String userName) {
        dashUserName.setText(userName);
    }

    public String getDashUserName() {
        return dashUserName.getText();
    }
}
