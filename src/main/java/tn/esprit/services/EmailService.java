package tn.esprit.services;

import java.time.LocalDate;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    public static void sendPromoToApprenant(String email, String codePromo, String description, double remise, LocalDate dateExpiration) {
        final String from = "noreply@pidev.tn";
        final String host = "localhost";
        final int port = 1025; // Port par défaut de MailHog

        // Configuration SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props);
        session.setDebug(false); // Passe à true pour activer les logs SMTP

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("🎁 Votre code promo exclusif : " + codePromo);

            // Corps HTML de l'e-mail avec format dynamique
            String htmlBody = String.format("""
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Votre code promo</title>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                        .header { text-align: center; background-color: #4CAF50; color: white; padding: 10px 0; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; text-align: center; }
                        .promo-details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; text-align: left; margin: 20px 0; }
                        .promo-details p { margin: 5px 0; font-size: 16px; }
                        .cta-button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; font-size: 16px; }
                        .footer { text-align: center; font-size: 14px; color: #777; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>PIDEV</h1></div>
                        <div class="content">
                            <h2>Bonjour,</h2>
                            <p>Félicitations ! Vous avez reçu un code promotionnel à utiliser sur notre plateforme.</p>
                            <div class="promo-details">
                                <p><strong>Code promo :</strong> %s</p>
                                <p><strong>Description :</strong> %s</p>
                                <p><strong>Remise :</strong> %.1f%%</p>
                                <p><strong>Date d'expiration :</strong> %s</p>
                            </div>
                            <p>Pour profiter de cette offre, saisissez ce code lors de votre prochaine inscription.</p>
                            
                            <p>À très bientôt sur notre plateforme !</p>
                        </div>
                        <div class="footer">L'équipe PIDEV</div>
                    </div>
                </body>
                </html>
                """, codePromo, description, remise, dateExpiration);

            message.setContent(htmlBody, "text/html; charset=UTF-8");

            // Envoi de l'e-mail
            Transport.send(message);
            System.out.println("✅ Email envoyé à " + email);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}

