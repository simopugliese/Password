package com.simonepugliese.password.model.user;

public class User {
    private final String username;
    private final byte[] salt;
    private final byte[] verifier;

    public User(String username, byte[] salt, byte[] verifier) {
        this.username = username;
        this.salt = salt;
        this.verifier = verifier;
    }

    public String getUsername() { return username; }
    public byte[] getSalt() { return salt; }
    public byte[] getVerifier() { return verifier; }
}
