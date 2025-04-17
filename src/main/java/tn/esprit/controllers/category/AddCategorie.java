package tn.esprit.controllers.category;



import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.services.CategorieService;
import tn.esprit.models.Categorie;


public class AddCategorie {

    @FXML
    private TextField categoryNameTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Label categoryErrorLabel;

    private final CategorieService categorieService = new CategorieService();

    @FXML
    private void initialize() {
        // Any initial setup for the form can be done here (if needed)
    }

    @FXML
    private void handleAddCategory() {
        String categoryName = categoryNameTextField.getText();
        String description = descriptionTextArea.getText();

        // Validation
        if (categoryName == null || categoryName.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de la catégorie est requis.");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est requise.");
            return;
        }

        if (description.trim().length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description doit contenir au moins 8 caractères.");
            return;
        }

        Categorie categorie = new Categorie(categoryName.trim(), description.trim());

        try {
            categorieService.add(categorie);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès.");
            categoryNameTextField.clear();
            descriptionTextArea.clear();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
