module java_pi{
    requires javafx.controls;
requires java.sql;
requires javafx.fxml;
requires javafx.base;
requires javafx.graphics;
    requires spring.security.crypto;
    requires java.desktop;
    requires stripe.java;
    requires okhttp3;
    requires com.google.gson;
    requires jakarta.mail;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    exports tn.esprit.controllers;
    exports tn.esprit.services;
    exports tn.esprit.models;
    exports tn.esprit.test;
    opens tn.esprit.controllers to javafx.fxml;



}
