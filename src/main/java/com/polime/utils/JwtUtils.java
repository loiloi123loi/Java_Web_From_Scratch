package com.polime.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtils {
    private static String accessSecret;
    private static String refreshSecret;
    private static String emailVerifySecret;
    private static long accessExpiration;
    private static long refreshExpiration;
    private static long emailVerifyExpiration;

    public static void init(String accessSec, long accessExp, String refreshSec, long refreshExp, String emailSec,
            long emailExp) {
        accessSecret = accessSec;
        accessExpiration = accessExp;
        refreshSecret = refreshSec;
        refreshExpiration = refreshExp;
        emailVerifySecret = emailSec;
        emailVerifyExpiration = emailExp;
    }

    public static String signAccessToken(Long userId, String verifyStatus) {
        return createToken(userId, verifyStatus, accessSecret, accessExpiration);
    }

    public static String signRefreshToken(Long userId, String verifyStatus) {
        return createToken(userId, verifyStatus, refreshSecret, refreshExpiration);
    }

    public static String signEmailVerifyToken(Long userId, String verifyStatus) {
        return createToken(userId, verifyStatus, emailVerifySecret, emailVerifyExpiration);
    }

    private static String createToken(Long userId, String verifyStatus, String secret, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().subject(userId.toString()).claim("verify", verifyStatus).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(key).compact();
    }

    public static Claims decodeToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public static String getAccessSecret() {
        return accessSecret;
    }

    public static String getRefreshSecret() {
        return refreshSecret;
    }
}
