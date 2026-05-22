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
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class AuthService {
    private final SessionFactory sessionFactory;
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PassengerDao passengerDao;

    // Конструктор для тестов
    public AuthService(SessionFactory sessionFactory, UserDao userDao, RoleDao roleDao, PassengerDao passengerDao) {
        this.sessionFactory = sessionFactory;
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passengerDao = passengerDao;
    }

    // Конструктор для продакшена
    public AuthService() {
        this(HibernateUtil.getSessionFactory(), new UserDao(), new RoleDao(), new PassengerDao());
    }

    public String register(String login, String password, String firstName, String lastName, String passportNum) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            if (userDao.findByLogin(session, login) != null) {
                return "Ошибка: Пользователь с таким логином уже существует.";
            }

            Role clientRole = roleDao.findByName(session, "CLIENT");
            if (clientRole == null) {
                return "Ошибка: Роль CLIENT не найдена в базе данных.";
            }

            String hashedPassword = HashUtil.hashPassword(password);
            User user = new User(login, hashedPassword, clientRole);
            userDao.save(session, user);

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
        try (Session session = sessionFactory.openSession()) {
            User user = userDao.findByLogin(session, login);
            String hashedPassword = HashUtil.hashPassword(password);
            if (user != null && user.getPassword().equals(hashedPassword)) {
                if (user.isBlocked()) return null;
                return user;
            }
            return null;
        }
    }

    public String updatePassword(Integer userId, String newPassword) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = userDao.findById(session, userId);
            if (user == null) {
                return "Ошибка: Пользователь не найден.";
            }
            String hashedPassword = HashUtil.hashPassword(newPassword);
            user.setPassword(hashedPassword);
            userDao.update(session, user);
            transaction.commit();
            return "Успех: Пароль успешно изменён.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка смены пароля: " + e.getMessage();
        }
    }
}