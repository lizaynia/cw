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

import java.time.LocalDateTime;
import java.util.List;

public class BookingService {

    private final FlightDao flightDao = new FlightDao();
    private final PassengerDao passengerDao = new PassengerDao();
    private final TicketDao ticketDao = new TicketDao();

    /**
     * Покупка билета - АТОМАРНАЯ ТРАНЗАКЦИЯ
     * @param passengerId ID пассажира
     * @param flightId ID рейса
     * @param seatNumber Номер места
     * @return Сообщение об успешности операции
     */
    public String bookTicket(Integer passengerId, Integer flightId, String seatNumber) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // 1. Проверка пассажира
            Passenger passenger = passengerDao.findById(session, passengerId);
            if (passenger == null) {
                return "Ошибка: Пассажир не найден.";
            }

            // 2. Проверка рейса
            Flight flight = flightDao.findById(session, flightId);
            if (flight == null) {
                return "Ошибка: Рейс не найден.";
            }

            // 3. Проверка - рейс еще не вылетел
            if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
                return "Ошибка: Рейс уже вылетел.";
            }

            // 4. Проверка - место не занято (в той же транзакции)
            if (ticketDao.isSeatTaken(session, flightId, seatNumber)) {
                return "Ошибка: Место " + seatNumber + " уже занято.";
            }

            // 5. Проверка - есть ли свободные места
            long bookedTickets = ticketDao.countTicketsForFlight(session, flightId);
            Integer capacity = flight.getAirplane().getCapacity();
            if (bookedTickets >= capacity) {
                return "Ошибка: Нет свободных мест на рейсе.";
            }

            // 6. Создание билета
            Ticket ticket = new Ticket();
            ticket.setPassenger(passenger);
            ticket.setFlight(flight);
            ticket.setPrice(flight.getBasePrice());
            ticket.setSeatNumber(seatNumber);
            ticket.setStatus(Ticket.TicketStatus.PAID);
            ticket.setBookingTime(LocalDateTime.now());

            ticketDao.save(session, ticket);

            // 7. COMMIT транзакции - все изменения сохраняются атомарно
            transaction.commit();

            return "Успех: Билет успешно куплен! Место: " + ticket.getSeatNumber();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // ROLLBACK при любой ошибке
            }
            e.printStackTrace();
            return "Ошибка сервера: " + e.getMessage();
        }
    }

    public List<String> getOccupiedSeats(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select t.seatNumber from Ticket t where t.flight.id = :flightId and t.seatNumber is not null",
                            String.class)
                    .setParameter("flightId", flightId)
                    .list();
        }
    }
}