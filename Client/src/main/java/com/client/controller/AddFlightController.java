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

public class AddFlightController extends BaseController {

    @FXML private TextField flightNumberField;
    @FXML private TextField departureCityField;
    @FXML private TextField arrivalCityField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<Airplane> airplaneComboBox;
    @FXML private TextField priceField;

    private Runnable onFlightAdded;

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

    private void loadAirplanes() {
        Request request = new Request(CommandType.GET_AIRPLANES.name());
        executeTask(request, response -> {
            List<Airplane> airplanes = (List<Airplane>) response.getData();
            airplaneComboBox.setItems(FXCollections.observableArrayList(airplanes));
        });
    }

    public void setOnFlightAdded(Runnable onFlightAdded) {
        this.onFlightAdded = onFlightAdded;
    }
    @FXML
    private void handleSave() {
        String flightNum = flightNumberField.getText();
        String depCity = departureCityField.getText();
        String arrCity = arrivalCityField.getText();
        Airplane airplane = airplaneComboBox.getValue();

        if (!ValidationUtil.isNotEmpty(flightNum, depCity, arrCity) || datePicker.getValue() == null || airplane == null) {
            showError("Ошибка", "Заполните все поля!");
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeField.getText());
        } catch (Exception e) {
            showError("Ошибка", "Неверный формат времени (HH:mm)");
            return;
        }

        Double price = null;
        if (priceField != null && !priceField.getText().trim().isEmpty()) {
            try {
                price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    showError("Ошибка", "Цена должна быть положительным числом");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Ошибка", "Неверный формат цены");
                return;
            }
        }

        LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), time);

        Request request = new Request(CommandType.ADD_FLIGHT.name(), flightNum, depCity, arrCity, dateTime, airplane.getId(), price);
        executeTask(request, response -> {
            showInfo("Успех", "Рейс успешно добавлен!");
            if (onFlightAdded != null) onFlightAdded.run();
            ((Stage) flightNumberField.getScene().getWindow()).close();
        });
    }

    @FXML
    private void handleCancel() {
        ((Stage) flightNumberField.getScene().getWindow()).close();
    }
}
