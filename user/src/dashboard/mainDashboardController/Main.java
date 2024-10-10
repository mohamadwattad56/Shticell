package dashboard.mainDashboardController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login layout FXML
        Parent root = FXMLLoader.load(getClass().getResource("/loginController/loginController.fxml"));

        // Set the scene
        Scene scene = new Scene(root, 400, 300);
        // Load the CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("/resources/login.css").toExternalForm());

        primaryStage.setTitle("Login to MA Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
