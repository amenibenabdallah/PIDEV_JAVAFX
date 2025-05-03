package tn.esprit.controllers.lecon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.Formation;
import tn.esprit.models.Lecon;
import tn.esprit.services.FormationService;
import tn.esprit.services.LeconService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddLecon {

    @FXML
    private TextField titreTextField;

    @FXML
    private TextArea contenuTextArea;

    @FXML
    private DatePicker dateCreationPicker;

    @FXML
    private ComboBox<Formation> formationComboBox;

    @FXML
    private Label errorLabel;

    private final LeconService serviceLecon = new LeconService();
    private final FormationService serviceFormation = new FormationService();

    @FXML
    public void initialize() throws SQLException {
        FormationService formationService = new FormationService();
        ObservableList<Formation> formations = FXCollections.observableArrayList(formationService.getAll());
        formationComboBox.setItems(formations);
    }


    @FXML
    private void handleAddLecon() throws SQLException {
        String titre = titreTextField.getText();
        String contenu = contenuTextArea.getText();
        LocalDate dateCreation = dateCreationPicker.getValue();
        Formation formation = formationComboBox.getValue();

        if (titre.isEmpty() || contenu.isEmpty() || dateCreation == null || formation == null) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        Lecon lecon = new Lecon();
        lecon.setTitre(titre);
        lecon.setContenu(contenu);
        lecon.setDateCreation(dateCreation);
        lecon.setFormation(formation);
        lecon.setUpdatedAt(LocalDateTime.now());

        serviceLecon.add(lecon);
        errorLabel.setText("Leçon ajoutée avec succès !");
    }
}
