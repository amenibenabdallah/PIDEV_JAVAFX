package tn.esprit.models;

import java.time.LocalDate;

public class instructeurs extends users {
    private String specialite;
    private String cv; // chemin vers un fichier PDF
    private String image;
    public instructeurs() {
        super();
    }

    public instructeurs(String email, String roles, String password, String nom, String prenom,
                        LocalDate dateNaissance, String resetToken, String userType,
                        String image, String cv) {

        super(email, roles, password, nom, prenom, dateNaissance, resetToken, userType, image);

        this.specialite = specialite;
        this.cv = cv;
        this.image = image ;
    }
    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getCv() {
        return cv;
    }

    public void setImage(String image) {
        this.image =image;
    }
    public String getImage() {
        return image;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }
}
