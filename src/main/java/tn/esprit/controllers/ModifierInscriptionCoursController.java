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
        if (selected != null) {
            try {
                // Mise à jour des champs
                selected.setNomApprenant(nomApprenantField.getText());
                selected.setEmail(emailField.getText());
                selected.setCin(cinField.getText());
                selected.setNomFormation(nomFormationField.getText());
                selected.setMontant(Double.parseDouble(montantField.getText()));
                selected.setStatus(statusComboBox.getValue());
                selected.setTypePaiement(typePaiementComboBox.getValue());
                selected.setApprenantId(selected.getApprenantId());
                selected.setFormationId(selected.getFormationId());
                // Log des valeurs pour débogage
                System.out.println("🆔 ID à modifier : " + selected.getId());
                System.out.println("🔁 Mise à jour de l'inscription avec ID : " + selected.getId());

                // Appel au service pour mettre à jour dans la base de données
                service.update(selected);

                // Fermer la fenêtre actuelle
                Stage stage = (Stage) nomApprenantField.getScene().getWindow();
                stage.close();

                // Réouvrir l'interface d'affichage
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherInscriptionView.fxml"));
                Parent root = loader.load();
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("Liste des Inscriptions");
                newStage.show();

            } catch (Exception e) {
                e.printStackTrace(); // Affiche la trace d'erreur complète
                showAlert("Erreur", "Une erreur s'est produite lors de la modification.");
            }
        }
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
