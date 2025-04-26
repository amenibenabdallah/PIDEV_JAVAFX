package tn.esprit.controllers;

import tn.esprit.models.FormationA;
import tn.esprit.services.FormationServiceA;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FormationController implements Initializable {
    @FXML
    private VBox formationsContainer;

    private List<FormationA> formations = new ArrayList<>();
    private AdminTemplateController templateController;

    // Setter for templateController
    public void setTemplateController(AdminTemplateController templateController) {
        this.templateController = templateController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFormations();
        displayFormations();
    }

    private void loadFormations() {
        FormationServiceA service = new FormationServiceA();
        try {
            formations = service.getAllFormations();
        } catch (SQLException e) {
            System.out.println("Error loading formations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayFormations() {
        formationsContainer.getChildren().clear();

        for (FormationA formation : formations) {
            HBox cardBox = new HBox(20);
            cardBox.setAlignment(Pos.CENTER);
            cardBox.setPadding(new Insets(15));
            cardBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
            DropShadow shadow = new DropShadow(10, 0, 3, javafx.scene.paint.Color.color(0, 0, 0, 0.1));
            cardBox.setEffect(shadow);

            cardBox.setOnMouseEntered(event -> {
                cardBox.setScaleX(1.02);
                cardBox.setScaleY(1.02);
                cardBox.setEffect(new DropShadow(15, 0, 5, javafx.scene.paint.Color.color(0, 0, 0, 0.2)));
            });
            cardBox.setOnMouseExited(event -> {
                cardBox.setScaleX(1.0);
                cardBox.setScaleY(1.0);
                cardBox.setEffect(shadow);
            });

            String textStyle = "-fx-font-size: 14; -fx-font-weight: 500; -fx-text-fill: #333333;";
            Label nameLabel = new Label("📖 " + formation.getName());
            nameLabel.setStyle(textStyle);
            nameLabel.setPrefWidth(200);

            Label scoreLabel = new Label("⭐ " + String.format("%.1f", formation.getAverageScore()));
            scoreLabel.setStyle(textStyle);
            scoreLabel.setPrefWidth(100);

            Label avisCountLabel = new Label("💬 " + formation.getAvisCount());
            avisCountLabel.setStyle(textStyle);
            avisCountLabel.setPrefWidth(100);

            Button compareButton = new Button("Comparer CVs 🔍");
            compareButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 8 15 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
            compareButton.setOnAction(e -> navigateToComparison(formation));

            HBox actionBox = new HBox(compareButton);
            actionBox.setAlignment(Pos.CENTER);
            actionBox.setPrefWidth(150);

            cardBox.getChildren().addAll(nameLabel, scoreLabel, avisCountLabel, actionBox);
            formationsContainer.getChildren().add(cardBox);
        }
    }

    private void navigateToComparison(FormationA formation) {
        if (templateController != null) {
            templateController.navigateToIdealCvComparison(formation);
        } else {
            System.out.println("TemplateController is not set.");
        }
    }
}