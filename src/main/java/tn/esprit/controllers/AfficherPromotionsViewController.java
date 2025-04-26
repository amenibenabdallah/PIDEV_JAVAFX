package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServicePromotion;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AfficherPromotionsViewController {

    @FXML
    private FlowPane promoCardContainer;
    @FXML
    private Label totalLabel;
    @FXML
    private Label activeLabel;
    @FXML
    private Label expiredLabel;
    @FXML
    private Pagination pagination;

    private VBox contentArea; // Référence au contentArea du template admin
    private final ServicePromotion service = new ServicePromotion();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ObservableList<Promotion> promotionsList;
    private final int ITEMS_PER_PAGE = 6;

    // Méthode pour injecter le contentArea depuis AdminTemplateController
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
        System.out.println("setContentArea called in AfficherPromotionsViewController: contentArea = " + contentArea);
    }

    @FXML
    public void initialize() {
        loadAllAndPaginate();
        configurePagination();
    }

    private void loadAllAndPaginate() {
        List<Promotion> allPromotions = service.getAll();
        promotionsList = FXCollections.observableArrayList(allPromotions);
        updatePagination();
        updateStats();
    }

    private void configurePagination() {
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            showPage(newVal.intValue());
        });
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) promotionsList.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        showPage(0);
    }

    private void showPage(int pageIndex) {
        int from = pageIndex * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, promotionsList.size());

        promoCardContainer.getChildren().clear();
        promotionsList.subList(from, to).forEach(promo -> {
            promoCardContainer.getChildren().add(createPromotionCard(promo));
        });
    }

    private void updateStats() {
        long total = promotionsList.size();
        long active = promotionsList.stream()
                .filter(p -> p.getDateExpiration().isAfter(LocalDate.now()))
                .count();

        totalLabel.setText("🔢 Total: " + total);
        activeLabel.setText("✅ Actives: " + active);
        expiredLabel.setText("⌛ Expirées: " + (total - active));
    }

    private VBox createPromotionCard(Promotion promotion) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        // Header
        HBox header = new HBox(10);
        Label codeLabel = new Label(promotion.getCodePromo());
        codeLabel.getStyleClass().add("card-title");

        Label statusBadge = new Label();
        statusBadge.getStyleClass().add(
                promotion.getDateExpiration().isAfter(LocalDate.now())
                        ? "badge-actif"
                        : "badge-expire"
        );
        statusBadge.setText(promotion.getDateExpiration().isAfter(LocalDate.now())
                ? "ACTIVE"
                : "EXPIRÉE");

        header.getChildren().addAll(codeLabel, statusBadge);

        // Content
        Label remiseLabel = new Label("Remise: " + promotion.getRemise() + "%");
        Label descLabel = new Label(promotion.getDescription());
        descLabel.setWrapText(true);
        Label expLabel = new Label("Expire le: " + promotion.getDateExpiration().format(dateFormatter));

        // Actions
        HBox buttons = new HBox(10);
        Button editBtn = new Button("Modifier");
        Button deleteBtn = new Button("Supprimer");

        editBtn.getStyleClass().addAll("button-modifier");
        deleteBtn.getStyleClass().addAll("button-supprimer");

        editBtn.setOnAction(e -> handleEdit(promotion));
        deleteBtn.setOnAction(e -> handleDelete(promotion));

        buttons.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(header, remiseLabel, descLabel, expLabel, buttons);

        return card;
    }

    // Charge ModifierPromotionView.fxml dans contentArea
    private void handleEdit(Promotion promotion) {
        if (contentArea == null) {
            showAlert("Erreur", "Le conteneur de contenu n'est pas initialisé. Contactez l'administrateur.");
            System.err.println("Erreur : contentArea est null dans handleEdit");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPromotionView.fxml"));
            Parent root = loader.load();

            ModifierPromotionViewController controller = loader.getController();
            controller.initData(promotion);
            controller.setContentArea(contentArea); // Injecte contentArea pour la navigation retour

            contentArea.getChildren().clear(); // Vide le contentArea
            contentArea.getChildren().add(root); // Ajoute la vue de modification
            VBox.setVgrow(root, Priority.ALWAYS); // Assure que la vue occupe tout l'espace

        } catch (IOException e) {
            showAlert("Erreur", "Échec de l'ouverture de l'éditeur : " + e.getMessage());
        }
    }

    private void handleDelete(Promotion promotion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la promotion " + promotion.getCodePromo() + " ?",
                ButtonType.YES,
                ButtonType.NO
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.delete(promotion);
                loadAllAndPaginate();
            }
        });
    }

    @FXML
    private void handleAjoutPromotion() {
        if (contentArea == null) {
            showAlert("Erreur", "Le conteneur de contenu n'est pas initialisé. Contactez l'administrateur.");
            System.err.println("Erreur : contentArea est null dans handleAjoutPromotion");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutPromotionView.fxml"));
            Parent root = loader.load();

            // Injection du contentArea dans le contrôleur d'ajout
            Object controller = loader.getController();
            if (controller instanceof AjoutPromotionViewController) {
                ((AjoutPromotionViewController) controller).setContentArea(contentArea);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            VBox.setVgrow(root, Priority.ALWAYS);

        } catch (IOException e) {
            showAlert("Erreur", "Échec du chargement du formulaire : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}