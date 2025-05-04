package tn.esprit.controllers.Formation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.json.JSONArray;
import tn.esprit.controllers.MainLayoutController;
import tn.esprit.models.Formation;
import tn.esprit.services.FormationService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GetAllFormationFront implements Initializable {
    @FXML
    private TextField searchField;

    @FXML
    private Button searchBtn;
    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button ajouterFormationBtn;

    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    private void showPage(int page) {
        cardsContainer.getChildren().clear();

        int fromIndex = (page - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allFormations.size());

        List<Formation> pageItems = allFormations.subList(fromIndex, toIndex);
        for (Formation formation : pageItems) {
            VBox card = createFormationCard(formation);
            cardsContainer.getChildren().add(card);
        }

        pageLabel.setText("Page " + page);
        prevBtn.setDisable(page == 1);
        nextBtn.setDisable(toIndex >= allFormations.size());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadFormationCards();
    }
    private List<Formation> recommendFormations(String userInput) {
        List<Formation> recommended = new ArrayList<>();

        try {
            URL url = new URL("http://127.0.0.1:5000/recommend");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{\"query\": \"" + userInput + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {

                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse JSON response (uses org.json or GSON, choose your lib)
                JSONArray jsonArray = new JSONArray(response.toString());

                FormationService fs = new FormationService();
                for (int i = 0; i < jsonArray.length(); i++) {
                    int id = jsonArray.getJSONObject(i).getInt("id");
                    Formation formation = fs.getById(id);  // make sure this method exists
                    if (formation != null) {
                        recommended.add(formation);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return recommended;
    }
    @FXML
    private void handleRecommendation() {
        String input = searchField.getText();
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        List<Formation> recommended = recommendFormations(input);

        cardsContainer.getChildren().clear();
        for (Formation f : recommended) {
            VBox card = createFormationCard(f);
            cardsContainer.getChildren().add(card);
        }
    }
    @FXML
    private void handleNextPage() {
        if ((currentPage * itemsPerPage) < allFormations.size()) {
            currentPage++;
            showPage(currentPage);
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage);
        }
    }


    private void loadFormationCards() {
        FormationService fs = new FormationService();
        try {
            allFormations = fs.getAll();  // stocke tout ici
            showPage(currentPage);        // affiche la page actuelle
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createFormationCard(Formation formation) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPrefHeight(330);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        // Image
        ImageView imageView = new ImageView();
        File imageFile = new File("images/formations/" + formation.getImageName());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Titre
        HBox titreBox = new HBox(5);
        Label titreText = new Label("Titre :");
        titreText.setStyle("-fx-font-weight: bold;");
        Label titreValue = new Label(formation.getTitre());
        titreBox.getChildren().addAll(titreText, titreValue);

        // Catégorie
        HBox categorieBox = new HBox(5);
        Label catText = new Label("Catégorie :");
        catText.setStyle("-fx-font-weight: bold;");
        Label catValue = new Label(formation.getCategorie().getNom());
        categorieBox.getChildren().addAll(catText, catValue);

        // Description
        HBox descBox = new HBox(5);
        Label descText = new Label("Description :");
        descText.setStyle("-fx-font-weight: bold;");
        Label descValue = new Label(formation.getDescription());
        descValue.setWrapText(true);
        descBox.getChildren().addAll(descText, descValue);

        // Prix
        HBox prixBox = new HBox(5);
        Label prixText = new Label("Prix :");
        prixText.setStyle("-fx-font-weight: bold;");
        Label prixValue = new Label(formation.getPrix() + " TND");
        prixBox.getChildren().addAll(prixText, prixValue);

        // Bouton Détail
        Button viewDetailsButton = new Button("Voir Détails");
        viewDetailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        viewDetailsButton.setOnAction(event -> {
            try {
                // 1. Load MainLayout
                FXMLLoader mainLayoutLoader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
                Parent mainLayoutRoot = mainLayoutLoader.load();
                MainLayoutController mainLayoutController = mainLayoutLoader.getController();

                // 2. Load FormationDetails
                FXMLLoader detailsLoader = new FXMLLoader(getClass().getResource("/Formation/FormationDetails.fxml"));
                Parent detailsContent = detailsLoader.load();
                FormationDetailsController detailsController = detailsLoader.getController();

                // 3. Set the Formation data and link the controller
                detailsController.setFormation(formation); // your model object
                detailsController.setMainLayoutController(mainLayoutController);

                // 4. Inject the details into contentArea
                mainLayoutController.getContentArea().getChildren().setAll(detailsContent);

                // 5. Set the scene root to the main layout
                Scene scene = viewDetailsButton.getScene();
                scene.setRoot(mainLayoutRoot);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        card.getChildren().addAll(imageView, titreBox, categorieBox, descBox, prixBox, viewDetailsButton);

        return card;
    }
    private final int itemsPerPage = 6;
    private int currentPage = 1;
    private List<Formation> allFormations;

    @FXML
    private Button prevBtn, nextBtn;

    @FXML
    private Label pageLabel;

    @FXML
    private void handleAjouterFormation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/addFormation.fxml"));
            Parent root = loader.load();
            Scene scene = ajouterFormationBtn.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
