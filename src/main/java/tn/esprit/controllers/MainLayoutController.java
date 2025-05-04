package tn.esprit.controllers;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import tn.esprit.models.InscriptionCours;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class MainLayoutController {

    @FXML private AnchorPane mainContainer;
    @FXML private StackPane sidebarPane;
    @FXML private StackPane contentArea;
    @FXML private Label off;
    @FXML private Label on;
    @FXML private Button chatBotButton;

    private boolean isSidebarCollapsed = false;

    @FXML
    private void initialize() {
        // Charger la page de bienvenue par défaut
        loadWelcomePage();
        // Animation pour le bouton du chatbot
        setupChatBotButtonAnimations();
    }

    private void setupChatBotButtonAnimations() {
        // Effet au survol
        chatBotButton.setOnMouseEntered(event -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), chatBotButton);
            scaleIn.setToX(1.1);
            scaleIn.setToY(1.1);
            scaleIn.play();
            chatBotButton.setStyle("-fx-background-color: rgba(52, 152, 219, 0.1); -fx-effect: dropshadow(three-pass-box, rgba(52, 152, 219, 0.3), 8, 0, 0, 2);");
        });

        chatBotButton.setOnMouseExited(event -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), chatBotButton);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
            chatBotButton.setStyle("-fx-background-color: transparent; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        });

        chatBotButton.setOnMousePressed(event -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), chatBotButton);
            scalePress.setToX(0.9);
            scalePress.setToY(0.9);
            scalePress.play();
            chatBotButton.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        });

        chatBotButton.setOnMouseReleased(event -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), chatBotButton);
            scaleRelease.setToX(1.0);
            scaleRelease.setToY(1.0);
            scaleRelease.play();
            chatBotButton.setStyle("-fx-background-color: rgba(52, 152, 219, 0.1); -fx-effect: dropshadow(three-pass-box, rgba(52, 152, 219, 0.3), 8, 0, 0, 2);");
        });
    }

    @FXML
    private void handleOff() {
        javafx.animation.KeyValue widthValue = new javafx.animation.KeyValue(sidebarPane.prefWidthProperty(), 60);
        javafx.animation.KeyFrame frame = new javafx.animation.KeyFrame(Duration.millis(300), widthValue);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(frame);
        timeline.setOnFinished(event -> {
            off.setVisible(false);
            on.setVisible(true);
            AnchorPane.setLeftAnchor(contentArea, 60.0);
            isSidebarCollapsed = true;
        });
        timeline.play();
    }

    @FXML
    private void handleOn() {
        javafx.animation.KeyValue widthValue = new javafx.animation.KeyValue(sidebarPane.prefWidthProperty(), 240);
        javafx.animation.KeyFrame frame = new javafx.animation.KeyFrame(Duration.millis(300), widthValue);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(frame);
        timeline.setOnFinished(event -> {
            on.setVisible(false);
            off.setVisible(true);
            AnchorPane.setLeftAnchor(contentArea, 240.0);
            isSidebarCollapsed = false;
        });
        timeline.play();
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
    

    private void handleLogout(ActionEvent event) {

        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            mainContainer.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la déconnexion : " + e.getMessage());
        }
    }


    public void loadWelcomePage() {
        loadFXML("/Welcome.fxml");
    }

    private void loadFXML(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue " + fxmlFile + " : " + e.getMessage());
        }
    }

    @FXML
    private void handleInscriptionCours(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/InscriptionCoursview.fxml"));
            Parent view = loader.load();
            InscriptionCoursViewController controller = loader.getController();
            controller.setMainLayoutController(this);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface d'inscription : " + e.getMessage());
        }
    }

    @FXML
    private void handleChatBot() {
        loadFXML("/ChatBotView.fxml");
    }

    public void loadPaymentView(double montant, InscriptionCours inscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaymentView.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();
            paymentController.setAmount(montant);
            paymentController.setInscription(inscription);
            paymentController.setMainLayoutController(this);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface de paiement : " + e.getMessage());
        }
    }

    public void loadVerificationPaiement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VerificationPaiementView.fxml"));
            Parent root = loader.load();
            VerificationPaiementController verificationController = loader.getController();
            verificationController.setMainLayoutController(this);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface de vérification : " + e.getMessage());
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