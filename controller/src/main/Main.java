/*
package main;

import gridPageController.CommandRangesController.CommandAndRangesController;
import gridPageController.gridController.SpreadsheetController;
import gridPageController.headController.HeadController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import gridPageController.mainController.appController;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main layout (StackPane as root with ScrollPane inside)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gridPageController/mainController/MainLayout.fxml"));

        StackPane root = fxmlLoader.load();  // The root is now StackPane

        // Get the main controller
        appController appController = fxmlLoader.getController();  // Must be after loading the FXML

        // Load the HeaderController
        FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/gridPageController/headController/HeaderController.fxml"));
        VBox headerComponent = headerLoader.load();
        HeadController headController = headerLoader.getController();

        // Load the CommandAndRangesController
        FXMLLoader commandRangesLoader = new FXMLLoader(getClass().getResource("/gridPageController/CommandRangesController/CommandAndRanges.fxml"));
        VBox commandRangesComponent = commandRangesLoader.load();
        CommandAndRangesController commandAndRangesController = commandRangesLoader.getController();

        // Load the SpreadsheetController
        FXMLLoader spreadsheetLoader = new FXMLLoader(getClass().getResource("/gridPageController/gridController/GridController.fxml"));
        GridPane spreadsheetComponent = spreadsheetLoader.load();
        SpreadsheetController spreadsheetController = spreadsheetLoader.getController();

        // Access the BorderPane inside the ScrollPane
        ScrollPane scrollPane = (ScrollPane) root.getChildren().get(0);  // Get the ScrollPane from StackPane
        BorderPane mainContainer = (BorderPane) scrollPane.getContent();  // Get the BorderPane inside ScrollPane

        // Set the components in the BorderPane
        mainContainer.setTop(headerComponent);
        mainContainer.setLeft(commandRangesComponent);
        mainContainer.setCenter(spreadsheetComponent);

        // Connect subcomponents to the main controller
        appController.setHeadController(headController);
        appController.setCommandAndRangesController(commandAndRangesController);
        appController.setSpreadsheetController(spreadsheetController);

        // Pass HeadController to SpreadsheetController
        spreadsheetController.setHeadController(headController);

        // Set up the primary stage
        Scene scene = new Scene(root, 1024, 800);

        // Apply CSS stylesheet here
        scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

        // Set up the primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Shticell Application");
        primaryStage.show();
    }
}
*/
