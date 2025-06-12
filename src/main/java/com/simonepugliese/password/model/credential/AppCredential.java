package com.simonepugliese.password.model.credential;

public class AppCredential implements Credential {
    private int id;
    private final String appName;
    private final String username;
    private final byte[] encryptedPassword;
    private final byte[] salt;
    private final byte[] iv;
    private String ownerUsername;

    public AppCredential(String appName, String username, byte[] encryptedPassword, byte[] salt, byte[] iv, String ownerUsername) {
        this.appName = appName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.iv = iv;
        this.ownerUsername = ownerUsername;
    }

    public AppCredential(int id, String appName, String username, byte[] encryptedPassword, byte[] salt, byte[] iv, String ownerUsername) {
        this.id = id;
        this.appName = appName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.iv = iv;
        this.ownerUsername = ownerUsername;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    @Override public String getLabel() { return appName; }
    @Override public byte[] getEncrypted() { return encryptedPassword; }
    @Override public CredentialType getType() { return CredentialType.APP; }

    public String getUsername() { return username; }

    @Override public byte[] getSalt() { return salt; }
    @Override public byte[] getIv() { return iv; }

    @Override
    public String getOwnerUsername() { return ownerUsername; }
    @Override
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}

