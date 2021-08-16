package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {// Интерфейс дает возможность проводить подготовительные действия,
    // проводить преднастройку контроллера
    @FXML
    TextArea msgArea;
    @FXML
    ListView<String> clientsList;
    @FXML
    TextField msgField, loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    HBox msgPanel, loginPanel;

    private Network network;
    private String username;
    private HistoryManager historyManager;

    public void setUsername(String username) {
        this.username = username;
        boolean usernameIsNull = username == null;
        loginPanel.setVisible(usernameIsNull);
        loginPanel.setManaged(usernameIsNull);
        msgPanel.setVisible(!usernameIsNull);
        msgPanel.setManaged(!usernameIsNull);
        clientsList.setVisible(!usernameIsNull);
        clientsList.setManaged(!usernameIsNull);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
        network = new Network();

        network.setOnAuthFailedCallback(new Callback() {
            @Override
            public void callback(Object... args) {
                String cause = (String) args[0];
                msgArea.appendText(cause + "\n");
            }
        });

        network.setOnAuthOkCallback(new Callback() {
            @Override
            public void callback(Object... args) {
                String msg = (String) args[0];
                setUsername(msg.split("\\s")[2]);
                historyManager.init(msg.split("\\s")[1]);
                msgArea.clear();
                msgArea.appendText(historyManager.load());
            }
        });

        network.setOnMessageReceivedCallback(new Callback() {
            @Override
            public void callback(Object... args) {
                String msg = (String) args[0];
                if (msg.startsWith("/")) {
                    if (msg.startsWith("/clients_list ")) {
                        // /clients_list Bob Max Jack
                        String[] tokens = msg.split("\\s");
                        Platform.runLater(() -> { //передаем задачу в поток JavaFX. Если пытаться это делать из текущего треда напрямую - будут ошибки
                            // В поток JavaFX из других потоков не лезем. Предаем задачи через Platform
                            clientsList.getItems().clear(); // getItems - запрос списка элементов, которые есть у view
                            for (int i = 1; i < tokens.length; i++) {
                                clientsList.getItems().add(tokens[i]);
                            }
                        });
                    }
                    return;
                }
                historyManager.write(msg + "\n");
                msgArea.appendText(msg + "\n");
            }
        });

        network.setOnDisconnectCallback(new Callback() {
            @Override
            public void callback(Object... args) {
                // сбрасываем имя пользователя, и забываем про его историю
                setUsername(null);
                historyManager.close();
            }
        });

        historyManager = new HistoryManager();
    }

    public void login() {
        if (loginField.getText().isEmpty()) {
            showErrorAlert("Имя пользователя не может быть пустым");
            return;
        }

        if (!network.isConnected()) {
            try {
                network.connect(8189);
            } catch (IOException e) {
                showErrorAlert("Невозможно подключиться к серверу на порт: " + 8189);
                return;
            }
        }

        try {
            network.tryToLogin(loginField.getText(), passwordField.getText());
        } catch (IOException e) {
            showErrorAlert("Невозможно отправить данные пользователя");
            return;
        }
    }

    public void sendMsg() {
        try {
            network.sendMessage(msgField.getText());
            msgField.clear();
            msgField.requestFocus(); // после предыдущего действия запрашиваем фокус в поле msgField
        } catch (IOException e) {
            showErrorAlert("Невозможно отправить сообщение");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setTitle("Fisunov Chat FX");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
