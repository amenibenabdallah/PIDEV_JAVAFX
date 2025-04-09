package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
    private Label smileyIconLabel; // Renamed from heartIconLabel

    @FXML
    private Label avisCountLabel;

    @FXML
    private ProgressBar scoreProgressBar;

    private ServiceAvis serviceAvis;
    private static final int FORMATION_ID = 1; // Filter by Formation ID = 1

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
        averageScoreLabel.setText(String.format("Note Moyenne: %.1f/5", averageRating));
        // Apply conditional style based on the rating
        averageScoreLabel.getStyleClass().removeAll("score-label-good", "score-label-bad");
        scoreProgressBar.getStyleClass().removeAll("score-progress-good", "score-progress-bad");
        if (averageRating > 2.5) {
            averageScoreLabel.getStyleClass().add("score-label-good"); // Green for ratings above 2.5
            scoreProgressBar.getStyleClass().add("score-progress-good"); // Green progress bar
        } else {
            averageScoreLabel.getStyleClass().add("score-label-bad"); // Red for ratings 2.5 or below
            scoreProgressBar.getStyleClass().add("score-progress-bad"); // Red progress bar
        }

        // Update the progress bar (rating out of 5)
        scoreProgressBar.setProgress(averageRating / 5.0);

        // Update the smiley icon based on the rating
        if (averageRating > 4.0) {
            smileyIconLabel.setText("😊"); // Beaming face for ratings above 4
        } else if (averageRating >= 3.0) {
            smileyIconLabel.setText("🙂"); // Slightly smiling face for ratings between 3 and 4
        } else {
            smileyIconLabel.setText("😐"); // Neutral face for ratings below 3
        }

        avisCountLabel.setText("Nombre d'avis: " + avisCount);

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

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("date-label");

        HBox timeBox = new HBox(5);
        Label clockIcon = new Label("🕒"); // Clock icon (Unicode)
        clockIcon.getStyleClass().add("clock-icon");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("time-label");
        timeBox.getChildren().addAll(clockIcon, timeLabel);

        HBox dateTimeBox = new HBox(10);
        dateTimeBox.getChildren().addAll(dateLabel, timeBox);

        // Comment
        Label commentLabel = new Label(avis.getCommentaire());
        commentLabel.getStyleClass().add("comment-text");
        commentLabel.setWrapText(true);

        // Add all elements to the card
        card.getChildren().addAll(ratingBox, dateTimeBox, commentLabel);

        // Apply fade-in animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), card);
        fadeTransition.setFromValue(0.0); // Start at 0 opacity
        fadeTransition.setToValue(1.0);   // End at full opacity
        fadeTransition.play();

        return card;
    }

    @FXML
    private void handleAddAvis() {
        try {
            // Load the AddAvis.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddAvis.fxml"));
            Parent root = loader.load();

            // Create a new stage for the AddAvis window
            Stage addStage = new Stage();
            addStage.setTitle("Formiai application");
            addStage.setScene(new Scene(root));
            addStage.show();

            // Close the current ListAvis window
            Stage currentStage = (Stage) avisFlowPane.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the Add Avis window.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}