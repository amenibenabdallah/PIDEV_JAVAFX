package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    private final UserService userService = new UserService();

    @FXML
    private void handleSendResetLink() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir votre email !");
            return;
        }

        if (!userService.emailExists(email)) {
            showAlert("Erreur", "Aucun utilisateur trouvé avec cet email.");
            return;
        }

        List<String> choices = Arrays.asList("Email", "SMS");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Email", choices);
        dialog.setTitle("Méthode de réinitialisation");
        dialog.setHeaderText("Choisissez comment recevoir votre token :");

        Optional<String> choice = dialog.showAndWait();
        if (choice.isPresent()) {
            boolean sent = false;

            if (choice.get().equals("Email")) {
                sent = userService.generateResetTokenEmailOnly(email);
            } else {
                TextInputDialog phoneDialog = new TextInputDialog();
                phoneDialog.setTitle("Téléphone requis");
                phoneDialog.setHeaderText("Entrez votre numéro de téléphone :");
                phoneDialog.setContentText("Numéro :");

                Optional<String> phone = phoneDialog.showAndWait();
                if (phone.isPresent() && !phone.get().isEmpty()) {
                    sent = userService.generateResetTokenSMS(email, phone.get());
                } else {
                    showAlert("Erreur", "Numéro de téléphone invalide.");
                    return;
                }
            }

            if (sent) {
                showAlert("Succès", "Token envoyé avec succès !");
                redirectToTokenVerification();
            } else {
                showAlert("Erreur", "Échec de l'envoi du token.");
            }
        }
    }

    private void redirectToTokenVerification() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/TokenVerification.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de vérification.");
        }
    }

    @FXML
    private void goBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la connexion.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
