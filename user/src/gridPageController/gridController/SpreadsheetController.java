package gridPageController.gridController;
import com.google.gson.Gson;
import dto.CellDTO;
import dto.CellUpdateDTO;
import dto.SpreadsheetManagerDTO;
import gridPageController.headController.HeadController;
import gridPageController.mainController.appController;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static httputils.Constants.CELL_UPDATE;

public class SpreadsheetController implements Serializable {

    private static final double Header_Row_And_Column = 2;
    private static final double Main_Header_Height = 88.0;
    private static final double CommandAndRanges_Section = 200.0;

    @FXML
    private GridPane spreadsheetGrid;
    private gridPageController.mainController.appController appController;
    private SpreadsheetManagerDTO spreadsheetManagerDTO;
    private HeadController headController;
    private static final Map<String, Integer> functionArgCounts = new HashMap<>();
    private ScheduledExecutorService scheduler;
    private boolean isPolling = false;


    static {
        functionArgCounts.put("PLUS", 2);
        functionArgCounts.put("MINUS", 2);
        functionArgCounts.put("TIMES", 2);
        functionArgCounts.put("DIVIDE", 2);
        functionArgCounts.put("MOD", 2);
        functionArgCounts.put("POW", 2);
        functionArgCounts.put("ABS", 1);
        functionArgCounts.put("REF", 1);
        functionArgCounts.put("SUM", 1);  // SUM takes a range
        functionArgCounts.put("AVERAGE", 1);  // AVERAGE takes a range
        functionArgCounts.put("PERCENT", 2);
        functionArgCounts.put("AND", 2);
        functionArgCounts.put("BIGGER", 2);
        functionArgCounts.put("CONCAT", 2);
        functionArgCounts.put("EQUAL", 2);
        functionArgCounts.put("IF", 3);
        functionArgCounts.put("LESS", 2);
        functionArgCounts.put("NOT", 1);
        functionArgCounts.put("OR", 2);
        functionArgCounts.put("SUB", 3);
        // Add more as needed
    }

