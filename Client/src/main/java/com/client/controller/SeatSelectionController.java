package com.client.controller;

import com.common.CommandType;
import com.common.Request;
import com.common.dto.FlightDto;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatSelectionController extends BaseController {

    @FXML
    private Label flightInfoLabel;

    @FXML
    private GridPane seatGrid;

    @FXML
    private Label selectedSeatLabel;

    @FXML
    private Button bookButton;

    private FlightDto flight;
    private String selectedSeat = null;
    private Set<String> occupiedSeats = new HashSet<>();

    public void setFlight(FlightDto flight) {
        this.flight = flight;
        flightInfoLabel.setText("Рейс: " + flight.getFlightNumber() + " | " + flight.getRoute());
        loadOccupiedSeats();
    }

    private void loadOccupiedSeats() {
        Request request = new Request(CommandType.GET_OCCUPIED_SEATS.name(), flight.getId());
        executeTask(request, response -> {
            List<String> seats = (List<String>) response.getData();
            occupiedSeats.addAll(seats);
            Platform.runLater(this::renderSeats);
        });
    }

    private void renderSeats() {
        seatGrid.getChildren().clear();
        
        // Рисуем сетку 6x10 (A-F, 1-10) для примера
        String[] rows = {"A", "B", "C", "D", "E", "F"};
        for (int r = 0; r < rows.length; r++) {
            for (int c = 1; c <= 10; c++) {
                String seatNum = rows[r] + c;
                
                StackPane seatPane = new StackPane();
                seatPane.getStyleClass().add("seat");
                
                if (occupiedSeats.contains(seatNum)) {
                    seatPane.getStyleClass().add("seat-occupied");
                } else {
                    seatPane.getStyleClass().add("seat-free");
                    seatPane.setOnMouseClicked(e -> selectSeat(seatNum, seatPane));
                }
                
                Label lbl = new Label(seatNum);
                seatPane.getChildren().add(lbl);
                
                seatGrid.add(seatPane, c, r);
            }
            // Проход между C и D
            if (r == 2) {
                Region spacer = new Region();
                spacer.setPrefHeight(20);
                seatGrid.add(spacer, 0, r + 1, 11, 1);
            }
        }
    }

    private void selectSeat(String seatNum, StackPane pane) {
        // Сброс предыдущего выбора
        seatGrid.getChildren().forEach(node -> {
            if (node instanceof StackPane) {
                node.getStyleClass().remove("seat-selected");
                if (!occupiedSeats.contains(((Label)((StackPane)node).getChildren().get(0)).getText())) {
                    if (!node.getStyleClass().contains("seat-free")) {
                         node.getStyleClass().add("seat-free");
                    }
                }
            }
        });

        selectedSeat = seatNum;
        pane.getStyleClass().remove("seat-free");
        pane.getStyleClass().add("seat-selected");
        
        selectedSeatLabel.setText("Выбрано место: " + selectedSeat);
        bookButton.setDisable(false);
    }

    @FXML
    private void handleProceedToPayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Payment.fxml"));
            Parent root = loader.load();
            
            PaymentController controller = loader.getController();
            controller.setData(flight, selectedSeat);
            
            Stage stage = (Stage) seatGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка", "Не удалось загрузить экран оплаты.");
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) seatGrid.getScene().getWindow()).close();
    }
}
