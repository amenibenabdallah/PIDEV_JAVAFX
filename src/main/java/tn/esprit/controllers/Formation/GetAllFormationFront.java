package tn.esprit.controllers.Formation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import tn.esprit.services.FormationService;
import tn.esprit.models.Formation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GetAllFormationFront implements Initializable {

    @FXML
    private FlowPane cardsContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadFormationCards();
    }

    private void loadFormationCards() {
        FormationService fs = new FormationService();
        try {
            List<Formation> formations = fs.getAll();

            for (Formation formation : formations) {
                VBox card = createFormationCard(formation);
                cardsContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createFormationCard(Formation formation) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPrefHeight(330);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        // Image
        ImageView imageView = new ImageView();
        File imageFile = new File("images/formations/" + formation.getImageName());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Titre
        HBox titreBox = new HBox(5);
        Label titreText = new Label("Titre :");
        titreText.setStyle("-fx-font-weight: bold;");
        Label titreValue = new Label(formation.getTitre());
        titreBox.getChildren().addAll(titreText, titreValue);

        // Catégorie
        HBox categorieBox = new HBox(5);
        Label catText = new Label("Catégorie :");
        catText.setStyle("-fx-font-weight: bold;");
        Label catValue = new Label(formation.getCategorie().getNom());
        categorieBox.getChildren().addAll(catText, catValue);

        // Description
        HBox descBox = new HBox(5);
        Label descText = new Label("Description :");
        descText.setStyle("-fx-font-weight: bold;");
        Label descValue = new Label(formation.getDescription());
        descValue.setWrapText(true);
        descBox.getChildren().addAll(descText, descValue);

        // Prix
        HBox prixBox = new HBox(5);
        Label prixText = new Label("Prix :");
        prixText.setStyle("-fx-font-weight: bold;");
        Label prixValue = new Label(formation.getPrix() + " TND");
        prixBox.getChildren().addAll(prixText, prixValue);

        // Bouton
        Button viewDetailsButton = new Button("Voir Détails");
        viewDetailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        viewDetailsButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/FormationDetails.fxml"));
                Parent root = loader.load();

                FormationDetailsController controller = loader.getController();
                controller.setFormation(formation);

                Scene scene = viewDetailsButton.getScene();
                scene.setRoot(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Ajout au card
        card.getChildren().addAll(imageView, titreBox, categorieBox, descBox, prixBox, viewDetailsButton);

        return card;
    }






}
