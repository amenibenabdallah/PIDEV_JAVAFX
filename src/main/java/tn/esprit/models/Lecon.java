package tn.esprit.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Lecon {

    private int id;
    private String titre;
    private String contenu;
    private LocalDate dateCreation;
    private Formation formation;
    private LocalDateTime updatedAt;

    public Lecon() {
        this.dateCreation = LocalDate.now();
    }

    public Lecon(int id, String titre, String contenu, LocalDate dateCreation, Formation formation, LocalDateTime updatedAt) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.formation = formation;

        this.updatedAt = updatedAt;
    }

    public Lecon(String titre, String contenu, LocalDate dateCreation) {
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }



    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}