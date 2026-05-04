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

    public java.util.List<Flight> findAll(Session session) {
        return session.createQuery("from Flight", Flight.class).list();
    }
}
