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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.json.JSONObject; // Added for JSON parsing

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
    private FormationA currentFormation;

    // SightEngine API Key (store securely in production)
    private static final String SIGHT_ENGINE_API_KEY = "1126907624"; // Replace with your SightEngine API key
    private static final String SIGHT_ENGINE_API_SECRET = "CL3tBwEJEJuRU9vZTaAre4KeVkVx5e6A"; // Replace with your SightEngine API secret
    private static final String SIGHT_ENGINE_API_URL = "https://api.sightengine.com/1.0/text/check.json";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load formations
        loadFormations();

        // Initialize the filtered list
        filteredFormationList = FXCollections.observableArrayList(formationList);

        // Style the hide avis button
        hideAvisButton.setPadding(new Insets(8, 20, 8, 20));

        // Bind FlowPane size to parent for responsive layout
        @SuppressWarnings("unused")
        Parent parent = formationFlowPane.getParent();

    }


    private void loadFormations() {
        try {
            formationList = FXCollections.observableArrayList(formationService.getAllFormations());
            updateFormationTiles(formationList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load formations: " + e.getMessage());
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
        Label avisCountLabel = new Label(formation.getAvisCount() + "");
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
            boolean hasBadWord = containsBadWord(avis.getCommentaire());
            VBox card = createAvisCard(avis, hasBadWord);
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

    private VBox createAvisCard(Avis avis, boolean hasBadWord) {
        VBox card = new VBox(5);
        card.getStyleClass().add("avis-card");
        card.setPrefWidth(350);
        card.setPrefHeight(220);
        card.setMinHeight(220);
        card.setMaxHeight(220);

        if (hasBadWord) {
            card.setStyle("-fx-border-color: #ff4040; -fx-border-width: 2; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, #ff4040, 10, 0.3, 0, 0);");

            HBox flaggedBox = new HBox(5);
            flaggedBox.setAlignment(Pos.CENTER_LEFT);
            Label flagIcon = new Label("\uD83D\uDEA9"); // Flag emoji (🚩)
            flagIcon.setStyle("-fx-text-fill: #ff4040; -fx-font-size: 18px; -fx-font-family: 'Montserrat';");
            Label flaggedLabel = new Label(" Flagged: Inappropriate Content");
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
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        try {
            // Build SightEngine API request
            String params = String.format("text=%s&lang=fr&mode=rules,ml&api_user=%s&api_secret=%s",
                    URLEncoder.encode(comment, StandardCharsets.UTF_8), SIGHT_ENGINE_API_KEY, SIGHT_ENGINE_API_SECRET);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SIGHT_ENGINE_API_URL + "?" + params))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Log the API response for debugging
            System.out.println("SightEngine API Response for comment \"" + comment + "\": " + responseBody);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Check if the response indicates success
            if (!jsonResponse.getString("status").equals("success")) {
                System.out.println("SightEngine API call failed: " + responseBody);
                throw new Exception("API call failed: " + jsonResponse.getString("error"));
            }

            // Check for profanity matches
            JSONObject profanity = jsonResponse.getJSONObject("profanity");
            boolean hasProfanity = profanity.getJSONArray("matches").length() > 0;

            System.out.println("Profanity detected in comment \"" + comment + "\": " + (hasProfanity ? "Yes" : "No"));
            return hasProfanity;
        } catch (Exception e) {
            System.out.println("Error checking bad word for comment \"" + comment + "\": " + e.getMessage());
            e.printStackTrace();
            // Fallback to local check in case of API failure
            List<String> badWords = Arrays.asList("bad", "stupid", "idiot", "terrible", "awful");
            boolean hasBadWord = badWords.stream().anyMatch(word -> comment.toLowerCase().contains(word));
            System.out.println("Fallback to local list for comment \"" + comment + "\": " + (hasBadWord ? "Flagged" : "Not flagged"));
            return hasBadWord;
        }
    }

    private void handleDeleteAvis(Avis avis) {
        boolean isFlagged = containsBadWord(avis.getCommentaire());
        if (!isFlagged) {
            showAlert("Error", "You cannot delete this review, it complies with our policy");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Review");
        confirmAlert.setHeaderText("Are you sure you want to delete this review?");
        confirmAlert.setContentText("Reason for flagging: Inappropriate Content\nThis action is irreversible.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                serviceAvis.delete(avis);
                showAlert("Success", "Flagged review deleted successfully");
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