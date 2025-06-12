package com.simonepugliese.password.gui;

import com.simonepugliese.password.model.credential.*;
import com.simonepugliese.password.model.CryptoManager;
import com.simonepugliese.password.model.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialDialog extends Dialog<Credential> {
    private static final Logger logger = LoggerFactory.getLogger(CredentialDialog.class);

    private final TextField labelField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<CredentialType> typeCombo = new ComboBox<>();

    private Credential existingCredential;

    public CredentialDialog(Credential credential) {
        try{
            Image icon = new Image(getClass().getResourceAsStream("/com/simonepugliese/password/icon.jpeg"));
            ((Stage) getDialogPane().getScene().getWindow()).getIcons().add(icon);
        } catch (Exception e){
            logger.error("icon.jpeg è null?? " + e.getMessage());
        }

        this.existingCredential = credential;

        setTitle(credential == null ? "Aggiungi Credenziale" : "Modifica Credenziale");
        setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(20, 70, 10, 10));
        grid.getStyleClass().add("credential-form");

        typeCombo.getItems().setAll(CredentialType.values());
        typeCombo.setValue(CredentialType.WEB);
        typeCombo.getStyleClass().add("combo-box");

        labelField.getStyleClass().add("text-field");
        usernameField.getStyleClass().add("text-field");
        passwordField.getStyleClass().add("text-field");

        Label typeLabel = new Label("Tipo:");
        Label labelLabel = new Label("Etichetta:");
        Label userLabel = new Label("Username:");
        Label passLabel = new Label("Password:");

        typeLabel.getStyleClass().add("label");
        labelLabel.getStyleClass().add("label");
        userLabel.getStyleClass().add("label");
        passLabel.getStyleClass().add("label");

        grid.add(typeLabel, 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(labelLabel, 0, 1);
        grid.add(labelField, 1, 1);
        grid.add(userLabel, 0, 2);
        grid.add(usernameField, 1, 2);
        grid.add(passLabel, 0, 3);
        grid.add(passwordField, 1, 3);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/com/simonepugliese/password/gui/style.css").toExternalForm());

        typeCombo.setOnAction(e -> updateUsernameField());
        updateUsernameField();

        if (credential != null) {
            typeCombo.setValue(credential.getType());
            labelField.setText(credential.getLabel());
            if (credential instanceof WebCredential wc) {
                usernameField.setText(wc.getUsername());
            } else if (credential instanceof AppCredential ac) {
                usernameField.setText(ac.getUsername());
            } else {
                usernameField.setText("");
            }
            passwordField.setPromptText("Lascia vuoto per mantenere");
        }

        setResultConverter(new Callback<ButtonType, Credential>() {
            @Override
            public Credential call(ButtonType buttonType) {
                if (buttonType == saveButtonType) {
                    return createOrUpdateCredential();
                }
                return null;
            }
        });
    }

    private void updateUsernameField() {
        CredentialType selected = typeCombo.getValue();
        if (selected == CredentialType.WEB || selected == CredentialType.APP) {
            usernameField.setDisable(false);
        } else {
            usernameField.clear();
            usernameField.setDisable(true);
        }
    }

    private Credential createOrUpdateCredential() {
        String label = labelField.getText();
        String username = usernameField.getText();
        String pwd = passwordField.getText();

        byte[] encrypted;
        byte[] salt;
        byte[] iv;

        try {
            if (existingCredential != null && (pwd == null || pwd.isEmpty())) {
                encrypted = existingCredential.getEncrypted();
                salt = existingCredential.getSalt();
                iv = existingCredential.getIv();
            } else {
                salt = CryptoManager.generateSalt();
                iv = CryptoManager.generateIV();
                encrypted = CryptoManager.encrypt(pwd.getBytes(), SessionManager.getKey(), iv);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

        CredentialType type = typeCombo.getValue();

        if (existingCredential != null) {
            int id = existingCredential.getId();
            switch (type) {
                case WEB -> {
                    logger.info("Modificata credenziale WEB{ID = %d} dell'utente ".formatted(id) + existingCredential.getOwnerUsername());
                    return new WebCredential(id, label, username, encrypted, salt, iv, SessionManager.getLoggedUsername());
                }
                case APP -> {
                    logger.info("Modificata credenziale APP{ID = %d} dell'utente ".formatted(id) + existingCredential.getOwnerUsername());
                    return new AppCredential(id, label, username, encrypted, salt, iv, SessionManager.getLoggedUsername());
                }
                case WIFI -> {
                    logger.info("Modificata credenziale WIFI{ID = %d} dell'utente ".formatted(id) + existingCredential.getOwnerUsername());
                    return new WifiCredential(id, label, encrypted, salt, iv, SessionManager.getLoggedUsername());
                }
            }
        } else {
            switch (type) {
                case WEB -> {
                    logger.info("Aggiunta credenziale WEB{LABEL = %s} dell'utente ".formatted(label) + SessionManager.getLoggedUsername());
                    return new WebCredential(label, username, encrypted, salt, iv, SessionManager.getLoggedUsername());
                }
                case APP -> {
                    logger.info("Aggiunta credenziale APP{LABEL = %s} dell'utente ".formatted(label) + SessionManager.getLoggedUsername());
                    return new AppCredential(label, username, encrypted, salt, iv, SessionManager.getLoggedUsername());
                }
                case WIFI -> {
                    logger.info("Aggiunta credenziale WIFI{LABEL = %s} dell'utente ".formatted(label) + SessionManager.getLoggedUsername());
                    return new WifiCredential(label, encrypted, salt, iv, SessionManager.getLoggedUsername());
                }
            }
        }
        return null;
    }
}