    public void loadSpreadsheetFromDTO(SpreadsheetManagerDTO spreadsheetDTO, String userPermission) {
        Platform.runLater(() -> {
            try {
                this.spreadsheetManagerDTO = spreadsheetDTO;
                // Clear existing grid and constraints
                spreadsheetGrid.getChildren().clear();
                spreadsheetGrid.getColumnConstraints().clear();
                spreadsheetGrid.getRowConstraints().clear();
                BorderPane.clearConstraints(this.appController.getMainContainer());

                // Set the size of the grid using data from the DTO
                int numberOfRows = spreadsheetDTO.getSpreadsheetDTO().getRows();
                int numberOfColumns = spreadsheetDTO.getSpreadsheetDTO().getColumns();
                double rowHeight = spreadsheetDTO.getSpreadsheetDTO().getRowHeight();
                double columnWidth = spreadsheetDTO.getSpreadsheetDTO().getColumnWidth();

                // Set grid dimensions
                spreadsheetGrid.setMinSize(columnWidth * (numberOfColumns + 1), rowHeight * (numberOfRows + 1));
                spreadsheetGrid.setPrefSize(columnWidth * (numberOfColumns + 1), rowHeight * (numberOfRows + 1));
                spreadsheetGrid.setMaxSize(columnWidth * (numberOfColumns + 1), rowHeight * (numberOfRows + 1));

                // Set padding and alignment
                spreadsheetGrid.setPadding(new Insets(10, 0, 0, 10));  // Adjust the left padding if necessary
                spreadsheetGrid.setAlignment(Pos.TOP_CENTER);

                // Set container size based on the spreadsheet dimensions
                this.appController.getMainContainer().setMinHeight(136 + 335);
                this.appController.getMainContainer().setMinWidth(200 + 600);
                BorderPane.setAlignment(this.appController.getMainContainer(), Pos.CENTER_LEFT);

                this.appController.getMainContainer().setPrefWidth(columnWidth * (numberOfColumns + 1));
                this.appController.getMainContainer().setPrefHeight(rowHeight * (numberOfRows + 1));

                // Configure the scroll pane to manage overflow
                this.appController.getScrollPane().setFitToWidth(false);

                // Add column and row constraints dynamically
                ColumnsAndRowsConstraints(numberOfColumns, columnWidth, numberOfRows, rowHeight);

                // Get the current skin's cell class (for styling)
                String cellClass = this.appController.getCellClassForCurrentSkin();


                // Populate the grid with data cells from the DTO
                for (int row = 1; row <= numberOfRows; row++) {
                    for (int col = 1; col <= numberOfColumns; col++) {
                        String rowLetter = String.valueOf((char) ('A' + (col - 1)));
                        String columnNumber = String.valueOf(row);
                        String cellIdentifier = rowLetter + columnNumber;

                        Label cell;
                        try {
                            // Retrieve the value from the DTO
                            String val = spreadsheetDTO.getCellDTO(cellIdentifier).getEffectiveValue().toString();
                            String formattedValue = formatCellValue(val);  // Optional: Format the cell value

                            cell = new Label(formattedValue);
                            cell.setAlignment(Pos.CENTER);
                            cell.setMaxWidth(Double.MAX_VALUE);
                            cell.setMaxHeight(Double.MAX_VALUE);

                            // Style the cell
                            cell.getStyleClass().clear();
                            cell.getStyleClass().add(cellClass);

                            // Set entrance animation (fade)
                            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), cell);
                            fadeIn.setFromValue(0);
                            fadeIn.setToValue(1);
                            fadeIn.play();

                            // Handle cell click (if needed)
                            cell.setOnMouseClicked(event -> handleCellClick(cellIdentifier, userPermission));

                            // Add the cell to the grid
                            spreadsheetGrid.add(cell, col, row);

                        } catch (Exception ignored) {

                        }
                    }
                }

                // Adjust the main container size based on the number of columns/rows
                SetMainContainerSize(columnWidth, numberOfColumns, rowHeight, numberOfRows);
                startPollingForNewVersions();
            } catch (Exception e) {
                appController.showError("Unexpected Error", e.getMessage());
            }
        });
    }

    private void SetMainContainerSize(double columnWidth, int numberOfColumns, double rowHeight, int numberOfRows) {
        appController.getMainContainer().setPrefHeight(Double.max(Main_Header_Height + columnWidth * (numberOfColumns + Header_Row_And_Column), appController.getMainContainer().getPrefHeight()));
        appController.getMainContainer().setPrefWidth(Double.max(CommandAndRanges_Section + rowHeight * (numberOfRows + Header_Row_And_Column), appController.getMainContainer().getPrefHeight()));
    }

    private void ColumnsAndRowsConstraints(int numberOfColumns, double columnWidth, int numberOfRows, double rowHeight) {
        // Add column constraints dynamically
        for (int j = 0; j <= numberOfColumns; j++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPrefWidth(columnWidth);
            columnConstraints.setMinWidth(columnWidth);
            columnConstraints.setMaxWidth(columnWidth);
            columnConstraints.setHgrow(Priority.ALWAYS);
            spreadsheetGrid.getColumnConstraints().add(columnConstraints);

            if (j == 0) {
                // First column for row numbers
                Label rowLabel = new Label(" ");
                rowLabel.setAlignment(Pos.CENTER);
                rowLabel.setMaxWidth(columnWidth);
                rowLabel.setMinWidth(columnWidth);
                rowLabel.setPrefWidth(columnWidth);
                rowLabel.setMaxHeight(Double.MAX_VALUE);
                spreadsheetGrid.add(rowLabel, j, 0);
            } else {
                String columnLetter = String.valueOf((char) ('A' + (j - 1)));
                Label columnLabel = new Label(columnLetter);
                columnLabel.setAlignment(Pos.CENTER);
                columnLabel.setMaxWidth(columnWidth);
                columnLabel.setMinWidth(columnWidth);
                columnLabel.setPrefWidth(columnWidth);
                columnLabel.setMaxHeight(Double.MAX_VALUE);
                columnLabel.setStyle("-fx-background-color: grey; -fx-border-color: black; -fx-padding: 5px;");
                spreadsheetGrid.add(columnLabel, j, 0);
            }
        }

        // Add row constraints dynamically
        for (int i = 0; i <= numberOfRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(rowHeight);
            rowConstraints.setMinHeight(rowHeight);
            rowConstraints.setMaxHeight(rowHeight);
            rowConstraints.setVgrow(Priority.ALWAYS);
            spreadsheetGrid.getRowConstraints().add(rowConstraints);

            if (i == 0) {
                // First row for column headers
                Label colHeaderLabel = new Label(" ");
                colHeaderLabel.setAlignment(Pos.CENTER);
                colHeaderLabel.setMaxHeight(rowHeight);
                colHeaderLabel.setMinHeight(rowHeight);
                colHeaderLabel.setPrefHeight(rowHeight);
                colHeaderLabel.setMaxWidth(Double.MAX_VALUE);
                spreadsheetGrid.add(colHeaderLabel, 0, i);
            } else {
                Label rowNumberLabel = new Label(String.valueOf(i));
                rowNumberLabel.setAlignment(Pos.CENTER);
                rowNumberLabel.setMaxWidth(Double.MAX_VALUE);
                // rowNumberLabel.setMaxHeight(Double.MAX_VALUE);
                rowNumberLabel.setMaxHeight(rowHeight);
                rowNumberLabel.setMinHeight(rowHeight);
                rowNumberLabel.setPrefHeight(rowHeight);
                rowNumberLabel.setStyle("-fx-background-color: grey; -fx-border-color: black; -fx-padding: 5px;");
                spreadsheetGrid.add(rowNumberLabel, 0, i);
            }
        }
    }

    private String convertToValidHex(String color) {
        if (color.equalsIgnoreCase("black") || color.equalsIgnoreCase("#000000")) {
            return "black";
        } else if (color.equalsIgnoreCase("white") || color.equalsIgnoreCase("#FFFFFF")) {
            return "white";
        } else {
            // Ensure the color string starts with a #
            if (!color.startsWith("#")) {
                return "#" + color;
            }
            return color;
        }
    }

    private void handleCellClick(String cellIdentifier, String userPermission) {

        // First, clear all existing highlights except for the first column and row
        for (Node node : spreadsheetGrid.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null) {
                int rowIndex = GridPane.getRowIndex(node);
                int columnIndex = GridPane.getColumnIndex(node);

                // Skip the first row and column
                if (rowIndex > 0 && columnIndex > 0) {
                    String currentCellId = getCellIdFromCoordinates(rowIndex, columnIndex);

                    // Get the current background and text colors of the cell from the data model
                    String backgroundColor = spreadsheetManagerDTO.getCellBackgroundColor(currentCellId);
                    String textColor = spreadsheetManagerDTO.getCellTextColor(currentCellId);
                    String cellClass = this.appController.getCellClassForCurrentSkin();


                    if (isDefaultColors(backgroundColor, textColor)) {
                        applyCellStyle(node, cellClass); // Apply skin style
                    } else {
                        applyCellCustomStyle(node, backgroundColor, textColor); // Retain custom style
                    }
                }
            }
        }

        // Highlight dependencies in light blue
        List<String> dependencies = spreadsheetManagerDTO.getCellDTO(cellIdentifier).getDependencies();
        for (String dependentCellId : dependencies) {
            highlightCell(dependentCellId, "lightblue");
        }

        // Highlight dependents in light green
        List<String> dependents = spreadsheetManagerDTO.getCellDTO(cellIdentifier).getDependents();
        for (String dependentCellId : dependents) {
            highlightCell(dependentCellId, "lightgreen");
        }

        // Update the HeadController if it's set
        if (headController != null) {
            TextField selectedCellIdField = headController.getSelectedCellIdField();
            TextField originalCellValueField = headController.getOriginalCellValueField();
            Label lastUpdateCellVersionField = headController.getLastUpdateCellVersionField();
            Label modifiedBy = headController.getModifiedBy();

            if (selectedCellIdField != null && originalCellValueField != null) {
                selectedCellIdField.setText(cellIdentifier);
                String sourceValue = spreadsheetManagerDTO.getCellDTO(cellIdentifier).getSourceValue().toString();
                int lastModifiedVersion = spreadsheetManagerDTO.getCellDTO(cellIdentifier).getLastModifiedVersion();
                originalCellValueField.setText(sourceValue.equals("EMPTY") ? "" : sourceValue);
                String tmp = spreadsheetManagerDTO.getCellDTO(cellIdentifier).getLastModifiedBy() == null ? "" : ("By user: " + spreadsheetManagerDTO.getCellDTO(cellIdentifier).getLastModifiedBy());
                lastUpdateCellVersionField.setText("Last update cell version: " + lastModifiedVersion);
                modifiedBy.setText(tmp);
                // Check the user's permission before showing the popup
                if (!userPermission.equals("READER")) {
                    originalCellValueField.setOnMouseClicked(event -> {
                        showFunctionInputPopup(originalCellValueField);
                    });
                } else {
                    // Consume the event so nothing happens
                    originalCellValueField.setOnMouseClicked(Event::consume);
                }
            }
        }
    }

    public SpreadsheetManagerDTO getSpreadsheetManagerDTO() {
        return spreadsheetManagerDTO;
    }

    private boolean isDefaultColors(String backgroundColor, String textColor) {
        return (backgroundColor.equalsIgnoreCase("white") || backgroundColor.equalsIgnoreCase("#FFFFFF")) &&
                (textColor.equalsIgnoreCase("black") || textColor.equalsIgnoreCase("#000000"));
    }

    private void applyCellStyle(Node node, String cellClass) {
        node.getStyleClass().clear();
        node.getStyleClass().add(cellClass);

        // Re-apply borders and text color based on the current skin
        node.setStyle("-fx-text-fill: " + (cellClass.equals("dark-cell") ? "#ffffff" : "#000000") +
                "; -fx-border-color: " + (cellClass.equals("dark-cell") ? "#ffffff" : "#000000") + ";");
    }

    private void applyCellCustomStyle(Node node, String backgroundColor, String textColor) {
        node.getStyleClass().clear();
        node.getStyleClass().add(appController.getCellClassForCurrentSkin());

        // Re-apply borders and text color based on the model (with correct CSS values)
        node.setStyle("-fx-text-fill: " + convertToValidHex(textColor) +
                "; -fx-border-color: #000000; -fx-background-color: " + convertToValidHex(backgroundColor) + ";");
    }

    private void highlightCell(String cellIdentifier, String highlightClass) {
        for (Node node : spreadsheetGrid.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null) {
                int rowIndex = GridPane.getRowIndex(node);
                int columnIndex = GridPane.getColumnIndex(node);
                String currentCellId = getCellIdFromCoordinates(rowIndex, columnIndex);

                // Skip the first row and first column
                if (currentCellId.equals(cellIdentifier) && rowIndex > 0 && columnIndex > 0) {
                    node.getStyleClass().clear();  // Clear previous styles
                    node.getStyleClass().add(highlightClass);  // Add highlight class (lightblue or lightgreen)

                    // Set the style with borders for the highlighted cells
                    (node).setStyle("-fx-background-color: " + highlightClass + "; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 1; -fx-padding: 5px;");
                    break;
                }
            }
        }
    }

    private void showFunctionInputPopup(TextField originalCellValueField) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Insert Function or Enter Manually");

        VBox popupLayout = new VBox(10);  // Set spacing between elements to 10
        popupLayout.setPadding(new Insets(20));

        // RadioButtons to let the user choose between manual value entry and function entry
        RadioButton enterManuallyButton = new RadioButton("Enter value manually");
        RadioButton enterFunctionButton = new RadioButton("Use function");

        // Group the RadioButtons
        ToggleGroup toggleGroup = new ToggleGroup();
        enterManuallyButton.setToggleGroup(toggleGroup);
        enterFunctionButton.setToggleGroup(toggleGroup);

        // Set the "Enter value manually" as selected by default
        enterManuallyButton.setSelected(true);  // Automatically select "Enter value manually"

        // Manual value entry TextField
        TextField manualValueField = new TextField(originalCellValueField.getText());  // Default value from originalCellValueField
        manualValueField.setPromptText("Enter value manually");

        // ComboBox for selecting the function
        ComboBox<String> functionComboBox = new ComboBox<>();
        functionComboBox.getItems().addAll("ABS", "AND", "AVERAGE", "BIGGER", "CONCAT", "DIVIDE",
                "EQUAL", "IF", "LESS", "MINUS", "MOD", "NOT", "OR",
                "PERCENT", "PLUS", "POW", "REF", "SUB", "SUM", "TIMES");
        // 20 functions included
        functionComboBox.setPromptText("Select a function");
        functionComboBox.setDisable(true);  // Initially disabled

        // Label for live preview of the function
        Label previewLabel = new Label("Live Calculation: ");
        previewLabel.setStyle("-fx-text-fill: green;");

        // Set up OK button
        Button okButton = new Button("OK");

        // Define and add the listener for manual input
        ChangeListener<String> manualValueListener = (observable, oldValue, newValue) -> {
            updatePreviewLabel(previewLabel, newValue, null, false);  // Update preview for manual input (not a function)
        };
        manualValueField.textProperty().addListener(manualValueListener);

        // Handle manual entry
        enterManuallyButton.setOnAction(event -> {
            manualValueField.setDisable(false);
            functionComboBox.setDisable(true);  // Disable the function ComboBox

            // Add the listener for manual input
            manualValueField.textProperty().addListener(manualValueListener);
        });

        // Handle function input
        enterFunctionButton.setOnAction(event -> {
            manualValueField.setDisable(true);
            functionComboBox.setDisable(false);  // Enable the function ComboBox

            // Remove the listener for manual input
            manualValueField.textProperty().removeListener(manualValueListener);

            handleFunctionSelection(functionComboBox, popupLayout, originalCellValueField, previewLabel, popupStage);
        });

        // OK button logic for manual entry
        okButton.setOnAction(event -> {
            if (enterManuallyButton.isSelected()) {
                originalCellValueField.setText(manualValueField.getText());  // Update originalCellValueField with manual value
                popupStage.close();
            }
        });

        // Add components to the layout
        popupLayout.getChildren().addAll(enterManuallyButton, enterFunctionButton, manualValueField, functionComboBox, previewLabel, okButton);

        // Set the scene and display the popup window
        Scene popupScene = new Scene(popupLayout, 400, 300);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    // Updated method to handle manual value vs function input
    private void updatePreviewLabel(Label previewLabel, String inputValue, Node[] argumentFields, boolean isFunction) {
        StringBuilder functionString = new StringBuilder();

        // If it's a function, wrap the function name in {}
        if (isFunction) {
            functionString.append("{").append(inputValue);
            if (argumentFields != null) {
                for (Node argumentField : argumentFields) {
                    if (argumentField instanceof TextField && !((TextField) argumentField).getText().isEmpty()) {
                        functionString.append(",").append(((TextField) argumentField).getText());
                    } else if (argumentField instanceof ComboBox && ((ComboBox<?>) argumentField).getValue() != null) {
                        functionString.append(",").append(((ComboBox<?>) argumentField).getValue().toString());
                    }
                }
            }
            functionString.append("}");
        } else {
            // If it's manual entry, just append the value directly
            functionString.append(inputValue);
        }

        // Pass the functionString and previewLabel to the calculateLivePreview method
        calculateLivePreview(functionString.toString(), previewLabel);
    }

    private void calculateLivePreview(String functionString, Label previewLabel) {
        try {
            if (isFunction(functionString)) {
                // Send request to the server to calculate live preview with sheetName as a query parameter
                OkHttpClient client = new OkHttpClient();

                // Send both sheetName and functionString to the server
                String url = String.format("http://localhost:8080/server_Web/calculateLivePreview?sheetName=%s", URLEncoder.encode(this.spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName(), "UTF-8"));

                RequestBody body = RequestBody.create(functionString, MediaType.parse("text/plain"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(() -> showError("Error calculating live preview", e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        assert response.body() != null;
                        String result = response.body().string();
                        Platform.runLater(() -> {
                            // Update the previewLabel here in the callback

                            previewLabel.setText("Formula: " + functionString + " = " + result);
                        });
                    }
                });
            } else {
                previewLabel.setText("Formula: " + functionString + " = " + formatCellValue(functionString));
            }
        } catch (Exception e) {
            previewLabel.setText("Error");
        }
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isFunction(String value) {
        return value.trim().startsWith("{") && value.trim().endsWith("}");
    }

    private void handleFunctionSelection(ComboBox<String> functionComboBox, VBox popupLayout, TextField originalCellValueField, Label previewLabel, Stage popupStage) {
        functionComboBox.setOnAction(event -> {
            popupLayout.getChildren().clear();  // Clear existing inputs except function combo box
            popupLayout.getChildren().addAll(functionComboBox, previewLabel);  // Keep functionComboBox and previewLabel visible

            String selectedFunction = functionComboBox.getValue();

            if (selectedFunction != null) {

                // Finalize the argument fields for the lambda
                final Node[] finalArgumentFields = switch (selectedFunction) {
                    case "PLUS", "MINUS", "TIMES", "DIVIDE", "MOD", "POW", "AND", "CONCAT", "EQUAL", "BIGGER", "LESS",
                         "OR", "PERCENT" -> addTwoArgumentFields(popupLayout);  // Add two argument fields
                    case "ABS", "NOT" -> new Node[]{addOneArgumentField(popupLayout)};  // Add one argument field
                    case "SUB", "IF" -> addThreeArgumentFields(popupLayout);  // Add three argument fields
                    case "REF" ->
                            new Node[]{addRefArgumentField(popupLayout)};  // Add ComboBox/TextField for REF function
                    case "AVERAGE", "SUM" -> new Node[]{addRangeArgumentField(popupLayout)};
                    default -> null;

                    // Handle each function's argument requirements
                    // Add ComboBox for range input
                };


                // Add the OK button after argument fields
                Button okButton = new Button("OK");
                okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                popupLayout.getChildren().add(okButton);

                // Live preview update based on function arguments
                if (finalArgumentFields != null) {
                    // Store the selected function in a final variable for use inside the lambda
                    final String finalSelectedFunction = selectedFunction;

                    for (Node argumentField : finalArgumentFields) {
                        if (argumentField instanceof TextField) {
                            ((TextField) argumentField).textProperty().addListener((obs, oldValue, newValue) -> {
                                updatePreviewLabel(previewLabel, finalSelectedFunction, finalArgumentFields, true);  // Update preview for functions
                            });
                        } else if (argumentField instanceof ComboBox) {
                            ((ComboBox<?>) argumentField).valueProperty().addListener((obs, oldValue, newValue) -> {
                                updatePreviewLabel(previewLabel, finalSelectedFunction, finalArgumentFields, true);  // Update preview for range/cell
                            });
                        }
                    }
                }

                // OK button logic for function input
                okButton.setOnAction(okEvent -> {
                    if (finalArgumentFields != null) {
                        String formulaOnly = null;
                        switch (selectedFunction) {
                            case "SUM":
                            case "AVERAGE":
                                // Extract the selected range from the ComboBox
                                String selectedRange = ((ComboBox<String>) finalArgumentFields[0]).getValue();
                                formulaOnly = "{" + selectedFunction + "," + selectedRange + "}";
                                break;

                            case "REF":
                                // Extract the single cell reference from the ComboBox
                                String selectedRef = ((ComboBox<String>) finalArgumentFields[0]).getValue();
                                formulaOnly = "{" + selectedFunction + "," + selectedRef + "}";
                                break;

                            default:
                                // Handle the formula for other functions
                                formulaOnly = previewLabel.getText().substring(previewLabel.getText().indexOf("{"), previewLabel.getText().lastIndexOf("}") + 1);
                                break;
                        }

                        // Set the formula to the cell and close the popup
                        originalCellValueField.setText(formulaOnly);
                        popupStage.close();
                    }
                });
            }
        });
    }

    // Adds a ComboBox for selecting a range
    private ComboBox<String> addRangeArgumentField(VBox popupLayout) {
        Label refLabel = new Label("Enter range name:");
        ComboBox<String> rangeComboBox = new ComboBox<>();
        populateRangesNames(rangeComboBox);
        rangeComboBox.setEditable(true);  // Allow manual input of range names
        rangeComboBox.setPromptText("Select range");

        popupLayout.getChildren().addAll(refLabel, rangeComboBox);
        return rangeComboBox;
    }

    // Adds a ComboBox for selecting a cell reference
    private ComboBox<String> addRefArgumentField(VBox popupLayout) {
        Label refLabel = new Label("Enter Cell Reference:");
        ComboBox<String> refComboBox = new ComboBox<>();
        populateCellReferences(refComboBox);
        refComboBox.setEditable(true);  // Allow manual input of cell references
        refComboBox.setPromptText("Select or enter cell reference");

        popupLayout.getChildren().addAll(refLabel, refComboBox);
        return refComboBox;  // Return the ComboBox to capture the input later
    }

    // Populates the ComboBox with existing range names
    public void populateRangesNames(ComboBox<String> dropdown) {
        dropdown.getItems().clear();  // Clear existing items to avoid duplication
        Set<String> rangeNames = this.appController.getSpreadsheetController().getSpreadsheet().getAllRangeNames();

        for (String rangeName : rangeNames) {
            dropdown.getItems().add(rangeName);
        }
    }

    // Populates the ComboBox with cell references (e.g., A1, B2, C3, etc.)
    public void populateCellReferences(ComboBox<String> dropdown) {
        dropdown.getItems().clear();  // Clear existing items
        int numRows = this.appController.getSpreadsheetController().getSpreadsheet().getNumOfRows();
        int numCols = this.appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns();

        for (int row = 1; row <= numRows; row++) {
            for (int col = 1; col <= numCols; col++) {
                String columnLetter = String.valueOf((char) ('A' + col - 1));
                String cellReference = columnLetter + row;
                dropdown.getItems().add(cellReference);  // Add cell reference
            }
        }
    }

    // Method to add one argument input field
    private TextField addOneArgumentField(VBox popupLayout) {
        Label argLabel = new Label("Enter Argument:");
        TextField argField = new TextField();
        argField.setPromptText("Enter a value");

        popupLayout.getChildren().addAll(argLabel, argField);
        return argField;
    }

    // Method to add two argument input fields
    private TextField[] addTwoArgumentFields(VBox popupLayout) {
        Label arg1Label = new Label("Enter Argument 1:");
        TextField arg1Field = new TextField();
        arg1Field.setPromptText("Enter first value");

        Label arg2Label = new Label("Enter Argument 2:");
        TextField arg2Field = new TextField();
        arg2Field.setPromptText("Enter second value");

        popupLayout.getChildren().addAll(arg1Label, arg1Field, arg2Label, arg2Field);
        return new TextField[]{arg1Field, arg2Field};
    }

    // Method to add three argument input fields
    private TextField[] addThreeArgumentFields(VBox popupLayout) {
        Label arg1Label = new Label("Enter Argument 1:");
        TextField arg1Field = new TextField();
        arg1Field.setPromptText("Enter first value");

        Label arg2Label = new Label("Enter Argument 2:");
        TextField arg2Field = new TextField();
        arg2Field.setPromptText("Enter second value");

        Label arg3Label = new Label("Enter Argument 3:");
        TextField arg3Field = new TextField();
        arg3Field.setPromptText("Enter third value");

        popupLayout.getChildren().addAll(arg1Label, arg1Field, arg2Label, arg2Field, arg3Label, arg3Field);
        return new TextField[]{arg1Field, arg2Field, arg3Field};
    }

    public void setHeadController(HeadController headController) {
        this.headController = headController;
    }

    public CompletableFuture<Void> updateCellValue(String cellIdentifier, String newValue, String oldValue) throws UnsupportedEncodingException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        int currentVersion = spreadsheetManagerDTO.getCurrentVersion();
        String url = null;
        String sheetname = spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName();
        String username = this.appController.getMainDashboardController().getDashboardHeaderController().getDashUserName();
        try {
            // Construct the URL with query parameters
            url = String.format(
                    CELL_UPDATE + "?cellId=%s&newValue=%s&oldValue=%s&sheetName=%s&versionNumber=%d&username=%s",
                    URLEncoder.encode(cellIdentifier, "UTF-8"),
                    URLEncoder.encode(newValue, "UTF-8"),
                    URLEncoder.encode(oldValue, "UTF-8"),
                    URLEncoder.encode(sheetname, "UTF-8"),
                    currentVersion,
                    URLEncoder.encode(username, "UTF-8")
            );

            System.out.println("URL constructed: " + url);  // Debugging print

        } catch (UnsupportedEncodingException e) {
            System.err.println("Error constructing URL: " + e.getMessage());  // Handle exception
            e.printStackTrace();
            future.completeExceptionally(new Exception("Failed to encode URL"));
            return future;  // Return early if URL encoding fails
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()  // Make a GET request
                .build();

        System.out.println("Sending request to: " + url);  // Debugging print to see when request is sent

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Request failed: " + e.getMessage());  // Debugging print on failure
                Platform.runLater(() -> future.completeExceptionally(new Exception("Error updating cell: " + e.getMessage())));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    System.out.println("Response received. Code: " + response.code());  // Debugging print to log response code

                    // Handle different HTTP response codes
                    if (response.code() == 409) {
                        System.out.println("Conflict response received. Handling version conflict.");
                        Platform.runLater(() -> future.completeExceptionally(new Exception("Version Conflict: You are viewing an outdated version. Please update to the latest version.")));
                        return;
                    }

                    if (response.code() == 400 || response.code() == 404) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        System.out.println("Error response received. Code: " + response.code() + ". Body: " + errorBody);  // Debugging print for error
                        Platform.runLater(() -> future.completeExceptionally(new Exception("Error " + response.code() + ": " + errorBody)));
                        return;
                    }

                    // Success response
                    assert response.body() != null;
                    String responseData = response.body().string();
                    System.out.println("Success response data: " + responseData);  // Debugging print to log response data

                    Gson gson = new Gson();
                    CellUpdateDTO cellUpdateDTO = gson.fromJson(responseData, CellUpdateDTO.class);

                    Platform.runLater(() -> {
                        try {
                            updateCellInClientDTO(cellUpdateDTO.getUpdatedCell(), cellUpdateDTO.getDependentCells(), cellUpdateDTO.getLastModifiedBy());
                            refreshCellAndDependents(cellIdentifier);
                            spreadsheetManagerDTO.getCellDTO(cellIdentifier).setLastModifiedBy(cellUpdateDTO.getLastModifiedBy());
                            spreadsheetManagerDTO.setCurrentVersion(cellUpdateDTO.getUpdatedCell().getLastModifiedVersion() + 1);

                            future.complete(null);  // Successfully complete the future.
                        } catch (UnsupportedEncodingException e) {
                            System.out.println("Error processing cell update: " + e.getMessage());  // Debugging print on error
                            future.completeExceptionally(new Exception("Error processing cell update: " + e.getMessage()));
                        }
                    });

                } catch (Exception e) {
                    System.out.println("Exception in response processing: " + e.getMessage());  // Debugging print on error
                    Platform.runLater(() -> future.completeExceptionally(new Exception("Error processing response: " + e.getMessage())));
                } finally {
                    response.close();  // Always close the response body
                }
            }
        });

        return future;  // Return the future to be handled in the calling method
    }

    public void refreshCellAndDependents(String cellIdentifier) throws UnsupportedEncodingException {
        // Send request to server to fetch updated cell value and dependents
        OkHttpClient client = new OkHttpClient();

        // Include userName (current user viewing the sheet) in the request
        String userName = this.appController.getMainDashboardController().getDashboardHeaderController().getDashUserName();

        // Construct the URL with query parameters, including the userName
        String url = String.format(
                "http://localhost:8080/server_Web/getCellUpdate?cellId=%s&sheetName=%s&username=%s",
                cellIdentifier,
                URLEncoder.encode(spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName(), "UTF-8"),
                URLEncoder.encode(userName, "UTF-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, IOException e) {
                Platform.runLater(() -> appController.showError("Error", "Failed to fetch cell updates."));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string();

                // Log the raw response data from the server

                Platform.runLater(() -> {
                    Gson gson = new Gson();
                    try {
                        // Deserialize the server's response
                        CellUpdateDTO cellUpdateDTO = gson.fromJson(responseData, CellUpdateDTO.class);

                        // Refresh the cell and dependents based on the updated data received from the server
                        updateCellInGrid(cellUpdateDTO.getUpdatedCell());
                        for (CellDTO dependentCell : cellUpdateDTO.getDependentCells()) {
                            refreshCellAndDependents(dependentCell.getCellId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public void updateCellInClientDTO(CellDTO updatedCell, List<CellDTO> dependentCells, String lastModifiedBy) {
        // Update the cell in the client-side DTO
        for (CellDTO cell : spreadsheetManagerDTO.getSpreadsheetDTO().getCells()) {
            if (cell.getCellId().equals(updatedCell.getCellId())) {
                // Update the source value, effective value, and other properties
                cell.setSourceValue(updatedCell.getSourceValue());
                cell.setEffectiveValue(updatedCell.getEffectiveValue());
                cell.setLastModifiedVersion(updatedCell.getLastModifiedVersion());
                cell.setTextColor(updatedCell.getTextColor());
                cell.setBackgroundColor(updatedCell.getBackgroundColor());
                cell.setLastModifiedBy(lastModifiedBy);
                break;
            }
        }

        // Update the dependent cells in the client-side DTO
        for (CellDTO dependentCell : dependentCells) {
            for (CellDTO cell : spreadsheetManagerDTO.getSpreadsheetDTO().getCells()) {
                if (cell.getCellId().equals(dependentCell.getCellId())) {
                    // Update the dependent cell properties
                    cell.setSourceValue(dependentCell.getSourceValue());
                    cell.setEffectiveValue(dependentCell.getEffectiveValue());
                    cell.setLastModifiedVersion(dependentCell.getLastModifiedVersion());
                    cell.setTextColor(dependentCell.getTextColor());
                    cell.setBackgroundColor(dependentCell.getBackgroundColor());
                    cell.setLastModifiedBy(lastModifiedBy);
                    break;
                }
            }
        }
    }

    private void updateCellInGrid(CellDTO updatedCell) {
        String currentCellId = updatedCell.getCellId();
        Node cellNode = getCellNodeById(currentCellId);
        if (cellNode instanceof Label) {
            Label cellLabel = (Label) cellNode;

            // Update the cell content based on the updated value
            String newValue = updatedCell.getEffectiveValue().toString();
            cellLabel.setText(newValue.equals("EMPTY") ? "" : newValue);

            // Apply the updated text color
            String textColor = updatedCell.getTextColor();
            cellLabel.setStyle("-fx-text-fill: " + textColor + ";");

            // Apply the updated background color
            String backgroundColor = updatedCell.getBackgroundColor();
            cellLabel.setStyle(cellLabel.getStyle() + "-fx-background-color: " + backgroundColor + ";");
        }

        for (Node node : spreadsheetGrid.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null) {
                int rowIndex = GridPane.getRowIndex(node);
                int columnIndex = GridPane.getColumnIndex(node);

                // Skip the first row and column
                if (rowIndex > 0 && columnIndex > 0) {
                    // String currentCellId = getCellIdFromCoordinates(rowIndex, columnIndex);

                    // Get the current background and text colors of the cell from the data model
                    String backgroundColor = spreadsheetManagerDTO.getCellBackgroundColor(currentCellId);
                    String textColor = spreadsheetManagerDTO.getCellTextColor(currentCellId);
                    String cellClass = this.appController.getCellClassForCurrentSkin();

                    // Only change the cell if it has a white background and black text
                    if ((backgroundColor.equals("white") || backgroundColor.equals("#FFFFFF")) &&
                            (textColor.equals("black") || textColor.equals("#000000"))) {
                        node.getStyleClass().clear();
                        node.getStyleClass().add(cellClass);

                        // Re-apply borders and text color based on the current skin
                        node.setStyle("-fx-text-fill: " + (cellClass.equals("dark-cell") ? "#ffffff" : "#000000") + "; -fx-border-color: " + (cellClass.equals("dark-cell") ? "#ffffff" : "#000000") + ";");
                    } else {
                        node.getStyleClass().clear();
                        node.getStyleClass().add(cellClass);

                        // Re-apply borders and text color based on the model (with correct CSS values)
                        node.setStyle("-fx-text-fill: " + textColor + "; -fx-border-color: #000000; -fx-background-color: " + backgroundColor + ";");
                    }
                }
            }
        }

    }

    public void startPollingForNewVersions() {
        if (isPolling) return;  // Prevent multiple polling sessions
        if (spreadsheetManagerDTO == null) {
            return;  // Exit if the DTO is not initialized
        }

        isPolling = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Check for new version from the server
                checkForNewVersion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);  // Poll every 10 seconds
    }

    public void stopPollingForNewVersions() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        isPolling = false;
    }

    private void checkForNewVersion() throws UnsupportedEncodingException {
        if (spreadsheetManagerDTO == null) {
            return;
        }

        // This method should call the server and check for new versions
        OkHttpClient client = new OkHttpClient();
        String sheetName = spreadsheetManagerDTO.getSpreadsheetDTO().getSheetName();
        String url = "http://localhost:8080/server_Web/getLatestVersion?sheetName=" + URLEncoder.encode(sheetName, "UTF-8");

        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Failed to check for new versions: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string();
                Platform.runLater(() -> {
                    // Assuming the server returns the latest version number
                    int latestVersion = Integer.parseInt(responseData);

                    if (latestVersion > spreadsheetManagerDTO.getCurrentVersion()) {
                        // A new version is available, show the indicator
                        headController.showNewVersionIndicator(latestVersion);
                    }
                });
            }
        });
    }

    public String getCellIdFromCoordinates(int row, int column) {
        char columnLetter = (char) ('A' + (column - 1));
        return String.valueOf(columnLetter) + row;
    }

    public void applyColumnAlignment(String columnLetter, Pos alignment) {
        int columnIndex = columnLetter.charAt(0) - 'A' + 1;  // Convert column letter to column index

        for (Node node : spreadsheetGrid.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == columnIndex && GridPane.getRowIndex(node) != 0) {
                if (node instanceof Label) {
                    ((Label) node).setAlignment(alignment);  // Set the alignment
                }
            }
        }
    }

    public GridPane getGridPane() {
        return this.spreadsheetGrid;
    }

    public void applyWidthToColumn(String columnIndex, double newWidth) {
        int colIndex = columnIndex.charAt(0) - 'A' + 1; // Convert letter to index (1-based)
        double oldWidth = spreadsheetGrid.getColumnConstraints().get(colIndex).getPrefWidth();
        // Update the width of the actual column
        if (colIndex >= 1 && colIndex <= spreadsheetGrid.getColumnConstraints().size()) {
            ColumnConstraints columnConstraints = spreadsheetGrid.getColumnConstraints().get(colIndex);
            columnConstraints.setPrefWidth(newWidth);
            columnConstraints.setMinWidth(newWidth);
            columnConstraints.setMaxWidth(newWidth);

        } else {
            throw new IllegalArgumentException("Invalid column index.");
        }

        // Update the width of the column header (first row, corresponding to the column letter)
        Label columnLabel = (Label) getNodeFromGridPane(spreadsheetGrid, colIndex, 0); // Get header label
        if (columnLabel != null) {
            columnLabel.setPrefWidth(newWidth);
            columnLabel.setMinWidth(newWidth);
            columnLabel.setMaxWidth(newWidth);
        }
        //  spreadsheetGrid.setAlignment(Pos.TOP_LEFT);  // Ensure it doesn't grow to the left
        //  spreadsheetGrid.setAlignment(Pos.TOP_CENTER);
        appController.getMainContainer().setPrefWidth((this.appController.getMainContainer().getWidth()) - oldWidth + newWidth);
        // Force the layout to refresh
        spreadsheetGrid.applyCss(); // Force CSS recalculation
        spreadsheetGrid.layout();   // Force layout update
    }

    public void applyHeightToRow(int rowIndex, double newHeight) {
        double oldHeight = spreadsheetGrid.getRowConstraints().get(rowIndex).getPrefHeight();

        // Update the height of the actual row
        if (rowIndex >= 1 && rowIndex <= spreadsheetGrid.getRowConstraints().size()) {
            RowConstraints rowConstraints = spreadsheetGrid.getRowConstraints().get(rowIndex);
            rowConstraints.setPrefHeight(newHeight);
            rowConstraints.setMinHeight(newHeight);
            rowConstraints.setMaxHeight(newHeight);


        } else {
            throw new IllegalArgumentException("Invalid row index.");
        }

        // Update the height of the row header (first column, corresponding to the row number)
        Label rowNumberLabel = (Label) getNodeFromGridPane(spreadsheetGrid, 0, rowIndex); // Get header label
        if (rowNumberLabel != null) {
            rowNumberLabel.setPrefHeight(newHeight);
            rowNumberLabel.setMinHeight(newHeight);
            rowNumberLabel.setMaxHeight(newHeight);
        }

        spreadsheetGrid.setAlignment(Pos.TOP_LEFT);  // Ensure it doesn't grow to the left
        spreadsheetGrid.setAlignment(Pos.TOP_CENTER);
        double gridHeight = appController.getMainContainer().getPrefHeight() - oldHeight + newHeight;
        appController.getMainContainer().setPrefHeight(gridHeight);

        // Force the layout to refresh
        spreadsheetGrid.applyCss(); // Force CSS recalculation
        spreadsheetGrid.layout();   // Force layout update
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    public void setMainController(appController appController) {
        this.appController = appController;
    }

    public SpreadsheetManagerDTO getSpreadsheet() {
        return spreadsheetManagerDTO;
    }

    public void highlightCellsInRange(Set<String> cellIdsInRange) {
        // Iterate over all the children (cells) in the GridPane
        for (Node node : spreadsheetGrid.getChildren()) {
            if (node instanceof Label) {
                // Get the column and row indices of the cell
                Integer colIndex = GridPane.getColumnIndex(node);
                Integer rowIndex = GridPane.getRowIndex(node);

                if (colIndex != null && rowIndex != null) {
                    // Generate the cell ID (e.g., A1, B2) based on column and row indices
                    String cellId = getCellIdFromCoordinates(rowIndex, colIndex);

                    // If the cell is part of the range, highlight it
                    if (cellIdsInRange.contains(cellId)) {
                        node.setStyle("-fx-background-color: lightgreen; -fx-border-color: black; -fx-padding: 5px;");
                    } else if (colIndex != 0 && rowIndex != 0) {
                        // Reset the style for non-range cells
                        node.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 5px;");
                    }
                }
            }
        }
    }

    public static java.awt.Color toAwtColor(javafx.scene.paint.Color fxColor) {
        return new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) fxColor.getOpacity());
    }

    private String toRgbCode(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void applyCellTextColor(String cellId, String textColor) {
        Node cellNode = getCellNodeById(cellId);
        if (cellNode instanceof Label) {
            Label label = (Label) cellNode;
            // Apply both colors (keeping background if set)
            String backgroundColor = spreadsheetManagerDTO.getCellBackgroundColor(cellId);
            label.setStyle("-fx-text-fill: " + textColor + "; -fx-background-color: " + backgroundColor + ";");
        }
        spreadsheetManagerDTO.setCellTextColor(cellId, textColor);
    }

    public void applyCellBackgroundColor(String cellId, String backgroundColor) {
        Node cellNode = getCellNodeById(cellId);
        if (cellNode instanceof Label) {
            Label label = (Label) cellNode;
            String textColor = spreadsheetManagerDTO.getCellTextColor(cellId);
            label.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + ";");
        }
        spreadsheetManagerDTO.setCellBackgroundColor(cellId, backgroundColor);
    }

    public void resetCellFormatting(String cellId) {
        // Get the cell's node in the grid
        Node cellNode = getCellNodeById(cellId);
        if (cellNode instanceof Label) {
            // Reset the text color to black and background color to white
            ((Label) cellNode).setStyle("-fx-text-fill: #000000; -fx-background-color: #FFFFFF;");

            // Reset formatting in the internal data model
            spreadsheetManagerDTO.setCellTextColor(cellId, "#000000"); // Default text color (black)
            spreadsheetManagerDTO.setCellBackgroundColor(cellId, "#FFFFFF"); // Default background color (white)
        }
    }

    private Node getCellNodeById(String cellId) {
        for (Node node : spreadsheetGrid.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null) {
                String currentCellId = getCellIdFromCoordinates(GridPane.getRowIndex(node), GridPane.getColumnIndex(node));
                if (currentCellId.equals(cellId)) {
                    return node;
                }
            }
        }
        return null; // Return null if no matching cell is found
    }

    public List<Map<String, String>> getAllRowsInSpreadsheet() {
        int numRows = spreadsheetManagerDTO.getNumOfRows();
        int numCols = appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns();

        List<Map<String, String>> fullSpreadsheetData = new ArrayList<>();
        for (int row = 1; row <= numRows; row++) {
            Map<String, String> rowData = new HashMap<>();
            for (int col = 1; col <= numCols; col++) {
                String cellId = String.valueOf((char) ('A' + col - 1)) + row;
                String cellValue = appController.getSpreadsheetController().getSpreadsheet().getCellDTO(cellId).getEffectiveValue().toString();
                rowData.put(String.valueOf((char) ('A' + col - 1)), cellValue);
            }
            fullSpreadsheetData.add(rowData);
        }
        return fullSpreadsheetData;
    }

    public void showFullSpreadsheetWithSortedSection(List<Map<String, String>> sortedRows, int fromRow, int toRow, int fromColumn, int toColumn, Map<String, CellDTO> cellDesignMap) {
        List<Map<String, String>> fullSpreadsheetData = getAllRowsInSpreadsheet();

        // Update the sorted section back into the full spreadsheet
        this.appController.getCommandAndRangesController().updateFullSpreadsheetWithSortedSection(fullSpreadsheetData, sortedRows, fromRow, toRow, fromColumn, toColumn);

        // Display the full spreadsheet with the sorted section integrated
        Stage sortedPopupStage = new Stage();
        sortedPopupStage.setTitle("Sorted Spreadsheet");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f9f9f9;");

        GridPane fullSpreadsheetGrid = new GridPane();
        fullSpreadsheetGrid.setGridLinesVisible(true);
        fullSpreadsheetGrid.setHgap(2);
        fullSpreadsheetGrid.setVgap(2);


        int numberOfRows = appController.getSpreadsheetController().getSpreadsheet().getNumOfRows();
        int numberOfColumns = appController.getSpreadsheetController().getSpreadsheet().getNumOfColumns();


        double cellWidth = 80; // Consistent width for all columns
        double cellHeight = 30;   // Consistent height for all rows

        for (int col = 0; col <= numberOfColumns; col++) {
            ColumnConstraints columnConstraints = new ColumnConstraints(cellWidth);
            columnConstraints.setHgrow(Priority.ALWAYS);
            fullSpreadsheetGrid.getColumnConstraints().add(columnConstraints);
        }

        // Set row constraints for consistent height
        for (int row = 0; row <= numberOfRows; row++) {
            RowConstraints rowConstraints = new RowConstraints(cellHeight);
            rowConstraints.setVgrow(Priority.ALWAYS);
            fullSpreadsheetGrid.getRowConstraints().add(rowConstraints);
        }

        // Add column headers (A, B, C, ...)
        for (int col = 1; col <= numberOfColumns; col++) {
            Label columnHeader = new Label(String.valueOf((char) ('A' + (col - 1))));
            columnHeader.setAlignment(Pos.CENTER);
            columnHeader.setMaxWidth(Double.MAX_VALUE);
            columnHeader.setMaxHeight(Double.MAX_VALUE);
            columnHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5px; -fx-border-color: #a9a9a9;");
            fullSpreadsheetGrid.add(columnHeader, col, 0);  // Add to the top row
        }

        // Add row headers (1, 2, 3, ...)
        for (int row = 1; row <= numberOfRows; row++) {
            Label rowHeader = new Label(String.valueOf(row));
            rowHeader.setAlignment(Pos.CENTER);
            rowHeader.setMaxWidth(Double.MAX_VALUE);
            rowHeader.setMaxHeight(Double.MAX_VALUE);
            rowHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #d3d3d3; -fx-padding: 5px; -fx-border-color: #a9a9a9;");
            fullSpreadsheetGrid.add(rowHeader, 0, row);  // Add to the leftmost column
        }


        // Populate the grid with cell values from the fullSpreadsheetData
        int rowIndex = 1;
        for (Map<String, String> row : fullSpreadsheetData) {
            int columnIndex = 1;
            for (Map.Entry<String, String> cellEntry : row.entrySet()) {
                String cellValue = cellEntry.getValue();

                // Retrieve the corresponding colors from the cellDesignMap using the cell ID
                String cellId = this.appController.getSpreadsheetController().getCellIdFromCoordinates(rowIndex, columnIndex);
                CellDTO cellDTO = cellDesignMap.get(cellId);
                String textColor = cellDTO != null ? cellDTO.getTextColor() : "black";
                String backgroundColor = cellDTO != null ? cellDTO.getBackgroundColor() : "white";

                Label cellLabel = new Label((cellValue.equals("EMPTY") ? "" : cellValue));
                cellLabel.setAlignment(Pos.CENTER);
                cellLabel.setStyle("-fx-border-color: #a9a9a9; -fx-padding: 5px; -fx-border-width: 1;" +
                        "-fx-text-fill: " + textColor + ";" +    // Apply text color
                        "-fx-background-color: " + backgroundColor + ";"); // Apply background color
                cellLabel.setPrefWidth(cellWidth);
                cellLabel.setPrefHeight(cellHeight);

                fullSpreadsheetGrid.add(cellLabel, columnIndex, rowIndex);

                columnIndex++;
            }
            rowIndex++;
        }

        ScrollPane scrollPane = new ScrollPane(fullSpreadsheetGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        layout.getChildren().add(scrollPane);

        Scene scene = new Scene(layout, 800, 600);
        sortedPopupStage.setScene(scene);
        sortedPopupStage.show();
    }

    private String formatCellValue(String value) {
        if (value.equalsIgnoreCase("EMPTY")) {
            return "";
        }

        // Handle boolean values
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return value.toUpperCase();
        }

        // Handle numbers with decimals
        try {
            double number = Double.parseDouble(value);
            // If the number has no fractional part, return it without ".0"
            if (number == (int) number) {
                return String.valueOf((int) number);
            } else {
                return value;  // Keep the decimal part as is
            }
        } catch (NumberFormatException e) {
            // Not a number, return the value as is
            return value;
        }
    }

    public void disableEditing() {
        // Disable grid editing but leave header editable for non-READERS
        for (Node node : spreadsheetGrid.getChildren()) {
            if (node instanceof TextField || node instanceof Button || node instanceof Label) {
                node.setDisable(true);  // Disable text fields and buttons for grid editing
            }
        }


    }
}
