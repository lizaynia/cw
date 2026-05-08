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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
            e.printStackTrace();
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
            e.printStackTrace();
            showError("Ошибка", "Не удалось открыть личный кабинет: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) catalogTable.getScene().getWindow();
        switchScene("/views/Login.fxml", "Login", stage);
    }
}
