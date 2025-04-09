package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.models.Avis;
import tn.esprit.services.ServiceAvis;

import java.time.LocalDateTime;

public class AddAvis {

    @FXML
    private HBox starRatingBox;

    @FXML
    private TextField commentaireTextField;

    @FXML
    private ComboBox<Integer> formationIdComboBox;

    private ServiceAvis serviceAvis;
    private float rating = 0.0f; // Default rating set to 0
    private Label[] stars; // Array to hold star nodes

    @FXML
    public void initialize() {
        // Initialize the ServiceAvis
        serviceAvis = new ServiceAvis();

        // Initialize the star rating system
        stars = new Label[5];
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★"); // Use the same filled star for all
            star.getStyleClass().add("star"); // Apply unselected style initially
            final int index = i + 1;
            star.setOnMouseClicked(event -> setRating(index));
            stars[i] = star;
            starRatingBox.getChildren().add(star);
        }

        // Populate the formationId ComboBox with sample formation IDs (e.g., 1, 2, 3)
        formationIdComboBox.getItems().addAll(1, 2, 3);
        formationIdComboBox.setValue(1); // Default value
    }

    private void setRating(int newRating) {
        rating = newRating;
        // Update star visuals
        for (int i = 0; i < 5; i++) {
            stars[i].getStyleClass().setAll(i < rating ? "star-selected" : "star");

        }
    }

    @FXML
    private void handleAddAvis() {
        // Get the input values
        String commentaire = commentaireTextField.getText();
        Integer formationId = formationIdComboBox.getValue();

        // Validate the input
        if (commentaire == null || commentaire.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment cannot be empty.");
            return;
        }
        if (rating == 0.0f) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a rating.");
            return;
        }

        // Create a new Avis object
        Avis avis = new Avis(rating, commentaire, LocalDateTime.now(), formationId);

        // Add the Avis to the database
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