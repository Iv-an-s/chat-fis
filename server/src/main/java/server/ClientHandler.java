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

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(()-> {
            try {
                while(true){
                    String msg = null;
                        msg = in.readUTF(); // если клиент остановит соединение, сервер  все равно будет пытаться
                    // отсюда что-то вычитать. Чтобы этого не происходило try выносим за цикл. Тогда выброшенное исключение
                    // будет обрабатываться за пределами цикла, а значит из цикла мы выйдем.
  //                      sendMessage("Echo: " + msg);
                    server.broadcastMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                disconnect(socket);
            }
        }).start();
    }

    private void disconnect(Socket socket) {
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
