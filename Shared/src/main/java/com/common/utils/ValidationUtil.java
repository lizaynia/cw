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
        return login != null && login.length() >= 3 && login.length() <= 20;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    public static boolean isValidPassport(String passport) {
        return passport != null && passport.matches("^[A-Za-z0-9]{7,15}$");
    }
}