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
            // Step 2: Retrieve the existing instructor (ID 8)
            int instructorId = 1; // Ahlem Ben Abdallah
            String selectInstructorSql = "SELECT * FROM instructeurs WHERE id = ?";
            instructeurs instructor = null;

            try (PreparedStatement stmt = conn.prepareStatement(selectInstructorSql)) {
                stmt.setInt(1, instructorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Construct the instructor object with minimal fields
                        // Fields like roles, password, dateNaissance, etc., are not in the table, so set to null
                        instructor = new instructeurs(
                                rs.getString("email_instructeur"), // email
                                null,                             // roles (not in table)
                                null,                             // password (not in table)
                                rs.getString("nom_instructeur"),  // nom
                                rs.getString("prenom_instructeur"), // prenom
                                null,                             // dateNaissance (not in table)
                                null,                             // resetToken (not in table)
                                null,                             // userType (not in table)
                                null,                             // image (not in table)
                                rs.getString("cv")                // cv
                        );
                        instructor.setId(rs.getInt("id"));
                        System.out.println("Found instructor: " + instructor.getNom() + " " + instructor.getPrenom());
                    } else {
                        System.out.println("Instructor with ID " + instructorId + " not found.");
                        return;
                    }
                }
            }

            // Step 3: Evaluate the instructor using EvaluationService
            EvaluationService service = new EvaluationService();
            Evaluation evaluation = service.evaluateInstructeur(instructor);
            if (evaluation == null) {
                System.out.println("Evaluation failed. Check CV path or API key.");
                return;
            }

            // Step 4: Print the evaluation result
            System.out.println("Evaluation completed:");
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