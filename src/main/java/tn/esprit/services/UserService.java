package tn.esprit.services;

import tn.esprit.models.users;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection conn;

    public UserService() {
        this.conn = MyDataBase.getInstance().getCnx();
    }

    public void addUser(users u, String niveauEtude) {
        try {
            // 1. Convertir les rôles en format JSON
            String[] rolesArray = u.getRoles().split(",");
            StringBuilder jsonRolesBuilder = new StringBuilder("[");
            for (int i = 0; i < rolesArray.length; i++) {
                jsonRolesBuilder.append("\"").append(rolesArray[i].trim()).append("\"");
                if (i < rolesArray.length - 1) {
                    jsonRolesBuilder.append(",");
                }
            }
            jsonRolesBuilder.append("]");
            String jsonRoles = jsonRolesBuilder.toString();

            // 2. Insertion dans `users`
            String sqlUser = "INSERT INTO users (email, roles, password, nom, prenom, date_de_naissance, reset_token, user_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, u.getEmail());
            ps.setString(2, jsonRoles);
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setDate(6, Date.valueOf(u.getDateNaissance()));
            ps.setString(7, u.getResetToken());
            ps.setString(8, u.getUserType());

            ps.executeUpdate();
            System.out.println("✅ Utilisateur inséré dans `users`");

            // 3. Récupération de l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            int userId = -1;
            if (rs.next()) {
                userId = rs.getInt(1);
            }
            System.out.println("🆔 ID utilisateur : " + userId);

            // 4. Insertion dans `apprenants`
            if (u.getRoles().equals("ROLE_APPRENANT")) {
                String sqlApprenant = "INSERT INTO apprenants (id, nom_apprenant, prenom_apprenant, email_apprenant, niveau_etude) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps2 = conn.prepareStatement(sqlApprenant);
                ps2.setInt(1, userId);
                ps2.setString(2, u.getNom());
                ps2.setString(3, u.getPrenom());
                ps2.setString(4, u.getEmail());
                ps2.setString(5, niveauEtude);
                ps2.executeUpdate();

                System.out.println("🎓 Apprenant inséré dans `apprenants`");
            }

            // 5. Insertion dans `instructeurs` avec image
            if (u.getRoles().equals("ROLE_INSTRUCTEUR")) {
                String sqlInstructeur = "INSERT INTO instructeurs (id, nom_instructeur, prenom_instructeur, email_instructeur, image) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps3 = conn.prepareStatement(sqlInstructeur);
                ps3.setInt(1, userId);
                ps3.setString(2, u.getNom());
                ps3.setString(3, u.getPrenom());
                ps3.setString(4, u.getEmail());
                ps3.setString(5, "C:\\Users\\Yassm\\Desktop\\pi\\photo.jpg"); // 👈 chemin absolu ici

                ps3.executeUpdate();

                System.out.println("📘 Instructeur inséré dans `instructeurs` avec image");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur d'ajout dans la base : " + e.getMessage());
        }
    }
    public List<users> getAllUsers() {
        List<users> list = new ArrayList<>();
        String sql = "SELECT id, nom,email, roles FROM users";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users u = new users();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setRoles(rs.getString("roles"));
                u.setEmail(rs.getString("email"));

                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
        return list;
    }
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Utilisateur supprimé avec ID = " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur de suppression : " + e.getMessage());
        }
    }

    // Mettre à jour un utilisateur
    public void updateUser(users user) {
        String query = "UPDATE users SET nom = ?, email = ?, roles = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getRoles());
            pst.setInt(4, user.getId());
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected == 0) {
                System.err.println("Aucun utilisateur trouvé avec l'ID: " + user.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }


    // Rechercher des utilisateurs
    public List<users> searchUsers(String keyword) {
        List<users> results = new ArrayList<>();
        String query = "SELECT * FROM users WHERE nom LIKE ? OR email LIKE ? OR roles LIKE ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                users user = new users();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                results.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche d'utilisateurs: " + e.getMessage());
        }
        return results;
    }
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public users findUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    users user = new users();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    // ... autres champs
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
