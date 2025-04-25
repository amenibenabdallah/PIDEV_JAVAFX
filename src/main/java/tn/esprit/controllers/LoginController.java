package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tn.esprit.models.User;
import tn.esprit.services.UserService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SessionManager sessionManager = new SessionManager();

    @FXML
    public void seConnecter(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            User user = userService.findUserByEmail(email);

            if (user != null) {
                if (passwordEncoder.matches(password, user.getPassword())) {
                    sessionManager.setUtilisateurConnecte(user);

                    System.out.println("✅ Connecté : ID = " + user.getId() + ", Rôle = " + user.getRole());

                    String role = user.getRole();  // Exemple : "APPRENANT", "ADMIN", "INSTRUCTEUR"
                    String fxmlFile;

                    switch (role) {
                        case "APPRENANT":
                            fxmlFile = "/EditUserForm.fxml";
                            break;
                        case "ADMIN":
                            fxmlFile = "/AdminTemplate.fxml";
                            break;
                        case "INSTRUCTEUR":
                            fxmlFile = "/DashboardInstructeur.fxml";
                            break;
                        default:
                            showAlert("Erreur", "Rôle non reconnu pour cet utilisateur");
                            return;
                    }

                    loadScene(event, fxmlFile);

                } else {
                    showAlert("Erreur", "Mot de passe incorrect");
                }
            } else {
                showAlert("Erreur", "Utilisateur introuvable");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la connexion");
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private void loadScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + fxmlFile);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToInscription(ActionEvent event) {
        loadScene(event, "/Inscription.fxml");
    }
}
