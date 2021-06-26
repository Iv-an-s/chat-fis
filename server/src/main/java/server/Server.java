package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticationProvider authenticationProvider;

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.authenticationProvider = new InMemoryAuthenticationProvider();
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                System.out.println("Ждем нового клиента");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вошел в чат");
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastMessage("Client " + clientHandler.getUsername() + " left this chat");
        broadcastClientsList();

    }

    public synchronized void broadcastMessage(String message){
        for(ClientHandler clientHandler : clients){
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message){
        for (ClientHandler clientHandler : clients){
            if(clientHandler.getUsername().equals(receiverUsername)) {
                clientHandler.sendMessage("От: " + sender.getUsername() + " Сообщение: " + message);
                sender.sendMessage("Пользователю: " + receiverUsername + " Сообщение: " + message + " отправлено.");
                return;
            }
        }
        sender.sendMessage("Невозможно отправить сообщение пользователю:" + receiverUsername + ". Такого пользователя нет в сети.");
    }

    public synchronized boolean isUserOnline(String username){
        for(ClientHandler clientHandler : clients){
            if (clientHandler.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClientsList(){
        StringBuilder sb = new StringBuilder("/clients_list ");
        for(ClientHandler client : clients){
            sb.append(client.getUsername()).append(" ");
        }
        sb.setLength(sb.length()-1);
        String clientsList = sb.toString();
        broadcastMessage(clientsList);
    }
}
