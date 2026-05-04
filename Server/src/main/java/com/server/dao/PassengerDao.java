package com.server.dao;

import com.common.entity.Passenger;

public class PassengerDao extends BaseDao<Passenger> {
    public PassengerDao() {
        super(Passenger.class);
    }
    public Passenger findByUserId(org.hibernate.Session session, Integer userId) {
        return session.createQuery("from Passenger where user.id = :userId", Passenger.class)
                .setParameter("userId", userId)
                .uniqueResult();
    }
}
