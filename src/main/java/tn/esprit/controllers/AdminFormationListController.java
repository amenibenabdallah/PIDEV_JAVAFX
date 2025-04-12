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

public class AdminFormationListController implements Initializable {

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
    private final FormationServiceA formationService = new FormationServiceA();
    private final DecimalFormat decimalFormat = new DecimalFormat("#.#"); // Format to 1 decimal place

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the TableView columns
        formationNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        formationNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(10, 10, 10, 10)); // Consistent padding
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
                setPadding(new Insets(10, 10, 10, 10)); // Consistent padding
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(item)+ "/5");
                    // Apply color based on the note value
                    if (item < 3) {
                        setStyle("-fx-text-fill: #e74c3c;"); // Red for poor (< 3)
                    } else if (item >= 3 && item < 4) {
                        setStyle("-fx-text-fill: #f39c12;"); // Orange for average (3 to <4)
                    } else {
                        setStyle("-fx-text-fill: #2ecc71;"); // Green for good (≥ 4)
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
                setPadding(new Insets(10, 10, 10, 10)); // Consistent padding
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Set up the Actions column with a button
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
                hbox.setPadding(new Insets(10, 10, 10, 10)); // Consistent padding
                avisButton.setPadding(new Insets(8, 20, 8, 20)); // Match button padding
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0)); // No extra padding on the cell itself
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Dynamically adjust column widths to fill the TableView
        formationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        formationNameColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.4)); // 40% for Formation
        noteMoyenneColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.2)); // 20% for Note Moyenne
        nombreAvisColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.2)); // 20% for Nombre d’Avis
        actionsColumn.prefWidthProperty().bind(formationTableView.widthProperty().multiply(0.2)); // 20% for Actions

        // Set a fixed cell size to ensure larger row height
        formationTableView.setFixedCellSize(60); // Increase row height

        // Load formations from the database
        loadFormations();
    }

    private void loadFormations() {
        try {
            formationList = FXCollections.observableArrayList(formationService.getAllFormations());
            formationTableView.setItems(formationList);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the error (e.g., show an alert to the user)
        }
    }

    private void handleShowAvis(FormationA selectedFormation) {
        if (selectedFormation == null) {
            return; // No formation selected
        }

        // Update the selected formation label
        selectedFormationLabel.setText("Avis for " + selectedFormation.getName());

        // Clear previous avis cards
        avisFlowPane.getChildren().clear();

        // Populate avis cards
        for (Avis avis : selectedFormation.getAvisList()) {
            avisFlowPane.getChildren().add(createAvisCard(avis));
        }

        // Show the avis container
        avisContainer.setVisible(true);
        avisContainer.setManaged(true);
    }

    @FXML
    private void handleHideAvis() {
        // Hide the avis container
        avisContainer.setVisible(false);
        avisContainer.setManaged(false);
    }

    private VBox createAvisCard(Avis avis) {
        // Create a card for the avis
        VBox card = new VBox(10);
        card.getStyleClass().add("avis-card");

        // Star rating
        HBox starBox = new HBox(5);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.getStyleClass().add(i <= (int) avis.getNote() ? "star-selected" : "star");
            starBox.getChildren().add(star);
        }

        // Date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Label dateLabel = new Label(avis.getDateCreation().format(formatter));
        dateLabel.getStyleClass().add("date-label");

        // Comment
        Label commentLabel = new Label(avis.getCommentaire());
        commentLabel.getStyleClass().add("comment-text");

        // Add elements to card
        card.getChildren().addAll(starBox, dateLabel, commentLabel);

        return card;
    }
}