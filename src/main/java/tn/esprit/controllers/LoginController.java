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
import tn.esprit.models.users;
import tn.esprit.services.UserService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    String hashedPassword;
    private final UserService userService = new UserService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final  SessionManager sessionManager = new SessionManager() ;

    @FXML
    public void seConnecter() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            users user = userService.findUserByEmail(email);
            System.out.println( password +"\n"+ user.getPassword());

            if (user != null) {
                // Utilisez matches() pour comparer le mot de passe en clair avec le hash stocké
                if (passwordEncoder.matches(password, user.getPassword())) {
                    showAlert("Succès", "Connexion réussie!", Alert.AlertType.INFORMATION);
                    sessionManager.setUtilisateurConnecte(user);

                    System.out.println(sessionManager.getUtilisateurConnecte().getId());
                    System.out.println(sessionManager.getUtilisateurConnecte().getRoles());
                    System.out.println(sessionManager.getUtilisateurConnecte().getUserType());



                } else {
                    showAlert("Erreur", "Identifiants incorrects");
                }
            } else {
                showAlert("Erreur", "Identifiants incorrects");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la connexion");
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void goToInscription(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Inscription.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page d'inscription");
        }
    }
}