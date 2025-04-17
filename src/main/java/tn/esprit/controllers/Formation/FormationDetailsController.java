package tn.esprit.controllers.Formation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    @FXML
    private void initialize() {
        backButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/GetAllFormationFront.fxml"));
                Parent root = loader.load();
                backButton.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
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
    private void handleAccessFormation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Lecon/LeconsByFormation.fxml"));
            Parent root = loader.load();

            tn.esprit.controllers.lecon.LeconByFormationController controller = loader.getController();
            controller.setFormation(formation);

            backButton.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
