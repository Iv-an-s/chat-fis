package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        //  System.out.println(System.getenv().get("USERNAME")); // getenv - получаем переменные среды (мапа)
        launch(args); // начиная с launch  все оборачивается в поток JavaFX
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/window.fxml"));
        primaryStage.setTitle("Fisunov chat");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
}
