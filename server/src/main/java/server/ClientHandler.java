package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket; // главное что должен знать обработчик о клиенте
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public String getUsername(){
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(()-> {
            try {
                // authorization loop
                while (true){
                    String msg = in.readUTF();
                    if(msg.startsWith("/login ")){
                        String usernameFromLogin = msg.split("\\s")[1];
                        if(server.isNickBusy(usernameFromLogin)) {
                            sendMessage("/login_failed Current nickname is already used");
                            continue;
                        }
                        username = usernameFromLogin;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;
                    }
                }
                // chatting with client loop
                while(true){
                    String msg = in.readUTF();

                    if(msg.equals("/who_am_i")){
                        sendMessage(username);
                        continue;
                    }

                    if(msg.startsWith("/w ")){
                        server.sendPrivateMessage(msg.split("\\s", 3)[1], msg.split("\\s", 3)[2]);
                        continue;
                    }

                    if(msg.equals("/exit")){
                        break;
                        //disconnect();
                    }

                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                disconnect();
            }
        }).start();
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

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }
}
