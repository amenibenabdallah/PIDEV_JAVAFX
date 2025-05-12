package tn.esprit.services;

import javafx.scene.chart.XYChart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tn.esprit.models.User;
import tn.esprit.utils.EmailUtil;
import tn.esprit.utils.MyDataBase;
import tn.esprit.utils.TwilioSMSUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection conn;

    public UserService() {
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // ➤ Ajouter un utilisateur
    public boolean addUser(User u) {
        String sql = "INSERT INTO user (email, password, nom, prenom, date_naissance, role, cv, image, reset_token, niveau_etude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNom());
            ps.setString(4, u.getPrenom());
            ps.setDate(5, Date.valueOf(u.getDateNaissance()));
            ps.setString(6, u.getRole());
            ps.setString(7, u.getCv());
            ps.setString(8, u.getImage());
            ps.setString(9, u.getResetToken());
            ps.setString(10, u.getNiveauEtude());   // Nouveau champ

            ps.executeUpdate();
            System.out.println("✅ Utilisateur ajouté avec succès !");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            return false;
        }
    }

    // ➤ Récupérer tous les utilisateurs
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, nom, email, role, niveau_etude FROM user WHERE role IN ('APPRENANT', 'INSTRUCTEUR')";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setNiveauEtude(rs.getString("niveau_etude"));  // Ajout niveauEtude
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération utilisateurs : " + e.getMessage());
        }
        return list;
    }

    // ➤ Supprimer un utilisateur
    public void deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Utilisateur supprimé ID = " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
        }
    }

    // ➤ Mettre à jour un utilisateur (avec niveauEtude)
    public void updateUser(User user) {
        String sql = "UPDATE user SET nom = ?, email = ?, role = ?, niveau_etude = ? ,prenom =? ,image=? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getNiveauEtude());
            ps.setInt(5, user.getId());
            ps.setString(6, user.getPrenom());
            ps.setString(7, user.getImage());

            ps.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour : " + e.getMessage());
        }
    }

    // ➤ Rechercher des utilisateurs
    public List<User> searchUsers(String keyword) {
        List<User> results = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE nom LIKE ? OR email LIKE ? OR role LIKE ? OR niveau_etude LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = mapResultSetToUser(rs);
                results.add(u);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche : " + e.getMessage());
        }
        return results;
    }

    // ➤ Vérifier si un email existe
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification email : " + e.getMessage());
        }
        return false;
    }

    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findUserByEmail : " + e.getMessage());
        }
        return null;
    }

    public User findUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findUserById : " + e.getMessage());
        }
        return null;
    }

    // ➤ Mapper le ResultSet vers User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setCv(rs.getString("cv"));
        u.setImage(rs.getString("image"));
        u.setResetToken(rs.getString("reset_token"));
        u.setNiveauEtude(rs.getString("niveau_etude"));  // Ajout niveauEtude
        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            u.setDateNaissance(dateNaissance.toLocalDate());
        }
        return u;
    }
    public boolean resetPasswordWithToken(String token, String newPassword) {
        String sql = "SELECT * FROM user WHERE reset_token = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashedPassword = new BCryptPasswordEncoder().encode(newPassword);
                PreparedStatement update = conn.prepareStatement("UPDATE user SET password = ?, reset_token = NULL WHERE id = ?");
                update.setString(1, hashedPassword);
                update.setInt(2, rs.getInt("id"));
                update.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur reset password: " + e.getMessage());
        }
        return false;
    }
    public boolean isValidToken(String token) {
        String sql = "SELECT COUNT(*) FROM user WHERE reset_token = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur token: " + e.getMessage());
        }
        return false;
    }


    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des utilisateurs : " + e.getMessage());
        }
        return 0;
    }
    public int countApprenantsByNiveau(String niveau) {
        String sql = "SELECT COUNT(*) FROM user WHERE role = 'APPRENANT' AND niveau_etude = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, niveau);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur comptage par niveau : " + e.getMessage());
        }
        return 0;
    }
    public double getAverageAge() {
        String sql = "SELECT AVG(TIMESTAMPDIFF(YEAR, date_naissance, CURDATE())) AS avg_age FROM user WHERE date_naissance IS NOT NULL";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("avg_age");
            }
        } catch (SQLException e) {
            System.err.println("Erreur calcul âge moyen : " + e.getMessage());
        }
        return 0;
    }
    public double getProfileCompletionRate() {
        String sql = "SELECT COUNT(*) AS total, " +
                "SUM(CASE WHEN role = 'INSTRUCTEUR' AND cv IS NOT NULL AND image IS NOT NULL THEN 1 " +
                "          WHEN role = 'APPRENANT' AND image IS NOT NULL THEN 1 ELSE 0 END) AS completed " +
                "FROM user WHERE role IN ('APPRENANT', 'INSTRUCTEUR')";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int completed = rs.getInt("completed");
                if (total == 0) return 0;
                return (completed * 100.0) / total;  // Retourne un pourcentage
            }
        } catch (SQLException e) {
            System.err.println("Erreur taux complétion : " + e.getMessage());
        }
        return 0;
    }


    public XYChart.Series<String, Number> getMonthlyInscriptionData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions 2025");

        String sql = "SELECT DATE_FORMAT(created_at, '%b') AS month, COUNT(*) AS total " +
                "FROM user " +
                "WHERE YEAR(created_at) = YEAR(CURDATE()) " +
                "GROUP BY MONTH(created_at) " +
                "ORDER BY MONTH(created_at)";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String month = rs.getString("month");  // Ex: Jan, Feb, Mar
                int total = rs.getInt("total");
                series.getData().add(new XYChart.Data<>(month, total));
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération des inscriptions mensuelles : " + e.getMessage());
        }

        return series;
    }

    public boolean generateResetTokenEmailOnly(String email) {
        int x = (int)(Math.random() * 900000) + 100000;
        String token = String.valueOf(x);
        String sql = "UPDATE user SET reset_token = ? WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, email);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                String htmlBody = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Réinitialisation de mot de passe</title>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                    .header { text-align: center; background-color: #e74c3c; color: white; padding: 10px 0; border-radius: 8px 8px 0 0; }
                    .content { padding: 20px; text-align: center; }
                    .token { font-size: 28px; color: #e74c3c; font-weight: bold; margin: 20px 0; }
                    .footer { text-align: center; font-size: 14px; color: #777; padding-top: 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>Formini</h1></div>
                    <div class="content">
                        <h2>Réinitialisation de mot de passe</h2>
                        <p>Bonjour,</p>
                        <p>Vous avez demandé à réinitialiser votre mot de passe. Voici votre code de réinitialisation :</p>
                        <div class="token">%s</div>
                        <p>Veuillez utiliser ce code pour réinitialiser votre mot de passe. Il est valide pendant une durée limitée.</p>
                        <p>Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer cet e-mail.</p>
                    </div>
                    <div class="footer">L'équipe Formini</div>
                </div>
            </body>
            </html>
            """, token);

                EmailUtil.sendHtmlEmail(email, "🔐 Code de réinitialisation", htmlBody);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur génération token (Email) : " + e.getMessage());
        }
        return false;
    }




    public boolean generateResetTokenSMS(String email, String phoneNumber) {
        int x = (int)(Math.random() * 900000) + 100000;
        String token = String.valueOf(x);
        String sql = "UPDATE user SET reset_token = ? WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, email);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                String body = "Votre Token de Réinitialisation : " + token;
                TwilioSMSUtil.sendSMS(phoneNumber, body);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur génération token (SMS) : " + e.getMessage());
        }
        return false;
    }

    public int getLastInsertedIdByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM user WHERE email = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Aucun ID trouvé pour l'email : " + email);
                }
            }
        }
    }



}