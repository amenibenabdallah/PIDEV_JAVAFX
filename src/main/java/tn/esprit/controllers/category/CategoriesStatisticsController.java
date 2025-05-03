package tn.esprit.controllers.category;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.esprit.models.Categorie;
import tn.esprit.services.CategorieService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CategoriesStatisticsController implements Initializable {

    @FXML private PieChart categoriePieChart;
    @FXML private BarChart<String, Number> categorieBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label totalCategoriesLabel;

    private final CategorieService categorieService = new CategorieService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCharts();
        loadStatistics();
    }

    private void setupCharts() {
        // Configure PieChart
        categoriePieChart.setLegendVisible(true);
        categoriePieChart.setLabelsVisible(true);

        // Configure BarChart
        xAxis.setLabel("Categories");
        yAxis.setLabel("Nombre des Formations");
        categorieBarChart.setLegendVisible(false);
    }

    private void loadStatistics() {
        List<Categorie> categories = categorieService.getUsedCategoriesWithFormationCount();

        if (categories != null && !categories.isEmpty()) {
            totalCategoriesLabel.setText("Total Categories: " + categories.size());
            loadCategoriesIntoPieChart(categories);
            loadCategoriesIntoBarChart(categories);
        } else {
            totalCategoriesLabel.setText("No Categories Available");
        }
    }

    private void loadCategoriesIntoPieChart(List<Categorie> categories) {
        categoriePieChart.getData().clear();
        categories.forEach(categorie -> {
            PieChart.Data slice = new PieChart.Data(
                    categorie.getNom() + " (" + categorie.getFormations().size() + ")",
                    categorie.getFormations().size()
            );
            categoriePieChart.getData().add(slice);
        });
    }

    private void loadCategoriesIntoBarChart(List<Categorie> categories) {
        categorieBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        categories.forEach(categorie ->
                series.getData().add(new XYChart.Data<>(categorie.getNom(), categorie.getFormations().size()))
        );

        categorieBarChart.getData().add(series);
    }

    public void refreshStatistics() {
        loadStatistics();
    }
}