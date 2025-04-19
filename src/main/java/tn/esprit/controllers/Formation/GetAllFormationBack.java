package tn.esprit.controllers.Formation;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.services.FormationService;
import tn.esprit.models.Formation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GetAllFormationBack implements Initializable {


    @FXML
    private TableView<Formation> formationTableView;

    @FXML
    private TableColumn<Formation, String> imageColumn;

    @FXML
    private TableColumn<Formation, String> titreColumn;

    @FXML
    private TableColumn<Formation, String> descriptionColumn;

    @FXML
    private TableColumn<Formation, String> dureeColumn;

    @FXML
    private TableColumn<Formation, String> niveauColumn;

    @FXML
    private TableColumn<Formation, Float> prixColumn;

    @FXML
    private TableColumn<Formation, Void> actionColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set column factories
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imageName"));

        // Image column custom cell
        imageColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                if (empty || imageName == null || imageName.isEmpty()) {
                    setGraphic(null);
                } else {
                    File imageFile = new File("images/formations/" + imageName);
                    if (imageFile.exists()) {
                        imageView.setImage(new Image(imageFile.toURI().toString()));
                        imageView.setFitWidth(80);
                        imageView.setFitHeight(60);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                        System.err.println("⚠️ Image not found: " + imageFile.getAbsolutePath());
                    }
                }
            }
        });

        // Action column custom cell
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox hbox = new HBox(10, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Formation formation = getTableView().getItems().get(getIndex());
                    handleEditFormation(formation);
                });

                deleteButton.setOnAction(event -> {
                    Formation formation = getTableView().getItems().get(getIndex());
                    handleDeleteFormation(formation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        // Load data
        loadFormations();
    }

    private void loadFormations() {
        FormationService fs = new FormationService();
        try {
            List<Formation> formations = fs.getAll();
            formationTableView.setItems(FXCollections.observableArrayList(formations));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des formations.");
        }
    }
    @FXML
    private void handleAjouterFormation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/addFormation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter Formation");
            stage.setScene(new Scene(root));

            // Set up a listener to reload the formations when the AddFormation window is closed
            stage.setOnHidden(e -> loadFormations()); // Reload the formations when the window is closed

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire d'ajout.");
        }
    }



    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void handleEditFormation(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/EditFormation.fxml"));
            Parent root = loader.load();

            EditFormationController controller = loader.getController();
            controller.setFormation(formation);

            Stage stage = new Stage();
            stage.setTitle("Modifier Formation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadFormations(); // refresh after editing

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/getAllFormations.fxml"));
            Parent root = loader.load();
            Node titleTextField = null;
            titleTextField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void handleDeleteFormation(Formation formation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer : " + formation.getTitre() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FormationService fs = new FormationService();
                    fs.delete(formation.getId());
                    loadFormations(); // refresh table
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur lors de la suppression.");
                }
            }
        });
    }
}
