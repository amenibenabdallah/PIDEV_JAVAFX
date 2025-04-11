package tn.esprit.controllers;

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
    @FXML private TextField txtTypePaiement;
    @FXML private TextField txtMontant;
    @FXML private TextField txtStatus;
    @FXML private TextField txtApprenantId;
    @FXML private TextField txtFormationId;
    @FXML private Button btnAjouter;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();

    @FXML
    public void initialize() {
        btnAjouter.setOnAction(event -> ajouterInscription());
    }

    private void ajouterInscription() {
        try {
            InscriptionCours ins = new InscriptionCours();
            ins.setNomApprenant(txtNomApprenant.getText());
            ins.setCin(txtCin.getText());
            ins.setEmail(txtEmail.getText());
            ins.setNomFormation(txtNomFormation.getText());
            ins.setTypePaiement(txtTypePaiement.getText());
            ins.setMontant(Double.parseDouble(txtMontant.getText()));
            ins.setStatus(txtStatus.getText());
            ins.setApprenantId(Integer.parseInt(txtApprenantId.getText()));
            ins.setFormationId(Integer.parseInt(txtFormationId.getText()));
            ins.setDateInscreption(LocalDateTime.now());

            service.add(ins);

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) btnAjouter.getScene().getWindow();
            currentStage.close();

            // Ouvrir l'interface d'affichage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherInscriptionView.fxml"));
            Parent root = loader.load();
            Stage displayStage = new Stage();
            displayStage.setScene(new Scene(root));
            displayStage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}