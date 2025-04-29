package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.utils.SessionManager;

public class WelcomController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        String nomUtilisateur = SessionManager.getInstance().getUtilisateurConnecte().getNom();
        welcomeLabel.setText("Bienvenue " + nomUtilisateur + " !");
    }
}
