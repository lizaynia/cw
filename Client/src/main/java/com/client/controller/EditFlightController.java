package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.dto.FlightDto;
import com.common.entity.Airplane;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class EditFlightController extends FlightFormController {

    private FlightDto originalFlight;

    public void setFlight(FlightDto flight) {
        this.originalFlight = flight;

        flightNumberField.setText(flight.getFlightNumber());
        departureCityField.setText(flight.getDepartureCity());
        arrivalCityField.setText(flight.getArrivalCity());
        datePicker.setValue(flight.getDepartureTime().toLocalDate());
        timeField.setText(flight.getDepartureTime().toLocalTime().toString());
        priceField.setText(String.valueOf(flight.getBasePrice()));
    }

    @Override
    protected void onAirplanesLoaded(List<Airplane> airplanes) {
        if (originalFlight != null && originalFlight.getAirplaneModel() != null) {
            for (Airplane airplane : airplanes) {
                if (airplane.getModel().equals(originalFlight.getAirplaneModel())) {
                    airplaneComboBox.getSelectionModel().select(airplane);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        LocalDateTime dateTime = getDateTimeFromFields();
        Airplane airplane = airplaneComboBox.getValue();
        Double price = getPrice();

        Request request = new Request(CommandType.UPDATE_FLIGHT.name(),
                originalFlight.getId(),
                flightNumberField.getText(),
                departureCityField.getText(),
                arrivalCityField.getText(),
                dateTime,
                airplane.getId(),
                price);

        executeTask(request, response -> {
            showInfo("Успех", "Рейс успешно обновлен!");
            if (onFlightSaved != null) onFlightSaved.run();
            ((Stage) flightNumberField.getScene().getWindow()).close();
        });
    }

    public void setOnFlightUpdated(Runnable callback) {
        this.onFlightSaved = callback;
    }
}