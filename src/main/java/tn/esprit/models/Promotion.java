package tn.esprit.models;

import java.time.LocalDate;

public class Promotion {
    private int id;
    private String codePromo;
    private String description;
    private double remise;
    private LocalDate dateExpiration;
    private int inscriptionCoursId;  // 🔸 Ajouté
    private int apprenantId;         // 🔸 Ajouté

    public Promotion() {}

    public Promotion(int id, String codePromo, String description, double remise, LocalDate dateExpiration, int inscriptionCoursId, int apprenantId) {
        this.id = id;
        this.codePromo = codePromo;
        this.description = description;
        this.remise = remise;
        this.dateExpiration = dateExpiration;
        this.inscriptionCoursId = inscriptionCoursId;
        this.apprenantId = apprenantId;
    }

    public Promotion(String codePromo, String description, double remise, LocalDate dateExpiration, int inscriptionCoursId, int apprenantId) {
        this.codePromo = codePromo;
        this.description = description;
        this.remise = remise;
        this.dateExpiration = dateExpiration;
        this.inscriptionCoursId = inscriptionCoursId;
        this.apprenantId = apprenantId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodePromo() { return codePromo; }
    public void setCodePromo(String codePromo) { this.codePromo = codePromo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public int getInscriptionCoursId() { return inscriptionCoursId; }
    public void setInscriptionCoursId(int inscriptionCoursId) { this.inscriptionCoursId = inscriptionCoursId; }

    public int getApprenantId() { return apprenantId; }
    public void setApprenantId(int apprenantId) { this.apprenantId = apprenantId; }

    @Override
    public String toString() {
        return "Promotion{" +
                "id=" + id +
                ", codePromo='" + codePromo + '\'' +
                ", description='" + description + '\'' +
                ", remise=" + remise +
                ", dateExpiration=" + dateExpiration +
                ", inscriptionCoursId=" + inscriptionCoursId +
                ", apprenantId=" + apprenantId +
                '}';
    }
}
