module tn.esprit{
       requires javafx.controls;
    requires java.sql;
    requires javafx.fxml;
    requires spring.security.crypto;
    requires mysql.connector.j;
    requires com.google.protobuf;
    requires twilio;
    requires com.google.api.client;
    requires google.api.client;
    requires java.desktop;
    requires com.google.api.client.json.jackson2;
    requires java.mail;
    exports tn.esprit.controllers;
    exports tn.esprit.services;
    exports tn.esprit.models;
    exports tn.esprit.test;
    opens tn.esprit.controllers to javafx.fxml;



}
