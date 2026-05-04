package com.server.service;

import com.common.entity.Airplane;
import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.AirplaneDao;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AdminService {
    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();
    private final AirplaneDao airplaneDao = new AirplaneDao();

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

    public String updateAirplaneStatus(Integer airplaneId, Airplane.AirplaneStatus newStatus) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Airplane airplane = airplaneDao.findById(session, airplaneId);
            if (airplane == null) return "Ошибка: Самолет не найден.";
            
            airplane.setStatus(newStatus);
            airplaneDao.update(session, airplane);
            
            transaction.commit();
            return "Успех: Статус самолета изменен на " + newStatus;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return "Ошибка обновления статуса: " + e.getMessage();
        }
    }

    public java.util.Map<String, Long> getStatistics() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put("users", (long) userDao.findAll(session).size());
            stats.put("airplanes", (long) airplaneDao.findAll(session).size());
            // Можно добавить более сложные запросы через DAO
            return stats;
        }
    }
}
