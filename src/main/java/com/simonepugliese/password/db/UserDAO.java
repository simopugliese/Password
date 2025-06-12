package com.simonepugliese.password.db;

import com.simonepugliese.password.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, salt, verifier) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setBytes(2, user.getSalt());
            stmt.setBytes(3, user.getVerifier());
            stmt.executeUpdate();
            logger.info("Inserito User " + user.getUsername());
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT username, salt, verifier FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                byte[] salt = rs.getBytes("salt");
                byte[] verifier = rs.getBytes("verifier");
                logger.info("Richiesti i dati dell'user " + username);
                return new User(username, salt, verifier);
            } else {
                return null;
            }
        }
    }
}

