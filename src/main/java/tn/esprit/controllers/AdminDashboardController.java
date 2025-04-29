// AdminDashboardController.java
package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.UserService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private PieChart roleDistributionChart;
    @FXML private BarChart<String, Number> niveauApprenantsChart;
    @FXML private LineChart<String, Number> inscriptionEvolutionChart;
    @FXML private Label avgAgeLabel;
    @FXML private Label completionRateLabel;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRoleDistribution();
        loadNiveauApprenants();
        loadInscriptionEvolution();
        loadAvgAge();
        loadCompletionRate();
    }

    private void loadRoleDistribution() {
        int apprenants = userService.countByRole("APPRENANT");
        int instructeurs = userService.countByRole("INSTRUCTEUR");

        roleDistributionChart.getData().add(new PieChart.Data("Apprenants", apprenants));
        roleDistributionChart.getData().add(new PieChart.Data("Instructeurs", instructeurs));
    }

    private void loadNiveauApprenants() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Répartition par Niveau");

        series.getData().add(new XYChart.Data<>("Débutant", userService.countApprenantsByNiveau("Débutant")));
        series.getData().add(new XYChart.Data<>("Intermédiaire", userService.countApprenantsByNiveau("Intermédiaire")));
        series.getData().add(new XYChart.Data<>("Avancé", userService.countApprenantsByNiveau("Avancé")));

        niveauApprenantsChart.getData().add(series);
    }

    private void loadInscriptionEvolution() {
        XYChart.Series<String, Number> series = userService.getMonthlyInscriptionData();
        inscriptionEvolutionChart.getData().add(series);
    }

    private void loadAvgAge() {
        double avgAge = userService.getAverageAge();
        avgAgeLabel.setText("\uD83D\uDC65 Âge Moyen : " + String.format("%.1f", avgAge) + " ans");
    }

    private void loadCompletionRate() {
        double rate = userService.getProfileCompletionRate();
        completionRateLabel.setText("\u2705 Taux de Complétion : " + String.format("%.1f", rate) + "%");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la déconnexion !");
            alert.showAndWait();
        }
    }
}
