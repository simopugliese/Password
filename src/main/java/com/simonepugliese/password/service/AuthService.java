package com.simonepugliese.password.service;

import com.simonepugliese.password.db.DatabaseManager;
import com.simonepugliese.password.db.UserDAO;
import com.simonepugliese.password.model.user.User;
import com.simonepugliese.password.model.CryptoManager;
import com.simonepugliese.password.model.SessionManager;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class AuthService {

    public static void registerUser(String username, char[] masterPassword) throws Exception {
        byte[] salt = CryptoManager.generateSalt();
        SecretKey key = CryptoManager.deriveKey(masterPassword, salt);
        byte[] verifier = CryptoManager.encrypt("check".getBytes(), key, salt); // salt usato come IV

        User user = new User(username, salt, verifier);
        new UserDAO(DatabaseManager.getConnection()).insert(user);

        Arrays.fill(masterPassword,'\0');
    }

    public static boolean loginUser(String username, char[] masterPassword) throws Exception {
        UserDAO userDAO = new UserDAO(DatabaseManager.getConnection());
        User user = userDAO.findByUsername(username);
        if (user == null) return false;

        SecretKey key = CryptoManager.deriveKey(masterPassword, user.getSalt());

        try {
            byte[] decrypted = CryptoManager.decrypt(user.getVerifier(), key, user.getSalt());
            if (new String(decrypted).equals("check")) {
                SessionManager.setKey(key);
                SessionManager.setLoggedUsername(username);
                return true;
            }
        } catch (Exception ignored) {}

        Arrays.fill(masterPassword,'\0');
        return false;
    }
}

