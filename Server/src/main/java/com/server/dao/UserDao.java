package com.server.dao;

import com.common.entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserDao extends BaseDao<User> {
    public UserDao() {
        super(User.class);
    }

    public User findByLogin(Session session, String login) {
        Query<User> query = session.createQuery("from User where login = :login", User.class);
        query.setParameter("login", login);
        return query.uniqueResult();
    }
}
