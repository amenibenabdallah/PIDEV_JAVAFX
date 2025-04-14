package tn.esprit.controllers.lecon;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.models.Lecon;
import tn.esprit.services.LeconService;

import java.sql.SQLException;

public class EditLeconController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea contenuField;

    private Lecon lecon;

    public void setLecon(Lecon lecon) {
        this.lecon = lecon;
        titreField.setText(lecon.getTitre());
        contenuField.setText(lecon.getContenu());
    }

    @FXML
    private void handleSave() {
        if (lecon != null) {
            lecon.setTitre(titreField.getText());
            lecon.setContenu(contenuField.getText());

            try {
                LeconService service = new LeconService();
                service.update(lecon);
                showAlert("Leçon mise à jour !");
                closeWindow();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la mise à jour.");
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
