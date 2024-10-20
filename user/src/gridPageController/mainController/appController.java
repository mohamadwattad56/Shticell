package gridPageController.mainController;
import dashboard.mainDashboardController.MainDashboardController;
import gridPageController.CommandRangesController.CommandAndRangesController;
import dto.CellDTO;
import gridPageController.gridController.SpreadsheetController;
import gridPageController.headController.HeadController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;

public class appController {

    @FXML
    private BorderPane mainContainer;
    @FXML
    private ScrollPane scrollPane;
    private HeadController headController;
    private CommandAndRangesController commandAndRangesController;
    private SpreadsheetController spreadsheetController;
    private String currentSkin = "Light";  // Set default skin to "Light"
    private MainDashboardController mainDashboardController;

    @FXML
    public void initialize() throws IOException {
        // Load the HeaderController
        FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/gridPageController/headController/HeaderController.fxml"));
        VBox headerComponent = headerLoader.load();
        headController = headerLoader.getController();  // Set header controller

        // Load the CommandAndRangesController
        FXMLLoader commandRangesLoader = new FXMLLoader(getClass().getResource("/gridPageController/CommandRangesController/CommandAndRanges.fxml"));
        VBox commandRangesComponent = commandRangesLoader.load();
        commandAndRangesController = commandRangesLoader.getController();  // Set command & ranges controller

        // Load the SpreadsheetController
        FXMLLoader spreadsheetLoader = new FXMLLoader(getClass().getResource("/gridPageController/gridController/GridController.fxml"));
        GridPane spreadsheetComponent = spreadsheetLoader.load();
        spreadsheetController = spreadsheetLoader.getController();  // Set spreadsheet controller

        // Access the BorderPane inside the ScrollPane
        mainContainer.setTop(headerComponent);
        mainContainer.setLeft(commandRangesComponent);
        mainContainer.setCenter(spreadsheetComponent);

        // Connect subcomponents to appController
        setHeadController(headController);
        setCommandAndRangesController(commandAndRangesController);
        setSpreadsheetController(spreadsheetController);

        // Pass HeadController to SpreadsheetController
        spreadsheetController.setHeadController(headController);
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public MainDashboardController getMainDashboardController() {
        return mainDashboardController;
    }

    public BorderPane getMainContainer() {
        return mainContainer;
    }

    public HeadController getHeadController() {
        return headController;
    }

    public void setHeadController(HeadController headController) {
        this.headController = headController;
        headController.setMainController(this);
    }

    public void setCommandAndRangesController(CommandAndRangesController commandAndRangesController) {
        this.commandAndRangesController = commandAndRangesController;
        commandAndRangesController.setMainController(this);
    }

    public void setSpreadsheetController(SpreadsheetController spreadsheetController) {
        this.spreadsheetController = spreadsheetController;
        spreadsheetController.setMainController(this);
    }

    public SpreadsheetController getSpreadsheetController() {
        return spreadsheetController;
    }

    public CommandAndRangesController getCommandAndRangesController() {
        return commandAndRangesController;
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void changeSkin(String skin) {
        this.currentSkin = skin;  // Update the current skin
        String buttonClass;
        String labelClass;
        String cellClass;

        switch (skin) {
            case "Light":
                buttonClass = "custom-button";
                labelClass = "custom-label";
                cellClass = "light-cell";
                break;
            case "Dark":
                buttonClass = "dark-button";
                labelClass = "dark-label";
                cellClass = "dark-cell";
                break;
            case "Colorful":
                buttonClass = "colorful-button";
                labelClass = "colorful-label";
                cellClass = "colorful-cell";
                break;
            default:
                return;  // If no valid skin is selected, return without changes
        }

        // Apply the correct CSS class to all buttons
        applyButtonStyles(buttonClass);
        // Apply the correct CSS class to all labels
        applyLabelStyles(labelClass);
        // Apply the correct CSS class to all cells
        applyCellStyles(cellClass);
    }

    private void applyCellStyles(String cellClass) {
        for (Node node : this.getSpreadsheetController().getGridPane().getChildren()) {
            if (node instanceof Label && GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null) {
                int rowIndex = GridPane.getRowIndex(node);
                int columnIndex = GridPane.getColumnIndex(node);

                // Only apply styles to actual data cells, not the first row and column
                if (rowIndex > 0 && columnIndex > 0) {
                    Label cellLabel = (Label) node;
                    String cellIdentifier = this.spreadsheetController.getCellIdFromCoordinates(rowIndex, columnIndex);

                    // Get the cell object from the spreadsheet
                    CellDTO cell = this.getSpreadsheetController().getSpreadsheet().getCellDTO(cellIdentifier);

                    // Check if the text color is black and background color is white
                    if (isBlack(cell.getTextColor()) && isWhite(cell.getBackgroundColor())) {
                        cellLabel.getStyleClass().clear();  // Clear existing styles
                        cellLabel.getStyleClass().add(cellClass);  // Add the new style class for the cell

                        // Apply the relevant text color based on the skin
                        switch (cellClass) {
                            case "light-cell" -> cellLabel.setStyle("-fx-text-fill: black;");
                            case "dark-cell" -> cellLabel.setStyle("-fx-text-fill: white;");
                            case "colorful-cell" -> cellLabel.setStyle("-fx-text-fill: black;");
                        }
                    }
                }
            }
        }
    }

    private boolean isBlack(String color) {
        return color.equalsIgnoreCase("black") || color.equalsIgnoreCase("#000000");
    }

    private boolean isWhite(String color) {
        return color.equalsIgnoreCase("white") || color.equalsIgnoreCase("#FFFFFF");
    }

     private void applyButtonFadeIn(Button button) {
         FadeTransition fadeIn = new FadeTransition(Duration.millis(500), button);
         fadeIn.setFromValue(0);
         fadeIn.setToValue(1);
         fadeIn.play();
     }

    private void applyButtonStyles(String buttonClass) {
        Node leftSection = mainContainer.getLeft();
        Node headerSection = mainContainer.getTop();
            // For other button classes, apply as before
            if (leftSection instanceof VBox) {
                for (Node node : ((VBox) leftSection).getChildren()) {
                    if (node instanceof Button button) {
                        button.getStyleClass().clear();
                        button.getStyleClass().add(buttonClass);
                        button.getStyleClass().add("button"); // Ensure basic button styling is applied
                        applyButtonFadeIn(button);
                    }
                }
            }

            // Apply to buttons in the header section
            if (headerSection instanceof VBox) {
                for (Node node : ((VBox) headerSection).getChildren()) {
                    if (node instanceof HBox) {
                        for (Node child : ((HBox) node).getChildren()) {
                            if (child instanceof Button) {
                                Button button = (Button) child;
                                button.getStyleClass().clear();
                                button.getStyleClass().add(buttonClass);
                                button.getStyleClass().add("button"); // Ensure basic button styling is applied
                                button.setStyle("-fx-padding: 3px 8px;");
                                applyButtonFadeIn(button);

                            }
                        }
                    }
                }
            }

    }

    private void applyLabelStyles(String labelClass) {
        Node headerSection = mainContainer.getTop();
        for (Node node : ((VBox) headerSection).getChildren()) {
            if (node instanceof HBox) {
                for (Node child : ((HBox) node).getChildren()) {
                    if (child instanceof Label) {
                        if (!((Label) child).getText().equals("Shticell")) {
                            Label label = (Label) child;
                            label.getStyleClass().clear();  // Clear existing styles
                            label.getStyleClass().add(labelClass);
                        }
                    }
                }
            }
        }
    }

    public String getCellClassForCurrentSkin() {
        return switch (currentSkin) {
            case "Light" -> "light-cell";
            case "Dark" -> "dark-cell";
            case "Colorful" -> "colorful-cell";
            default -> "light-cell";  // Default to light cell if no skin is selected
        };
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
