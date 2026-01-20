package com.polime.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.polime.core.DatabaseManager;
import com.polime.enums.EUserVerifyStatus;
import com.polime.model.User;

public class UserRepository {
	private static final String TABLE_NAME = "users";

	public UserRepository() {
	}

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public void initTable() throws SQLException {
		try (Statement stmt = getConnection().createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
					+ "name VARCHAR(255), " + "username VARCHAR(255), " + "email VARCHAR(255) UNIQUE, "
					+ "date_of_birth DATE, " + "password VARCHAR(255), " + "created_at TIMESTAMP, "
					+ "updated_at TIMESTAMP, " + "verify_status VARCHAR(50), " + "email_verify_token VARCHAR(255), "
					+ "forgot_password_token VARCHAR(255))");
		}
	}

	public User save(User user) throws SQLException {
		String sql = "INSERT INTO " + TABLE_NAME
				+ " (name, username, email, date_of_birth, password, created_at, updated_at, verify_status, email_verify_token, forgot_password_token) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, user.getName());
			pstmt.setString(2, user.getUsername());
			pstmt.setString(3, user.getEmail());
			pstmt.setDate(4, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
			pstmt.setString(5, user.getPassword());
			pstmt.setTimestamp(6, user.getCreatedAt() != null ? Timestamp.from(user.getCreatedAt().toInstant()) : null);
			pstmt.setTimestamp(7, user.getUpdatedAt() != null ? Timestamp.from(user.getUpdatedAt().toInstant()) : null);
			pstmt.setString(8, user.getVerifyStatus() != null ? user.getVerifyStatus().name() : null);
			pstmt.setString(9, user.getEmailVerifyToken());
			pstmt.setString(10, user.getForgotPasswordToken());

			pstmt.executeUpdate();

			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next()) {
					user.setId(rs.getLong(1));
				}
			}
		}
		return user;
	}

	public void update(User user) throws SQLException {
		String sql = "UPDATE " + TABLE_NAME
				+ " SET name = ?, username = ?, email = ?, date_of_birth = ?, password = ?, updated_at = ?, verify_status = ?, email_verify_token = ?, forgot_password_token = ? WHERE id = ?";
		try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
			pstmt.setString(1, user.getName());
			pstmt.setString(2, user.getUsername());
			pstmt.setString(3, user.getEmail());
			pstmt.setDate(4, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
			pstmt.setString(5, user.getPassword());
			pstmt.setTimestamp(6, user.getUpdatedAt() != null ? Timestamp.from(user.getUpdatedAt().toInstant()) : null);
			pstmt.setString(7, user.getVerifyStatus() != null ? user.getVerifyStatus().name() : null);
			pstmt.setString(8, user.getEmailVerifyToken());
			pstmt.setString(9, user.getForgotPasswordToken());
			pstmt.setLong(10, user.getId());
			pstmt.executeUpdate();
		}
	}

	public User findByEmail(String email) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";
		try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
			pstmt.setString(1, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToUser(rs);
				}
			}
		}
		return null;
	}

	public User findById(Long id) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
		try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
			pstmt.setLong(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToUser(rs);
				}
			}
		}
		return null;
	}

	public List<User> findAll() throws SQLException {
		List<User> users = new ArrayList<>();
		try (Statement stmt = getConnection().createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME);
			while (rs.next()) {
				users.add(mapResultSetToUser(rs));
			}
		}
		return users;
	}

	private User mapResultSetToUser(ResultSet rs) throws SQLException {
		Date sqlDate = rs.getDate("date_of_birth");
		LocalDate dob = (sqlDate != null) ? sqlDate.toLocalDate() : null;

		String statusStr = rs.getString("verify_status");
		EUserVerifyStatus status = (statusStr != null) ? EUserVerifyStatus.valueOf(statusStr)
				: EUserVerifyStatus.Unverified;

		Timestamp createdTs = rs.getTimestamp("created_at");
		OffsetDateTime createdAt = (createdTs != null)
				? OffsetDateTime.ofInstant(createdTs.toInstant(), ZoneId.systemDefault())
				: null;

		Timestamp updatedTs = rs.getTimestamp("updated_at");
		OffsetDateTime updatedAt = (updatedTs != null)
				? OffsetDateTime.ofInstant(updatedTs.toInstant(), ZoneId.systemDefault())
				: null;

		return new User(rs.getLong("id"), rs.getString("name"), rs.getString("username"), rs.getString("email"), dob,
				rs.getString("password"), createdAt, updatedAt, rs.getString("email_verify_token"),
				rs.getString("forgot_password_token"), status);
	}
}
