package tn.esprit.models;

import java.util.List;

public class GroupDiscussion {
    private int id;
    private String name;
    private List<users> participants;

    // Constructeur par défaut
    public GroupDiscussion() {
    }

    // Constructeur avec paramètres
    public GroupDiscussion(int id, String name, List<users> participants) {
        this.id = id;
        this.name = name;
        this.participants = participants;
    }

    // Getter et Setter pour id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter et Setter pour name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter et Setter pour participants
    public List<users> getParticipants() {
        return participants;
    }

    public void setParticipants(List<users> participants) {
        this.participants = participants;
    }

    // Pour affichage dans ListView (facultatif)
    @Override
    public String toString() {
        return name;
    }
}

