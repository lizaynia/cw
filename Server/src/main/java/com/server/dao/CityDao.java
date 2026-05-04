package com.server.dao;

import com.common.entity.City;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class CityDao extends BaseDao<City> {
    public CityDao() {
        super(City.class);
    }

    public City findByName(Session session, String cityName) {
        Query<City> query = session.createQuery("from City where cityName = :cityName", City.class);
        query.setParameter("cityName", cityName);
        return query.uniqueResult();
    }
}
