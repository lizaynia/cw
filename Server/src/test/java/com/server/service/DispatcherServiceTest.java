package com.server.service;

import com.common.entity.*;
import com.server.dao.AirplaneDao;
import com.server.dao.CityDao;
import com.server.dao.FlightDao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @InjectMocks
    private DispatcherService dispatcherService;

    private Airplane testAirplane;
    private City minsk;
    private City moscow;

    @BeforeEach
    void setUp() {
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

    @Nested
    @DisplayName("Get Schedule Tests")
    class GetScheduleTests {

        @Test
        @DisplayName("Should return list of all flights")
        void getSchedule_shouldReturnListOfFlights() {
            // Given
            Flight flight1 = new Flight();
            flight1.setId(1);
            flight1.setFlightNumber("SU123");

            Flight flight2 = new Flight();
            flight2.setId(2);
            flight2.setFlightNumber("SU456");

            when(flightDao.findAll(any(Session.class))).thenReturn(Arrays.asList(flight1, flight2));

            // When
            List<Flight> result = dispatcherService.getSchedule();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Flight::getFlightNumber).contains("SU123", "SU456");
        }
    }

    @Nested
    @DisplayName("Get Airplanes Tests")
    class GetAirplanesTests {

        @Test
        @DisplayName("Should return list of all airplanes")
        void getAirplanes_shouldReturnListOfAirplanes() {
            // Given
            List<Airplane> expected = Arrays.asList(testAirplane, new Airplane());
            when(airplaneDao.findAll(any(Session.class))).thenReturn(expected);

            // When
            List<Airplane> result = dispatcherService.getAirplanes();

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Add Airplane Tests")
    class AddAirplaneTests {

        @Test
        @DisplayName("Should successfully add airplane")
        void addAirplane_shouldSucceed() {
            // Given
            doNothing().when(airplaneDao).save(any(Session.class), any(Airplane.class));

            // When
            String result = dispatcherService.addAirplane("Airbus A380", 500);

            // Then
            assertThat(result).startsWith("Успех");
            verify(airplaneDao, times(1)).save(any(Session.class), any(Airplane.class));
        }
    }

    @Nested
    @DisplayName("Add Flight Tests")
    class AddFlightTests {

        @Test
        @DisplayName("Should successfully add flight")
        void addFlight_shouldSucceed() {
            // Given
            when(airplaneDao.findById(any(Session.class), eq(1))).thenReturn(testAirplane);
            when(cityDao.findByName(any(Session.class), eq("Minsk"))).thenReturn(minsk);
            when(cityDao.findByName(any(Session.class), eq("Moscow"))).thenReturn(moscow);
            doNothing().when(flightDao).save(any(Session.class), any(Flight.class));

            // When
            String result = dispatcherService.addFlight(
                    "SU1234", "Minsk", "Moscow",
                    LocalDateTime.now().plusDays(1), 1, BigDecimal.valueOf(150.00)
            );

            // Then
            assertThat(result).startsWith("Успех");
            verify(flightDao, times(1)).save(any(Session.class), any(Flight.class));
        }

        @Test
        @DisplayName("Should fail when airplane not found")
        void addFlight_shouldFail_whenAirplaneNotFound() {
            // Given
            when(airplaneDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = dispatcherService.addFlight(
                    "SU1234", "Minsk", "Moscow",
                    LocalDateTime.now().plusDays(1), 999, BigDecimal.valueOf(150.00)
            );

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Самолет не найден");
        }

        @Test
        @DisplayName("Should fail when airplane is not ACTIVE")
        void addFlight_shouldFail_whenAirplaneNotActive() {
            // Given
            testAirplane.setStatus(Airplane.AirplaneStatus.MAINTENANCE);
            when(airplaneDao.findById(any(Session.class), eq(1))).thenReturn(testAirplane);

            // When
            String result = dispatcherService.addFlight(
                    "SU1234", "Minsk", "Moscow",
                    LocalDateTime.now().plusDays(1), 1, BigDecimal.valueOf(150.00)
            );

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("не готов");
        }
    }

    @Nested
    @DisplayName("Delete Flight Tests")
    class DeleteFlightTests {

        @Test
        @DisplayName("Should successfully delete flight")
        void deleteFlight_shouldSucceed() {
            // Given
            Flight flight = new Flight();
            flight.setId(1);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(flight);

            // Mock count query
            var query = mock(org.hibernate.query.Query.class);
            when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter(anyString(), any())).thenReturn(query);
            when(query.uniqueResult()).thenReturn(0L);

            doNothing().when(flightDao).delete(any(Session.class), any(Flight.class));

            // When
            String result = dispatcherService.deleteFlight(1);

            // Then
            assertThat(result).startsWith("Успех");
        }

        @Test
        @DisplayName("Should fail when flight has tickets")
        void deleteFlight_shouldFail_whenFlightHasTickets() {
            // Given
            Flight flight = new Flight();
            flight.setId(1);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(flight);

            var query = mock(org.hibernate.query.Query.class);
            when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter(anyString(), any())).thenReturn(query);
            when(query.uniqueResult()).thenReturn(5L); // 5 tickets exist

            // When
            String result = dispatcherService.deleteFlight(1);

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("невозможно удалить");
            verify(flightDao, never()).delete(any(Session.class), any(Flight.class));
        }
    }

    @Nested
    @DisplayName("Update Flight Tests")
    class UpdateFlightTests {

        @Test
        @DisplayName("Should successfully update flight")
        void updateFlight_shouldSucceed() {
            // Given
            Flight existingFlight = new Flight();
            existingFlight.setId(1);
            existingFlight.setFlightNumber("OLD123");

            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(existingFlight);
            when(airplaneDao.findById(any(Session.class), eq(1))).thenReturn(testAirplane);
            when(cityDao.findByName(any(Session.class), eq("Minsk"))).thenReturn(minsk);
            when(cityDao.findByName(any(Session.class), eq("Moscow"))).thenReturn(moscow);
            doNothing().when(flightDao).update(any(Session.class), any(Flight.class));

            // When
            String result = dispatcherService.updateFlight(
                    1, "NEW123", "Minsk", "Moscow",
                    LocalDateTime.now().plusDays(2), 1, BigDecimal.valueOf(200.00)
            );

            // Then
            assertThat(result).startsWith("Успех");
            assertThat(existingFlight.getFlightNumber()).isEqualTo("NEW123");
        }
    }
}