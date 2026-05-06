package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.dto.FlightDto;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PaymentController extends BaseController {

    @FXML
    private Label flightLabel;

    @FXML
    private Label routeLabel;

    @FXML
    private Label seatLabel;

    @FXML
    private Label priceLabel;

    private FlightDto flight;
    private String seat;

    public void setData(FlightDto flight, String seat) {
        this.flight = flight;
        this.seat = seat;
        
        flightLabel.setText(flight.getFlightNumber());
        routeLabel.setText(flight.getRoute());
        seatLabel.setText(seat);
        priceLabel.setText(String.format("%.2f BYN", flight.getBasePrice()));
    }

    @FXML
    private void handlePay() {
        Request request = new Request(CommandType.BOOK_TICKET.name(), 
                ServerConnection.getInstance().getCurrentUser().getId(), 
                flight.getId(), 
                seat);
                
        executeTask(request, response -> {
            showInfo("Успех", "Оплата прошла успешно! Билет забронирован.");
            ((Stage) flightLabel.getScene().getWindow()).close();
        });
    }

    @FXML
    private void handleCancel() {
        // Можно вернуться на выбор места, но для простоты закроем
        ((Stage) flightLabel.getScene().getWindow()).close();
    }
}
