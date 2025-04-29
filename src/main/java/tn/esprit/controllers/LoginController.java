package tn.esprit.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tn.esprit.models.User;
import tn.esprit.services.UserService;
import tn.esprit.utils.GoogleAuth;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private ImageView togglePasswordVisibility;

    private boolean isPasswordVisible = false;

    private final UserService userService = new UserService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void seConnecter(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = isPasswordVisible ? passwordVisibleField.getText().trim() : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            User user = userService.findUserByEmail(email);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                sessionManager.setUtilisateurConnecte(user);
                redirectToDashboard(event, user);
            } else {
                showAlert("Erreur", "Email ou mot de passe incorrect !");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Problème de connexion : " + e.getMessage());
        }
    }

    @FXML
    public void togglePassword(MouseEvent event) {
        if (isPasswordVisible) {
            // Cacher le mot de passe
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);

            // Mettre l'icône œil fermé
            togglePasswordVisibility.setImage(new Image(getClass().getResourceAsStream("/images/oeil.png")));
        } else {
            // Montrer le mot de passe
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            // Mettre l'icône œil ouvert
            togglePasswordVisibility.setImage(new Image(getClass().getResourceAsStream("/images/oeilon.png")));
        }
        isPasswordVisible = !isPasswordVisible;
    }


    private void redirectToDashboard(ActionEvent event, User user) throws IOException {
        FXMLLoader loader;
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            loader = new FXMLLoader(getClass().getResource("/AdminTemplate.fxml"));
        } else {
            loader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
        }
        Parent root = loader.load();
        switchScene(event, root);
    }

    private void switchScene(ActionEvent event, Parent root) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToInscription(ActionEvent event) {
        loadSimpleScene(event, "/Inscription.fxml");
    }

    @FXML
    private void goToForgotPassword(ActionEvent event) {
        loadSimpleScene(event, "/forgot_password.fxml");
    }

    private void loadSimpleScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            switchScene(event, root);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger : " + fxmlFile);
        }
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        try {
            GoogleIdToken idToken = GoogleAuth.authenticate();
            if (idToken != null) {
                String email = idToken.getPayload().getEmail();
                User user = userService.findUserByEmail(email);

                if (user != null) {
                    sessionManager.setUtilisateurConnecte(user);
                    redirectToDashboard(event, user);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/GoogleSignupForm.fxml"));
                    Parent root = loader.load();
                    GoogleSignupController controller = loader.getController();
                    controller.prefillGoogleData(email, (String) idToken.getPayload().get("name"));
                    switchScene(event, root);
                }
            } else {
                showAlert("Erreur", "Authentification Google échouée !");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'authentification Google : " + e.getMessage());
        }
    }
}
