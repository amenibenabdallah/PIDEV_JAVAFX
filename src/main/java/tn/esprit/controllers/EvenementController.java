package tn.esprit.controllers;

import com.google.zxing.WriterException;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import tn.esprit.models.Evenement;
import tn.esprit.services.EvenementService;
import tn.esprit.utils.QRCodeUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class EvenementController implements Searchable {

    @FXML private TextField titreField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private TextField emplacementField; // Added for emplacement
    @FXML private ImageView photoPreview;
    @FXML private ImageView qrCodePreview; // Added for QR code preview
    @FXML private VBox evenementCards;
    @FXML private SplitPane splitPane;
    @FXML private VBox formPanel;

    private EvenementService evenementService;
    private ObservableList<Evenement> evenementList;
    private ObservableList<Evenement> filteredEvenementList;
    private String selectedImagePath;
    private Evenement selectedEvenement;
    private boolean isFormVisible = false;

    @FXML
    public void initialize() {
        evenementService = new EvenementService();
        evenementList = FXCollections.observableArrayList(evenementService.getAll());
        filteredEvenementList = FXCollections.observableArrayList(evenementList);
        refreshCards(filteredEvenementList);

        splitPane.setDividerPositions(1.0);
        formPanel.setPrefWidth(0);
    }

    private void refreshCards(ObservableList<Evenement> events) {
        evenementCards.getChildren().clear();
        for (Evenement evenement : events) {
            VBox card = createEvenementCard(evenement);
            evenementCards.getChildren().add(card);
        }
    }

    private VBox createEvenementCard(Evenement evenement) {
        VBox card = new VBox(5);
        card.getStyleClass().add("evenement-card");

        Label titleLabel = new Label(evenement.getTitre());
        titleLabel.getStyleClass().add("evenement-card-title");

        Label dateLabel = new Label(evenement.getDate().toString());
        dateLabel.getStyleClass().add("evenement-card-date");

        Label descriptionLabel = new Label(evenement.getDescription());
        descriptionLabel.getStyleClass().add("evenement-card-description");

        Label emplacementLabel = new Label("Lieu: " + (evenement.getEmplacement() != null ? evenement.getEmplacement() : "Non spécifié"));
        emplacementLabel.getStyleClass().add("evenement-card-emplacement");

        ImageView cardImageView = new ImageView();
        if (evenement.getPhoto() != null && !evenement.getPhoto().isEmpty()) {
            try {
                String absolutePath = new File("src/main/resources" + evenement.getPhoto()).getAbsolutePath();
                File imageFile = new File(absolutePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    cardImageView.setImage(image);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de charger l'image : " + e.getMessage());
            }
        }
        cardImageView.getStyleClass().add("evenement-card-image");

        ImageView qrCodeView = new ImageView();
        if (evenement.getEmplacement() != null && !evenement.getEmplacement().isEmpty()) {
            try {
                Image qrCodeImage = QRCodeUtil.generateQRCode(evenement.getEmplacement(), 100, 100);
                qrCodeView.setImage(qrCodeImage);
            } catch (WriterException e) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de générer le QR code : " + e.getMessage());
            }
        }
        qrCodeView.getStyleClass().add("evenement-card-qrcode");

        HBox buttonBox = new HBox(10);
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("evenement-button", "evenement-button-update");
        editButton.setOnAction(e -> {
            selectedEvenement = evenement;
            titreField.setText(evenement.getTitre());
            datePicker.setValue(evenement.getDate());
            descriptionArea.setText(evenement.getDescription());
            emplacementField.setText(evenement.getEmplacement());
            selectedImagePath = evenement.getPhoto();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                try {
                    String absolutePath = new File("src/main/resources" + selectedImagePath).getAbsolutePath();
                    File imageFile = new File(absolutePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        photoPreview.setImage(image);
                    } else {
                        photoPreview.setImage(null);
                    }
                } catch (Exception ex) {
                    photoPreview.setImage(null);
                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de charger l'image : " + ex.getMessage());
                }
            } else {
                photoPreview.setImage(null);
            }
            try {
                Image qrCodeImage = evenement.getEmplacement() != null ?
                        QRCodeUtil.generateQRCode(evenement.getEmplacement(), 100, 100) : null;
                qrCodePreview.setImage(qrCodeImage);
            } catch (WriterException ex) {
                qrCodePreview.setImage(null);
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de générer le QR code : " + ex.getMessage());
            }
            toggleForm();
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("evenement-button", "evenement-button-delete");
        deleteButton.setOnAction(e -> {
            evenementService.delete(evenement.getId());
            evenementList.remove(evenement);
            filteredEvenementList.remove(evenement);
            refreshCards(filteredEvenementList);
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Évènement supprimé avec succès.");
        });

        buttonBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(cardImageView, titleLabel, dateLabel, descriptionLabel, emplacementLabel, qrCodeView, buttonBox);
        return card;
    }

    @FXML
    private void toggleForm() {
        isFormVisible = !isFormVisible;
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), formPanel);
        if (isFormVisible) {
            splitPane.setDividerPositions(0.7);
            formPanel.setPrefWidth(400);
            transition.setToX(0);
        } else {
            splitPane.setDividerPositions(1.0);
            formPanel.setPrefWidth(0);
            transition.setToX(400);
            clearFields();
        }
        transition.play();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(titreField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String targetDir = "src/main/resources/images";
                File dir = new File(targetDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = selectedFile.getName();
                Path targetPath = Paths.get(targetDir, fileName);
                Files.copy(selectedFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String runtimeDir = "target/classes/images";
                File runtimeDirFile = new File(runtimeDir);
                if (!runtimeDirFile.exists()) {
                    runtimeDirFile.mkdirs();
                }
                Path runtimePath = Paths.get(runtimeDir, fileName);
                Files.copy(selectedFile.toPath(), runtimePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                selectedImagePath = "/images/" + fileName;

                File imageFile = new File(targetDir + "/" + fileName);
                Image image = new Image(imageFile.toURI().toString());
                photoPreview.setImage(image);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            if (selectedEvenement == null) {
                Evenement evenement = new Evenement();
                evenement.setTitre(titreField.getText());
                evenement.setDate(datePicker.getValue());
                evenement.setDescription(descriptionArea.getText());
                evenement.setEmplacement(emplacementField.getText());
                evenement.setPhoto(selectedImagePath);
                evenementService.add(evenement);
                evenementList.add(evenement);
            } else {
                selectedEvenement.setTitre(titreField.getText());
                selectedEvenement.setDate(datePicker.getValue());
                selectedEvenement.setDescription(descriptionArea.getText());
                selectedEvenement.setEmplacement(emplacementField.getText());
                selectedEvenement.setPhoto(selectedImagePath);
                evenementService.update(selectedEvenement);
            }
            filteredEvenementList.setAll(evenementList);
            refreshCards(filteredEvenementList);
            toggleForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", selectedEvenement == null ? "Évènement ajouté avec succès." : "Évènement mis à jour avec succès.");
        }
    }

    @FXML
    private void handleCancel() {
        toggleForm();
    }

    @Override
    public void handleSearch(String searchText) {
        if (searchText.isEmpty()) {
            filteredEvenementList.setAll(evenementList);
        } else {
            filteredEvenementList.setAll(evenementList.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(searchText) ||
                            e.getDescription().toLowerCase().contains(searchText) ||
                            (e.getPhoto() != null && e.getPhoto().toLowerCase().contains(searchText)) ||
                            (e.getEmplacement() != null && e.getEmplacement().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList()));
        }
        refreshCards(filteredEvenementList);
    }

    private boolean validateInput() {
        if (titreField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est requis.");
            return false;
        }
        if (datePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date est requise.");
            return false;
        }
        if (descriptionArea.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est requise.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        titreField.clear();
        datePicker.setValue(null);
        descriptionArea.clear();
        emplacementField.clear();
        photoPreview.setImage(null);
        qrCodePreview.setImage(null);
        selectedImagePath = null;
        selectedEvenement = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEmplacementChange() {
        String emplacement = emplacementField.getText();
        try {
            Image qrCodeImage = emplacement != null && !emplacement.isEmpty() ?
                    QRCodeUtil.generateQRCode(emplacement, 100, 100) : null;
            qrCodePreview.setImage(qrCodeImage);
        } catch (WriterException e) {
            qrCodePreview.setImage(null);
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de générer le QR code : " + e.getMessage());
        }
    }
}