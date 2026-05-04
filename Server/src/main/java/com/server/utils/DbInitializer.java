package com.server.utils;

import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DbInitializer {
    public static void seedData() {
        RoleDao roleDao = new RoleDao();
        UserDao userDao = new UserDao();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Проверяем наличие ролей
            if (roleDao.findByName(session, "ADMIN") == null) {
                session.persist(new Role("ADMIN"));
            }
            if (roleDao.findByName(session, "DISPATCHER") == null) {
                session.persist(new Role("DISPATCHER"));
            }
            if (roleDao.findByName(session, "CLIENT") == null) {
                session.persist(new Role("CLIENT"));
            }

            // Проверяем наличие админа
            User admin = userDao.findByLogin(session, "admin");
            if (admin == null) {
                Role adminRole = roleDao.findByName(session, "ADMIN");
                admin = new User("admin", HashUtil.hashPassword("admin123"), adminRole);
                session.persist(admin);
            }
            if (session.createQuery("from Passenger where user.id = :uid", com.common.entity.Passenger.class)
                    .setParameter("uid", admin.getId()).uniqueResult() == null) {
                com.common.entity.Passenger p = new com.common.entity.Passenger();
                p.setFirstName("Admin");
                p.setLastName("System");
                p.setPassportNumber("ADMIN001");
                p.setUser(admin);
                session.persist(p);
            }

            // Проверяем наличие диспетчера
            User disp = userDao.findByLogin(session, "dispatcher");
            if (disp == null) {
                Role dispRole = roleDao.findByName(session, "DISPATCHER");
                disp = new User("dispatcher", HashUtil.hashPassword("disp123"), dispRole);
                session.persist(disp);
            }
            if (session.createQuery("from Passenger where user.id = :uid", com.common.entity.Passenger.class)
                    .setParameter("uid", disp.getId()).uniqueResult() == null) {
                com.common.entity.Passenger p = new com.common.entity.Passenger();
                p.setFirstName("Main");
                p.setLastName("Dispatcher");
                p.setPassportNumber("DISP001");
                p.setUser(disp);
                session.persist(p);
            }

            // Проверяем наличие клиента
            User client = userDao.findByLogin(session, "client");
            if (client == null) {
                Role clientRole = roleDao.findByName(session, "CLIENT");
                client = new User("client", HashUtil.hashPassword("client123"), clientRole);
                session.persist(client);
            }
            if (session.createQuery("from Passenger where user.id = :uid", com.common.entity.Passenger.class)
                    .setParameter("uid", client.getId()).uniqueResult() == null) {
                com.common.entity.Passenger p = new com.common.entity.Passenger();
                p.setFirstName("Test");
                p.setLastName("Client");
                p.setPassportNumber("CLIENT001");
                p.setUser(client);
                session.persist(p);
            }


            // --- ДОБАВЛЕНИЕ ТЕСТОВЫХ ДАННЫХ ДЛЯ РЕЙСОВ ---
            
            // 1. Города
            if (session.createQuery("select count(c) from City c", Long.class).uniqueResult() == 0) {
                session.persist(new com.common.entity.City("Минск"));
                session.persist(new com.common.entity.City("Москва"));
                session.persist(new com.common.entity.City("Париж"));
            }

            // 2. Самолеты
            if (session.createQuery("select count(a) from Airplane a", Long.class).uniqueResult() == 0) {
                com.common.entity.Airplane a1 = new com.common.entity.Airplane();
                a1.setModel("Boeing 737");
                a1.setCapacity(180);
                session.persist(a1);

                com.common.entity.Airplane a2 = new com.common.entity.Airplane();
                a2.setModel("Airbus A320");
                a2.setCapacity(150);
                session.persist(a2);
            }

            // 3. Рейсы
            if (session.createQuery("select count(f) from Flight f", Long.class).uniqueResult() == 0) {
                java.util.List<com.common.entity.City> cities = session.createQuery("from City", com.common.entity.City.class).list();
                java.util.List<com.common.entity.Airplane> planes = session.createQuery("from Airplane", com.common.entity.Airplane.class).list();
                
                if (cities.size() >= 2 && !planes.isEmpty()) {
                    com.common.entity.Flight f1 = new com.common.entity.Flight();
                    f1.setFlightNumber("MSQ-777");
                    f1.setDepartureCity(cities.get(0));
                    f1.setArrivalCity(cities.get(1));
                    f1.setDepartureTime(java.time.LocalDateTime.now().plusDays(1));
                    f1.setAirplane(planes.get(0));
                    session.persist(f1);

                    com.common.entity.Flight f2 = new com.common.entity.Flight();
                    f2.setFlightNumber("PAR-123");
                    f2.setDepartureCity(cities.get(2 % cities.size()));
                    f2.setArrivalCity(cities.get(0));
                    f2.setDepartureTime(java.time.LocalDateTime.now().plusDays(2));
                    f2.setAirplane(planes.get(0));
                    session.persist(f2);
                }
            }

            tx.commit();
            System.out.println("База данных инициализирована (роли, пользователи и тестовые рейсы добавлены).");

        } catch (Exception e) {
            System.err.println("Ошибка при инициализации данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
