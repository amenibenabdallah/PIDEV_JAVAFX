package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Avis;
import tn.esprit.services.ServiceAvis;

import java.time.LocalDateTime;

public class AddAvis {

    @FXML
    private HBox ratingBox;

    @FXML
    private Label star1;

    @FXML
    private Label star2;

    @FXML
    private Label star3;

    @FXML
    private Label star4;

    @FXML
    private Label star5;

    @FXML
    private TextArea commentaireField;

    @FXML
    private ComboBox<String> formationComboBox;

    @FXML
    private VBox addAvisForm;

    private ServiceAvis serviceAvis;
    private ListAvisController listAvisController;
    private static final int FORMATION_ID = 1;
    private int selectedRating = 0; // Store the selected rating

    @FXML
    public void initialize() {
        serviceAvis = new ServiceAvis();
        // Initialize the ComboBox (already set to "1" in FXML)
    }

    public void setListAvisController(ListAvisController controller) {
        this.listAvisController = controller;
    }

    @FXML
    private void handleStarClick(MouseEvent event) {
        Label clickedStar = (Label) event.getSource();
        String starId = clickedStar.getId();
        int rating = switch (starId) {
            case "star1" -> 1;
            case "star2" -> 2;
            case "star3" -> 3;
            case "star4" -> 4;
            case "star5" -> 5;
            default -> 0;
        };

        selectedRating = rating;
        updateStarStyles();
    }

    private void updateStarStyles() {
        // Update the style of each star based on the selected rating
        star1.getStyleClass().setAll("star", selectedRating >= 1 ? "star-selected" : "star");
        star2.getStyleClass().setAll("star", selectedRating >= 2 ? "star-selected" : "star");
        star3.getStyleClass().setAll("star", selectedRating >= 3 ? "star-selected" : "star");
        star4.getStyleClass().setAll("star", selectedRating >= 4 ? "star-selected" : "star");
        star5.getStyleClass().setAll("star", selectedRating >= 5 ? "star-selected" : "star");
    }

    @FXML
    private void handleAdd() {
        try {
            // Validate the rating
            if (selectedRating < 1 || selectedRating > 5) {
                showAlert("Erreur", "Veuillez sélectionner une note (1 à 5 étoiles).");
                return;
            }

            // Get the comment
            String commentaire = commentaireField.getText();
            if (commentaire == null || commentaire.trim().isEmpty()) {
                showAlert("Erreur", "Le commentaire ne peut pas être vide.");
                return;
            }

            // Get the formation ID
            String formationIdStr = formationComboBox.getValue();
            int formationId = Integer.parseInt(formationIdStr);

            // Create a new Avis
            Avis newAvis = new Avis();
            newAvis.setNote(selectedRating);
            newAvis.setCommentaire(commentaire);
            newAvis.setFormationId(formationId);
            newAvis.setDateCreation(LocalDateTime.now());

            // Add the Avis using the service
            serviceAvis.add(newAvis);

            // Refresh the list in ListAvisController
            if (listAvisController != null) {
                listAvisController.refreshAvisList();
            }

            // Clear the form
            clearForm();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur s'est produite lors de l'ajout de l'avis.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        // Clear the form
        clearForm();
        // Let ListAvisController handle the visibility
        if (listAvisController != null) {
            listAvisController.refreshAvisList();
        }
    }

    private void clearForm() {
        selectedRating = 0;
        updateStarStyles();
        commentaireField.clear();
        formationComboBox.setValue("1");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}