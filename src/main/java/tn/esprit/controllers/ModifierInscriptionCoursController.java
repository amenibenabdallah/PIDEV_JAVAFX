package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.models.InscriptionCours;
import tn.esprit.services.ServiceInscriptionCours;

import java.io.IOException;

public class ModifierInscriptionCoursController {

    @FXML private TextField nomApprenantField;
    @FXML private TextField emailField;
    @FXML private TextField cinField;
    @FXML private TextField nomFormationField;
    @FXML private ComboBox<String> typePaiementComboBox;
    @FXML private TextField apprenantIdField;
    @FXML private TextField formationIdField;

    private InscriptionCours selected;
    private VBox contentArea; // Référence au contentArea
    private final ServiceInscriptionCours service = new ServiceInscriptionCours();
    private Runnable refreshCallback;

    // Injecter contentArea
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    public void initialize() {
        typePaiementComboBox.setItems(FXCollections.observableArrayList(
                "Carte bancaire", "Espèces", "Virement"
        ));
    }

    public void initData(InscriptionCours inscription) {
        this.selected = inscription;
        if (inscription != null) {
            nomApprenantField.setText(inscription.getNomApprenant());
            emailField.setText(inscription.getEmail());
            cinField.setText(inscription.getCin());
            nomFormationField.setText(inscription.getNomFormation());
            typePaiementComboBox.setValue(inscription.getTypePaiement());
            apprenantIdField.setText(String.valueOf(inscription.getApprenantId()));
            formationIdField.setText(String.valueOf(inscription.getFormationId()));
            selected.setDateInscreption(inscription.getDateInscreption());
        }
    }

    @FXML
    private void modifierInscriptionCours(ActionEvent event) {
        if (contentArea == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le conteneur de contenu n'est pas initialisé.");
            return;
        }

        resetFieldStyles();
        if (!validateFields()) return;

        try {
            selected.setNomApprenant(nomApprenantField.getText());
            selected.setEmail(emailField.getText());
            selected.setCin(cinField.getText());
            selected.setNomFormation(nomFormationField.getText());
            selected.setTypePaiement(typePaiementComboBox.getValue());
            selected.setApprenantId(Integer.parseInt(apprenantIdField.getText()));
            selected.setFormationId(Integer.parseInt(formationIdField.getText()));

            service.update(selected);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'inscription a été modifiée avec succès.");

            if (refreshCallback != null) {
                refreshCallback.run();
            }

            retourAffichage();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la modification : " + e.getMessage());
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
            showError(typePaiementComboBox, "Veuillez sélectionner un type de paiement.");
            return false;
        }

        try {
            int apprenantId = Integer.parseInt(apprenantIdField.getText());
            int formationId = Integer.parseInt(formationIdField.getText());
            if (apprenantId <= 0 || formationId <= 0) {
                showError(apprenantIdField, "Les IDs doivent être des entiers positifs.");
                showError(formationIdField, "Les IDs doivent être des entiers positifs.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError(apprenantIdField, "ID Apprenant ou ID Formation invalide.");
            showError(formationIdField, "ID Apprenant ou ID Formation invalide.");
            return false;
        }

        return true;
    }

    private void showError(Control field, String message) {
        field.getStyleClass().add("error-border");
        showAlert(Alert.AlertType.ERROR, "Validation", message);
    }

    private void resetFieldStyles() {
        nomApprenantField.getStyleClass().remove("error-border");
        emailField.getStyleClass().remove("error-border");
        cinField.getStyleClass().remove("error-border");
        nomFormationField.getStyleClass().remove("error-border");
        typePaiementComboBox.getStyleClass().remove("error-border");
        apprenantIdField.getStyleClass().remove("error-border");
        formationIdField.getStyleClass().remove("error-border");
    }

    @FXML
    private void handleAnnuler() {
        retourAffichage();
    }

    private void retourAffichage() {
        if (contentArea == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le conteneur de contenu n'est pas initialisé.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherInscriptionView.fxml"));
            Parent root = loader.load();

            AfficherInscriptionsViewController controller = loader.getController();
            controller.setContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            VBox.setVgrow(root, Priority.ALWAYS);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Erreur de retour à l'affichage des inscriptions : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}