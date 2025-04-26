package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainInscription extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Démarrage de l'application...");

        try {
            // Chargement du fichier FXML
            Parent root = FXMLLoader.load(getClass().getResource("/PaymentView.fxml"));
            System.out.println("FXML chargé avec succès.");

            // Configuration de la scène
            primaryStage.setTitle("Formini Application");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}