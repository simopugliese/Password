package com.simonepugliese.password.model.credential;

public class WebCredential implements Credential {
    private int id;
    private final String url;
    private final String username;
    private final byte[] encryptedPassword;
    private final byte[] salt;
    private final byte[] iv;
    private String ownerUsername;

    public WebCredential(String url, String username, byte[] encryptedPassword, byte[] salt, byte[] iv, String ownerUsername) {
        this.url = url;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.iv = iv;
        this.ownerUsername = ownerUsername;
    }

    public WebCredential(int id, String url, String username, byte[] encryptedPassword, byte[] salt, byte[] iv, String ownerUsername) {
        this.id = id;
        this.url = url;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.iv = iv;
        this.ownerUsername = ownerUsername;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    @Override public String getLabel() { return url; }
    @Override public CredentialType getType() { return CredentialType.WEB; }

    public String getUsername() { return username; }

    @Override public byte[] getEncrypted() { return encryptedPassword;}
    @Override public byte[] getIv() { return iv; }
    @Override public byte[] getSalt() { return salt; }

    @Override
    public String getOwnerUsername() { return ownerUsername; }
    @Override
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
