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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tn.esprit.models.User;
import tn.esprit.services.UserService;

import java.io.File;
import java.io.IOException;
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
    @FXML private Label cvLabelText;   // Ce label est utilisé pour afficher le nom du CV
    @FXML private Button btnChoisirCV;
    @FXML private Label lblNiveauEtude;

    private File selectedImageFile;
    private File selectedCVFile;

    private final UserService userService = new UserService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;  // 2 Mo
    private static final long MAX_CV_SIZE = 5 * 1024 * 1024;     // 5 Mo

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        rolesCombo.getItems().addAll("APPRENANT", "INSTRUCTEUR");

        toggleInstructeurFields(false);
        toggleApprenantFields(false);

        rolesCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            toggleInstructeurFields("INSTRUCTEUR".equals(newVal));
            toggleApprenantFields("APPRENANT".equals(newVal));
        });

        setupInputValidation();
    }

    private void toggleInstructeurFields(boolean visible) {
        btnChoisirCV.setVisible(visible);
        cvLabel.setVisible(visible);
        cvLabelText.setVisible(visible);

        if (!visible) {
            selectedCVFile = null;
            cvLabelText.setText("Aucun CV sélectionné");
        }
    }

    private void toggleApprenantFields(boolean visible) {
        niveauCombo.setVisible(visible);
        lblNiveauEtude.setVisible(visible);
    }

    private void setupInputValidation() {
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s\\-]*") || newVal.length() > 50) {
                nomField.setText(oldVal);
            }
        });
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s\\-]*") || newVal.length() > 50) {
                prenomField.setText(oldVal);
            }
        });
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                emailField.setText(oldVal);
            }
        });
    }

    @FXML
    private void inscrireUtilisateur() {
        ajouterUtilisateur();
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            if (selectedImageFile.length() <= MAX_IMAGE_SIZE) {
                imageLabel.setText(selectedImageFile.getName());
                imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
            } else {
                showAlert("Erreur", "Image trop volumineuse (max 2 Mo)");
                selectedImageFile = null;
                imageLabel.setText("Aucune image sélectionnée");
                imagePreview.setImage(null);
            }
        }
    }

    @FXML
    private void choisirCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un CV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx"));

        selectedCVFile = fileChooser.showOpenDialog(null);
        if (selectedCVFile != null) {
            if (selectedCVFile.length() <= MAX_CV_SIZE) {
                cvLabelText.setText(selectedCVFile.getName());
            } else {
                showAlert("Erreur", "CV trop volumineux (max 5 Mo)");
                selectedCVFile = null;
                cvLabelText.setText("Aucun CV sélectionné");
            }
        }
    }

    private void ajouterUtilisateur() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate dateNaissance = datePicker.getValue();
        String niveau = niveauCombo.getValue();
        String role = rolesCombo.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || dateNaissance == null || role == null) {
            showAlert("Erreur", "Tous les champs obligatoires doivent être remplis !");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas !");
            return;
        }

        if (Period.between(dateNaissance, LocalDate.now()).getYears() < 13) {
            showAlert("Erreur", "Vous devez avoir au moins 13 ans !");
            return;
        }

        if ("APPRENANT".equals(role) && (niveau == null || niveau.isEmpty())) {
            showAlert("Erreur", "Sélectionnez un niveau !");
            return;
        }

        if ("INSTRUCTEUR".equals(role) && selectedCVFile == null) {
            showAlert("Erreur", "Veuillez uploader un CV !");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert("Erreur", "Cet e-mail est déjà utilisé !");
            return;
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(email, role, hashedPassword, nom, prenom, dateNaissance);
        user.setCv(selectedCVFile != null ? selectedCVFile.getAbsolutePath() : null);
        user.setImage(selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null);

        boolean success = userService.addUser(user, niveau);

        if (success) {
            showAlert("Succès", "Utilisateur enregistré avec succès !");
            clearForm();
        } else {
            showAlert("Erreur", "Erreur lors de l'enregistrement !");
        }
    }

    private void clearForm() {
        nomField.clear(); prenomField.clear(); emailField.clear();
        passwordField.clear(); confirmPasswordField.clear();
        datePicker.setValue(null); niveauCombo.setValue(null); rolesCombo.setValue(null);
        imageLabel.setText("Aucune image sélectionnée"); imagePreview.setImage(null);
        cvLabelText.setText("Aucun CV sélectionné");
        selectedImageFile = null; selectedCVFile = null;
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
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de connexion");
        }
    }
}
