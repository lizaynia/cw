package com.server.service;

import com.common.entity.*;
import com.server.dao.AirplaneDao;
import com.server.dao.CityDao;
import com.server.dao.FlightDao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DispatcherService Tests")
class DispatcherServiceTest {

    @Mock
    private FlightDao flightDao;

    @Mock
    private AirplaneDao airplaneDao;

    @Mock
    private CityDao cityDao;

    private DispatcherService dispatcherService;

    private Airplane testAirplane;
    private City minsk;
    private City moscow;

    @BeforeEach
    void setUp() {
        // Используем конструктор только с DAO (без SessionFactory)
        dispatcherService = new DispatcherService(flightDao, airplaneDao, cityDao);

        testAirplane = new Airplane();
        testAirplane.setId(1);
        testAirplane.setModel("Boeing 737");
        testAirplane.setCapacity(180);
        testAirplane.setStatus(Airplane.AirplaneStatus.ACTIVE);

        minsk = new City("Minsk");
        minsk.setId(1);

        moscow = new City("Moscow");
        moscow.setId(2);
    }

    @Test
    @DisplayName("Should return list of all flights")
    void getSchedule_shouldReturnListOfFlights() {
        Flight flight1 = new Flight();
        flight1.setId(1);
        flight1.setFlightNumber("SU123");

        Flight flight2 = new Flight();
        flight2.setId(2);
        flight2.setFlightNumber("SU456");

        List<Flight> expectedFlights = Arrays.asList(flight1, flight2);
        when(flightDao.findAll(any())).thenReturn(expectedFlights);

        List<Flight> result = dispatcherService.getSchedule();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Flight::getFlightNumber).contains("SU123", "SU456");
    }

    @Test
    @DisplayName("Should successfully add airplane")
    void addAirplane_shouldSucceed() {
        doNothing().when(airplaneDao).save(any(), any(Airplane.class));

        String result = dispatcherService.addAirplane("Airbus A380", 500);

        assertThat(result).startsWith("Успех");
        verify(airplaneDao, times(1)).save(any(), any(Airplane.class));
    }

    @Test
    @DisplayName("Should successfully add flight")
    void addFlight_shouldSucceed() {
        when(airplaneDao.findById(any(), eq(1))).thenReturn(testAirplane);
        when(cityDao.findByName(any(), eq("Minsk"))).thenReturn(minsk);
        when(cityDao.findByName(any(), eq("Moscow"))).thenReturn(moscow);
        doNothing().when(flightDao).save(any(), any(Flight.class));

        String result = dispatcherService.addFlight(
                "SU1234", "Minsk", "Moscow",
                LocalDateTime.now().plusDays(1), 1, BigDecimal.valueOf(150.00)
        );

        assertThat(result).startsWith("Успех");
        verify(flightDao, times(1)).save(any(), any(Flight.class));
    }

    @Test
    @DisplayName("Should fail when airplane not found")
    void addFlight_shouldFail_whenAirplaneNotFound() {
        when(airplaneDao.findById(any(), eq(999))).thenReturn(null);

        String result = dispatcherService.addFlight(
                "SU1234", "Minsk", "Moscow",
                LocalDateTime.now().plusDays(1), 999, BigDecimal.valueOf(150.00)
        );

        assertThat(result)
                .contains("Ошибка")
                .contains("Самолет не найден");
    }
}