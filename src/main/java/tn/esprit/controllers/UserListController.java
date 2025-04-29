package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.UserService;
import java.io.IOException;
import java.util.List;

public class UserListController {

    @FXML
    private VBox userCardList;

    @FXML
    private Pagination pagination;

    private final UserService userService = new UserService();
    private final int rowsPerPage = 4;
    private List<User> allUsers;

    @FXML
    public void initialize() {
        allUsers = userService.getAllUsers();
        setupPagination();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allUsers.size() / rowsPerPage);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        userCardList.getChildren().clear();

        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, allUsers.size());

        List<User> usersOnPage = allUsers.subList(fromIndex, toIndex);

        for (User user : usersOnPage) {
            userCardList.getChildren().add(createUserCard(user));
        }

        return new VBox();
    }

    private HBox createUserCard(User user) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: white; "
                + "-fx-background-radius: 16; "
                + "-fx-padding: 15; "
                + "-fx-alignment: center-left; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.3, 0, 4);");

        VBox info = new VBox(5);
        Label nomLabel = new Label("Nom: " + user.getNom());
        Label emailLabel = new Label("Email: " + user.getEmail());
        Label roleLabel = new Label("Rôle: " + user.getRole());

        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        emailLabel.setStyle("-fx-text-fill: #4a5568;");
        roleLabel.setStyle("-fx-text-fill: #718096;");

        info.getChildren().addAll(nomLabel, emailLabel, roleLabel);

        // ➤ Bouton Supprimer seulement
        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDelete.setOnAction(event -> deleteUser(user));

        HBox actions = new HBox(10, btnDelete);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(info, spacer, actions);

        return card;
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getNom() + " ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            userService.deleteUser(user.getId());
            allUsers.remove(user);
            refreshTable();
            showAlert("Succès", "Utilisateur supprimé avec succès.");
        }
    }

    public void refreshTable() {
        allUsers = userService.getAllUsers();
        setupPagination();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
