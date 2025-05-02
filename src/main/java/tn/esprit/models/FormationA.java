package tn.esprit.models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class FormationA {
    private final int id;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty averageScore;
    private final SimpleIntegerProperty avisCount;
    private final List<Avis> avisList;
    private final List<instructeurs> instructors;
    private final SimpleStringProperty niveau;
    private final SimpleStringProperty description;
    private final SimpleStringProperty duree;
    private final SimpleIntegerProperty categorieId;
    private final SimpleStringProperty categoryName; // Added field

    public FormationA(int id, String name, double averageScore, int avisCount, List<Avis> avisList, List<instructeurs> instructors, String niveau, String description, String duree, int categorieId, String categoryName) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.averageScore = new SimpleDoubleProperty(averageScore);
        this.avisCount = new SimpleIntegerProperty(avisCount);
        this.avisList = avisList != null ? avisList : new ArrayList<>();
        this.instructors = instructors != null ? instructors : new ArrayList<>();
        this.niveau = new SimpleStringProperty(niveau);
        this.description = new SimpleStringProperty(description);
        this.duree = new SimpleStringProperty(duree);
        this.categorieId = new SimpleIntegerProperty(categorieId);
        this.categoryName = new SimpleStringProperty(categoryName);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public double getAverageScore() {
        return averageScore.get();
    }

    public SimpleDoubleProperty averageScoreProperty() {
        return averageScore;
    }

    public int getAvisCount() {
        return avisCount.get();
    }

    public SimpleIntegerProperty avisCountProperty() {
        return avisCount;
    }

    public List<Avis> getAvisList() {
        return avisList;
    }

    public List<instructeurs> getInstructors() {
        return instructors;
    }

    public String getNiveau() {
        return niveau.get();
    }

    public SimpleStringProperty niveauProperty() {
        return niveau;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public String getDuree() {
        return duree.get();
    }

    public SimpleStringProperty dureeProperty() {
        return duree;
    }

    public int getCategorieId() {
        return categorieId.get();
    }

    public SimpleIntegerProperty categorieIdProperty() {
        return categorieId;
    }

    public String getCategoryName() {
        return categoryName.get();
    }

    public SimpleStringProperty categoryNameProperty() {
        return categoryName;
    }
}