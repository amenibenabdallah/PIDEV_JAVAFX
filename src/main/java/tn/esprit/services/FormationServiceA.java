package tn.esprit.services;

import tn.esprit.models.Avis;
import tn.esprit.models.FormationA;
import tn.esprit.models.FormationScore;
import tn.esprit.models.instructeurs;
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
        String queryFormations = "SELECT f.id, f.titre, f.niveau, f.description, f.duree, f.categorie_id, c.nom AS category_name " +
                "FROM formation f " +
                "LEFT JOIN categorie c ON f.categorie_id = c.id";

        try (PreparedStatement stmt = cnx.prepareStatement(queryFormations);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("titre");
                String niveau = rs.getString("niveau");
                String description = rs.getString("description");
                String duree = rs.getString("duree");
                int categorieId = rs.getInt("categorie_id");
                String categoryName = rs.getString("category_name");

                // Fetch FormationScore for this formation
                FormationScore formationScore = getFormationScore(id);
                double averageScore = (formationScore != null) ? formationScore.getNoteMoyenne() : 0.0;
                int avisCount = (formationScore != null) ? formationScore.getNombreAvis() : 0;

                // Fetch avis for this formation
                List<Avis> avisList = getAvisForFormation(id);

                // Fetch instructors for this formation
                List<instructeurs> instructors = getInstructorsForFormation(id);

                formations.add(new FormationA(id, name, averageScore, avisCount, avisList, instructors, niveau, description, duree, categorieId, categoryName));
            }
        }
        return formations;
    }

    // New method to retrieve the formation for a given instructor ID
    public FormationA getFormationByInstructeurId(int instructeurId) throws SQLException {
        // First, get the formation_id from the instructeurs table
        String queryInstructeur = "SELECT formation_id FROM instructeurs WHERE id = ?";
        Integer formationId = null;

        try (PreparedStatement stmt = cnx.prepareStatement(queryInstructeur)) {
            stmt.setInt(1, instructeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    formationId = rs.getInt("formation_id");
                    if (rs.wasNull()) {
                        return null; // Instructor is not assigned to any formation
                    }
                } else {
                    return null; // Instructor not found
                }
            }
        }

        // Now, fetch the formation using the formation_id
        String queryFormation = "SELECT f.id, f.titre, f.niveau, f.description, f.duree, f.categorie_id, c.nom AS category_name " +
                "FROM formation f " +
                "LEFT JOIN categorie c ON f.categorie_id = c.id " +
                "WHERE f.id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(queryFormation)) {
            stmt.setInt(1, formationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("titre");
                    String niveau = rs.getString("niveau");
                    String description = rs.getString("description");
                    String duree = rs.getString("duree");
                    int categorieId = rs.getInt("categorie_id");
                    String categoryName = rs.getString("category_name");

                    // Fetch FormationScore for this formation
                    FormationScore formationScore = getFormationScore(id);
                    double averageScore = (formationScore != null) ? formationScore.getNoteMoyenne() : 0.0;
                    int avisCount = (formationScore != null) ? formationScore.getNombreAvis() : 0;

                    // Fetch avis for this formation
                    List<Avis> avisList = getAvisForFormation(id);

                    // Fetch instructors for this formation
                    List<instructeurs> instructors = getInstructorsForFormation(id);

                    return new FormationA(id, name, averageScore, avisCount, avisList, instructors, niveau, description, duree, categorieId, categoryName);
                }
            }
        }
        return null; // Formation not found
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
                    avisList.add(avis);
                }
            }
        }
        return avisList;
    }

    private List<instructeurs> getInstructorsForFormation(int formationId) throws SQLException {
        List<instructeurs> instructors = new ArrayList<>();
        String queryInstructors = "SELECT * FROM instructeurs WHERE formation_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(queryInstructors)) {
            stmt.setInt(1, formationId);
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
        return instructors;
    }
    public FormationA getById(int formationId) throws SQLException {
        String queryFormation = "SELECT f.id, f.titre, f.niveau, f.description, f.duree, f.categorie_id, c.nom AS category_name " +
                "FROM formation f " +
                "LEFT JOIN categorie c ON f.categorie_id = c.id " +
                "WHERE f.id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(queryFormation)) {
            stmt.setInt(1, formationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("titre");
                    String niveau = rs.getString("niveau");
                    String description = rs.getString("description");
                    String duree = rs.getString("duree");
                    int categorieId = rs.getInt("categorie_id");
                    String categoryName = rs.getString("category_name");

                    // Fetch FormationScore for this formation
                    FormationScore formationScore = getFormationScore(id);
                    double averageScore = (formationScore != null) ? formationScore.getNoteMoyenne() : 0.0;
                    int avisCount = (formationScore != null) ? formationScore.getNombreAvis() : 0;

                    // Fetch avis for this formation
                    List<Avis> avisList = getAvisForFormation(id);

                    // Fetch instructors for this formation
                    List<instructeurs> instructors = getInstructorsForFormation(id);

                    return new FormationA(id, name, averageScore, avisCount, avisList, instructors, niveau, description, duree, categorieId, categoryName);
                }
            }
        }
        return null; // Formation not found
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