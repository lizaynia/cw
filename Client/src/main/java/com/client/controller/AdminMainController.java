package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.dto.UserDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class AdminMainController extends BaseController {

    @FXML
    private TableView<UserDto> usersTable;

    @FXML
    private TableColumn<UserDto, Long> idColumn;

    @FXML
    private TableColumn<UserDto, String> loginColumn;

    @FXML
    private TableColumn<UserDto, String> roleColumn;

    @FXML
    private TableColumn<UserDto, String> statusColumn;

    private ObservableList<UserDto> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("isBlocked"));

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
        // Открыть диалог выбора роли
    }

    @FXML
    private void handleBlockUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите пользователя");
            return;
        }
        // Отправить запрос на блокировку
    }

}
