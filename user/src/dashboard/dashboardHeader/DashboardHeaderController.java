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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static httputils.Constants.UPLOAD_FILE;

public class DashboardHeaderController {

    @FXML
    private Button loadFileButton;

    @FXML
    private TextField filePathField;

 /*   @FXML
    private ProgressBar progressBar;*/

    @FXML
    private Label dashUserName;

    private Timeline fetchFilesTimeline;
    private Set<String> uploadedFilesSet = new HashSet<>(); // To track added files
    private MainDashboardController mainDashboardController;  // Reference to MainDashboardController

    private static final String UPLOAD_URL = "http://localhost:8080/server_Web/uploadFile";  // Your upload servlet URL

    public void initialize() {
        loadFileButton.setOnAction(event -> handleLoadFile());
       // progressBar.setVisible(false);

        // Add Timeline for polling every 2 seconds
        startFetchingFiles(); // Start fetching the uploaded files every 2 seconds
    }

    private void startFetchingFiles() {
        fetchFilesTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> fetchUploadedFiles()));
        fetchFilesTimeline.setCycleCount(Timeline.INDEFINITE); // Run indefinitely every 2 seconds
        fetchFilesTimeline.play();
    }

    private void fetchUploadedFiles() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:8080/server_Web/getUploadedFiles")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch uploaded files: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
                        System.out.println("No files uploaded yet.");
                        return;
                    }

                    // Clear any existing entries
                    mainDashboardController.getDashboardTablesController().clearTable1();

                    // Iterate over the map and add sheets to the table
                    for (SpreadsheetManagerDTO dto : spreadsheetManagerMap.values()) {
                        String sheetName = dto.getSpreadsheetDTO().getSheetName();
                        String uploader = dto.getUploaderName();  // Assuming you added this field to SpreadsheetManagerDTO
                        String sheetSize = dto.getSpreadsheetDTO().getRows() + "x" + dto.getSpreadsheetDTO().getColumns();

                        // Add the sheet details to the first table
                        mainDashboardController.getDashboardTablesController().addSheet(uploader, sheetName, sheetSize);
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
        } else {
            System.out.println("No file selected");
        }
    }

    private void uploadFileToServer(File file) {
     //   progressBar.setVisible(true);
       // progressBar.setProgress(0.0);

        String uploaderName = dashUserName.getText();  // The uploader's name
        String filePath = file.getAbsolutePath();  // Get the absolute file path
        String url = UPLOAD_URL;  // The server upload URL

        // Use the new uploadFileAsync method
        HttpClientUtil.uploadFileAsync(url, file, filePath, uploaderName, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                  //  progressBar.setProgress(0.0);
                    System.out.println("Upload failed: " + e.getMessage());
                    // Show error message to the user (you can replace this with a proper UI message)
                    showError("Upload Error", "Failed to upload the file: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Platform.runLater(() -> {
                //    progressBar.setProgress(1.0);
                    if (response.isSuccessful()) {
                        System.out.println("File uploaded successfully!");
                        showSuccessHint("File uploaded successfully!");

                        if (mainDashboardController != null) {
                            fetchUploadedFiles();  // Fetch updated files after successful upload
                        }
                    } else {
                        // Extract the error message from the response body
                        String responseBody;
                        try {
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
            if (startIndex != -1 && endIndex != -1) {
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

    private void showSuccessHint(String message) {
        System.out.println("Attempting to show success message: " + message);  // Debugging line

        // Create the label with the success message
        Label successLabel = new Label(message);
        successLabel.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-padding: 10;");
        successLabel.setVisible(true);

        VBox header = (VBox) this.mainDashboardController.getMainLayout().getTop();
        // Add the label to the top of the main layout (or centerHBox, adjust as needed)
        this.mainDashboardController.getMainLayout().setTop(successLabel);

        // Fade the label out after a few seconds
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(3), successLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> {
            successLabel.setVisible(false);
            this.mainDashboardController.getMainLayout().setTop(header); // Remove the label after fading out
        });

        fadeTransition.play();

        System.out.println("Success message added to the UI");  // Debugging line
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
