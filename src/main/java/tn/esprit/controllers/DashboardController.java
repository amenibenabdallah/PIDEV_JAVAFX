package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalFormationsLabel;

    @FXML
    private Label totalEventsLabel;

    @FXML
    private Label totalPromotionsLabel;

    @FXML
    private PieChart formationsPieChart;

    @FXML
    private BarChart<String, Number> eventsPromotionsBarChart;

    @FXML
    private LineChart<String, Number> userRegistrationsLineChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate summary cards with dummy data
        totalUsersLabel.setText("Total Users: 150");
        totalFormationsLabel.setText("Total Formations: 25");
        totalEventsLabel.setText("Total Events: 10");
        totalPromotionsLabel.setText("Total Promotions: 5");

        // Populate PieChart: Formations by Category
        formationsPieChart.getData().addAll(
                new PieChart.Data("Technology", 10),
                new PieChart.Data("Business", 8),
                new PieChart.Data("Art", 7)
        );

        // Populate BarChart: Events and Promotions by Month
        XYChart.Series<String, Number> eventsSeries = new XYChart.Series<>();
        eventsSeries.setName("Events");
        eventsSeries.getData().addAll(
                new XYChart.Data<>("Jan", 3),
                new XYChart.Data<>("Feb", 2),
                new XYChart.Data<>("Mar", 5)
        );

        XYChart.Series<String, Number> promotionsSeries = new XYChart.Series<>();
        promotionsSeries.setName("Promotions");
        promotionsSeries.getData().addAll(
                new XYChart.Data<>("Jan", 1),
                new XYChart.Data<>("Feb", 2),
                new XYChart.Data<>("Mar", 2)
        );

        eventsPromotionsBarChart.getData().addAll(eventsSeries, promotionsSeries);

        // Populate LineChart: User Registrations Over Time
        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Users");
        userSeries.getData().addAll(
                new XYChart.Data<>("Jan", 20),
                new XYChart.Data<>("Feb", 30),
                new XYChart.Data<>("Mar", 50),
                new XYChart.Data<>("Apr", 45)
        );

        userRegistrationsLineChart.getData().add(userSeries);
    }
}