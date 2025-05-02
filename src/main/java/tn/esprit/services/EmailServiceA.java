package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailServiceA {
    private final Properties props;
    private final String username;
    private final String password;

    public EmailServiceA() {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // Use SSL for port 465
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        username = "benabdallah2ameni@gmail.com";
        password = "xclt iknh zctm cgnh";
    }

    public void sendAcceptanceEmail(String instructorEmail, String instructorName) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true); // Enable debug logging

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(instructorEmail));
            message.setSubject("Bienvenue chez Formini - Votre candidature a été acceptée !");
            message.setText(
                    "Cher(e) " + instructorName + ",\n\n" +
                            "Nous sommes ravis de vous informer que votre candidature comme instructeur chez Formini a été acceptée !\n" +
                            "Vous pouvez commencer à préparer vos cours. Connectez-vous à notre plateforme pour plus de détails.\n\n" +
                            "Cordialement,\nL'équipe Formini"
            );

            Transport.send(message);
            System.out.println("Email sent to " + instructorEmail);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendRejectionEmail(String instructorEmail, String instructorName) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true); // Enable debug logging

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(instructorEmail));
            message.setSubject("Formini - Mise à jour de votre candidature");
            message.setText(
                    "Cher(e) " + instructorName + ",\n\n" +
                            "Nous vous remercions pour l'intérêt que vous portez à Formini.\n" +
                            "Après une analyse attentive de votre candidature, nous sommes au regret de vous informer qu'elle n'a pas été retenue.\n" +
                            "Nous vous encourageons à continuer à enrichir votre parcours et à postuler à nouveau à l'avenir.\n\n" +
                            "Cordialement,\nL'équipe Formini"
            );

            Transport.send(message);
            System.out.println("Rejection email sent successfully!");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }
}
