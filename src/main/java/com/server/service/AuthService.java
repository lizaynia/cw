package com.server.service;

import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import com.server.utils.HashUtil;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();

    public String register(String login, String password) {
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

            transaction.commit();
            return "Успех: Вы успешно зарегистрировались.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return "Ошибка сервера при регистрации.";
        }
    }

    public User login(String login, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = userDao.findByLogin(session, login);
            String hashedPassword = HashUtil.hashPassword(password);
            if (user != null && user.getPassword().equals(hashedPassword)) {
                return user; // В реальном проекте возвращаем DTO, но для курсовой сойдет и Entity
            }
            return null;
        }
    }
}
