package tn.esprit.services;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;

public class EmailService {
    public static void sendPromoToApprenant(String email, String codePromo, String description, double remise, LocalDate dateExpiration) {
        final String from = "noreply@pidev.tn";
        final String host = "localhost";
        final int port = 1025; // Port par défaut de MailHog

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props, null);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("🎁 Votre code promo exclusif : " + codePromo);
            String body = "Bonjour,\n\n" +
                    "Félicitations ! Vous avez reçu un code promotionnel à utiliser sur notre plateforme.\n" +
                    "---------------------------------------------\n" +
                    "Code promo : " + codePromo + "\n" +
                    "Description : " + description + "\n" +
                    "Remise : " + remise + "%\n" +
                    "Date d'expiration : " + dateExpiration + "\n" +
                    "---------------------------------------------\n" +
                    "\nPour profiter de cette offre, saisissez ce code lors de votre prochaine inscription.\n" +
                    "\nÀ très bientôt sur notre plateforme !\n" +
                    "\nL'équipe PIDEV";
            message.setText(body);
            Transport.send(message);
            System.out.println("Email envoyé à " + email);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
} 