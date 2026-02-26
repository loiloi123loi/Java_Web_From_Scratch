package com.polime.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public final class JwtUtils {
    private JwtUtils() {
    }

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

    public static String signRefreshToken(Long userId, String verifyStatus, Date expDate) {
        return createToken(userId, verifyStatus, refreshSecret, expDate);
    }

    public static String signEmailVerifyToken(Long userId, String verifyStatus) {
        return createToken(userId, verifyStatus, emailVerifySecret, emailVerifyExpiration);
    }

    private static String createToken(Long userId, String verifyStatus, String secret, long expirationMs) {
        long currentMs = System.currentTimeMillis();
        Date expDate = new Date(currentMs + expirationMs);
        return createToken(userId, verifyStatus, secret, expDate);
    }

    private static String createToken(Long userId, String verifyStatus, String secret, Date expDate) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(secretBytes);
        String subject = userId.toString();
        Date now = new Date();

        JwtBuilder builder = Jwts.builder();
        builder.subject(subject);
        builder.claim("verify", verifyStatus);
        builder.issuedAt(now);
        builder.expiration(expDate);
        builder.signWith(key);

        return builder.compact();
    }

    public static Claims decodeToken(String token, String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(secretBytes);

        JwtParserBuilder builder = Jwts.parser();
        builder.verifyWith(key);

        JwtParser parser = builder.build();
        Jws<Claims> jws = parser.parseSignedClaims(token);
        Claims payload = jws.getPayload();

        return payload;
    }

    public static String getAccessSecret() {
        return accessSecret;
    }

    public static String getRefreshSecret() {
        return refreshSecret;
    }

    public static String getEmailVerifySecret() {
        return emailVerifySecret;
    }
}
