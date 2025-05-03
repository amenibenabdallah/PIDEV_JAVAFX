package tn.esprit.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.InscriptionCours;
import tn.esprit.models.Promotion;
import tn.esprit.services.ServiceInscriptionCours;
import tn.esprit.services.ServicePromotion;

import java.time.LocalDate;
import java.time.YearMonth;

public class PaymentController {

    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private Label amountLabel;
    @FXML private Button payButton;
    @FXML private Button cancelButton;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private TextField codePromoField;
    @FXML private Button verifierPromoButton;
    @FXML private Label remiseLabel;
    private static final String REMISE_LABEL_DEFAULT = "Entrez un code promo pour bénéficier d'une remise";

    private double amount = 1.00; // Montant par défaut
    private InscriptionCours inscription;
    private final ServiceInscriptionCours inscriptionService = new ServiceInscriptionCours();
    private final ServicePromotion servicePromotion = new ServicePromotion();
    private Double montantRemise = null;
    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void initialize() {
        try {
            StripeConfig.initialize();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de configuration", e.getMessage());
            return;
        }

        cardNumberField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cardNumberField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (cardNumberField.getText().length() > 16) {
                cardNumberField.setText(cardNumberField.getText().substring(0, 16));
            }
        });

        expiryDateField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                expiryDateField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (expiryDateField.getText().length() > 4) {
                expiryDateField.setText(expiryDateField.getText().substring(0, 4));
            }
        });

        cvvField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cvvField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (cvvField.getText().length() > 3) {
                cvvField.setText(cvvField.getText().substring(0, 3));
            }
        });

        setAmount(amount);
    }

    public void setAmount(double amount) {
        this.amount = amount;
        amountLabel.setText(String.format("Montant à payer : $%.2f", amount));
        payButton.setText(String.format("Pay $%.2f", amount));
    }

    public void setInscription(InscriptionCours inscription) {
        this.inscription = inscription;
    }

    @FXML
    private void handleVerifierPromo() {
        if (codePromoField.getText() != null && !codePromoField.getText().isEmpty()) {
            Promotion promo = servicePromotion.getByCode(codePromoField.getText().trim());
            if (promo != null && !promo.getDateExpiration().isBefore(LocalDate.now())) {
                montantRemise = amount - (amount * promo.getRemise() / 100.0);
                remiseLabel.setText("Montant après remise : " + String.format("%.2f", montantRemise) + " €");
            } else {
                montantRemise = null;
                remiseLabel.setText(REMISE_LABEL_DEFAULT);
                showAlert(Alert.AlertType.WARNING, "Code promo invalide", "Le code promo est invalide ou expiré.");
            }
        } else {
            montantRemise = null;
            remiseLabel.setText(REMISE_LABEL_DEFAULT);
            showAlert(Alert.AlertType.WARNING, "Champ vide", "Veuillez saisir un code promo.");
        }
    }

    @FXML
    private void handlePayment() {
        if (!validateFields()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez remplir tous les champs correctement.");
            return;
        }

        // Application du code promo si vérifié
        double montantFinal = (montantRemise != null) ? montantRemise : amount;

        try {
            // Utiliser un PaymentMethod de test prédéfini (pm_card_visa simule une carte Visa valide)
            String paymentMethodId = "pm_card_visa";

            // Créer un PaymentIntent avec le PaymentMethod de test et les paramètres requis
            long amountInCents = (long) (montantFinal * 100); // Convertir en centimes
            PaymentIntentCreateParams intentParams = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur") // Changé en "eur" pour correspondre à votre Dashboard
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true) // Confirmer immédiatement
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
                    .setReturnUrl("https://example.com/return") // URL de retour pour gérer les redirections
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(intentParams);

            // Vérifier le statut du paiement
            if ("succeeded".equals(paymentIntent.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Paiement effectué avec succès !");
                if (inscription != null) {
                    inscription.setStatus("payé");
                    inscription.setMontant(montantFinal);
                    inscriptionService.update(inscription);
                }

                // Charger l'interface de vérification dans contentArea
                if (mainLayoutController != null) {
                    mainLayoutController.loadVerificationPaiement();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'interface de vérification.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec du paiement", "Le paiement a échoué. Statut : " + paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Stripe", "Échec du paiement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        String cardNumber = cardNumberField.getText();
        if (cardNumber.length() != 16 || !isValidLuhn(cardNumber)) {
            return false;
        }

        String expiry = expiryDateField.getText();
        if (expiry.length() != 4) {
            return false;
        }
        try {
            int month = Integer.parseInt(expiry.substring(0, 2));
            int year = Integer.parseInt("20" + expiry.substring(2, 4));
            if (month < 1 || month > 12) {
                return false;
            }
            YearMonth expiryDate = YearMonth.of(year, month);
            if (expiryDate.isBefore(YearMonth.now())) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return cvvField.getText().length() == 3;
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        if (mainLayoutController != null) {
            mainLayoutController.loadWelcomePage();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la page d'accueil.");
        }
    }
}