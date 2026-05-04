package com.server.dao;

import org.hibernate.Session;
import java.util.List;

public abstract class BaseDao<T> {
    private final Class<T> entityClass;

    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void save(Session session, T entity) {
        session.persist(entity);
    }

    public void update(Session session, T entity) {
        session.merge(entity);
    }

    public void delete(Session session, T entity) {
        session.remove(entity);
    }

    public T findById(Session session, Integer id) {
        return session.get(entityClass, id);
    }

    public List<T> findAll(Session session) {
        return session.createQuery("from " + entityClass.getSimpleName(), entityClass).list();
    }
}
