package tn.esprit.services;

import tn.esprit.models.Formation1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFormation1 {
    private static final String URL = "jdbc:mysql://localhost:3306/forminifix";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Formation1> getIdAndTitre() {
        List<Formation1> formations = new ArrayList<>();
        String query = "SELECT id, titre FROM formation";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Formation1 formation = new Formation1();
                formation.setId(rs.getInt("id"));
                formation.setTitre(rs.getString("titre"));
                formations.add(formation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formations;
    }

    public double getPrixById(int id) {
        double prix = 0.0;
        String query = "SELECT prix FROM formation WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prix = rs.getDouble("prix");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prix;
    }
}