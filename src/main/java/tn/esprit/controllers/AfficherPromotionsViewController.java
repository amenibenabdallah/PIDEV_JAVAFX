package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;
import java.io.IOException;
import java.time.LocalDate;

public class AfficherPromotionsViewController {

    @FXML private TableView<Promotion> tablePromotions;
    @FXML private TableColumn<Promotion, String> colCode;
    @FXML private TableColumn<Promotion, String> colDescription;
    @FXML private TableColumn<Promotion, Double> colRemise;
    @FXML private TableColumn<Promotion, LocalDate> colExpiration;
    @FXML private TableColumn<Promotion, Void> colActions;

    private final ServicePromotion service = new ServicePromotion();

    @FXML
    public void initialize() {
        configureColumns();
        loadData();
    }

    private void configureColumns() {
        // Configuration des PropertyValueFactory
        colCode.setCellValueFactory(new PropertyValueFactory<>("codePromo"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRemise.setCellValueFactory(new PropertyValueFactory<>("remise"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        // Configuration de la colonne "Actions"
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox buttons = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                btnEdit.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadData() {
        tablePromotions.getItems().setAll(service.getAll());
    }

    private void handleEdit(Promotion promotion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPromotionView.fxml"));
            Parent root = loader.load();
            ModifierPromotionViewController controller = loader.getController();
            controller.initData(promotion);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData(); // Rafraîchir après modification
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue de modification.");
        }
    }

    private void handleDelete(Promotion promotion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette promotion ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.delete(promotion); // ✅ Suppression par ID
                loadData();
            }
        });
    }

    @FXML
    private void handleRetour() {
        try {
            Stage currentStage = (Stage) tablePromotions.getScene().getWindow();
            currentStage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutPromotionView.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue d'ajout.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}