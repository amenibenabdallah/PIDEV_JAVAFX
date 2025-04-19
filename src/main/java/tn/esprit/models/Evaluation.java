package tn.esprit.models;

import java.time.LocalDate;

/**
 * Represents an evaluation for an instructor, with a one-to-one relationship to Instructeur.
 * Fields are populated from CV via API and score via ML model API.
 */
public class Evaluation {
    private int id;
    private int instructorId;
    private double score;
    private String niveau;
    private int status;
    private LocalDate dateCreation;
    private String education;
    private int yearsOfExperience;
    private String skills;
    private String certifications;
    private double educationWeight;
    private double experienceWeight;
    private double skillsWeight;
    private double certificationsWeight;
    private instructeurs instructeur;

    public Evaluation() {
        this.dateCreation = LocalDate.now();
        this.status = 0;
        this.educationWeight = 0.25;
        this.experienceWeight = 0.25;
        this.skillsWeight = 0.25;
        this.certificationsWeight = 0.25;
    }

    public Evaluation(int id, int instructorId, double score, String niveau, int status,
                      LocalDate dateCreation, String education, int yearsOfExperience,
                      String skills, String certifications, instructeurs instructeur) {
        this.id = id;
        this.instructorId = instructorId;
        this.score = score;
        this.niveau = niveau;
        this.status = status;
        this.dateCreation = dateCreation != null ? dateCreation : LocalDate.now();
        this.education = education;
        this.yearsOfExperience = yearsOfExperience;
        this.skills = skills;
        this.certifications = certifications;
        this.educationWeight = 0.25;
        this.experienceWeight = 0.25;
        this.skillsWeight = 0.25;
        this.certificationsWeight = 0.25;
        this.instructeur = instructeur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public double getScore() { return score; }
    public void setScore(double score) {
        if (score >= 0 && score <= 100) this.score = score;
    }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int years) {
        if (years >= 0) this.yearsOfExperience = years;
    }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public double getEducationWeight() { return educationWeight; }
    public void setEducationWeight(double weight) {
        if (weight >= 0 && weight <= 1) this.educationWeight = weight;
    }

    public double getExperienceWeight() { return experienceWeight; }
    public void setExperienceWeight(double weight) {
        if (weight >= 0 && weight <= 1) this.experienceWeight = weight; }

    public double getSkillsWeight() { return skillsWeight; }
    public void setSkillsWeight(double weight) {
        if (weight >= 0 && weight <= 1) this.skillsWeight = weight; }

    public double getCertificationsWeight() { return certificationsWeight; }
    public void setCertificationsWeight(double weight) {
        if (weight >= 0 && weight <= 1) this.certificationsWeight = weight; }

    public instructeurs getInstructeur() { return instructeur; }
    public void setInstructeur(instructeurs instructeur) {
        this.instructeur = instructeur;
        this.instructorId = (instructeur != null) ? instructeur.getId() : 0;
    }
}
