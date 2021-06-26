package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
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

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void setUsername (String username){
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
    }

    public void login(){
        if(loginField.getText().isEmpty()){
            showErrorAlert("Имя пользователя не может быть пустым");
            return;
        }
        if(socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/login " + loginField.getText() + " " + passwordField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
        try {
            socket = new Socket("localhost", 8189 );
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            Thread t = new Thread(() -> {
                try {
                    // Цикл авторизации
                    while (true) {
                        String msg = in.readUTF();
                        if(msg.startsWith("/login_ok ")){
                            setUsername(msg.split("\\s")[1]);
                            break;
                        }
                        if(msg.startsWith("/login_failed ")){
                            String cause = msg.split("\\s", 2)[1];
                            msgArea.appendText(cause + "\n");
                        }
                    }
                    // Цикл общения
                    while (true){
                        String msg = in.readUTF();
                        if(msg.startsWith("/")){
                            if(msg.startsWith("/clients_list ")){
                                String[] tokens = msg.split("\\s");
                                //for (int i = 0; i < tokens.length; i++) {
                                    //System.out.println(tokens[i]);
                                Platform.runLater(()->{ //передаем задачу в поток JavaFX. Если пытаться это делать из текущего треда напрямую - будут ошибки
                                    // В поток JavaFX из других потоков не лезем. Предаем задачи через Platform
                                    clientsList.getItems().clear(); // getItems - запрос списка элементов, которые есть у view
                                    for (int i = 1; i < tokens.length ; i++) {
                                        clientsList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                            continue;
                        }
                        msgArea.appendText(msg + "\n");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    disconnect();
                }
            });
            t.start();
        } catch (IOException e) {
            showErrorAlert("Невозможно подключиться к серверу");
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus(); // после предыдущего действия запрашиваем фокус в поле msgField
        }catch (IOException e){
            showErrorAlert("Невозможно отправить сообщение");
        }
    }

    private void disconnect(){
        setUsername(null);
        try {
            if(socket!=null){
            socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setTitle("Fisunov Chat FX");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
