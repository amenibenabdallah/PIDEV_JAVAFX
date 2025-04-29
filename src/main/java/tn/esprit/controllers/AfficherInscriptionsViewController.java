package tn.esprit.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.InscriptionCours;
import tn.esprit.services.ServiceInscriptionCours;

import java.io.IOException;
import java.util.List;

public class AfficherInscriptionsViewController {

    @FXML private TableView<InscriptionCours> tableInscriptions;
    @FXML private TableColumn<InscriptionCours, String> colNom;
    @FXML private TableColumn<InscriptionCours, String> colEmail;
    @FXML private TableColumn<InscriptionCours, String> colFormation;
    @FXML private TableColumn<InscriptionCours, Double> colMontant;
    @FXML private TableColumn<InscriptionCours, String> colStatut;
    @FXML private TableColumn<InscriptionCours, Void> colAction;
    @FXML private Pagination pagination;
    @FXML private Button btnRetour;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();
    private ObservableList<InscriptionCours> inscriptionsList;
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        configureColumns();
        configureActionColumn();
        loadAllAndPaginate();

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> changePage(newIndex.intValue()));
        btnRetour.setOnAction(event -> handleRetour());
    }

    private void configureColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomApprenant"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFormation.setCellValueFactory(new PropertyValueFactory<>("nomFormation"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Infobulles et rendu personnalisé
        colNom.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTooltip(new Tooltip("Nom de l'apprenant"));
            }
        });
        colEmail.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTooltip(new Tooltip("Adresse email de l'apprenant"));
            }
        });
        colFormation.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTooltip(new Tooltip("Nom de la formation"));
            }
        });
        colMontant.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.2f DT", item));
                setTooltip(new Tooltip("Montant de l'inscription"));
            }
        });
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTooltip(new Tooltip("Statut de l'inscription"));
                    if (item.equalsIgnoreCase("Payé")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("En attente")) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void configureActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox buttons = new HBox(10, btnEdit, btnDelete);

            {
                buttons.setStyle("-fx-alignment: center;");
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-min-width: 80;");
                btnDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-min-width: 80;");

                // Tooltips
                btnEdit.setTooltip(new Tooltip("Modifier les détails de l'inscription"));
                btnDelete.setTooltip(new Tooltip("Supprimer cette inscription"));

                // Animations
                btnEdit.setOnMouseEntered(e -> {
                    btnEdit.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-min-width: 80;");
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), btnEdit);
                    st.setToX(1.1);
                    st.setToY(1.1);
                    st.play();
                });
                btnEdit.setOnMouseExited(e -> {
                    btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-min-width: 80;");
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), btnEdit);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnDelete.setOnMouseEntered(e -> {
                    btnDelete.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-min-width: 80;");
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), btnDelete);
                    st.setToX(1.1);
                    st.setToY(1.1);
                    st.play();
                });
                btnDelete.setOnMouseExited(e -> {
                    btnDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-min-width: 80;");
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), btnDelete);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                btnEdit.setOnAction(event -> handleEdit(getCurrentItem()));
                btnDelete.setOnAction(event -> handleDelete(getCurrentItem()));
            }

            private InscriptionCours getCurrentItem() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadAllAndPaginate() {
        List<InscriptionCours> all = service.getAll();
        inscriptionsList = FXCollections.observableArrayList(all);
        setupPagination();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) inscriptionsList.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        changePage(0);
    }

    private void changePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, inscriptionsList.size());
        tableInscriptions.setItems(FXCollections.observableArrayList(inscriptionsList.subList(fromIndex, toIndex)));
    }

    private void handleEdit(InscriptionCours inscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierInscriptionCoursView.fxml"));
            Parent root = loader.load();

            ModifierInscriptionCoursController controller = loader.getController();
            controller.initData(inscription);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'inscription");
            stage.show();

            Stage currentStage = (Stage) tableInscriptions.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue de modification.");
        }
    }

    private void handleDelete(InscriptionCours inscription) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette inscription ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.delete(inscription);
                loadAllAndPaginate();
            }
        });
    }

    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inscriptionCoursView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une inscription");
            stage.show();

            Stage currentStage = (Stage) btnRetour.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'inscription.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}