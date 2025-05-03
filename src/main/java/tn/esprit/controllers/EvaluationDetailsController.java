package tn.esprit.controllers;

import tn.esprit.models.Evaluation;
import tn.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EvaluationDetailsController {
    @FXML
    private Label nomLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label niveauLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label educationLabel;

    @FXML
    private Label experienceLabel;

    @FXML
    private Label skillsLabel;

    @FXML
    private Label certificationsLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Button toggleStatusButton;

    @FXML
    private Button backButton;

    private Evaluation evaluation;
    private AdminTemplateController templateController;

    // Setter for templateController
    public void setTemplateController(AdminTemplateController templateController) {
        this.templateController = templateController;
    }

    // Method to set the evaluation and populate the UI
    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        nomLabel.setText(evaluation.getInstructeur().getNom() + " " + evaluation.getInstructeur().getPrenom());
        emailLabel.setText(evaluation.getInstructeur().getEmail());
        scoreLabel.setText(String.format("%.1f", evaluation.getScore()));
        niveauLabel.setText(evaluation.getNiveau());
        statusLabel.setText(evaluation.getStatus() == 1 ? "Accepté" : "Non Accepté");
        educationLabel.setText(evaluation.getEducation());
        experienceLabel.setText(String.valueOf(evaluation.getYearsOfExperience()));
        skillsLabel.setText(evaluation.getSkills());
        certificationsLabel.setText(evaluation.getCertifications());
        dateLabel.setText(evaluation.getDateCreation().toString().replace("-", "/"));

        // Set text color for niveau
        if (evaluation.getNiveau().equals("EXCELLENT")) {
            niveauLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #00FF00;");
        } else {
            niveauLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #333333;");
        }

        // Add status circle
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(5);
        circle.setFill(evaluation.getStatus() == 1 ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        statusLabel.setGraphic(circle);
    }

    @FXML
    private void toggleStatus() {
        int newStatus = (evaluation.getStatus() == 1) ? 0 : 1;
        evaluation.setStatus(newStatus);
        statusLabel.setText(newStatus == 1 ? "Accepté" : "Non Accepté");

        // Update status circle
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(5);
        circle.setFill(newStatus == 1 ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
        statusLabel.setGraphic(circle);

        // Update status in the database
        Connection conn = MyDataBase.getInstance().getCnx();
        try {
            String updateSql = "UPDATE evaluation SET status = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setInt(1, newStatus);
            stmt.setInt(2, evaluation.getId());
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            System.out.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        if (templateController != null) {
            templateController.navigateToEvaluation();
        } else {
            System.out.println("TemplateController is not set.");
        }
    }
}