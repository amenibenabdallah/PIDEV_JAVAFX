package tn.esprit.services;

import tn.esprit.models.Discussion;
import tn.esprit.models.GroupDiscussion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscussionService {
    private Connection connection;

    public DiscussionService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/forminiahmed3", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer toutes les discussions
    public List<Discussion> getAllDiscussions() {
        List<Discussion> discussions = new ArrayList<>();
        String query = "SELECT d.*, u1.nom AS sender_nom, u1.prenom AS sender_prenom, " +
                "u2.nom AS receiver_nom, u2.prenom AS receiver_prenom " +
                "FROM discussion d " +
                "JOIN users u1 ON d.sender_id = u1.id " +
                "JOIN users u2 ON d.receiver_id = u2.id";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                String senderName = rs.getString("sender_prenom") + " " + rs.getString("sender_nom");
                String receiverName = rs.getString("receiver_prenom") + " " + rs.getString("receiver_nom");

                discussions.add(new Discussion(id, senderId, receiverId, senderName, receiverName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return discussions;
    }
    public void addDiscussion(int senderId, int receiverId) {
        String req = "INSERT INTO discussion (sender_id, receiver_id) VALUES (" + senderId + ", " + receiverId + ")";
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<GroupDiscussion> getGroupsForUser(int userId) throws SQLException {
        List<GroupDiscussion> groups = new ArrayList<>();
        String query = "SELECT g.id, g.name FROM diss_grp g " +
                "JOIN diss_grp_users gu ON g.id = gu.group_id " +
                "WHERE gu.user_id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            GroupDiscussion group = new GroupDiscussion();
            group.setId(rs.getInt("id"));
            group.setName(rs.getString("name"));
            // Tu peux aussi charger les membres ici si nécessaire
            groups.add(group);
        }
        return groups;
    }


}
