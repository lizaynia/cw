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
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private ObservableList<TicketDto> activeList = FXCollections.observableArrayList();
    private ObservableList<TicketDto> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable(activeTicketsTable, activeFlightColumn, activeRouteColumn, activeSeatColumn, activeStatusColumn);
        setupTable(historyTicketsTable, histFlightColumn, histRouteColumn, null, histStatusColumn);

        activeTicketsTable.setItems(activeList);
        historyTicketsTable.setItems(historyList);

        loadUserData();
        loadTickets();
    }

    private void setupTable(TableView<TicketDto> table, TableColumn<TicketDto, String> flight, 
                            TableColumn<TicketDto, String> route, TableColumn<TicketDto, String> seat, 
                            TableColumn<TicketDto, String> status) {
        flight.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        route.setCellValueFactory(new PropertyValueFactory<>("route"));
        if (seat != null) seat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadUserData() {
        UserDto user = ServerConnection.getInstance().getCurrentUser();
        if (user != null) {
            loginField.setText(user.getLogin());
            // Дополнительные данные можно получить отдельным запросом, если они не в UserDto
        }
    }

    private void loadTickets() {
        Request request = new Request(CommandType.GET_TICKET_HISTORY.name(), 
                ServerConnection.getInstance().getCurrentUser().getId());
        
        executeTask(request, response -> {
            List<TicketDto> allTickets = (List<TicketDto>) response.getData();
            
            // Фильтруем активные и прошлые (упрощенно по статусу)
            activeList.setAll(allTickets.stream()
                .filter(t -> "PAID".equalsIgnoreCase(t.getStatus()) || "BOOKED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList()));
                
            historyList.setAll(allTickets.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) || "CANCELLED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList()));
        });
    }

    @FXML
    private void handleUpdateProfile() {
        // Логика обновления ФИО и Email
        showInfo("Профиль", "Данные профиля обновлены (заглушка)");
    }

    @FXML
    private void handleChangePassword() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        
        if (pass.isEmpty() || !pass.equals(confirm)) {
            showError("Ошибка", "Пароли не совпадают или пусты.");
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
