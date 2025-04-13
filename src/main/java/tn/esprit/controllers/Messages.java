package tn.esprit.controllers;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import tn.esprit.models.Message;
import tn.esprit.services.MessageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Messages extends Application {

    @FXML
    private TextArea messageTextArea;

    @FXML
    private VBox messagesBox;

    private final int senderId = 1;
    private final int receiverId = 2;

    private final MessageService messageService = new MessageService();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/messages.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Messagerie");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        afficherMessages();
    }

    private void afficherMessages() {
        messagesBox.getChildren().clear();

        List<Message> messages = messageService.getMessages(senderId, receiverId);

        for (Message msg : messages) {
            Label messageLabel = new Label(msg.getCreatedAt() + " : " + msg.getContent());
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(300);

            Button deleteButton = new Button("🗑️");
            deleteButton.setOnAction(e -> {
                messageService.supprimerMessage(msg.getId());
                afficherMessages();
            });

            Button editButton = new Button("✏️");
            editButton.setOnAction(e -> modifierMessage(msg));

            HBox messageBox = new HBox(10, messageLabel, editButton, deleteButton);
            messageBox.setPadding(new Insets(10));
            messageBox.setMaxWidth(400);

            if (msg.getSenderId() == senderId) {
                messageBox.setStyle("-fx-background-color: #cce5ff; -fx-background-radius: 12;");
                messageBox.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageBox.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 12;");
                messageBox.setAlignment(Pos.CENTER_LEFT);
            }

            VBox bubbleWrapper = new VBox(messageBox);
            bubbleWrapper.setPadding(new Insets(5, 10, 5, 10));
            bubbleWrapper.setAlignment(msg.getSenderId() == senderId ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            messagesBox.getChildren().add(bubbleWrapper);
        }
    }

    @FXML
    private void envoyerMessage() {
        String contenu = messageTextArea.getText().trim();

        if (contenu.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champ vide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez écrire un message avant d’envoyer.");
            alert.showAndWait();
            return;
        }

        Message message = new Message(0, senderId, receiverId, contenu, LocalDateTime.now(), 4); // discussionId = 4 pour l'exemple
        messageService.envoyerMessage(message);

        messageTextArea.clear();
        afficherMessages();
    }

    private void modifierMessage(Message msg) {
        TextInputDialog dialog = new TextInputDialog(msg.getContent());
        dialog.setTitle("Modifier le message");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau message :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauContenu -> {
            if (!nouveauContenu.trim().isEmpty()) {
                messageService.modifierMessage(msg.getId(), nouveauContenu);
                afficherMessages();
            }
        });
    }
}
