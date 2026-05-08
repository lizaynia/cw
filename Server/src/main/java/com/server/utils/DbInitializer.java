package com.server.utils;

import com.common.entity.*;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DbInitializer.class);

    public static void seedData() {
        RoleDao roleDao = new RoleDao();
        UserDao userDao = new UserDao();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // === 1. СОЗДАЁМ РОЛИ (если их нет) ===
            if (roleDao.findByName(session, "ADMIN") == null) {
                session.persist(new Role("ADMIN"));
                logger.info("Роль ADMIN создана");
            }
            if (roleDao.findByName(session, "DISPATCHER") == null) {
                session.persist(new Role("DISPATCHER"));
                logger.info("Роль DISPATCHER создана");
            }
            if (roleDao.findByName(session, "CLIENT") == null) {
                session.persist(new Role("CLIENT"));
                logger.info("Роль CLIENT создана");
            }

            // === 2. СОЗДАЁМ АДМИНИСТРАТОРА (без пассажира) ===
            User admin = userDao.findByLogin(session, "admin");
            if (admin == null) {
                Role adminRole = roleDao.findByName(session, "ADMIN");
                admin = new User("admin", HashUtil.hashPassword("admin123"), adminRole);
                session.persist(admin);
                logger.info("Создан администратор: admin");
            }

            // === 3. СОЗДАЁМ ДИСПЕТЧЕРА (без пассажира) ===
            User dispatcher = userDao.findByLogin(session, "dispatcher");
            if (dispatcher == null) {
                Role dispRole = roleDao.findByName(session, "DISPATCHER");
                dispatcher = new User("dispatcher", HashUtil.hashPassword("disp123"), dispRole);
                session.persist(dispatcher);
                logger.info("Создан диспетчер: dispatcher");
            }

            // === 4. СОЗДАЁМ КЛИЕНТА (ТОЛЬКО ДЛЯ НЕГО ПАССАЖИРА) ===
            User client = userDao.findByLogin(session, "client");
            if (client == null) {
                Role clientRole = roleDao.findByName(session, "CLIENT");
                client = new User("client", HashUtil.hashPassword("client123"), clientRole);
                session.persist(client);
                logger.info("Создан пользователь-клиент: client");
            }

            // client гарантированно существует, проверка не нужна
            Passenger existingPassenger = session.createQuery(
                            "from Passenger where user.id = :uid", Passenger.class)
                    .setParameter("uid", client.getId())
                    .uniqueResult();

            if (existingPassenger == null) {
                Passenger passenger = new Passenger();
                passenger.setFirstName("Test");
                passenger.setLastName("Client");
                passenger.setPassportNumber("CLIENT001");
                passenger.setUser(client);
                session.persist(passenger);
                logger.info("Создан пассажир для клиента: Test Client");
            }

            // === 5. ТЕСТОВЫЕ ГОРОДА, САМОЛЁТЫ, РЕЙСЫ ===
            createTestData(session);

            tx.commit();
            logger.info("Инициализация базы данных завершена успешно");

        } catch (Exception e) {
            logger.error("Ошибка при инициализации данных: {}", e.getMessage(), e);
        }
    }

    private static void createTestData(Session session) {
        // Города
        if (session.createQuery("select count(c) from City c", Long.class).uniqueResult() == 0) {
            session.persist(new City("Минск"));
            session.persist(new City("Москва"));
            session.persist(new City("Париж"));
            session.persist(new City("Лондон"));
            session.persist(new City("Стамбул"));
            logger.info("Созданы тестовые города");
        }

        // Самолеты
        if (session.createQuery("select count(a) from Airplane a", Long.class).uniqueResult() == 0) {
            Airplane a1 = new Airplane();
            a1.setModel("Boeing 737");
            a1.setCapacity(180);
            a1.setStatus(Airplane.AirplaneStatus.ACTIVE);
            session.persist(a1);

            Airplane a2 = new Airplane();
            a2.setModel("Airbus A320");
            a2.setCapacity(150);
            a2.setStatus(Airplane.AirplaneStatus.ACTIVE);
            session.persist(a2);

            Airplane a3 = new Airplane();
            a3.setModel("Boeing 787 Dreamliner");
            a3.setCapacity(290);
            a3.setStatus(Airplane.AirplaneStatus.ACTIVE);
            session.persist(a3);

            logger.info("Созданы тестовые самолеты");
        }

        // Рейсы (если нет)
        if (session.createQuery("select count(f) from Flight f", Long.class).uniqueResult() == 0) {
            var cities = session.createQuery("from City", City.class).list();
            var planes = session.createQuery("from Airplane", Airplane.class).list();

            if (cities.size() >= 2 && !planes.isEmpty()) {
                // Рейс Минск -> Москва
                Flight f1 = new Flight();
                f1.setFlightNumber("MSQ-777");
                f1.setDepartureCity(cities.get(0));
                f1.setArrivalCity(cities.get(1));
                f1.setDepartureTime(java.time.LocalDateTime.now().plusDays(1));
                f1.setAirplane(planes.get(0));
                f1.setBasePrice(java.math.BigDecimal.valueOf(150.00));
                session.persist(f1);

                // Рейс Москва -> Париж
                if (cities.size() > 2) {
                    Flight f2 = new Flight();
                    f2.setFlightNumber("SVO-123");
                    f2.setDepartureCity(cities.get(1));
                    f2.setArrivalCity(cities.get(2));
                    f2.setDepartureTime(java.time.LocalDateTime.now().plusDays(2));
                    f2.setAirplane(planes.get(1));
                    f2.setBasePrice(java.math.BigDecimal.valueOf(250.00));
                    session.persist(f2);
                }

                logger.info("Созданы тестовые рейсы");
            }
        }
    }
}