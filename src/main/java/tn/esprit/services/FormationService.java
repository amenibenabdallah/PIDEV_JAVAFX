package tn.esprit.services;

import tn.esprit.services.IService;
import tn.esprit.models.Categorie;
import tn.esprit.models.Formation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService implements IService<Formation> {

    private Connection cnx;
    private PreparedStatement pst;

    public FormationService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Formation formation) throws SQLException {
        String query = "INSERT INTO formation (titre, description, duree, niveau, date_creation, prix, image_name, categorie_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, formation.getTitre());
            pst.setString(2, formation.getDescription());
            pst.setString(3, formation.getDuree());
            pst.setString(4, formation.getNiveau());
            pst.setDate(5, Date.valueOf(formation.getDateCreation()));
            pst.setFloat(6, formation.getPrix());
            pst.setString(7, formation.getImageName());
            pst.setInt(8, formation.getCategorie().getId());
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                formation.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new SQLException("Error while adding formation: " + e.getMessage());
        }
    }

    @Override
    public void update(Formation formation) throws SQLException {
        String query = "UPDATE formation SET titre = ?, description = ?, duree = ?, niveau = ?, date_creation = ?, prix = ?, image_name = ?, categorie_id = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, formation.getTitre());
            pst.setString(2, formation.getDescription());
            pst.setString(3, formation.getDuree());
            pst.setString(4, formation.getNiveau());
            pst.setDate(5, Date.valueOf(formation.getDateCreation()));
            pst.setFloat(6, formation.getPrix());
            pst.setString(7, formation.getImageName());
            pst.setInt(8, formation.getCategorie().getId());
            pst.setInt(9, formation.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while updating formation: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM formation WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while deleting formation: " + e.getMessage());
        }
    }

    @Override
    public List<Formation> getAll() throws SQLException {
        List<Formation> formations = new ArrayList<>();
        String query = "SELECT f.*, c.nom AS categorie_nom FROM formation f JOIN categorie c ON f.categorie_id = c.id";
        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Formation formation = new Formation();
                formation.setId(resultSet.getInt("id"));
                formation.setTitre(resultSet.getString("titre"));
                formation.setDescription(resultSet.getString("description"));
                formation.setDuree(resultSet.getString("duree"));
                formation.setNiveau(resultSet.getString("niveau"));
                formation.setDateCreation(resultSet.getDate("date_creation").toLocalDate());
                formation.setPrix(resultSet.getFloat("prix"));
                formation.setImageName(resultSet.getString("image_name"));

                Categorie categorie = new Categorie();
                categorie.setId(resultSet.getInt("categorie_id"));
                categorie.setNom(resultSet.getString("categorie_nom")); // Set the name here
                formation.setCategorie(categorie);

                formations.add(formation);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while fetching formations: " + e.getMessage());
        }
        return formations;
    }

    @Override
    public Formation getById(int id) throws SQLException {
        Formation formation = null;
        String query = "SELECT f.*, c.nom AS categorie_nom FROM formation f JOIN categorie c ON f.categorie_id = c.id WHERE f.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                formation = new Formation();
                formation.setId(rs.getInt("id"));
                formation.setTitre(rs.getString("titre"));
                formation.setDescription(rs.getString("description"));
                formation.setDuree(rs.getString("duree"));
                formation.setNiveau(rs.getString("niveau"));
                formation.setDateCreation(rs.getDate("date_creation").toLocalDate());
                formation.setPrix(rs.getFloat("prix"));
                formation.setImageName(rs.getString("image_name"));

                Categorie categorie = new Categorie();
                categorie.setId(rs.getInt("categorie_id"));
                categorie.setNom(rs.getString("categorie_nom")); // Set the name here
                formation.setCategorie(categorie);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while fetching formation by ID: " + e.getMessage());
        }
        return formation;
    }
}
