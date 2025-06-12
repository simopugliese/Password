package com.simonepugliese.password;

import com.simonepugliese.password.cli.ConsoleUI;
import com.simonepugliese.password.gui.GuiApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        /*if (args.length > 0 && args[0].equalsIgnoreCase("cli")) {
            new ConsoleUI().start();
        } else {
            logger.info("GUI avviata da main");
        }*/
        GuiApp.main(args);
    }
}


/*
java --module-path "target/Password-1.0-SNAPSHOT.jar;C:\Users\simon\IdeaProjects\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml -m com.simonepugliese.password/com.simonepugliese.password.Main
 */
