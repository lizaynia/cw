package com.server.service;

import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import org.example.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AdminService {
    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();

    public List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return userDao.findAll(session);
        }
    }

    public String changeUserRole(Integer userId, String newRoleName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = userDao.findById(session, userId);
            if (user == null) return "Ошибка: Пользователь не найден.";

            Role role = roleDao.findByName(session, newRoleName);
            if (role == null) return "Ошибка: Роль не найдена.";

            user.setRole(role);
            userDao.update(session, user);

            transaction.commit();
            return "Успех: Роль пользователя обновлена.";
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка смены роли: " + e.getMessage();
        }
    }
}
