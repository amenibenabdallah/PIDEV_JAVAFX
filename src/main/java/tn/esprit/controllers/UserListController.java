package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.users;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.util.List;

public class UserListController {

    @FXML
    private TableView<users> userTable;

    @FXML
    private TableColumn<users, String> colNom;

    @FXML
    private TableColumn<users, String> colEmail;

    @FXML
    private TableColumn<users, String> colRole;

    @FXML
    private TableColumn<users, Void> colActions;

    @FXML
    private Pagination pagination;

    private final UserService userService = new UserService();
    private final int rowsPerPage = 10;
    private List<users> allUsers;

    private int currentPageIndex = 0;

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roles"));

        addActionButtonsToTable();

        allUsers = userService.getAllUsers();
        setupPagination();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allUsers.size() / rowsPerPage);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

        // Préserve la page actuelle (sauf si elle dépasse la limite)
        if (currentPageIndex >= pagination.getPageCount()) {
            currentPageIndex = pagination.getPageCount() - 1;
        }

        pagination.setCurrentPageIndex(currentPageIndex);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<users> createPage(int pageIndex) {
        this.currentPageIndex = pageIndex;

        if (allUsers == null || allUsers.isEmpty()) {
            userTable.setItems(FXCollections.observableArrayList());
            return userTable;
        }

        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, allUsers.size());

        if (fromIndex > toIndex) {
            fromIndex = Math.max(0, toIndex - rowsPerPage);
        }

        userTable.setItems(FXCollections.observableArrayList(allUsers.subList(fromIndex, toIndex)));
        return userTable;
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnEdit.setOnAction(event -> {
                    users user = getTableView().getItems().get(getIndex());
                    openEditUserDialog(user);
                });

                btnDelete.setOnAction(event -> {
                    users user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
    }

    private void openEditUserDialog(users user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditUserForm.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(user);
            controller.setUserListController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier l'utilisateur");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification");
        }
    }

    private void deleteUser(users user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getNom() + " ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            userService.deleteUser(user.getId());
            allUsers.remove(user);
            refreshTable();
            showAlert("Succès", "Utilisateur supprimé avec succès");
        }
    }

    public void refreshTable() {
        allUsers = userService.getAllUsers();
        setupPagination();
    }

    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        refreshTable();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
