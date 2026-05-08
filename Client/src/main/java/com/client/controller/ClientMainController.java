package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.dto.FlightDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
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

    // ✅ ДОБАВЛЕНЫ ПОЛЯ ДЛЯ ПОИСКА
    @FXML
    private TextField fromField;

    @FXML
    private TextField toField;

    @FXML
    private DatePicker datePicker;

    private ObservableList<FlightDto> flightsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("basePrice"));

        catalogTable.setItems(flightsList);

        // Устанавливаем дату по умолчанию - сегодня
        datePicker.setValue(LocalDate.now());

        // Загружаем все рейсы при старте
        loadFlights();
    }

    private void loadFlights() {
        Request request = new Request(CommandType.GET_SCHEDULE.name());
        executeTask(request, response -> {
            List<FlightDto> flights = (List<FlightDto>) response.getData();
            flightsList.setAll(flights);
        });
    }

    // ✅ НОВЫЙ МЕТОД ДЛЯ ПОИСКА
    @FXML
    private void handleSearch() {
        String from = fromField.getText();
        String to = toField.getText();
        LocalDate date = datePicker.getValue();

        if ((from == null || from.trim().isEmpty()) &&
                (to == null || to.trim().isEmpty()) &&
                date == null) {
            // Если все поля пустые - показываем все рейсы
            loadFlights();
            return;
        }

        Request request = new Request(CommandType.SEARCH_FLIGHTS.name(),
                from != null ? from.trim() : "",
                to != null ? to.trim() : "",
                date != null ? date : LocalDate.now());

        executeTask(request, response -> {
            List<FlightDto> flights = (List<FlightDto>) response.getData();
            flightsList.setAll(flights);
            if (flights.isEmpty()) {
                showInfo("Результаты поиска", "Рейсы не найдены");
            }
        });
    }

    @FXML
    private void handleBuyTicket() {
        FlightDto selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите рейс для покупки.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SeatSelection.fxml"));
            Parent root = loader.load();

            SeatSelectionController controller = loader.getController();
            controller.setFlight(selected);

            Stage stage = new Stage();
            stage.setTitle("Выбор места");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(catalogTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Ошибка", "Не удалось открыть выбор мест");
        }
    }

    @FXML
    private void handleShowHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TicketHistory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("История билетов");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(catalogTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка открытия истории: " + e.getMessage());
            showError("Ошибка", "Не удалось открыть историю: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserProfile.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Личный кабинет");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(catalogTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка открытия профиля: " + e.getMessage());
            showError("Ошибка", "Не удалось открыть личный кабинет: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) catalogTable.getScene().getWindow();
        switchScene("/views/Login.fxml", "Login", stage);
    }
}