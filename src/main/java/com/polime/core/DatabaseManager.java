package com.polime.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static String url;
    private static String username;
    private static String password;
    private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    public static void init(String url, String username, String password) {
        DatabaseManager.url = url;
        DatabaseManager.username = username;
        DatabaseManager.password = password;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(url, username, password);
            threadLocalConnection.set(conn);
        }
        return conn;
    }

    public static void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    public static void commit() throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (conn != null && !conn.isClosed()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public static void rollback() {
        Connection conn = threadLocalConnection.get();
        try {
            if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Rollback failed: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        Connection conn = threadLocalConnection.get();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Close connection failed: " + e.getMessage());
        } finally {
            threadLocalConnection.remove();
        }
    }
}
