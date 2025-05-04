package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;
import tn.esprit.models.Evaluation;
import tn.esprit.models.FormationA;
import tn.esprit.models.User;
import tn.esprit.services.EvaluationService;
import tn.esprit.utils.MyDataBase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class IdealCvComparisonController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label instructorLabel;

    @FXML
    private Button backButton;

    @FXML
    private Pane spiderChartPane;

    private FormationA formation;
    private Evaluation idealCv;
    private final ObservableList<Evaluation> instructorEvaluations = FXCollections.observableArrayList();
    private AdminTemplateController templateController;

    // Color palette for the chart
    private final Color IDEAL_COLOR = Color.web("#F43F5E"); // Rose for Ideal CV
    private final Color[] INSTRUCTOR_COLORS = {
            Color.web("#3B82F6"), // Blue for first instructor
            Color.web("#22C55E"), // Green for second instructor
            Color.web("#A855F7"), // Purple for third instructor
            Color.web("#F59E0B")  // Amber for fourth instructor
    };

    // Map to store polygons for interactivity
    private final Map<String, double[][]> polygons = new HashMap<>();
    private final Map<String, Color> polygonColors = new HashMap<>();
    private final Map<String, Double> polygonOpacities = new HashMap<>();

    // Map to store percentage label positions and grid line levels for interactivity
    private final Map<Integer, Map<Integer, double[]>> percentageLabelPositions = new HashMap<>();
    private final Map<Integer, double[][]> gridLines = new HashMap<>();
    private int highlightedLevel = -1;
    private int highlightedAxis = -1;

    // Map to store legend positions for interactivity
    private final Map<String, double[]> legendPositions = new HashMap<>();

    public void setTemplateController(AdminTemplateController templateController) {
        this.templateController = templateController;
        if (templateController == null) {
            System.out.println("Warning: templateController is set to null.");
        } else {
            System.out.println("templateController set successfully.");
        }
    }

    public void setFormation(FormationA formation) {
        this.formation = formation;
        loadData();
    }

    private void loadData() {
        EvaluationService service = new EvaluationService();
        idealCv = service.createIdealCvProfile(formation);
        // Ensure Ideal CV is 100% for all categories
        if (idealCv != null) {
            idealCv.setEducationWeight(1.0);    // 100%
            idealCv.setExperienceWeight(1.0);   // 100%
            idealCv.setSkillsWeight(1.0);       // 100%
            idealCv.setCertificationsWeight(1.0); // 100%
            System.out.println("Ideal CV weights - Education: " + idealCv.getEducationWeight() +
                    ", Experience: " + idealCv.getExperienceWeight() +
                    ", Skills: " + idealCv.getSkillsWeight() +
                    ", Certifications: " + idealCv.getCertificationsWeight());
        }
        instructorEvaluations.clear();

        if (formation.getInstructors().isEmpty()) {
            System.out.println("No instructors assigned to this formation.");
            return;
        }

        Connection conn = MyDataBase.getInstance().getCnx();
        String query = "SELECT * FROM evaluation WHERE user_id = ?";

        for (User instructor : formation.getInstructors()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, instructor.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Evaluation evaluation = new Evaluation();
                        evaluation.setId(rs.getInt("id"));
                        evaluation.setUserId(rs.getInt("user_id"));
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
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        updateRadarChart();
    }

    private void updateRadarChart() {
        try {
            spiderChartPane.getChildren().clear();
            polygons.clear();
            polygonColors.clear();
            polygonOpacities.clear();
            percentageLabelPositions.clear();
            gridLines.clear();
            legendPositions.clear();
            highlightedLevel = -1;
            highlightedAxis = -1;

            Canvas canvas = new Canvas(spiderChartPane.getPrefWidth(), spiderChartPane.getPrefHeight());
            canvas.setVisible(true);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            double centerX = spiderChartPane.getPrefWidth() / 2;
            double centerY = spiderChartPane.getPrefHeight() / 2 - 50; // Shift chart up to make space for legend
            double radius = Math.min(spiderChartPane.getPrefWidth(), spiderChartPane.getPrefHeight() - 150) / 2 - 50; // Adjust radius to fit labels and legend
            int numAxes = 4;
            double maxValue = 1.0;

            // Draw background grid with smooth gradient
            LinearGradient gridGradient = new LinearGradient(
                    centerX, centerY, centerX + radius, centerY, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#D1D5DB").deriveColor(0, 1, 1, 0.8)),
                    new Stop(1, Color.web("#E2E8F0").deriveColor(0, 1, 1, 0.5))
            );
            gc.setStroke(gridGradient);
            gc.setLineWidth(1.5);
            gc.setFill(Color.web("#475569"));
            gc.setFont(javafx.scene.text.Font.font("Inter", 14));
            for (int level = 1; level <= 5; level++) {
                double levelRadius = (level / 5.0) * radius;
                double[] xPoints = new double[numAxes];
                double[] yPoints = new double[numAxes];
                for (int i = 0; i < numAxes; i++) {
                    double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                    xPoints[i] = centerX + levelRadius * Math.cos(angle);
                    yPoints[i] = centerY - levelRadius * Math.sin(angle);
                }
                gridLines.put(level, new double[][]{xPoints, yPoints});
                gc.strokePolygon(xPoints, yPoints, numAxes);
            }

            // Draw percentage labels on all axes, scaled to 100%
            for (int level = 1; level <= 5; level++) {
                double levelRadius = (level / 5.0) * radius;
                Map<Integer, double[]> axisPositions = new HashMap<>();
                for (int i = 0; i < numAxes; i++) {
                    double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                    double labelRadius = levelRadius * 0.9; // Position labels inside the chart
                    double labelX = centerX + labelRadius * Math.cos(angle);
                    double labelY = centerY - labelRadius * Math.sin(angle);
                    double xOffset = 0;
                    double yOffset = 0;

                    switch (i) {
                        case 0: // Education (top)
                            xOffset = -18;
                            yOffset = -6;
                            break;
                        case 1: // Experience (left)
                            xOffset = -35;
                            yOffset = 6;
                            break;
                        case 2: // Skills (bottom)
                            xOffset = -18;
                            yOffset = 18;
                            break;
                        case 3: // Certificats (right)
                            xOffset = 6;
                            yOffset = 6;
                            break;
                    }
                    axisPositions.put(i, new double[]{labelX + xOffset, labelY + yOffset});
                    gc.fillText((level * 20) + "%", labelX + xOffset, labelY + yOffset); // Scale to 100%
                }
                percentageLabelPositions.put(level, axisPositions);
            }

            // Draw axes with gradient
            LinearGradient axisGradient = new LinearGradient(
                    centerX, centerY, centerX + radius, centerY, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#9CA3AF")),
                    new Stop(1, Color.web("#6B7280"))
            );
            gc.setStroke(axisGradient);
            gc.setLineWidth(1.5);
            gc.setFill(Color.web("#1E293B"));
            gc.setFont(javafx.scene.text.Font.font("Inter", 18)); // Slightly smaller font to fit within bounds
            for (int i = 0; i < numAxes; i++) {
                double angle = Math.toRadians(90 + i * 360.0 / numAxes);
                double x = centerX + radius * Math.cos(angle);
                double y = centerY - radius * Math.sin(angle);
                gc.strokeLine(centerX, centerY, x, y);

                String label = switch (i) {
                    case 0 -> "Éducation";
                    case 1 -> "Expérience";
                    case 2 -> "Skills";
                    case 3 -> "Certificats";
                    default -> "";
                };
                double labelX = centerX + (radius + 40) * Math.cos(angle); // Reduced distance to fit within bounds
                double labelY = centerY - (radius + 40) * Math.sin(angle);
                double xOffset = switch (i) {
                    case 0 -> -40;  // Education (top)
                    case 1 -> -60;  // Experience (left)
                    case 2 -> -30;  // Skills (bottom)
                    case 3 -> 10;   // Certificats (right)
                    default -> 0;
                };
                double yOffset = switch (i) {
                    case 0 -> -10;  // Education (top)
                    case 1 -> 5;    // Experience (left)
                    case 2 -> 25;   // Skills (bottom)
                    case 3 -> 5;    // Certificats (right)
                    default -> 0;
                };
                gc.fillText(label, labelX + xOffset, labelY + yOffset);
            }

            // Draw ideal CV with debug logging
            if (idealCv != null) {
                double[] idealValues = {1.0, 1.0, 1.0, 1.0}; // Forces full value for all axes
                System.out.println("Ideal CV values: " + java.util.Arrays.toString(idealValues)); // Optional: verify values
                drawPolygon(gc, "Perfect CV", idealValues, IDEAL_COLOR, centerX, centerY, radius, numAxes, 1.0);

                System.out.println("Ideal CV values before drawing: " + java.util.Arrays.toString(idealValues));
                drawPolygon(gc, "Perfect CV", idealValues, IDEAL_COLOR, centerX, centerY, radius, numAxes, maxValue);
            }

            // Draw instructors with their actual names
            for (int idx = 0; idx < instructorEvaluations.size(); idx++) {
                Evaluation eval = instructorEvaluations.get(idx);
                if (eval == null || eval.getInstructeur() == null) continue;
                Color color = INSTRUCTOR_COLORS[idx % INSTRUCTOR_COLORS.length];
                double[] values = {
                        clamp(eval.getEducationWeight(), 0, maxValue),
                        clamp(eval.getExperienceWeight(), 0, maxValue),
                        clamp(eval.getSkillsWeight(), 0, maxValue),
                        clamp(eval.getCertificationsWeight(), 0, maxValue)
                };
                String name = eval.getInstructeur().getNom() + " " + eval.getInstructeur().getPrenom();
                drawPolygon(gc, name, values, color, centerX, centerY, radius, numAxes, maxValue);
            }

            // Draw legend on the canvas
            drawLegend(gc, centerX, spiderChartPane.getPrefHeight() - 100); // Position legend at the bottom

            spiderChartPane.getChildren().add(canvas);

            canvas.setOnMouseMoved(event -> handleMouseMoved(event, centerX, centerY, radius, numAxes, maxValue));
            canvas.setOnMouseExited(event -> resetInteractivity(gc, centerX, centerY, radius, numAxes, maxValue));
        } catch (Exception e) {
            System.out.println("Error rendering radar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void drawPolygon(GraphicsContext gc, String name, double[] values, Color color, double centerX, double centerY, double radius, int numAxes, double maxValue) {
        double[] pointsX = new double[numAxes];
        double[] pointsY = new double[numAxes];
        for (int i = 0; i < numAxes; i++) {
            double angle = Math.toRadians(90 + i * 360.0 / numAxes);
            double value = values[i] / maxValue; // Normalize to 0-1
            double pointRadius = value * radius; // Scale to chart radius
            pointsX[i] = centerX + pointRadius * Math.cos(angle);
            pointsY[i] = centerY - pointRadius * Math.sin(angle);
        }

        polygons.put(name, new double[][]{pointsX, pointsY});
        polygonColors.put(name, color);
        polygonOpacities.put(name, 0.3);

        LinearGradient gradient = new LinearGradient(
                centerX, centerY, centerX + radius, centerY, false, CycleMethod.NO_CYCLE,
                new Stop(0, color.deriveColor(0, 1, 1, 0.5)),
                new Stop(1, color.deriveColor(0, 1, 1, 0.2))
        );
        gc.setFill(gradient);
        gc.setStroke(color.deriveColor(0, 1, 1, 0.8));
        gc.setLineWidth(2.5);
        gc.fillPolygon(pointsX, pointsY, numAxes);
        gc.strokePolygon(pointsX, pointsY, numAxes);
    }

    private void drawLegend(GraphicsContext gc, double centerX, double legendY) {
        double legendBoxWidth = 120;
        double legendBoxHeight = 26;
        double spacing = 10;
        double totalEntries = (idealCv != null ? 1 : 0) + instructorEvaluations.size();
        double totalWidth = totalEntries * (legendBoxWidth + spacing) - spacing;
        double startX = centerX - totalWidth / 2;

        int idx = 0;
        if (idealCv != null) {
            drawLegendEntry(gc, "Perfect CV", IDEAL_COLOR, startX + idx * (legendBoxWidth + spacing), legendY, legendBoxWidth, legendBoxHeight);
            idx++;
        }
        for (int i = 0; i < instructorEvaluations.size(); i++) {
            Evaluation eval = instructorEvaluations.get(i);
            if (eval != null && eval.getInstructeur() != null) {
                String name = eval.getInstructeur().getNom() + " " + eval.getInstructeur().getPrenom();
                Color color = INSTRUCTOR_COLORS[i % INSTRUCTOR_COLORS.length];
                drawLegendEntry(gc, name, color, startX + idx * (legendBoxWidth + spacing), legendY, legendBoxWidth, legendBoxHeight);
                idx++;
            }
        }
    }

    private void drawLegendEntry(GraphicsContext gc, String name, Color color, double x, double y, double width, double height) {
        // Draw background rectangle
        gc.setFill(color);
        gc.fillRoundRect(x, y, width, height, 15, 15);

        // Draw text
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 14));
        double textX = x + (width - gc.getFont().getSize() * name.length() / 2) / 2;
        double textY = y + height / 2 + 5;
        gc.fillText(name, textX, textY);

        // Store position for interactivity
        legendPositions.put(name, new double[]{x, y, x + width, y + height});
    }

    private void handleMouseMoved(MouseEvent event, double centerX, double centerY, double radius, int numAxes, double maxValue) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        GraphicsContext gc = ((Canvas) event.getSource()).getGraphicsContext2D();

        // Check for polygon hover
        String hoveredPolygon = null;
        for (Map.Entry<String, double[][]> entry : polygons.entrySet()) {
            String name = entry.getKey();
            double[][] points = entry.getValue();
            double[] pointsX = points[0];
            double[] pointsY = points[1];
            if (isPointInPolygon(mouseX, mouseY, pointsX, pointsY)) {
                hoveredPolygon = name;
                break;
            }
        }

        // Check for legend hover
        String hoveredLegend = null;
        for (Map.Entry<String, double[]> entry : legendPositions.entrySet()) {
            String name = entry.getKey();
            double[] pos = entry.getValue();
            if (mouseX >= pos[0] && mouseX <= pos[2] && mouseY >= pos[1] && mouseY <= pos[3]) {
                hoveredLegend = name;
                break;
            }
        }

        for (Map.Entry<String, double[][]> entry : polygons.entrySet()) {
            String name = entry.getKey();
            polygonOpacities.put(name, (name.equals(hoveredPolygon) || name.equals(hoveredLegend)) ? 0.7 : 0.3);
        }

        // Check for percentage label hover
        highlightedLevel = -1;
        highlightedAxis = -1;
        outerLoop:
        for (Map.Entry<Integer, Map<Integer, double[]>> levelEntry : percentageLabelPositions.entrySet()) {
            int level = levelEntry.getKey();
            Map<Integer, double[]> axisPositions = levelEntry.getValue();
            for (Map.Entry<Integer, double[]> axisEntry : axisPositions.entrySet()) {
                int axis = axisEntry.getKey();
                double[] pos = axisEntry.getValue();
                double labelX = pos[0];
                double labelY = pos[1];
                double labelWidth = 35;
                double labelHeight = 18;
                if (mouseX >= labelX && mouseX <= labelX + labelWidth && mouseY >= labelY - labelHeight && mouseY <= labelY) {
                    highlightedLevel = level;
                    highlightedAxis = axis;
                    break outerLoop;
                }
            }
        }

        redrawChart(gc, centerX, centerY, radius, numAxes, maxValue);

        // Show tooltip for polygon or legend hover
        if (hoveredPolygon != null || hoveredLegend != null) {
            String tooltipName = hoveredPolygon != null ? hoveredPolygon : hoveredLegend;
            String tooltipText = buildTooltipText(tooltipName);
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle("-fx-background-color: #1E293B; -fx-text-fill: white; -fx-font-size: 13; -fx-font-family: 'Inter'; -fx-background-radius: 8; -fx-opacity: 0.95; -fx-padding: 10;");
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), tooltip.getGraphic());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            Tooltip.install(spiderChartPane, tooltip);
        } else {
            Tooltip.uninstall(spiderChartPane, null);
        }

        // Show tooltip for percentage label hover
        if (highlightedLevel != -1 && highlightedAxis != -1) {
            String axisName = switch (highlightedAxis) {
                case 0 -> "Éducation";
                case 1 -> "Expérience";
                case 2 -> "Skills";
                case 3 -> "Certificats";
                default -> "";
            };
            String tooltipText = axisName + ": " + (highlightedLevel * 20) + "%";
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle("-fx-background-color: #1E293B; -fx-text-fill: white; -fx-font-size: 13; -fx-font-family: 'Inter'; -fx-background-radius: 8; -fx-opacity: 0.95; -fx-padding: 10;");
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), tooltip.getGraphic());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            Tooltip.install(spiderChartPane, tooltip);
        }
    }

    private void resetInteractivity(GraphicsContext gc, double centerX, double centerY, double radius, int numAxes, double maxValue) {
        for (String name : polygons.keySet()) {
            polygonOpacities.put(name, 0.3);
        }
        highlightedLevel = -1;
        highlightedAxis = -1;
        redrawChart(gc, centerX, centerY, radius, numAxes, maxValue);
        Tooltip.uninstall(spiderChartPane, null);
    }

    private void redrawChart(GraphicsContext gc, double centerX, double centerY, double radius, int numAxes, double maxValue) {
        gc.clearRect(0, 0, spiderChartPane.getPrefWidth(), spiderChartPane.getPrefHeight());

        // Redraw grid lines
        for (int level = 1; level <= 5; level++) {
            double[] xPoints = gridLines.get(level)[0];
            double[] yPoints = gridLines.get(level)[1];
            if (level == highlightedLevel) {
                gc.setStroke(Color.web("#3B82F6"));
                gc.setLineWidth(2.5);
            } else {
                LinearGradient gridGradient = new LinearGradient(
                        centerX, centerY, centerX + radius, centerY, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#D1D5DB").deriveColor(0, 1, 1, 0.8)),
                        new Stop(1, Color.web("#E2E8F0").deriveColor(0, 1, 1, 0.5))
                );
                gc.setStroke(gridGradient);
                gc.setLineWidth(1.5);
            }
            gc.strokePolygon(xPoints, yPoints, numAxes);
        }

        // Redraw percentage labels with highlight
        gc.setFill(Color.web("#475569"));
        gc.setFont(javafx.scene.text.Font.font("Inter", 14));
        for (int level = 1; level <= 5; level++) {
            Map<Integer, double[]> axisPositions = percentageLabelPositions.get(level);
            for (int i = 0; i < numAxes; i++) {
                double labelX = axisPositions.get(i)[0];
                double labelY = axisPositions.get(i)[1];
                if (level == highlightedLevel && i == highlightedAxis) {
                    gc.setFill(Color.web("#3B82F6"));
                    gc.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 14));
                } else {
                    gc.setFill(Color.web("#475569"));
                    gc.setFont(javafx.scene.text.Font.font("Inter", 14));
                }
                double xOffset = 0;
                double yOffset = 0;
                switch (i) {
                    case 0: // Education (top)
                        xOffset = -18;
                        yOffset = -6;
                        break;
                    case 1: // Experience (left)
                        xOffset = -35;
                        yOffset = 6;
                        break;
                    case 2: // Skills (bottom)
                        xOffset = -18;
                        yOffset = 18;
                        break;
                    case 3: // Certificats (right)
                        xOffset = 6;
                        yOffset = 6;
                        break;
                }
                gc.fillText((level * 20) + "%", labelX, labelY);
            }
        }

        // Redraw axes
        LinearGradient axisGradient = new LinearGradient(
                centerX, centerY, centerX + radius, centerY, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#9CA3AF")),
                new Stop(1, Color.web("#6B7280"))
        );
        gc.setStroke(axisGradient);
        gc.setLineWidth(1.5);
        gc.setFill(Color.web("#1E293B"));
        gc.setFont(javafx.scene.text.Font.font("Inter", 18));
        for (int i = 0; i < numAxes; i++) {
            double angle = Math.toRadians(90 + i * 360.0 / numAxes);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY - radius * Math.sin(angle);
            gc.strokeLine(centerX, centerY, x, y);

            String label = switch (i) {
                case 0 -> "Éducation";
                case 1 -> "Expérience";
                case 2 -> "Skills";
                case 3 -> "Certificats";
                default -> "";
            };
            double labelX = centerX + (radius + 40) * Math.cos(angle);
            double labelY = centerY - (radius + 40) * Math.sin(angle);
            double xOffset = switch (i) {
                case 0 -> -40;  // Education (top)
                case 1 -> -60;  // Experience (left)
                case 2 -> -30;  // Skills (bottom)
                case 3 -> 10;   // Certificats (right)
                default -> 0;
            };
            double yOffset = switch (i) {
                case 0 -> -10;  // Education (top)
                case 1 -> 5;    // Experience (left)
                case 2 -> 25;   // Skills (bottom)
                case 3 -> 5;    // Certificats (right)
                default -> 0;
            };
            gc.fillText(label, labelX + xOffset, labelY + yOffset);
        }

        // Redraw polygons with smooth opacity transition
        for (Map.Entry<String, double[][]> entry : polygons.entrySet()) {
            String name = entry.getKey();
            double[][] points = entry.getValue();
            double[] pointsX = points[0];
            double[] pointsY = points[1];
            Color color = polygonColors.get(name);
            double opacity = polygonOpacities.get(name);

            LinearGradient gradient = new LinearGradient(
                    centerX, centerY, centerX + radius, centerY, false, CycleMethod.NO_CYCLE,
                    new Stop(0, color.deriveColor(0, 1, 1, opacity)),
                    new Stop(1, color.deriveColor(0, 1, 1, opacity / 2))
            );
            gc.setFill(gradient);
            gc.setStroke(color.deriveColor(0, 1, 1, opacity + 0.2));
            gc.setLineWidth(2.5);
            gc.fillPolygon(pointsX, pointsY, numAxes);
            gc.strokePolygon(pointsX, pointsY, numAxes);
        }

        // Redraw legend
        drawLegend(gc, centerX, spiderChartPane.getPrefHeight() - 100);
    }

    private String buildTooltipText(String name) {
        StringBuilder tooltipText = new StringBuilder(name + "\n");
        Evaluation eval = name.equals("Perfect CV") ? idealCv : instructorEvaluations.stream()
                .filter(e -> {
                    if (e.getInstructeur() == null) return false;
                    String fullName = e.getInstructeur().getNom() + " " + e.getInstructeur().getPrenom();
                    return fullName.equalsIgnoreCase(name);
                })
                .findFirst().orElse(null);
        if (eval != null) {
            tooltipText.append("Éducation: ").append(String.format("%.1f", eval.getEducationWeight() * 100)).append("%\n");
            tooltipText.append("Expérience: ").append(String.format("%.1f", eval.getExperienceWeight() * 100)).append("%\n");
            tooltipText.append("Skills: ").append(String.format("%.1f", eval.getSkillsWeight() * 100)).append("%\n");
            tooltipText.append("Certificats: ").append(String.format("%.1f", eval.getCertificationsWeight() * 100)).append("%");
        }
        return tooltipText.toString();
    }

    private boolean isPointInPolygon(double x, double y, double[] pointsX, double[] pointsY) {
        int i, j;
        boolean inside = false;
        for (i = 0, j = pointsX.length - 1; i < pointsX.length; j = i++) {
            if (((pointsY[i] > y) != (pointsY[j] > y)) &&
                    (x < (pointsX[j] - pointsX[i]) * (y - pointsY[i]) / (pointsY[j] - pointsY[i]) + pointsX[i])) {
                inside = !inside;
            }
        }
        return inside;
    }

    private double clamp(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0;
        }
        return Math.max(min, Math.min(max, value));
    }
}