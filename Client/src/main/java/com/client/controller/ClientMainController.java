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

        TextInputDialog dialog = new TextInputDialog("A1");
        dialog.setTitle("Выбор места");
        dialog.setHeaderText("Рейс: " + selected.getFlightNumber());
        dialog.setContentText("Введите номер места (например, A1, B10):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String seat = result.get().trim();
            if (seat.isEmpty()) {
                showError("Ошибка", "Номер места не может быть пустым.");
                return;
            }

            Request request = new Request(CommandType.BOOK_TICKET.name(), 
                    ServerConnection.getInstance().getCurrentUser().getId(), 
                    selected.getId(), 
                    seat);
                    
            executeTask(request, response -> {
                showInfo("Успех", "Билет на место " + seat + " успешно куплен!");
                loadFlights();
            });
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
    private void handleLogout() {
        Stage stage = (Stage) catalogTable.getScene().getWindow();
        switchScene("/views/Login.fxml", "Login", stage);
    }
}
