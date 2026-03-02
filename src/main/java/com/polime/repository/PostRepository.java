package com.polime.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.polime.core.DatabaseManager;
import com.polime.enums.EPostAudience;
import com.polime.enums.EPostType;
import com.polime.model.Post;

public class PostRepository {
    private static final String TABLE_NAME = "posts";

    public void initTable() throws SQLException {
        try (Statement stmt = DatabaseManager.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT NOT NULL, " + "type VARCHAR(50) NOT NULL, " + "audience VARCHAR(50) NOT NULL, "
                    + "content TEXT, " + "parent_id BIGINT, " + "created_at TIMESTAMP, " + "updated_at TIMESTAMP)");
        }
    }

    public Post save(Post post) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME
                + " (user_id, type, audience, content, parent_id, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, post.getUserId());
            pstmt.setString(2, post.getType().name());
            pstmt.setString(3, post.getAudience().name());
            pstmt.setString(4, post.getContent());
            if (post.getParentId() != null) {
                pstmt.setLong(5, post.getParentId());
            } else {
                pstmt.setNull(5, Types.BIGINT);
            }
            pstmt.setTimestamp(6, Timestamp.from(post.getCreatedAt().toInstant()));
            pstmt.setTimestamp(7, Timestamp.from(post.getUpdatedAt().toInstant()));

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                }
            }
        }
        return post;
    }

    public Post findById(Long id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        }
        return null;
    }

    private Post mapResultSetToModel(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setUserId(rs.getLong("user_id"));
        post.setType(EPostType.valueOf(rs.getString("type")));
        post.setAudience(EPostAudience.valueOf(rs.getString("audience")));
        post.setContent(rs.getString("content"));
        post.setParentId(rs.getObject("parent_id", Long.class));
        Timestamp createdTs = rs.getTimestamp("created_at");
        post.setCreatedAt(
                createdTs != null ? OffsetDateTime.ofInstant(createdTs.toInstant(), ZoneId.systemDefault()) : null);

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        post.setUpdatedAt(
                updatedTs != null ? OffsetDateTime.ofInstant(updatedTs.toInstant(), ZoneId.systemDefault()) : null);
        return post;
    }
}
