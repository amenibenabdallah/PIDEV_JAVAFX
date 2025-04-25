package tn.esprit.services;

import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection conn;

    public UserService() {
        this.conn = MyDataBase.getInstance().getCnx();
    }

    // ➤ Ajouter un utilisateur
    public boolean addUser(User u, String niveauEtude) {
        String sql = "INSERT INTO user (email, password, nom, prenom, date_naissance, role, cv, image, reset_token) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            ps.executeUpdate();
            System.out.println("✅ Utilisateur ajouté avec succès !");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            return false;
        }
    }

    // ➤ Récupérer tous les utilisateurs (hors ADMIN si besoin)
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, nom, email, role FROM user WHERE role IN ('APPRENANT', 'INSTRUCTEUR')";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
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

    // ➤ Mettre à jour un utilisateur
    public void updateUser(User user) {
        String sql = "UPDATE user SET nom = ?, email = ?, role = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour : " + e.getMessage());
        }
    }

    // ➤ Rechercher des utilisateurs
    public List<User> searchUsers(String keyword) {
        List<User> results = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE nom LIKE ? OR email LIKE ? OR role LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
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

    // ➤ Trouver un utilisateur par email
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

    // ➤ Trouver un utilisateur par ID
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

    // ➤ Méthode utilitaire pour mapper le ResultSet vers User
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
        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            u.setDateNaissance(dateNaissance.toLocalDate());
        }
        return u;
    }
}
