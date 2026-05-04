package com.server.dao;

import com.common.entity.Flight;
import org.hibernate.Session;

import java.util.List;

public class FlightDao extends BaseDao<Flight> {
    public FlightDao() {
        super(Flight.class);
    }

    public Flight findByFlightNumber(Session session, String flightNumber) {
        return session.createQuery("from Flight where flightNumber = :fn", Flight.class)
                .setParameter("fn", flightNumber)
                .uniqueResult();
    }

    public List<Flight> search(Session session, String departureCity, String arrivalCity, java.time.LocalDate date) {
        String hql = "from Flight f where f.departureCity.cityName = :dep and f.arrivalCity.cityName = :arr " +
                     "and cast(f.departureTime as date) = :date";
        return session.createQuery(hql, Flight.class)
                .setParameter("dep", departureCity)
                .setParameter("arr", arrivalCity)
                .setParameter("date", date)
                .list();
    }
}
