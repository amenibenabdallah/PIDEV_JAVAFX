package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;

public class ModifierPromotionViewController {
    @FXML private TextField codeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField remiseField;
    @FXML private DatePicker dateExpirationField;
    @FXML private TextField inscriptionIdField;
    @FXML private TextField apprenantIdField;

    private Promotion promotion;
    private final ServicePromotion service = new ServicePromotion();

    public void initData(Promotion promotion) {
        this.promotion = promotion;
        codeField.setText(promotion.getCodePromo());
        descriptionField.setText(promotion.getDescription());
        remiseField.setText(String.valueOf(promotion.getRemise()));
        dateExpirationField.setValue(promotion.getDateExpiration());
        inscriptionIdField.setText(String.valueOf(promotion.getInscriptionCoursId()));
        apprenantIdField.setText(String.valueOf(promotion.getApprenantId()));
    }

    @FXML
    private void handleEnregistrer() {
        promotion.setCodePromo(codeField.getText());
        promotion.setDescription(descriptionField.getText());
        promotion.setRemise(Double.parseDouble(remiseField.getText()));
        promotion.setDateExpiration(dateExpirationField.getValue());
        promotion.setInscriptionCoursId(Integer.parseInt(inscriptionIdField.getText()));
        promotion.setApprenantId(Integer.parseInt(apprenantIdField.getText()));
        service.update(promotion);
        ((Stage) codeField.getScene().getWindow()).close();
    }

    @FXML
    private void handleAnnuler() {
        ((Stage) codeField.getScene().getWindow()).close();
    }
}