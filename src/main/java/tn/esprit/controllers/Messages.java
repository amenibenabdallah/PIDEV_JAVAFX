package tn.esprit.controllers;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.StringConverter;
import tn.esprit.models.Message;
import tn.esprit.models.users;
import tn.esprit.services.MessageService;
import tn.esprit.models.Discussion;
import tn.esprit.services.DiscussionService;
import javafx.scene.layout.VBox;
import tn.esprit.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Messages extends Application {

    @FXML
    private TextArea messageTextArea;

    @FXML
    private ComboBox<users> userComboBox;
    @FXML
    private Button addDiscussionBtn;

    private final UserService userService = new UserService();
    @FXML
    private ListView<Discussion> discussionListView;
    @FXML
    private VBox messagesBox;

    private DiscussionService discussionService;

    public Messages() {
        discussionService = new DiscussionService();
    }


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
        refreshDiscussions();

        // Charger les utilisateurs dans la ComboBox
        List<users> allUsers = userService.getAllUsers();
        ObservableList<users> observableUsers = FXCollections.observableArrayList(allUsers);
        userComboBox.setItems(observableUsers);

        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(users user) {
                if (user == null) return "";  // Évite le crash ici
                return user.getPrenom() + " " + user.getNom();
            }

            @Override
            public users fromString(String string) {
                return null;  // On ne l'utilise pas
            }

        });

        addDiscussionBtn.setOnAction(e -> {
            users selectedUser = userComboBox.getValue();
            if (selectedUser != null) {
                discussionService.addDiscussion(senderId, selectedUser.getId());
                refreshDiscussions();
            }
        });
    }

    private void refreshDiscussions() {
        ObservableList<Discussion> discussions = FXCollections.observableArrayList(discussionService.getAllDiscussions());
        discussionListView.setItems(discussions);
    }

    private void afficherMessages() {
        messagesBox.getChildren().clear();

        List<Message> messages = messageService.getMessages(senderId, receiverId);

        for (Message msg : messages) {
            // Label pour le contenu du message
            Label contentLabel = new Label(msg.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(300);
            contentLabel.getStyleClass().add("message-label");

            // Label pour la date
            Label dateLabel = new Label(msg.getCreatedAt().toString());
            dateLabel.getStyleClass().add("message-date");

            // VBox contenant le contenu et la date
            VBox messageContentBox = new VBox(5, contentLabel, dateLabel);

            Button deleteButton = new Button("❌");
            deleteButton.setOnAction(e -> {
                messageService.supprimerMessage(msg.getId());
                afficherMessages();
            });

            Button editButton = new Button("✏");
            editButton.setOnAction(e -> modifierMessage(msg));

            HBox messageBox = new HBox(10, messageContentBox, editButton, deleteButton);
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
