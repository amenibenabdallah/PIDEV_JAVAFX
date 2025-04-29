module tn.esprit{
    requires javafx.controls;
requires java.sql;
requires javafx.fxml;
    requires spring.security.crypto;
    requires twilio;
    requires java.mail;
    requires google.api.client;
    requires java.desktop;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.client;
    exports tn.esprit.controllers;
    exports tn.esprit.services;
    exports tn.esprit.models;
    exports tn.esprit.test;
    opens tn.esprit.controllers to javafx.fxml;



}
