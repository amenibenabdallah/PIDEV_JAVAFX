package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.InscriptionCours;
import tn.esprit.models.users;
import tn.esprit.models.Formation;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServiceInscriptionCours;
import tn.esprit.services.ServiceFormation;
import tn.esprit.services.ServicePromotion;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InscriptionCoursViewController {

    @FXML private TextField txtNomApprenant;
    @FXML private TextField txtCin;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> nomFormationComboBox;
    @FXML private ComboBox<String> typePaiementComboBox;
    @FXML private TextField txtApprenantId;
    @FXML private TextField txtFormationId;
    @FXML private Button btnAjouter;
    @FXML private TextField txtCodePromo;

    private final ServiceInscriptionCours service = new ServiceInscriptionCours();
    private final ServiceFormation formationService = new ServiceFormation();
    private final ServicePromotion servicePromotion = new ServicePromotion();
    private Map<String, Integer> formationMap; // Associe titre à id

    @FXML
    public void initialize() {
        typePaiementComboBox.setItems(FXCollections.observableArrayList(
                "Carte bancaire", "Espèces", "Virement"
        ));
        loadFormations();
        prefillFields();
    }

    private void loadFormations() {
        formationMap = new HashMap<>();
        List<Formation> formations = formationService.getIdAndTitre();
        for (Formation formation : formations) {
            formationMap.put(formation.getTitre(), formation.getId());
        }
        nomFormationComboBox.setItems(FXCollections.observableArrayList(formationMap.keySet()));
    }

    private void prefillFields() {
        users user = SessionManager.getUtilisateurConnecte();
        if (user != null) {
            txtNomApprenant.setText(user.getNom() + " " + user.getPrenom());
            txtEmail.setText(user.getEmail());
            txtApprenantId.setText(String.valueOf(user.getId()));
            txtNomApprenant.setDisable(true);
            txtEmail.setDisable(true);
            txtApprenantId.setDisable(true);
        } else {
            showAlert(Alert.AlertType.WARNING, "Session", "Aucun utilisateur connecté.");
        }
    }

    @FXML
    private void ajouterInscription() {
        try {
            resetFieldStyles();
            users user = SessionManager.getUtilisateurConnecte();

            // Validation
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Connectez-vous d'abord !");
                return;
            }

            if (txtNomApprenant.getText().isEmpty()) {
                txtNomApprenant.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Le nom de l'apprenant est requis.");
                return;
            }

            if (txtCin.getText().isEmpty() || !txtCin.getText().matches("\\d{8}")) {
                txtCin.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "CIN invalide (8 chiffres requis).");
                return;
            }

            if (!txtEmail.getText().matches("^\\S+@\\S+\\.\\S+$")) {
                txtEmail.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Email invalide.");
                return;
            }

            if (nomFormationComboBox.getValue() == null) {
                nomFormationComboBox.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Veuillez sélectionner une formation.");
                return;
            }

            if (typePaiementComboBox.getValue() == null) {
                typePaiementComboBox.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Veuillez sélectionner un type de paiement.");
                return;
            }

            int apprenantId;
            try {
                apprenantId = Integer.parseInt(txtApprenantId.getText());
                if (apprenantId <= 0) {
                    txtApprenantId.getStyleClass().add("error-border");
                    showAlert(Alert.AlertType.ERROR, "Validation", "ID Apprenant invalide.");
                    return;
                }
            } catch (NumberFormatException e) {
                txtApprenantId.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "ID Apprenant invalide.");
                return;
            }

            Integer formationId = formationMap.get(nomFormationComboBox.getValue());
            if (formationId == null || formationId <= 0) {
                nomFormationComboBox.getStyleClass().add("error-border");
                showAlert(Alert.AlertType.ERROR, "Validation", "Formation sélectionnée invalide.");
                return;
            }

            // Création de l'inscription
            InscriptionCours ins = new InscriptionCours();
            ins.setNomApprenant(user.getNom() + " " + user.getPrenom());
            ins.setCin(txtCin.getText());
            ins.setEmail(user.getEmail());
            ins.setNomFormation(nomFormationComboBox.getValue());
            ins.setTypePaiement(typePaiementComboBox.getValue());
            ins.setApprenantId(apprenantId);
            ins.setFormationId(formationId);
            ins.setDateInscreption(LocalDateTime.now());
            ins.setStatus("en attente");
            ins.setMontant(0.0);

            // Ajout à la base de données
            service.add(ins);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription enregistrée !");

            // Fermer la fenêtre d'inscription
            Stage currentStage = (Stage) btnAjouter.getScene().getWindow();
            currentStage.close();

            // Ouvrir l'interface de paiement avec le montant de la formation
            double montant = formationService.getPrixById(formationId);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaymentView.fxml"));
                Parent root = loader.load();
                PaymentController paymentController = loader.getController();
                paymentController.setAmount(montant);
                paymentController.setInscription(ins);
                Stage stage = new Stage();
                stage.setTitle("Paiement");
                stage.setScene(new Scene(root));
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Réinitialiser le formulaire
            handleAnnuler();

        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("unique_email_formation")) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous êtes déjà inscrit à cette formation !");
            } else if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("UNIQ_AF83D8D1ABE530DA")) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce CIN est déjà utilisé pour une autre inscription.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        nomFormationComboBox.getSelectionModel().clearSelection();
        txtCin.clear();
        typePaiementComboBox.getSelectionModel().clearSelection();
        if (txtFormationId != null) {
            txtFormationId.clear();
        }
        if (txtApprenantId != null) {
            txtApprenantId.clear();
        }
        resetFieldStyles();
        prefillFields();
    }

    private void resetFieldStyles() {
        txtNomApprenant.getStyleClass().remove("error-border");
        txtCin.getStyleClass().remove("error-border");
        txtEmail.getStyleClass().remove("error-border");
        nomFormationComboBox.getStyleClass().remove("error-border");
        typePaiementComboBox.getStyleClass().remove("error-border");
        if (txtApprenantId != null) {
            txtApprenantId.getStyleClass().remove("error-border");
        }
        if (txtFormationId != null) {
            txtFormationId.getStyleClass().remove("error-border");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}