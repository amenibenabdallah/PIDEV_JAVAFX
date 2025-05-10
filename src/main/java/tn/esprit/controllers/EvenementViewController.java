package tn.esprit.controllers;

import com.google.zxing.WriterException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Evenement;
import tn.esprit.services.EvenementService;
import tn.esprit.utils.QRCodeUtil;

import java.io.File;
import java.util.stream.Collectors;

public class EvenementViewController {

    @FXML private TextField searchField;
    @FXML private VBox evenementCards;

    private EvenementService evenementService;
    private ObservableList<Evenement> evenementList;
    private ObservableList<Evenement> filteredEvenementList;

    @FXML
    public void initialize() {
        evenementService = new EvenementService();
        evenementList = FXCollections.observableArrayList(evenementService.getAll());
        filteredEvenementList = FXCollections.observableArrayList(evenementList);
        refreshCards(filteredEvenementList);
    }

    private void refreshCards(ObservableList<Evenement> events) {
        evenementCards.getChildren().clear();
        for (Evenement evenement : events) {
            VBox card = createEvenementCard(evenement);
            evenementCards.getChildren().add(card);
        }
    }

    private VBox createEvenementCard(Evenement evenement) {
        VBox card = new VBox(5);
        card.getStyleClass().add("evenement-card");

        Label titleLabel = new Label(evenement.getTitre());
        titleLabel.getStyleClass().add("evenement-card-title");

        Label dateLabel = new Label(evenement.getDate().toString());
        dateLabel.getStyleClass().add("evenement-card-date");

        Label descriptionLabel = new Label(evenement.getDescription());
        descriptionLabel.getStyleClass().add("evenement-card-description");

        Label emplacementLabel = new Label("Lieu: " + (evenement.getEmplacement() != null ? evenement.getEmplacement() : "Non spécifié"));
        emplacementLabel.getStyleClass().add("evenement-card-emplacement");

        ImageView cardImageView = new ImageView();
        if (evenement.getPhoto() != null && !evenement.getPhoto().isEmpty()) {
            try {
                String absolutePath = new File("src/main/resources" + evenement.getPhoto()).getAbsolutePath();
                File imageFile = new File(absolutePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    cardImageView.setImage(image);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de charger l'image : " + e.getMessage());
            }
        }
        cardImageView.getStyleClass().add("evenement-card-image");

        ImageView qrCodeView = new ImageView();
        if (evenement.getEmplacement() != null && !evenement.getEmplacement().isEmpty()) {
            try {
                Image qrCodeImage = QRCodeUtil.generateQRCode(evenement.getEmplacement(), 100, 100);
                qrCodeView.setImage(qrCodeImage);
            } catch (WriterException e) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de générer le QR code : " + e.getMessage());
            }
        }
        qrCodeView.getStyleClass().add("evenement-card-qrcode");

        card.getChildren().addAll(cardImageView, titleLabel, dateLabel, descriptionLabel, emplacementLabel, qrCodeView);
        return card;
    }

    @FXML
    private void handleSearchAction(ActionEvent event) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            filteredEvenementList.setAll(evenementList);
        } else {
            filteredEvenementList.setAll(evenementList.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            e.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                            (e.getPhoto() != null && e.getPhoto().toLowerCase().contains(searchText.toLowerCase())) ||
                            (e.getEmplacement() != null && e.getEmplacement().toLowerCase().contains(searchText.toLowerCase())))
                    .collect(Collectors.toList()));
        }
        refreshCards(filteredEvenementList);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}