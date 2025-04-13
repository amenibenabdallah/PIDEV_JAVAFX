package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Avis {
    private int id;




    private int formationId;
    private int formationScoreId; // Fixed typo
    private int instructeurId;
    private int apprenantId;
    private boolean isFlagged;
    private String[] flaggedReason = new String[10]; // Array for flagged reasons

    private float note;
    private String commentaire;
    private LocalDateTime dateCreation;

    public Avis() {}

    public Avis(float note, String commentaire, LocalDateTime dateCreation, int formationId) {
        this.note = note;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
        this.formationId = formationId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public int getFormationScoreId() { return formationScoreId; }
    public void setFormationScoreId(int formationScoreId) { this.formationScoreId = formationScoreId; }

    public int getInstructeurId() { return instructeurId; }
    public void setInstructeurId(int instructeurId) { this.instructeurId = instructeurId; }

    public int getApprenantId() { return apprenantId; }
    public void setApprenantId(int apprenantId) { this.apprenantId = apprenantId; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public String[] getFlaggedReason() { return flaggedReason; }
    public void setFlaggedReason(String[] flaggedReason) { this.flaggedReason = flaggedReason; }

    public float getNote() { return note; }
    public void setNote(float note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", formationId=" + formationId +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}