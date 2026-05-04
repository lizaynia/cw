package com.server.dao;

import com.common.entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class UserDao {
    public void save(Session session, User user) {
        session.persist(user);
    }

    public void update(Session session, User user) {
        session.merge(user);
    }

    public User findById(Session session, Integer id) {
        return session.get(User.class, id);
    }

    public User findByLogin(Session session, String login) {
        Query<User> query = session.createQuery("from User where login = :login", User.class);
        query.setParameter("login", login);
        return query.uniqueResult();
    }

    public List<User> findAll(Session session) {
        return session.createQuery("from User", User.class).list();
    }
}
