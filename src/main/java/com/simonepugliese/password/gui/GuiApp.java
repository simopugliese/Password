package com.simonepugliese.password.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GuiApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(GuiApp.class);

    private Stage primaryStage;
    private LoginController loginController;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        primaryStage.getIcons().add(new Image(GuiApp.class.getResourceAsStream("/com/simonepugliese/password/icon.jpeg")));
        showLoginScreen();
        logger.info("Chiamata a start(Stage stage)");
    }

    private void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/simonepugliese/password/gui/login_screen.fxml"));
        Parent root = loader.load();

        loginController = loader.getController();
        loginController.setOnLoginSuccess(() -> {
            try {
                String user = loginController.getLoggedUsername();
                showMainScreen(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        primaryStage.setTitle("Password Manager - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        logger.info("Viene mostrato il Login Screen");
        primaryStage.getIcons().add(new Image(GuiApp.class.getResourceAsStream("/com/simonepugliese/password/icon.jpeg")));
    }

    private void showMainScreen(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/simonepugliese/password/gui/main_screen.fxml"));
        Parent root = loader.load();

        GuiController mainController = loader.getController();
        mainController.setLoggedUsername(username);
        mainController.loadData();

        primaryStage.setTitle("Password Manager - Main");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        logger.info("Viene mostrato il Main Screen");
        primaryStage.getIcons().add(new Image(GuiApp.class.getResourceAsStream("/com/simonepugliese/password/icon.jpeg")));
    }

    public static void main(String[] args) {
        launch(args);
        logger.info("GUI avviata");
    }
}
