package com.simonepugliese.password.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            initializeSchema();
            logger.info("Creata connessione");
        }
        return connection;
    }

    private static void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                salt BLOB NOT NULL,
                verifier BLOB NOT NULL
            );
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS credentials (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                label TEXT NOT NULL,
                type TEXT NOT NULL,
                username TEXT,
                encrypted_secret BLOB NOT NULL,
                salt BLOB NOT NULL,
                iv BLOB NOT NULL,
                owner_username TEXT NOT NULL
            );
        """);
        }
    }


    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            logger.info("Terminata connessione");
        }
    }
}

