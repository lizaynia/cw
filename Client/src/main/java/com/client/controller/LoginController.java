package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.Response;
import com.common.dto.UserDto;
import com.common.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController extends BaseController {

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

        if (!ValidationUtil.isNotEmpty(login, password)) {
            errorLabel.setText("Заполните все поля!");
            return;
        }

        if (!ValidationUtil.isValidLogin(login)) {
            errorLabel.setText("Логин должен быть от 3 до 20 символов");
            return;
        }

        Request request = new Request(CommandType.LOGIN.name(), login, password);
        
        executeTask(request, response -> {
            UserDto user = (UserDto) response.getData();
            ServerConnection.getInstance().setCurrentUser(user);
            System.out.println("Успешный вход: " + user.getLogin() + " (" + user.getRoleName() + ")");
            
            switchToMainView(user.getRoleName());
        });
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
                showError("Ошибка", "Неизвестная роль: " + roleName);
                return;
        }

        Stage stage = (Stage) loginField.getScene().getWindow();
        switchScene(fxmlFile, roleName, stage);
    }

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (!ValidationUtil.isNotEmpty(login, password)) {
            errorLabel.setStyle("-fx-text-fill: #ff4444;");
            errorLabel.setText("Заполните все поля для регистрации!");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            errorLabel.setStyle("-fx-text-fill: #ff4444;");
            errorLabel.setText("Пароль должен быть не менее 4 символов");
            return;
        }

        Request request = new Request(CommandType.REGISTER.name(), login, password);
        
        executeTask(request, response -> {
            showInfo("Успех", "Регистрация успешна! Теперь вы можете войти.");
            errorLabel.setStyle("-fx-text-fill: #00ff00;");
            errorLabel.setText("Регистрация успешна! Теперь войдите.");
        });
    }
}
