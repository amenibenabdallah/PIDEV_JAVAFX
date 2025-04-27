package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

import java.io.IOException;

public class ResetPasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserService userService = new UserService();

    @FXML
    private void resetPassword(ActionEvent event) {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }

        boolean success = userService.resetPasswordWithToken(TokenVerificationController.validToken, newPass);
        if (success) {
            showAlert("Succès", "Mot de passe réinitialisé. Vous pouvez vous connecter.");
            // Retour automatique vers Login
            new TokenVerificationController().goToLogin(event);
        } else {
            showAlert("Erreur", "Erreur lors de la réinitialisation.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void goToLogin(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la page de connexion.");

    }
    }
}
