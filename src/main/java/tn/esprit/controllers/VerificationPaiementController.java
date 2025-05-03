package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class VerificationPaiementController {

    @FXML
    private Button closeButton;
    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    private void handleClose() {
        // Charger la page d'accueil ou une autre vue par défaut dans contentArea
        if (mainLayoutController != null) {
            mainLayoutController.loadWelcomePage();
        } else {
            // Afficher une alerte si mainLayoutController n'est pas défini
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de revenir à la page d'accueil.");
            alert.showAndWait();
        }
    }
}