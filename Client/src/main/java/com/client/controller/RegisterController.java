package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController extends BaseController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField passportField;
    @FXML private Label errorLabel;

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String passport = passportField.getText();

        if (!ValidationUtil.isNotEmpty(login, password, firstName, lastName, passport)) {
            errorLabel.setText("Заполните все поля!");
            return;
        }

        if (!ValidationUtil.isValidLogin(login)) {
            errorLabel.setText("Логин должен быть от 3 до 20 символов");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            errorLabel.setText("Пароль должен быть не менее 4 символов");
            return;
        }
        
        if (!ValidationUtil.isValidPassport(passport)) {
            errorLabel.setText("Неверный формат паспорта (7-15 символов, буквы/цифры)");
            return;
        }

        Request request = new Request(CommandType.REGISTER.name(), login, password, firstName, lastName, passport);
        
        executeTask(request, response -> {
            showInfo("Успех", "Регистрация успешна! Теперь вы можете войти.");
            handleBack();
        });
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) loginField.getScene().getWindow();
        switchScene("/views/Login.fxml", "Aero System - Login", stage);
    }
}
