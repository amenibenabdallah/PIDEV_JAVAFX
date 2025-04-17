package tn.esprit.controllers.Formation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.services.FormationService;
import tn.esprit.models.Formation;

import java.sql.SQLException;

public class EditFormationController {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField dureeField;
    @FXML
    private TextField niveauField;
    @FXML
    private TextField prixField;

    private Formation formation;

    public void setFormation(Formation formation) {
        this.formation = formation;
        titreField.setText(formation.getTitre());
        descriptionField.setText(formation.getDescription());
        dureeField.setText(formation.getDuree());
        niveauField.setText(formation.getNiveau());
        prixField.setText(String.valueOf(formation.getPrix()));
    }

    @FXML
    private void handleSave() {
        if (formation != null) {
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            String duree = dureeField.getText().trim();
            String niveau = niveauField.getText().trim();
            String prixStr = prixField.getText().trim();

            // Vérifier que tous les champs sont remplis
            if (titre.isEmpty() || description.isEmpty() || duree.isEmpty() || niveau.isEmpty() || prixStr.isEmpty()) {
                showAlert("Veuillez remplir tous les champs.");
                return;
            }

            // Vérifier que la description contient au moins 8 caractères
            if (description.length() < 8) {
                showAlert("La description doit contenir au moins 8 caractères.");
                return;
            }

            // Vérifier que le prix est un nombre valide
            float prix;
            try {
                prix = Float.parseFloat(prixStr);
            } catch (NumberFormatException e) {
                showAlert("Le prix doit être un nombre valide.");
                return;
            }

            // Mise à jour de l'objet formation
            formation.setTitre(titre);
            formation.setDescription(description);
            formation.setDuree(duree);
            formation.setNiveau(niveau);
            formation.setPrix(prix);

            try {
                FormationService service = new FormationService();
                service.update(formation);
                showAlert("Formation mise à jour avec succès !");
                closeWindow();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la mise à jour de la formation.");
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
