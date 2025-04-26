package tn.esprit.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class XaiService {
    // Remplacez par votre clé API xAI (obtenue depuis https://console.x.ai/)
    private static final String API_KEY = "xai-w315ymjC3X9uC0iaXgsha1LkqH18XdjEVgfa97QJpGQ0eSeRZ80GiGrAOkmG7ScqsB5FKhho1Epy4Ca4";
    // Endpoint de l'API xAI (confirmé via la documentation)
    private static final String API_URL = "https://api.x.ai/v1/chat/completions";

    // Configuration d'OkHttp avec des timeouts
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public String askQuestion(String question) throws IOException {
        // Créer le message de l'utilisateur
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", question);

        JsonArray messages = new JsonArray();
        messages.add(message);

        // Construire le corps de la requête
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "grok-2-1212"); // Modèle récent de xAI
        requestBody.add("messages", messages);

        System.out.println("Requête JSON : " + requestBody.toString()); // Log pour débogage

        // Créer la requête HTTP
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        // Exécuter la requête
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Aucune réponse";
                throw new IOException("Erreur API xAI : " + response.code() + " - " + response.message() + " - " + errorBody);
            }
            if (response.body() == null) {
                throw new IOException("Réponse API vide");
            }
            String responseBody = response.body().string();
            System.out.println("Réponse API : " + responseBody); // Log pour débogage

            // Parser la réponse JSON
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            if (!jsonResponse.has("choices") || jsonResponse.getAsJsonArray("choices").size() == 0) {
                throw new IOException("Réponse API invalide : " + responseBody);
            }
            return jsonResponse
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();
        }
    }
}