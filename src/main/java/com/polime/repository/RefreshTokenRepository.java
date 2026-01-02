package com.polime.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.polime.core.DatabaseManager;
import com.polime.model.RefreshToken;

public class RefreshTokenRepository {
    public RefreshTokenRepository() {
    }

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public void initTable() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS refresh_tokens (" + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT, " + "token VARCHAR(500), " + "iat TIMESTAMP, " + "exp TIMESTAMP, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id)" + ")");
        }
    }

    public void save(RefreshToken token) throws SQLException {
        String sql = "INSERT INTO refresh_tokens (user_id, token, iat, exp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, token.getUserId());
            pstmt.setString(2, token.getToken());
            pstmt.setTimestamp(3, Timestamp.from(token.getIat().toInstant()));
            pstmt.setTimestamp(4, Timestamp.from(token.getExp().toInstant()));
            pstmt.executeUpdate();
        }
    }
}
