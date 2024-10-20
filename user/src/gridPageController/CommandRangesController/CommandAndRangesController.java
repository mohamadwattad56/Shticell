package gridPageController.CommandRangesController;
import dto.CellDTO;
import gridPageController.mainController.appController;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class CommandAndRangesController {

    @FXML
    private Button commandsTextArea;

    @FXML
    private Button rangesTextArea;

    private ContextMenu commandsMenu;

    private ContextMenu rangesMenu;

    private gridPageController.mainController.appController appController;

    private static class FilterCriteria {
        private final String column;
        private final List<String> values;

        public FilterCriteria(String column, List<String> values) {
            this.column = column;
            this.values = values;
        }

        public String getColumn() {
            return column;
        }

        public List<String> getValues() {
            return values;
        }
    }

    @FXML
    public void initialize() {
        rangesMenu = new ContextMenu();
        fillRangesMenu();
        commandsMenu = new ContextMenu();
        fillCommandsMenu();
    }

    @FXML
    public void resetSelectedCellDesign() {
        // Create a new stage for the cell selection dialog
        Stage cellSelectionStage = new Stage();
        cellSelectionStage.setTitle("Reset Cell Design");

        // Layout and padding for the stage
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // Instruction label
        Label instructionLabel = new Label("Select a cell:");
        ComboBox<String> cellComboBox = new ComboBox<>();
        cellComboBox.setPromptText("Select cell");

        // Populate ComboBox with cell IDs
        for (int row = 1; row <= appController.getSpreadsheetController().getSpreadsheet().getNumOfRows(); row++) {
            for (int col = 1; col <= appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns(); col++) {
                String cellId = this.appController.getSpreadsheetController().getCellIdFromCoordinates(row, col);
                cellComboBox.getItems().add(cellId);
            }
        }

        // Reset Button
        Button resetButton = new Button("Reset Design");
        resetButton.setDisable(true); // Disable the button until a cell is selected

        // Enable the reset button when a cell is selected
        cellComboBox.setOnAction(event -> resetButton.setDisable(cellComboBox.getValue() == null));

        // Reset button action
        resetButton.setOnAction(event -> {
            String selectedCellId = cellComboBox.getValue();

            // Confirmation dialog
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirm Reset");
            confirmationDialog.setHeaderText("Are you sure you want to reset the design for cell: " + selectedCellId + "?");

            // Wait for user confirmation
            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Reset the cell design
                appController.getSpreadsheetController().resetCellFormatting(selectedCellId);
                cellSelectionStage.close(); // Close the selection dialog
            } else {
                // If Cancel or Close was pressed, do nothing
                confirmationDialog.close();
            }
        });

        // Cancel button to close the dialog
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> cellSelectionStage.close());

        // Add all elements to the layout
        HBox buttonBox = new HBox(10, cancelButton, resetButton);
        layout.getChildren().addAll(instructionLabel, cellComboBox, buttonBox);

        // Set the scene and show the stage
        Scene scene = new Scene(layout, 300, 200);
        cellSelectionStage.setScene(scene);
        cellSelectionStage.show();
    }

    @FXML
    public void showDeleteRangePopup() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Delete Range");

        VBox popupLayout = new VBox(10);
        popupLayout.setPadding(new Insets(20));

        ComboBox<String> rangeComboBox = new ComboBox<>();
        rangeComboBox.setPromptText("Select range");
        rangeComboBox.getItems().addAll(appController.getSpreadsheetController().getSpreadsheet().getAllRangeNames());

        Button deleteButton = new Button("Delete");
        deleteButton.setDisable(true);

        rangeComboBox.setOnAction(event -> deleteButton.setDisable(rangeComboBox.getValue() == null));

        deleteButton.setOnAction(event -> {
            String selectedRange = rangeComboBox.getValue();

            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirm Delete");
            confirmationDialog.setHeaderText("Are you sure you want to delete the range: " + selectedRange + "?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean isDeleted = appController.getSpreadsheetController().getSpreadsheet().deleteRange(selectedRange);
                if (isDeleted) {
                    popupStage.close(); // Close the delete range popup
                } else {
                    appController.showError("Error", "Could not delete the range.");
                }
            } else {
                confirmationDialog.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> popupStage.close());

        popupLayout.getChildren().addAll(new Label("Select range to delete:"), rangeComboBox, new HBox(10, cancelButton, deleteButton));
        Scene popupScene = new Scene(popupLayout, 300, 200);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    @FXML
    public void showDisplayRangePopup() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Display Range");

        VBox popupLayout = new VBox(10);
        popupLayout.setPadding(new Insets(20));

        ComboBox<String> rangeComboBox = new ComboBox<>();
        rangeComboBox.setPromptText("Select range");
        rangeComboBox.getItems().addAll(appController.getSpreadsheetController().getSpreadsheet().getAllRangeNames());

        Button displayButton = new Button("Display");
        displayButton.setDisable(true);

        rangeComboBox.setOnAction(event -> displayButton.setDisable(rangeComboBox.getValue() == null));

        displayButton.setOnAction(event -> {
            String selectedRange = rangeComboBox.getValue();
            Set<String> cellIdsInRange = appController.getSpreadsheetController().getSpreadsheet().getRangeCells(selectedRange);

            if (cellIdsInRange != null) {
                appController.getSpreadsheetController().highlightCellsInRange(cellIdsInRange);
                popupStage.close();
            } else {
                appController.showError("", "No range found for the selected range: " + selectedRange);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> popupStage.close());

        popupLayout.getChildren().addAll(new Label("Select range to display:"), rangeComboBox, new HBox(10, cancelButton, displayButton));
        Scene popupScene = new Scene(popupLayout, 300, 200);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    @FXML
    public void showCommandsMenu() {
        // Get the button's screen coordinates
        double x = commandsTextArea.localToScreen(commandsTextArea.getBoundsInLocal()).getMinX();
        double y = (commandsTextArea.localToScreen(commandsTextArea.getBoundsInLocal()).getMaxY()) -30;

        // Show the ContextMenu near the button
        commandsMenu.show(commandsTextArea, x, y);
    }

    @FXML
    private void showRangesMenu() {
        rangesMenu.show(rangesTextArea, Side.BOTTOM, 0, 0);
    }

    @FXML
    public void showAlignmentMenu() {
        int columnsNum = this.appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns();
        // Create a new Stage for the alignment popup
        Stage popupStage = new Stage();
        popupStage.setTitle("Align Text");

        // Main layout - VBox with padding and spacing
        VBox popupLayout = new VBox(20);
        popupLayout.setPadding(new Insets(20));
        popupLayout.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-radius: 10px; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, #888, 10, 0, 0, 0);");

        // Instruction Label
        Label instructionLabel = new Label("Select a column for alignment:");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        // ComboBox for column selection
        ComboBox<String> columnComboBox = new ComboBox<>();
        columnComboBox.setPromptText("Choose Column");
        for(int i = 0; i < columnsNum; i++) {
            String columnLetter = String.valueOf((char) ('A' + (i)));
            columnComboBox.getItems().add(columnLetter);
        }
       // columnComboBox.getItems().addAll("A", "B", "C");  // Add column options dynamically

        // Alignment options (Checkboxes)
        CheckBox leftCheckBox = new CheckBox("Left");
        CheckBox centerCheckBox = new CheckBox("Center");
        CheckBox rightCheckBox = new CheckBox("Right");

        // Disable checkboxes by default until a column is selected
        leftCheckBox.setDisable(true);
        centerCheckBox.setDisable(true);
        rightCheckBox.setDisable(true);

        // Enable checkboxes when a column is selected
        columnComboBox.setOnAction(event -> {
            if (columnComboBox.getValue() != null) {
                leftCheckBox.setDisable(false);
                centerCheckBox.setDisable(false);
                rightCheckBox.setDisable(false);
            }
        });

        // Ensure only one checkbox is selected at a time
        leftCheckBox.setOnAction(event -> {
            if (leftCheckBox.isSelected()) {
                centerCheckBox.setSelected(false);
                rightCheckBox.setSelected(false);
            }
        });
        centerCheckBox.setOnAction(event -> {
            if (centerCheckBox.isSelected()) {
                leftCheckBox.setSelected(false);
                rightCheckBox.setSelected(false);
            }
        });
        rightCheckBox.setOnAction(event -> {
            if (rightCheckBox.isSelected()) {
                leftCheckBox.setSelected(false);
                centerCheckBox.setSelected(false);
            }
        });

        // Cancel and OK buttons
        Button cancelButton = new Button("Cancel");
        Button okButton = new Button("OK");

        // Button styles
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        okButton.setDisable(true); // Initially disabled

        // Enable OK button only when an alignment option is selected
        leftCheckBox.setOnAction(e -> okButton.setDisable(!leftCheckBox.isSelected()));
        centerCheckBox.setOnAction(e -> okButton.setDisable(!centerCheckBox.isSelected()));
        rightCheckBox.setOnAction(e -> okButton.setDisable(!rightCheckBox.isSelected()));

        cancelButton.setOnAction(event -> popupStage.close());


        okButton.setOnAction(event -> {
            // Determine which alignment was selected
            Pos alignment;
            if (leftCheckBox.isSelected()) {
                alignment = Pos.CENTER_LEFT;
            } else if (centerCheckBox.isSelected()) {
                alignment = Pos.CENTER;
            } else {
                alignment = Pos.CENTER_RIGHT;
            }

            // Apply alignment to the column in SpreadsheetController
            appController.getSpreadsheetController().applyColumnAlignment(columnComboBox.getValue(), alignment);

            popupStage.close();
        });

        // HBox for buttons
        HBox buttonBox = new HBox(10, cancelButton, okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add components to the layout
        popupLayout.getChildren().addAll(instructionLabel, columnComboBox, leftCheckBox, centerCheckBox, rightCheckBox, buttonBox);

        // Set the layout on the scene and show the popup
        Scene popupScene = new Scene(popupLayout, 350, 250);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    private void fillRangesMenu() {
        MenuItem addRange = new MenuItem("Add Range");
        MenuItem deleteRange = new MenuItem("Delete Range");
        MenuItem displayRange = new MenuItem("Display Range");
        addRange.setOnAction(event -> showAddRangePopup());
        deleteRange.setOnAction(event -> showDeleteRangePopup());
        displayRange.setOnAction(event -> showDisplayRangePopup());
        rangesMenu.getItems().addAll(addRange, deleteRange, displayRange);
    }

    private void fillCommandsMenu() {
        MenuItem setColumnOrRowsWidth = new MenuItem("Set Column/Row size");
        MenuItem alignText = new MenuItem("Align Text (Left, Center, Right)");
        MenuItem setCellDesign = new MenuItem("Set Cell Background/Text Color");
        MenuItem resetCellDesign = new MenuItem("Reset Cell Design");
        MenuItem sortOption = new MenuItem("Sort");  // New Sort Option
        MenuItem filterOption = new MenuItem("Filter");
        MenuItem barGraphOption = new MenuItem("Create Graph");


        // Add event handlers
        setColumnOrRowsWidth.setOnAction(event -> showColumnRowWidthChangePopup());
        alignText.setOnAction(event -> showAlignmentMenu());
        sortOption.setOnAction(event -> showSortPopup());  // Handle Sort Option
        filterOption.setOnAction(event -> showFilterPopup());
        setCellDesign.setOnAction(event -> showCellSelectionDialogForColor());
        resetCellDesign.setOnAction(event -> resetSelectedCellDesign());
        barGraphOption.setOnAction(event -> showGraphPopup());

        // Add items to the context menu
        commandsMenu.getItems().addAll(setColumnOrRowsWidth, alignText, setCellDesign, resetCellDesign, sortOption,filterOption,barGraphOption);
    }

    private void showGraphPopup() {
        Stage graphPopupStage = new Stage();
        graphPopupStage.setTitle("Create Graph");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20px;");

        // X-Axis Range Selection
        Label xAxisLabel = new Label("Select 'From' cell for X-axis:");
        ComboBox<String> fromXComboBox = new ComboBox<>();
        fromXComboBox.setPromptText("Select starting cell for X-axis");
        this.appController.getSpreadsheetController().populateCellReferences(fromXComboBox);

        Label toXLabel = new Label("Select 'To' cell for X-axis:");
        ComboBox<String> toXComboBox = new ComboBox<>();
        toXComboBox.setPromptText("Select ending cell for X-axis");

        // Populate the "To" options for X-axis
        fromXComboBox.setOnAction(event -> {
            String selectedFrom = fromXComboBox.getValue();
            if (selectedFrom != null) {
                populateValidToOptions(selectedFrom, toXComboBox); // Populate valid "To" cells based on "From"
            }
        });

        // Y-Axis Range Selection
        Label yAxisLabel = new Label("Select 'From' cell for Y-axis:");
        ComboBox<String> fromYComboBox = new ComboBox<>();
        fromYComboBox.setPromptText("Select starting cell for Y-axis");
        this.appController.getSpreadsheetController().populateCellReferences(fromYComboBox);

        Label toYLabel = new Label("Select 'To' cell for Y-axis:");
        ComboBox<String> toYComboBox = new ComboBox<>();
        toYComboBox.setPromptText("Select ending cell for Y-axis");

        fromYComboBox.setOnAction(event -> {
            String selectedFrom = fromYComboBox.getValue();
            if (selectedFrom != null) {
                populateValidToOptions(selectedFrom, toYComboBox);
            }
        });

        // Graph Type Selection
        Label graphTypeLabel = new Label("Select graph type:");
        ComboBox<String> graphTypeComboBox = new ComboBox<>();
        graphTypeComboBox.getItems().addAll("Bar Graph", "Line Graph");
        graphTypeComboBox.setPromptText("Choose graph type");

        Button createGraphButton = new Button("Create Graph");
        createGraphButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        createGraphButton.setOnAction(event -> {
            String fromXCell = fromXComboBox.getValue();
            String toXCell = toXComboBox.getValue();
            String fromYCell = fromYComboBox.getValue();
            String toYCell = toYComboBox.getValue();
            String graphType = graphTypeComboBox.getValue();

            if (fromXCell == null || toXCell == null || fromYCell == null || toYCell == null || graphType == null) {
                errorLabel.setText("Please select all ranges and graph type.");
            } else {
                createGraph(fromXCell, toXCell, fromYCell, toYCell, graphType);
                graphPopupStage.close();
            }
        });

        layout.getChildren().addAll(xAxisLabel, fromXComboBox, toXLabel, toXComboBox,
                yAxisLabel, fromYComboBox, toYLabel, toYComboBox,
                graphTypeLabel, graphTypeComboBox, createGraphButton, errorLabel);

        Scene scene = new Scene(layout, 400, 400);
        graphPopupStage.setScene(scene);
        graphPopupStage.show();
    }

    public void createGraph(String fromXCell, String toXCell, String fromYCell, String toYCell, String graphType) {
        List<Double> xData = extractDataFromRange(fromXCell, toXCell);
        List<Double> yData = extractDataFromRange(fromYCell, toYCell);

        if (xData.isEmpty() || yData.isEmpty()) {
            appController.showError("Invalid Range", "Please enter valid ranges for X and Y axes.");
            return;
        }

        // Ensure both X and Y data sets have the same size
        if (xData.size() != yData.size()) {
            appController.showError("Mismatched Data", "The number of X values and Y values must be equal.");
            return;
        }

        // Create the graph with fade-in
        if (graphType.equals("Bar Graph")) {
            showBarGraph(xData, yData);
        } else if (graphType.equals("Line Graph")) {
            showLineGraph(xData, yData);
        }
    }

    private List<Double> extractDataFromRange(String fromCell, String toCell) {
        List<Double> dataList = new ArrayList<>();

        // Extract row and column indices from the cell references
        int startRow = getRowIndex(fromCell);
        int endRow = getRowIndex(toCell);
        String column = getColumnIndex(fromCell);  // Assuming column doesn't change for the range

        for (int row = startRow; row <= endRow; row++) {
            String cellId = column + row;
            CellDTO cell = this.appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId);
            Object effectiveValue = cell.getEffectiveValue();

            // Check if effectiveValue is a number or a string that represents a number
            if (effectiveValue instanceof Number) {
                dataList.add(((Number) effectiveValue).doubleValue());
            } else if (effectiveValue instanceof String) {
                try {
                    // Try to parse the string as a double
                    double numericValue = Double.parseDouble((String) effectiveValue);
                    dataList.add(numericValue);
                } catch (NumberFormatException e) {
                    // If the string cannot be parsed to a number, show an error and stop
                    appController.showError("Invalid Data", "Cell " + cellId + " contains non-numerical data: " + effectiveValue);
                    return new ArrayList<>();  // Return empty list in case of invalid data
                }
            } else {
                // If neither, handle invalid data
                appController.showError("Invalid Data", "Cell " + cellId + " contains non-numerical data.");
                return new ArrayList<>();  // Return empty list in case of invalid data
            }
        }

        return dataList;
    }

    private void showBarGraph(List<Double> xData, List<Double> yData) {
        // Create a new stage for displaying the bar chart
        Stage stage = new Stage();
        stage.setTitle("Bar Graph");

        // Create the X and Y axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X Axis");

        // Create the BarChart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Bar Graph");

        // Add data to the bar chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < xData.size(); i++) {
            series.getData().add(new XYChart.Data<>(xData.get(i).toString(), yData.get(i)));
        }

        barChart.getData().add(series);

        // Show the chart
        Scene scene = new Scene(barChart, 800, 600);
        stage.setScene(scene);
        applyFadeInEffect(stage);

        stage.show();
    }

    private void showLineGraph(List<Double> xData, List<Double> yData) {
        // Similar to showBarGraph, but create a LineChart instead of a BarChart
        Stage stage = new Stage();
        stage.setTitle("Line Graph");

        // Create the X and Y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X Axis");

        // Create the LineChart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Line Graph");

        // Add data to the line chart
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < xData.size(); i++) {
            series.getData().add(new XYChart.Data<>(xData.get(i), yData.get(i)));
        }

        lineChart.getData().add(series);

        // Show the chart
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        applyFadeInEffect(stage);

        stage.show();
    }

    private void applyFadeInEffect(Stage stage) {
        // Create a fade-in effect that lasts 500 milliseconds
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), stage.getScene().getRoot());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private int getRowIndex(String cellReference) {
        // Extract row number from cell reference (e.g., A1 -> 1)
        return Integer.parseInt(cellReference.replaceAll("[^0-9]", ""));
    }

    private String getColumnIndex(String cellReference) {
        // Extract column letter(s) from cell reference (e.g., A1 -> A)
        return cellReference.replaceAll("[^A-Za-z]", "");
    }

    public void showCellSelectionDialogForColor() {
        // Create a new stage for the dialog
        Stage cellSelectionStage = new Stage();
        cellSelectionStage.setTitle("Select Cell to Change Color");

        // Main layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // Label for instructions
        Label instructionLabel = new Label("Select a cell:");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        // ComboBox to choose a cell
        ComboBox<String> cellComboBox = new ComboBox<>();
        cellComboBox.setPromptText("Select cell");

        for (int row = 1; row <= appController.getSpreadsheetController().getSpreadsheet().getNumOfRows(); row++) {
            for (int col = 1; col <= appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns(); col++) {
                String cellId = this.appController.getSpreadsheetController().getCellIdFromCoordinates(row, col); // Assuming this method exists
                cellComboBox.getItems().add(cellId);
            }
        }

        // OK Button
        Button okButton = new Button("OK");
        okButton.setDisable(true);  // Initially disabled until a cell is selected

        // Enable the OK button when a cell is selected
        cellComboBox.setOnAction(event -> okButton.setDisable(cellComboBox.getValue() == null));

        // OK button action
        okButton.setOnAction(event -> {
            String selectedCellId = cellComboBox.getValue();
            if (selectedCellId != null) {
                showColorPickerForCell(selectedCellId); // Show the color picker for the selected cell
                cellSelectionStage.close();
            }
        });

        // Cancel button to close the dialog
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> cellSelectionStage.close());

        // Layout for buttons
        HBox buttonBox = new HBox(10, cancelButton, okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add components to the layout
        layout.getChildren().addAll(instructionLabel, cellComboBox, buttonBox);

        // Set the scene and show the stage
        Scene scene = new Scene(layout, 300, 200);
        cellSelectionStage.setScene(scene);
        cellSelectionStage.show();
    }

    public void showColorPickerForCell(String cellId) {
        // Create a new stage for the color picker
        Stage colorPickerStage = new Stage();
        colorPickerStage.setTitle("Set Cell Color for " + cellId);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // Text color picker
        Label textColorLabel = new Label("Select Text Color:");
        ColorPicker textColorPicker = new ColorPicker(Color.BLACK); // Default color

        // Background color picker
        Label backgroundColorLabel = new Label("Select Background Color:");
        ColorPicker backgroundColorPicker = new ColorPicker(Color.WHITE); // Default color

        // OK button to apply the colors
        Button applyButton = new Button("Apply");
        applyButton.setOnAction(event -> {
            Color textColor = textColorPicker.getValue();
            Color backgroundColor = backgroundColorPicker.getValue();

            // Apply colors to the cell
            appController.getSpreadsheetController().applyCellTextColor(cellId, textColor);
            appController.getSpreadsheetController().applyCellBackgroundColor(cellId, backgroundColor);

            colorPickerStage.close(); // Close the color picker stage
        });

        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> colorPickerStage.close());

        // Button layout
        HBox buttonBox = new HBox(10, cancelButton, applyButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add components to the layout
        layout.getChildren().addAll(textColorLabel, textColorPicker, backgroundColorLabel, backgroundColorPicker, buttonBox);

        Scene scene = new Scene(layout, 400, 300);
        colorPickerStage.setScene(scene);
        colorPickerStage.show();
    }

    private void showSortPopup() {
        Stage sortPopupStage = new Stage();
        sortPopupStage.setTitle("Sort Table");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20px;");

        Label fromLabel = new Label("Select 'From' cell:");
        ComboBox<String> fromComboBox = new ComboBox<>();
        fromComboBox.setPromptText("Select the starting cell");
        this.appController.getSpreadsheetController().populateCellReferences(fromComboBox); // Populate with cell references

        Label toLabel = new Label("Select 'To' cell:");
        ComboBox<String> toComboBox = new ComboBox<>();
        toComboBox.setPromptText("Select the ending cell");

        // When "From" is selected, populate valid "To" options
        fromComboBox.setOnAction(event -> {
            String selectedFrom = fromComboBox.getValue();
            if (selectedFrom != null) {
                populateValidToOptions(selectedFrom, toComboBox); // Populate "To" options based on "From"
            }
        });

        // Dynamic sort options
        VBox sortColumnsBox = new VBox(10);
        Label sortByLabel = new Label("Sort by:");
        ComboBox<String> firstSortColumn = createColumnDropdown(fromComboBox, toComboBox); // Create the first sort dropdown

        // Populate the first column dropdown when "To" is selected
        toComboBox.setOnAction(event -> {
            String fromCell = fromComboBox.getValue();
            String toCell = toComboBox.getValue();
            if (fromCell != null && toCell != null) {
                populateColumnDropdown(firstSortColumn, fromCell, toCell, getSelectedColumns(sortColumnsBox)); // Explicitly populate the first sort dropdown
            }
        });

        sortColumnsBox.getChildren().addAll(sortByLabel, firstSortColumn);

        Button addSortColumnButton = new Button("Add another sort column");
        addSortColumnButton.setOnAction(event -> {
            ComboBox<String> nextSortColumn = createColumnDropdown(fromComboBox, toComboBox);
            populateColumnDropdown(nextSortColumn, fromComboBox.getValue(), toComboBox.getValue(), getSelectedColumns(sortColumnsBox)); // Populate the new dropdown
            sortColumnsBox.getChildren().add(nextSortColumn);
        });

        // Option to sort by all columns
        CheckBox sortAllColumnsCheckbox = new CheckBox("Sort by all columns");

        Button okButton = new Button("Sort");
        okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        // Sorting logic on OK button
        okButton.setOnAction(event -> {
            String fromCell = fromComboBox.getValue();
            String toCell = toComboBox.getValue();
            if (fromCell == null || toCell == null) {
                errorLabel.setText("Please select both 'From' and 'To' cells.");
            } else if (sortAllColumnsCheckbox.isSelected()) {
                performSort(fromCell, toCell, getAllColumnsInRange(fromCell, toCell));
                sortPopupStage.close();
            } else {
                List<String> columnsToSortBy = getSelectedColumns(sortColumnsBox);
                if (columnsToSortBy.isEmpty()) {
                    errorLabel.setText("Please select at least one column to sort by.");
                } else {
                    performSort(fromCell, toCell, columnsToSortBy.toArray(new String[0]));
                    sortPopupStage.close();
                }
            }
        });

        // Add components to the layout
        layout.getChildren().addAll(fromLabel, fromComboBox, toLabel, toComboBox, sortColumnsBox, addSortColumnButton, sortAllColumnsCheckbox, okButton, errorLabel);

        Scene scene = new Scene(layout, 400, 400);
        sortPopupStage.setScene(scene);
        sortPopupStage.show();
    }

    private void populateColumnDropdown(ComboBox<String> columnDropdown, String fromCell, String toCell, List<String> alreadySelectedColumns) {
        if (fromCell != null && toCell != null) {
            Set<String> columns = extractColumnsInRange(fromCell, toCell);
            alreadySelectedColumns.forEach(columns::remove); // Remove already selected columns
            columnDropdown.getItems().clear(); // Clear previous items
            columnDropdown.getItems().addAll(columns); // Add valid columns
        }
    }

    // Helper to create a column dropdown based on the selected range
    private ComboBox<String> createColumnDropdown(ComboBox<String> fromComboBox, ComboBox<String> toComboBox) {
        ComboBox<String> columnComboBox = new ComboBox<>();
        if (fromComboBox.getValue() != null && toComboBox.getValue() != null) {
            Set<String> columns = extractColumnsInRange(fromComboBox.getValue(), toComboBox.getValue());
            columnComboBox.getItems().addAll(columns);
        }
        columnComboBox.setPromptText("Select column");
        return columnComboBox;
    }

    // Extract columns in the range (From..To)
    private Set<String> extractColumnsInRange(String fromCell, String toCell) {
        Set<String> columns = new TreeSet<>();
        int fromCol = extractColumn(fromCell);
        int toCol = extractColumn(toCell);

        for (int col = fromCol; col <= toCol; col++) {
            columns.add(String.valueOf((char) ('A' + (col - 1))));
        }
        return columns;
    }

    // Helper to get the columns that have already been selected
    private List<String> getSelectedColumns(VBox sortColumnsBox) {
        List<String> selectedColumns = new ArrayList<>();
        for (Node node : sortColumnsBox.getChildren()) {
            if (node instanceof ComboBox) {
                ComboBox<String> comboBox = (ComboBox<String>) node;
                String selectedColumn = comboBox.getValue();
                if (selectedColumn != null) {
                    selectedColumns.add(selectedColumn);
                }
            }
        }
        return selectedColumns;
    }

    // Get all columns in the range
    private String[] getAllColumnsInRange(String fromCell, String toCell) {
        Set<String> columns = extractColumnsInRange(fromCell, toCell);
        return columns.toArray(new String[0]);
    }

    // Sorting logic, ignoring non-numeric columns
    private void performSort(String fromCell, String toCell, String[] columnsToSortBy) {
        // Extract row and column indices from the cell range
        int fromRow = extractRow(fromCell);
        int toRow = extractRow(toCell);
        int fromColumn = extractColumn(fromCell);
        int toColumn = extractColumn(toCell);

        // Get the rows within the range
        List<Map<String, String>> rowsData = getRowsInRange(fromRow, toRow, fromColumn, toColumn);

        // Create a map to save each cell's ID and corresponding CellDTO (for color and design)
        Map<String, CellDTO> cellDesignMap = new HashMap<>();

        // Save the design of each cell based on its cell ID
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            for (int colIndex = fromColumn; colIndex <= toColumn; colIndex++) {
                String cellId = this.appController.getSpreadsheetController().getCellIdFromCoordinates(rowIndex, colIndex);

                // Fetch the CellDTO for this cell, which includes text and background colors
                CellDTO cellDTO = this.appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId);

                // Save the design (text/background color) in the map with cellId as the key
                if (cellDTO != null) {
                    cellDesignMap.put(cellId, cellDTO);
                }
            }
        }

        // Apply sorting logic using the selected columns
        Comparator<Map<String, String>> comparator = Comparator.comparing(row -> {
            String value = row.get(columnsToSortBy[0]);

            // Handle null or empty values
            if (value == null || value.isEmpty()) {
                return Double.MAX_VALUE; // Treat empty or null values as very large numbers so they appear last
            }

            return value.matches("-?\\d+(\\.\\d+)?") ? Double.parseDouble(value) : Double.MAX_VALUE;
        }, Comparator.naturalOrder());

        for (int i = 1; i < columnsToSortBy.length; i++) {
            String column = columnsToSortBy[i];
            comparator = comparator.thenComparing(row -> {
                String value = row.get(column);

                // Handle null or empty values
                if (value == null || value.isEmpty()) {
                    return Double.MAX_VALUE; // Treat empty or null values as very large numbers so they appear last
                }

                return value.matches("-?\\d+(\\.\\d+)?") ? Double.parseDouble(value) : Double.MAX_VALUE;
            }, Comparator.naturalOrder());
        }

        rowsData.sort(comparator);

        // Now that the sorting is done, show the full spreadsheet with the sorted section
        this.appController.getSpreadsheetController().showFullSpreadsheetWithSortedSection(rowsData, fromRow, toRow, fromColumn, toColumn, cellDesignMap);
    }

    // Update the full spreadsheet with the sorted section
    public void updateFullSpreadsheetWithSortedSection(List<Map<String, String>> fullSpreadsheetData, List<Map<String, String>> sortedSection, int fromRow, int toRow, int fromColumn, int toColumn) {
        int sortedRowIndex = 0;
        for (int rowIndex = fromRow - 1; rowIndex < toRow; rowIndex++) {
            Map<String, String> sortedRow = sortedSection.get(sortedRowIndex++);
            for (int colIndex = fromColumn - 1; colIndex < toColumn; colIndex++) {
                String columnLetter = String.valueOf((char) ('A' + colIndex));
                fullSpreadsheetData.get(rowIndex).put(columnLetter, sortedRow.get(columnLetter));
            }
        }
    }

    // Helper method to extract row number from cell reference (e.g., "B3" -> 3)
    private int extractRow(String cellId) {
        return Integer.parseInt(cellId.replaceAll("[A-Z]", ""));
    }

    // Helper method to extract column index from cell reference (e.g., "B3" -> 2 for column 'B')
    private int extractColumn(String cellId) {
        return cellId.charAt(0) - 'A' + 1;
    }

    // Helper method to retrieve rows in the specified range
    private List<Map<String, String>> getRowsInRange(int fromRow, int toRow, int fromColumn, int toColumn) {
        List<Map<String, String>> rowsData = new ArrayList<>();

        for (int row = fromRow; row <= toRow; row++) {
            Map<String, String> rowData = new HashMap<>();
            for (int col = fromColumn; col <= toColumn; col++) {
                String cellId = String.valueOf((char) ('A' + col - 1)) + row;
                String cellValue = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId).getEffectiveValue().toString();
                rowData.put(String.valueOf((char) ('A' + col - 1)), cellValue);
            }
            rowsData.add(rowData);
        }

        return rowsData;
    }

    // Helper method to show a dialog for selecting sorting columns
    private void populateValidToOptions(String fromCell, ComboBox<String> toComboBox) {
        char fromColumn = fromCell.charAt(0);  // e.g., 'A'
        int fromRow = Integer.parseInt(fromCell.substring(1)); // e.g., 3

        // Clear previous items
        toComboBox.getItems().clear();

        // Add valid "To" cells
        int numRows = appController.getSpreadsheetController().getSpreadsheet().getNumOfRows();
        int numCols = appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns();


        for (int row = fromRow; row <= numRows; row++) {
            for (int col = fromColumn - 'A'; col < numCols; col++) {
                String columnLetter = String.valueOf((char) ('A' + col));
                String cellReference = columnLetter + row;
                toComboBox.getItems().add(cellReference);
            }
        }
    }

    private void showFilterPopup() {
        Stage filterPopupStage = new Stage();
        filterPopupStage.setTitle("Filter Table");

        // Main layout (VBox) wrapped in a ScrollPane
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20px;");

        Label fromLabel = new Label("Select 'From' cell:");
        ComboBox<String> fromComboBox = new ComboBox<>();
        fromComboBox.setPromptText("Select the starting cell");
        this.appController.getSpreadsheetController().populateCellReferences(fromComboBox);

        Label toLabel = new Label("Select 'To' cell:");
        ComboBox<String> toComboBox = new ComboBox<>();
        toComboBox.setPromptText("Select the ending cell");

        // Populate valid "To" options based on "From" selection
        fromComboBox.setOnAction(event -> {
            String selectedFrom = fromComboBox.getValue();
            if (selectedFrom != null) {
                populateValidToOptions(selectedFrom, toComboBox);
            }
        });

        // Container for adding filter columns and their associated value selectors
        VBox filterColumnsBox = new VBox(10);
        Label filterByLabel = new Label("Filter by:");
        ComboBox<String> firstFilterColumn = createColumnDropdown(fromComboBox, toComboBox);

        // Populate first column dropdown after selecting the range
        toComboBox.setOnAction(event -> {
            String fromCell = fromComboBox.getValue();
            String toCell = toComboBox.getValue();
            if (fromCell != null && toCell != null) {
                populateColumnDropdown(firstFilterColumn, fromCell, toCell, new ArrayList<>());
            }
        });

        ListView<String> firstFilterValues = new ListView<>();
        firstFilterValues.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        firstFilterColumn.setOnAction(event -> {
            String selectedColumn = firstFilterColumn.getValue();
            if (selectedColumn != null) {
                populateUniqueValues(selectedColumn, firstFilterValues, fromComboBox.getValue(), toComboBox.getValue());
            }
        });

        filterColumnsBox.getChildren().addAll(filterByLabel, firstFilterColumn, new Label("Select values:"), firstFilterValues);

        Button addFilterColumnButton = new Button("Add another filter column");
        addFilterColumnButton.setOnAction(event -> {
            ComboBox<String> nextFilterColumn = createColumnDropdown(fromComboBox, toComboBox);
            ListView<String> nextFilterValues = new ListView<>();
            nextFilterValues.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            nextFilterColumn.setOnAction(columnEvent -> {
                String selectedColumn = nextFilterColumn.getValue();
                if (selectedColumn != null) {
                    populateUniqueValues(selectedColumn, nextFilterValues, fromComboBox.getValue(), toComboBox.getValue());
                }
            });

            filterColumnsBox.getChildren().addAll(new Label("Filter by:"), nextFilterColumn, new Label("Select values:"), nextFilterValues);
        });

        Button okButton = new Button("Apply Filter");
        okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        okButton.setOnAction(event -> {
            String fromCell = fromComboBox.getValue();
            String toCell = toComboBox.getValue();
            if (fromCell == null || toCell == null) {
                errorLabel.setText("Please select both 'From' and 'To' cells.");
            } else {
                List<FilterCriteria> filterCriteriaList = gatherFilterCriteria(filterColumnsBox);
                if (filterCriteriaList.isEmpty()) {
                    errorLabel.setText("Please select at least one column and its corresponding values to filter.");
                } else {
                    applyFilterAndShowPopup(fromCell, toCell, filterCriteriaList);
                    filterPopupStage.close();
                }
            }
        });

        // Add all components to the layout
        layout.getChildren().addAll(fromLabel, fromComboBox, toLabel, toComboBox, filterColumnsBox, addFilterColumnButton, okButton, errorLabel);

        // Create a ScrollPane to contain the layout
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        // Set up the scene
        Scene scene = new Scene(scrollPane, 400, 600);
        filterPopupStage.setScene(scene);
        filterPopupStage.show();
    }

    private List<FilterCriteria> gatherFilterCriteria(VBox filterColumnsBox) {
        List<FilterCriteria> filterCriteriaList = new ArrayList<>();

        for (int i = 0; i < filterColumnsBox.getChildren().size(); i += 4) {
            ComboBox<String> columnComboBox = (ComboBox<String>) filterColumnsBox.getChildren().get(i + 1);
            ListView<String> valuesListView = (ListView<String>) filterColumnsBox.getChildren().get(i + 3);
            String selectedColumn = columnComboBox.getValue();
            List<String> selectedValues = new ArrayList<>(valuesListView.getSelectionModel().getSelectedItems());

            // Exclude the value "EMPTY"
            selectedValues.remove("EMPTY");

            if (selectedColumn != null && !selectedValues.isEmpty()) {
                filterCriteriaList.add(new FilterCriteria(selectedColumn, selectedValues));
            }
        }

        return filterCriteriaList;
    }

    private void applyFilterAndShowPopup(String fromCell, String toCell, List<FilterCriteria> filterCriteriaList) {
        int fromRow = extractRow(fromCell);
        int toRow = extractRow(toCell);
        int fromColumn = extractColumn(fromCell);
        int toColumn = extractColumn(toCell);

        // Filter the rows based on the provided criteria
        List<Map<String, String>> filteredData = filterRowsByCriteria(fromRow, toRow, fromColumn, toColumn, filterCriteriaList);

        // Check if the result is empty before proceeding
        if (filteredData.isEmpty()) {
            this.appController.showError("No Results Found","No rows match the selected filter criteria.");  // Display message to user
        } else {
            // Show the filtered results in a popup
            showFilteredResults(filteredData);
        }
    }

    private List<Map<String, String>> filterRowsByCriteria(int fromRow, int toRow, int fromColumn, int toColumn, List<FilterCriteria> filterCriteriaList) {
        List<Map<String, String>> filteredData = new ArrayList<>();

        // Loop through the rows to apply the filter
        for (int row = fromRow; row <= toRow; row++) {
            if (doesRowMeetCriteria(row, filterCriteriaList)) {
                // Instead of only storing the values, store the full cell IDs
                Map<String, String> rowData = new HashMap<>();
                for (int col = fromColumn; col <= toColumn; col++) {
                    String columnLetter = String.valueOf((char) ('A' + col - 1));
                    String cellId = columnLetter + row;
                    String cellValue = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId).getEffectiveValue().toString();
                    rowData.put(cellId, cellValue);
                }
                filteredData.add(rowData);
            }
        }
        return filteredData;
    }

    private boolean doesRowMeetCriteria(int row, List<FilterCriteria> filterCriteriaList) {
        for (FilterCriteria criteria : filterCriteriaList) {
            String column = criteria.getColumn();
            String cellId = column + row;
            String cellValue = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId).getEffectiveValue().toString();

            // If the value doesn't match any of the filter values, return false
            if (!criteria.getValues().contains(cellValue)) {
                return false;
            }
        }
        return true;
    }

    private void showFilteredResults(List<Map<String, String>> filteredData) {
        Stage filteredPopupStage = new Stage();
        filteredPopupStage.setTitle("Filtered Results");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f9f9f9;");

        GridPane filteredGrid = new GridPane();
        filteredGrid.setGridLinesVisible(true);
        filteredGrid.setHgap(2);
        filteredGrid.setVgap(2);


        // Get the total number of rows and columns from the original spreadsheet
        int totalRows = appController.getSpreadsheetController().getSpreadsheet().getNumOfRows();
        int totalColumns = appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns();

        double cellWidth = 80;
        double cellHeight = 30;

        setConstraints(totalColumns, cellWidth, filteredGrid, totalRows, cellHeight);

        // Populate the entire grid with default empty cells and apply colors
        for (int row = 1; row <= totalRows; row++) {
            for (int col = 1; col <= totalColumns; col++) {
                String cellId = this.appController.getSpreadsheetController().getCellIdFromCoordinates(row, col);

                // Retrieve the cell color information
                CellDTO cellDTO = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId);
                String textColor = cellDTO != null ? cellDTO.getTextColor() : "black";
                String backgroundColor = cellDTO != null ? cellDTO.getBackgroundColor() : "white";

                // Create label for each cell
                Label cellLabel = new Label("");
                cellLabel.setAlignment(Pos.CENTER);
                cellLabel.setMaxWidth(Double.MAX_VALUE);
                cellLabel.setMaxHeight(Double.MAX_VALUE);
                cellLabel.setStyle("-fx-border-color: #a9a9a9; -fx-padding: 5px; -fx-border-width: 1;" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-color: " + backgroundColor + ";");
                filteredGrid.add(cellLabel, col, row);
            }
        }

        // Populate the grid with the filtered data (preserving original cell positions)
        for (Map<String, String> rowData : filteredData) {
            for (Map.Entry<String, String> cellEntry : rowData.entrySet()) {
                String cellId = cellEntry.getKey();
                String cellValue = cellEntry.getValue();

                // Extract row and column from cell ID
                int rowIndex = extractRow(cellId);
                int columnIndex = extractColumn(cellId);

                // Find the corresponding label in the grid and update its value
                for (Node node : filteredGrid.getChildren()) {
                    if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null) {
                        int col = GridPane.getColumnIndex(node);
                        int row = GridPane.getRowIndex(node);

                        // Compare cell identifiers and update the relevant cells with the filtered values
                        if (row == rowIndex && col == columnIndex) {
                            Label label = (Label) node;
                            label.setText(cellValue.equals("EMPTY") ? "" : cellValue);
                            break;
                        }
                    }
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(filteredGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        layout.getChildren().add(scrollPane);

        Scene scene = new Scene(layout, 800, 600);
        filteredPopupStage.setScene(scene);
        filteredPopupStage.show();
    }

    private static void setConstraints(int totalColumns, double cellWidth, GridPane filteredGrid, int totalRows, double cellHeight) {
        // Set column constraints for consistent width
        for (int col = 0; col <= totalColumns; col++) {
            ColumnConstraints columnConstraints = new ColumnConstraints(cellWidth);
            columnConstraints.setHgrow(Priority.ALWAYS);
            filteredGrid.getColumnConstraints().add(columnConstraints);
        }

        // Set row constraints for consistent height
        for (int row = 0; row <= totalRows; row++) {
            RowConstraints rowConstraints = new RowConstraints(cellHeight);
            rowConstraints.setVgrow(Priority.ALWAYS);
            filteredGrid.getRowConstraints().add(rowConstraints);
        }

        // Add column headers (A, B, C, ...)
        for (int col = 1; col <= totalColumns; col++) {
            Label columnHeader = new Label(String.valueOf((char) ('A' + (col - 1))));
            columnHeader.setAlignment(Pos.CENTER);
            columnHeader.setMaxWidth(Double.MAX_VALUE);
            columnHeader.setMaxHeight(Double.MAX_VALUE);
            columnHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5px; -fx-border-color: #a9a9a9;");
            filteredGrid.add(columnHeader, col, 0);  // Add to the top row
        }

        // Add row headers (1, 2, 3, ...)
        for (int row = 1; row <= totalRows; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setAlignment(Pos.CENTER);
            rowHeader.setMaxWidth(Double.MAX_VALUE);
            rowHeader.setMaxHeight(Double.MAX_VALUE);
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5px; -fx-border-color: #a9a9a9;");
            filteredGrid.add(rowHeader, 0, row);  // Add to the leftmost column
        }
    }

    private void populateUniqueValues(String selectedColumn, ListView<String> valuesListView, String fromCell, String toCell) {
        valuesListView.getItems().clear();
        Set<String> uniqueValues = extractUniqueValuesInColumn(selectedColumn, fromCell, toCell);
        valuesListView.getItems().addAll(uniqueValues);
    }

    private Set<String> extractUniqueValuesInColumn(String selectedColumn, String fromCell, String toCell) {
        int fromRow = extractRow(fromCell);
        int toRow = extractRow(toCell);
        Set<String> uniqueValues = new HashSet<>();

        for (int row = fromRow; row <= toRow; row++) {
            String cellId = selectedColumn + row;
            String cellValue = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId).getEffectiveValue().toString();
            if (!cellValue.equals("EMPTY")) {
                uniqueValues.add(cellValue);
            }
        }

        return uniqueValues;
    }

    public void showAddRangePopup() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Add Range");

        VBox popupLayout = new VBox(10);
        popupLayout.setPadding(new Insets(20));

        Label rangeNameLabel = new Label("Range Name:");
        TextField rangeNameInput = new TextField();
        Label rangeErrorLabel = new Label("Range name already exists");
        rangeErrorLabel.setStyle("-fx-text-fill: red;");
        rangeErrorLabel.setVisible(false);

        Label fromLabel = new Label("Select 'From' cell:");
        ComboBox<String> fromComboBox = new ComboBox<>();
        fromComboBox.setPromptText("Select the starting cell");
        this.appController.getSpreadsheetController().populateCellReferences(fromComboBox);

        Label toLabel = new Label("Select 'To' cell:");
        ComboBox<String> toComboBox = new ComboBox<>();
        toComboBox.setPromptText("Select the ending cell");

        fromComboBox.setOnAction(event -> {
            String selectedFrom = fromComboBox.getValue();
            if (selectedFrom != null) {
                populateValidToOptions(selectedFrom, toComboBox);
            }
        });

        Button addButton = new Button("Add");
        addButton.setDisable(true);

        toComboBox.setOnAction(event -> validateRangeSelections(fromComboBox, toComboBox, addButton));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> popupStage.close());

        addButton.setOnAction(event -> {
            String rangeName = rangeNameInput.getText();
            String fromCellId = fromComboBox.getValue();
            String toCellId = toComboBox.getValue();

            // Send request to server to add range
            boolean isAdded = appController.getSpreadsheetController().getSpreadsheet().addRange(rangeName, fromCellId, toCellId);

            if (!isAdded) {
                rangeErrorLabel.setVisible(true);
            } else {
                rangeErrorLabel.setVisible(false);
                popupStage.close(); // Successfully added range, close the popup
            }
        });

        popupLayout.getChildren().addAll(rangeNameLabel, rangeNameInput, rangeErrorLabel, fromLabel, fromComboBox, toLabel, toComboBox, new HBox(10, cancelButton, addButton));
        Scene popupScene = new Scene(popupLayout, 300, 500);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    private void validateRangeSelections(ComboBox<String> fromColumn, ComboBox<String> toColumn, Button addButton) {
        if (fromColumn.getValue() != null && toColumn.getValue() != null) {
            addButton.setDisable(false);
        }
    }

    public void showColumnRowWidthChangePopup() {
        // Create a new Stage for the popup
        Stage popupStage = new Stage();
        popupStage.setTitle("Change Width/Height for Specific Row or Column");

        // Main layout - VBox with padding and spacing
        VBox popupLayout = new VBox(20);
        popupLayout.setPadding(new Insets(20));
        popupLayout.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 5;");

        // Instruction Label
        Label instructionLabel = new Label("Choose Row or Column to Change:");
        instructionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        // Dropdown to choose between Row or Column
        ComboBox<String> dimensionComboBox = new ComboBox<>();
        dimensionComboBox.getItems().addAll("Column", "Row");
        dimensionComboBox.setPromptText("Select Dimension");
        dimensionComboBox.setStyle("-fx-padding: 10px; -fx-border-radius: 5px;");

        // Dropdown for selecting the specific row/column
        ComboBox<String> selectionComboBox = new ComboBox<>();
        selectionComboBox.setPromptText("Select Column/Row");
        selectionComboBox.setStyle("-fx-padding: 10px; -fx-border-radius: 5px;");

        // Label to display current width or height
        Label currentSizeLabel = new Label();
        currentSizeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        currentSizeLabel.setVisible(false); // Initially hidden

        // TextField for entering the new width or height
        TextField sizeInput = new TextField();
        sizeInput.setPromptText("Enter new size");
        sizeInput.setDisable(true); // Initially disabled
        sizeInput.setStyle("-fx-opacity: 1; -fx-padding: 10px;");

        // Label to show error message
        Label errorMessageLabel = new Label();
        errorMessageLabel.setStyle("-fx-text-fill: red;"); // Style for error message
        errorMessageLabel.setVisible(false); // Hidden initially

        // ComboBox for selecting units
        ComboBox<String> unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("Pixels", "Inches", "Centimeters");
        unitComboBox.setValue("Pixels");
        unitComboBox.setStyle("-fx-padding: 10px; -fx-border-radius: 5px;");

        // OK and Cancel buttons
        Button okButton = new Button("Apply");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        okButton.setDisable(true); // Initially disabled

        // Event handler for Cancel button
        cancelButton.setOnAction(event -> popupStage.close());

        // Populate the selection dropdown when a dimension is chosen
        dimensionComboBox.setOnAction(event -> {
            String selectedDimension = dimensionComboBox.getValue();
            selectionComboBox.getItems().clear();
            sizeInput.setDisable(true); // Disable the TextField until both dimension and selection are made
            okButton.setDisable(true); // Disable the Apply button until a valid size is entered
            errorMessageLabel.setVisible(false); // Hide the error message when a new selection is made
            currentSizeLabel.setVisible(false); // Hide current size label initially

            if (selectedDimension.equals("Column")) {
                sizeInput.setPromptText("Enter new width"); // Set the prompt text for column width
                for (int i = 1; i <= appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns(); i++) {
                    selectionComboBox.getItems().add("Column " + ((char) ('A' + (i - 1))));
                }
            } else if (selectedDimension.equals("Row")) {
                sizeInput.setPromptText("Enter new height"); // Set the prompt text for row height
                for (int i = 1; i <= appController.getSpreadsheetController().getSpreadsheet().getNumOfRows(); i++) {
                    selectionComboBox.getItems().add("Row " + i);
                }
            }
        });

        // Enable the size input and display current size when a row/column is selected
        selectionComboBox.setOnAction(event -> {
            if (dimensionComboBox.getValue() != null && selectionComboBox.getValue() != null) {
                sizeInput.setDisable(false); // Enable the size input when both selections are made
                errorMessageLabel.setVisible(false); // Hide error message when user selects new options

                // Get the selected dimension (Row or Column)
                String selectedDimension = dimensionComboBox.getValue();
                String selectedItem = selectionComboBox.getValue();

                // Display current height or width based on the selected row or column
                if (selectedDimension.equals("Column")) {
                    if (!selectedItem.equals("All columns")) {
                        String columnIndex = selectedItem.replace("Column ", "");
                        int colIndex = columnIndex.charAt(0) - 'A' + 1; // Convert letter to 0-based index

                        // Ensure that the column constraints exist and then retrieve the width
                        if (colIndex >= 0 && colIndex < appController.getSpreadsheetController().getGridPane().getColumnConstraints().size()) {
                            double currentWidth = appController.getSpreadsheetController().getGridPane().getColumnConstraints().get(colIndex).getPrefWidth();

                            // Use Platform.runLater to ensure UI updates on JavaFX thread
                            Platform.runLater(() -> {
                                currentSizeLabel.setText("Current width of " + selectedItem + ": " + currentWidth + " px");
                                currentSizeLabel.setVisible(true); // Show the label with the current size
                                popupLayout.layout(); // Force a layout update on the parent container
                            });

                        } else {
                            Platform.runLater(() -> {
                                currentSizeLabel.setText("Column constraints not set.");
                                currentSizeLabel.setVisible(true);
                                popupLayout.layout(); // Force a layout update
                            });
                        }

                    }
                } else if (selectedDimension.equals("Row")) {
                    if (!selectedItem.equals("All rows")) {
                        int rowIndex = Integer.parseInt(selectedItem.replace("Row ", "")) ; // 0-based index

                        // Ensure that the row constraints exist and then retrieve the height
                        if (rowIndex >= 0 && rowIndex < appController.getSpreadsheetController().getGridPane().getRowConstraints().size()) {
                            double currentHeight = appController.getSpreadsheetController().getGridPane().getRowConstraints().get(rowIndex).getPrefHeight();


                            // Use Platform.runLater to ensure UI updates on JavaFX thread
                            Platform.runLater(() -> {
                                currentSizeLabel.setText("Current height of " + selectedItem + ": " + currentHeight + " px");
                                currentSizeLabel.setVisible(true); // Show the label with the current size
                                popupLayout.layout(); // Force a layout update
                            });

                        } else {
                            Platform.runLater(() -> {
                                currentSizeLabel.setText("Row constraints not set.");
                                currentSizeLabel.setVisible(true);
                                popupLayout.layout(); // Force a layout update
                            });
                        }
                    }
                }
            }
        });

        // Validate size input and enable/disable the Apply button
        sizeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            // Check if the input is a valid number
            try {
                double value = Double.parseDouble(newValue);
                if (value > 0) {
                    errorMessageLabel.setVisible(false); // Hide error message if valid number
                    okButton.setDisable(false); // Enable Apply button
                } else {
                    errorMessageLabel.setText("Size must be a positive number.");
                    errorMessageLabel.setVisible(true); // Show error message if value is negative
                    okButton.setDisable(true); // Disable Apply button
                }
            } catch (NumberFormatException e) {
                errorMessageLabel.setText("Please enter a valid number."); // Show error message for non-number input
                errorMessageLabel.setVisible(true);
                okButton.setDisable(true); // Disable Apply button if input is not a number
            }
        });

        // Event handler for OK button
        okButton.setOnAction(event -> {
            String selectedDimension = dimensionComboBox.getValue();
            String selectedItem = selectionComboBox.getValue();
            String unit = unitComboBox.getValue();
            double enteredValue = Double.parseDouble(sizeInput.getText());
            double valueInPixels = convertToPixels(enteredValue, unit);

            if (selectedDimension.equals("Column")) {

                String columnIndex = selectedItem.replace("Column ", "");
                appController.getSpreadsheetController().applyWidthToColumn(columnIndex, valueInPixels);

            } else if (selectedDimension.equals("Row")) {
                int rowIndex = Integer.parseInt(selectedItem.replace("Row ", ""));
                appController.getSpreadsheetController().applyHeightToRow(rowIndex, valueInPixels);

            }

            popupStage.close();
        });

        // Add components to the layout
        popupLayout.getChildren().addAll(instructionLabel, dimensionComboBox, selectionComboBox, currentSizeLabel, sizeInput, errorMessageLabel, unitComboBox, okButton, cancelButton);

        // Set the layout on the scene and show the popup
        Scene popupScene = new Scene(popupLayout, 400, 500);
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    private double convertToPixels(double enteredValue, String unit) {
        double pixelsPerInch = 96;
        double pixelsPerCentimeter = pixelsPerInch / 2.54;
        return switch (unit) {
            case "Inches" -> enteredValue * pixelsPerInch;
            case "Centimeters" -> enteredValue * pixelsPerCentimeter;
            default -> enteredValue; // Assume it's pixels
        };
    }

    public void setMainController(appController appController) {
        this.appController = appController;
    }
}
