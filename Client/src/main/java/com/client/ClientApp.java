package com.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            Scene scene = new Scene(root, 400, 500);

            primaryStage.setTitle("Airport System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        ServerConnection.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
