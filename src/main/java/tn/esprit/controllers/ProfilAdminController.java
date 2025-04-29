package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.models.User;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfilAdminController implements Initializable {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private ImageView profileImage;

    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAdminProfile();
    }

    private void loadAdminProfile() {
        User admin = sessionManager.getUtilisateurConnecte();

        if (admin != null && "ADMIN".equalsIgnoreCase(admin.getRole())) {
            nomField.setText(admin.getNom());
            prenomField.setText(admin.getPrenom());
            emailField.setText(admin.getEmail());

            if (admin.getImage() != null && !admin.getImage().isEmpty()) {
                try {
                    Image image = new Image(getClass().getResourceAsStream(admin.getImage()));
                    profileImage.setImage(image);
                } catch (Exception e) {
                    System.out.println("Erreur de chargement de l'image : " + e.getMessage());
                    // Optionnel : profileImage.setImage(null);  // Laisser vide en cas d'erreur
                }
            } else {
                // Laisser l'image vide si aucun chemin n'est défini
                profileImage.setImage(null);
            }

        } else {
            // Sécurité : Si aucun admin connecté, désactive le formulaire
            nomField.setText("Accès refusé");
            prenomField.setDisable(true);
            emailField.setDisable(true);
        }
    }
}
