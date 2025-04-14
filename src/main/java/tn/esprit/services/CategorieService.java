package tn.esprit.services;

import tn.esprit.models.Categorie;
import tn.esprit.utils.MyDataBase;
import tn.esprit.interfaces.IService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {
    private Connection cnx;
    private PreparedStatement pst;

    public CategorieService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Categorie categorie) throws SQLException {
        String query = "INSERT INTO categorie (nom, description) VALUES (?, ?)";
        try {
            pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, categorie.getNom());
            pst.setString(2, categorie.getDescription());
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                categorie.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new SQLException("Error while adding category: " + e.getMessage());
        }
    }

    @Override
    public void update(Categorie categorie) throws SQLException {
        String query = "UPDATE categorie SET nom = ?, description = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, categorie.getNom());
            pst.setString(2, categorie.getDescription());
            pst.setInt(3, categorie.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while updating category: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM categorie WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while deleting category: " + e.getMessage());
        }
    }

    @Override
    public List<Categorie> getAll() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie";
        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Categorie categorie = new Categorie(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description")
                );
                categories.add(categorie);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while fetching categories: " + e.getMessage());
        }
        return categories;
    }

    @Override
    public Categorie getById(int id) throws SQLException {
        Categorie categorie = null;
        String query = "SELECT * FROM categorie WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                categorie = new Categorie(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
            }
        } catch (SQLException e) {
            throw new SQLException("Error while fetching category by ID: " + e.getMessage());
        }
        return categorie;
    }
}

