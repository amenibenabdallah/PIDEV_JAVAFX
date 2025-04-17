package tn.esprit.models;



import java.util.ArrayList;
import java.util.List;

public class Categorie {
    @Override
    public String toString() {
        return

                " Categorie:=" + nom

                ;
    }

    private int id;
    private String nom;
    private String description;
    private List<Formation> formations;

    public Categorie() {
        this.formations = new ArrayList<>();
    }

    public Categorie(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.formations = new ArrayList<>();
    }

    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
        this.formations = new ArrayList<>();
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Formation> getFormations() {
        return formations;
    }

    public void setFormations(List<Formation> formations) {
        this.formations = formations;
    }

    public void addFormation(Formation formation) {
        if (!this.formations.contains(formation)) {
            this.formations.add(formation);
            formation.setCategorie(this); // Maintain bidirectional relationship
        }
    }

    public void removeFormation(Formation formation) {
        if (this.formations.remove(formation)) {
            if (formation.getCategorie() == this) {
                formation.setCategorie(null); // Break bidirectional link
            }
        }
    }
}
