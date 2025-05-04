package tn.esprit.controllers.Formation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.models.Formation;
import tn.esprit.services.CategorieService;
import tn.esprit.services.FormationService;
import tn.esprit.utils.EmailSender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AddFormation {

    @FXML
    private TextField titleTextField, dureeTextField, niveauTextField, prixTextField, imageNameTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private DatePicker dateCreationPicker;

    @FXML
    private ComboBox<Categorie> categorieComboBox;

    @FXML
    private Label errorLabel;

    private FormationService formationService = new FormationService();
    private CategorieService categorieService = new CategorieService();

    private File selectedImageFile = null; // holds the chosen file

    public void initialize() throws SQLException {
        List<Categorie> categories = categorieService.getAll();
        ObservableList<Categorie> observableCategories = FXCollections.observableArrayList(categories);
        categorieComboBox.setItems(observableCategories);
    }

    @FXML
    private void handleImageChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Formation Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        selectedImageFile = fileChooser.showOpenDialog(new Stage());

        if (selectedImageFile != null) {
            imageNameTextField.setText(selectedImageFile.getName()); // Just show the original file name
        }
    }

    @FXML
    private void handleAddFormation() throws SQLException {
        String titre = titleTextField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        String duree = dureeTextField.getText().trim();
        String niveau = niveauTextField.getText().trim();
        String imageName = "";
        float prix;
        LocalDate dateCreation = dateCreationPicker.getValue();
        Categorie categorie = categorieComboBox.getValue();

        try {
            prix = Float.parseFloat(prixTextField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Le prix doit être un nombre valide.");
            return;
        }

        // Vérifier que tous les champs sont remplis
        if (titre.isEmpty() || description.isEmpty() || duree.isEmpty() || niveau.isEmpty() || dateCreation == null || categorie == null || selectedImageFile == null) {
            errorLabel.setText("Veuillez remplir tous les champs et sélectionner une image.");
            return;
        }

        // Vérifier que la description contient au moins 8 caractères
        if (description.length() < 8) {
            errorLabel.setText("La description doit contenir au moins 8 caractères.");
            return;
        }

        // Vérifier que la date de création est celle d'aujourd'hui
        if (!dateCreation.equals(LocalDate.now())) {
            errorLabel.setText("La date de création doit être celle d'aujourd'hui.");
            return;
        }
        try {
            // Prepare destination directory inside the project
            File destinationDir = new File("images/formations/");
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }

            // Generate unique name
            String fileExtension = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf('.'));
            String uuid = UUID.randomUUID().toString();
            String newFileName = uuid + fileExtension;

            File destFile = new File(destinationDir, newFileName);
            Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Save just the relative image name
            imageName = newFileName;

        } catch (IOException e) {
            errorLabel.setText("Error saving image.");
            e.printStackTrace();
            return;
        }

        Formation formation = new Formation(0, titre, description, duree, niveau, dateCreation, prix, imageName);
        formation.setCategorie(categorie);
        formationService.add(formation);
        String emailBody = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>" +
                "<div style='background-color: #ffffff; padding: 20px; border-radius: 10px; max-width: 600px; margin: auto; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #2c3e50;'>🎉 Nouvelle formation disponible sur Formini !</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Une <strong>nouvelle formation</strong> vient d’être ajoutée à notre plateforme Formini.</p>" +
                "<p><a href='https://formini.tn/formations' style='background-color: #1abc9c; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>Découvrir la formation</a></p>" +
                "<p style='color: #7f8c8d; font-size: 12px;'>Merci de faire partie de la communauté Formini. Nous vous souhaitons un excellent apprentissage !</p>" +
                "<hr style='border: none; border-top: 1px solid #ecf0f1;'/>" +
                "<p style='font-size: 12px; color: #bdc3c7;'>© 2025 Formini. Tous droits réservés.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        EmailSender.sendEmail("gobjiwalid1@gmail.com", "Nouvelle formation ajoutée sur Formini", emailBody);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "La formation a été ajoutée avec succès !");

    }
        private void showAlert(Alert.AlertType alertType, String title, String content) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/getAllFormations.fxml")); // à adapter si ton fichier s'appelle différemment
            Parent root = loader.load();
            titleTextField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
