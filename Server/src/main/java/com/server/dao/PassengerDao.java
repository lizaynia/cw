package com.server.dao;

import com.common.entity.Passenger;
import org.hibernate.Session;

public class PassengerDao extends BaseDao<Passenger> {
    public PassengerDao() {
        super(Passenger.class);
    }

    public Passenger findByUserId(Session session, Integer userId) {
        if (userId == null) return null;

        String hql = "SELECT p FROM Passenger p WHERE p.user.id = :userId";
        return session.createQuery(hql, Passenger.class)
                .setParameter("userId", userId)
                .uniqueResult();
    }
}