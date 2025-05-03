package tn.esprit.controllers;

import tn.esprit.models.instructeurs;
import tn.esprit.models.Evaluation;
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

    private List<Evaluation> evaluations = new ArrayList<>();
    private List<instructeurs> instructors = new ArrayList<>();
    private AdminTemplateController templateController;

    // Setter for templateController
    public void setTemplateController(AdminTemplateController templateController) {
        this.templateController = templateController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadEvaluations();
        displayEvaluations();
    }

    private void loadEvaluations() {
        Connection conn = MyDataBase.getInstance().getCnx();
        if (conn == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        try {
            // Fetch all instructors
            String selectInstructorSql = "SELECT * FROM instructeurs";
            try (PreparedStatement stmt = conn.prepareStatement(selectInstructorSql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        instructeurs instructor = new instructeurs(
                                rs.getString("email_instructeur"),
                                null,
                                null,
                                rs.getString("nom_instructeur"),
                                rs.getString("prenom_instructeur"),
                                null,
                                null,
                                null,
                                null,
                                rs.getString("cv")
                        );
                        instructor.setId(rs.getInt("id"));
                        instructors.add(instructor);
                    }
                }
            }

            // Fetch or evaluate each instructor
            EvaluationService service = new EvaluationService();
            for (instructeurs instructor : instructors) {
                String selectEvaluationSql = "SELECT * FROM evaluation WHERE instructor_id = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectEvaluationSql)) {
                    selectStmt.setInt(1, instructor.getId());
                    try (ResultSet evalRs = selectStmt.executeQuery()) {
                        if (evalRs.next()) {
                            // Evaluation exists, use it
                            Evaluation evaluation = new Evaluation();
                            evaluation.setId(evalRs.getInt("id"));
                            evaluation.setInstructorId(evalRs.getInt("instructor_id"));
                            evaluation.setScore(evalRs.getDouble("score"));
                            evaluation.setNiveau(evalRs.getString("niveau"));
                            evaluation.setStatus(evalRs.getInt("status"));
                            evaluation.setEducation(evalRs.getString("education"));
                            evaluation.setYearsOfExperience(evalRs.getInt("years_of_experience"));
                            evaluation.setSkills(evalRs.getString("skills"));
                            evaluation.setCertifications(evalRs.getString("certifications"));
                            evaluation.setDateCreation(evalRs.getDate("date_creation").toLocalDate());
                            evaluation.setEducationWeight(evalRs.getDouble("education_weight"));
                            evaluation.setExperienceWeight(evalRs.getDouble("experience_weight"));
                            evaluation.setSkillsWeight(evalRs.getDouble("skills_weight"));
                            evaluation.setCertificationsWeight(evalRs.getDouble("certifications_weight"));
                            evaluation.setInstructeur(instructor);
                            evaluations.add(evaluation);
                        } else {
                            // No evaluation exists, perform evaluation
                            Evaluation evaluation = service.evaluateInstructeur(instructor);
                            if (evaluation != null) {
                                evaluations.add(evaluation);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during evaluation loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayEvaluations() {
        cardsContainer.getChildren().clear();
        for (Evaluation evaluation : evaluations) {
            instructeurs instructor = evaluation.getInstructeur();

            // Card container (tile)
            HBox cardBox = new HBox(50);
            cardBox.setAlignment(Pos.CENTER);
            cardBox.setPadding(new Insets(15));
            cardBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
            DropShadow shadow = new DropShadow(10, 0, 3, javafx.scene.paint.Color.color(0, 0, 0, 0.1));
            cardBox.setEffect(shadow);

            // Hover effect
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
            Label nomLabel = new Label("👤 " + instructor.getNom() + " " + instructor.getPrenom());
            nomLabel.setStyle(textStyle);
            nomLabel.setPrefWidth(150);

            Label emailLabel = new Label("📧 " + instructor.getEmail());
            emailLabel.setStyle(textStyle);
            emailLabel.setPrefWidth(200);

            Label scoreLabel = new Label("⭐ " + String.format("%.1f", evaluation.getScore()));
            scoreLabel.setStyle(textStyle);
            scoreLabel.setPrefWidth(80);

            Label niveauLabel = new Label("🏆 " + evaluation.getNiveau());
            niveauLabel.setStyle(textStyle + (evaluation.getNiveau().equals("EXCELLENT") ? "-fx-text-fill: #00CC00;" : "-fx-text-fill: #FF5555;"));
            niveauLabel.setPrefWidth(100);

            // Status label with dynamic symbol based on status
            Label statutLabel = new Label((evaluation.getStatus() == 1 ? "✅ " : "❌ ") + (evaluation.getStatus() == 1 ? "Accepté" : "Non Accepté"));
            statutLabel.setStyle(textStyle);
            statutLabel.setPrefWidth(100);
            statutLabel.setGraphic(createStatusCircle(evaluation.getStatus()));

            Label dateLabel = new Label("📅 " + evaluation.getDateCreation().toString().replace("-", "/"));
            dateLabel.setStyle(textStyle);
            dateLabel.setPrefWidth(100);

            // Details button with magnifying glass symbol
            Button detailsButton = new Button("🔍");
            detailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 8 15 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
            detailsButton.setOnAction(e -> showDetails(evaluation));

            HBox actionBox = new HBox(detailsButton);
            actionBox.setAlignment(Pos.CENTER);
            actionBox.setPrefWidth(150);

            cardBox.getChildren().addAll(nomLabel, emailLabel, scoreLabel, niveauLabel, statutLabel, dateLabel, actionBox);
            cardsContainer.getChildren().add(cardBox);
        }
    }

    private javafx.scene.shape.Circle createStatusCircle(int status) {
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(5);
        circle.setFill(status == 1 ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
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
        for (Evaluation evaluation : evaluations) {
            instructeurs instructor = evaluation.getInstructeur();
            if (instructor.getNom().toLowerCase().contains(searchText) ||
                    instructor.getPrenom().toLowerCase().contains(searchText) ||
                    instructor.getEmail().toLowerCase().contains(searchText) ||
                    evaluation.getNiveau().toLowerCase().contains(searchText)) {
                HBox cardBox = new HBox(50);
                cardBox.setAlignment(Pos.CENTER);
                cardBox.setPadding(new Insets(15));
                cardBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
                DropShadow shadow = new DropShadow(10, 0, 3, javafx.scene.paint.Color.color(0, 0, 0, 0.1));
                cardBox.setEffect(shadow);

                // Hover effect
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
                Label nomLabel = new Label("👤 " + instructor.getNom() + " " + instructor.getPrenom());
                nomLabel.setStyle(textStyle);
                nomLabel.setPrefWidth(150);

                Label emailLabel = new Label("📧 " + instructor.getEmail());
                emailLabel.setStyle(textStyle);
                emailLabel.setPrefWidth(200);

                Label scoreLabel = new Label("⭐ " + String.format("%.1f", evaluation.getScore()));
                scoreLabel.setStyle(textStyle);
                scoreLabel.setPrefWidth(80);

                Label niveauLabel = new Label("🏆 " + evaluation.getNiveau());
                niveauLabel.setStyle(textStyle + (evaluation.getNiveau().equals("EXCELLENT") ? "-fx-text-fill: #00CC00;" : "-fx-text-fill: #FF5555;"));
                niveauLabel.setPrefWidth(100);

                // Status label with dynamic symbol based on status
                Label statutLabel = new Label((evaluation.getStatus() == 1 ? "✅ " : "❌ ") + (evaluation.getStatus() == 1 ? "Accepté" : "Non Accepté"));
                statutLabel.setStyle(textStyle);
                statutLabel.setPrefWidth(100);
                statutLabel.setGraphic(createStatusCircle(evaluation.getStatus()));

                Label dateLabel = new Label("📅 " + evaluation.getDateCreation().toString().replace("-", "/"));
                dateLabel.setStyle(textStyle);
                dateLabel.setPrefWidth(100);

                // Details button with magnifying glass symbol
                Button detailsButton = new Button("🔍");
                detailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 8 15 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
                detailsButton.setOnAction(e -> showDetails(evaluation));

                HBox actionBox = new HBox(detailsButton);
                actionBox.setAlignment(Pos.CENTER);
                actionBox.setPrefWidth(150);

                cardBox.getChildren().addAll(nomLabel, emailLabel, scoreLabel, niveauLabel, statutLabel, dateLabel, actionBox);
                cardsContainer.getChildren().add(cardBox);
            }
        }
    }
}