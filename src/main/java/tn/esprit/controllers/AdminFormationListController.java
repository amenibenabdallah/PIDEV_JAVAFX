package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Avis;
import tn.esprit.models.FormationA;
import tn.esprit.services.FormationServiceA;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminFormationListController implements Initializable, Searchable {

    @FXML
    private TableView<FormationA> formationTableView;

    @FXML
    private TableColumn<FormationA, String> formationNameColumn;

    @FXML
    private TableColumn<FormationA, Double> noteMoyenneColumn;

    @FXML
    private TableColumn<FormationA, Integer> nombreAvisColumn;

    @FXML
    private TableColumn<FormationA, Void> actionsColumn;

    @FXML
    private Label selectedFormationLabel;

    @FXML
    private VBox avisContainer;

    @FXML
    private FlowPane avisFlowPane;

    private ObservableList<FormationA> formationList;
    private ObservableList<FormationA> filteredFormationList;
    private final FormationServiceA formationService = new FormationServiceA();
    private final DecimalFormat decimalFormat = new DecimalFormat("#.#");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the TableView columns
        formationNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        formationNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(10, 10, 10, 10));
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        noteMoyenneColumn.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        noteMoyenneColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(10, 10, 10, 10));
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(item) + "/5");
                    if (item < 3) {
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else if (item >= 3 && item < 4) {
                        setStyle("-fx-text-fill: #f39c12;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71;");
                    }
                }
            }
        });

        nombreAvisColumn.setCellValueFactory(new PropertyValueFactory<>("avisCount"));
        nombreAvisColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(10, 10, 10, 10));
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button avisButton = new Button("Avis");
            private final HBox hbox = new HBox(avisButton);

            {
                avisButton.getStyleClass().add("action-button");
                avisButton.setOnAction(event -> {
                    FormationA formation = getTableView().getItems().get(getIndex());
                    handleShowAvis(formation);
                });
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(10, 10, 10, 10));
                avisButton.setPadding(new Insets(8, 20, 8, 20));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0));
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Dynamically adjust column widths
        formationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        formationNameColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.4));
        noteMoyenneColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.2));
        nombreAvisColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.2));
        actionsColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.2));

        formationTableView.setFixedCellSize(60);

        // Load formations
        loadFormations();

        // Initialize the filtered list
        filteredFormationList = FXCollections.observableArrayList(formationList);
    }

    private void loadFormations() {
        try {
            formationList = FXCollections.observableArrayList(formationService.getAllFormations());
            formationTableView.setItems(formationList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les formations : " + e.getMessage());
        }
    }

    @Override
    public void handleSearch(String searchText) {
        if (searchText.isEmpty()) {
            formationTableView.setItems(formationList);
        } else {
            filteredFormationList.clear();
            filteredFormationList.addAll(formationList.stream()
                    .filter(formation -> formation.getName().toLowerCase().contains(searchText))
                    .collect(Collectors.toList()));
            formationTableView.setItems(filteredFormationList);
        }
    }

    private void handleShowAvis(FormationA selectedFormation) {
        if (selectedFormation == null) {
            return;
        }

        selectedFormationLabel.setText("Avis for " + selectedFormation.getName());
        avisFlowPane.getChildren().clear();

        for (Avis avis : selectedFormation.getAvisList()) {
            avisFlowPane.getChildren().add(createAvisCard(avis));
        }

        avisContainer.setVisible(true);
        avisContainer.setManaged(true);
    }

    @FXML
    private void handleHideAvis() {
        avisContainer.setVisible(false);
        avisContainer.setManaged(false);
    }

    private VBox createAvisCard(Avis avis) {
        VBox card = new VBox(10);
        card.getStyleClass().add("avis-card");

        HBox starBox = new HBox(5);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add(i <= (int) avis.getNote() ? "star-selected" : "star");
            starBox.getChildren().add(star);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Label dateLabel = new Label(avis.getDateCreation().format(formatter));
        dateLabel.getStyleClass().add("date-label");

        Label commentLabel = new Label(avis.getCommentaire());
        commentLabel.getStyleClass().add("comment-text");

        card.getChildren().addAll(starBox, dateLabel, commentLabel);

        return card;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}