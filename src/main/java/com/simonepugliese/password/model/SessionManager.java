package com.simonepugliese.password.model;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class SessionManager {
    private static SecretKey sessionKey;
    private static String loggedUsername;

    public static void setKey(SecretKey key) {
        sessionKey = key;
    }

    public static SecretKey getKey() {
        return sessionKey;
    }

    public static void clearKey() {
        if (sessionKey != null) {
            Arrays.fill(sessionKey.getEncoded(), (byte) 0);
            sessionKey = null;
        }
        loggedUsername = null;
    }

    public static boolean hasActiveSession() {
        return sessionKey != null;
    }

    public static void setLoggedUsername(String username) {
        loggedUsername = username;
    }

    public static String getLoggedUsername() {
        return loggedUsername;
    }
}
