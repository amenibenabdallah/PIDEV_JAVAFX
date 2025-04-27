package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.models.Avis;
import tn.esprit.models.FormationA;
import tn.esprit.services.ServiceAvis;
import tn.esprit.services.FormationServiceA;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

    @FXML
    private ComboBox<FormationA> formationComboBox;

    private ServiceAvis serviceAvis;
    private FormationServiceA serviceFormation;
    private AddAvis addAvisController;
    private final SessionManager sessionManager = new SessionManager();

    @FXML
    public void initialize() {
        System.out.println("Initializing ListAvisController...");
        serviceAvis = new ServiceAvis();
        serviceFormation = new FormationServiceA();
        initializeFormationComboBox();
        loadAvisData(null);
    }

    private void initializeFormationComboBox() {
        if (formationComboBox == null) {
            System.err.println("formationComboBox is null. Check fx:id in ListAvis.fxml");
            return;
        }
        try {
            List<FormationA> formations = serviceFormation.getAllFormations();
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
            formationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                loadAvisData(newSelection != null ? newSelection.getId() : null);
            });
            if (!formations.isEmpty()) {
                formationComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load formations.");
        }
    }

    private void loadAvisData(Integer formationId) {
        avisFlowPane.getChildren().clear();
        List<Avis> avisList = serviceAvis.getAll();
        if (formationId != null) {
            avisList = avisList.stream()
                    .filter(avis -> avis.getFormationId() == formationId)
                    .toList();
        }

        double averageRating = avisList.isEmpty() ? 0.0 : avisList.stream()
                .mapToDouble(Avis::getNote)
                .average()
                .orElse(0.0);
        int avisCount = avisList.size();

        averageScoreLabel.setText(String.format("%.1f/5", averageRating));
        avisCountLabel.setText(String.valueOf(avisCount));

        for (Avis avis : avisList) {
            VBox card = createAvisCard(avis);
            avisFlowPane.getChildren().add(card);
        }
    }

    private VBox createAvisCard(Avis avis) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");




        HBox ratingBox = new HBox(8);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add(i <= Math.round(avis.getNote()) ? "star-selected" : "star");
            ratingBox.getChildren().add(star);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String date = avis.getDateCreation().format(dateFormatter);
        String time = avis.getDateCreation().format(timeFormatter);

        Label dateLabel = new Label(date + "  " + time);
        dateLabel.getStyleClass().add("date-label");

        Label commentLabel = new Label(avis.getCommentaire());
        commentLabel.getStyleClass().add("comment-text");
        commentLabel.setWrapText(true);

        HBox buttonBox = new HBox(15);
        if (sessionManager.getUtilisateurConnecte() != null) {
            int connectedUserId = sessionManager.getUtilisateurConnecte().getId();
            System.out.println("Avis ID: " + avis.getId() + ", Avis apprenant_id: " + avis.getApprenantId() +
                    ", Connected user_id: " + connectedUserId);

            if (avis.getApprenantId() == connectedUserId) {
                System.out.println("Showing Edit/Delete buttons for Avis ID: " + avis.getId());
                Button editButton = new Button("Edit");
                editButton.getStyleClass().addAll("button", "edit-button");
                editButton.setOnAction(e -> handleEdit(avis));

                Button deleteButton = new Button("🗑️");
                deleteButton.getStyleClass().addAll("button", "delete-button");
                deleteButton.setOnAction(e -> handleDelete(avis));

                buttonBox.getChildren().addAll(editButton, deleteButton);
            } else {
                System.out.println("Hiding Edit/Delete buttons for Avis ID: " + avis.getId() + " (apprenant_id mismatch)");
            }
        } else {
            System.out.println("No user logged in. Hiding Edit/Delete buttons for Avis ID: " + avis.getId());
        }

        card.getChildren().addAll( ratingBox, dateLabel, commentLabel, buttonBox);

        FadeTransition fade = new FadeTransition(Duration.millis(600), card);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        return card;
    }

    @FXML
    private void handleToggleAddForm() {
        try {
            if (addAvisContainer.isVisible()) {
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), addAvisContainer);
                slideOut.setToX(300);
                slideOut.setOnFinished(e -> {
                    addAvisContainer.setVisible(false);
                    addAvisContainer.setManaged(false);
                    addAvisContainer.setTranslateX(0);
                    toggleAddFormButton.setText("Ajouter un avis");
                    if (addAvisController != null) {
                        addAvisController.clearForm();
                    }
                });
                slideOut.play();
            } else {
                if (addAvisContainer.getChildren().isEmpty()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddAvis.fxml"));
                    VBox addAvisForm = loader.load();
                    addAvisController = loader.getController();
                    addAvisController.setListAvisController(this);
                    addAvisContainer.getChildren().setAll(addAvisForm);
                }
                addAvisContainer.setVisible(true);
                addAvisContainer.setManaged(true);
                toggleAddFormButton.setText("Masquer le formulaire");
                addAvisContainer.setTranslateX(300);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), addAvisContainer);
                slideIn.setToX(0);
                slideIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Add Avis form.");
        }
    }

    private void handleEdit(Avis avis) {
        try {
            if (addAvisContainer.getChildren().isEmpty()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddAvis.fxml"));
                VBox addAvisForm = loader.load();
                addAvisController = loader.getController();
                addAvisController.setListAvisController(this);
                addAvisContainer.getChildren().setAll(addAvisForm);
            }
            addAvisController.editAvis(avis);
            addAvisContainer.setVisible(true);
            addAvisContainer.setManaged(true);
            toggleAddFormButton.setText("Masquer le formulaire");
            addAvisContainer.setTranslateX(300);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), addAvisContainer);
            slideIn.setToX(0);
            slideIn.play();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Edit Avis form.");
        }
    }

    private void handleDelete(Avis avis) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cet avis ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceAvis.delete(avis);
            refreshAvisList();
        }
    }

    public void refreshAvisList() {
        loadAvisData(formationComboBox != null && formationComboBox.getValue() != null ? formationComboBox.getValue().getId() : null);
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