package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.entity.Airplane;
import com.common.utils.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public abstract class FlightFormController extends BaseController {

    @FXML protected TextField flightNumberField;
    @FXML protected TextField departureCityField;
    @FXML protected TextField arrivalCityField;
    @FXML protected DatePicker datePicker;
    @FXML protected TextField timeField;
    @FXML protected ComboBox<Airplane> airplaneComboBox;
    @FXML protected TextField priceField;

    protected Runnable onFlightSaved;

    @FXML
    public void initialize() {
        loadAirplanes();

        airplaneComboBox.setConverter(new StringConverter<Airplane>() {
            @Override
            public String toString(Airplane airplane) {
                return airplane == null ? "" : airplane.getModel() + " (ID: " + airplane.getId() + ")";
            }

            @Override
            public Airplane fromString(String string) {
                return null;
            }
        });
    }

    protected void loadAirplanes() {
        Request request = new Request(CommandType.GET_AIRPLANES.name());
        executeTask(request, response -> {
            List<Airplane> airplanes = (List<Airplane>) response.getData();
            airplaneComboBox.setItems(FXCollections.observableArrayList(airplanes));
            onAirplanesLoaded(airplanes);
        });
    }

    protected void onAirplanesLoaded(List<Airplane> airplanes) {
        // Переопределяется в наследниках
    }

    protected LocalDateTime getDateTimeFromFields() {
        LocalTime time = LocalTime.parse(timeField.getText());
        return LocalDateTime.of(datePicker.getValue(), time);
    }

    protected boolean validateFields() {
        String flightNum = flightNumberField.getText();
        String depCity = departureCityField.getText();
        String arrCity = arrivalCityField.getText();
        Airplane airplane = airplaneComboBox.getValue();

        if (!ValidationUtil.isNotEmpty(flightNum, depCity, arrCity) || datePicker.getValue() == null || airplane == null) {
            showError("Ошибка", "Заполните все поля!");
            return false;
        }

        try {
            LocalTime.parse(timeField.getText());
        } catch (Exception e) {
            showError("Ошибка", "Неверный формат времени (HH:mm)");
            return false;
        }

        if (priceField != null && !priceField.getText().trim().isEmpty()) {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    showError("Ошибка", "Цена должна быть положительным числом");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Ошибка", "Неверный формат цены");
                return false;
            }
        }

        return true;
    }

    protected Double getPrice() {
        if (priceField != null && !priceField.getText().trim().isEmpty()) {
            return Double.parseDouble(priceField.getText());
        }
        return null;
    }

    public void setOnFlightSaved(Runnable onFlightSaved) {
        this.onFlightSaved = onFlightSaved;
    }

    @FXML
    protected void handleCancel() {
        ((Stage) flightNumberField.getScene().getWindow()).close();
    }
}