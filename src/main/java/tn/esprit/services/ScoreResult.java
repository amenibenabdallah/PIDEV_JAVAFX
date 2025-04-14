package tn.esprit.services;

public class ScoreResult {
    private double averageScore;
    private int count;

    public ScoreResult(double averageScore, int count) {
        this.averageScore = averageScore;
        this.count = count;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public int getCount() {
        return count;
    }
}
