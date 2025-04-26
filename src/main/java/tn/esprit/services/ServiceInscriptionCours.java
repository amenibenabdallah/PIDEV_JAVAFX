package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.InscriptionCours;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceInscriptionCours implements IService<InscriptionCours> {

    private final Connection cnx;

    public ServiceInscriptionCours() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // 🔹 CREATE
    @Override
    public void add(InscriptionCours inscription) {
        String sql = "INSERT INTO inscription_cours (date_inscreption, type_paiement, nom_formation, cin, email, apprenant_id, formation_id, nom_apprenant, status, montant) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(inscription.getDateInscreption()));
            ps.setString(2, inscription.getTypePaiement());
            ps.setString(3, inscription.getNomFormation());
            ps.setString(4, inscription.getCin());
            ps.setString(5, inscription.getEmail());
            ps.setInt(6, inscription.getApprenantId());
            ps.setInt(7, inscription.getFormationId());
            ps.setString(8, inscription.getNomApprenant());
            ps.setString(9, inscription.getStatus());
            ps.setDouble(10, inscription.getMontant());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                inscription.setId(id);
            }
            System.out.println("✅ Inscription ajoutée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // 🔹 READ ALL
    @Override
    public List<InscriptionCours> getAll() {
        List<InscriptionCours> inscriptions = new ArrayList<>();
        String sql = "SELECT * FROM inscription_cours";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                InscriptionCours ins = new InscriptionCours();
                ins.setId(rs.getInt("id"));
                ins.setStatus(rs.getString("status"));
                ins.setDateInscreption(rs.getTimestamp("date_inscreption").toLocalDateTime());
                ins.setMontant(rs.getDouble("montant"));
                ins.setTypePaiement(rs.getString("type_paiement"));
                ins.setNomFormation(rs.getString("nom_formation"));
                ins.setCin(rs.getString("cin"));
                ins.setEmail(rs.getString("email"));
                ins.setApprenantId(rs.getInt("apprenant_id"));
                ins.setFormationId(rs.getInt("formation_id"));
                ins.setNomApprenant(rs.getString("nom_apprenant"));
                inscriptions.add(ins);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération : " + e.getMessage());
        }
        return inscriptions;
    }

    // 🔹 UPDATE
    @Override
    public void update(InscriptionCours inscription) {
        String sql = "UPDATE inscription_cours SET status=?, date_inscreption=?, montant=?, type_paiement=?, " +
                "nom_formation=?, cin=?, email=?, apprenant_id=?, formation_id=?, nom_apprenant=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, inscription.getStatus());
            ps.setTimestamp(2, Timestamp.valueOf(inscription.getDateInscreption()));
            ps.setDouble(3, inscription.getMontant());
            ps.setString(4, inscription.getTypePaiement());
            ps.setString(5, inscription.getNomFormation());
            ps.setString(6, inscription.getCin());
            ps.setString(7, inscription.getEmail());
            ps.setInt(8, inscription.getApprenantId());
            ps.setInt(9, inscription.getFormationId());
            ps.setString(10, inscription.getNomApprenant());
            ps.setInt(11, inscription.getId());
            ps.executeUpdate();
            System.out.println("✅ Inscription mise à jour !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // 🔹 DELETE
    @Override
    public void delete(InscriptionCours inscription) {
        String sql = "DELETE FROM inscription_cours WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, inscription.getId());
            ps.executeUpdate();
            System.out.println("🗑️ Inscription supprimée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }
}
