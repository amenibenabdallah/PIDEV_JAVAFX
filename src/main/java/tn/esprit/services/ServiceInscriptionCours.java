package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.InscriptionCours;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceInscriptionCours implements IService<InscriptionCours> {
    private Connection cnx;

    public ServiceInscriptionCours() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(InscriptionCours inscription) {
        String qry = "INSERT INTO inscription_cours (date_inscreption, type_paiement, nom_formation, cin, email, apprenant_id, formation_id, nom_apprenant, status, montant) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setObject(1, inscription.getDateInscreption());
            pstm.setString(2, inscription.getTypePaiement());
            pstm.setString(3, inscription.getNomFormation());
            pstm.setString(4, inscription.getCin());
            pstm.setString(5, inscription.getEmail());
            pstm.setInt(6, inscription.getApprenantId());
            pstm.setInt(7, inscription.getFormationId());
            pstm.setString(8, inscription.getNomApprenant());
            pstm.setString(9, inscription.getStatus());
            pstm.setDouble(10, inscription.getMontant());
            pstm.executeUpdate();
            System.out.println("✅ Inscription ajoutée avec valeurs par défaut !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<InscriptionCours> getAll() {
        List<InscriptionCours> inscriptions = new ArrayList<>();
        String qry = "SELECT * FROM inscription_cours";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
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
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
        }
        return inscriptions;
    }

    @Override
    public void update(InscriptionCours inscription) {
        String qry = "UPDATE inscription_cours SET status=?, date_inscreption=?, montant=?, type_paiement=?, nom_formation=?, cin=?, email=?, apprenant_id=?, formation_id=?, nom_apprenant=? WHERE id=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, inscription.getStatus());
            pstm.setTimestamp(2, Timestamp.valueOf(inscription.getDateInscreption()));
            pstm.setDouble(3, inscription.getMontant());
            pstm.setString(4, inscription.getTypePaiement());
            pstm.setString(5, inscription.getNomFormation());
            pstm.setString(6, inscription.getCin());
            pstm.setString(7, inscription.getEmail());
            pstm.setInt(8, inscription.getApprenantId());
            pstm.setInt(9, inscription.getFormationId());
            pstm.setString(10, inscription.getNomApprenant());
            pstm.setInt(11, inscription.getId());
            pstm.executeUpdate();
            System.out.println("✅ Inscription mise à jour !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void delete(InscriptionCours inscription) {
        String qry = "DELETE FROM inscription_cours WHERE id=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, inscription.getId());
            pstm.executeUpdate();
            System.out.println("✅ Inscription supprimée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }
}