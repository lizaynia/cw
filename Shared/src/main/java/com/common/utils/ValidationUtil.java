package com.common.utils;

public class ValidationUtil {

    public static boolean isNotEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        String trimmed = login.trim();
        // Только буквы, цифры, подчеркивание, дефис. Длина 3-20
        return trimmed.length() >= 3 && trimmed.length() <= 20 && trimmed.matches("^[a-zA-Z0-9_-]+$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        String trimmed = password.trim();
        // Пароль: минимум 4 символа
        return trimmed.length() >= 4;
    }

    public static boolean isValidPassport(String passport) {
        if (passport == null || passport.trim().isEmpty()) {
            return false;
        }
        String trimmed = passport.trim();
        // Паспорт: только буквы и цифры, длина 7-15
        return trimmed.length() >= 7 && trimmed.length() <= 15 && trimmed.matches("^[A-Za-z0-9]+$");
    }
}