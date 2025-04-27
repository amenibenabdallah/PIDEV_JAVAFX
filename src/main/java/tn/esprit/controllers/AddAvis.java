package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import tn.esprit.models.Avis;
import tn.esprit.models.FormationA;
import tn.esprit.services.ServiceAvis;
import tn.esprit.services.FormationServiceA;
import tn.esprit.services.TextCorrectionService;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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
    private ComboBox<FormationA> formationComboBox;

    @FXML
    private VBox addAvisForm;

    @FXML
    private Button addButton;

    @FXML
    private Label titleLabel;

    private ServiceAvis serviceAvis;
    private FormationServiceA formationService;
    private TextCorrectionService correctionService;
    private ListAvisController listAvisController;
    private float selectedRating = 0;
    private Avis currentAvis;
    private final SessionManager sessionManager = new SessionManager();
    private PauseTransition debounceTimer;

    @FXML
    public void initialize() {
        serviceAvis = new ServiceAvis();
        formationService = new FormationServiceA();
        correctionService = new TextCorrectionService();
        debounceTimer = new PauseTransition(Duration.millis(500));
        initializeFormationComboBox();
        setupRealTimeAutocorrect();
    }

    private void initializeFormationComboBox() {
        try {
            List<FormationA> formations = formationService.getAllFormations();
            formationComboBox.getItems().addAll(formations);
            formationComboBox.setCellFactory(param -> new ListCell<FormationA>() {
                @Override
                protected void updateItem(FormationA formation, boolean empty) {
                    super.updateItem(formation, empty);
                    setText(empty || formation == null ? null : formation.getName());
                }
            });
            formationComboBox.setButtonCell(new ListCell<FormationA>() {
                @Override
                protected void updateItem(FormationA formation, boolean empty) {
                    super.updateItem(formation, empty);
                    setText(empty || formation == null ? null : formation.getName());
                }
            });
            if (!formations.isEmpty()) {
                formationComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les formations.");
        }
    }

    private void setupRealTimeAutocorrect() {
        commentaireField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                return;
            }
            debounceTimer.setOnFinished(event -> {
                String currentText = commentaireField.getText();
                if (currentText == null || currentText.trim().isEmpty()) {
                    return;
                }
                System.out.println("Checking text: " + currentText); // Debug
                List<TextCorrectionService.Correction> corrections = correctionService.checkText(currentText, "fr-FR");
                System.out.println("Corrections found: " + corrections.size()); // Debug
                if (!corrections.isEmpty()) {
                    Platform.runLater(() -> applyAutoCorrections(currentText, corrections));
                }
            });
            debounceTimer.playFromStart();
        });
    }

    private void applyAutoCorrections(String text, List<TextCorrectionService.Correction> corrections) {
        String currentText = commentaireField.getText();
        if (!currentText.equals(text)) {
            System.out.println("Text changed, retrying corrections"); // Debug
            // Retry with current text
            List<TextCorrectionService.Correction> newCorrections = correctionService.checkText(currentText, "fr-FR");
            if (!newCorrections.isEmpty()) {
                applyAutoCorrections(currentText, newCorrections);
            }
            return;
        }

        int caretPosition = commentaireField.getCaretPosition();
        StringBuilder correctedText = new StringBuilder(text);
        int offsetAdjustment = 0;

        for (TextCorrectionService.Correction correction : corrections) {
            if (!correction.getSuggestions().isEmpty()) {
                int start = correction.getOffset();
                int end = start + correction.getLength();
                if (start >= 0 && end <= text.length() && start <= end) {
                    String suggestion = correction.getSuggestions().get(0);
                    System.out.println("Correcting: " + correction.getWord() + " to " + suggestion); // Debug
                    correctedText.replace(start + offsetAdjustment, end + offsetAdjustment, suggestion);
                    offsetAdjustment += suggestion.length() - correction.getLength();
                } else {
                    System.out.println("Invalid offset: start=" + start + ", end=" + end + ", text length=" + text.length()); // Debug
                }
            }
        }

        if (!correctedText.toString().equals(text)) {
            commentaireField.setText(correctedText.toString());
            int newCaretPosition = Math.min(caretPosition + offsetAdjustment, correctedText.length());
            commentaireField.positionCaret(newCaretPosition);
            System.out.println("Applied corrected text: " + correctedText); // Debug
        }
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

            FormationA selectedFormation = formationComboBox.getValue();
            if (selectedFormation == null) {
                showAlert("Erreur", "Veuillez sélectionner une formation.");
                return;
            }
            int formationId = selectedFormation.getId();

            if (sessionManager.getUtilisateurConnecte() == null) {
                showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter.");
                return;
            }
            int apprenantId = sessionManager.getUtilisateurConnecte().getId();

            if (currentAvis == null) {
                Avis newAvis = new Avis();
                newAvis.setNote(selectedRating);
                newAvis.setCommentaire(commentaire);
                newAvis.setFormationId(formationId);
                newAvis.setApprenantId(apprenantId);
                newAvis.setDateCreation(LocalDateTime.now());
                serviceAvis.add(newAvis);
                showAlert("Succès", "Avis ajouté avec succès");
            } else {
                currentAvis.setNote(selectedRating);
                currentAvis.setCommentaire(commentaire);
                currentAvis.setFormationId(formationId);
                currentAvis.setApprenantId(apprenantId);
                currentAvis.setDateCreation(LocalDateTime.now());
                serviceAvis.update(currentAvis);
                showAlert("Succès", "Avis mis à jour avec succès");
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
        try {
            FormationA formation = formationService.getById(avis.getFormationId());
            if (formation != null) {
                formationComboBox.setValue(formation);
            } else {
                formationComboBox.getSelectionModel().clearSelection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la formation pour cet avis.");
        }
        updateStarStyles();
        addButton.setText("Mettre à jour");
        titleLabel.setText("Modifier l'Avis");
    }

    public void clearForm() {
        selectedRating = 0;
        updateStarStyles();
        commentaireField.clear();
        formationComboBox.getSelectionModel().clearSelection();
        if (!formationComboBox.getItems().isEmpty()) {
            formationComboBox.getSelectionModel().selectFirst();
        }
        currentAvis = null;
        addButton.setText("Ajouter");
        titleLabel.setText("Laisser un Avis");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
