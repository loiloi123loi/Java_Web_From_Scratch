package com.polime.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBlacklist {
    private static final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public static void add(String token, long expiryTime) {
        revokedTokens.put(token, expiryTime);
    }

    public static boolean isBlacklisted(String token) {
        Long expiry = revokedTokens.get(token);
        if (expiry == null) {
            return false;
        }

        if (expiry < System.currentTimeMillis()) {
            revokedTokens.remove(token);
            return false;
        }

        return true;
    }
}
