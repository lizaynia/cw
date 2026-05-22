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

    // Конструктор для тестов (с инъекцией зависимостей, без SessionFactory)
    public DispatcherService(FlightDao flightDao, AirplaneDao airplaneDao, CityDao cityDao) {
        this.sessionFactory = null;
        this.flightDao = flightDao;
        this.airplaneDao = airplaneDao;
        this.cityDao = cityDao;
    }

    // Конструктор для тестов (с инъекцией всех зависимостей)
    public DispatcherService(SessionFactory sessionFactory,
                             FlightDao flightDao,
                             AirplaneDao airplaneDao,
                             CityDao cityDao) {
        this.sessionFactory = sessionFactory;
        this.flightDao = flightDao;
        this.airplaneDao = airplaneDao;
        this.cityDao = cityDao;
    }

    // Конструктор для продакшена
    public DispatcherService() {
        this(HibernateUtil.getSessionFactory(),
                new FlightDao(),
                new AirplaneDao(),
                new CityDao());
    }

    public List<Flight> getSchedule() {
        if (sessionFactory == null) {
            // Для тестов - используем моки напрямую
            return flightDao.findAll(null);
        }
        try (Session session = sessionFactory.openSession()) {
            return flightDao.findAll(session);
        }
    }

    public List<Airplane> getAirplanes() {
        if (sessionFactory == null) {
            return airplaneDao.findAll(null);
        }
        try (Session session = sessionFactory.openSession()) {
            return airplaneDao.findAll(session);
        }
    }

    public String addAirplane(String model, Integer capacity) {
        if (sessionFactory == null) {
            // Для тестов
            Airplane airplane = new Airplane();
            airplane.setModel(model);
            airplane.setCapacity(capacity);
            airplane.setStatus(Airplane.AirplaneStatus.ACTIVE);
            airplaneDao.save(null, airplane);
            return "Успех: Самолет успешно добавлен.";
        }

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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

    public String addFlight(String flightNumber, String departureCityName,
                            String arrivalCityName, LocalDateTime departureTime,
                            Integer airplaneId, java.math.BigDecimal basePrice) {
        if (sessionFactory == null) {
            // Для тестов
            Airplane airplane = airplaneDao.findById(null, airplaneId);
            if (airplane == null) return "Ошибка: Самолет не найден.";
            if (airplane.getStatus() != Airplane.AirplaneStatus.ACTIVE) {
                return "Ошибка: Самолет не готов к вылету (статус: " + airplane.getStatus() + ").";
            }

            City depCity = cityDao.findByName(null, departureCityName);
            if (depCity == null) {
                depCity = new City(departureCityName);
                cityDao.save(null, depCity);
            }

            City arrCity = cityDao.findByName(null, arrivalCityName);
            if (arrCity == null) {
                arrCity = new City(arrivalCityName);
                cityDao.save(null, arrCity);
            }

            Flight flight = new Flight();
            flight.setFlightNumber(flightNumber);
            flight.setDepartureCity(depCity);
            flight.setArrivalCity(arrCity);
            flight.setDepartureTime(departureTime);
            flight.setAirplane(airplane);
            flight.setBasePrice(basePrice != null ? basePrice : java.math.BigDecimal.valueOf(100.0));
            flightDao.save(null, flight);
            return "Успех: Рейс добавлен в расписание.";
        }

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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
        if (sessionFactory == null) {
            Flight flight = flightDao.findById(null, flightId);
            if (flight == null) return "Ошибка: Рейс не найден.";
            flightDao.delete(null, flight);
            return "Успех: Рейс удалён.";
        }

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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
        if (sessionFactory == null) {
            Flight flight = flightDao.findById(null, flightId);
            if (flight == null) return "Ошибка: Рейс не найден.";

            Airplane airplane = airplaneDao.findById(null, airplaneId);
            if (airplane == null) return "Ошибка: Самолет не найден.";

            City depCity = cityDao.findByName(null, departureCityName);
            if (depCity == null) {
                depCity = new City(departureCityName);
                cityDao.save(null, depCity);
            }

            City arrCity = cityDao.findByName(null, arrivalCityName);
            if (arrCity == null) {
                arrCity = new City(arrivalCityName);
                cityDao.save(null, arrCity);
            }

            flight.setFlightNumber(flightNumber);
            flight.setDepartureCity(depCity);
            flight.setArrivalCity(arrCity);
            flight.setDepartureTime(departureTime);
            flight.setAirplane(airplane);
            flight.setBasePrice(basePrice != null ? basePrice : java.math.BigDecimal.valueOf(100.0));

            flightDao.update(null, flight);
            return "Успех: Рейс обновлен.";
        }

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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