package dashboard.chat.main;
import dashboard.chat.api.HttpStatusUpdate;
import dashboard.chat.chatroom.ChatRoomMainController;
import dashboard.chat.status.StatusController;
import dashboard.mainDashboardController.MainDashboardController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import login.LoginController;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static httputils.Constants.*;

public class ChatAppMainController implements Closeable, HttpStatusUpdate {

    @FXML private Parent httpStatusComponent;
    @FXML private StatusController httpStatusComponentController;

    private GridPane loginComponent;

    private Parent chatRoomComponent;
    private ChatRoomMainController chatRoomComponentController;

    private MainDashboardController mainDashboardController;

    @FXML private Label userGreetingLabel;
    @FXML private AnchorPane mainPanel;

    private final StringProperty currentUserName;

    public ChatAppMainController() {
        currentUserName = new SimpleStringProperty(DEFAULT_USER);
    }

    @FXML
    public void initialize() {
        userGreetingLabel.textProperty().bind(Bindings.concat("Hello ", currentUserName));

        // prepare components
        //loadLoginPage();
        //loadChatRoomPage();
    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    @Override
    public void close() throws IOException {
        chatRoomComponentController.close();
    }

 /*   private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource("/login/login.fxml");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            LoginController logicController = fxmlLoader.getController();
            logicController.setChatAppMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void loadChatRoomPage() {
        if (chatRoomComponent == null) {  // Only load if not already loaded
            URL chatRoomPageUrl = getClass().getResource("/dashboard/chat/chatroom/chat-room-main.fxml");
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(chatRoomPageUrl);
                chatRoomComponent = fxmlLoader.load();
                chatRoomComponentController = fxmlLoader.getController();
                chatRoomComponentController.setChatAppMainController(this);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void updateHttpLine(String line) {
        httpStatusComponentController.addHttpStatusLine(line);
    }

    public void switchToChatRoom() {
        setMainPanelTo(chatRoomComponent);
        chatRoomComponentController.setActive();
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set(DEFAULT_USER);
            chatRoomComponentController.setInActive();
            setMainPanelTo(loginComponent);
        });
    }

    public Parent getChatRoomComponent() {
        return chatRoomComponent;
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }
    public MainDashboardController getMainDashboardController() {
        return mainDashboardController;
    }
}
