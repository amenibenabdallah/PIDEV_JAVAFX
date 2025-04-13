package tn.esprit.models;

public class FormationScore {
    private int id;
    private int formationId;
    private double noteMoyenne;
    private int nombreAvis;
    private int classement;

    public FormationScore() {}

    public FormationScore(int formationId, double noteMoyenne,int nombreAvis) {
        this.formationId = formationId;
        this.noteMoyenne = noteMoyenne;
        this.nombreAvis=nombreAvis;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public int getNombreAvis() {
        return nombreAvis;
    }

    public void setNombreAvis(int nombreAvis) {
        this.nombreAvis = nombreAvis;
    }

    @Override
    public String toString() {
        return "FormationScore{" +
                "id=" + id +
                ", formationId=" + formationId +
                ", averageScore=" + noteMoyenne +
                ", NombreAvis=" + nombreAvis +
                '}';
    }
}

