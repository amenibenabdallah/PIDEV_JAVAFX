package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.UserService;

import java.io.File;
import java.io.IOException;

public class GoogleSignupController {
    @FXML private DatePicker datePicker;

    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox apprenantBox;
    @FXML private VBox instructeurBox;
    @FXML private ComboBox<String> niveauComboBox;
    @FXML private Label cvFileLabel;

    private File selectedCV;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            apprenantBox.setVisible("APPRENANT".equals(newVal));
            instructeurBox.setVisible("INSTRUCTEUR".equals(newVal));
        });
    }

    public void prefillGoogleData(String email, String fullName) {
        emailField.setText(email);
        nameField.setText(fullName);
    }

    @FXML
    private void handleChooseCV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx"));
        selectedCV = chooser.showOpenDialog(null);
        if (selectedCV != null) {
            cvFileLabel.setText(selectedCV.getName());
        }
    }

    @FXML
    private void finaliserInscription() throws IOException {
        String role = roleComboBox.getValue();
        if (role == null) {
            showAlert("Erreur", "Veuillez sélectionner un rôle.");
            return;
        }

        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner votre date de naissance.");
            return;
        }

        User user = new User();
        user.setEmail(emailField.getText());
        user.setNom(nameField.getText());
        user.setRole(role);
        user.setDateNaissance(datePicker.getValue());
        user.setPassword("");
        user.setPrenom("");
        user.setImage("");

        if ("APPRENANT".equals(role)) {
            if (niveauComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez sélectionner votre niveau d'étude.");
                return;
            }
            user.setNiveauEtude(niveauComboBox.getValue());
        } else if ("INSTRUCTEUR".equals(role)) {
            if (selectedCV == null) {
                showAlert("Erreur", "Veuillez uploader votre CV.");
                return;
            }
            user.setCv(selectedCV.getAbsolutePath());
        }

        boolean success = userService.addUser(user);
        if (success) {
            showAlert("Succès", "Inscription réussie !");
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } else {
            showAlert("Erreur", "Échec de l'enregistrement.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de revenir à la page de connexion.");
        }
    }

}
