package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;
import java.time.LocalDate;

public class AjoutPromotionViewController {
    @FXML private TextField codeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField remiseField;
    @FXML private DatePicker dateExpirationField;
    @FXML private TextField inscriptionIdField;
    @FXML private TextField apprenantIdField;

    private final ServicePromotion service = new ServicePromotion();

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    @FXML
    private void handleAjouter() {
        try {
            resetFieldStyles(); // Réinitialiser les styles

            // Validation 1: Champs obligatoires
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

            if (remiseField.getText().isEmpty()) {
                remiseField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "La remise est obligatoire");
                return;
            }

            if (dateExpirationField.getValue() == null) {
                dateExpirationField.setStyle("-fx-border-color: red;");
                showAlert("Erreur", "La date d'expiration est obligatoire");
                return;
            }

            // Validation 2: Format code promo (ex: PROMO2023)
            if (!codeField.getText().matches("^[A-Z0-9]{4,10}$")) {
                codeField.setStyle("-fx-border-color: red;");
                showAlert("Format invalide", "Code promo : 4-10 caractères (majuscules/chiffres)");
                return;
            }

            // Validation 3: Remise valide
            double remise;
            try {
                remise = Double.parseDouble(remiseField.getText());
                if (remise <= 0 || remise > 100) {
                    remiseField.setStyle("-fx-border-color: red;");
                    showAlert("Valeur incorrecte", "La remise doit être entre 0.01 et 100");
                    return;
                }
            } catch (NumberFormatException e) {
                remiseField.setStyle("-fx-border-color: red;");
                showAlert("Format invalide", "La remise doit être un nombre");
                return;
            }

            // Validation 4: Date expiration
            if (dateExpirationField.getValue().isBefore(LocalDate.now())) {
                dateExpirationField.setStyle("-fx-border-color: red;");
                showAlert("Date invalide", "La date doit être dans le futur");
                return;
            }

            // Validation 5: IDs numériques
            int inscriptionId, apprenantId;
            try {
                inscriptionId = Integer.parseInt(inscriptionIdField.getText());
                apprenantId = Integer.parseInt(apprenantIdField.getText());

                if (inscriptionId <= 0 || apprenantId <= 0) {
                    inscriptionIdField.setStyle("-fx-border-color: red;");
                    apprenantIdField.setStyle("-fx-border-color: red;");
                    showAlert("Valeur incorrecte", "Les IDs doivent être positifs");
                    return;
                }
            } catch (NumberFormatException e) {
                inscriptionIdField.setStyle("-fx-border-color: red;");
                apprenantIdField.setStyle("-fx-border-color: red;");
                showAlert("Format invalide", "Les IDs doivent être des nombres");
                return;
            }

            // Création de l'objet
            Promotion promotion = new Promotion(
                    codeField.getText().toUpperCase(),
                    descriptionField.getText(),
                    remise,
                    dateExpirationField.getValue(),
                    inscriptionId,
                    apprenantId
            );

            service.add(promotion);
            redirectToDisplay();

        } catch (Exception e) {
            showAlert("Erreur critique", "Erreur inattendue : " + e.getMessage());
        }
    }

    private void redirectToDisplay() throws Exception {
        Stage stage = (Stage) codeField.getScene().getWindow();
        stage.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPromotionsView.fxml"));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Promotion ajoutée avec succès !");
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler() {
        codeField.clear();
        descriptionField.clear();
        remiseField.clear();
        dateExpirationField.setValue(null);
        inscriptionIdField.clear();
        apprenantIdField.clear();
        resetFieldStyles();
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