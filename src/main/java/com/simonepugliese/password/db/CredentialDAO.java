package com.simonepugliese.password.db;

import com.simonepugliese.password.model.credential.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CredentialDAO {
    private static final Logger logger = LoggerFactory.getLogger(CredentialDAO.class);

    private final Connection connection;

    public CredentialDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(Credential c) throws SQLException {
        String sql = "INSERT INTO credentials(label, type, username, encrypted_secret, salt, iv, owner_username) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getLabel());
            stmt.setString(2, c.getType().name());
            stmt.setString(3, (c instanceof WebCredential wc) ? wc.getUsername() :
                    (c instanceof AppCredential ac) ? ac.getUsername() : null);
            stmt.setBytes(4, c.getEncrypted());
            stmt.setBytes(5, c.getSalt());
            stmt.setBytes(6, c.getIv());
            stmt.setString(7, c.getOwnerUsername());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
                logger.info("Inserita credenziale con label " + c.getLabel() + " dall'utente " + c.getOwnerUsername());
            }
        }
    }

    public List<Credential> findAllForUser(String ownerUsername) throws SQLException {
        String sql = "SELECT * FROM credentials WHERE owner_username = ?";
        List<Credential> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String label = rs.getString("label");
                    String type = rs.getString("type");
                    String username = rs.getString("username");
                    byte[] secret = rs.getBytes("encrypted_secret");
                    byte[] salt = rs.getBytes("salt");
                    byte[] iv = rs.getBytes("iv");
                    String owner = rs.getString("owner_username");

                    Credential c = switch (CredentialType.valueOf(type)) {
                        case WEB -> new WebCredential(id, label, username, secret, salt, iv, owner);
                        case APP -> new AppCredential(id, label, username, secret, salt, iv, owner);
                        case WIFI -> new WifiCredential(id, label, secret, salt, iv, owner);
                    };
                    result.add(c);
                }
            }
        }
        logger.info("Richieste tutte le credenziali di " + ownerUsername + " con la funzione findAllForUser");
        return result;
    }

    public void update(Credential c) throws SQLException {
        String sql = "UPDATE credentials SET label = ?, type = ?, username = ?, encrypted_secret = ?, salt = ?, iv = ?, owner_username = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, c.getLabel());
            stmt.setString(2, c.getType().name());
            stmt.setString(3, (c instanceof WebCredential wc) ? wc.getUsername() :
                    (c instanceof AppCredential ac) ? ac.getUsername() : null);
            stmt.setBytes(4, c.getEncrypted());
            stmt.setBytes(5, c.getSalt());
            stmt.setBytes(6, c.getIv());
            stmt.setString(7, c.getOwnerUsername());
            stmt.setInt(8, c.getId());
            stmt.executeUpdate();
            logger.info("Eseguito update sulla credenziale con id %d dell'utente ".formatted(c.getId()) + c.getOwnerUsername());
        }
    }

    public void delete(int id, String ownerUsername) throws SQLException {
        String sql = "DELETE FROM credentials WHERE id = ? AND owner_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, ownerUsername);
            stmt.executeUpdate();
            logger.info("Eliminata credenziale con id %d dell'utente ".formatted(id) + ownerUsername);
        }
    }
}
