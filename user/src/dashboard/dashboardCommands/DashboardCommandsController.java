package dashboard.dashboardCommands;

import com.google.gson.Gson;
import dashboard.dashboardTables.DashboardTablesController;
import dashboard.mainDashboardController.MainDashboardController;
import dto.PermissionRequestDTO;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DashboardCommandsController {
    private MainDashboardController mainDashboardController;
    private TableView<PermissionRequestDTO> requestTable;  // Make it class-level

    @FXML
    private Button commandsButton;

    private ContextMenu commandsMenu;

    @FXML
    public void initialize() {
        commandsMenu = new ContextMenu();
        fillCommandsMenu();

        // Set up event handler to show the context menu when the button is clicked
        commandsButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                commandsMenu.show(commandsButton, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void fillCommandsMenu() {
        MenuItem viewSheet = new MenuItem("View sheet");
        MenuItem requestPermission = new MenuItem("Request permission");
        MenuItem ackOrDenyPermissionRequest = new MenuItem("Acknowledge/deny permission request");

        viewSheet.setOnAction(event -> {
            try {
                handleViewSheet();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });

        requestPermission.setOnAction(event -> handleRequestPermission());
        ackOrDenyPermissionRequest.setOnAction(event -> handleAckOrDenyPermission());

        commandsMenu.getItems().addAll(viewSheet, requestPermission, ackOrDenyPermissionRequest);
    }


    private void handleViewSheet() throws UnsupportedEncodingException {
        // Get the selected sheet from DashboardTablesController
        DashboardTablesController.SheetRowData selectedSheet = mainDashboardController.getDashboardTablesController().getSelectedSheet();

        if (selectedSheet != null) {
            String sheetName = selectedSheet.getSheetName();
            String username = mainDashboardController.getDashboardHeaderController().getDashUserName();  // Current user's name

            // Fetch the user's permission for this sheet from the server
            fetchUserPermission(sheetName, username, permission -> {
                if (permission.equals("NONE")) {
                    this.mainDashboardController.getAppController().showError("No permissions.","You do not have permission to view this sheet.");;
                } else {
                    try {
                        // Call displaySheet with the permission type
                        mainDashboardController.loadSheetFromServer(sheetName, selectedSheet.getUploader(), permission);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            System.out.println("No sheet selected.");
        }
    }



    private void requestPermission(String sheetName, String requestedPermission, String uploaderName) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8080/server_Web/requestPermission";

        RequestBody requestBody = new FormBody.Builder()
                .add("sheetName", sheetName)
                .add("username", this.mainDashboardController.getDashboardHeaderController().getDashUserName())  // The user requesting access
                .add("permissionType", requestedPermission)  // READER or WRITER
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Permission request failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        System.out.println("Permission request submitted successfully!");
                        mainDashboardController.fetchSheetPermissions(sheetName, uploaderName);  // Refresh the permissions table

                    } else {
                        System.out.println("Failed to request permission: " + response.message());
                    }
                });
            }
        });
    }


    public void fetchUserPermission(String sheetName, String username, Consumer<String> callback) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8080/server_Web/getUserPermission?sheetName=" + sheetName + "&username=" + username;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch user permission: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Platform.runLater(() -> {
                    callback.accept(responseData);  // Pass the permission string to the callback
                });
            }
        });
    }



    private void handleRequestPermission() {
        System.out.println("Request permission clicked.");

        // Get the selected sheet from DashboardTablesController
        DashboardTablesController.SheetRowData selectedSheet = mainDashboardController.getDashboardTablesController().getSelectedSheet();

        if (selectedSheet != null) {
            String sheetName = selectedSheet.getSheetName();
            System.out.println("Selected sheet: " + sheetName);

            // Ask the user to choose the type of permission they want (READER or WRITER)
            ChoiceDialog<String> dialog = new ChoiceDialog<>("READER", "READER", "WRITER");
            dialog.setTitle("Request Permission");
            dialog.setHeaderText("Choose permission type for sheet: " + sheetName);
            dialog.setContentText("Permission type:");

            // Show the dialog and wait for the user response
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(permissionType -> {
                System.out.println("User chose permission type: " + permissionType);

                // Call the method to request the permission
                requestPermission(sheetName, permissionType,selectedSheet.getUploader());
            });
        } else {
            System.out.println("No sheet selected.");
        }
    }

    private void handleAckOrDenyPermission() {
        System.out.println("Acknowledge/deny permission request clicked.");

        // Get the selected sheet from DashboardTablesController
        DashboardTablesController.SheetRowData selectedSheet = mainDashboardController.getDashboardTablesController().getSelectedSheet();
        if (selectedSheet != null) {
            if(selectedSheet.getUploader().equalsIgnoreCase(this.mainDashboardController.getDashboardHeaderController().getDashUserName()))
            {
                String sheetName = selectedSheet.getSheetName();
                System.out.println("Selected sheet: " + sheetName);

                // Fetch the pending permission requests from the server
                fetchPendingRequests(sheetName);
            }
            else {
                this.mainDashboardController.getAppController().showError("","You must be the owner to acknowledge/deny permissions.");
            }
        } else {
            System.out.println("No sheet selected.");
        }
    }




    private void fetchPendingRequests(String sheetName) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8080/server_Web/getPendingRequests?sheetName=" + sheetName;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to fetch pending requests: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();

                // Parse the response into PermissionRequestDTO objects
                Gson gson = new Gson();
                PermissionRequestDTO[] pendingRequestsArray = gson.fromJson(responseData, PermissionRequestDTO[].class);

                // Show the pending requests in a popup
                Platform.runLater(() -> showPendingRequestsPopup(Arrays.asList(pendingRequestsArray), sheetName));
            }
        });
    }




    private void showPendingRequestsPopup(List<PermissionRequestDTO> pendingRequests, String sheetName) {
        TableView<DashboardTablesController.PermissionRowData> requestTable = new TableView<>();

        // Create columns
        TableColumn<DashboardTablesController.PermissionRowData, String> usernameCol = new TableColumn<>("Username");
        TableColumn<DashboardTablesController.PermissionRowData, String> permissionTypeCol = new TableColumn<>("Permission Type");
        TableColumn<DashboardTablesController.PermissionRowData, String> statusCol = new TableColumn<>("Status");
        TableColumn<DashboardTablesController.PermissionRowData, Void> actionCol = new TableColumn<>("Action");

        // Set cell value factories
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        permissionTypeCol.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up action column with Approve/Deny buttons
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button("Approve");
            private final Button denyButton = new Button("Deny");

            {
                approveButton.setOnAction(event -> {
                    DashboardTablesController.PermissionRowData rowData = getTableView().getItems().get(getIndex());
                    // Convert PermissionRowData to PermissionRequestDTO and handle approval
                    PermissionRequestDTO requestDTO = convertRowDataToDTO(rowData, sheetName);
                    handlePermissionApproval(requestDTO, "approve", requestTable);
                });

                denyButton.setOnAction(event -> {
                    DashboardTablesController.PermissionRowData rowData = getTableView().getItems().get(getIndex());
                    // Convert PermissionRowData to PermissionRequestDTO and handle denial
                    PermissionRequestDTO requestDTO = convertRowDataToDTO(rowData, sheetName);
                    handlePermissionApproval(requestDTO, "deny", requestTable);
                });


            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actionButtons = new HBox(approveButton, denyButton);
                    actionButtons.setSpacing(10);
                    setGraphic(actionButtons);
                }
            }
        });

        // Convert PermissionRequestDTO to PermissionRowData
        List<DashboardTablesController.PermissionRowData> rowDataList = pendingRequests.stream()
                .map(dto -> new DashboardTablesController.PermissionRowData(dto.getUsername(), dto.getPermissionType(), dto.getRequestStatus().name()))
                .collect(Collectors.toList());

        // Add columns to the table
        requestTable.getColumns().addAll(usernameCol, permissionTypeCol, statusCol, actionCol);
        requestTable.setItems(FXCollections.observableArrayList(rowDataList));

        // Display popup window
        Stage popupStage = new Stage();
        popupStage.setTitle("Pending Permission Requests");
        popupStage.setScene(new Scene(new VBox(requestTable), 600, 400));
        popupStage.show();
    }

    // Helper method to convert PermissionRowData back to PermissionRequestDTO for server-side actions
    private PermissionRequestDTO convertRowDataToDTO(DashboardTablesController.PermissionRowData rowData, String sheetName) {
        PermissionRequestDTO.RequestStatus isApproved = rowData.getStatus().equalsIgnoreCase("PENDING") ? PermissionRequestDTO.RequestStatus.PENDING : rowData.getStatus().equalsIgnoreCase("approved") ? PermissionRequestDTO.RequestStatus.APPROVED : PermissionRequestDTO.RequestStatus.DENIED;
        return new PermissionRequestDTO(rowData.getUserName(), rowData.getPermissionType(), isApproved, sheetName);
    }






    private void handlePermissionApproval(PermissionRequestDTO request, String decision, TableView<DashboardTablesController.PermissionRowData> requestTable) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8080/server_Web/acknowledgePermission";

        // Send the approve/deny decision to the server
        RequestBody requestBody = new FormBody.Builder()
                .add("sheetName", request.getSheetName())  // Sheet for which the permission is requested
                .add("username", request.getUsername())    // The user requesting the permission
                .add("decision", decision)                // "approve" or "deny"
                .build();

        Request httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to " + decision + " request: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        System.out.println("Permission request " + decision + "d successfully!");

                        // Update the UI by changing the row's status to approved
                        requestTable.getItems().stream()
                                .filter(row -> row.getUserName().equals(request.getUsername()))
                                .findFirst()
                                .ifPresent(row -> row.setStatus(decision));

                        // Optionally show a message to the owner
                        showSuccessHint("Request " + decision + "d for user: " + request.getUsername());
                        mainDashboardController.fetchSheetPermissions(request.getSheetName(), mainDashboardController.getDashboardHeaderController().getDashUserName());  // Refresh the permissions table

                    } else {
                        System.out.println("Failed to " + decision + " permission: " + response.message());
                    }
                });
            }
        });
    }


    private void showSuccessHint(String message) {
        // Show a label with the success message
        Label successLabel = new Label(message);
        successLabel.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-padding: 10;");
        successLabel.setVisible(true);

        // Add the label to your UI, assuming there's a Pane or some container for this
        mainDashboardController.getMainLayout().getChildren().add(successLabel);

        // Fade the label out after a few seconds
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(3), successLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> successLabel.setVisible(false));

        fadeTransition.play();
    }


    public void setMainController(MainDashboardController mainController) {
        this.mainDashboardController = mainController;
    }
}
