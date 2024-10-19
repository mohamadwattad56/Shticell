package login;

import com.sun.istack.NotNull;
import dashboard.chat.main.ChatAppMainController;
import dashboard.mainDashboardController.MainDashboardController;
import gridPageController.mainController.appController;
import httputils.HttpClientUtil;
import jakarta.servlet.http.HttpServletResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.*;
import java.io.IOException;

import static httputils.Constants.LOGIN_PAGE;

public class LoginController {

    @FXML
    private TextField userNameTextField;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Button quitButton;

    @FXML
    private Button loginButton;



    private appController mainController;  // Reference to the main controller
    private final StringProperty errorMessageProperty = new SimpleStringProperty();  // Property for error messages
    private Stage primaryStage;  // To handle stage switching
    private ChatAppMainController chatAppMainController;

    @FXML
    public void initialize() {
        // Bind error messages to the label in the UI
        errorMessageLabel.textProperty().bind(errorMessageProperty);

        // Disable login button initially
        loginButton.setDisable(true);

        // Handle the quit button action
        quitButton.setOnAction(this::quitButtonClicked);

        // Add a listener to the TextField to enable the button when input is detected
        userNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Enable the button if the new value is not empty, disable it otherwise
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        loginButton.setOnAction(this::loginButtonClicked);
    }


    // Triggered when the login button is clicked
    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText();

        if (userName.isEmpty()) {
            errorMessageProperty.set("Username cannot be empty.");
            return;
        }

        // Build the login URL with the username as a query parameter
        String finalUrl = HttpUrl.parse(LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();

        // Send asynchronous login request to the server
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorMessageProperty.set("Connection failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    // Handle error response
                    String responseBody = response.body().string();
                    Platform.runLater(() -> errorMessageProperty.set("Login failed: " + responseBody));
                } else {
                    // Handle successful login
                    Platform.runLater(() -> switchToDashboard(userName));
                }
            }
        });
    }


    // Switch to the dashboard after successful login
    private void switchToDashboard(String username) {
        try {
            // Load the main dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/mainDashboardController/mainDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the controller and set the username
            MainDashboardController dashboardController = loader.getController();
            if (dashboardController != null) {
                dashboardController.setDashUserName(username);
            }

            // Create a new stage for the dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - " + username);
            Scene dashboardScene = new Scene(dashboardRoot);
            dashboardStage.setScene(dashboardScene);

            // Set the on-close request for the dashboard window
            dashboardStage.setOnCloseRequest(event -> {
                System.out.println("Dashboard is closing! Removing user from list...");

                // Send an HTTP request to remove the user from the set
                String finalUrl = "http://localhost:8080/server_Web/removeuser";  // Update with the actual URL
                HttpClientUtil.runAsync(finalUrl, new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                        System.out.println("Failed to remove user: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                        System.out.println("Remove User Servlet Response Code: " + response.code());
                        if (response.code() == HttpServletResponse.SC_OK) {
                            System.out.println("User removed successfully.");
                        } else {
                            System.out.println("Failed to remove user. Response code: " + response.code());
                        }
                    }
                });
            });


            // Show the dashboard stage
            dashboardStage.show();

            // Close the login window
            primaryStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            errorMessageProperty.set("Failed to load the dashboard.");
        }
    }





    // Clears the error message when the user types in the username field
    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    // Exits the application when the quit button is clicked
    @FXML
    private void quitButtonClicked(ActionEvent event) {
        Platform.exit();
    }


    // Set primary stage to handle switching between windows
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    public void setChatAppMainController(ChatAppMainController chatAppMainController) {
        this.chatAppMainController = chatAppMainController;
    }
}