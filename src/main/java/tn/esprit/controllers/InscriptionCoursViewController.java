package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.*;
import tn.esprit.services.ServiceInscriptionCours;
import tn.esprit.services.ServiceFormation1;
import tn.esprit.services.ServicePromotion;
import tn.esprit.utils.SessionManager;

import java.time.LocalDateTime;
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
    private final ServiceFormation1 formationService = new ServiceFormation1();
    private final ServicePromotion servicePromotion = new ServicePromotion();
    private Map<String, Integer> formationMap; // Associe titre à id
    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }
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
        List<Formation1> formations = formationService.getIdAndTitre();
        for (Formation1 formation : formations) {
            formationMap.put(formation.getTitre(), formation.getId());
        }
        nomFormationComboBox.setItems(FXCollections.observableArrayList(formationMap.keySet()));
    }

    private void prefillFields() {
        User user = SessionManager.getUtilisateurConnecte();
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
            User user = SessionManager.getUtilisateurConnecte();

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

            // Charger l'interface de paiement dans le contentArea
            double montant = formationService.getPrixById(formationId);
            mainLayoutController.loadPaymentView(montant, ins);

            // Réinitialiser le formulaire
            handleAnnuler();

        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("unique_email_formation")) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Vous êtes déjà inscrit à cette formation !");
                } else if (e.getMessage().contains("UNIQ_AF83D8D1ABE530DA")) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Ce CIN est déjà utilisé pour une autre inscription.");
                }
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