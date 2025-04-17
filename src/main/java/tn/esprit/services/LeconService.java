package tn.esprit.services;


import tn.esprit.models.Formation;
import tn.esprit.models.Lecon;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeconService implements IService<Lecon> {

    private Connection cnx;

    public LeconService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Lecon lecon) throws SQLException {
        String query = "INSERT INTO lecon (titre, contenu, date_creation, formation_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, lecon.getTitre());
            pst.setString(2, lecon.getContenu());
            pst.setDate(3, Date.valueOf(lecon.getDateCreation()));
            pst.setInt(4, lecon.getFormation().getId());

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                lecon.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Lecon lecon) throws SQLException {
        String query = "UPDATE lecon SET titre = ?, contenu = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, lecon.getTitre());
            pst.setString(2, lecon.getContenu());

            pst.setInt(3, lecon.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM lecon WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Lecon> getAll() throws SQLException {
        List<Lecon> lecons = new ArrayList<>();
        String query = "SELECT * FROM lecon";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Lecon lecon = new Lecon();
                lecon.setId(rs.getInt("id"));
                lecon.setTitre(rs.getString("titre"));
                lecon.setContenu(rs.getString("contenu"));
                lecon.setDateCreation(rs.getDate("date_creation").toLocalDate());


                Formation formation = new Formation();
                formation.setId(rs.getInt("formation_id"));
                lecon.setFormation(formation);

                lecons.add(lecon);
            }
        }
        return lecons;
    }

    public List<Lecon> getByFormation(int formationId) throws SQLException {
        List<Lecon> lecons = new ArrayList<>();
        String query = "SELECT * FROM lecon WHERE formation_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, formationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Lecon lecon = new Lecon();
                lecon.setId(rs.getInt("id"));
                lecon.setTitre(rs.getString("titre"));
                lecon.setContenu(rs.getString("contenu"));
                lecon.setDateCreation(rs.getDate("date_creation").toLocalDate());


                Formation formation = new Formation();
                formation.setId(rs.getInt("formation_id"));
                lecon.setFormation(formation);

                lecons.add(lecon);
            }
        }
        return lecons;
    }

    @Override
    public Lecon getById(int id) throws SQLException {
        Lecon lecon = null;
        String query = "SELECT * FROM lecon WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                lecon = new Lecon();
                lecon.setId(rs.getInt("id"));
                lecon.setTitre(rs.getString("titre"));
                lecon.setContenu(rs.getString("contenu"));
                lecon.setDateCreation(rs.getDate("date_creation").toLocalDate());


                Formation formation = new Formation();
                formation.setId(rs.getInt("formation_id"));
                lecon.setFormation(formation);
            }
        }
        return lecon;
    }
}
