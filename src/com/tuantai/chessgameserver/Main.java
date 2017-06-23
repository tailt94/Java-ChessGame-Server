package com.tuantai.chessgameserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application implements Server.OnConnectionEstablishListener{
    Stage primaryStage = null;
    Server server = new Server();
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        server.setOnConnectionEstablishListener(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("loading_screen.fxml"));

        Parent root = loader.load();
        primaryStage.setTitle("Chess Game - Server");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        });
        t.start();
    }

    @Override
    public void onConnectionEstablish() {
        try {
            startMainScreen(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startMainScreen(Stage stage) throws Exception {
        MainController controller = new MainController(server);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_screen.fxml"));
        loader.setController(controller);

        Parent root = loader.load();
        stage.getScene().setRoot(root);
        stage.show();
        stage.sizeToScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
