package login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main2 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file relative to the current package
        URL fxmlLocation = getClass().getResource("login.fxml");

        if (fxmlLocation == null) {
            System.out.println("FXML file not found!");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();
        LoginController loginController = loader.getController();
        loginController.setPrimaryStage(primaryStage);
        // Set the title and scene
        primaryStage.setTitle("Login");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Show the login window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
