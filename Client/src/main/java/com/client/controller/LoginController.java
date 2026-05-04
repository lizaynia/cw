package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.Response;
import com.common.dto.UserDto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните все поля!");
            return;
        }

        Request request = new Request(CommandType.LOGIN.name(), login, password);
        Response response = ServerConnection.getInstance().sendRequest(request);

        if (response.isSuccess()) {
            UserDto user = (UserDto) response.getData();
            ServerConnection.getInstance().setCurrentUser(user);
            System.out.println("Успешный вход: " + user.getLogin() + " (" + user.getRoleName() + ")");
            
            switchToMainView(user.getRoleName());
        } else {
            errorLabel.setText(response.getMessage());
        }
    }

    private void switchToMainView(String roleName) {
        String fxmlFile = "";
        switch (roleName) {
            case "ADMIN":
                fxmlFile = "/views/AdminMain.fxml";
                break;
            case "DISPATCHER":
                fxmlFile = "/views/DispatcherMain.fxml";
                break;
            case "CLIENT":
                fxmlFile = "/views/ClientMain.fxml";
                break;
            default:
                errorLabel.setText("Неизвестная роль: " + roleName);
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) loginField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Airport System - " + roleName);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки интерфейса: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #ff4444;");
            errorLabel.setText("Заполните все поля для регистрации!");
            return;
        }

        Request request = new Request(CommandType.REGISTER.name(), login, password);
        Response response = ServerConnection.getInstance().sendRequest(request);

        if (response.isSuccess()) {
            errorLabel.setStyle("-fx-text-fill: #00ff00;");
            errorLabel.setText("Регистрация успешна! Теперь войдите.");
        } else {
            errorLabel.setStyle("-fx-text-fill: #ff4444;");
            errorLabel.setText(response.getMessage());
        }
    }
}
