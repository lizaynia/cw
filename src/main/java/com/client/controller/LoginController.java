package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.Response;
import com.common.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
            User user = (User) response.getData();
            ServerConnection.getInstance().setCurrentUser(user);
            System.out.println("Успешный вход: " + user.getLogin() + " (" + user.getRole().getName() + ")");
            // Здесь будет переход на главное окно в зависимости от роли
        } else {
            errorLabel.setText(response.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
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
