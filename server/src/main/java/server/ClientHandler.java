package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket; // главное что должен знать обработчик о клиенте
    private final DataInputStream in;
    private final DataOutputStream out;
    private String username;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                // authorization loop
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        // /login Bob 100xyz
                        String[] tokens = msg.split("\\s+");
                        if (tokens.length != 3) {
                            sendMessage("/login_failed Введите имя пользователя и пароль");
                            continue;
                        }
                        String login = tokens[1];
                        String password = tokens[2];

                        String userNickname = server.getAuthenticationProvider().getNicknameByLoginAndPassword(login, password);
                        if (userNickname == null) {
                            sendMessage("/login_failed Введен некорректный логин/пароль");
                            continue;
                        }
                        if (server.isUserOnline(userNickname)) {
                            sendMessage("/login_failed Учетная запись уже используется");
                            continue;
                        }
                        username = userNickname;
                        sendMessage("/login_ok " + login + " " + username);
                        server.subscribe(this);
                        break;
                    }
                }
                // chatting with client loop
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }

                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private void executeCommand(String cmd) {
        // /w Bob Hello, Bob!!!
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s+", 3);
            if (tokens.length != 3) {
                sendMessage("Server: Incorrect command!!!");
                return;
            }
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
            return;
        }
        // /change_nick myNewNickName
        if (cmd.startsWith("/change_nick ")) {
            String[] tokens = cmd.split("\\s+");
            if (tokens.length != 2) {
                sendMessage("Server: Incorrect command!!!");
                return;
            }
            String newNickname = tokens[1];
            if (server.getAuthenticationProvider().isNickBusy(newNickname)) {
                sendMessage("Server: This nickname is already used");
                return;
            }
            server.getAuthenticationProvider().changeNickname(username, newNickname);
            this.username = newNickname;
            sendMessage("Server: Вы изменили никнейм на " + newNickname);
            server.broadcastClientsList();
        }
    }

    private void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
//            e.printStackTrace();
        }
    }
}
