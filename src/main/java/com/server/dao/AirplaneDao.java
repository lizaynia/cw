package com.server.dao;

import com.common.entity.Airplane;
import org.hibernate.Session;

import java.util.List;

public class AirplaneDao {
    public void save(Session session, Airplane airplane) {
        session.persist(airplane);
    }

    public Airplane findById(Session session, Integer id) {
        return session.get(Airplane.class, id);
    }

    public List<Airplane> findAll(Session session) {
        return session.createQuery("from Airplane", Airplane.class).list();
    }
}
