package tn.esprit.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.InscriptionCours;
import tn.esprit.services.ServiceInscriptionCours;

import java.io.IOException;
import java.util.List;

public class AfficherInscriptionsViewController {

    @FXML private FlowPane flowPane;
    @FXML private Pagination pagination;
    @FXML private Button btnRetour;
    @FXML private Label totalLabel;
    @FXML private Label confirmedLabel;
    @FXML private Label pendingLabel;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();
    private ObservableList<InscriptionCours> inscriptionsList;
    private static final int ITEMS_PER_PAGE = 6;

    @FXML
    public void initialize() {
        loadAllAndPaginate();
        setupPaginationListener();
        setupButtonActions();
    }

    private void loadAllAndPaginate() {
        List<InscriptionCours> allInscriptions = service.getAll();
        inscriptionsList = FXCollections.observableArrayList(allInscriptions);
        updatePagination();
        updateStats();
    }

    private void updateStats() {
        long total = inscriptionsList.size();
        long confirmed = inscriptionsList.stream().filter(i -> i.getStatus().equalsIgnoreCase("payé")).count();
        long pending = inscriptionsList.stream().filter(i -> i.getStatus().equalsIgnoreCase("en attente")).count();

        totalLabel.setText("🔢 Total: " + total);
        confirmedLabel.setText("✅ Confirmées: " + confirmed);
        pendingLabel.setText("⏳ En attente: " + pending);
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) inscriptionsList.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        updatePageContent(pagination.getCurrentPageIndex());
    }

    private void setupPaginationListener() {
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> updatePageContent(newIndex.intValue())
        );
    }

    private void updatePageContent(int pageIndex) {
        int from = pageIndex * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, inscriptionsList.size());
        flowPane.getChildren().clear();
        for (InscriptionCours inscription : inscriptionsList.subList(from, to)) {
            VBox card = createCard(inscription);
            flowPane.getChildren().add(card);
        }
    }

    private VBox createCard(InscriptionCours inscription) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-container");

        HBox header = new HBox(8);
        Label nomLabel = new Label(inscription.getNomApprenant());
        nomLabel.getStyleClass().add("card-title");

        Label statutLabel = new Label(inscription.getStatus());
        statutLabel.getStyleClass().addAll("status-label", getStatusStyle(inscription.getStatus()));
        HBox.setHgrow(statutLabel, Priority.ALWAYS);

        header.getChildren().addAll(nomLabel, statutLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = createInfoLabel("📧 " + inscription.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px;");
        Label formationLabel = createInfoLabel("🎓 " + inscription.getNomFormation());
        formationLabel.setStyle("-fx-font-size: 12px;");
        Label montantLabel = createInfoLabel(String.format("💶 %.2f DT", inscription.getMontant()));
        montantLabel.setStyle("-fx-font-size: 12px;");

        HBox buttons = createActionButtons(inscription);

        card.getChildren().addAll(header, emailLabel, formationLabel, montantLabel, buttons);
        return card;
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "payé" -> "status-success";
            case "en attente" -> "status-warning";
            default -> "status-error";
        };
    }

    private HBox createActionButtons(InscriptionCours inscription) {
        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("button-modifier");
        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("button-supprimer");

        btnEdit.setOnAction(e -> handleEdit(inscription));
        btnDelete.setOnAction(e -> handleDelete(inscription));

        addHoverAnimation(btnEdit);
        addHoverAnimation(btnDelete);

        HBox buttonBox = new HBox(8, btnEdit, btnDelete);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        return buttonBox;
    }

    private void addHoverAnimation(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        button.setOnMouseEntered(e -> scaleIn.play());
        button.setOnMouseExited(e -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), button);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
        });
    }

    private void setupButtonActions() {
        btnRetour.setOnAction(e -> handleRetour());
    }

    private void handleEdit(InscriptionCours inscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierInscriptionCoursView.fxml"));
            Parent root = loader.load();

            ModifierInscriptionCoursController controller = loader.getController();
            controller.initData(inscription);
            controller.setRefreshCallback(this::refreshData);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur");
        }
    }

    private void handleDelete(InscriptionCours inscription) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cette inscription ?",
                ButtonType.YES, ButtonType.NO
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.delete(inscription);
                refreshData();
            }
        });
    }

    @FXML
    private void handleRetour() {
        try {
            Stage currentStage = (Stage) btnRetour.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inscriptionCoursView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            currentStage.close();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    public void refreshData() {
        loadAllAndPaginate();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}