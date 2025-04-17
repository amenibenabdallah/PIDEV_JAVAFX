package tn.esprit.controllers.lecon;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.models.Formation;
import tn.esprit.models.Lecon;
import tn.esprit.services.LeconService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeconByFormationController {

    @FXML private Label formationTitleLabel;
    @FXML private TabPane tabPaneLecons;

    private Formation formation;
    private final LeconService leconService = new LeconService();

    public void setFormation(Formation formation) {
        this.formation = formation;
        formationTitleLabel.setText("Leçons de la formation \"" + formation.getTitre() + "\"");
        loadLecons();
    }

    private void loadLecons() {
        tabPaneLecons.getTabs().clear();
        try {
            List<Lecon> lecons = leconService.getByFormation(formation.getId());
            for (Lecon lecon : lecons) {
                Tab tab = new Tab(lecon.getTitre());

                VBox contentBox = new VBox(10);
                contentBox.setStyle("-fx-padding: 15");

                Label dateLabel = new Label("Créé le " + lecon.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                Button viewButton = new Button("Voir les détails");
                viewButton.setOnAction(e -> showLeconDetails(lecon));

                contentBox.getChildren().addAll(dateLabel, viewButton);
                tab.setContent(contentBox);

                tabPaneLecons.getTabs().add(tab);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showLeconDetails(Lecon lecon) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lecon.getTitre());
        alert.setHeaderText("Contenu de la leçon");
        alert.setContentText(lecon.getContenu());
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/GetAllFormationFront.fxml"));
            Parent root = loader.load();
            tabPaneLecons.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
