package tn.esprit.models;

import java.time.LocalDate;

public class apprenants extends users {
    private String niveauEtude;
    private String image;
    public apprenants() {
        super();
    }

    public apprenants(String email, String roles, String password, String nom, String prenom,
                      LocalDate dateNaissance, String resetToken, String userType,
                      String niveauEtude, String image) {
        super(email, roles, password, nom, prenom, dateNaissance, resetToken, userType);
        this.niveauEtude = niveauEtude;
        this.image = image;
    }
    public String getNiveauEtude() {
        return niveauEtude;
    }

    public void setNiveauEtude(String niveauEtude) {
        this.niveauEtude = niveauEtude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    @Override
    public String toString() {
        return "Apprenant : " + nom + " " + prenom + " | Email : " + email + " | Niveau : " + niveauEtude;
    }
}