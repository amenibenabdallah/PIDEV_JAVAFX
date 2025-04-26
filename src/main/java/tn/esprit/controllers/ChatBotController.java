package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.services.XaiService;
import java.io.IOException;

public class ChatBotController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField userInput;

    private final XaiService xaiService = new XaiService();

    @FXML
    public void initialize() {
        // Permettre d'envoyer le message avec la touche Entrée
        userInput.setOnAction(event -> handleSend());
    }

    @FXML
    private void handleSend() {
        String input = userInput.getText().trim();
        if (!input.isEmpty()) {
            // Afficher le message de l'utilisateur
            chatArea.appendText("Vous: " + input + "\n");
            userInput.clear();

            // Appeler l'API xAI dans un thread séparé
            new Thread(() -> {
                try {
                    String response = xaiService.askQuestion(input);
                    // Mettre à jour l'interface sur le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        chatArea.appendText("Bot: " + response.trim() + "\n");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    // Afficher l'erreur dans l'interface
                    javafx.application.Platform.runLater(() -> {
                        chatArea.appendText("Bot: Erreur de communication avec xAI : " + e.getMessage() + "\n");
                    });
                }
            }).start();
        }
    }
}