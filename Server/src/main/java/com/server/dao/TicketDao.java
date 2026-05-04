package com.server.dao;

import com.common.entity.Ticket;
import org.hibernate.Session;
import java.util.List;

public class TicketDao extends BaseDao<Ticket> {
    public TicketDao() {
        super(Ticket.class);
    }

    public long countTicketsForFlight(Session session, Integer flightId) {
        Long count = session.createQuery("select count(t) from Ticket t where t.flight.id = :flightId", Long.class)
                .setParameter("flightId", flightId)
                .uniqueResult();
        return count != null ? count : 0;
    }

    public List<Ticket> findAllByPassengerId(Session session, Integer passengerId) {
        return session.createQuery("from Ticket t where t.passenger.id = :passengerId", Ticket.class)
                .setParameter("passengerId", passengerId)
                .list();
    }
}
