package tn.esprit.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;

public class OpenAIService {
    private static final String API_KEY = "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"; // Mets ta NOUVELLE clé API ici
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();

    public String askQuestion(String question) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", question);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo"); // modèle GPT utilisé
        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Erreur API : " + response);
        }

        String responseBody = response.body().string();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        return jsonResponse
                .getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }
}
