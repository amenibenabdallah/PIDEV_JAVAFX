package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Notification;
import tn.esprit.services.NotificationService;

import java.util.stream.Collectors;

public class NotificationViewController {

    @FXML private TextField searchField;
    @FXML private VBox notificationCards;

    private NotificationService notificationService;
    private ObservableList<Notification> notificationList;
    private ObservableList<Notification> filteredNotificationList;

    @FXML
    public void initialize() {
        notificationService = new NotificationService();
        notificationList = FXCollections.observableArrayList(notificationService.getAll());
        filteredNotificationList = FXCollections.observableArrayList(notificationList);
        refreshCards(filteredNotificationList);
    }

    private void refreshCards(ObservableList<Notification> notifications) {
        notificationCards.getChildren().clear();
        for (Notification notification : notifications) {
            VBox card = createNotificationCard(notification);
            notificationCards.getChildren().add(card);
        }
    }

    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(5);
        card.getStyleClass().add("notification-card");

        Label titreLabel = new Label(notification.getTitre());
        titreLabel.getStyleClass().add("notification-card-title");

        Label contenuLabel = new Label(notification.getContenu());
        contenuLabel.getStyleClass().add("notification-card-content");

        Label sentAtLabel = new Label(notification.getSentAt().toString());
        sentAtLabel.getStyleClass().add("notification-card-date");

        Label evenementLabel = new Label(notification.getEvenement() != null
                ? "Événement: " + notification.getEvenement().getTitre()
                : "Aucun événement associé");
        evenementLabel.getStyleClass().add("notification-card-event");

        card.getChildren().addAll(titreLabel, contenuLabel, sentAtLabel, evenementLabel);
        return card;
    }

    @FXML
    private void handleSearchAction(ActionEvent event) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            filteredNotificationList.setAll(notificationList);
        } else {
            filteredNotificationList.setAll(notificationList.stream()
                    .filter(n -> n.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            n.getContenu().toLowerCase().contains(searchText.toLowerCase()) ||
                            (n.getEvenement() != null && n.getEvenement().getTitre().toLowerCase().contains(searchText.toLowerCase())))
                    .collect(Collectors.toList()));
        }
        refreshCards(filteredNotificationList);
    }
}