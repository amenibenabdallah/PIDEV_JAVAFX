package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InscriptionCours {
    private int id;
    private String status = "en attente";
    private LocalDateTime dateInscreption;
    private double montant = 0.0;
    private String typePaiement;
    private String nomFormation;
    private String cin;
    private String email;
    private int apprenantId;
    private int formationId;
    private String nomApprenant;
    private List<Promotion> promotions = new ArrayList<>();

    // Constructeurs
    public InscriptionCours() {}

    public InscriptionCours(LocalDateTime dateInscreption, String typePaiement,
                            String nomFormation, String cin, String email,
                            int apprenantId, int formationId, String nomApprenant) {
        this.dateInscreption = dateInscreption;
        this.typePaiement = typePaiement;
        this.nomFormation = nomFormation;
        this.cin = cin;
        this.email = email;
        this.apprenantId = apprenantId;
        this.formationId = formationId;
        this.nomApprenant = nomApprenant;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDateInscreption() { return dateInscreption; }
    public void setDateInscreption(LocalDateTime dateInscreption) {
        this.dateInscreption = dateInscreption;
    }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getTypePaiement() { return typePaiement; }
    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public String getNomFormation() { return nomFormation; }
    public void setNomFormation(String nomFormation) {
        this.nomFormation = nomFormation;
    }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getApprenantId() { return apprenantId; }
    public void setApprenantId(int apprenantId) {
        this.apprenantId = apprenantId;
    }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) {
        this.formationId = formationId;
    }

    public String getNomApprenant() { return nomApprenant; }
    public void setNomApprenant(String nomApprenant) {
        this.nomApprenant = nomApprenant;
    }

    public List<Promotion> getPromotions() { return promotions; }
    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    @Override
    public String toString() {
        return "InscriptionCours{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", nomFormation='" + nomFormation + '\'' +
                ", promotions=" + promotions.size() +
                '}';
    }
}