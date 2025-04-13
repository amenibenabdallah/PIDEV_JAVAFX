package tn.esprit.services;

import tn.esprit.models.Message;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private final Connection cnx = MyDataBase.getInstance().getCnx();

    public void envoyerMessage(Message message) {
        String sql = "INSERT INTO message (sender_id, receiver_id, content, created_at, discussion_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            ps.setString(3, message.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(message.getCreatedAt()));
            ps.setInt(5, message.getDiscussionId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerMessage(int id) {
        String sql = "DELETE FROM message WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierMessage(int id, String nouveauContenu) {
        String sql = "UPDATE message SET content = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouveauContenu);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getMessages(int senderId, int receiverId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY created_at";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setInt(3, receiverId); // inversé
            ps.setInt(4, senderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("discussion_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }
}
