package tn.esprit.controllers.lecon;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.controllers.MainLayoutController;
import tn.esprit.models.Formation;
import tn.esprit.models.Lecon;
import tn.esprit.services.LeconService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeconByFormationController {

    @FXML private Label formationTitleLabel;
    @FXML private TabPane tabPaneLecons;

    private Formation formation;
    private final LeconService leconService = new LeconService();
    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        formationTitleLabel.setText("Leçons de la formation \"" + formation.getTitre() + "\"");
        loadLecons();
    }

    private void loadLecons() {
        tabPaneLecons.getTabs().clear();
        try {
            List<Lecon> lecons = leconService.getByFormation(formation.getId());
            for (Lecon lecon : lecons) {
                Tab tab = new Tab(lecon.getTitre());

                VBox contentBox = new VBox(10);
                contentBox.setStyle("-fx-padding: 15");

                Label dateLabel = new Label("Créé le " + lecon.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                Button viewButton = new Button("Voir les détails");
                viewButton.setOnAction(e -> showLeconDetails(lecon));

                Button downloadButton = new Button("Télécharger");
                downloadButton.setOnAction(e -> exportPrescriptionToPDF(lecon));

                contentBox.getChildren().addAll(dateLabel, viewButton, downloadButton);
                tab.setContent(contentBox);

                tabPaneLecons.getTabs().add(tab);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void exportPrescriptionToPDF(Lecon lecon) {
        if (lecon == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La leçon est vide.");
            return;
        }

        String fileName = "leçon_" + lecon.getId() + ".pdf";
        String exportPath = "C:\\Users\\walid\\Downloads\\";
        Path exportDir = Path.of(exportPath);
        Path fullFilePath = exportDir.resolve(fileName);

        try {
            Files.createDirectories(exportDir);
            PdfWriter writer = new PdfWriter(fullFilePath.toString());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(30, 30, 30, 30);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            try (InputStream logoStream = getClass().getResourceAsStream("/icons/formini.png")) {
                if (logoStream != null) {
                    byte[] imageBytes = logoStream.readAllBytes();
                    ImageData logoData = ImageDataFactory.create(imageBytes);
                    Image logo = new Image(logoData).setWidth(100).setMarginBottom(20);
                    document.add(logo);
                } else {
                    System.err.println("Logo non trouvé dans les ressources.");
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement logo : " + e.getMessage());
            }

            Paragraph header = new Paragraph("Leçon : " + lecon.getTitre())
                    .setFont(bold)
                    .setFontSize(20)
                    .setFontColor(ColorConstants.RED)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);

            document.add(sectionTitle("Titre", bold));
            document.add(sectionContent(lecon.getTitre(), regular));

            document.add(sectionTitle("Contenu", bold));
            document.add(sectionContent(lecon.getContenu(), regular));

            document.add(sectionTitle("Date de Création", bold));
            document.add(sectionContent(lecon.getDateCreation().toString(), regular));

            Paragraph quote = new Paragraph("\"L'éducation est l'arme la plus puissante que vous puissiez utiliser pour changer le monde.\" - Nelson Mandela")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(regular)
                    .setFontSize(11)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(new DeviceRgb(38, 50, 56))
                    .setPadding(15)
                    .setMarginTop(20)
                    .setMarginBottom(20);
            document.add(quote);

            document.add(new Paragraph("FORMINI\n18, rue de l'Usine\nZI Aéroport Charguia II 2035 Ariana\nPhone: (+216) 58 26 64 36\nEmail: formini@esprit.tn")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(regular)
                    .setFontSize(10)
                    .setMarginTop(30));

            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le PDF a été téléchargé avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur génération PDF : " + e.getMessage());
        }
    }

    private Paragraph sectionTitle(String title, PdfFont font) {
        return new Paragraph(title)
                .setFont(font)
                .setFontSize(14)
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setPadding(5);
    }

    private Paragraph sectionContent(String content, PdfFont font) {
        return new Paragraph(content)
                .setFont(font)
                .setFontSize(12)
                .setBackgroundColor(new DeviceRgb(250, 250, 250))
                .setPadding(5)
                .setMarginBottom(15);
    }

    private void showLeconDetails(Lecon lecon) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lecon.getTitre());
        alert.setHeaderText("Contenu de la leçon");
        alert.setContentText(lecon.getContenu());
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Formation/GetAllFormationFront.fxml"));
            Parent allFormations = loader.load();
            mainLayoutController.getContentArea().getChildren().setAll(allFormations);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la liste des formations.");
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
