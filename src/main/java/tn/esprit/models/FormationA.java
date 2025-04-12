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

    public FormationA(int id, String name, double averageScore, int avisCount, List<Avis> avisList) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.averageScore = new SimpleDoubleProperty(averageScore);
        this.avisCount = new SimpleIntegerProperty(avisCount);
        this.avisList = avisList != null ? avisList : new ArrayList<>();
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
}
