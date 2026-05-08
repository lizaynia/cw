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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatSelectionController extends BaseController {

    @FXML
    private Label flightInfoLabel;

    @FXML
    private GridPane leftSeatsGrid;

    @FXML
    private GridPane rightSeatsGrid;

    @FXML
    private Label selectedSeatLabel;

    @FXML
    private Button bookButton;

    private FlightDto flight;
    private String selectedSeat = null;
    private Set<String> occupiedSeats = new HashSet<>();
    private Set<VBox> seatBoxes = new HashSet<>(); // Для отслеживания всех созданных кнопок мест

    private static final int ROWS = 30;
    private static final String[] LEFT_COLUMNS = {"A", "B", "C"};
    private static final String[] RIGHT_COLUMNS = {"D", "E", "F"};

    public void setFlight(FlightDto flight) {
        this.flight = flight;
        int availableSeats = flight.getAvailableSeats() != null ? flight.getAvailableSeats() : 180;
        flightInfoLabel.setText("Рейс: " + flight.getFlightNumber() + " | " + flight.getRoute() +
                " | Свободно мест: " + availableSeats);
        loadOccupiedSeats();
    }

    private void loadOccupiedSeats() {
        Request request = new Request(CommandType.GET_OCCUPIED_SEATS.name(), flight.getId());
        executeTask(request, response -> {
            if (response.isSuccess() && response.getData() != null) {
                List<String> seats = (List<String>) response.getData();
                occupiedSeats.clear();
                occupiedSeats.addAll(seats);
                Platform.runLater(this::renderSeats);
            } else {
                Platform.runLater(this::renderSeats);
            }
        });
    }

    private void renderSeats() {
        seatBoxes.clear();
        leftSeatsGrid.getChildren().clear();
        rightSeatsGrid.getChildren().clear();

        // Заголовки колонок для левой стороны
        for (int i = 0; i < LEFT_COLUMNS.length; i++) {
            Label header = new Label(LEFT_COLUMNS[i]);
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #666; -fx-padding: 5;");
            leftSeatsGrid.add(header, i, 0);
        }

        // Заголовки колонок для правой стороны
        for (int i = 0; i < RIGHT_COLUMNS.length; i++) {
            Label header = new Label(RIGHT_COLUMNS[i]);
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #666; -fx-padding: 5;");
            rightSeatsGrid.add(header, i, 0);
        }

        // Рисуем места (ряды 1-30)
        for (int row = 1; row <= ROWS; row++) {
            for (int col = 0; col < LEFT_COLUMNS.length; col++) {
                String seatNum = LEFT_COLUMNS[col] + row;
                VBox seatBox = createSeatBox(seatNum);
                seatBoxes.add(seatBox);
                leftSeatsGrid.add(seatBox, col, row);
            }

            for (int col = 0; col < RIGHT_COLUMNS.length; col++) {
                String seatNum = RIGHT_COLUMNS[col] + row;
                VBox seatBox = createSeatBox(seatNum);
                seatBoxes.add(seatBox);
                rightSeatsGrid.add(seatBox, col, row);
            }
        }
    }

    private VBox createSeatBox(String seatNum) {
        VBox seatBox = new VBox(2);
        seatBox.setAlignment(javafx.geometry.Pos.CENTER);
        seatBox.setPrefWidth(55);
        seatBox.setPrefHeight(55);
        seatBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1; -fx-border-color: #ccc;");
        seatBox.setUserData(seatNum); // Сохраняем номер места в userData

        Text seatText = new Text(seatNum);
        seatText.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        boolean isOccupied = occupiedSeats.contains(seatNum);

        if (isOccupied) {
            seatBox.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1; -fx-border-color: #bbb;");
            seatText.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-fill: #999;");
            Text occupiedText = new Text("ЗАНЯТО");
            occupiedText.setStyle("-fx-font-size: 8; -fx-fill: #999;");
            seatBox.getChildren().addAll(seatText, occupiedText);
        } else {
            seatBox.setStyle("-fx-background-color: #f0f9f0; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1; -fx-border-color: #4caf50;");
            seatBox.setCursor(javafx.scene.Cursor.HAND);
            seatText.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-fill: #4caf50;");
            Text freeText = new Text("СВОБОДНО");
            freeText.setStyle("-fx-font-size: 8; -fx-fill: #4caf50;");
            seatBox.getChildren().addAll(seatText, freeText);

            seatBox.setOnMouseClicked(e -> selectSeat(seatNum, seatBox));

            seatBox.setOnMouseEntered(e -> {
                if (selectedSeat == null || !seatNum.equals(selectedSeat)) {
                    seatBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-border-color: #4caf50;");
                }
            });
            seatBox.setOnMouseExited(e -> {
                if (selectedSeat == null || !seatNum.equals(selectedSeat)) {
                    seatBox.setStyle("-fx-background-color: #f0f9f0; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1; -fx-border-color: #4caf50;");
                }
            });
        }

        return seatBox;
    }

    private void selectSeat(String seatNum, VBox seatBox) {
        resetSelection();
        selectedSeat = seatNum;

        // Стиль для выбранного места
        seatBox.setStyle("-fx-background-color: #d63384; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 2; -fx-border-color: #c12a75;");

        if (seatBox.getChildren().size() > 0) {
            Text seatText = (Text) seatBox.getChildren().get(0);
            seatText.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-fill: white;");
        }
        if (seatBox.getChildren().size() > 1) {
            Text statusText = (Text) seatBox.getChildren().get(1);
            statusText.setStyle("-fx-font-size: 8; -fx-fill: white;");
            statusText.setText("ВЫБРАНО");
        }

        selectedSeatLabel.setText("Выбрано место: " + selectedSeat);
        bookButton.setDisable(false);
    }

    private void resetSelection() {
        for (VBox box : seatBoxes) {
            String seatNum = (String) box.getUserData();
            if (seatNum == null) continue;

            if (!occupiedSeats.contains(seatNum) && (selectedSeat == null || !seatNum.equals(selectedSeat))) {
                box.setStyle("-fx-background-color: #f0f9f0; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1; -fx-border-color: #4caf50;");

                if (box.getChildren().size() > 0) {
                    Text seatText = (Text) box.getChildren().get(0);
                    seatText.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-fill: #4caf50;");
                }
                if (box.getChildren().size() > 1) {
                    Text statusText = (Text) box.getChildren().get(1);
                    statusText.setText("СВОБОДНО");
                    statusText.setStyle("-fx-font-size: 8; -fx-fill: #4caf50;");
                }
            }
        }
    }

    @FXML
    private void handleProceedToPayment() {
        if (selectedSeat == null) {
            showError("Ошибка", "Выберите место!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Payment.fxml"));
            Parent root = loader.load();

            PaymentController controller = loader.getController();
            controller.setData(flight, selectedSeat);

            Stage stage = (Stage) bookButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Ошибка загрузки экрана оплаты: " + e.getMessage());
            showError("Ошибка", "Не удалось загрузить экран оплаты.");
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) bookButton.getScene().getWindow()).close();
    }
}