package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import tn.esprit.models.InscriptionCours;
import tn.esprit.services.ServiceInscriptionCours;

import java.io.IOException;

public class ModifierInscriptionCoursController {

    @FXML private TextField nomApprenantField;
    @FXML private TextField emailField;
    @FXML private TextField cinField;
    @FXML private TextField nomFormationField;
    @FXML private TextField montantField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> typePaiementComboBox;
    @FXML private TextField apprenantIdField;
    @FXML private TextField formationIdField;

    private InscriptionCours selected;
    private final ServiceInscriptionCours service = new ServiceInscriptionCours();
    private Runnable refreshCallback;
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }
    public void initData(InscriptionCours inscription) {
        this.selected = inscription;
        if (inscription != null) {
            nomApprenantField.setText(inscription.getNomApprenant());
            emailField.setText(inscription.getEmail());
            cinField.setText(inscription.getCin());
            nomFormationField.setText(inscription.getNomFormation());
            montantField.setText(String.valueOf(inscription.getMontant()));
            statusComboBox.setValue(inscription.getStatus());
            typePaiementComboBox.setValue(inscription.getTypePaiement());
            apprenantIdField.setText(String.valueOf(inscription.getApprenantId()));
            formationIdField.setText(String.valueOf(inscription.getFormationId()));

            // Conserver la date d'inscription sans modification
            selected.setDateInscreption(inscription.getDateInscreption());
        }
    }

    @FXML
    private void modifierInscriptionCours(ActionEvent event) {
        // Réinitialiser les styles avant toute validation
        resetFieldStyles();

        if (!validateFields()) return;

        try {
            selected.setNomApprenant(nomApprenantField.getText());
            selected.setEmail(emailField.getText());
            selected.setCin(cinField.getText());
            selected.setNomFormation(nomFormationField.getText());
            selected.setMontant(Double.parseDouble(montantField.getText()));
            selected.setStatus(statusComboBox.getValue());
            selected.setTypePaiement(typePaiementComboBox.getValue());
            selected.setApprenantId(Integer.parseInt(apprenantIdField.getText()));
            selected.setFormationId(Integer.parseInt(formationIdField.getText()));

            // Log des valeurs
            System.out.println("🔁 Mise à jour de l'inscription avec ID : " + selected.getId());

            // Mise à jour en BDD
            service.update(selected);

            // Fermer et recharger
            Stage stage = (Stage) nomApprenantField.getScene().getWindow();
            stage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherInscriptionView.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Liste des Inscriptions");
            newStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de la modification.");
        }
    }

    private boolean validateFields() {
        if (nomApprenantField.getText().isEmpty()) {
            showError(nomApprenantField, "Le nom de l'apprenant est requis.");
            return false;
        }

        if (!cinField.getText().matches("\\d{8}")) {
            showError(cinField, "CIN doit contenir exactement 8 chiffres.");
            return false;
        }

        if (!emailField.getText().matches("^\\S+@\\S+\\.\\S+$")) {
            showError(emailField, "Email invalide.");
            return false;
        }

        if (nomFormationField.getText().isEmpty()) {
            showError(nomFormationField, "Le nom de la formation est requis.");
            return false;
        }

        if (typePaiementComboBox.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner un type de paiement.");
            return false;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                showError(montantField, "Le montant doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError(montantField, "Montant invalide.");
            return false;
        }

        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            showAlert("Validation", "Le statut est requis.");
            return false;
        }

        try {
            int apprenantId = Integer.parseInt(apprenantIdField.getText());
            int formationId = Integer.parseInt(formationIdField.getText());
            if (apprenantId <= 0 || formationId <= 0) {
                showAlert("Validation", "Les IDs doivent être des entiers positifs.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation", "ID Apprenant ou ID Formation invalide.");
            return false;
        }

        return true;
    }

    private void showError(TextField field, String message) {
        field.setStyle("-fx-border-color: red;");
        showAlert("Validation", message);
    }

    private void resetFieldStyles() {
        nomApprenantField.setStyle("");
        emailField.setStyle("");
        cinField.setStyle("");
        nomFormationField.setStyle("");
        montantField.setStyle("");
        apprenantIdField.setStyle("");
        formationIdField.setStyle("");
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) nomApprenantField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
