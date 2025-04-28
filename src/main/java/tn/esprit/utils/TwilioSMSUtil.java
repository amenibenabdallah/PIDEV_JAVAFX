package tn.esprit.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TwilioSMSUtil {

    public static final String ACCOUNT_SID;
    public static final String AUTH_TOKEN;
    public static final String TWILIO_PHONE_NUMBER;

    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (IOException e) {
            throw new IllegalStateException("❌ Failed to load .env file: " + e.getMessage());
        }

        ACCOUNT_SID = props.getProperty("TWILIO_ACCOUNT_SID");
        AUTH_TOKEN = props.getProperty("TWILIO_AUTH_TOKEN");
        TWILIO_PHONE_NUMBER = props.getProperty("TWILIO_PHONE_NUMBER");

        if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_PHONE_NUMBER == null) {
            throw new IllegalStateException("❌ Twilio credentials are missing in .env file");
        }
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoi un SMS via Twilio
     * @param toPhoneNumber Numéro du destinataire (format international, ex: +216...)
     * @param messageBody Contenu du SMS
     */
    public static void sendSMS(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    messageBody
            ).create();

            System.out.println("✅ SMS envoyé avec succès ! SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS : " + e.getMessage());
        }
    }
}