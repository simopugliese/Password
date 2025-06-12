package com.simonepugliese.password.model;

import com.simonepugliese.password.model.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PasswordModel {
    private static final Logger logger = LoggerFactory.getLogger(PasswordModel.class);

    private final List<Credential> credentials = new ArrayList<>();

    public void addCredential(Credential credential) {
        credentials.add(credential);
        logger.info("Aggiunta credenziale");
    }

    public List<Credential> getAll() {
        logger.info("Richieste tutte le credenziali");
        return credentials;
    }

    public void clear() {
        logger.info("Pulite tutte le credenziali");
        credentials.clear();
    }
}

