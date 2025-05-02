module java_pi{
    requires javafx.controls;
requires java.sql;
requires javafx.fxml;
    requires spring.security.crypto;
    requires java.mail;
    requires kernel;
    requires layout;
    requires io;
    requires org.json;
    exports tn.esprit.controllers;
    exports tn.esprit.services;
    exports tn.esprit.models;
    exports tn.esprit.test;
    opens tn.esprit.controllers to javafx.fxml;

    opens tn.esprit.controllers.Formation to javafx.fxml; // 👈 ligne nécessaire pour résoudre ton erreur
    opens tn.esprit.controllers.category to javafx.fxml;
    opens tn.esprit.controllers.lecon to javafx.fxml;
}
