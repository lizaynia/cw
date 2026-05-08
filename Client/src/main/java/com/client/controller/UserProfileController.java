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
    @FXML private Label fullNameLabel;        // ✅ Label, а не TextField
    @FXML private Label passportLabel;        // ✅ Label
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private ObservableList<TicketDto> activeList = FXCollections.observableArrayList();
    private ObservableList<TicketDto> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        activeFlightColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        activeRouteColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        activeSeatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        activeStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

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
            fullNameLabel.setText(user.getFullName() != null ? user.getFullName() : "Не указано");
            passportLabel.setText(user.getPassportNumber() != null ? user.getPassportNumber() : "Не указан");
        }
    }

    private void loadTickets() {
        Request request = new Request(CommandType.GET_TICKET_HISTORY.name(),
                ServerConnection.getInstance().getCurrentUser().getId());

        executeTask(request, response -> {
            List<TicketDto> allTickets = (List<TicketDto>) response.getData();

            activeList.setAll(allTickets.stream()
                    .filter(t -> "PAID".equalsIgnoreCase(t.getStatus()) || "BOOKED".equalsIgnoreCase(t.getStatus()))
                    .collect(Collectors.toList()));

            historyList.setAll(allTickets.stream()
                    .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) || "CANCELLED".equalsIgnoreCase(t.getStatus()))
                    .collect(Collectors.toList()));
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
            showInfo("Успех", "Пароль успешно изменен.");
            newPasswordField.clear();
            confirmPasswordField.clear();
        });
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) loginField.getScene().getWindow();
        switchScene("/views/ClientMain.fxml", "Airport Management", stage);
    }
}