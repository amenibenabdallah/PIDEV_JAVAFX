package tn.esprit.models;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Formation {

    private int id;
    private String imageName;
    private String titre;
    private String description;
    private String duree;
    private String niveau;
    private LocalDate dateCreation;
    private float prix;
    private Categorie categorie;
    private List<Lecon> lecons;
    private LocalDateTime updatedAt;

    public Formation() {
        this.lecons = new ArrayList<>();
        this.dateCreation = LocalDate.now();
    }

    public Formation(int id, String titre, String description, String duree, String niveau, LocalDate dateCreation, float prix, String imageName) {
        this();
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.duree = duree;
        this.niveau = niveau;
        this.dateCreation = dateCreation;
        this.prix = prix;
        this.imageName = imageName;
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

    @Override
    public String toString() {
        return  titre
                ;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<Lecon> getLecons() {
        return lecons;
    }

    public void addLecon(Lecon lecon) {
        if (!lecons.contains(lecon)) {
            lecons.add(lecon);
            lecon.setFormation(this);
        }
    }

    public void removeLecon(Lecon lecon) {
        if (lecons.remove(lecon)) {
            if (lecon.getFormation() == this) {
                lecon.setFormation(null);
            }
        }
    }
}