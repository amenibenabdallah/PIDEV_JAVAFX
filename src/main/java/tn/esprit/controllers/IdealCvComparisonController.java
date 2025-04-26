package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.models.Evaluation;
import tn.esprit.models.FormationA;
import tn.esprit.services.EvaluationService;
import tn.esprit.utils.MyDataBase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdealCvComparisonController {

    @FXML
    private Button backButton;

    @FXML
    private Label idealEducationLabel;

    @FXML
    private Label idealExperienceLabel;

    @FXML
    private Label idealSkillsLabel;

    @FXML
    private Label idealCertificationsLabel;

    @FXML
    private Pane spiderChartPane;

    @FXML
    private VBox legendBox;

    private FormationA formation;
    private Evaluation idealCv;
    private final ObservableList<Evaluation> instructorEvaluations = FXCollections.observableArrayList();
    private AdminTemplateController templateController;

    // Colors for the radar chart
    private final Color IDEAL_COLOR = Color.RED;
    private final Color[] INSTRUCTOR_COLORS = {Color.BLUE, Color.GREEN, Color.PURPLE, Color.ORANGE};

    // Setter for templateController
    public void setTemplateController(AdminTemplateController templateController) {
        this.templateController = templateController;
    }

    public void setFormation(FormationA formation) {
        this.formation = formation;
        loadData();
    }

    private void loadData() {
        EvaluationService service = new EvaluationService();
        idealCv = service.createIdealCvProfile(formation);
        instructorEvaluations.clear();

        if (formation.getInstructors().isEmpty()) {
            System.out.println("No instructors assigned to this formation.");
            return;
        }

        Connection conn = MyDataBase.getInstance().getCnx();
        String query = "SELECT * FROM evaluation WHERE instructor_id = ?";

        for (var instructor : formation.getInstructors()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, instructor.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setInstructorId(rs.getInt("instructor_id"));
                    evaluation.setScore(rs.getDouble("score"));
                    evaluation.setNiveau(rs.getString("niveau"));
                    evaluation.setStatus(rs.getInt("status"));
                    evaluation.setEducation(rs.getString("education"));
                    evaluation.setYearsOfExperience(rs.getInt("years_of_experience"));
                    evaluation.setSkills(rs.getString("skills"));
                    evaluation.setCertifications(rs.getString("certifications"));
                    evaluation.setEducationWeight(rs.getDouble("education_weight"));
                    evaluation.setExperienceWeight(rs.getDouble("experience_weight"));
                    evaluation.setSkillsWeight(rs.getDouble("skills_weight"));
                    evaluation.setCertificationsWeight(rs.getDouble("certifications_weight"));
                    evaluation.setDateCreation(rs.getDate("date_creation").toLocalDate());
                    evaluation.setInstructeur(instructor);
                    instructorEvaluations.add(evaluation);
                } else {
                    Evaluation evaluation = service.evaluateInstructeur(instructor);
                    if (evaluation != null) {
                        instructorEvaluations.add(evaluation);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Update UI labels with ideal CV data
        idealEducationLabel.setText(idealCv.getEducation());
        idealExperienceLabel.setText(idealCv.getYearsOfExperience() + " years");
        idealSkillsLabel.setText(idealCv.getSkills().replace(",", ", "));
        idealCertificationsLabel.setText(idealCv.getCertifications().replace(",", ", "));

        updateRadarChart();
    }

    private void updateRadarChart() {
        try {
            // Clear the pane
            spiderChartPane.getChildren().clear();

            // Log the data being used for the chart
            System.out.println("Ideal CV Data: Education=" + idealCv.getEducationWeight() +
                    ", Experience=" + idealCv.getExperienceWeight() +
                    ", Skills=" + idealCv.getSkillsWeight() +
                    ", Certifications=" + idealCv.getCertificationsWeight());
            for (Evaluation eval : instructorEvaluations) {
                System.out.println("Instructor " + eval.getInstructeur().getNom() + " " + eval.getInstructeur().getPrenom() +
                        ": Education=" + eval.getEducationWeight() +
                        ", Experience=" + eval.getExperienceWeight() +
                        ", Skills=" + eval.getSkillsWeight() +
                        ", Certifications=" + eval.getCertificationsWeight());
            }

            // Create a Canvas for the radar chart
            Canvas canvas = new Canvas(spiderChartPane.getPrefWidth(), spiderChartPane.getPrefHeight());
            canvas.setVisible(true); // Ensure the canvas is visible
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Chart parameters
            double centerX = spiderChartPane.getPrefWidth() / 2;
            double centerY = spiderChartPane.getPrefHeight() / 2;
            double radius = Math.min(spiderChartPane.getPrefWidth(), spiderChartPane.getPrefHeight()) / 2 - 40; // Adjusted for labels
            int numAxes = 4; // Education, Experience, Skills, Certifications
            double maxValue = 1.0; // Weights are between 0 and 1

            // Draw background grid (concentric polygons)
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(1);
            for (int level = 1; level <= 5; level++) {
                double levelRadius = (level / 5.0) * radius;
                double[] xPoints = new double[numAxes];
                double[] yPoints = new double[numAxes];
                for (int i = 0; i < numAxes; i++) {
                    double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                    xPoints[i] = centerX + levelRadius * Math.cos(angle);
                    yPoints[i] = centerY - levelRadius * Math.sin(angle);
                }
                gc.strokePolygon(xPoints, yPoints, numAxes);
            }

            // Draw axes
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            for (int i = 0; i < numAxes; i++) {
                double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                double x = centerX + radius * Math.cos(angle);
                double y = centerY - radius * Math.sin(angle);
                gc.strokeLine(centerX, centerY, x, y);

                // Label the axes
                String label = switch (i) {
                    case 0 -> "Education";
                    case 1 -> "Experience";
                    case 2 -> "Skills";
                    case 3 -> "Certifications";
                    default -> "";
                };
                double labelX = centerX + (radius + 30) * Math.cos(angle);
                double labelY = centerY - (radius + 30) * Math.sin(angle);
                gc.fillText(label, labelX - 20, labelY + 5);
            }

            // Draw ideal CV
            if (idealCv != null) {
                gc.setFill(IDEAL_COLOR.deriveColor(0, 1, 1, 0.2));
                gc.setStroke(IDEAL_COLOR);
                gc.setLineWidth(2);
                double[] idealPointsX = new double[numAxes];
                double[] idealPointsY = new double[numAxes];
                double[] idealValues = {
                        clamp(idealCv.getEducationWeight(), 0, maxValue),
                        clamp(idealCv.getExperienceWeight(), 0, maxValue),
                        clamp(idealCv.getSkillsWeight(), 0, maxValue),
                        clamp(idealCv.getCertificationsWeight(), 0, maxValue)
                };
                for (int i = 0; i < numAxes; i++) {
                    double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                    double value = idealValues[i] / maxValue;
                    double pointRadius = value * radius;
                    idealPointsX[i] = centerX + pointRadius * Math.cos(angle);
                    idealPointsY[i] = centerY - pointRadius * Math.sin(angle);
                }
                gc.fillPolygon(idealPointsX, idealPointsY, numAxes);
                gc.strokePolygon(idealPointsX, idealPointsY, numAxes);
            } else {
                System.out.println("Ideal CV is null, cannot draw its radar chart.");
            }

            // Draw instructors
            for (int idx = 0; idx < instructorEvaluations.size(); idx++) {
                Evaluation eval = instructorEvaluations.get(idx);
                if (eval == null) {
                    System.out.println("Instructor evaluation at index " + idx + " is null.");
                    continue;
                }
                Color color = INSTRUCTOR_COLORS[idx % INSTRUCTOR_COLORS.length];
                gc.setFill(color.deriveColor(0, 1, 1, 0.2));
                gc.setStroke(color);
                double[] pointsX = new double[numAxes];
                double[] pointsY = new double[numAxes];
                double[] values = {
                        clamp(eval.getEducationWeight(), 0, maxValue),
                        clamp(eval.getExperienceWeight(), 0, maxValue),
                        clamp(eval.getSkillsWeight(), 0, maxValue),
                        clamp(eval.getCertificationsWeight(), 0, maxValue)
                };
                for (int i = 0; i < numAxes; i++) {
                    double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                    double value = values[i] / maxValue;
                    double pointRadius = value * radius;
                    pointsX[i] = centerX + pointRadius * Math.cos(angle);
                    pointsY[i] = centerY - pointRadius * Math.sin(angle);
                }
                gc.fillPolygon(pointsX, pointsY, numAxes);
                gc.strokePolygon(pointsX, pointsY, numAxes);
            }

            // Add the canvas to the pane
            spiderChartPane.getChildren().add(canvas);
            spiderChartPane.setVisible(true); // Ensure the pane is visible

            // Update the legend
            legendBox.getChildren().clear();
            if (idealCv != null) {
                Label idealLegend = new Label("Ideal CV");
                idealLegend.setStyle("-fx-text-fill: #FF0000; -fx-font-size: 14;");
                legendBox.getChildren().add(idealLegend);
            }

            for (int idx = 0; idx < instructorEvaluations.size(); idx++) {
                Evaluation eval = instructorEvaluations.get(idx);
                if (eval != null && eval.getInstructeur() != null) {
                    Label instructorLegend = new Label(eval.getInstructeur().getNom() + " " + eval.getInstructeur().getPrenom());
                    instructorLegend.setStyle("-fx-text-fill: " + toHexColor(INSTRUCTOR_COLORS[idx % INSTRUCTOR_COLORS.length]) + "; -fx-font-size: 14;");
                    legendBox.getChildren().add(instructorLegend);
                }
            }
        } catch (Exception e) {
            System.out.println("Error rendering radar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility method to clamp values
    private double clamp(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0;
        }
        return Math.max(min, Math.min(max, value));
    }

    // Utility method to convert Color to hex
    private String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormationView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}