package com.server;

import com.common.entity.Role;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class DbTest {
    public static void main(String[] args) {
        System.out.println("Тестирование подключения к БД...");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Role> roles = session.createQuery("from Role", Role.class).list();
            System.out.println("Подключение успешно!");
            System.out.println("Найдено ролей в БД: " + roles.size());
            for (Role role : roles) {
                System.out.println(" - " + role.getRoleName());
            }
        } catch (Exception e) {
            System.err.println("ОШИБКА ПОДКЛЮЧЕНИЯ:");
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
