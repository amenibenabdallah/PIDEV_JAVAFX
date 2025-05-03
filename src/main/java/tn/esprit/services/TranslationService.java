package tn.esprit.services;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TranslationService {
    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String apiUrl = String.format(
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    sourceLang, targetLang, encodedText
            );

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String resp = response.toString();
            int firstQuote = resp.indexOf('\"') + 1;
            int secondQuote = resp.indexOf('\"', firstQuote);

            return resp.substring(firstQuote, secondQuote);

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de traduction.";
        }
    }
}
