package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.dto.CityDto;
import com.common.dto.FlightDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class ClientMainController extends BaseController {

    private static List<ClientMainController> activeControllers = new ArrayList<>();

    @FXML private TableView<FlightDto> catalogTable;
    @FXML private TableColumn<FlightDto, String> numberColumn;
    @FXML private TableColumn<FlightDto, String> routeColumn;
    @FXML private TableColumn<FlightDto, LocalDateTime> departureTimeColumn;
    @FXML private TableColumn<FlightDto, Double> priceColumn;

    @FXML private ComboBox<CityDto> fromComboBox;
    @FXML private ComboBox<CityDto> toComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField flightNumberField;
    @FXML private Slider priceSlider;
    @FXML private Label priceValueLabel;
    @FXML private CheckBox directFlightsOnlyCheckBox;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Label resultsCountLabel;

    private ObservableList<FlightDto> flightsList = FXCollections.observableArrayList();
    private ObservableList<CityDto> citiesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настройка таблицы
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("basePrice"));

        // Форматирование даты и времени
        departureTimeColumn.setCellFactory(column -> new TableCell<FlightDto, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        priceColumn.setCellFactory(column -> new TableCell<FlightDto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f BYN", item));
                }
            }
        });

        catalogTable.setItems(flightsList);

        // Настройка ComboBox для городов
        fromComboBox.setItems(citiesList);
        toComboBox.setItems(citiesList);
        fromComboBox.setPromptText("Город отправления");
        toComboBox.setPromptText("Город прибытия");

        fromComboBox.setEditable(true);
        toComboBox.setEditable(true);

        // StringConverter для fromComboBox
        fromComboBox.setConverter(new StringConverter<CityDto>() {
            @Override
            public String toString(CityDto city) {
                return city == null ? "" : city.getCityName();
            }

            @Override
            public CityDto fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                for (CityDto city : citiesList) {
                    if (city.getCityName().equalsIgnoreCase(string.trim())) {
                        return city;
                    }
                }
                return new CityDto(null, string.trim());
            }
        });

        // StringConverter для toComboBox
        toComboBox.setConverter(new StringConverter<CityDto>() {
            @Override
            public String toString(CityDto city) {
                return city == null ? "" : city.getCityName();
            }

            @Override
            public CityDto fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                for (CityDto city : citiesList) {
                    if (city.getCityName().equalsIgnoreCase(string.trim())) {
                        return city;
                    }
                }
                return new CityDto(null, string.trim());
            }
        });

        // Настройка слайдера цены
        priceSlider.setMin(0);
        priceSlider.setMax(1000);
        priceSlider.setValue(1000);
        priceValueLabel.setText("до 1000 BYN");
        priceSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                priceValueLabel.setText(String.format("до %.0f BYN", newVal))
        );

        // Автопоиск при вводе номера рейса
        flightNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 2) {
                handleSearch();
            } else if (newVal.isEmpty() && oldVal.length() == 1) {
                handleSearch();
            }
        });

        // Автопоиск при изменении других параметров
        fromComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        toComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        directFlightsOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        registerController();
        loadCitiesFromFlights();
    }

    private void registerController() {
        activeControllers.add(this);
        catalogTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                activeControllers.remove(this);
            }
        });
    }

    public static void notifyCitiesChanged() {
        Platform.runLater(() -> {
            for (ClientMainController controller : activeControllers) {
                controller.loadCitiesFromFlights();
            }
        });
    }

    private void loadCitiesFromFlights() {
        Request request = new Request(CommandType.GET_CITIES_FROM_FLIGHTS.name());
        executeTask(request, response -> {
            if (response.isSuccess()) {
                List<CityDto> cities = (List<CityDto>) response.getData();
                CityDto selectedFrom = fromComboBox.getValue();
                CityDto selectedTo = toComboBox.getValue();
                citiesList.setAll(cities);

                if (selectedFrom != null) {
                    CityDto newFrom = cities.stream()
                            .filter(c -> c.getCityName().equalsIgnoreCase(selectedFrom.getCityName()))
                            .findFirst()
                            .orElse(null);
                    fromComboBox.setValue(newFrom);
                }
                if (selectedTo != null) {
                    CityDto newTo = cities.stream()
                            .filter(c -> c.getCityName().equalsIgnoreCase(selectedTo.getCityName()))
                            .findFirst()
                            .orElse(null);
                    toComboBox.setValue(newTo);
                }
                loadAllFlights();
            } else {
                showError("Ошибка", "Не удалось загрузить города: " + response.getMessage());
            }
        });
    }

    private void loadAllFlights() {
        Request request = new Request(CommandType.GET_SCHEDULE.name());
        executeTask(request, response -> {
            List<FlightDto> flights = (List<FlightDto>) response.getData();
            flightsList.setAll(flights);
            updateResultsCount();
        });
    }

    @FXML
    private void handleSearch() {
        String flightNumber = flightNumberField.getText().trim();
        CityDto fromCity = fromComboBox.getValue();
        CityDto toCity = toComboBox.getValue();
        LocalDate date = datePicker.getValue();
        double maxPrice = priceSlider.getValue();
        boolean directOnly = directFlightsOnlyCheckBox.isSelected();

        if (flightNumber.isEmpty() && fromCity == null && toCity == null && date == null && maxPrice >= 999) {
            loadAllFlights();
            return;
        }

        Request request = new Request(CommandType.ADVANCED_SEARCH_FLIGHTS.name(),
                flightNumber,
                fromCity != null ? fromCity.getCityName() : "",
                toCity != null ? toCity.getCityName() : "",
                date,
                maxPrice,
                directOnly);

        executeTask(request, response -> {
            List<FlightDto> flights = (List<FlightDto>) response.getData();
            flightsList.setAll(flights);
            updateResultsCount();

            if (flights.isEmpty()) {
                showInfo("Результаты поиска", "Рейсы не найдены.\n\nПопробуйте изменить параметры поиска.");
            }
        });
    }

    private void updateResultsCount() {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText("Найдено рейсов: " + flightsList.size());
        }
    }

    @FXML
    private void handleResetFilters() {
        flightNumberField.clear();
        fromComboBox.getSelectionModel().clearSelection();
        toComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        priceSlider.setValue(priceSlider.getMax());
        directFlightsOnlyCheckBox.setSelected(false);
        fromComboBox.getEditor().clear();
        toComboBox.getEditor().clear();
        loadAllFlights();
        showInfo("Фильтры сброшены", "Показаны все доступные рейсы");
    }

    @FXML
    private void handleShowAll() {
        handleResetFilters();
    }

    @FXML
    private void handleQuickSearchToday() {
        datePicker.setValue(LocalDate.now());
        handleSearch();
    }

    @FXML
    private void handleQuickSearchTomorrow() {
        datePicker.setValue(LocalDate.now().plusDays(1));
        handleSearch();
    }

    @FXML
    private void handleQuickSearchWeekend() {
        datePicker.setValue(LocalDate.now().plusDays(5));
        handleSearch();
    }

    @FXML
    private void handleSwapCities() {
        CityDto temp = fromComboBox.getValue();
        fromComboBox.setValue(toComboBox.getValue());
        toComboBox.setValue(temp);
        handleSearch();
    }

    @FXML
    private void handleBuyTicket() {
        FlightDto selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Ошибка", "Выберите рейс для покупки.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SeatSelection.fxml"));
            Parent root = loader.load();
            SeatSelectionController controller = loader.getController();
            controller.setFlight(selected);
            Stage stage = new Stage();
            stage.setTitle("Выбор места");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(catalogTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Ошибка", "Не удалось открыть выбор мест: " + e.getMessage());
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
            showError("Ошибка", "Не удалось открыть историю: " + e.getMessage());
        }
    }


    @FXML
    private void handleShowProfile() {
        try {
            // ИСПРАВЛЕНО: правильный путь с /views/ и .fxml расширением
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserProfile.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Личный кабинет");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(catalogTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка открытия профиля: " + e.getMessage());
            e.printStackTrace();
            showError("Ошибка", "Не удалось открыть личный кабинет: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadCitiesFromFlights();
        showInfo("Обновление", "Список рейсов и городов обновлён");
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) catalogTable.getScene().getWindow();
        activeControllers.remove(this);
        switchScene("/views/Login.fxml", "Login", stage);
    }
}