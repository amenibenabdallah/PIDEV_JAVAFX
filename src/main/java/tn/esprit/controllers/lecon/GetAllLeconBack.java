package tn.esprit.controllers.lecon;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tn.esprit.models.Formation;
import tn.esprit.models.Lecon;
import tn.esprit.services.FormationService;
import tn.esprit.services.LeconService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class GetAllLeconBack implements Initializable {

    @FXML
    private ComboBox<Formation> formationComboBox;
    @FXML
    private Button ajouterLeconBtn;
    @FXML
    private FlowPane leconContainer;

    private final LeconService leconService = new LeconService();
    private final FormationService formationService = new FormationService();

    private Formation currentFormation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        ajouterLeconBtn.setOnAction(e -> handleAddLecon());
    }

    private void setupComboBox() {
        try {
            List<Formation> formations = formationService.getAll();
            formationComboBox.setItems(FXCollections.observableArrayList(formations));
            formationComboBox.setOnAction(e -> {
                currentFormation = formationComboBox.getSelectionModel().getSelectedItem();
                if (currentFormation != null) {
                    loadLeconsForFormation(currentFormation.getId());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLeconsForFormation(int formationId) {
        try {
            List<Lecon> lecons = leconService.getByFormation(formationId);

            leconContainer.getChildren().clear(); // Clear previous cards

            for (Lecon lecon : lecons) {
                VBox leconCard = createLeconCard(lecon);
                leconContainer.getChildren().add(leconCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createLeconCard(Lecon lecon) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #cccccc; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label titreLabel = new Label(lecon.getTitre());
        titreLabel.setFont(new Font("Arial Bold", 16));
        titreLabel.setWrapText(true);

        Label contenuLabel = new Label(lecon.getContenu());
        contenuLabel.setWrapText(true);
        contenuLabel.setMaxHeight(80); // Limit content height

        Label dateLabel = new Label("Créé le: " + lecon.getDateCreation().toString());
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");

        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        editButton.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white;");
        deleteButton.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, editButton, deleteButton);

        editButton.setOnAction(e -> handleEditLecon(lecon));
        deleteButton.setOnAction(e -> {
            try {
                leconService.delete(lecon.getId());
                loadLeconsForFormation(currentFormation.getId()); // reload after deletion
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(titreLabel, contenuLabel, dateLabel, buttonBox);

        return card;
    }

    private void handleAddLecon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Lecon/AddLecon.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Leçon");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (currentFormation != null) {
                loadLeconsForFormation(currentFormation.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEditLecon(Lecon lecon) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lecon/EditLecon.fxml"));
            Parent root = loader.load();

            tn.esprit.controllers.lecon.EditLeconController controller = loader.getController();
            controller.setLecon(lecon);

            Stage stage = new Stage();
            stage.setTitle("Modifier Leçon");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (currentFormation != null) {
                loadLeconsForFormation(currentFormation.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
