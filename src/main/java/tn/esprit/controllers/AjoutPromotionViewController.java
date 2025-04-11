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
    private void handleAjouter() {
        try {
            Promotion promotion = new Promotion(
                    codeField.getText(),
                    descriptionField.getText(),
                    Double.parseDouble(remiseField.getText()),
                    dateExpirationField.getValue(),
                    Integer.parseInt(inscriptionIdField.getText()),
                    Integer.parseInt(apprenantIdField.getText())
            );

            service.add(promotion);

            // Redirection vers l'affichage
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPromotionsView.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        stage.close();
    }
}