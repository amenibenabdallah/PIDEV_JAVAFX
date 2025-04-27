package tn.esprit.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSMSUtil {


    public static final String ACCOUNT_SID = "AC7556b2670120201b8dd7d36e1ff06571";
    public static final String AUTH_TOKEN = "ebd69f977fb85ef1026507e1f39bb23e";
    public static final String TWILIO_PHONE_NUMBER = "\t\n" +
            "+16084532249";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un SMS via Twilio
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
