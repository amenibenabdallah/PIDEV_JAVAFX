package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

public class TokenVerificationController {
    @FXML private TextField tokenField;

    private final UserService userService = new UserService();
    public static String validToken;  // Stocker le token valide

    @FXML
    private void verifyToken(ActionEvent event) {
        String token = tokenField.getText().trim();
        if (token.isEmpty()) {
            showAlert("Erreur", "Veuillez coller votre token.");
            return;
        }

        if (userService.isValidToken(token)) {
            validToken = token;  // Stocker le token pour l'étape suivante
            loadScene(event, "/reset_password.fxml");
        } else {
            showAlert("Erreur", "Token invalide ou expiré.");
        }
    }

    private void loadScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger l'interface.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    void goToLogin(ActionEvent event) {
        loadScene(event, "/login.fxml");
    }
}
