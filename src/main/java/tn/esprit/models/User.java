package tn.esprit.models;

import java.time.LocalDate;

public class User {
    protected int id;
    protected String email;
    protected String role;         // ENUM : 'ADMIN', 'APPRENANT', 'INSTRUCTEUR'
    protected String password;
    protected String nom;
    protected String prenom;
    protected LocalDate dateNaissance;
    protected String resetToken;
    protected String cv;
    protected String image;
    protected String niveauEtude;
    public User(String email, String role, String password, String nom, String prenom, LocalDate dateNaissance) {
        this.email = email;
        this.role = role;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
    }

    public User(String email, String role, String password, String nom, String prenom,
                LocalDate dateNaissance, String resetToken, String cv, String image, String niveauEtude) {
        this(email, role, password, nom, prenom, dateNaissance);
        this.resetToken = resetToken;
        this.cv = cv;
        this.image = image;
        this.niveauEtude = niveauEtude;
    }

    public User() {

    }

    // ===== Getters & Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public String getCv() { return cv; }
    public void setCv(String cv) { this.cv = cv; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }
}
