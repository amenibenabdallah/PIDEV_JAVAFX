package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;

import java.io.IOException;
import java.time.LocalDate;

public class ModifierPromotionViewController {

    @FXML private TextField codeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField remiseField;
    @FXML private DatePicker dateExpirationField;
    @FXML private TextField inscriptionIdField;
    @FXML private TextField apprenantIdField;

    private Promotion promotion;
    private VBox contentArea; // Référence au contentArea pour navigation dynamique
    private final ServicePromotion service = new ServicePromotion();

    // Initialise les champs avec les données de la promotion
    public void initData(Promotion promotion) {
        this.promotion = promotion;
        codeField.setText(promotion.getCodePromo());
        descriptionField.setText(promotion.getDescription());
        remiseField.setText(String.valueOf(promotion.getRemise()));
        dateExpirationField.setValue(promotion.getDateExpiration());
        inscriptionIdField.setText(String.valueOf(promotion.getInscriptionCoursId()));
        apprenantIdField.setText(String.valueOf(promotion.getApprenantId()));
    }

    // Injecte le contentArea depuis AfficherPromotionsViewController
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    private void handleEnregistrer() {
        try {
            resetFieldStyles();

            // Validation des champs
            if (codeField.getText().isEmpty()) {
                codeField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "Le code promo est obligatoire");
                return;
            }

            if (descriptionField.getText().isEmpty()) {
                descriptionField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "La description est obligatoire");
                return;
            }

            double remise;
            try {
                remise = Double.parseDouble(remiseField.getText());
                if (remise <= 0 || remise > 100) {
                    remiseField.setStyle("-fx-border-color: red;");
                    showAlert("Erreur", "La remise doit être entre 0.01 et 100");
                    return;
                }
            } catch (NumberFormatException e) {
                remiseField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "Format numérique invalide pour la remise");
                return;
            }

            if (dateExpirationField.getValue() == null || dateExpirationField.getValue().isBefore(LocalDate.now())) {
                dateExpirationField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "La date d'expiration doit être dans le futur");
                return;
            }

            int inscriptionId, apprenantId;
            try {
                inscriptionId = Integer.parseInt(inscriptionIdField.getText());
                apprenantId = Integer.parseInt(apprenantIdField.getText());
                if (inscriptionId <= 0 || apprenantId <= 0) {
                    inscriptionIdField.setStyle("-fx-border-color: red;");
                    apprenantIdField.setStyle("-fx-border-color: red;");
                    showAlert("Erreur", "Les IDs doivent être positifs");
                    return;
                }
            } catch (NumberFormatException e) {
                inscriptionIdField.setStyle("-fx-border-color: red;");
                apprenantIdField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "Format numérique invalide pour les IDs");
                return;
            }

            // Mise à jour de la promotion
            promotion.setCodePromo(codeField.getText());
            promotion.setDescription(descriptionField.getText());
            promotion.setRemise(remise);
            promotion.setDateExpiration(dateExpirationField.getValue());
            promotion.setInscriptionCoursId(inscriptionId);
            promotion.setApprenantId(apprenantId);

            service.update(promotion);

            // Retour à la liste des promotions dans contentArea
            retourAffichage();

        } catch (Exception e) {
            showAlert("Erreur critique", "Erreur inattendue : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        // Retour à la liste des promotions sans enregistrer
        retourAffichage();
    }

    // Recharge AfficherPromotionsView.fxml dans contentArea
    private void retourAffichage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPromotionsView.fxml"));
            Parent root = loader.load();

            AfficherPromotionsViewController controller = loader.getController();
            controller.setContentArea(contentArea); // Injecte contentArea

            contentArea.getChildren().clear(); // Vide le contentArea
            contentArea.getChildren().add(root); // Ajoute la vue de la liste
            VBox.setVgrow(root, Priority.ALWAYS); // Assure que la vue occupe tout l'espace

        } catch (IOException e) {
            showAlert("Navigation", "Erreur de retour à l'affichage des promotions");
        }
    }

    private void resetFieldStyles() {
        codeField.setStyle("");
        descriptionField.setStyle("");
        remiseField.setStyle("");
        dateExpirationField.setStyle("");
        inscriptionIdField.setStyle("");
        apprenantIdField.setStyle("");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}