package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminTemplateController implements Initializable {

    @FXML
    private VBox contentArea;

    @FXML
    private Label pageTitle;

    @FXML
    private TextField searchField;

    @FXML
    private ImageView adminImage;

    @FXML
    private ComboBox<String> adminDropdown;

    @FXML
    private ComboBox<String> promotionDropdown;

    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the ComboBox items
        adminDropdown.setItems(javafx.collections.FXCollections.observableArrayList("Reconnecter", "Profil", "Déconnexion"));

        // Handle dropdown actions
        adminDropdown.setOnAction(event -> handleDropdownAction(adminDropdown.getSelectionModel().getSelectedItem()));

// Load a working image (add.png) from the classpath
        try {
            URL imageUrl = getClass().getResource("/images/add.png");
            if (imageUrl == null) {
                showAlert("Erreur", "Image add.png not found in /images/");
                return;
            }
            Image image = new Image(imageUrl.toString());
            if (image.isError()) {
                showAlert("Erreur", "Failed to load add.png: " + image.getException().getMessage());
                return;
            }
            adminImage.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'image : " + e.getMessage());
        }
        // Load the Dashboard as the default page
        navigateToDashboard();
    }

    @FXML
    private void handleSearch() {
        // Delegate search to the current controller in contentArea
        if (contentArea.getChildren().size() > 0) {
            Parent currentContent = (Parent) contentArea.getChildren().get(0);
            if (currentContent.getUserData() instanceof Searchable) {
                ((Searchable) currentContent.getUserData()).handleSearch(searchField.getText().trim().toLowerCase());
            }
        }
    }



    @FXML
    private void showPromotionDropdown() {
        promotionDropdown.show(); // Programmatically show the dropdown menu
    }
    private void handleDropdownAction(String selectedOption) {
        switch (selectedOption) {
            case "Reconnecter":
            case "Déconnexion":
                sessionManager.clearSession();
                navigateToLogin();
                break;
            case "Profil":
                showAlert("Info", "Fonctionnalité de profil à implémenter.");
                break;
            default:
                break;
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminDropdown.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion : " + e.getMessage());
        }
    }

    @FXML
    private void navigateToDashboard() {
        loadContent("/Dashboard.fxml", "Dashboard");
    }

    @FXML
    private void navigateToFormations() {
        loadContent("/formation.fxml", "Liste des Formations");
    }

    @FXML
    private void navigateToFeedback() {
        loadContent("/AdminFormationList.fxml", "Feedback");
    }

    @FXML
    private void navigateToUtilisateurs() {
        loadContent("/UserList.fxml", "Utilisateurs");
    }

    @FXML
    private void navigateToEvenements() {
        loadContent("/Evenements.fxml", "Evenements");
    }

    @FXML
    private void navigateToNotifications() {
        loadContent("/Notifications.fxml", "Notifications");
    }

    @FXML
    private void navigateToCategories() {
        loadContent("/Categories.fxml", "Categories");
    }

    @FXML
    private void navigateToEvaluation() {
        loadContent("/Evaluation.fxml", "Evaluation");
    }

    @FXML
    private void navigateToListeInscription() {
        loadContent("/afficherInscriptionView.fxml", "Liste d'inscription");
    }

    @FXML
    private void navigateToPromotionList() {
        loadContent("/AfficherPromotionsView.fxml", "Liste des Promotions");
    }

    @FXML
    private void navigateToAddPromotion() {
        loadContent("/AjoutPromotionView.fxml", "Ajouter Promotion");
    }

    private void loadContent(String fxmlPath, String title) {
        try {
            // Check if the FXML file exists
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                // Display a message in the contentArea instead of an alert
                contentArea.getChildren().clear();
                Label notImplementedLabel = new Label("This page is not implemented. The responsible of this page didn't do his work.");
                notImplementedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #E53E3E; -fx-padding: 20;");
                contentArea.getChildren().add(notImplementedLabel);
                pageTitle.setText(title);
                searchField.setPromptText("Rechercher " + title.toLowerCase() + "...");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            pageTitle.setText(title);

            // Update search field prompt text based on the page
            searchField.setPromptText("Rechercher " + title.toLowerCase() + "...");

            // Set the controller as user data for search delegation
            content.setUserData(loader.getController());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


interface Searchable {
    void handleSearch(String searchText);
}