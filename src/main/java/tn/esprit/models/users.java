package tn.esprit.models;

import java.time.LocalDate;

public class users {
    protected int id;
    protected String email;
    protected String roles;
    protected String password;
    protected String nom;
    protected String prenom;
    protected LocalDate dateNaissance;
    protected String resetToken;
    protected String userType;


    public users() {}

    public users(String email, String roles, String password, String nom, String prenom,
                 LocalDate dateNaissance, String resetToken, String userType) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.resetToken = resetToken;
        this.userType = userType;
    }
    public users(String email, String roles, String password, String nom, String prenom,
                 LocalDate dateNaissance, String resetToken, String userType, String image) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.resetToken = resetToken;
        this.userType = userType;

    }
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

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

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }


}