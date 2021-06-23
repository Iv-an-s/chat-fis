package client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;

public class Controller /*implements Initializable*/ {
                                                // Интерфейс дает возможность проводить подготовительные действия,
                                                  // проводить преднастройку контроллера
    @FXML
    TextArea msgArea;
    @FXML
    TextField msgField, usernameField;
    @FXML
    HBox msgPanel, loginPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void setUsername (String username){
        this.username = username;
        if(username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        }else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//
//    }

    public void login(){
        if(socket == null || socket.isClosed()){
            connect();
        }
        if(usernameField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + usernameField.getText());
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно подключиться к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus(); // после предыдущего действия запрашиваем фокус в поле msgField
        }catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
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
}
