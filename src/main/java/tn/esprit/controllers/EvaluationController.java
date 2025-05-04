package tn.esprit.controllers;

import tn.esprit.models.Evaluation;
import tn.esprit.models.User;
import tn.esprit.services.EvaluationService;
import tn.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EvaluationController implements Initializable, Searchable {
    @FXML
    private VBox cardsContainer;

    private final List<Evaluation> evaluations = new ArrayList<>();
    private final List<User> instructors = new ArrayList<>();
    private AdminTemplateController templateController;

    public void setTemplateController(AdminTemplateController templateController) {
        this.templateController = templateController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadEvaluations();
        displayEvaluations();
    }

    private void loadEvaluations() {
        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            if (conn == null) {
                System.out.println("Failed to connect to the database.");
                return;
            }

            // Fetch instructors with role 'INSTRUCTEUR'
            String selectInstructorSql = "SELECT id, email, nom, prenom, cv FROM user WHERE role = 'INSTRUCTEUR'";
            try (PreparedStatement stmt = conn.prepareStatement(selectInstructorSql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        User instructor = new User(
                                rs.getString("email"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("cv")
                        );
                        instructor.setId(rs.getInt("id"));
                        instructors.add(instructor);
                    }
                }
            }

            // Fetch or evaluate each instructor's evaluation
            EvaluationService service = new EvaluationService();
            for (User instructor : instructors) {
                String selectEvaluationSql = "SELECT * FROM evaluation WHERE user_id = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectEvaluationSql)) {
                    selectStmt.setInt(1, instructor.getId());
                    try (ResultSet evalRs = selectStmt.executeQuery()) {
                        Evaluation evaluation = evalRs.next() ? mapResultSetToEvaluation(evalRs, instructor) : service.evaluateInstructeur(instructor);
                        if (evaluation != null) {
                            evaluations.add(evaluation);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during evaluation loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Evaluation mapResultSetToEvaluation(ResultSet rs, User instructor) throws Exception {
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
        evaluation.setDateCreation(rs.getDate("date_creation").toLocalDate());
        evaluation.setEducationWeight(rs.getDouble("education_weight"));
        evaluation.setExperienceWeight(rs.getDouble("experience_weight"));
        evaluation.setSkillsWeight(rs.getDouble("skills_weight"));
        evaluation.setCertificationsWeight(rs.getDouble("certifications_weight"));
        evaluation.setInstructeur(instructor);
        return evaluation;
    }

    private void displayEvaluations() {
        cardsContainer.getChildren().clear();
        evaluations.forEach(this::createEvaluationCard);
    }

    private HBox createEvaluationCard(Evaluation evaluation) {
        User instructor = evaluation.getInstructeur();
        HBox cardBox = new HBox(50);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPadding(new Insets(15));
        cardBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
        cardBox.setEffect(new DropShadow(10, 0, 3, Color.color(0, 0, 0, 0.1)));

        setupCardHoverEffect(cardBox);

        cardBox.getChildren().addAll(
                createLabel("👤 " + instructor.getNom() + " " + instructor.getPrenom(), 150),
                createLabel("📧 " + instructor.getEmail(), 200),
                createLabel("⭐ " + String.format("%.1f", evaluation.getScore()), 80),
                createLabel("🏆 " + evaluation.getNiveau(), 100, evaluation.getNiveau().equals("EXCELLENT") ? "#00CC00" : "#FF5555"),
                createStatusLabel(evaluation.getStatus(), 100),
                createLabel("📅 " + evaluation.getDateCreation().toString().replace("-", "/"), 100),
                createActionBox(evaluation)
        );

        return cardBox;
    }

    private Label createLabel(String text, double prefWidth) {
        return createLabel(text, prefWidth, "#333333");
    }

    private Label createLabel(String text, double prefWidth, String textFill) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: 500; -fx-text-fill: " + textFill + ";");
        label.setPrefWidth(prefWidth);
        return label;
    }

    private Label createStatusLabel(int status, double prefWidth) {
        Label label = new Label((status == 1 ? "✅ " : "❌ ") + (status == 1 ? "Accepté" : "Non Accepté"));
        label.setStyle("-fx-font-size: 14; -fx-font-weight: 500; -fx-text-fill: #333333;");
        label.setPrefWidth(prefWidth);
        label.setGraphic(createStatusCircle(status));
        return label;
    }

    private HBox createActionBox(Evaluation evaluation) {
        Button detailsButton = new Button("🔍");
        detailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 8 15 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> showDetails(evaluation));

        HBox actionBox = new HBox(detailsButton);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPrefWidth(150);
        return actionBox;
    }

    private void setupCardHoverEffect(HBox cardBox) {
        DropShadow baseShadow = new DropShadow(10, 0, 3, Color.color(0, 0, 0, 0.1));
        cardBox.setOnMouseEntered(event -> {
            cardBox.setScaleX(1.02);
            cardBox.setScaleY(1.02);
            cardBox.setEffect(new DropShadow(15, 0, 5, Color.color(0, 0, 0, 0.2)));
        });
        cardBox.setOnMouseExited(event -> {
            cardBox.setScaleX(1.0);
            cardBox.setScaleY(1.0);
            cardBox.setEffect(baseShadow);
        });
    }

    private javafx.scene.shape.Circle createStatusCircle(int status) {
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(5);
        circle.setFill(status == 1 ? Color.GREEN : Color.RED);
        return circle;
    }

    private void showDetails(Evaluation evaluation) {
        if (templateController != null) {
            templateController.navigateToEvaluationDetails(evaluation);
        } else {
            System.out.println("TemplateController is not set.");
        }
    }

    @Override
    public void handleSearch(String searchText) {
        cardsContainer.getChildren().clear();
        evaluations.stream()
                .filter(evaluation -> {
                    User instructor = evaluation.getInstructeur();
                    return instructor != null && (
                            instructor.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                                    instructor.getPrenom().toLowerCase().contains(searchText.toLowerCase()) ||
                                    instructor.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                                    evaluation.getNiveau().toLowerCase().contains(searchText.toLowerCase())
                    );
                })
                .forEach(this::createEvaluationCard);
    }
}