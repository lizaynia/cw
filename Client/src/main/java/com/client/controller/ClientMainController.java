package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.dto.FlightDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class ClientMainController extends BaseController {

    @FXML
    private TableView<FlightDto> catalogTable;

    @FXML
    private TableColumn<FlightDto, String> numberColumn;

    @FXML
    private TableColumn<FlightDto, String> routeColumn;

    @FXML
    private TableColumn<FlightDto, Double> priceColumn;

    private ObservableList<FlightDto> flightsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        // В FlightDto может быть геттер getRoute() или комбинированный
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route")); 
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("basePrice"));

        catalogTable.setItems(flightsList);
        loadFlights();
    }

    private void loadFlights() {
        Request request = new Request(CommandType.GET_SCHEDULE.name());
        executeTask(request, response -> {
            List<FlightDto> flights = (List<FlightDto>) response.getData();
            flightsList.setAll(flights);
        });
    }

    @FXML
    private void handleBuyTicket() {
        FlightDto selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите рейс для покупки.");
            return;
        }

        if (showConfirmation("Покупка билета", "Вы уверены, что хотите купить билет на рейс " + selected.getFlightNumber() + "?")) {
            Request request = new Request(CommandType.BOOK_TICKET.name(), selected.getId(), ServerConnection.getInstance().getCurrentUser().getId());
            executeTask(request, response -> {
                showInfo("Успех", "Билет успешно куплен!");
                loadFlights(); // Обновить данные (например, кол-во мест)
            });
        }
    }

    @FXML
    private void handleShowHistory() {
        showInfo("История", "Функционал просмотра истории билетов в разработке.");
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) catalogTable.getScene().getWindow();
        switchScene("/views/Login.fxml", "Login", stage);
    }
}
