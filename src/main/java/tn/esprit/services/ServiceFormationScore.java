package tn.esprit.services;

import tn.esprit.models.FormationScore;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFormationScore {
    private Connection cnx;

    public ServiceFormationScore() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public List<FormationScore> getAll() {
        List<FormationScore> formationScores = new ArrayList<>();
        String qry = "SELECT * FROM formation_score";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                FormationScore formationScore = new FormationScore();
                formationScore.setId(rs.getInt("id"));
                formationScore.setFormationId(rs.getInt("formation_id"));
                formationScore.setNoteMoyenne(rs.getDouble("note_moyenne"));
                formationScore.setNombreAvis(rs.getInt("nombre_avis"));
                formationScores.add(formationScore);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching FormationScores: " + e.getMessage());
        }
        return formationScores;
    }

    public FormationScore getByFormationId(int formationId) {
        FormationScore formationScore = null;
        String qry = "SELECT * FROM formation_score WHERE formation_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, formationId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                double noteMoyenne = rs.getDouble("note_moyenne");
                int nombreAvis = rs.getInt("nombre_avis");
                return new FormationScore(formationId, noteMoyenne, nombreAvis);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching FormationScore: " + e.getMessage());
        }
        return null;
    }
}
