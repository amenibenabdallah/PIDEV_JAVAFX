package tn.esprit.controllers;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.models.Evenement;
import tn.esprit.models.Notification;
import tn.esprit.services.EvenementService;
import tn.esprit.services.NotificationService;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class NotificationController implements Searchable {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private ComboBox<Evenement> evenementComboBox;
    @FXML private VBox notificationCards;
    @FXML private SplitPane splitPane;
    @FXML private VBox formPanel;
    @FXML private TextField searchField;

    private NotificationService notificationService;
    private EvenementService evenementService;
    private ObservableList<Notification> notificationList;
    private ObservableList<Notification> filteredNotificationList;
    private ObservableList<Evenement> evenementList;
    private Notification selectedNotification;
    private boolean isFormVisible = false;

    @FXML
    public void initialize() {
        notificationService = new NotificationService();
        evenementService = new EvenementService();

        notificationList = FXCollections.observableArrayList(notificationService.getAll());
        filteredNotificationList = FXCollections.observableArrayList(notificationList);
        evenementList = FXCollections.observableArrayList(evenementService.getAll());
        evenementComboBox.setItems(evenementList);

        refreshCards(filteredNotificationList);

        // Ensure form panel is hidden initially
        splitPane.setDividerPositions(1.0);
        formPanel.setPrefWidth(0);

        // Set up ComboBox display
        evenementComboBox.setCellFactory(cb -> new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitre());
            }
        });
        evenementComboBox.setButtonCell(new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitre());
            }
        });
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

        HBox buttonBox = new HBox(10);
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("notification-button", "notification-button-update");
        editButton.setOnAction(e -> {
            selectedNotification = notification;
            titreField.setText(notification.getTitre());
            contenuArea.setText(notification.getContenu());
            evenementComboBox.setValue(notification.getEvenement());
            toggleForm();
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("notification-button", "notification-button-delete");
        deleteButton.setOnAction(e -> {
            notificationService.delete(notification.getId());
            notificationList.remove(notification);
            filteredNotificationList.remove(notification);
            refreshCards(filteredNotificationList);
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Notification supprimée avec succès.");
        });

        buttonBox.getChildren().addAll(editButton, deleteButton);
        card.getChildren().addAll(titreLabel, contenuLabel, sentAtLabel, evenementLabel, buttonBox);
        return card;
    }

    @FXML
    private void toggleForm() {
        isFormVisible = !isFormVisible;
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), formPanel);
        if (isFormVisible) {
            splitPane.setDividerPositions(0.7);
            formPanel.setPrefWidth(400);
            transition.setToX(0);
        } else {
            splitPane.setDividerPositions(1.0);
            formPanel.setPrefWidth(0);
            transition.setToX(400);
            clearFields();
        }
        transition.play();
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            Notification notification;
            if (selectedNotification == null) {
                notification = new Notification();
                notification.setSentAt(LocalDateTime.now());
            } else {
                notification = selectedNotification;
            }

            notification.setTitre(titreField.getText());
            notification.setContenu(contenuArea.getText());
            notification.setEvenement(evenementComboBox.getValue());

            if (selectedNotification == null) {
                notificationService.add(notification);
                notificationList.add(notification);
            } else {
                notificationService.update(notification);
            }

            filteredNotificationList.setAll(notificationList);
            refreshCards(filteredNotificationList);
            toggleForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    selectedNotification == null ? "Notification ajoutée avec succès." : "Notification mise à jour avec succès.");
        }
    }

    @FXML
    private void handleCancel() {
        toggleForm();
    }

    @FXML
    public void handleSearchAction(ActionEvent event) {
        String searchText = searchField.getText();
        handleSearch(searchText);
    }

    @Override
    public void handleSearch(String searchText) {
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

    private boolean validateInput() {
        if (titreField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est requis.");
            return false;
        }
        if (contenuArea.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le contenu est requis.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        titreField.clear();
        contenuArea.clear();
        evenementComboBox.setValue(null);
        selectedNotification = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}