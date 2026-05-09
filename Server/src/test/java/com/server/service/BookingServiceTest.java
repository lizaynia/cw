package com.server.service;

import com.common.entity.*;
import com.server.dao.FlightDao;
import com.server.dao.PassengerDao;
import com.server.dao.TicketDao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private SessionFactory sessionFactory;
    @Mock private Session session;
    @Mock private Transaction transaction;
    @Mock private FlightDao flightDao;
    @Mock private PassengerDao passengerDao;
    @Mock private TicketDao ticketDao;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        bookingService = new BookingService(sessionFactory, flightDao, passengerDao, ticketDao);
    }

    @Test
    void bookTicket_shouldSucceed() {

    @Nested
    @DisplayName("Book Ticket Tests - Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Should successfully book a ticket")
        void bookTicket_shouldSucceed_whenAllConditionsMet() {
            // Given
            when(passengerDao.findById(any(Session.class), eq(1))).thenReturn(testPassenger);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(testFlight);
            when(ticketDao.isSeatTaken(any(Session.class), eq(1), eq("A1"))).thenReturn(false);
            when(ticketDao.countTicketsForFlight(any(Session.class), eq(1))).thenReturn(0L);
            doNothing().when(ticketDao).save(any(Session.class), any(Ticket.class));

            // When
            String result = bookingService.bookTicket(1, 1, "A1");

            // Then
            assertThat(result).startsWith("Успех");
            verify(ticketDao, times(1)).save(any(Session.class), any(Ticket.class));
        }

        @Test
        @DisplayName("Should book ticket when exactly one seat left")
        void bookTicket_shouldSucceed_whenLastSeat() {
            // Given
            when(passengerDao.findById(any(Session.class), eq(1))).thenReturn(testPassenger);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(testFlight);
            when(ticketDao.isSeatTaken(any(Session.class), eq(1), eq("A1"))).thenReturn(false);
            when(ticketDao.countTicketsForFlight(any(Session.class), eq(1))).thenReturn(179L); // 179 booked, 1 left

            // When
            String result = bookingService.bookTicket(1, 1, "A1");

            // Then
            assertThat(result).startsWith("Успех");
        }
    }

    @Nested
    @DisplayName("Book Ticket Tests - Failure Scenarios")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when passenger not found")
        void bookTicket_shouldFail_whenPassengerNotFound() {
            // Given
            when(passengerDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = bookingService.bookTicket(999, 1, "A1");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Пассажир не найден");
            verify(ticketDao, never()).save(any(Session.class), any(Ticket.class));
        }

        @Test
        @DisplayName("Should fail when flight not found")
        void bookTicket_shouldFail_whenFlightNotFound() {
            // Given
            when(passengerDao.findById(any(Session.class), eq(1))).thenReturn(testPassenger);
            when(flightDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = bookingService.bookTicket(1, 999, "A1");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Рейс не найден");
        }

        @Test
        @DisplayName("Should fail when flight already departed")
        void bookTicket_shouldFail_whenFlightDeparted() {
            // Given
            testFlight.setDepartureTime(LocalDateTime.now().minusHours(1));
            when(passengerDao.findById(any(Session.class), eq(1))).thenReturn(testPassenger);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(testFlight);

            // When
            String result = bookingService.bookTicket(1, 1, "A1");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("уже вылетел");
        }

        @Test
        @DisplayName("Should fail when seat is already taken")
        void bookTicket_shouldFail_whenSeatTaken() {
            // Given
            when(passengerDao.findById(any(Session.class), eq(1))).thenReturn(testPassenger);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(testFlight);
            when(ticketDao.isSeatTaken(any(Session.class), eq(1), eq("A1"))).thenReturn(true);

            // When
            String result = bookingService.bookTicket(1, 1, "A1");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("уже занято");
        }

        @Test
        @DisplayName("Should fail when no seats available")
        void bookTicket_shouldFail_whenNoSeatsAvailable() {
            // Given
            when(passengerDao.findById(any(Session.class), eq(1))).thenReturn(testPassenger);
            when(flightDao.findById(any(Session.class), eq(1))).thenReturn(testFlight);
            when(ticketDao.isSeatTaken(any(Session.class), eq(1), eq("A1"))).thenReturn(false);
            when(ticketDao.countTicketsForFlight(any(Session.class), eq(1))).thenReturn(180L); // Full

            // When
            String result = bookingService.bookTicket(1, 1, "A1");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Нет свободных мест");
        }
    }

    @Nested
    @DisplayName("Get Occupied Seats Tests")
    class GetOccupiedSeatsTests {

        @Test
        @DisplayName("Should return list of occupied seats")
        void getOccupiedSeats_shouldReturnList() {
            // Given
            java.util.List<String> expectedSeats = java.util.Arrays.asList("A1", "A2", "B5");
            var query = mock(org.hibernate.query.Query.class);
            when(session.createQuery(anyString(), eq(String.class))).thenReturn(query);
            when(query.setParameter(anyString(), any())).thenReturn(query);
            when(query.list()).thenReturn(expectedSeats);

            // When
            java.util.List<String> result = bookingService.getOccupiedSeats(1);

            // Then
            assertThat(result).hasSize(3).containsExactly("A1", "A2", "B5");
        }
    }
}