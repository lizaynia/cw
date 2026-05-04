package com.server.dao;

import com.common.entity.Passenger;
import org.hibernate.Session;

public class PassengerDao {
    public Passenger findById(Session session, Integer id) {
        return session.get(Passenger.class, id);
    }
    public void save(Session session, Passenger passenger) {
        session.persist(passenger);
    }

    public void update(Session session, Passenger passenger) {
        session.merge(passenger);
    }

    public void delete(Session session, Passenger passenger) {
        session.remove(passenger);
    }

    public java.util.List<Passenger> findAll(Session session) {
        return session.createQuery("from Passenger", Passenger.class).list();
    }
}
