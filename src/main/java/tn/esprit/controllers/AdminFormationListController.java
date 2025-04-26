package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Avis;
import tn.esprit.models.FormationA;
import tn.esprit.services.FormationServiceA;
import tn.esprit.services.ServiceAvis;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AdminFormationListController implements Initializable, Searchable {

    @FXML
    private FlowPane formationFlowPane;

    @FXML
    private Label selectedFormationLabel;

    @FXML
    private VBox avisContainer;

    @FXML
    private FlowPane avisFlowPane;

    @FXML
    private Button hideAvisButton;

    private ObservableList<FormationA> formationList;
    private ObservableList<FormationA> filteredFormationList;
    private final FormationServiceA formationService = new FormationServiceA();
    private final ServiceAvis serviceAvis = new ServiceAvis();
    private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private final List<String> badWords = Arrays.asList("bad", "stupid", "idiot", "terrible", "awful");
    private FormationA currentFormation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load formations
        loadFormations();

        // Initialize the filtered list
        filteredFormationList = FXCollections.observableArrayList(formationList);

        // Style the hide avis button
        hideAvisButton.setPadding(new Insets(8, 20, 8, 20));

        // Bind FlowPane size to parent for responsive layout
        Parent parent = formationFlowPane.getParent();

    }

    private void loadFormations() {
        try {
            formationList = FXCollections.observableArrayList(formationService.getAllFormations());
            updateFormationTiles(formationList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les formations : " + e.getMessage());
        }
    }

    private void updateFormationTiles(ObservableList<FormationA> formations) {
        formationFlowPane.getChildren().clear();
        for (FormationA formation : formations) {
            VBox tile = createFormationTile(formation);
            formationFlowPane.getChildren().add(tile);
        }
    }

    private VBox createFormationTile(FormationA formation) {
        VBox tile = new VBox(10);
        tile.getStyleClass().add("formation-tile");
        tile.setPrefWidth(200);
        tile.setPrefHeight(200);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(15));

        // Formation Name
        Label nameLabel = new Label(formation.getName());
        nameLabel.getStyleClass().add("formation-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        // Average Score with Animated Stars
        HBox scoreBox = new HBox(4);
        scoreBox.getChildren().addAll(createStarBox(formation.getAverageScore()));
        Label scoreLabel = new Label(decimalFormat.format(formation.getAverageScore()) + "/5");
        scoreLabel.getStyleClass().add("score-label");
        scoreBox.getChildren().add(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        // Number of Reviews
        Label avisCountLabel = new Label(formation.getAvisCount() + " Avis");
        avisCountLabel.getStyleClass().add("avis-count");

        // Action Button
        Button avisButton = new Button("Avis");
        avisButton.getStyleClass().add("action-button");
        avisButton.setPadding(new Insets(6, 12, 6, 12));
        avisButton.setOnAction(event -> handleShowAvis(formation));

        tile.getChildren().addAll(nameLabel, scoreBox, avisCountLabel, avisButton);

        // Hover effect (glow and tilt)
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), tile);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        tile.setOnMouseEntered(e -> {
            tile.setStyle("-fx-effect: dropshadow(gaussian, #00aaff, 20, 0.7, 0, 0); -fx-rotate: 3;");
            scale.play();
        });
        tile.setOnMouseExited(e -> {
            tile.setStyle("-fx-effect: none; -fx-rotate: 0;");
            scale.stop();
            tile.setScaleX(1);
            tile.setScaleY(1);
        });

        return tile;
    }

    private List<Label> createStarBox(double score) {
        List<Label> stars = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add(i <= (int) score ? "star-selected" : "star");
            ScaleTransition starAnimation = new ScaleTransition(Duration.millis(200), star);
            starAnimation.setToX(1.2);
            starAnimation.setToY(1.2);
            starAnimation.setAutoReverse(true);
            starAnimation.setCycleCount(2);
            star.setOnMouseEntered(e -> starAnimation.play());
            stars.add(star);
        }
        return stars;
    }

    @Override
    public void handleSearch(String searchText) {
        if (searchText.isEmpty()) {
            updateFormationTiles(formationList);
        } else {
            filteredFormationList.clear();
            filteredFormationList.addAll(formationList.stream()
                    .filter(formation -> formation.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .toList());
            updateFormationTiles(filteredFormationList);
        }
    }

    private void handleShowAvis(FormationA selectedFormation) {
        if (selectedFormation == null) {
            return;
        }

        currentFormation = selectedFormation;
        selectedFormationLabel.setText("Avis for " + selectedFormation.getName());
        avisFlowPane.getChildren().clear();

        for (Avis avis : selectedFormation.getAvisList()) {
            VBox card = createAvisCard(avis);
            // Entrance animation for cards
            FadeTransition fade = new FadeTransition(Duration.millis(500), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            TranslateTransition translate = new TranslateTransition(Duration.millis(500), card);
            translate.setFromX(20);
            translate.setToX(0);
            fade.play();
            translate.play();
            avisFlowPane.getChildren().add(card);
        }

        avisContainer.setVisible(true);
        avisContainer.setManaged(true);
    }

    @FXML
    private void handleHideAvis() {
        avisContainer.setVisible(false);
        avisContainer.setManaged(false);
        currentFormation = null;
    }

    private VBox createAvisCard(Avis avis) {
        VBox card = new VBox(5);
        card.getStyleClass().add("avis-card");
        card.setPrefWidth(350);
        card.setPrefHeight(220);
        card.setMinHeight(220);
        card.setMaxHeight(220);

        // Check for bad words
        boolean hasBadWord = containsBadWord(avis.getCommentaire());
        if (hasBadWord) {
            card.setStyle("-fx-border-color: #ff4040; -fx-border-width: 2; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, #ff4040, 10, 0.3, 0, 0);");

            HBox flaggedBox = new HBox(5);
            flaggedBox.setAlignment(Pos.CENTER_LEFT);
            Label flagIcon = new Label("\uD83D\uDEA9"); // Flag emoji (🚩)
            flagIcon.setStyle("-fx-text-fill: #ff4040; -fx-font-size: 18px; -fx-font-family: 'Montserrat';");
            Label flaggedLabel = new Label(" Inappropriate Content");
            flaggedLabel.setStyle("-fx-text-fill: #ff4040; -fx-font-weight: bold; -fx-font-family: 'Montserrat';");
            flaggedBox.getChildren().addAll(flagIcon, flaggedLabel);
            flaggedBox.setStyle("-fx-background-color: #ffe6e6; -fx-background-radius: 5; -fx-padding: 8;");
            card.getChildren().add(flaggedBox);
        }

        HBox starBox = new HBox(5);
        starBox.getChildren().addAll(createStarBox(avis.getNote()));
        if (!hasBadWord) {
            starBox.setPadding(new Insets(40, 0, 0, 0));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Label dateLabel = new Label(avis.getDateCreation().format(formatter));
        dateLabel.getStyleClass().add("date-label");

        Label commentLabel = new Label(avis.getCommentaire());
        commentLabel.getStyleClass().add("comment-text");
        commentLabel.setWrapText(true);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(0, 10, 10, 0));
        buttonBox.setMinHeight(40);

        Button deleteButton = new Button();
        Label trashIcon = new Label("\uD83D\uDDD1"); // Trash can emoji (🗑)
        trashIcon.setStyle("-fx-text-fill: #ff4040; -fx-font-size: 35px; -fx-font-weight: bold;");
        deleteButton.setGraphic(trashIcon);
        deleteButton.setStyle("-fx-background-color: transparent; -fx-padding: 5;");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> handleDeleteAvis(avis));
        buttonBox.getChildren().add(deleteButton);

        card.getChildren().addAll(starBox, dateLabel, commentLabel, buttonBox);

        // Hover effect (gradient border)
        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: linear-gradient(to right, #2b6cb0, #6b46c1); -fx-border-width: 2;"));
        card.setOnMouseExited(e -> {
            if (!hasBadWord) {
                card.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1;");
            }
        });

        return card;
    }

    private boolean containsBadWord(String comment) {
        if (comment == null) {
            return false;
        }
        String lowerCaseComment = comment.toLowerCase();
        return badWords.stream().anyMatch(badWord -> lowerCaseComment.contains(badWord.toLowerCase()));
    }

    private void handleDeleteAvis(Avis avis) {
        boolean isFlagged = containsBadWord(avis.getCommentaire());
        if (!isFlagged) {
            showAlert("Erreur", "Vous ne pouvez pas supprimer cet avis, il respecte notre politique");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Supprimer Avis");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet avis ?");
        confirmAlert.setContentText("Cette action est irréversible.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                serviceAvis.delete(avis);
                showAlert("Succès", "Avis signalé supprimé avec succès");
                loadFormations();
                if (currentFormation != null) {
                    currentFormation = formationList.stream()
                            .filter(f -> f.getId() == currentFormation.getId())
                            .findFirst()
                            .orElse(null);
                    if (currentFormation != null) {
                        handleShowAvis(currentFormation);
                    } else {
                        handleHideAvis();
                    }
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}