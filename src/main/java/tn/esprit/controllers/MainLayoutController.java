package tn.esprit.controllers;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.InscriptionCours;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private AnchorPane mainContainer;

    @FXML
    private StackPane sidebarPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label off;

    @FXML
    private Label on;

    @FXML
    public void initialize() {
        loadWelcomePage();
    }

    @FXML
    private void handleOff() {
        javafx.animation.KeyValue widthValue = new javafx.animation.KeyValue(sidebarPane.prefWidthProperty(), 60);
        javafx.animation.KeyFrame frame = new javafx.animation.KeyFrame(Duration.millis(300), widthValue);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(frame);
        timeline.play();

        off.setVisible(false);
        on.setVisible(true);
    }


    @FXML
    private void handleOn() {
        javafx.animation.KeyValue widthValue = new javafx.animation.KeyValue(sidebarPane.prefWidthProperty(), 240);
        javafx.animation.KeyFrame frame = new javafx.animation.KeyFrame(Duration.millis(300), widthValue);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(frame);
        timeline.play();

        on.setVisible(false);
        off.setVisible(true);
    }


    @FXML
    private void handleHome() {
        loadWelcomePage(); // Maintenant, Home recharge la page de bienvenue
    }

    @FXML
    private void handleModifier() {
        String role = SessionManager.getInstance().getUtilisateurConnecte().getRole();
        switch (role) {
            case "APPRENANT" -> loadFXML("/ProfilApprenant.fxml");
            case "INSTRUCTEUR" -> loadFXML("/ProfilInstructeur.fxml");
            case "ADMIN" -> loadFXML("/AdminDashboard.fxml");
            default -> showAlert("Erreur", "Rôle inconnu : accès refusé.");
        }
    }

    @FXML
    private void handleFeedback() {
        loadFXML("/ListAvis.fxml");
    }
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            mainContainer.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la déconnexion !");
        }
    }


    public void loadWelcomePage() {
        loadFXML("/Welcome.fxml"); // <-- Une nouvelle vue de Bienvenue
    }

    private void loadFXML(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue : " + fxmlFile);
        }
    }

    // Méthode pour charger l'interface d'inscription

    @FXML
    private void handleInscriptionCours(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/InscriptionCoursview.fxml"));
            Parent view = loader.load();
            InscriptionCoursViewController controller = loader.getController();
            controller.setMainLayoutController(this); // Passer la référence
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface d'inscription.");
        }
    }
    // Méthode pour charger l'interface de paiement
    public void loadPaymentView(double montant, InscriptionCours inscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaymentView.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();
            paymentController.setAmount(montant);
            paymentController.setInscription(inscription);
            paymentController.setMainLayoutController(this); // Passer la référence
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de paiement.");
        }
    }

    // Méthode pour charger l'interface de vérification
    public void loadVerificationPaiement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VerificationPaiementView.fxml"));
            Parent root = loader.load();
            VerificationPaiementController verificationController = loader.getController();
            verificationController.setMainLayoutController(this); // Passer la référence
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de vérification.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}