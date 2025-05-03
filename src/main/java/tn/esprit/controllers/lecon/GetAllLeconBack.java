package tn.esprit.controllers.lecon;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.models.Lecon;
import tn.esprit.models.Formation;
import tn.esprit.services.LeconService;
import tn.esprit.services.FormationService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class GetAllLeconBack implements Initializable {

    @FXML
    private ComboBox<Formation> formationComboBox;

    @FXML
    private Button ajouterLeconBtn;

    @FXML
    private TableView<Lecon> leconTableView;

    @FXML
    private TableColumn<Lecon, Integer> idCol;

    @FXML
    private TableColumn<Lecon, String> titreCol;

    @FXML
    private TableColumn<Lecon, String> contenuCol;

    @FXML
    private TableColumn<Lecon, String> dateCreationCol;

    @FXML
    private TableColumn<Lecon, Void> actionCol;

    private final LeconService leconService = new LeconService();
    private final FormationService formationService = new FormationService();
    private Formation currentFormation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        setupTableView();
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
            showAlert("Error", "Failed to load formations", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTableView() {
        // Configure table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenuCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        dateCreationCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Configure actions column
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

                // Set button actions
                editButton.setOnAction(event -> {
                    Lecon lecon = getTableView().getItems().get(getIndex());
                    handleEditLecon(lecon);
                });

                deleteButton.setOnAction(event -> {
                    Lecon lecon = getTableView().getItems().get(getIndex());
                    handleDeleteLecon(lecon);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadLeconsForFormation(int formationId) {
        try {
            List<Lecon> lecons = leconService.getByFormation(formationId);
            leconTableView.setItems(FXCollections.observableArrayList(lecons));
        } catch (SQLException e) {
            showAlert("Error", "Failed to load lessons", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleAddLecon() {
        try {
            // Load the AddLecon view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Lecon/AddLecon.fxml"));
            Parent root = loader.load();

            // Pass the current formation to the add controller if needed


            // Show the add dialog
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Leçon");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh after closing
            if (currentFormation != null) {
                loadLeconsForFormation(currentFormation.getId());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open add lesson window", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleEditLecon(Lecon lecon) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Lecon/EditLecon.fxml"));
            Parent root = loader.load();

            EditLeconController controller = loader.getController();
            controller.setLecon(lecon);

            Stage stage = new Stage();
            stage.setTitle("Modifier Leçon");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (currentFormation != null) {
                loadLeconsForFormation(currentFormation.getId());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open edit lesson window", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleDeleteLecon(Lecon lecon) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la leçon");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette leçon?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    leconService.delete(lecon.getId());
                    if (currentFormation != null) {
                        loadLeconsForFormation(currentFormation.getId());
                    }
                    showAlert("Success", "Lesson deleted successfully", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete lesson", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}