package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;
import tn.esprit.services.ServiceInscriptionCours;
import java.time.LocalDate;
import javafx.scene.layout.VBox;
import tn.esprit.services.EmailService;

public class AjoutPromotionViewController {
    @FXML private TextField codeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField remiseField;
    @FXML private DatePicker dateExpirationField;
    @FXML private TextField inscriptionIdField;
    @FXML private ComboBox<String> apprenantComboBox;
    @FXML private ListView<String> doublonsListView;
    private VBox contentArea; // Référence au contentArea du template admin

    private final ServicePromotion service = new ServicePromotion();

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
        ServiceInscriptionCours serviceInscription = new ServiceInscriptionCours();
        if (doublonsListView != null) {
            doublonsListView.getItems().setAll(serviceInscription.getApprenantsAvecDoublons());
        }
        if (apprenantComboBox != null) {
            apprenantComboBox.getItems().setAll(serviceInscription.getApprenantsAvecDoublons());
        }
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
            int inscriptionId;
            String apprenantNom;
            try {
                inscriptionId = 0; // Champ caché, valeur par défaut
                apprenantNom = apprenantComboBox.getValue();
                if (apprenantNom == null || apprenantNom.isEmpty()) {
                    apprenantComboBox.setStyle("-fx-border-color: red;");
                    showAlert("Sélection requise", "Veuillez sélectionner un apprenant.");
                    return;
                }
            } catch (Exception e) {
                apprenantComboBox.setStyle("-fx-border-color: red;");
                showAlert("Sélection requise", "Veuillez sélectionner un apprenant.");
                return;
            }

            // Création de l'objet
            Promotion promotion = new Promotion(
                    codeField.getText().toUpperCase(),
                    descriptionField.getText(),
                    remise,
                    dateExpirationField.getValue(),
                    inscriptionId,
                    0 // apprenantId remplacé par 0, car on utilise le nom
            );
            // Ajoute le nom de l'apprenant dans la description pour traçabilité
            promotion.setDescription(promotion.getDescription() + " (Apprenant: " + apprenantNom + ")");

            service.add(promotion);

            // Envoi du code promo par email à l'apprenant
            ServiceInscriptionCours serviceInscription = new ServiceInscriptionCours();
            String emailApprenant = serviceInscription.getEmailByNom(apprenantNom);
            if (emailApprenant != null && !emailApprenant.isEmpty()) {
                EmailService.sendPromoToApprenant(
                    emailApprenant,
                    codeField.getText().toUpperCase(),
                    descriptionField.getText(),
                    remise,
                    dateExpirationField.getValue()
                );
            } else {
                System.err.println("Impossible d'envoyer l'email : email de l'apprenant introuvable.");
            }

            redirectToDisplay();

        } catch (Exception e) {
            showAlert("Erreur critique", "Erreur inattendue : " + e.getMessage());
        }
    }

    private void redirectToDisplay() throws Exception {
        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPromotionsView.fxml"));
            Parent root = loader.load();
            
            // Injecter contentArea dans le contrôleur d'affichage
            Object controller = loader.getController();
            if (controller instanceof AfficherPromotionsViewController) {
                ((AfficherPromotionsViewController) controller).setContentArea(contentArea);
            }
            
            // Remplacer le contenu actuel par la vue d'affichage des promotions
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            VBox.setVgrow(root, javafx.scene.layout.Priority.ALWAYS);
            
            // Afficher une alerte de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Promotion ajoutée avec succès !");
            alert.showAndWait();
        } else {
            // Fallback : ancienne méthode (nouvelle fenêtre)
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
    }

    @FXML
    private void handleAnnuler() {
        codeField.clear();
        descriptionField.clear();
        remiseField.clear();
        dateExpirationField.setValue(null);
        inscriptionIdField.clear();
        apprenantComboBox.getSelectionModel().clearSelection();
        resetFieldStyles();
    }

    private void resetFieldStyles() {
        codeField.setStyle("");
        descriptionField.setStyle("");
        remiseField.setStyle("");
        dateExpirationField.setStyle("");
        inscriptionIdField.setStyle("");
        apprenantComboBox.setStyle("");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }
}