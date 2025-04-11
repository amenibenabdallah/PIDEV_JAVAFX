package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.users;
import tn.esprit.services.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class InscriptionController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField; // À ajouter dans ton FXML
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> rolesCombo;
    @FXML private Label imageLabel;
    @FXML private ImageView imagePreview;

    private File selectedImageFile;
    private final UserService userService = new UserService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        rolesCombo.getItems().addAll("ROLE_APPRENANT", "ROLE_INSTRUCTEUR");
    }

    @FXML
    private void inscrireApprenant() {
        ajouterUtilisateur();
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            if (selectedImageFile.length() > 2 * 1024 * 1024) { // 2 MB max
                showAlert("Erreur", "L'image sélectionnée est trop volumineuse (max 2 Mo)");
                selectedImageFile = null;
                return;
            }
            imageLabel.setText(selectedImageFile.getName());
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    private void ajouterUtilisateur() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate dateNaissance = datePicker.getValue();
        String niveau = niveauCombo.getValue();
        String role = rolesCombo.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || dateNaissance == null || role == null) {
            showAlert("Erreur", "Tous les champs obligatoires doivent être remplis !");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            showAlert("Erreur", "Adresse e-mail invalide !");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas !");
            return;
        }

        if (role.equals("ROLE_APPRENANT") && (niveau == null || niveau.isEmpty())) {
            showAlert("Erreur", "Veuillez sélectionner un niveau pour l'apprenant !");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert("Erreur", "Cet e-mail est déjà utilisé !");
            return;
        }

        String hashedPassword = passwordEncoder.encode(password);
        System.out.println(hashedPassword+"hashedPassword");

        users user = new users(
                email,
                role,
                hashedPassword,
                nom,
                prenom,
                dateNaissance,
                null,
                role.equals("ROLE_APPRENANT") ? "apprenant" : "instructeur",
                selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null
        );

        userService.addUser(user, role.equals("ROLE_APPRENANT") ? niveau : null);

        showAlert("Succès", "Utilisateur enregistré avec succès !");
        clearForm();
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        datePicker.setValue(null);
        niveauCombo.setValue(null);
        rolesCombo.setValue(null);
        imageLabel.setText("Aucune image sélectionnée");
        imagePreview.setImage(null);
        selectedImageFile = null;
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion");
        }
    }
}
