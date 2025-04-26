package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class VerificationPaiementController {
    @FXML
    private Button closeButton;

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
} 