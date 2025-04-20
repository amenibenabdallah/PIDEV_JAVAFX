package tn.esprit.controllers.lecon;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.models.Formation;
import tn.esprit.models.Lecon;
import tn.esprit.services.LeconService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeconByFormationController {

    @FXML private Label formationTitleLabel;
    @FXML private TabPane tabPaneLecons;

    private Formation formation;
    private final LeconService leconService = new LeconService();

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
        if (lecon != null) {
            String path = "C:\\Users\\walid\\Downloads\\leçon_" + lecon.getId() + ".pdf";

            try {
                PdfWriter writer = new PdfWriter(path);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.setMargins(30, 30, 30, 30);

                // Fonts & Styling
                PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                // Title centered
                Paragraph header = new Paragraph("Leçon : " + lecon.getTitre())
                        .setFont(bold)
                        .setFontSize(20)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(header);

                // Style block for section titles and values
                float boxWidth = 500f;

                // --- Titre ---
                document.add(new Paragraph("Titre")
                        .setFont(bold)
                        .setFontSize(14)
                        .setBackgroundColor(new DeviceRgb(240, 240, 240))
                        .setPadding(5));
                document.add(new Paragraph(lecon.getTitre())
                        .setFont(regular)
                        .setFontSize(12)
                        .setBackgroundColor(new DeviceRgb(250, 250, 250))
                        .setPadding(5)
                        .setMarginBottom(15));

                // --- Contenu ---
                document.add(new Paragraph("Contenu")
                        .setFont(bold)
                        .setFontSize(14)
                        .setBackgroundColor(new DeviceRgb(240, 240, 240))
                        .setPadding(5));
                document.add(new Paragraph(lecon.getContenu())
                        .setFont(regular)
                        .setFontSize(12)
                        .setBackgroundColor(new DeviceRgb(250, 250, 250))
                        .setPadding(5)
                        .setMarginBottom(15));

                // --- Date de création ---
                document.add(new Paragraph("Date de Création")
                        .setFont(bold)
                        .setFontSize(14)
                        .setBackgroundColor(new DeviceRgb(240, 240, 240))
                        .setPadding(5));
                document.add(new Paragraph(lecon.getDateCreation().toString())
                        .setFont(regular)
                        .setFontSize(12)
                        .setBackgroundColor(new DeviceRgb(250, 250, 250))
                        .setPadding(5)
                        .setMarginBottom(30));

                // --- Quote Footer ---
                Paragraph quote = new Paragraph("\"L'éducation est l'arme la plus puissante que vous puissiez utiliser pour changer le monde.\" - Nelson Mandela")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFont(regular)
                        .setFontSize(11)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(new DeviceRgb(38, 50, 56)) // Dark blue
                        .setPadding(15)
                        .setBorderRadius(new BorderRadius(5));
                document.add(quote);
                document.add(new Paragraph("FORMINI\n18, rue de l'Usine\nZI Aéroport Charguia\nII 2035 Ariana\nPhone: (+216) 58 26 64 36\nEmail: formini@esprit.tn")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(10));
                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le PDF a été téléchargé avec succès !");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la création du PDF.");
            }
        }
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
            Parent root = loader.load();
            tabPaneLecons.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
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
