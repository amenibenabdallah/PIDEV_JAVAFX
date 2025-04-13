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

    @FXML
    private Button addButton;

    @FXML
    private Label titleLabel; // New reference for dynamic title

    private ServiceAvis serviceAvis;
    private ListAvisController listAvisController;
    private static final int FORMATION_ID = 1;
    private float selectedRating = 0;
    private Avis currentAvis;

    @FXML
    public void initialize() {
        serviceAvis = new ServiceAvis();
        formationComboBox.setItems(FXCollections.observableArrayList("1"));
        formationComboBox.setValue("1");
    }

    public void setListAvisController(ListAvisController controller) {
        this.listAvisController = controller;
    }

    @FXML
    private void handleStarClick(MouseEvent event) {
        Label clickedStar = (Label) event.getSource();
        String starId = clickedStar.getId();
        selectedRating = switch (starId) {
            case "star1" -> 1.0f;
            case "star2" -> 2.0f;
            case "star3" -> 3.0f;
            case "star4" -> 4.0f;
            case "star5" -> 5.0f;
            default -> 0.0f;
        };
        updateStarStyles();
    }

    private void updateStarStyles() {
        star1.getStyleClass().setAll("star", selectedRating >= 1 ? "star-selected" : "star");
        star2.getStyleClass().setAll("star", selectedRating >= 2 ? "star-selected" : "star");
        star3.getStyleClass().setAll("star", selectedRating >= 3 ? "star-selected" : "star");
        star4.getStyleClass().setAll("star", selectedRating >= 4 ? "star-selected" : "star");
        star5.getStyleClass().setAll("star", selectedRating >= 5 ? "star-selected" : "star");
    }

    @FXML
    private void handleAdd() {
        try {
            if (selectedRating < 1 || selectedRating > 5) {
                showAlert("Erreur", "Veuillez sélectionner une note (1 à 5 étoiles).");
                return;
            }

            String commentaire = commentaireField.getText();
            if (commentaire == null || commentaire.trim().isEmpty()) {
                showAlert("Erreur", "Le commentaire ne peut pas être vide.");
                return;
            }

            String formationIdStr = formationComboBox.getValue();
            int formationId = Integer.parseInt(formationIdStr);

            if (currentAvis == null) {
                Avis newAvis = new Avis();
                newAvis.setNote(selectedRating);
                newAvis.setCommentaire(commentaire);
                newAvis.setFormationId(formationId);
                newAvis.setDateCreation(LocalDateTime.now());
                serviceAvis.add(newAvis);
            } else {
                currentAvis.setNote(selectedRating);
                currentAvis.setCommentaire(commentaire);
                currentAvis.setFormationId(formationId);
                currentAvis.setDateCreation(LocalDateTime.now());
                serviceAvis.update(currentAvis);
            }

            if (listAvisController != null) {
                listAvisController.refreshAvisList();
            }

            clearForm();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur s'est produite lors de l'opération.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        if (listAvisController != null) {
            listAvisController.refreshAvisList();
        }
    }

    public void editAvis(Avis avis) {
        currentAvis = avis;
        selectedRating = avis.getNote();
        commentaireField.setText(avis.getCommentaire());
        formationComboBox.setValue(String.valueOf(avis.getFormationId()));
        updateStarStyles();
        addButton.setText("Update Avis");
        titleLabel.setText("Edit Avis"); // Update title
    }

     void clearForm() {
        selectedRating = 0;
        updateStarStyles();
        commentaireField.clear();
        formationComboBox.setValue("1");
        currentAvis = null;
        addButton.setText("Add Avis");
        titleLabel.setText("Add New Avis"); // Reset title
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}