package tn.esprit.controllers.Formation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import tn.esprit.controllers.MainLayoutController;
import tn.esprit.controllers.NavBar;
import tn.esprit.controllers.lecon.LeconByFormationController;
import tn.esprit.models.Formation;

import java.io.File;
import java.io.IOException;

public class FormationDetailsController {

    @FXML private ImageView imageView;
    @FXML private Label titreLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label categorieLabel;
    @FXML private Label dureeLabel;
    @FXML private Label niveauLabel;
    @FXML private Label prixLabel;
    @FXML
    private Button backButton;
    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    private void initialize() {
        backButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/GetAllFormationFront.fxml"));
                Parent allFormations = loader.load();

                // Replace center content
                mainLayoutController.getContentArea().getChildren().setAll(allFormations);

            } catch (IOException e) {
                e.printStackTrace();
//                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la liste des formations.");
            }
        });
    }





    private Formation formation;

    public void setFormation(Formation formation) {
        this.formation = formation;
        updateUI();
    }

    private void updateUI() {
        if (formation == null) return;

        titreLabel.setText(formation.getTitre());
        descriptionLabel.setText(formation.getDescription());
        categorieLabel.setText(formation.getCategorie() != null ? formation.getCategorie().getNom() : "N/A");
        dureeLabel.setText(formation.getDuree());
        niveauLabel.setText(formation.getNiveau());
        prixLabel.setText(formation.getPrix() + " TND");

        File imageFile = new File("images/formations/" + formation.getImageName());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        }
    }


    @FXML
    private void handleAccessFormation(ActionEvent event) {
        try {
            // 1. Load MainLayout.fxml
            FXMLLoader mainLayoutLoader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
            Parent mainLayoutRoot = mainLayoutLoader.load();
            MainLayoutController mainLayoutController = mainLayoutLoader.getController();

            // 2. Load LeconsByFormation.fxml
            FXMLLoader leconsLoader = new FXMLLoader(getClass().getResource("/Lecon/LeconsByFormation.fxml"));
            Parent leconsContent = leconsLoader.load();
            LeconByFormationController leconController = leconsLoader.getController();

            // 3. Pass data to the Leçon controller
            leconController.setFormation(formation);
            leconController.setMainLayoutController(mainLayoutController);

            // 4. Inject the Leçons view into content area
            mainLayoutController.getContentArea().getChildren().setAll(leconsContent);

            // 5. Set the scene root to the main layout (from button source)
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(mainLayoutRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
