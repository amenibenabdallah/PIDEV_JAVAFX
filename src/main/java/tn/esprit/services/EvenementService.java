package tn.esprit.services;

import tn.esprit.models.Evenement;
import tn.esprit.models.Notification;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {
    private final Connection cnx;

    public EvenementService() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Evenement evenement) {
        String sqlEvenement = "INSERT INTO evenement (titre, date, description, photo, emplacement) VALUES (?, ?, ?, ?, ?)";
        String sqlNotification = "INSERT INTO notification (titre, contenu, sent_at, evenement_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmtEvenement = cnx.prepareStatement(sqlEvenement, Statement.RETURN_GENERATED_KEYS)) {
            stmtEvenement.setString(1, evenement.getTitre());
            stmtEvenement.setDate(2, Date.valueOf(evenement.getDate()));
            stmtEvenement.setString(3, evenement.getDescription());
            stmtEvenement.setString(4, evenement.getPhoto());
            stmtEvenement.setString(5, evenement.getEmplacement());
            stmtEvenement.executeUpdate();

            ResultSet rs = stmtEvenement.getGeneratedKeys();
            if (rs.next()) {
                evenement.setId(rs.getInt(1));
            }

            if (evenement.getNotification() != null) {
                try (PreparedStatement stmtNotification = cnx.prepareStatement(sqlNotification)) {
                    stmtNotification.setString(1, evenement.getNotification().getTitre());
                    stmtNotification.setString(2, evenement.getNotification().getContenu());
                    stmtNotification.setTimestamp(3, Timestamp.valueOf(evenement.getNotification().getSentAt()));
                    stmtNotification.setInt(4, evenement.getId());
                    stmtNotification.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Evenement get(int id) {
        String sql = "SELECT e.id, e.titre, e.date, e.description, e.photo, e.emplacement, " +
                "n.id AS notif_id, n.titre AS notif_titre, n.contenu, n.sent_at " +
                "FROM evenement e LEFT JOIN notification n ON e.id = n.evenement_id WHERE e.id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setTitre(rs.getString("titre"));
                evenement.setDate(rs.getDate("date").toLocalDate());
                evenement.setDescription(rs.getString("description"));
                evenement.setPhoto(rs.getString("photo"));
                evenement.setEmplacement(rs.getString("emplacement"));

                if (rs.getInt("notif_id") != 0) {
                    Notification notification = new Notification();
                    notification.setId(rs.getInt("notif_id"));
                    notification.setTitre(rs.getString("notif_titre"));
                    notification.setContenu(rs.getString("contenu"));
                    Timestamp sentAt = rs.getTimestamp("sent_at");
                    if (sentAt != null) {
                        notification.setSentAt(sentAt.toLocalDateTime());
                    }
                    notification.setEvenement(evenement);
                    evenement.setNotification(notification);
                }

                return evenement;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Evenement> getAll() {
        List<Evenement> evenements = new ArrayList<>();
        String sql = "SELECT e.id, e.titre, e.date, e.description, e.photo, e.emplacement, " +
                "n.id AS notif_id, n.titre AS notif_titre, n.contenu, n.sent_at " +
                "FROM evenement e LEFT JOIN notification n ON e.id = n.evenement_id";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setTitre(rs.getString("titre"));
                evenement.setDate(rs.getDate("date").toLocalDate());
                evenement.setDescription(rs.getString("description"));
                evenement.setPhoto(rs.getString("photo"));
                evenement.setEmplacement(rs.getString("emplacement"));

                if (rs.getInt("notif_id") != 0) {
                    Notification notification = new Notification();
                    notification.setId(rs.getInt("notif_id"));
                    notification.setTitre(rs.getString("notif_titre"));
                    notification.setContenu(rs.getString("contenu"));
                    Timestamp sentAt = rs.getTimestamp("sent_at");
                    if (sentAt != null) {
                        notification.setSentAt(sentAt.toLocalDateTime());
                    }
                    notification.setEvenement(evenement);
                    evenement.setNotification(notification);
                }

                evenements.add(evenement);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenements;
    }

    public void update(Evenement evenement) {
        String sqlEvenement = "UPDATE evenement SET titre = ?, date = ?, description = ?, photo = ?, emplacement = ? WHERE id = ?";
        String sqlNotification = "INSERT INTO notification (titre, contenu, sent_at, evenement_id) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE titre = ?, contenu = ?, sent_at = ?";

        try (PreparedStatement stmtEvenement = cnx.prepareStatement(sqlEvenement)) {
            stmtEvenement.setString(1, evenement.getTitre());
            stmtEvenement.setDate(2, Date.valueOf(evenement.getDate()));
            stmtEvenement.setString(3, evenement.getDescription());
            stmtEvenement.setString(4, evenement.getPhoto());
            stmtEvenement.setString(5, evenement.getEmplacement());
            stmtEvenement.setInt(6, evenement.getId());
            stmtEvenement.executeUpdate();

            if (evenement.getNotification() != null) {
                try (PreparedStatement stmtNotification = cnx.prepareStatement(sqlNotification)) {
                    stmtNotification.setString(1, evenement.getNotification().getTitre());
                    stmtNotification.setString(2, evenement.getNotification().getContenu());
                    stmtNotification.setTimestamp(3, Timestamp.valueOf(evenement.getNotification().getSentAt()));
                    stmtNotification.setInt(4, evenement.getId());
                    stmtNotification.setString(5, evenement.getNotification().getTitre());
                    stmtNotification.setString(6, evenement.getNotification().getContenu());
                    stmtNotification.setTimestamp(7, Timestamp.valueOf(evenement.getNotification().getSentAt()));
                    stmtNotification.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = cnx.prepareStatement("DELETE FROM notification WHERE evenement_id = ?")) {
                    stmt.setInt(1, evenement.getId());
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM evenement WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}