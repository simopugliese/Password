package com.simonepugliese.password.gui;

import com.simonepugliese.password.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        char[] pwd = passwordField.getText().toCharArray();
        char[] confirmPwd = confirmPasswordField.getText().toCharArray();

        if (!new String(pwd).equals(new String(confirmPwd))) {
            messageLabel.setText("Le password non corrispondono.");
            return;
        }

        try {
            AuthService.registerUser(username, pwd);
            logger.info("Registrato con successo utente " + username);
            messageLabel.setText("Registrazione completata. Ora fai il login.");
        } catch (Exception e) {
            logger.error("Errore: " + e.getMessage());
            messageLabel.setText("Errore: " + e.getMessage());
        } finally {
            java.util.Arrays.fill(pwd, '\0');
            java.util.Arrays.fill(confirmPwd, '\0');
        }
    }
}
