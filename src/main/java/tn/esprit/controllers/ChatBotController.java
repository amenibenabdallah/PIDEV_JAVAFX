package tn.esprit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import okhttp3.*;

import java.io.IOException;

public class ChatBotController {

    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextField userInputField;
    @FXML
    private Button sendButton;

    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Intégration de ta clé API ici :
    private final String apiKey = "AIzaSyDNqiooMiPIMUzkxhwhW1jfpyZjA7D98CQ";

    // Configuration de retry
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 5000;

    @FXML
    public void initialize() {
        chatListView.setItems(messages);
    }

    @FXML
    public void sendMessage() {
        String userMessage = userInputField.getText().trim();
        if (userMessage.isEmpty()) return;

        sendButton.setDisable(true);
        messages.add("Vous: " + userMessage);
        userInputField.clear();

        new Thread(() -> {
            try {
                String response = getChatbotResponse(userMessage);
                System.out.println("Réponse du bot: " + response);
                Platform.runLater(() -> {
                    messages.add("Bot: " + response);
                    chatListView.scrollTo(messages.size() - 1);
                });
            } catch (IOException e) {
                System.out.println("Erreur: " + e.getMessage());
                Platform.runLater(() -> {
                    messages.add("Bot: Erreur : " + e.getMessage());
                    chatListView.scrollTo(messages.size() - 1);
                });
            } finally {
                Platform.runLater(() -> sendButton.setDisable(false));
            }
        }).start();
    }

    private String getChatbotResponse(String userMessage) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("La clé API n'est pas configurée. Merci de vérifier votre clé.");
        }

        String instructionSysteme = "Tu es un chatbot éducatif qui parle français. "
                + "Tu dois répondre uniquement aux questions liées à l'éducation en français. "
                + "Si la question n'est pas liée à l'éducation, réponds poliment : "
                + "'Désolé, je ne peux répondre qu'aux questions concernant l'éducation.'";

        String promptComplet = instructionSysteme + "\n\nQuestion de l'utilisateur : " + userMessage;

        String jsonBody = """
        {
            "contents": [
                {
                    "parts": [
                        {"text": "%s"}
                    ]
                }
            ],
            "generationConfig": {
                "maxOutputTokens": 300
            }
        }
        """.formatted(promptComplet.replace("\"", "\\\""));

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .post(body)
                .build();

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return mapper.readTree(responseBody)
                            .get("candidates").get(0)
                            .get("content").get("parts").get(0)
                            .get("text").asText().trim();
                } else if (response.code() == 429) {
                    attempt++;
                    String errorBody = response.body() != null ? response.body().string() : "Pas de corps de réponse";
                    String retryAfter = response.header("Retry-After");
                    long delayMs = retryAfter != null ? Long.parseLong(retryAfter) * 1000 : BASE_RETRY_DELAY_MS * attempt;
                    System.out.println("Trop de requêtes : " + errorBody + ". Nouvel essai après " + delayMs + "ms (Essai " + attempt + "/" + MAX_RETRIES + ")");
                    if (attempt >= MAX_RETRIES) {
                        throw new IOException("Limite atteinte après " + MAX_RETRIES + " essais : " + errorBody);
                    }
                    Thread.sleep(delayMs);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Pas de corps de réponse";
                    throw new IOException("Code inattendu " + response.code() + " : " + errorBody);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interruption pendant l'attente entre les essais : " + e.getMessage());
            }
        }
        throw new IOException("Échec après " + MAX_RETRIES + " tentatives");
    }
}
