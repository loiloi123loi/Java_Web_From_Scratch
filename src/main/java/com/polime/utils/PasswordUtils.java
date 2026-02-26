package com.polime.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {
    private PasswordUtils() {
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String hashed) {
        try {
            return BCrypt.checkpw(password, hashed);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
