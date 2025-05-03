package tn.esprit.services;

import tn.esprit.models.Promotion;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicePromotion {

    private final Connection cnx;

    public ServicePromotion() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Promotion promotion) {
        if (promotion.getDateExpiration().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("❌ La date d'expiration de la promotion est passée.");
        }

        String sql = "INSERT INTO promotion (code_promo, description, remise, date_expiration, inscription_cours_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, promotion.getCodePromo());
            ps.setString(2, promotion.getDescription());
            ps.setDouble(3, promotion.getRemise());
            ps.setDate(4, Date.valueOf(promotion.getDateExpiration()));
            ps.setInt(5, promotion.getInscriptionCoursId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    promotion.setId(rs.getInt(1));
                }
            }
            System.out.println("✅ Promotion ajoutée !");
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de l'ajout de la promotion : " + e.getMessage(), e);
        }
    }

    public List<Promotion> getAll() {
        List<Promotion> promotions = new ArrayList<>();
        String sql = "SELECT * FROM promotion";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Promotion p = new Promotion();
                p.setId(rs.getInt("id"));
                p.setCodePromo(rs.getString("code_promo"));
                p.setDescription(rs.getString("description"));
                p.setRemise(rs.getDouble("remise"));
                p.setDateExpiration(rs.getDate("date_expiration").toLocalDate());
                p.setInscriptionCoursId(rs.getInt("inscription_cours_id"));
                promotions.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des promotions : " + e.getMessage());
            e.printStackTrace();
        }
        return promotions;
    }

    public List<Promotion> getPage(int page, int pageSize, int lastId) throws SQLException {
        List<Promotion> promotions = new ArrayList<>();
        String sql = "SELECT * FROM promotion WHERE id > ? ORDER BY id ASC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, lastId);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Promotion promo = mapResultSetToPromotion(rs);
                    promotions.add(promo);
                    System.out.println("Promotion récupérée : " + promo);
                }
            }
            System.out.println("Nombre de promotions récupérées pour la page : " + promotions.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la page : " + e.getMessage());
            throw e;
        }
        return promotions;
    }

    public long getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM promotion";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                long count = rs.getLong(1);
                System.out.println("Nombre total de promotions (getTotalCount) : " + count);
                return count;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage total : " + e.getMessage());
            throw e;
        }
        return 0;
    }

    public long getActiveCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM promotion WHERE date_expiration > ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long count = rs.getLong(1);
                    System.out.println("Nombre de promotions actives (getActiveCount) : " + count);
                    return count;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des actives : " + e.getMessage());
            throw e;
        }
        return 0;
    }

    public void update(Promotion promotion) {
        String sql = "UPDATE promotion SET code_promo=?, description=?, remise=?, date_expiration=?, inscription_cours_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, promotion.getCodePromo());
            ps.setString(2, promotion.getDescription());
            ps.setDouble(3, promotion.getRemise());
            ps.setDate(4, Date.valueOf(promotion.getDateExpiration()));
            ps.setInt(5, promotion.getInscriptionCoursId());
            ps.setInt(6, promotion.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de la promotion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Promotion promotion) {
        String sql = "DELETE FROM promotion WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, promotion.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("🗑️ Promotion supprimée !");
            } else {
                System.out.println("⚠️ Aucune promotion trouvée avec l'ID " + promotion.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la suppression de la promotion : " + e.getMessage(), e);
        }
    }

    public Promotion getByCode(String code) {
        String sql = "SELECT * FROM promotion WHERE code_promo=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPromotion(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors de la récupération par code promo : " + e.getMessage(), e);
        }
        return null;
    }

    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        return new Promotion(
                rs.getInt("id"),
                rs.getString("code_promo"),
                rs.getString("description"),
                rs.getDouble("remise"),
                rs.getDate("date_expiration").toLocalDate(),
                rs.getInt("inscription_cours_id")
        );
    }
}