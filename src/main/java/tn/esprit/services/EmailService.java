package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.util.Properties;

public class EmailService {
    private final Properties props;
    private final String username;
    private final String password;

    public EmailService() {
        props = new Properties();
        try {
            Properties config = new Properties();
            FileInputStream fis = new FileInputStream("C:/Users/LENOVO/Desktop/PIDEV_JAVAFX/config.properties");
            config.load(fis);
            fis.close();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", config.getProperty("smtp.host"));
            props.put("mail.smtp.port", config.getProperty("smtp.port"));
            username = config.getProperty("smtp.username");
            password = config.getProperty("smtp.password");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SMTP config", e);
        }
    }

    public void sendAcceptanceEmail(String instructorEmail, String instructorName) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

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
            e.printStackTrace();
        }
    }

}