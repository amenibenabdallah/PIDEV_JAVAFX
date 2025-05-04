package tn.esprit.models;

import java.time.LocalDate;

/**
 * Represents an evaluation for an instructor, with a one-to-one relationship to a User.
 * Fields are populated from CV data via an API and scored using an ML model API.
 */
public class Evaluation {
    private int id;
    private int userId; // Updated from instructorId to align with schema change
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
    private User instructeur;

    /**
     * Default constructor initializing default values.
     */
    public Evaluation() {
        this.dateCreation = LocalDate.now();
        this.status = 0;
        setDefaultWeights();
    }

    /**
     * Parameterized constructor with full evaluation details.
     * @param id Unique identifier for the evaluation
     * @param userId ID of the associated user (instructor)
     * @param score Evaluation score (0-100)
     * @param niveau Skill level (e.g., "EXCELLENT", "AVERAGE")
     * @param status Status flag (e.g., 1 for accepted, 0 for not accepted)
     * @param dateCreation Creation date of the evaluation
     * @param education Educational background
     * @param yearsOfExperience Years of professional experience
     * @param skills List of skills
     * @param certifications List of certifications
     * @param instructeur Associated instructor (User object)
     */
    public Evaluation(int id, int userId, double score, String niveau, int status,
                      LocalDate dateCreation, String education, int yearsOfExperience,
                      String skills, String certifications, User instructeur) {
        this.id = id;
        this.userId = userId;
        setScore(score);
        this.niveau = niveau;
        this.status = status;
        this.dateCreation = (dateCreation != null) ? dateCreation : LocalDate.now();
        this.education = education;
        setYearsOfExperience(yearsOfExperience);
        this.skills = skills;
        this.certifications = certifications;
        setDefaultWeights();
        this.instructeur = instructeur;
        if (instructeur != null) {
            this.userId = instructeur.getId();
        }
    }

    /**
     * Sets default weights for evaluation criteria (25% each).
     */
    private void setDefaultWeights() {
        this.educationWeight = 0.25;
        this.experienceWeight = 0.25;
        this.skillsWeight = 0.25;
        this.certificationsWeight = 0.25;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getScore() { return score; }
    public void setScore(double score) {
        if (score >= 0 && score <= 100) this.score = score;
        else throw new IllegalArgumentException("Score must be between 0 and 100");
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
        else throw new IllegalArgumentException("Years of experience cannot be negative");
    }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public double getEducationWeight() { return educationWeight; }
    public void setEducationWeight(double weight) { setWeight(weight, "education"); }

    public double getExperienceWeight() { return experienceWeight; }
    public void setExperienceWeight(double weight) { setWeight(weight, "experience"); }

    public double getSkillsWeight() { return skillsWeight; }
    public void setSkillsWeight(double weight) { setWeight(weight, "skills"); }

    public double getCertificationsWeight() { return certificationsWeight; }
    public void setCertificationsWeight(double weight) { setWeight(weight, "certifications"); }

    public User getInstructeur() { return instructeur; }
    public void setInstructeur(User instructeur) {
        this.instructeur = instructeur;
        this.userId = (instructeur != null) ? instructeur.getId() : 0;
    }

    /**
     * Validates and sets a weight value for a specific criterion.
     * @param weight The weight value to set (0 to 1)
     * @param criterion The criterion being weighted (e.g., "education", "experience")
     * @throws IllegalArgumentException if weight is out of range
     */
    private void setWeight(double weight, String criterion) {
        if (weight >= 0 && weight <= 1) {
            switch (criterion.toLowerCase()) {
                case "education" -> this.educationWeight = weight;
                case "experience" -> this.experienceWeight = weight;
                case "skills" -> this.skillsWeight = weight;
                case "certifications" -> this.certificationsWeight = weight;
                default -> throw new IllegalArgumentException("Invalid criterion: " + criterion);
            }
            normalizeWeights();
        } else {
            throw new IllegalArgumentException("Weight must be between 0 and 1 for " + criterion);
        }
    }

    /**
     * Normalizes weights to ensure they sum to 1 after individual updates.
     */
    private void normalizeWeights() {
        double total = educationWeight + experienceWeight + skillsWeight + certificationsWeight;
        if (total > 0) {
            educationWeight /= total;
            experienceWeight /= total;
            skillsWeight /= total;
            certificationsWeight /= total;
        }
    }
}