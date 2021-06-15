package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) throws Exception {
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            System.out.println("Сервер запущен на порту 8189. Ожидаем клиентов...");
            Socket socket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.printf("Client %s connected%n", socket.getInetAddress());

            String msg;
            while (true){
                msg = inputStream.readUTF();
                System.out.println(msg);
                outputStream.writeUTF("Echo" + msg);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
