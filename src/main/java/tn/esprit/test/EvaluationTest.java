package tn.esprit.test;

import tn.esprit.models.instructeurs;
import tn.esprit.models.Evaluation;
import tn.esprit.services.EvaluationService;
import tn.esprit.utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EvaluationTest {
    public static void main(String[] args) {
        // Step 1: Get database connection
        Connection conn = MyDataBase.getInstance().getCnx();
        if (conn == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        try {
            // Step 2: Retrieve all instructors from the database
            String selectAllInstructorsSql = "SELECT * FROM instructeurs";
            List<instructeurs> instructors = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(selectAllInstructorsSql);
                 ResultSet rs = stmt.executeQuery()) {
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
                    System.out.println("Found instructor: " + instructor.getNom() + " " + instructor.getPrenom() + " (ID: " + instructor.getId() + ")");
                }
            }



            // Step 3: Evaluate only instructors without existing evaluations
            EvaluationService service = new EvaluationService();
            for (instructeurs instructor : instructors) {
                System.out.println("\nChecking instructor: " + instructor.getNom() + " " + instructor.getPrenom() + " (ID: " + instructor.getId() + ")");

                // Check if the instructor already has an evaluation
                String checkEvaluationSql = "SELECT COUNT(*) FROM evaluation WHERE instructor_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkEvaluationSql)) {
                    checkStmt.setInt(1, instructor.getId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        rs.next();
                        int evaluationCount = rs.getInt(1);
                        if (evaluationCount > 0) {
                            System.out.println("Instructor already has an evaluation. Skipping evaluation.");
                            continue;
                        }
                    }
                }

                // Evaluate the instructor
                System.out.println("No existing evaluation found. Creating new evaluation...");
                Evaluation evaluation = service.evaluateInstructeur(instructor);
                if (evaluation == null) {
                    System.out.println("Evaluation failed for instructor ID " + instructor.getId() + ". Check CV path or API key.");
                    continue;
                }

                // Step 4: Print the evaluation result
                System.out.println("Evaluation completed (Score from Flask API):");
                System.out.println("Score: " + evaluation.getScore());
                System.out.println("Level: " + evaluation.getNiveau());
                System.out.println("Status: " + evaluation.getStatus() + " (" + (evaluation.getStatus() == 1 ? "ACCEPTED" : "NOT ACCEPTED") + ")");
                System.out.println("Education: " + evaluation.getEducation());
                System.out.println("Years of Experience: " + evaluation.getYearsOfExperience());
                System.out.println("Skills: " + evaluation.getSkills());
                System.out.println("Certifications: " + evaluation.getCertifications());
                System.out.println("Date Created: " + evaluation.getDateCreation());

                // Step 5: Verify the evaluation in the database
                String selectEvaluationSql = "SELECT * FROM evaluation WHERE instructor_id = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectEvaluationSql)) {
                    selectStmt.setInt(1, instructor.getId());
                    try (ResultSet evalRs = selectStmt.executeQuery()) {
                        if (evalRs.next()) {
                            System.out.println("Evaluation saved in database:");
                            System.out.println("Instructor ID: " + evalRs.getInt("instructor_id"));
                            System.out.println("Score: " + evalRs.getDouble("score"));
                            System.out.println("Level: " + evalRs.getString("niveau"));
                            System.out.println("Status: " + evalRs.getInt("status") + " (" + (evalRs.getInt("status") == 1 ? "ACCEPTED" : "NOT ACCEPTED") + ")");
                        } else {
                            System.out.println("Evaluation not found in database for instructor ID " + instructor.getId() + ".");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}