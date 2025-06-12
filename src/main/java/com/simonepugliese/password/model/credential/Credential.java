package com.simonepugliese.password.model.credential;

public interface Credential {
    int getId();
    void setId(int id);
    String getLabel();
    byte[] getEncrypted();
    byte[] getIv();
    byte[] getSalt();
    CredentialType getType();
    String getOwnerUsername();
    void setOwnerUsername(String ownerUsername);
}
