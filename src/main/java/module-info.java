module tn.esprit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;
    requires spring.security.crypto;
    requires org.json;
    requires java.net.http;
    requires java.mail;
    requires java.desktop;
    requires stripe.java;
    requires okhttp3;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires com.google.protobuf;
    requires twilio;
    requires com.google.api.client;
    requires com.google.api.client.json.jackson2;
    requires google.api.client;
    requires com.google.zxing;        // From block 1
    requires com.google.zxing.javase; // From block 1

    exports tn.esprit.controllers;
    exports tn.esprit.services;
    exports tn.esprit.models;
    exports tn.esprit.test;
    exports tn.esprit.utils;         // From block 1

    opens tn.esprit.controllers to javafx.fxml;
    opens tn.esprit.services to javafx.fxml; // From block 1
    opens tn.esprit.models to javafx.fxml;   // From block 1
    opens tn.esprit.utils to javafx.fxml;    // From block 1
}