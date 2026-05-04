package com.server.dao;

import com.common.entity.Flight;
import org.hibernate.Session;

public class FlightDao {
    public Flight findById(Session session, Integer id) {
        return session.get(Flight.class, id);
    }

    public void save(Session session, Flight flight) {
        session.persist(flight);
    }

    public void update(Session session, Flight flight) {
        session.merge(flight);
    }

    public void delete(Session session, Flight flight) {
        session.remove(flight);
    }

    public java.util.List<Flight> findAll(Session session) {
        return session.createQuery("from Flight", Flight.class).list();
    }

    public Flight findByFlightNumber(Session session, String flightNumber) {
        return session.createQuery("from Flight where flightNumber = :fn", Flight.class)
                .setParameter("fn", flightNumber)
                .uniqueResult();
    }

    public java.util.List<Flight> search(Session session, String departureCity, String arrivalCity, java.time.LocalDate date) {
        String hql = "from Flight f where f.departureCity = :dep and f.arrivalCity = :arr " +
                     "and cast(f.departureTime as date) = :date";
        return session.createQuery(hql, Flight.class)
                .setParameter("dep", departureCity)
                .setParameter("arr", arrivalCity)
                .setParameter("date", date)
                .list();
    }
}
