package dashboard.dashboardCommands;

import dashboard.dashboardTables.DashboardTablesController;
import dashboard.mainDashboardController.MainDashboardController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import java.io.UnsupportedEncodingException;

public class DashboardCommandsController {
    private MainDashboardController mainDashboardController;

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
        // Log to check if the method is triggered
        System.out.println("View sheet clicked");

        // Get the selected sheet from DashboardTablesController
        DashboardTablesController.SheetRowData selectedSheet = mainDashboardController.getDashboardTablesController().getSelectedSheet();

        // Check if a sheet is actually selected
        if (selectedSheet != null) {
            String sheetName = selectedSheet.getSheetName();
            String uploader = selectedSheet.getUploader();
            System.out.println("Selected sheet: " + sheetName);

            // Call the method to fetch and display the sheet from the server
            mainDashboardController.loadSheetFromServer(sheetName,uploader);
        } else {
            System.out.println("No sheet selected.");
        }
    }

    private void handleRequestPermission() {
        System.out.println("Request permission clicked.");
    }

    private void handleAckOrDenyPermission() {
        System.out.println("Acknowledge/deny permission request clicked.");
    }

    public void setMainController(MainDashboardController mainController) {
        this.mainDashboardController = mainController;
    }
}
