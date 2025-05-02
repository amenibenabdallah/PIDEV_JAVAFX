package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import tn.esprit.models.User;
import tn.esprit.services.UserService;

public class EditUserController {
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;

    private User user;
    private UserListController userListController;
    private final UserService userService = new UserService();

    public void setUser(User user) {
        this.user = user;
        nomField.setText(user.getNom());
        emailField.setText(user.getEmail());
        roleField.setText(user.getRole());
    }

    public void setUserListController(UserListController controller) {
        this.userListController = controller;
    }

    @FXML
    private void handleSave() {
        user.setNom(nomField.getText());
        user.setEmail(emailField.getText());
        user.setRole(roleField.getText());

        userService.updateUser(user);
        userListController.refreshTable();
        nomField.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        nomField.getScene().getWindow().hide();
    }
}