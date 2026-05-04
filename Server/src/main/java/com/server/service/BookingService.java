package com.server.service;

import com.common.entity.Flight;
import com.common.entity.Passenger;
import com.common.entity.Ticket;
import com.server.dao.FlightDao;
import com.server.dao.PassengerDao;
import com.server.dao.TicketDao;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingService {

    private final FlightDao flightDao = new FlightDao();
    private final PassengerDao passengerDao = new PassengerDao();
    private final TicketDao ticketDao = new TicketDao();

    /**
     * Покупка билета. Выполняется в рамках одной транзакции.
     * @param passengerId ID пассажира
     * @param flightId ID рейса
     * @param price Цена (устанавливается клиентом или системой)
     * @return Сообщение об успешности операции
     */
    public String bookTicket(Integer passengerId, Integer flightId, BigDecimal price) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction(); // Начало транзакции

            Passenger passenger = passengerDao.findById(session, passengerId);
            if (passenger == null) return "Ошибка: Пассажир не найден.";

            Flight flight = flightDao.findById(session, flightId);
            if (flight == null) return "Ошибка: Рейс не найден.";

            // Валидация
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                return "Ошибка: Некорректная цена билета.";
            }
            if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
                return "Ошибка: Рейс уже вылетел.";
            }

            // Проверка свободных мест (вместимость самолета минус проданные билеты)
            long bookedTickets = ticketDao.countTicketsForFlight(session, flightId);
            Integer capacity = flight.getAirplane().getCapacity();
            if (bookedTickets >= capacity) {
                return "Ошибка: Нет свободных мест.";
            }

            // Создаем билет
            Ticket ticket = new Ticket();
            ticket.setPassenger(passenger);
            ticket.setFlight(flight);
            ticket.setPrice(price);
            ticket.setSeatNumber("S" + (bookedTickets + 1)); // Генерируем номер места
            ticket.setStatus(Ticket.TicketStatus.PAID);

            ticketDao.save(session, ticket);

            transaction.commit(); // Сохраняем изменения в БД
            return "Успех: Билет успешно куплен! Место: " + ticket.getSeatNumber();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Откат в случае ошибки (Rollback)
            }
            e.printStackTrace();
            return "Ошибка сервера: " + e.getMessage();
        }
    }
}
