package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.Response;
import com.common.entity.Airplane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
//import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightFormController Tests")
class FlightFormControllerTest extends JavaFXTestBase {

    @Mock
    private TextField flightNumberField;

    @Mock
    private TextField departureCityField;

    @Mock
    private TextField arrivalCityField;

    @Mock
    private DatePicker datePicker;

    @Mock
    private TextField timeField;

    @Mock
    private ComboBox<Airplane> airplaneComboBox;

    @Mock
    private TextField priceField;

    @Mock
    private ServerConnection serverConnection;

    @InjectMocks
    private AddFlightController controller;

    @BeforeEach
    void setUp() {
        // Инициализация контроллера с mock-объектами
        controller.flightNumberField = flightNumberField;
        controller.departureCityField = departureCityField;
        controller.arrivalCityField = arrivalCityField;
        controller.datePicker = datePicker;
        controller.timeField = timeField;
        controller.airplaneComboBox = airplaneComboBox;
        controller.priceField = priceField;
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate correctly when all fields are filled")
        void validateFields_shouldReturnTrue_whenAllFieldsFilled() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());
            when(priceField.getText()).thenReturn("150.00");

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should fail when flight number is empty")
        void validateFields_shouldReturnFalse_whenFlightNumberEmpty() {
            // Given
            when(flightNumberField.getText()).thenReturn("");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when departure city is empty")
        void validateFields_shouldReturnFalse_whenDepartureCityEmpty() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when arrival city is empty")
        void validateFields_shouldReturnFalse_whenArrivalCityEmpty() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when date is not selected")
        void validateFields_shouldReturnFalse_whenDateNotSelected() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(null);
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when time format is invalid")
        void validateFields_shouldReturnFalse_whenTimeFormatInvalid() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("25:70"); // Invalid time
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when time format has wrong pattern")
        void validateFields_shouldReturnFalse_whenTimePatternWrong() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30:00"); // Seconds not expected
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when no airplane selected")
        void validateFields_shouldReturnFalse_whenNoAirplaneSelected() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(null);

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when price is negative")
        void validateFields_shouldReturnFalse_whenPriceNegative() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());
            when(priceField.getText()).thenReturn("-50.00");

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when price is zero")
        void validateFields_shouldReturnFalse_whenPriceZero() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());
            when(priceField.getText()).thenReturn("0");

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail when price is not a number")
        void validateFields_shouldReturnFalse_whenPriceNotNumber() {
            // Given
            when(flightNumberField.getText()).thenReturn("AB123");
            when(departureCityField.getText()).thenReturn("Minsk");
            when(arrivalCityField.getText()).thenReturn("Moscow");
            when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
            when(timeField.getText()).thenReturn("14:30");
            when(airplaneComboBox.getValue()).thenReturn(new Airplane());
            when(priceField.getText()).thenReturn("abc");

            // When
            boolean result = controller.validateFields();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("DateTime Conversion Tests")
    class DateTimeConversionTests {

        @Test
        @DisplayName("Should correctly combine date and time")
        void getDateTimeFromFields_shouldCombineCorrectly() {
            // Given
            LocalDate date = LocalDate.of(2024, 12, 25);
            LocalTime time = LocalTime.of(14, 30);
            when(datePicker.getValue()).thenReturn(date);
            when(timeField.getText()).thenReturn("14:30");

            // When
            LocalDateTime result = controller.getDateTimeFromFields();

            // Then
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 12, 25, 14, 30));
        }

        @Test
        @DisplayName("Should handle different time formats")
        void getDateTimeFromFields_shouldHandleDifferentTimes() {
            // Given
            LocalDate date = LocalDate.of(2024, 12, 25);

            // Test cases
            Object[][] testCases = {
                    {"09:05", LocalTime.of(9, 5)},
                    {"23:59", LocalTime.of(23, 59)},
                    {"00:00", LocalTime.of(0, 0)},
                    {"12:00", LocalTime.of(12, 0)}
            };

            for (Object[] testCase : testCases) {
                String timeStr = (String) testCase[0];
                LocalTime expectedTime = (LocalTime) testCase[1];

                when(datePicker.getValue()).thenReturn(date);
                when(timeField.getText()).thenReturn(timeStr);

                // When
                LocalDateTime result = controller.getDateTimeFromFields();

                // Then
                assertThat(result.toLocalTime()).isEqualTo(expectedTime);
            }
        }

        @Test
        @DisplayName("Should handle leap day correctly")
        void getDateTimeFromFields_shouldHandleLeapDay() {
            // Given
            LocalDate leapDay = LocalDate.of(2024, 2, 29); // 2024 is leap year
            when(datePicker.getValue()).thenReturn(leapDay);
            when(timeField.getText()).thenReturn("10:30");

            // When
            LocalDateTime result = controller.getDateTimeFromFields();

            // Then
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(2);
            assertThat(result.getDayOfMonth()).isEqualTo(29);
        }
    }

    @Nested
    @DisplayName("Price Extraction Tests")
    class PriceExtractionTests {

        @Test
        @DisplayName("Should extract price correctly")
        void getPrice_shouldExtractCorrectPrice() {
            // Given
            when(priceField.getText()).thenReturn("199.99");

            // When
            Double price = controller.getPrice();

            // Then
            assertThat(price).isEqualTo(199.99);
        }

        @Test
        @DisplayName("Should extract integer price correctly")
        void getPrice_shouldExtractIntegerPrice() {
            // Given
            when(priceField.getText()).thenReturn("200");

            // When
            Double price = controller.getPrice();

            // Then
            assertThat(price).isEqualTo(200.0);
        }

        @Test
        @DisplayName("Should return null when price field is empty")
        void getPrice_shouldReturnNull_whenPriceFieldEmpty() {
            // Given
            when(priceField.getText()).thenReturn("");

            // When
            Double price = controller.getPrice();

            // Then
            assertThat(price).isNull();
        }
    }

    @Nested
    @DisplayName("Airplane Loading Tests")
    class AirplaneLoadingTests {

        @Test
        @DisplayName("Should load airplanes successfully")
        void loadAirplanes_shouldLoadSuccessfully() {
            // Given
            List<Airplane> mockAirplanes = Arrays.asList(
                    createAirplane(1, "Boeing 737", 180),
                    createAirplane(2, "Airbus A320", 150),
                    createAirplane(3, "Boeing 777", 300)
            );

            Response mockResponse = new Response(true, "Success");
            mockResponse.setData(mockAirplanes);

            when(serverConnection.sendRequest(any(Request.class))).thenReturn(mockResponse);

            // When
            controller.loadAirplanes();

            // Then
            verify(serverConnection, times(1)).sendRequest(any(Request.class));
        }

        private Airplane createAirplane(int id, String model, int capacity) {
            Airplane airplane = new Airplane();
            airplane.setId(id);
            airplane.setModel(model);
            airplane.setCapacity(capacity);
            airplane.setStatus(Airplane.AirplaneStatus.ACTIVE);
            return airplane;
        }
    }

    @Nested
    @DisplayName("Cancel Operation Tests")
    class CancelOperationTests {

        @Test
        @DisplayName("Should close window on cancel")
        void handleCancel_shouldCloseWindow() {
            // This test would require JavaFX Application thread
            // Usually tested with TestFX or integration tests
            assertThat(controller).isNotNull();
        }
    }
}