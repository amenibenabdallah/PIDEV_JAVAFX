package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Avis;
import tn.esprit.services.ServiceAvis;

import java.time.LocalDateTime;

public class AddAvis {

    @FXML
    private ComboBox<Float> noteComboBox;

    @FXML
    private TextField commentaireTextField;

    @FXML
    private ComboBox<Integer> formationIdComboBox;

    private ServiceAvis serviceAvis;

    @FXML
    public void initialize() {
        // Initialize the ServiceAvis
        serviceAvis = new ServiceAvis();

        // Populate the note ComboBox with rating options (1.0 to 5.0)
        noteComboBox.getItems().addAll(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);
        noteComboBox.setValue(1.0f); // Default value

        // Populate the formationId ComboBox with sample formation IDs (e.g., 1, 2, 3)
        // In a real application, you’d fetch these from a ServiceFormation
        formationIdComboBox.getItems().addAll(1, 2, 3);
        formationIdComboBox.setValue(1); // Default value
    }

    @FXML
    private void handleAddAvis() {
        // Get the input values
        Float note = noteComboBox.getValue();
        String commentaire = commentaireTextField.getText();
        Integer formationId = formationIdComboBox.getValue();

        // Validate the input
        if (commentaire == null || commentaire.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment cannot be empty.");
            return;
        }

        // Create a new Avis object
        Avis avis = new Avis(note, commentaire, LocalDateTime.now(), formationId);

        // Add the Avis to the database
        // Since add is void, we rely on the ID being set on the Avis object
        int originalId = avis.getId(); // Should be 0 before adding
        serviceAvis.add(avis);
        int generatedId = avis.getId(); // Get the ID after adding

        // Check if the ID changed (indicating a successful add)
        if (generatedId != originalId && generatedId > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Avis added successfully with ID: " + generatedId);
            // Close the window
            Stage stage = (Stage) commentaireTextField.getScene().getWindow();
            stage.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add Avis.");
        }
    }

    @FXML
    private void handleCancel() {
        // Close the window without saving
        Stage stage = (Stage) commentaireTextField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}