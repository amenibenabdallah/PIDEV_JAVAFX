package tn.esprit.services;

import tn.esprit.models.Promotion;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicePromotion {

    private final Connection connection;

    public ServicePromotion() {
        connection = MyDataBase.getInstance().getCnx();
    }

    // 🔹 CREATE
    public void add(Promotion promotion) {
        // Validation : Vérifier que la date d'expiration n'est pas passée
        if (promotion.getDateExpiration().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("❌ La date d'expiration de la promotion est passée.");
        }

        String sql = "INSERT INTO promotion (code_promo, description, remise, date_expiration, inscription_cours_id, apprenant_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, promotion.getCodePromo());
            ps.setString(2, promotion.getDescription());
            ps.setDouble(3, promotion.getRemise());
            ps.setDate(4, Date.valueOf(promotion.getDateExpiration()));
            ps.setInt(5, promotion.getInscriptionCoursId());
            ps.setInt(6, promotion.getApprenantId());
            ps.executeUpdate();
            System.out.println("✅ Promotion ajoutée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la promotion : " + e.getMessage());
        }
    }

    // 🔹 READ ALL
    public List<Promotion> getAll() {
        List<Promotion> promotions = new ArrayList<>();
        String sql = "SELECT * FROM promotion";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Promotion p = new Promotion(
                        rs.getInt("id"),
                        rs.getString("code_promo"),
                        rs.getString("description"),
                        rs.getDouble("remise"),
                        rs.getDate("date_expiration").toLocalDate(),
                        rs.getInt("inscription_cours_id"),
                        rs.getInt("apprenant_id")
                );
                promotions.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des promotions : " + e.getMessage());
        }
        return promotions;
    }

    // 🔹 UPDATE
    public void update(Promotion promotion) {
        // Validation : Vérifier que la date d'expiration n'est pas passée
        if (promotion.getDateExpiration().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("❌ La date d'expiration de la promotion est passée.");
        }

        String sql = "UPDATE promotion SET code_promo=?, description=?, remise=?, date_expiration=?, inscription_cours_id=?, apprenant_id=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, promotion.getCodePromo());
            ps.setString(2, promotion.getDescription());
            ps.setDouble(3, promotion.getRemise());
            ps.setDate(4, Date.valueOf(promotion.getDateExpiration()));
            ps.setInt(5, promotion.getInscriptionCoursId());
            ps.setInt(6, promotion.getApprenantId());
            ps.setInt(7, promotion.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Promotion mise à jour !");
            } else {
                System.out.println("⚠️ Aucune promotion trouvée avec l'ID " + promotion.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de la promotion : " + e.getMessage());
        }
    }

    // 🔹 DELETE
    public void delete(Promotion promotion) {
        String sql = "DELETE FROM promotion WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, promotion.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("🗑️ Promotion supprimée !");
            } else {
                System.out.println("⚠️ Aucune promotion trouvée avec l'ID " + promotion.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la promotion : " + e.getMessage());
        }
    }

    // 🔹 GET BY CODE PROMO
    public Promotion getByCode(String code) {
        String sql = "SELECT * FROM promotion WHERE code_promo=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Promotion(
                            rs.getInt("id"),
                            rs.getString("code_promo"),
                            rs.getString("description"),
                            rs.getDouble("remise"),
                            rs.getDate("date_expiration").toLocalDate(),
                            rs.getInt("inscription_cours_id"),
                            rs.getInt("apprenant_id")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération par code promo : " + e.getMessage());
        }
        return null;
    }
}