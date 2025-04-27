package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

public class TextCorrectionService {

    private static final String API_URL = "https://api.languagetool.org/v2/check";

    public List<Correction> checkText(String text, String language) {
        List<Correction> corrections = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = "text=" + URLEncoder.encode(text, "UTF-8") + "&language=" + language;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray matches = jsonResponse.getJSONArray("matches");

            for (int i = 0; i < matches.length(); i++) {
                JSONObject match = matches.getJSONObject(i);
                String message = match.getString("message");
                int offset = match.getInt("offset");
                int length = match.getInt("length");
                String word = text.substring(offset, offset + length);
                List<String> suggestions = new ArrayList<>();
                if (match.has("replacements")) {
                    JSONArray replacements = match.getJSONArray("replacements");
                    for (int j = 0; j < replacements.length(); j++) {
                        suggestions.add(replacements.getJSONObject(j).getString("value"));
                    }
                }
                corrections.add(new Correction(word, message, suggestions, offset, length));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return corrections;
    }

    public static class Correction {
        private final String word;
        private final String message;
        private final List<String> suggestions;
        private final int offset;
        private final int length;

        public Correction(String word, String message, List<String> suggestions, int offset, int length) {
            this.word = word;
            this.message = message;
            this.suggestions = suggestions;
            this.offset = offset;
            this.length = length;
        }

        public String getWord() {
            return word;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }
    }
}