package tn.esprit.controllers.Formation;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.esprit.services.FormationService;
import tn.esprit.models.Formation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GetAllFormationBack implements Initializable {

    @FXML
    private GridPane cardsGrid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load data
        loadFormations();
    }

    private void loadFormations() {
        FormationService fs = new FormationService();
        try {
            List<Formation> formations = fs.getAll();
            displayFormationCards(formations);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des formations.");
        }
    }

    private void displayFormationCards(List<Formation> formations) {
        // Clear existing cards
        cardsGrid.getChildren().clear();

        // Calculate number of columns (3 columns for example)
        int columns = 3;
        int row = 0;
        int column = 0;

        for (Formation formation : formations) {
            // Create card for each formation
            VBox card = createFormationCard(formation);

            // Add card to grid
            cardsGrid.add(card, column, row);

            // Update column and row for next card
            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createFormationCard(Formation formation) {
        // Create card container
        VBox card = new VBox();
        card.getStyleClass().add("formation-card");
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setMaxWidth(220);

        // Add image
        ImageView imageView = new ImageView();
        if (formation.getImageName() != null && !formation.getImageName().isEmpty()) {
            File imageFile = new File("images/formations/" + formation.getImageName());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
                imageView.setFitWidth(200);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);

                // Clip image to rounded rectangle
                Rectangle clip = new Rectangle(200, 120);
                clip.setArcWidth(15);
                clip.setArcHeight(15);
                imageView.setClip(clip);
            }
        }

        // Add title
        Label titleLabel = new Label(formation.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt;");
        titleLabel.setWrapText(true);

        // Add description (shortened)
        Label descLabel = new Label(formation.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);
        descLabel.setMaxHeight(40);

        // Add duration and level
        HBox detailsRow1 = new HBox(10);
        detailsRow1.getChildren().addAll(
                new Label("Durée: " + formation.getDuree()),
                new Label("Niveau: " + formation.getNiveau())
        );

        // Add price
        Label priceLabel = new Label("Prix: " + formation.getPrix() + " DT");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");

        // Add action buttons
        HBox actionButtons = new HBox(10);
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

        // Set button actions
        editButton.setOnAction(event -> handleEditFormation(formation));
        deleteButton.setOnAction(event -> handleDeleteFormation(formation));

        actionButtons.getChildren().addAll(editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(imageView, titleLabel, descLabel, detailsRow1, priceLabel, actionButtons);

        return card;
    }

    @FXML
    private void handleAjouterFormation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/addFormation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter Formation");
            stage.setScene(new Scene(root));

            // Set up a listener to reload the formations when the AddFormation window is closed
            stage.setOnHidden(e -> loadFormations());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire d'ajout.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleEditFormation(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/EditFormation.fxml"));
            Parent root = loader.load();

            EditFormationController controller = loader.getController();
            controller.setFormation(formation);

            Stage stage = new Stage();
            stage.setTitle("Modifier Formation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadFormations(); // refresh after editing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/getAllFormations.fxml"));
            Parent root = loader.load();
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteFormation(Formation formation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer : " + formation.getTitre() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FormationService fs = new FormationService();
                    fs.delete(formation.getId());
                    loadFormations(); // refresh cards
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur lors de la suppression.");
                }
            }
        });
    }
}