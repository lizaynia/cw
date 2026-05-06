package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.dto.TicketDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class TicketHistoryController extends BaseController {

    @FXML private TableView<TicketDto> historyTable;
    @FXML private TableColumn<TicketDto, String> flightColumn;
    @FXML private TableColumn<TicketDto, String> routeColumn;
    @FXML private TableColumn<TicketDto, String> seatColumn;
    @FXML private TableColumn<TicketDto, Double> priceColumn;
    @FXML private TableColumn<TicketDto, String> statusColumn;

    private ObservableList<TicketDto> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        flightColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        seatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        historyTable.setItems(historyList);
        loadHistory();
    }

    private void loadHistory() {
        // Мы передаем userId, а ClientHandler на сервере сам найдет passengerId
        Integer userId = ServerConnection.getInstance().getCurrentUser().getId();
        Request request = new Request(CommandType.GET_TICKET_HISTORY.name(), userId);
        
        executeTask(request, response -> {
            List<TicketDto> history = (List<TicketDto>) response.getData();
            historyList.setAll(history);
        });
    }

    @FXML
    private void handleClose() {
        ((Stage) historyTable.getScene().getWindow()).close();
    }
}
