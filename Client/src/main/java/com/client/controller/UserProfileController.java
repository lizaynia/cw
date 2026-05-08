package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.dto.TicketDto;
import com.common.dto.UserDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class UserProfileController extends BaseController {

    @FXML private TableView<TicketDto> activeTicketsTable;
    @FXML private TableColumn<TicketDto, String> activeFlightColumn;
    @FXML private TableColumn<TicketDto, String> activeRouteColumn;
    @FXML private TableColumn<TicketDto, String> activeSeatColumn;
    @FXML private TableColumn<TicketDto, String> activeStatusColumn;

    @FXML private TableView<TicketDto> historyTicketsTable;
    @FXML private TableColumn<TicketDto, String> histFlightColumn;
    @FXML private TableColumn<TicketDto, String> histRouteColumn;
    @FXML private TableColumn<TicketDto, String> histDateColumn;
    @FXML private TableColumn<TicketDto, String> histStatusColumn;

    @FXML private TextField loginField;
    @FXML private TextField fullNameField;
    @FXML private TextField passportField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Эти поля НЕ используются в FXML, убираем их или делаем опциональными
    // @FXML private Label fullNameLabel;
    // @FXML private Label passportLabel;

    private ObservableList<TicketDto> activeList = FXCollections.observableArrayList();
    private ObservableList<TicketDto> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настройка колонок активных билетов
        activeFlightColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        activeRouteColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        activeSeatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        activeStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Настройка колонок истории
        histFlightColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        histRouteColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        histDateColumn.setCellValueFactory(new PropertyValueFactory<>("flightDate"));
        histStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        activeTicketsTable.setItems(activeList);
        historyTicketsTable.setItems(historyList);

        loadUserData();
        loadTickets();
    }

    private void loadUserData() {
        UserDto user = ServerConnection.getInstance().getCurrentUser();
        if (user != null) {
            loginField.setText(user.getLogin());

            // Заполняем ФИО
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                fullNameField.setText(user.getFullName());
            } else {
                fullNameField.setText("Не указано");
            }

            // Заполняем паспорт
            if (user.getPassportNumber() != null && !user.getPassportNumber().isEmpty()) {
                passportField.setText(user.getPassportNumber());
            } else {
                passportField.setText("Не указан");
            }
        }
    }

    private void loadTickets() {
        Request request = new Request(CommandType.GET_TICKET_HISTORY.name(),
                ServerConnection.getInstance().getCurrentUser().getId());

        executeTask(request, response -> {
            List<TicketDto> allTickets = (List<TicketDto>) response.getData();

            // Активные билеты (PAID или BOOKED)
            activeList.setAll(allTickets.stream()
                    .filter(t -> "PAID".equalsIgnoreCase(t.getStatus()) || "BOOKED".equalsIgnoreCase(t.getStatus()))
                    .collect(Collectors.toList()));

            // История (COMPLETED или CANCELLED)
            historyList.setAll(allTickets.stream()
                    .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) || "CANCELLED".equalsIgnoreCase(t.getStatus()))
                    .collect(Collectors.toList()));
        });
    }

    @FXML
    private void handleUpdateProfile() {
        String newFullName = fullNameField.getText();
        String newPassport = passportField.getText();

        if (newFullName == null || newFullName.trim().isEmpty()) {
            showError("Ошибка", "Имя не может быть пустым");
            return;
        }

        // Отправляем запрос на обновление профиля
        Request request = new Request(CommandType.UPDATE_PROFILE_INFO.name(),
                ServerConnection.getInstance().getCurrentUser().getId(),
                newFullName,
                newPassport);

        executeTask(request, response -> {
            if (response.isSuccess()) {
                showInfo("Успех", "Профиль успешно обновлён!");
                // Обновляем данные в текущем объекте пользователя
                UserDto currentUser = ServerConnection.getInstance().getCurrentUser();
                currentUser.setFullName(newFullName);
                currentUser.setPassportNumber(newPassport);
                loadUserData(); // Обновляем отображение
            } else {
                showError("Ошибка", response.getMessage());
            }
        });
    }

    @FXML
    private void handleChangePassword() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (pass.isEmpty() || !pass.equals(confirm)) {
            showError("Ошибка", "Пароли не совпадают или пусты.");
            return;
        }

        if (pass.length() < 4) {
            showError("Ошибка", "Пароль должен быть не менее 4 символов");
            return;
        }

        Request request = new Request(CommandType.UPDATE_PROFILE.name(),
                ServerConnection.getInstance().getCurrentUser().getId(),
                pass);

        executeTask(request, response -> {
            showInfo("Успех", "Пароль успешно изменён.");
            newPasswordField.clear();
            confirmPasswordField.clear();
        });
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) loginField.getScene().getWindow();
        switchScene("/views/ClientMain.fxml", "CLIENT", stage);
    }
}