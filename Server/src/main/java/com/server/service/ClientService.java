package com.server.service;

import com.common.entity.Flight;
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

    public List<Flight> searchFlights(String dep, String arr, java.time.LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("from Flight f where 1=1");

            if (dep != null && !dep.trim().isEmpty()) {
                hql.append(" and lower(f.departureCity.cityName) like lower(:dep)");
            }
            if (arr != null && !arr.trim().isEmpty()) {
                hql.append(" and lower(f.arrivalCity.cityName) like lower(:arr)");
            }
            if (date != null) {
                hql.append(" and function('DATE', f.departureTime) = :date");
            }

            hql.append(" order by f.departureTime");

            var query = session.createQuery(hql.toString(), Flight.class);

            if (dep != null && !dep.trim().isEmpty()) {
                query.setParameter("dep", "%" + dep.trim() + "%");
            }
            if (arr != null && !arr.trim().isEmpty()) {
                query.setParameter("arr", "%" + arr.trim() + "%");
            }
            if (date != null) {
                query.setParameter("date", date);
            }

            return query.list();
        }
    }
}
