package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Avis;
import tn.esprit.models.FormationScore;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceAvis implements IService<Avis>{
    private Connection cnx;
    private ServiceFormationScore serviceFormationScore;
    public ServiceAvis() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceFormationScore = new ServiceFormationScore();
    }
    @Override
    public void add(Avis avis) {

        String qry = "INSERT INTO avis (note, commentaire, date_creation, formation_id) VALUES (?, ?, ? ,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setFloat(1, avis.getNote());
            pstm.setString(2, avis.getCommentaire());
            pstm.setTimestamp(3, Timestamp.valueOf(avis.getDateCreation()));
            pstm.setInt(4,avis.getFormationId());
            //pstm.setInt(4, avis.getFormationId());
            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Avis added successfully!");
                updateFormationScore(avis.getFormationId());
            } else {
                System.out.println("Failed to add Avis.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding Avis: " + e.getMessage());
        }
    }

    @Override
    public List<Avis> getAll() {
        List<Avis> avisList = new ArrayList<>();
        String qry = "SELECT * FROM avis";
        try {
            if (cnx == null) {
                System.out.println("Database connection is null!");
                return avisList;
            }
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Avis avis = new Avis();
                avis.setId(rs.getInt("id"));
                avis.setNote(rs.getFloat("note"));
                avis.setCommentaire(rs.getString("commentaire"));
                avis.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                avis.setFormationId(rs.getInt("formation_id"));
                avisList.add(avis);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving Avis: " + e.getMessage());
            e.printStackTrace();
        }
        return avisList;
    }

    @Override
    public void update(Avis avis ) {
        String qry = "UPDATE avis SET note = ?, commentaire = ?, date_creation = ?, formation_id = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setFloat(1, avis.getNote());
            pstm.setString(2, avis.getCommentaire());
            pstm.setTimestamp(3, Timestamp.valueOf(avis.getDateCreation()));
            pstm.setInt(4, avis.getFormationId());
            pstm.setInt(5, avis.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Avis updated successfully for ID: " + avis.getId());
                updateFormationScore(avis.getFormationId());
            } else {
                System.out.println("Failed to update Avis. No record found with ID: " + avis.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error updating Avis: " + e.getMessage());
        }

    }

    @Override
    public void delete(Avis avis) {
        int formationId = avis.getFormationId(); // Store formationId before deletion
        String qry = "DELETE FROM avis WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, avis.getId());
            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Avis deleted successfully!");
                updateFormationScore(formationId);
            } else {
                System.out.println("No Avis found with ID: " + avis.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error deleting Avis: " + e.getMessage());
        }
    }

    //helper function for the calcul of the formation score

    private void updateFormationScore(int formationId) {
        // Calculate the average score and count for the formation
        ScoreResult result = calculateAverageScore(formationId);
        double averageScore = result.getAverageScore();
        int nombreAvis = result.getCount();

        // Check if a FormationScore already exists for this formation
        FormationScore formationScore = serviceFormationScore.getByFormationId(formationId);

        if (nombreAvis == 0) {
            // No Avis records exist for this formation, delete the FormationScore if it exists
            if (formationScore != null) {
                String qry = "DELETE FROM formation_score WHERE formation_id = ?";
                try {
                    PreparedStatement pstm = cnx.prepareStatement(qry);
                    pstm.setInt(1, formationId);
                    pstm.executeUpdate();
                    System.out.println("FormationScore deleted for formation ID: " + formationId);
                } catch (SQLException e) {
                    System.out.println("Error deleting FormationScore: " + e.getMessage());
                }
            }
        } else {
            // Update or create the FormationScore
            if (formationScore == null) {
                // Create a new FormationScore
                String qry = "INSERT INTO formation_score (formation_id, note_moyenne, nombre_avis) VALUES (?, ?, ?)";
                try {
                    PreparedStatement pstm = cnx.prepareStatement(qry);
                    pstm.setInt(1, formationId);
                    pstm.setDouble(2, averageScore);
                    pstm.setInt(3, nombreAvis);
                    pstm.executeUpdate();
                    System.out.println("FormationScore created for formation ID: " + formationId + " with " + nombreAvis + " reviews");
                } catch (SQLException e) {
                    System.out.println("Error creating FormationScore: " + e.getMessage());
                }
            } else {
                // Update the existing FormationScore
                String qry = "UPDATE formation_score SET note_moyenne = ?, nombre_avis = ? WHERE formation_id = ?";
                try {
                    PreparedStatement pstm = cnx.prepareStatement(qry);
                    pstm.setDouble(1, averageScore);
                    pstm.setInt(2, nombreAvis);
                    pstm.setInt(3, formationId);
                    pstm.executeUpdate();
                    System.out.println("FormationScore updated for formation ID: " + formationId + " with " + nombreAvis + " reviews");
                } catch (SQLException e) {
                    System.out.println("Error updating FormationScore: " + e.getMessage());
                }
            }
        }
    }
    private ScoreResult calculateAverageScore(int formationId) {
        List<Avis> avisList = getAll();
        double totalScore = 0.0;
        int count = 0;

        for (Avis avis : avisList) {
            if (avis.getFormationId() == formationId) {
                totalScore += avis.getNote();
                count++;
            }
        }

        double averageScore = count > 0 ? totalScore / count : 0.0;
        return new ScoreResult(averageScore, count);
    }
}
