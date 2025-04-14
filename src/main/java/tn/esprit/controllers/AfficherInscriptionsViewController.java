package tn.esprit.controllers;

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
import tn.esprit.models.InscriptionCours;
import tn.esprit.services.ServiceInscriptionCours;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherInscriptionsViewController {

    @FXML private TableView<InscriptionCours> tableInscriptions;
    @FXML private TableColumn<InscriptionCours, String> colNom;
    @FXML private TableColumn<InscriptionCours, String> colEmail;
    @FXML private TableColumn<InscriptionCours, String> colFormation;
    @FXML private TableColumn<InscriptionCours, Double> colMontant;
    @FXML private TableColumn<InscriptionCours, String> colStatut;
    @FXML private TableColumn<InscriptionCours, Void> colAction;
    @FXML private TextField searchField;
    @FXML private Pagination pagination;
    @FXML private Button btnRetour;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();
    private ObservableList<InscriptionCours> inscriptionsList;
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        configureColumns();
        configureActionColumn();
        loadInscriptions();

        searchField.textProperty().addListener((obs, oldText, newText) -> filterAndPaginate());
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> changePage(newIndex.intValue()));
        btnRetour.setOnAction(event -> handleRetour());
    }

    private void configureColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomApprenant"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFormation.setCellValueFactory(new PropertyValueFactory<>("nomFormation"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void configureActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox buttons = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
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

    private void loadInscriptions() {
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

    private void filterAndPaginate() {
        String filter = searchField.getText().toLowerCase();
        List<InscriptionCours> filtered = service.getAll().stream()
                .filter(i -> i.getNomApprenant().toLowerCase().contains(filter)
                        || i.getEmail().toLowerCase().contains(filter)
                        || i.getNomFormation().toLowerCase().contains(filter))
                .collect(Collectors.toList());
        inscriptionsList = FXCollections.observableArrayList(filtered);
        setupPagination();
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.delete(inscription);
                loadInscriptions();
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
