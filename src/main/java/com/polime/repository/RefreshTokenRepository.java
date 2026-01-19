package com.polime.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.polime.core.DatabaseManager;
import com.polime.model.RefreshToken;

public class RefreshTokenRepository {
    private static final String TABLE_NAME = "refresh_tokens";

    public RefreshTokenRepository() {
    }

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public void initTable() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT, " + "token VARCHAR(500), " + "iat TIMESTAMP, " + "exp TIMESTAMP, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id)" + ")");
        }
    }

    public void save(RefreshToken token) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (user_id, token, iat, exp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, token.getUserId());
            pstmt.setString(2, token.getToken());
            pstmt.setTimestamp(3, Timestamp.from(token.getIat().toInstant()));
            pstmt.setTimestamp(4, Timestamp.from(token.getExp().toInstant()));
            pstmt.executeUpdate();
        }
    }

    public void deleteByToken(String token) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE token = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        }
    }

    public boolean existsByToken(String token) throws SQLException {
        String sql = "SELECT COUNT(1) FROM " + TABLE_NAME + " WHERE token = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, token);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
