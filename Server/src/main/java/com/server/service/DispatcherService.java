package com.server.service;

import com.common.entity.Airplane;
import com.common.entity.City;
import com.common.entity.Flight;
import com.server.dao.AirplaneDao;
import com.server.dao.CityDao;
import com.server.dao.FlightDao;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class DispatcherService {
    private final SessionFactory sessionFactory;
    private final FlightDao flightDao;
    private final AirplaneDao airplaneDao;
    private final CityDao cityDao;

    public DispatcherService(SessionFactory sessionFactory,
                             FlightDao flightDao,
                             AirplaneDao airplaneDao,
                             CityDao cityDao) {
        this.sessionFactory = sessionFactory;
        this.flightDao = flightDao;
        this.airplaneDao = airplaneDao;
        this.cityDao = cityDao;
    }

    public DispatcherService() {
        this(HibernateUtil.getSessionFactory(),
                new FlightDao(),
                new AirplaneDao(),
                new CityDao());
    }
    public List<Flight> getSchedule() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return flightDao.findAll(session);
        }
    }

    public List<Airplane> getAirplanes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return airplaneDao.findAll(session);
        }
    }

    public String addAirplane(String model, Integer capacity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Airplane airplane = new Airplane();
            airplane.setModel(model);
            airplane.setCapacity(capacity);
            airplane.setStatus(Airplane.AirplaneStatus.ACTIVE);

            airplaneDao.save(session, airplane);

            transaction.commit();
            return "Успех: Самолет успешно добавлен.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка добавления самолета: " + e.getMessage();
        }
    }

    public String addFlight(String flightNumber, String departureCityName, String arrivalCityName, LocalDateTime departureTime, Integer airplaneId, java.math.BigDecimal basePrice) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Airplane airplane = airplaneDao.findById(session, airplaneId);
            if (airplane == null) return "Ошибка: Самолет не найден.";
            if (airplane.getStatus() != Airplane.AirplaneStatus.ACTIVE) {
                return "Ошибка: Самолет не готов к вылету (статус: " + airplane.getStatus() + ").";
            }

            City depCity = cityDao.findByName(session, departureCityName);
            if (depCity == null) {
                depCity = new City(departureCityName);
                cityDao.save(session, depCity);
            }

            City arrCity = cityDao.findByName(session, arrivalCityName);
            if (arrCity == null) {
                arrCity = new City(arrivalCityName);
                cityDao.save(session, arrCity);
            }

            Flight flight = new Flight();
            flight.setFlightNumber(flightNumber);
            flight.setDepartureCity(depCity);
            flight.setArrivalCity(arrCity);
            flight.setDepartureTime(departureTime);
            flight.setAirplane(airplane);
            flight.setBasePrice(basePrice != null ? basePrice : java.math.BigDecimal.valueOf(100.0));
            flightDao.save(session, flight);

            transaction.commit();
            return "Успех: Рейс добавлен в расписание.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка добавления рейса: " + e.getMessage();
        }
    }

    public String deleteFlight(Integer flightId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Flight flight = flightDao.findById(session, flightId);
            if (flight == null) return "Ошибка: Рейс не найден.";

            long ticketCount = session.createQuery(
                            "select count(t) from Ticket t where t.flight.id = :id", Long.class)
                    .setParameter("id", flightId)
                    .uniqueResult();
            if (ticketCount > 0) {
                return "Ошибка: Невозможно удалить рейс с проданными билетами.";
            }

            flightDao.delete(session, flight);
            transaction.commit();
            return "Успех: Рейс удалён.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка удаления: " + e.getMessage();
        }
    }


    public String updateFlight(Integer flightId, String flightNumber, String departureCityName,
                               String arrivalCityName, LocalDateTime departureTime,
                               Integer airplaneId, java.math.BigDecimal basePrice) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Flight flight = flightDao.findById(session, flightId);
            if (flight == null) return "Ошибка: Рейс не найден.";

            Airplane airplane = airplaneDao.findById(session, airplaneId);
            if (airplane == null) return "Ошибка: Самолет не найден.";

            City depCity = cityDao.findByName(session, departureCityName);
            if (depCity == null) {
                depCity = new City(departureCityName);
                cityDao.save(session, depCity);
            }

            City arrCity = cityDao.findByName(session, arrivalCityName);
            if (arrCity == null) {
                arrCity = new City(arrivalCityName);
                cityDao.save(session, arrCity);
            }

            flight.setFlightNumber(flightNumber);
            flight.setDepartureCity(depCity);
            flight.setArrivalCity(arrCity);
            flight.setDepartureTime(departureTime);
            flight.setAirplane(airplane);
            flight.setBasePrice(basePrice != null ? basePrice : java.math.BigDecimal.valueOf(100.0));

            flightDao.update(session, flight);
            transaction.commit();
            return "Успех: Рейс обновлен.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка обновления рейса: " + e.getMessage();
        }
    }
}
