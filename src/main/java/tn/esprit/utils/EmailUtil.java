package tn.esprit.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    private static final String FROM_EMAIL = "yassminehnainia1@gmail.com";
    private static final String PASSWORD = "xwwd qthh uvwc mcsz"; // Mot de passe d'application sécurisé

    public static void sendEmail(String toEmail, String subject, String body) {
        sendHtmlEmail(toEmail, subject, body); // Redirige vers l'envoi HTML par défaut
    }

    public static void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }
}