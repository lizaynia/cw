package com.client.controller;

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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DispatcherMainController extends BaseController {

    @FXML
    private TableView<FlightDto> flightsTable;

    @FXML
    private TableColumn<FlightDto, String> flightNumberColumn;

    @FXML
    private TableColumn<FlightDto, String> departureColumn;

    @FXML
    private TableColumn<FlightDto, String> destinationColumn;

    @FXML
    private TableColumn<FlightDto, String> timeColumn;

    private ObservableList<FlightDto> flightsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureCity"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalCity"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        flightsTable.setItems(flightsList);
        loadFlights();
    }

    @FXML
    private void handleEditFlight() {
        FlightDto selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите рейс для редактирования.");
            return;
        }
        // Открыть окно редактирования (реализуйте по аналогии с AddFlight)
    }

    private void loadFlights() {
        Request request = new Request(CommandType.GET_SCHEDULE.name());
        executeTask(request, response -> {
            List<FlightDto> flights = (List<FlightDto>) response.getData();
            flightsList.setAll(flights);
        });
    }

    @FXML
    private void handleAddFlight() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddFlight.fxml"));
            Parent root = loader.load();
            
            AddFlightController controller = loader.getController();
            controller.setOnFlightAdded(this::loadFlights);
            
            Stage stage = new Stage();
            stage.setTitle("Aero System - Добавить рейс");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(flightsTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка загрузки", "Не удалось открыть окно добавления рейса: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteFlight() {
        FlightDto selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите рейс для удаления.");
            return;
        }

        if (showConfirmation("Удаление рейса", "Вы уверены, что хотите удалить рейс " + selected.getFlightNumber() + "?")) {
            Request request = new Request(CommandType.DELETE_FLIGHT.name(), selected.getId());
            executeTask(request, response -> {
                showInfo("Успех", "Рейс удален.");
                loadFlights();
            });
        }
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) flightsTable.getScene().getWindow();
        switchScene("/views/Login.fxml", "Login", stage);
    }
}
