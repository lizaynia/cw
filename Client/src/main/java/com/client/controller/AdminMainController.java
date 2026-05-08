package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.dto.UserDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AdminMainController extends BaseController {

    @FXML
    private TableView<UserDto> usersTable;

    @FXML
    private TableColumn<UserDto, Integer> idColumn;

    @FXML
    private TableColumn<UserDto, String> loginColumn;

    @FXML
    private TableColumn<UserDto, String> roleColumn;

    @FXML
    private TableColumn<UserDto, Boolean> statusColumn;

    private ObservableList<UserDto> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));

        usersTable.setItems(usersList);
        loadUsers();
    }

    private void loadUsers() {
        Request request = new Request(CommandType.GET_USERS.name());
        executeTask(request, response -> {
            List<UserDto> users = (List<UserDto>) response.getData();
            usersList.setAll(users);
        });
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) usersTable.getScene().getWindow();
        switchScene("/views/Login.fxml", "Login", stage);
    }

    @FXML
    private void handleChangeRole() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите пользователя");
            return;
        }

        // Создаем диалог выбора роли
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getRoleName(), "CLIENT", "DISPATCHER", "ADMIN");
        dialog.setTitle("Смена роли");
        dialog.setHeaderText("Смена роли для пользователя: " + selected.getLogin());
        dialog.setContentText("Выберите новую роль:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            if (newRole.equals(selected.getRoleName())) {
                return;
            }

            Request request = new Request(CommandType.CHANGE_ROLE.name(), selected.getId(), newRole);
            executeTask(request, response -> {
                showInfo("Успех", "Роль пользователя " + selected.getLogin() + " изменена на " + newRole);
                loadUsers();
            });
        });
    }

    @FXML
    private void handleBlockUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите пользователя");
            return;
        }

        String action = selected.isBlocked() ? "разблокировать" : "заблокировать";

        if (showConfirmation("Подтверждение", "Вы уверены, что хотите " + action + " пользователя " + selected.getLogin() + "?")) {
            boolean block = !selected.isBlocked();
            Request request = new Request(CommandType.BLOCK_USER.name(), selected.getId(), block);
            executeTask(request, response -> {
                showInfo("Успех", "Пользователь " + selected.getLogin() + (block ? " заблокирован" : " разблокирован"));
                loadUsers();
            });
        }
    }
}