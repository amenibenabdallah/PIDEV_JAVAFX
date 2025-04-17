package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.InscriptionCours;
import tn.esprit.services.ServiceInscriptionCours;

import java.io.IOException;
import java.time.LocalDateTime;

public class InscriptionCoursViewController {

    @FXML private TextField txtNomApprenant;
    @FXML private TextField txtCin;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNomFormation;
    @FXML private ComboBox<String> typePaiementComboBox;
    @FXML private TextField txtApprenantId;
    @FXML private TextField txtFormationId;
    @FXML private Button btnAjouter;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();

    @FXML
    public void initialize() {
        // Initialisation des ComboBox
        typePaiementComboBox.setItems(FXCollections.observableArrayList(
                "Carte bancaire", "Espèces", "Virement"
        ));
    }

    @FXML
    private void ajouterInscription() {
        try {
            resetFieldStyles();

            // Validation des champs
            if (txtNomApprenant.getText().isEmpty()) {
                txtNomApprenant.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Le nom de l'apprenant est requis.");
                return;
            }

            if (!txtCin.getText().matches("\\d{8}")) {
                txtCin.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "CIN doit contenir exactement 8 chiffres.");
                return;
            }

            if (!txtEmail.getText().matches("^\\S+@\\S+\\.\\S+$")) {
                txtEmail.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Email invalide.");
                return;
            }

            if (txtNomFormation.getText().isEmpty()) {
                txtNomFormation.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Le nom de la formation est requis.");
                return;
            }

            if (typePaiementComboBox.getValue() == null) {
                typePaiementComboBox.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Veuillez sélectionner un type de paiement.");
                return;
            }

            int apprenantId, formationId;
            try {
                apprenantId = Integer.parseInt(txtApprenantId.getText());
                formationId = Integer.parseInt(txtFormationId.getText());

                if (apprenantId <= 0 || formationId <= 0) {
                    txtApprenantId.getStyleClass().add("error-border");
                    txtFormationId.getStyleClass().add("error-border");
                    showAlert(Alert.AlertType.ERROR, "Validation", "Les IDs doivent être des entiers positifs.");
                    return;
                }
            } catch (NumberFormatException e) {
                txtApprenantId.getStyleClass().add("error-border");
                txtFormationId.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "ID Apprenant ou ID Formation invalide.");
                return;
            }

            // Création de l'objet InscriptionCours
            InscriptionCours ins = new InscriptionCours();
            ins.setNomApprenant(txtNomApprenant.getText());
            ins.setCin(txtCin.getText());
            ins.setEmail(txtEmail.getText());
            ins.setNomFormation(txtNomFormation.getText());
            ins.setTypePaiement(typePaiementComboBox.getValue());
            ins.setApprenantId(apprenantId);
            ins.setFormationId(formationId);
            ins.setDateInscreption(LocalDateTime.now());

            // Ajout à la base de données
            service.add(ins);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription ajoutée avec succès !");

            // Fermer la fenêtre actuelle et ouvrir la vue d'affichage
            Stage currentStage = (Stage) btnAjouter.getScene().getWindow();
            currentStage.close();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherInscriptionView.fxml"));
                Parent root = loader.load();
                Stage displayStage = new Stage();
                displayStage.setTitle("Liste des Inscriptions");
                displayStage.setScene(new Scene(root));
                displayStage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue d'affichage : " + e.getMessage());
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        txtNomApprenant.clear();
        txtCin.clear();
        txtEmail.clear();
        txtNomFormation.clear();
        typePaiementComboBox.getSelectionModel().clearSelection();
        txtApprenantId.clear();
        txtFormationId.clear();
        resetFieldStyles();
    }

    private void resetFieldStyles() {
        txtNomApprenant.getStyleClass().remove("error-border");
        txtCin.getStyleClass().remove("error-border");
        txtEmail.getStyleClass().remove("error-border");
        txtNomFormation.getStyleClass().remove("error-border");
        typePaiementComboBox.getStyleClass().remove("error-border");
        txtApprenantId.getStyleClass().remove("error-border");
        txtFormationId.getStyleClass().remove("error-border");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}