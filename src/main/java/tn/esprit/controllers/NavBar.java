package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavBar {
    @FXML
    private Button btnFormations, btnEvents, btnProfile;

    @FXML
    private Pane pnItems;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        loadFXML("/Formation/GetAllFormationFront.fxml"); // page d'accueil par défaut
//    }

    @FXML
    public void handleClicks(ActionEvent event) {
        if (event.getSource() == btnFormations) {
            loadFXML("/Formation/GetAllFormationFront.fxml");
        } else if (event.getSource() == btnEvents) {
            loadFXML("/frontOffice/events.fxml");
        }
    }

    @FXML
    void btnProfile(ActionEvent event) {
        openWindow("/frontOffice/profile.fxml", "Mon Profil");
    }

    public void setCenterContent(Parent content) {
        pnItems.getChildren().clear();
        pnItems.getChildren().add(content);
    }


    private void loadFXML(String path) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(path));
            pnItems.getChildren().clear();
            pnItems.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openWindow(String fxmlFilePath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFilePath));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
