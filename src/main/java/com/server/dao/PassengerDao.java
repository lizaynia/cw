package com.server.dao;

import com.common.entity.Passenger;
import org.hibernate.Session;

public class PassengerDao {
    public Passenger findById(Session session, Integer id) {
        return session.get(Passenger.class, id);
    }
}
