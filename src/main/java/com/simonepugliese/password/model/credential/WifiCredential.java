package com.simonepugliese.password.model.credential;

public class WifiCredential implements Credential {
    private int id;
    private final String ssid;
    private final byte[] encryptedPassword;
    private final byte[] salt;
    private final byte[] iv;
    private String ownerUsername;

    public WifiCredential(String ssid, byte[] encryptedPassword, byte[] salt, byte[] iv, String ownerUsername) {
        this.ssid = ssid;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.iv = iv;
        this.ownerUsername = ownerUsername;
    }

    public WifiCredential(int id, String ssid, byte[] encryptedPassword, byte[] salt, byte[] iv, String ownerUsername) {
        this.id = id;
        this.ssid = ssid;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.iv = iv;
        this.ownerUsername = ownerUsername;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    @Override public String getLabel() { return ssid; }
    @Override public byte[] getEncrypted() { return encryptedPassword; }
    @Override public CredentialType getType() { return CredentialType.WIFI; }

    @Override public byte[] getSalt() { return salt; }
    @Override public byte[] getIv() { return iv; }

    @Override
    public String getOwnerUsername() { return ownerUsername; }
    @Override
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}

