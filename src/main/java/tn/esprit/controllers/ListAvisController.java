package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.models.Avis;
import tn.esprit.services.ServiceAvis;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListAvisController {

    @FXML
    private FlowPane avisFlowPane;

    @FXML
    private Label averageScoreLabel;

    @FXML
    private Label avisCountLabel;

    @FXML
    private Button toggleAddFormButton;

    @FXML
    private VBox addAvisContainer;

    private ServiceAvis serviceAvis;
    private static final int FORMATION_ID = 1; // Filter by Formation ID = 1
    private AddAvis addAvisController; // Updated to AddAvis

    @FXML
    public void initialize() {
        // Initialize the ServiceAvis
        serviceAvis = new ServiceAvis();

        // Load the Avis data
        loadAvisData();
    }

    private void loadAvisData() {
        // Clear the FlowPane
        avisFlowPane.getChildren().clear();

        // Fetch all Avis records and filter by Formation ID = 1
        List<Avis> avisList = serviceAvis.getAll().stream()
                .filter(avis -> avis.getFormationId() == FORMATION_ID)
                .toList();

        // Calculate the average rating and number of Avis
        double averageRating = avisList.isEmpty() ? 0.0 : avisList.stream()
                .mapToDouble(Avis::getNote)
                .average()
                .orElse(0.0);
        int avisCount = avisList.size();

        // Update the labels
        averageScoreLabel.setText(String.format("%.1f/5", averageRating));
        avisCountLabel.setText(String.valueOf(avisCount));

        // Create a card for each filtered Avis
        for (Avis avis : avisList) {
            VBox card = createAvisCard(avis);
            avisFlowPane.getChildren().add(card);
        }
    }

    private VBox createAvisCard(Avis avis) {
        // Card container
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        // Rating (Stars)
        HBox ratingBox = new HBox(5);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add(i <= avis.getNote() ? "star-selected" : "star");
            ratingBox.getChildren().add(star);
        }

        // Date and Time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String date = avis.getDateCreation().format(dateFormatter);
        String time = avis.getDateCreation().format(timeFormatter);

        Label dateLabel = new Label(date + "  " + time);
        dateLabel.getStyleClass().add("date-label");

        // Comment
        Label commentLabel = new Label(avis.getCommentaire());
        commentLabel.getStyleClass().add("comment-text");
        commentLabel.setWrapText(true);

        // Add all elements to the card
        card.getChildren().addAll(ratingBox, dateLabel, commentLabel);

        // Apply fade-in animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), card);
        fadeTransition.setFromValue(0.0); // Start at 0 opacity
        fadeTransition.setToValue(1.0);   // End at full opacity
        fadeTransition.play();

        return card;
    }

    @FXML
    private void handleToggleAddForm() {
        try {
            if (addAvisContainer.isVisible()) {
                // Hide the form
                addAvisContainer.setVisible(false);
                addAvisContainer.setManaged(false);
                toggleAddFormButton.setText("Ajouter un avis");
            } else {
                // Load AddAvis.fxml if not already loaded
                if (addAvisContainer.getChildren().isEmpty()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddAvis.fxml"));
                    VBox addAvisForm = loader.load();
                    addAvisController = loader.getController(); // Updated to AddAvis
                    addAvisController.setListAvisController(this); // Pass reference to this controller
                    addAvisContainer.getChildren().setAll(addAvisForm);
                }
                // Show the form
                addAvisContainer.setVisible(true);
                addAvisContainer.setManaged(true);
                toggleAddFormButton.setText("Masquer le formulaire");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Add Avis form.");
        }
    }

    // Method to be called by AddAvis to refresh the list
    public void refreshAvisList() {
        loadAvisData();
        // Hide the form after adding
        addAvisContainer.setVisible(false);
        addAvisContainer.setManaged(false);
        toggleAddFormButton.setText("Ajouter un avis");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}