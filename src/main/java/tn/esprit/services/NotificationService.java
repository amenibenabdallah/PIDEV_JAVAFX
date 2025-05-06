package tn.esprit.services;

import tn.esprit.models.Evenement;
import tn.esprit.models.Notification;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final Connection cnx;

    public NotificationService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Notification notification) {
        String sql = "INSERT INTO notification (titre, contenu, sent_at, evenement_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, notification.getTitre());
            stmt.setString(2, notification.getContenu());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getSentAt()));
            stmt.setInt(4, notification.getEvenement() != null ? notification.getEvenement().getId() : null);
            stmt.executeUpdate();

            // Get generated ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                notification.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Notification get(int id) {
        String sql = "SELECT n.id, n.titre, n.contenu, n.sent_at, n.evenement_id, " +
                "e.id AS evt_id, e.titre AS evt_titre, e.date, e.description, e.photo " +
                "FROM notification n LEFT JOIN evenement e ON n.evenement_id = e.id WHERE n.id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setTitre(rs.getString("titre"));
                notification.setContenu(rs.getString("contenu"));
                Timestamp sentAt = rs.getTimestamp("sent_at");
                if (sentAt != null) {
                    notification.setSentAt(sentAt.toLocalDateTime());
                }

                if (rs.getInt("evenement_id") != 0) {
                    Evenement evenement = new Evenement();
                    evenement.setId(rs.getInt("evt_id"));
                    evenement.setTitre(rs.getString("evt_titre"));
                    evenement.setDate(rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null);
                    evenement.setDescription(rs.getString("description"));
                    evenement.setPhoto(rs.getString("photo"));
                    notification.setEvenement(evenement);
                }

                return notification;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Notification> getAll() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT n.id, n.titre, n.contenu, n.sent_at, n.evenement_id, " +
                "e.id AS evt_id, e.titre AS evt_titre, e.date, e.description, e.photo " +
                "FROM notification n LEFT JOIN evenement e ON n.evenement_id = e.id";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setTitre(rs.getString("titre"));
                notification.setContenu(rs.getString("contenu"));
                Timestamp sentAt = rs.getTimestamp("sent_at");
                if (sentAt != null) {
                    notification.setSentAt(sentAt.toLocalDateTime());
                }

                if (rs.getInt("evenement_id") != 0) {
                    Evenement evenement = new Evenement();
                    evenement.setId(rs.getInt("evt_id"));
                    evenement.setTitre(rs.getString("evt_titre"));
                    evenement.setDate(rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null);
                    evenement.setDescription(rs.getString("description"));
                    evenement.setPhoto(rs.getString("photo"));
                    notification.setEvenement(evenement);
                }

                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public void update(Notification notification) {
        String sql = "UPDATE notification SET titre = ?, contenu = ?, sent_at = ?, evenement_id = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, notification.getTitre());
            stmt.setString(2, notification.getContenu());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getSentAt()));
            stmt.setObject(4, notification.getEvenement() != null ? notification.getEvenement().getId() : null);
            stmt.setInt(5, notification.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM notification WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
