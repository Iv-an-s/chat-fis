package client;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Callback onAuthOkCallback;
    private Callback onAuthFailedCallback;
    private Callback onMessageReceivedCallback;
    private Callback onConnectCallback;
    private Callback onDisconnectCallback;


    public void setOnAuthOkCallback(Callback onAuthOkCallback) {
        this.onAuthOkCallback = onAuthOkCallback;
    }

    public void setOnAuthFailedCallback(Callback onAuthFailedCallback) {
        this.onAuthFailedCallback = onAuthFailedCallback;
    }

    public void setOnMessageReceivedCallback(Callback onMessageReceivedCallback) {
        this.onMessageReceivedCallback = onMessageReceivedCallback;
    }

    public void setOnConnectCallback(Callback onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
    }

    public void setOnDisconnectCallback(Callback onDisconnectCallback) {
        this.onDisconnectCallback = onDisconnectCallback;
    }

    public boolean isConnected(){
        return socket != null && !socket.isClosed();
    }

    public void connect(int port) throws IOException {
            socket = new Socket("localhost", port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            if(onConnectCallback != null) {
                onConnectCallback.callback();
            }
            Thread t = new Thread(() -> {
                try {
                    // Цикл авторизации
                    while (true) {
                        String msg = in.readUTF();
                        if(msg.startsWith("/login_ok ")){
                            if (onAuthOkCallback != null) {
                                onAuthOkCallback.callback(msg);
                            }
                            break;
                        }
                        if(msg.startsWith("/login_failed ")){
                            String cause = msg.split("\\s", 2)[1];
                            if (onAuthFailedCallback != null) {
                                onAuthFailedCallback.callback(cause);
                            }
                        }
                    }
                    // Цикл общения
                    while (true){
                        String msg = in.readUTF();
                        if (onMessageReceivedCallback != null){
                            onMessageReceivedCallback.callback(msg);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    disconnect();
                }
            });
            t.start();
        }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void tryToLogin(String login, String password) throws IOException {
        sendMessage("/login " + login + " " + password);
    }


    public void disconnect(){
        if (onDisconnectCallback != null){
            onDisconnectCallback.callback();
        }
        try {
            if(in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
