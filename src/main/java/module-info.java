module com.simonepugliese.password {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;
    requires java.desktop;

    opens com.simonepugliese.password to javafx.fxml;
    opens com.simonepugliese.password.gui to javafx.fxml;

    exports com.simonepugliese.password;
    exports com.simonepugliese.password.model;
    exports com.simonepugliese.password.model.credential;
    exports com.simonepugliese.password.model.user;
    exports com.simonepugliese.password.db;
    exports com.simonepugliese.password.cli;
    exports com.simonepugliese.password.gui;
    exports com.simonepugliese.password.util;
}
