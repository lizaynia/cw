package com.server.service;

import com.common.entity.Airplane;
import com.common.entity.City;
import com.common.entity.Flight;
import com.server.dao.AirplaneDao;
import com.server.dao.CityDao;
import com.server.dao.FlightDao;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class DispatcherService {
    private final FlightDao flightDao = new FlightDao();
    private final AirplaneDao airplaneDao = new AirplaneDao();
    private final CityDao cityDao = new CityDao();

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

    public String addFlight(String flightNumber, String departureCityName, String arrivalCityName, LocalDateTime departureTime, Integer airplaneId) {
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
            
            flightDao.delete(session, flight);
            transaction.commit();
            return "Успех: Рейс удален.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка удаления рейса: " + e.getMessage();
        }
    }
}
