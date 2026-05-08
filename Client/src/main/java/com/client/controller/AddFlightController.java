package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.entity.Airplane;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class AddFlightController extends FlightFormController {

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        LocalDateTime dateTime = getDateTimeFromFields();
        Airplane airplane = airplaneComboBox.getValue();
        Double price = getPrice();

        Request request = new Request(CommandType.ADD_FLIGHT.name(),
                flightNumberField.getText(),
                departureCityField.getText(),
                arrivalCityField.getText(),
                dateTime,
                airplane.getId(),
                price);

        executeTask(request, response -> {
            showInfo("Успех", "Рейс успешно добавлен!");
            if (onFlightSaved != null) onFlightSaved.run();
            ((Stage) flightNumberField.getScene().getWindow()).close();
        });
    }

    public void setOnFlightAdded(Runnable callback) {
        this.onFlightSaved = callback;
    }
}