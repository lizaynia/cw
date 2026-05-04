package com.common.utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z0-9]{7,15}$");

    public static boolean isValidLogin(String login) {
        return login != null && login.length() >= 3 && login.length() <= 20;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    public static boolean isValidPassport(String passport) {
        return passport != null && PASSPORT_PATTERN.matcher(passport).matches();
    }

    public static boolean isNotEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
