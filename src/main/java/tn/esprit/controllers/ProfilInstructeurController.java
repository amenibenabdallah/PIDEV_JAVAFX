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
import tn.esprit.models.User;
import tn.esprit.services.UserService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;

public class ProfilInstructeurController {

    @FXML private TextField nomField, prenomField, emailField;
    @FXML private DatePicker datePicker;
    @FXML private ImageView profileImage;
    @FXML private Label cvLabel;

    private final UserService userService = new UserService();
    private final User user = SessionManager.getInstance().getUtilisateurConnecte();
    private File selectedImage;
    private File selectedCV;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        datePicker.setValue(user.getDateNaissance());

        if (user.getImage() != null) {
            profileImage.setImage(new Image(new File(user.getImage()).toURI().toString()));
        }
        if (user.getCv() != null) {
            cvLabel.setText(new File(user.getCv()).getName());
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        selectedImage = chooser.showOpenDialog(null);
        if (selectedImage != null) {
            profileImage.setImage(new Image(selectedImage.toURI().toString()));
        }
    }

    @FXML
    private void handleChangeCV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx"));
        selectedCV = chooser.showOpenDialog(null);
        if (selectedCV != null) {
            cvLabel.setText(selectedCV.getName());
        }
    }

    @FXML
    private void handleSave() {
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setDateNaissance(datePicker.getValue());
        if (selectedImage != null) user.setImage(selectedImage.getAbsolutePath());
        if (selectedCV != null) user.setCv(selectedCV.getAbsolutePath());

        userService.updateUser(user);
        showAlert("Succès", "Profil mis à jour !");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la déconnexion !");
            alert.showAndWait();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
