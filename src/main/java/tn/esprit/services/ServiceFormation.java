package tn.esprit.services;

import tn.esprit.models.Formation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFormation {
    private static final String URL = "jdbc:mysql://localhost:3306/formini111";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Formation> getIdAndTitre() {
        List<Formation> formations = new ArrayList<>();
        String query = "SELECT id, titre FROM formation";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Formation formation = new Formation();
                formation.setId(rs.getInt("id"));
                formation.setTitre(rs.getString("titre"));
                formations.add(formation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formations;
    }
}