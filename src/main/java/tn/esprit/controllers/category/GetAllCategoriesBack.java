package tn.esprit.controllers.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.services.CategorieService;
import java.sql.SQLException;
import java.util.List;

public class GetAllCategoriesBack {
    @FXML
    private Button addCategoryButton;


    @FXML
    private VBox categoriesVBox;  // VBox to hold all category cards

    private ObservableList<Categorie> categories;
    private CategorieService categorieService;

    public GetAllCategoriesBack() {
        categorieService = new CategorieService();
    }

    public void initialize() {
        try {
            List<Categorie> categoriesFromDb = categorieService.getAll();
            categories = FXCollections.observableArrayList(categoriesFromDb);
            for (Categorie category : categories) {
                HBox categoryCard = createCategoryCard(category);
                categoriesVBox.getChildren().add(categoryCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createCategoryCard(Categorie category) {
        // Text content
        Label categoryName = new Label(category.getNom());
        categoryName.getStyleClass().add("category-name");

        Label categoryDescription = new Label(category.getDescription());
        categoryDescription.getStyleClass().add("category-description");

        VBox textContent = new VBox(5, categoryName, categoryDescription);

        // Create edit button with larger icon
        Button editButton = new Button();
        editButton.getStyleClass().addAll("icon-button", "edit-button");
        try {
            ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/edit.png")));
            editIcon.setFitWidth(24);
            editIcon.setFitHeight(24);
            editButton.setGraphic(editIcon);
        } catch (Exception e) {
            // Fallback to text if icon not found
            editButton.setText("Edit");
            editButton.setStyle("-fx-text-fill: #4CAF50;");
        }
        editButton.setOnAction(event -> handleEditCategory(category));

        // Create delete button with larger icon
        Button deleteButton = new Button();
        deleteButton.getStyleClass().addAll("icon-button", "delete-button");
        try {
            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/delete.png")));
            deleteIcon.setFitWidth(24);
            deleteIcon.setFitHeight(24);
            deleteButton.setGraphic(deleteIcon);
        } catch (Exception e) {
            // Fallback to text if icon not found
            deleteButton.setText("Delete");
            deleteButton.setStyle("-fx-text-fill: #F44336;");
        }
        deleteButton.setOnAction(event -> handleDeleteCategory(category));

        // Button container
        HBox buttonBox = new HBox(10, editButton, deleteButton);
        buttonBox.setStyle("-fx-alignment: center-right;");

        // Spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Card container
        HBox card = new HBox(10, textContent, spacer, buttonBox);
        card.getStyleClass().add("category-card");
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }
    private void handleEditCategory(Categorie category) {
        Dialog<Categorie> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Update the selected category");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        TextField nameField = new TextField(category.getNom());
        nameField.setPromptText("Category name");

        TextArea descriptionField = new TextArea(category.getDescription());
        descriptionField.setPromptText("Category description");
        descriptionField.setPrefRowCount(4);
        descriptionField.setWrapText(true);

        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");

        VBox dialogContent = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionField,
                validationLabel
        );
        dialogContent.setStyle("-fx-padding: 20;");
        dialog.getDialogPane().setContent(dialogContent);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);  // Initially disable

        // Validation en temps réel
        Runnable validate = () -> {
            String name = nameField.getText().trim();
            String desc = descriptionField.getText().trim();

            if (name.isEmpty()) {
                validationLabel.setText("Title cannot be empty.");
                saveButton.setDisable(true);
            } else if (desc.length() < 8) {
                validationLabel.setText("Description must be at least 8 characters.");
                saveButton.setDisable(true);
            } else {
                validationLabel.setText("");
                saveButton.setDisable(false);
            }
        };

        // Attach the validation to input changes
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());

        // Trigger once at start
        validate.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                category.setNom(nameField.getText().trim());
                category.setDescription(descriptionField.getText().trim());
                return category;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCategory -> {
            try {
                categorieService.update(updatedCategory);
                refreshCategories();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Category/addCategory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une catégorie");
            stage.setScene(new Scene(root));

            // Refresh when the Add window is closed
            stage.setOnHidden(event -> refreshCategories());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void handleDeleteCategory(Categorie category) {
        // Show a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Are you sure you want to delete this category?");
        alert.setContentText("Category: " + category.getNom());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete category from the database
                    categorieService.delete(category.getId());
                    // Refresh the UI
                    refreshCategories();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void refreshCategories() {
        try {
            // Re-fetch the categories from the database
            List<Categorie> updatedCategories = categorieService.getAll();
            categories.setAll(updatedCategories);  // Update the observable list
            categoriesVBox.getChildren().clear();  // Clear the old items in the VBox
            for (Categorie category : categories) {
                HBox categoryCard = createCategoryCard(category);
                categoriesVBox.getChildren().add(categoryCard);  // Add updated cards
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
