package tn.esprit.models;

import java.time.LocalDateTime;

public class InscriptionCours {
    private int id;
    private String status = "en attente";      // Valeur par défaut
    private LocalDateTime dateInscreption;
    private double montant = 0.0;              // Valeur par défaut
    private String typePaiement;
    private String nomFormation;
    private String cin;
    private String email;
    private int apprenantId;
    private int formationId;
    private String nomApprenant;

    public InscriptionCours() {
        // les valeurs par défaut sont déjà initialisées
    }

    // Constructeur utilisé pour insertion
    public InscriptionCours(LocalDateTime dateInscreption, String typePaiement, String nomFormation,
                            String cin, String email, int apprenantId, int formationId, String nomApprenant) {
        this.dateInscreption = dateInscreption;
        this.typePaiement = typePaiement;
        this.nomFormation = nomFormation;
        this.cin = cin;
        this.email = email;
        this.apprenantId = apprenantId;
        this.formationId = formationId;
        this.nomApprenant = nomApprenant;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDateInscreption() { return dateInscreption; }
    public void setDateInscreption(LocalDateTime dateInscreption) { this.dateInscreption = dateInscreption; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getTypePaiement() { return typePaiement; }
    public void setTypePaiement(String typePaiement) { this.typePaiement = typePaiement; }

    public String getNomFormation() { return nomFormation; }
    public void setNomFormation(String nomFormation) { this.nomFormation = nomFormation; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getApprenantId() { return apprenantId; }
    public void setApprenantId(int apprenantId) { this.apprenantId = apprenantId; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public String getNomApprenant() { return nomApprenant; }
    public void setNomApprenant(String nomApprenant) { this.nomApprenant = nomApprenant; }

    @Override
    public String toString() {
        return "InscriptionCours{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", dateInscreption=" + dateInscreption +
                ", montant=" + montant +
                ", typePaiement='" + typePaiement + '\'' +
                ", nomFormation='" + nomFormation + '\'' +
                ", cin='" + cin + '\'' +
                ", email='" + email + '\'' +
                ", apprenantId=" + apprenantId +
                ", formationId=" + formationId +
                ", nomApprenant='" + nomApprenant + '\'' +
                '}';
    }
}
