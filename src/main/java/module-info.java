module java_pi{
    requires javafx.controls;
requires java.sql;
requires javafx.fxml;
    requires spring.security.crypto;
    exports tn.esprit.controllers;
    exports tn.esprit.services;
    exports tn.esprit.models;
    exports tn.esprit.test;
    opens tn.esprit.controllers to javafx.fxml;



}
