package tn.esprit.services;

import tn.esprit.models.Avis;
import tn.esprit.models.FormationA;
import tn.esprit.models.FormationScore;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FormationServiceA {

    private final Connection cnx;

    public FormationServiceA() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public List<FormationA> getAllFormations() throws SQLException {
        List<FormationA> formations = new ArrayList<>();
        String queryFormations = "SELECT id, titre FROM formation"; // Adjust table name if different

        try (PreparedStatement stmt = cnx.prepareStatement(queryFormations);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("titre");

                // Fetch FormationScore for this formation
                FormationScore formationScore = getFormationScore(id);
                double averageScore = (formationScore != null) ? formationScore.getNoteMoyenne() : 0.0;
                int avisCount = (formationScore != null) ? formationScore.getNombreAvis() : 0;

                // Fetch avis for this formation
                List<Avis> avisList = getAvisForFormation(id);

                formations.add(new FormationA(id, name, averageScore, avisCount, avisList));
            }
        }
        return formations;
    }

    private FormationScore getFormationScore(int formationId) throws SQLException {
        String query = "SELECT note_moyenne, nombre_avis FROM formation_score WHERE formation_id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, formationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double noteMoyenne = rs.getDouble("note_moyenne");
                    int nombreAvis = rs.getInt("nombre_avis");
                    FormationScore formationScore = new FormationScore(formationId, noteMoyenne, nombreAvis);
                    return formationScore;
                }
            }
        }
        return null; // No FormationScore found for this formation
    }

    private List<Avis> getAvisForFormation(int formationId) throws SQLException {
        List<Avis> avisList = new ArrayList<>();
        String queryAvis = "SELECT id, note, commentaire, date_creation, formation_id, formation_score_id, instructeur_id, apprenant_id, is_flagged FROM avis WHERE formation_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(queryAvis)) {
            stmt.setInt(1, formationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    float note = rs.getFloat("note");
                    String commentaire = rs.getString("commentaire");
                    LocalDateTime dateCreation = rs.getTimestamp("date_creation").toLocalDateTime();
                    int formationScoreId = rs.getInt("formation_score_id");
                    int instructeurId = rs.getInt("instructeur_id");
                    int apprenantId = rs.getInt("apprenant_id");
                    boolean isFlagged = rs.getBoolean("is_flagged");

                    Avis avis = new Avis(note, commentaire, dateCreation, formationId);
                    avis.setId(id);
                    avis.setFormationScoreId(formationScoreId);
                    avis.setInstructeurId(instructeurId);
                    avis.setApprenantId(apprenantId);
                    avis.setFlagged(isFlagged);
                    // Note: flaggedReason is not fetched as it's an array; adjust if needed

                    avisList.add(avis);
                }
            }
        }
        return avisList;
    }

    public int getTotalAvisCount() throws SQLException {
        String query = "SELECT SUM(nombre_avis) FROM formation_score";
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public double getGlobalAverageScore() throws SQLException {
        String query = "SELECT AVG(note_moyenne) FROM formation_score WHERE nombre_avis > 0";
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }
}
