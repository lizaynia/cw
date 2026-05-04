package com.server.dao;

import com.common.entity.Role;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class RoleDao {
    public Role findByName(Session session, String roleName) {
        Query<Role> query = session.createQuery("from Role where roleName = :roleName", Role.class);
        query.setParameter("roleName", roleName);
        return query.uniqueResult();
    }
}
