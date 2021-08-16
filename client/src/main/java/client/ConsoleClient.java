package client;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleClient {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Network network = new Network();
        network.connect(8189);

        network.setOnMessageReceivedCallback(new Callback() {
            @Override
            public void callback(Object... args) {
                String message = (String) args[0];
                System.out.println(message);
            }
        });

        System.out.println("ВВедите /login login password");
        String loginLine = sc.nextLine();
        String login = loginLine.split("\\s+")[1];
        String password = loginLine.split("\\s+")[2];
        network.tryToLogin(login, password);

        while (true) {
            String message = sc.nextLine();
            network.sendMessage(message);
        }
    }
}
