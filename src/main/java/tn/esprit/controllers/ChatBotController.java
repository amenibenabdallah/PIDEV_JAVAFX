package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.IOException;
import tn.esprit.services.OpenAIService;
// import tn.esprit.services.OpenAIService; // Décommente si OpenAIService est dans un autre package

public class ChatBotController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField userInput;

    private final OpenAIService openAIService = new OpenAIService();

    @FXML
    private void handleSend() {
        String input = userInput.getText().trim();
        if (!input.isEmpty()) {
            chatArea.appendText("Vous: " + input + "\n");
            userInput.clear();

            new Thread(() -> {
                try {
                    String response = openAIService.askQuestion(input);
                    javafx.application.Platform.runLater(() -> {
                        chatArea.appendText("Bot: " + response.trim() + "\n");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        chatArea.appendText("Bot: Erreur de communication avec OpenAI.\n");
                    });
                }
            }).start();
        }
    }
}

