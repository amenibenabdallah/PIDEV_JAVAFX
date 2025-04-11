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
import tn.esprit.models.instructeurs;
import tn.esprit.models.users;
import tn.esprit.services.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class InscriptionController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> rolesCombo;
    @FXML private Label imageLabel;
    @FXML private ImageView imagePreview;
    @FXML private Label cvLabel;
    @FXML private Button btnChoisirCV;
    @FXML private Label lblSpecialite;
    @FXML private TextField specialiteField;

    private File selectedImageFile;
    private File selectedCVFile;
    private final UserService userService = new UserService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @FXML
    public void initialize() {
        // Initialisation des ComboBox
        niveauCombo.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        rolesCombo.getItems().addAll("ROLE_APPRENANT", "ROLE_INSTRUCTEUR");

        // Masquer les champs spécifiques aux instructeurs par défaut
        toggleInstructeurFields(false);

        // Gestion de la visibilité des champs selon le rôle
        rolesCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isInstructeur = "ROLE_INSTRUCTEUR".equals(newVal);
            toggleInstructeurFields(isInstructeur);
            niveauCombo.setVisible(!isInstructeur);
        });

        // Contrôles de saisie en temps réel
        setupInputValidation();
    }

    private void toggleInstructeurFields(boolean visible) {
        btnChoisirCV.setVisible(visible);
        cvLabel.setVisible(visible);
        lblSpecialite.setVisible(visible);
        specialiteField.setVisible(visible);

        if (!visible) {
            selectedCVFile = null;
            cvLabel.setText("Aucun CV sélectionné");
            specialiteField.clear();
        }
    }

    private void setupInputValidation() {
        // Nom: seulement des lettres, espaces et tirets
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s\\-]*")) {
                nomField.setText(oldValue);
            } else if (newValue.length() > 50) {
                nomField.setText(oldValue);
            }
        });

        // Prénom: mêmes règles que le nom
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s\\-]*")) {
                prenomField.setText(oldValue);
            } else if (newValue.length() > 50) {
                prenomField.setText(oldValue);
            }
        });

        // Email: validation en temps réel
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 100) {
                emailField.setText(oldValue);
            }
        });

        // Password: maximum 50 caractères
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                passwordField.setText(oldValue);
            }
        });

        // Confirmation password: maximum 50 caractères
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                confirmPasswordField.setText(oldValue);
            }
        });
    }

    @FXML
    private void inscrireApprenant() throws SQLException {
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
                imageLabel.setText("Aucune image sélectionnée");
                imagePreview.setImage(null);
                return;
            }
            imageLabel.setText(selectedImageFile.getName());
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    private void choisirCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un CV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
        );
        selectedCVFile = fileChooser.showOpenDialog(null);
        if (selectedCVFile != null) {
            if (selectedCVFile.length() > 5 * 1024 * 1024) { // 5 MB max
                showAlert("Erreur", "Le fichier sélectionné est trop volumineux (max 5 Mo)");
                selectedCVFile = null;
                cvLabel.setText("Aucun CV sélectionné");
                return;
            }
            cvLabel.setText(selectedCVFile.getName());
        }
    }

    private void ajouterUtilisateur() throws SQLException {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate dateNaissance = datePicker.getValue();
        String niveau = niveauCombo.getValue();
        String role = rolesCombo.getValue();
        String specialite = specialiteField.getText().trim();

        // Validation des champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || dateNaissance == null || role == null) {
            showAlert("Erreur", "Tous les champs obligatoires doivent être remplis !");
            return;
        }

        // Validation du nom et prénom
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s\\-]{2,50}")) {
            showAlert("Erreur", "Le nom doit contenir entre 2 et 50 lettres, espaces ou tirets");
            return;
        }

        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s\\-]{2,50}")) {
            showAlert("Erreur", "Le prénom doit contenir entre 2 et 50 lettres, espaces ou tirets");
            return;
        }

        // Validation de l'email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,10}$")) {
            showAlert("Erreur", "Adresse e-mail invalide !");
            return;
        }

        // Validation du mot de passe
        if (password.length() < 8) {
            showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caractères !");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas !");
            return;
        }

        // Validation de la date de naissance (au moins 13 ans)
        if (Period.between(dateNaissance, LocalDate.now()).getYears() < 13) {
            showAlert("Erreur", "Vous devez avoir au moins 13 ans pour vous inscrire !");
            return;
        }

        // Validation spécifique aux apprenants
        if (role.equals("ROLE_APPRENANT") && (niveau == null || niveau.isEmpty())) {
            showAlert("Erreur", "Veuillez sélectionner un niveau pour l'apprenant !");
            return;
        }

        // Validation spécifique aux instructeurs
        if (role.equals("ROLE_INSTRUCTEUR")) {
            if (selectedCVFile == null) {
                showAlert("Erreur", "Veuillez uploader un CV pour les instructeurs !");
                return;
            }
            if (specialite.isEmpty()) {
                showAlert("Erreur", "Veuillez saisir une spécialité pour les instructeurs !");
                return;
            }
        }

        // Vérification de l'unicité de l'email
        if (userService.emailExists(email)) {
            showAlert("Erreur", "Cet e-mail est déjà utilisé !");
            return;
        }

        // Hachage du mot de passe
        String hashedPassword = passwordEncoder.encode(password);

        // Création et enregistrement de l'utilisateur
        boolean success;
        if (role.equals("ROLE_INSTRUCTEUR")) {
            instructeurs instructeur = new instructeurs(
                    email,
                    role,
                    hashedPassword,
                    nom,
                    prenom,
                    dateNaissance,
                    null,
                    "instructeur",
                    selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null,

                    selectedCVFile.getAbsolutePath()
            );
            success = userService.addUser(instructeur,"");
        } else {
            users user = new users(
                    email,
                    role,
                    hashedPassword,
                    nom,
                    prenom,
                    dateNaissance,
                    null,
                    "apprenant",
                    selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null
            );
            success = userService.addUser(user, niveau);
        }

        if (success) {
            showAlert("Succès", "Utilisateur enregistré avec succès !");
            clearForm();
        } else {
            clearForm();
        }
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
        specialiteField.clear();
        imageLabel.setText("Aucune image sélectionnée");
        imagePreview.setImage(null);
        cvLabel.setText("Aucun CV sélectionné");
        selectedImageFile = null;
        selectedCVFile = null;
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