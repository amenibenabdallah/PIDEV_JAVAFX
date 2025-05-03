package tn.esprit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
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

    private final String apiKey = "AIzaSyDNqiooMiPIMUzkxhwhW1jfpyZjA7D98CQ";
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 5000;

    @FXML
    public void initialize() {
        chatListView.setItems(messages);
        setupCellFactory();
        messages.add(" Bonjour ! Je suis votre assistant éducatif. Comment puis-je vous aider aujourd'hui ?");
    }

    private void setupCellFactory() {
        chatListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("user-message", "bot-message");
                } else {
                    setText(item);
                    getStyleClass().removeAll("user-message", "bot-message");

                    if (item.startsWith("Vous: ")) {
                        getStyleClass().add("user-message");
                        setAlignment(Pos.CENTER_RIGHT);
                    } else if (item.startsWith("Bot: ")) {
                        getStyleClass().add("bot-message");
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            }
        });
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
                Platform.runLater(() -> {
                    messages.add("Bot: " + response);
                    chatListView.scrollTo(messages.size() - 1);
                });
            } catch (IOException e) {
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
        String instruction = "Tu es un chatbot éducatif francophone. Réponds exclusivement aux questions sur:\n"
                + "- Pédagogie\n- Programmes scolaires\n- Méthodes d'apprentissage\n- Orientation professionnelle\n"
                + "Pour les questions hors-sujet, répondre: 'Désolé, je ne traite que les sujets éducatifs.'";

        String jsonBody = String.format("""
        {
            "contents": [{
                "parts": [{"text": "%s\\n\\nQuestion: %s"}]
            }],
            "generationConfig": {
                "maxOutputTokens": 400,
                "temperature": 0.7
            }
        }
        """, instruction.replace("\"", "\\\""), userMessage.replace("\"", "\\\""));

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .post(body)
                .build();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return mapper.readTree(response.body().string())
                            .path("candidates").get(0)
                            .path("content").path("parts").get(0)
                            .path("text").asText().trim();
                } else if (response.code() == 429) {
                    handleRateLimit(attempt, response);
                } else {
                    throw new IOException("Erreur API: " + response.code() + " - " + response.body().string());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Requête interrompue");
            }
        }
        throw new IOException("Échec après " + MAX_RETRIES + " tentatives");
    }

    private void handleRateLimit(int attempt, Response response) throws InterruptedException, IOException {
        long delay = BASE_RETRY_DELAY_MS * (long) Math.pow(2, attempt);
        System.out.println("Tentative " + attempt + "/" + MAX_RETRIES + " - Nouvel essai dans " + delay + "ms");
        Thread.sleep(delay);
        if (attempt == MAX_RETRIES) {
            throw new IOException("Limite de requêtes dépassée");
        }
    }
}