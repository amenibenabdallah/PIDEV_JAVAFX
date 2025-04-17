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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InstructeurTemplateController implements Initializable {

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

    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adminDropdown.setItems(javafx.collections.FXCollections.observableArrayList("Reconnecter", "Profil", "Déconnexion"));
        adminDropdown.setOnAction(event -> handleDropdownAction(adminDropdown.getSelectionModel().getSelectedItem()));

        try {
            URL imageUrl = getClass().getResource("/images/add.png");
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toString());
                if (!image.isError()) {
                    adminImage.setImage(image);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        navigateToDashboard(); // load default page
    }

    @FXML
    private void handleSearch() {
        if (!contentArea.getChildren().isEmpty()) {
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
                sessionManager.clearSession();
                navigateToLogin();
                break;
            case "Profil":
                showAlert("Info", "Fonctionnalité de profil à implémenter.");
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

    // === Navigation Methods ===
    @FXML
    private void navigateToDashboard() {
        loadContent("Dashboard.fxml", "Dashboard");
    }
    @FXML
    private void navigateToCategories() {
        loadContent("/Category/getAllCategories.fxml", "Categories");
    }

    @FXML
    private void navigateToFormations() {
        loadContent("/Formation/getAllFormations.fxml", "Formations");
    }

    @FXML
    private void navigateToLecons() {
        loadContent("/Lecon/GetAllLeconBack.fxml", "Leçons");
    }

    // === Loader ===
    private void loadContent(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                contentArea.getChildren().clear();
                Label errorLabel = new Label("La page " + title + " n'est pas encore disponible.");
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                contentArea.getChildren().add(errorLabel);
                pageTitle.setText(title);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            content.setUserData(loader.getController());

            pageTitle.setText(title);
            searchField.setPromptText("Rechercher " + title.toLowerCase() + "...");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page : " + e.getMessage());
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
