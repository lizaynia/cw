package com.server.service;

import com.common.entity.Ticket;
import com.server.dao.TicketDao;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class ClientService {
    private final TicketDao ticketDao = new TicketDao();

    public List<Ticket> getTicketHistory(Integer passengerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return ticketDao.findAllByPassengerId(session, passengerId);
        }
    }

    public List<com.common.entity.Flight> searchFlights(String dep, String arr, java.time.LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return new com.server.dao.FlightDao().search(session, dep, arr, date);
        }
    }
}
