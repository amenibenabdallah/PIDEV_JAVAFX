package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServiceInscriptionCours;
import tn.esprit.services.ServicePromotion;

import java.io.IOException;
import java.time.LocalDate;

public class ModifierPromotionViewController {

    @FXML private TextField codeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField remiseField;
    @FXML private DatePicker dateExpirationField;
    @FXML private Label apprenantLabel;
    @FXML private TextField inscriptionIdField;

    private Promotion promotion;
    private VBox contentArea;
    private final ServicePromotion service = new ServicePromotion();

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    public void initData(Promotion promotion) {
        this.promotion = promotion;
        codeField.setText(promotion.getCodePromo());

        // Extraire la description et le nom de l'apprenant
        String description = promotion.getDescription();
        String nomApprenant = "Non spécifié";
        if (description.contains("(Apprenant: ")) {
            int startIndex = description.indexOf("(Apprenant: ") + "(Apprenant: ".length();
            int endIndex = description.lastIndexOf(")");
            if (endIndex > startIndex) {
                nomApprenant = description.substring(startIndex, endIndex).trim();
                description = description.substring(0, description.indexOf("(Apprenant: ")).trim();
            }
        }
        descriptionField.setText(description);
        apprenantLabel.setText(nomApprenant);

        remiseField.setText(String.valueOf(promotion.getRemise()));
        dateExpirationField.setValue(promotion.getDateExpiration());
        inscriptionIdField.setText(String.valueOf(promotion.getInscriptionCoursId()));
        inscriptionIdField.setEditable(false); // Rendre non modifiable
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

            // Mise à jour de la promotion
            promotion.setCodePromo(codeField.getText());
            // Ajouter le nom de l'apprenant à la description
            String updatedDescription = descriptionField.getText() + " (Apprenant: " + apprenantLabel.getText() + ")";
            promotion.setDescription(updatedDescription);
            promotion.setRemise(remise);
            promotion.setDateExpiration(dateExpirationField.getValue());

            service.update(promotion);
            retourAffichage();

            // Afficher une alerte de succès
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Promotion modifiée avec succès !");
            successAlert.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur critique", "Erreur inattendue : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        retourAffichage();
    }

    private void retourAffichage() {
        try {
            if (contentArea == null) {
                showAlert("Erreur", "Le conteneur de contenu n'est pas initialisé");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPromotionsView.fxml"));
            Parent root = loader.load();

            AfficherPromotionsViewController controller = loader.getController();
            if (controller != null) {
                controller.setContentArea(contentArea);
            } else {
                showAlert("Erreur", "Impossible de charger le contrôleur de la vue");
                return;
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            VBox.setVgrow(root, Priority.ALWAYS);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface d'affichage des promotions: " + e.getMessage());
        }
    }

    private void resetFieldStyles() {
        codeField.setStyle("");
        descriptionField.setStyle("");
        remiseField.setStyle("");
        dateExpirationField.setStyle("");
        inscriptionIdField.setStyle("");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}