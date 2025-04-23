package tn.esprit.test;

import tn.esprit.models.instructeurs;
import tn.esprit.models.Evaluation;
import tn.esprit.services.EvaluationService;
import tn.esprit.utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EvaluationTest {
    public static void main(String[] args) {
        // Step 1: Get database connection
        Connection conn = MyDataBase.getInstance().getCnx();
        if (conn == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        try {
            // Step 2: Retrieve the existing instructor (ID 1)
            int instructorId = 1; // Ameni Ben Abdallah
            String selectInstructorSql = "SELECT * FROM instructeurs WHERE id = ?";
            instructeurs instructor = null;

            try (PreparedStatement stmt = conn.prepareStatement(selectInstructorSql)) {
                stmt.setInt(1, instructorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        instructor = new instructeurs(
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
                        System.out.println("Found instructor: " + instructor.getNom() + " " + instructor.getPrenom());
                    } else {
                        System.out.println("Instructor with ID " + instructorId + " not found.");
                        return;
                    }
                }
            }

            // Step 3: Evaluate the instructor using EvaluationService (calls Flask API for score)
            EvaluationService service = new EvaluationService();
            Evaluation evaluation = service.evaluateInstructeur(instructor);
            if (evaluation == null) {
                System.out.println("Evaluation failed. Check CV path or API key.");
                return;
            }

            // Step 4: Print the evaluation result (focus on the score from Flask API)
            System.out.println("Evaluation completed (Score from Flask API):");
            System.out.println("Score: " + evaluation.getScore());
            System.out.println("Level: " + evaluation.getNiveau());
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
                    } else {
                        System.out.println("Evaluation not found in database.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error during test: " + e.getMessage());
        }
    }
}