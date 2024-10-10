package dashboard.dashboardHeader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dashboard.dashboardTables.DashboardTablesController;
import dashboard.mainDashboardController.MainDashboardController;
import dto.SpreadsheetManagerDTO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label dashUserName;

    private Timeline fetchFilesTimeline;
    private Set<String> uploadedFilesSet = new HashSet<>(); // To track added files
    private MainDashboardController mainDashboardController;  // Reference to MainDashboardController

    private static final String UPLOAD_URL = "http://localhost:8080/server_Web/uploadFile";  // Your upload servlet URL

    public void initialize() {
        loadFileButton.setOnAction(event -> handleLoadFile());
        progressBar.setVisible(false);

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
        progressBar.setVisible(true);
        progressBar.setProgress(0.0);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("application/xml")))
                .addFormDataPart("filePath", file.getAbsolutePath())  // Add the file path as form data
                .addFormDataPart("uploaderName", dashUserName.getText()) // Pass the uploader's name to the server
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    progressBar.setProgress(0.0);
                    System.out.println("Upload failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    if (response.isSuccessful()) {
                        System.out.println("File uploaded successfully!");

                        if (mainDashboardController != null) {
                            fetchUploadedFiles();  // Fetch updated files after successful upload
                        }
                    } else {
                        System.out.println("Upload failed: " + response.message());
                        String responseBody = null;
                        try {
                            responseBody = response.body().string();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Response body: " + responseBody);
                    }
                });
            }
        });
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
