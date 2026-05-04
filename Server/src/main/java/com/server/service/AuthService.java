package com.server.service;

import com.common.entity.Passenger;
import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.PassengerDao;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import com.server.utils.HashUtil;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();
    private final PassengerDao passengerDao = new PassengerDao();

    public String register(String login, String password, String firstName, String lastName, String passportNum) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (userDao.findByLogin(session, login) != null) {
                return "Ошибка: Пользователь с таким логином уже существует.";
            }

            // По умолчанию регистрируем как CLIENT
            Role clientRole = roleDao.findByName(session, "CLIENT");
            if (clientRole == null) {
                return "Ошибка: Роль CLIENT не найдена в базе данных.";
            }

            String hashedPassword = HashUtil.hashPassword(password);
            User user = new User(login, hashedPassword, clientRole);
            userDao.save(session, user);

            // Создаем запись пассажира
            Passenger passenger = new Passenger();
            passenger.setFirstName(firstName);
            passenger.setLastName(lastName);
            passenger.setPassportNumber(passportNum);
            passenger.setUser(user);
            passengerDao.save(session, passenger);

            transaction.commit();
            return "Успех: Вы успешно зарегистрировались.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return "Ошибка сервера при регистрации: " + e.getMessage();
        }
    }


    public User login(String login, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = userDao.findByLogin(session, login);
            String hashedPassword = HashUtil.hashPassword(password);
            if (user != null && user.getPassword().equals(hashedPassword)) {
                if (user.isBlocked()) return null; // Или можно бросить исключение
                return user;
            }
            return null;
        }
    }
}
