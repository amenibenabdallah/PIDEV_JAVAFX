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

import java.time.LocalDateTime;

public class InscriptionCoursViewController {

    @FXML private TextField txtNomApprenant;
    @FXML private TextField txtCin;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNomFormation;
    @FXML private ComboBox<String> typePaiementComboBox;
    @FXML private TextField txtMontant;
    @FXML private TextField txtStatus;
    @FXML private TextField txtApprenantId;
    @FXML private TextField txtFormationId;
    @FXML private Button btnAjouter;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();

    @FXML
    public void initialize() {
        typePaiementComboBox.setItems(FXCollections.observableArrayList(
                "Carte bancaire", "Espèces", "Virement"
        ));

        btnAjouter.setOnAction(event -> ajouterInscription());
    }

    private void ajouterInscription() {
        try {
            resetFieldStyles(); // ✅ Nettoyer les styles avant vérif

            if (txtNomApprenant.getText().isEmpty()) {
                txtNomApprenant.setStyle("-fx-border-color: red;");
                showAlert("Validation", "Le nom de l'apprenant est requis.");
                return;
            }

            if (!txtCin.getText().matches("\\d{8}")) {
                txtCin.setStyle("-fx-border-color: red;");
                showAlert("Validation", "CIN doit contenir exactement 8 chiffres.");
                return;
            }

            if (!txtEmail.getText().matches("^\\S+@\\S+\\.\\S+$")) {
                txtEmail.setStyle("-fx-border-color: red;");
                showAlert("Validation", "Email invalide.");
                return;
            }

            if (txtNomFormation.getText().isEmpty()) {
                txtNomFormation.setStyle("-fx-border-color: red;");
                showAlert("Validation", "Le nom de la formation est requis.");
                return;
            }

            if (typePaiementComboBox.getValue() == null) {
                typePaiementComboBox.setStyle("-fx-border-color: red;");
                showAlert("Validation", "Veuillez sélectionner un type de paiement.");
                return;
            }

            double montant;
            try {
                montant = Double.parseDouble(txtMontant.getText());
                if (montant <= 0) {
                    txtMontant.setStyle("-fx-border-color: red;");
                    showAlert("Validation", "Le montant doit être supérieur à 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                txtMontant.setStyle("-fx-border-color: red;");
                showAlert("Validation", "Montant invalide.");
                return;
            }

            if (txtStatus.getText().isEmpty()) {
                txtStatus.setStyle("-fx-border-color: red;");
                showAlert("Validation", "Le statut est requis.");
                return;
            }

            int apprenantId, formationId;
            try {
                apprenantId = Integer.parseInt(txtApprenantId.getText());
                formationId = Integer.parseInt(txtFormationId.getText());

                if (apprenantId <= 0 || formationId <= 0) {
                    txtApprenantId.setStyle("-fx-border-color: red;");
                    txtFormationId.setStyle("-fx-border-color: red;");
                    showAlert("Validation", "Les IDs doivent être des entiers positifs.");
                    return;
                }
            } catch (NumberFormatException e) {
                txtApprenantId.setStyle("-fx-border-color: red;");
                txtFormationId.setStyle("-fx-border-color: red;");
                showAlert("Validation", "ID Apprenant ou ID Formation invalide.");
                return;
            }

            // Création de l'inscription
            InscriptionCours ins = new InscriptionCours();
            ins.setNomApprenant(txtNomApprenant.getText());
            ins.setCin(txtCin.getText());
            ins.setEmail(txtEmail.getText());
            ins.setNomFormation(txtNomFormation.getText());
            ins.setTypePaiement(typePaiementComboBox.getValue());
            ins.setMontant(montant);
            ins.setStatus(txtStatus.getText());
            ins.setApprenantId(apprenantId);
            ins.setFormationId(formationId);
            ins.setDateInscreption(LocalDateTime.now());

            // Ajouter via service
            service.add(ins);

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) btnAjouter.getScene().getWindow();
            currentStage.close();

            // Affichage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherInscriptionView.fxml"));
            Parent root = loader.load();
            Stage displayStage = new Stage();
            displayStage.setScene(new Scene(root));
            displayStage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        txtNomApprenant.clear();
        txtCin.clear();
        txtEmail.clear();
        txtNomFormation.clear();
        typePaiementComboBox.getSelectionModel().clearSelection();
        txtMontant.clear();
        txtStatus.clear();
        txtApprenantId.clear();
        txtFormationId.clear();
        resetFieldStyles(); // aussi réinitialise les styles
    }

    private void resetFieldStyles() {
        txtNomApprenant.setStyle("");
        txtCin.setStyle("");
        txtEmail.setStyle("");
        txtNomFormation.setStyle("");
        typePaiementComboBox.setStyle("");
        txtMontant.setStyle("");
        txtStatus.setStyle("");
        txtApprenantId.setStyle("");
        txtFormationId.setStyle("");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
