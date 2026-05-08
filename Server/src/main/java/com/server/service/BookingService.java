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
import java.util.List;

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
    public String bookTicket(Session session, Integer passengerId, Integer flightId, String seatNumber) {
        try {

            Passenger passenger = passengerDao.findById(session, passengerId);
            if (passenger == null) return "Ошибка: Пассажир не найден.";

            Flight flight = flightDao.findById(session, flightId);
            if (flight == null) return "Ошибка: Рейс не найден.";

            if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
                return "Ошибка: Рейс уже вылетел.";
            }

            if (ticketDao.isSeatTaken(session, flightId, seatNumber)) {
                return "Ошибка: Место " + seatNumber + " уже занято.";
            }

            long bookedTickets = ticketDao.countTicketsForFlight(session, flightId);
            Integer capacity = flight.getAirplane().getCapacity();
            if (bookedTickets >= capacity) {
                return "Ошибка: Нет свободных мест на рейсе.";
            }

            Ticket ticket = new Ticket();
            ticket.setPassenger(passenger);
            ticket.setFlight(flight);
            ticket.setPrice(flight.getBasePrice());
            ticket.setSeatNumber(seatNumber);
            ticket.setStatus(Ticket.TicketStatus.PAID);

            ticketDao.save(session, ticket);



            return "Успех: Билет успешно куплен! Место: " + ticket.getSeatNumber();

        } catch (Exception e) {

            e.printStackTrace();
            return "Ошибка сервера: " + e.getMessage();
        }
    }

    public List<String> getOccupiedSeats(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Ticket> tickets = session.createQuery(
                            "select t from Ticket t where t.flight.id = :flightId", Ticket.class)
                    .setParameter("flightId", flightId)
                    .list();
            return tickets.stream()
                    .map(Ticket::getSeatNumber)
                    .filter(seat -> seat != null && !seat.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
    }

}
