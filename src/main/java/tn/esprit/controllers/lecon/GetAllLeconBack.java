package tn.esprit.controllers.lecon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
    private TableView<Lecon> leconTableView;
    @FXML
    private TableColumn<Lecon, Integer> idCol;
    @FXML
    private TableColumn<Lecon, String> titreCol;
    @FXML
    private TableColumn<Lecon, String> contenuCol;
    @FXML
    private TableColumn<Lecon, LocalDate> dateCreationCol;
    @FXML
    private TableColumn<Lecon, Void> actionCol;

    private final LeconService leconService = new LeconService();
    private final FormationService formationService = new FormationService();

    private Formation currentFormation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        setupTableColumns();
        addActionButtonsToTable();
        ajouterLeconBtn.setOnAction(e -> handleAddLecon());

    }
    private void handleAddLecon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Lecon/AddLecon.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Leçon");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Waits for the AddLecon window to close

            // After the add lesson window is closed, reload the lessons if a formation is selected
            if (currentFormation != null) {
                loadLeconsForFormation(currentFormation.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
            ObservableList<Lecon> leconList = FXCollections.observableArrayList(lecons);
            leconTableView.setItems(leconList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenuCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        dateCreationCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
    }

    private void addActionButtonsToTable() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox actionBox = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    Lecon lecon = getTableView().getItems().get(getIndex());
                    handleEditLecon(lecon);
                });

                btnSupprimer.setOnAction(event -> {
                    Lecon lecon = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText(null);
                    alert.setContentText("Voulez-vous vraiment supprimer la leçon : " + lecon.getTitre() + " ?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                leconService.delete(lecon.getId());
                                getTableView().getItems().remove(lecon); // refresh table
                            } catch (SQLException e) {
                                e.printStackTrace();
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Erreur");
                                errorAlert.setHeaderText(null);
                                errorAlert.setContentText("Erreur lors de la suppression.");
                                errorAlert.showAndWait();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
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
