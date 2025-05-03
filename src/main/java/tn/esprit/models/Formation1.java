package tn.esprit.models;

import java.time.LocalDate;

public class Formation1 {
    private int id;
    private Integer categorieId; // Peut être null
    private String titre;
    private String description;
    private String duree;
    private String niveau;
    private LocalDate dateCreation;
    private double prix;

    // Constructeurs
    public Formation1() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getCategorieId() { return categorieId; }
    public void setCategorieId(Integer categorieId) { this.categorieId = categorieId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
}