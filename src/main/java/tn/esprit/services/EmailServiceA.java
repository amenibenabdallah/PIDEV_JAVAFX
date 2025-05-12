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

            // HTML content for the email
            String htmlContent = "<!DOCTYPE html>"
                    + "<html lang=\"en\">"
                    + "<head>"
                    + "<meta charset=\"UTF-8\">"
                    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                    + "<title>Formini</title>"
                    + "<style>"
                    + "body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }"
                    + "table { border-collapse: collapse; }"
                    + "img { border: 0; height: auto; line-height: 100%; outline: none; text-decoration: none; }"
                    + "a { text-decoration: none; }"
                    + "</style>"
                    + "</head>"
                    + "<body style=\"margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, sans-serif;\">"
                    + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f4f4f4;\">"
                    + "<tr>"
                    + "<td align=\"center\" style=\"padding: 40px 0;\">"
                    + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">"
                    + "<tr>"
                    + "<td style=\"padding: 20px; text-align: center; background-color: #C0392B; border-top-left-radius: 8px; border-top-right-radius: 8px;\">"
                    + "<h2>Welcome to Formini</h2>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style=\"padding: 40px 20px; text-align: center;\">"
                    + "<h2 style=\"color: #1E293B; font-size: 22px; margin: 0 0 20px;\">Congratulations, " + instructorName + "!</h2>"
                    + "<p style=\"color: #475569; font-size: 16px; line-height: 1.5; margin: 0 0 20px;\">"
                    + "We’re thrilled to inform you that your application to become an instructor at Formini has been accepted!"
                    + "</p>"
                    + "<p style=\"color: #475569; font-size: 16px; line-height: 1.5; margin: 0 0 30px;\">"
                    + "You can now start preparing your courses. Log in to our platform to get started."
                    + "</p>"
                    + "<a href=\"http://127.0.0.1:8000/\" style=\"display: inline-block; background-color: #C0392B; color: #ffffff; padding: 12px 24px; border-radius: 5px; font-size: 16px; text-decoration: none;\">Log in to Formini</a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style=\"padding: 20px; text-align: center; background-color: #f4f4f4; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;\">"
                    + "<p style=\"color: #64748B; font-size: 14px; margin: 0;\">If you have any questions, contact us at <a href=\"mailto:support@formini.com\" style=\"color: #C0392B;\">support@formini.com</a>.</p>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</body>"
                    + "</html>";

            // Set the HTML content of the message
            message.setContent(htmlContent, "text/html; charset=utf-8");

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

            // Email body for rejection
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
