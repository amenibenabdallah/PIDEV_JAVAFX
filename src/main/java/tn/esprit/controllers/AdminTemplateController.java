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
import tn.esprit.models.Evaluation;
import tn.esprit.models.FormationA;
import tn.esprit.utils.SessionManager;

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

    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the ComboBox items
        adminDropdown.setItems(javafx.collections.FXCollections.observableArrayList("Profil", "Déconnexion"));

        // Handle dropdown actions
        adminDropdown.setOnAction(event -> handleDropdownAction(adminDropdown.getSelectionModel().getSelectedItem()));

        // Load a working image (add.png) from the classpath
        try {
            URL imageUrl = getClass().getResource("/images/admin.jpg");
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

    private void handleDropdownAction(String selectedOption) {
        switch (selectedOption) {
            case "Reconnecter":
            case "Déconnexion":
                sessionManager.logout();
                navigateToLogin();
                break;
            case "Profil":
                navigateToProfilAdmin();
                break;
            default:
                break;
        }
    }

    private void navigateToProfilAdmin() {
        loadContent("/ProfilAdmin.fxml", "Profil Admin");
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
        loadContent("/AdminDashboard.fxml", "Dashboard");
    }

    @FXML
    public void navigateToFormations() {
        loadContent("/formation.fxml", "Formations");
    }

    @FXML
    public void navigateToReaderChart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormationViewA.fxml"));
            Parent root = loader.load();
            FormationAController controller = loader.getController();
            controller.setTemplateController(this);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.out.println("Error navigating to Formations: " + e.getMessage());
            e.printStackTrace();
        }
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
        loadContent("/evenement_crud.fxml", "Evenements");
    }

    @FXML
    private void navigateToNotifications() {
        loadContent("/Notificationcrud.fxml", "Notifications");
    }

    @FXML
    private void navigateToCategories() {
        loadContent("/Categories.fxml", "Categories");
    }

    @FXML
    void navigateToEvaluation() {
        loadContent("/EvaluationView.fxml", "Evaluation");
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

    public void navigateToEvaluationDetails(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EvaluationDetailsView.fxml"));
            Parent content = loader.load();

            // Pass the evaluation to the details controller
            EvaluationDetailsController controller = loader.getController();
            controller.setEvaluation(evaluation);
            controller.setTemplateController(this); // Pass the template controller for navigation

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            pageTitle.setText("Détails de l'Évaluation");

            // Update search field prompt text
            searchField.setPromptText("Rechercher détails...");

            // Set the controller as user data for search delegation
            content.setUserData(loader.getController());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails de l'évaluation : " + e.getMessage());
        }
    }

    public void navigateToIdealCvComparison(FormationA formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IdealCvComparisonView.fxml"));
            Parent root = loader.load();
            IdealCvComparisonController controller = loader.getController();
            controller.setTemplateController(this);
            controller.setFormation(formation);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.out.println("Error navigating to Ideal CV Comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadContent(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                contentArea.getChildren().clear();
                Label notImplementedLabel = new Label("Cette page n'est pas implémentée.");
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
            searchField.setPromptText("Rechercher " + title.toLowerCase() + "...");

            // Set the controller as user data for search delegation
            Object controller = loader.getController();
            content.setUserData(controller);

            // Inject this AdminTemplateController into EvaluationController if applicable
            if (controller instanceof EvaluationController) {
                ((EvaluationController) controller).setTemplateController(this);
            }

            // Inject contentArea into appropriate controllers
            if (controller instanceof AfficherPromotionsViewController) {
                ((AfficherPromotionsViewController) controller).setContentArea(contentArea);
            } else if (controller instanceof AfficherInscriptionsViewController) {
                ((AfficherInscriptionsViewController) controller).setContentArea(contentArea);
            } else if (controller instanceof AjoutPromotionViewController) {
                ((AjoutPromotionViewController) controller).setContentArea(contentArea);
            }
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