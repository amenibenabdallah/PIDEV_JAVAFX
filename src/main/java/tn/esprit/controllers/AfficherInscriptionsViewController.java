package tn.esprit.controllers;

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

public class AfficherInscriptionsViewController {

    @FXML private TableView<InscriptionCours> tableInscriptions;
    @FXML private TableColumn<InscriptionCours, String> colNom;
    @FXML private TableColumn<InscriptionCours, String> colEmail;
    @FXML private TableColumn<InscriptionCours, String> colFormation;
    @FXML private TableColumn<InscriptionCours, Double> colMontant;
    @FXML private TableColumn<InscriptionCours, String> colStatut;
    @FXML private TableColumn<InscriptionCours, Void> colAction;
    @FXML private Button btnRetour;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();

    @FXML
    public void initialize() {
        configureColumns();
        configureActionColumn();
        loadInscriptions();

        // Action pour le bouton "Retour à l'ajout"
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

            // Fermer la fenêtre actuelle (affichage)
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
                refreshTable();
            }
        });
    }

    private void refreshTable() {
        tableInscriptions.getItems().setAll(service.getAll());
    }

    private void loadInscriptions() {
        tableInscriptions.getItems().setAll(service.getAll());
    }

    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inscriptionCoursView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une inscription");
            stage.show();

            // Fermer la fenêtre actuelle
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
