package com.simonepugliese.password.gui;

import com.simonepugliese.password.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private Runnable onLoginSuccess;
    private String loggedUsername;

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public String getLoggedUsername() {
        return loggedUsername;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        char[] password = passwordField.getText().toCharArray();

        try {
            boolean success = AuthService.loginUser(username, password);
            java.util.Arrays.fill(password, '\0');

            if (success) {
                loggedUsername = username;
                if (onLoginSuccess != null) onLoginSuccess.run();
            } else {
                messageLabel.setText("Username o password errati.");
            }
        } catch (Exception e) {
            logger.error("Errore: " + e.getMessage());
            messageLabel.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/simonepugliese/password/gui/register_screen.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Registrazione Nuovo Utente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Errore apertura registrazione: " + e.getMessage());
            messageLabel.setText("Errore apertura registrazione: " + e.getMessage());
        }
    }
}
