package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Instructeur_Template.fxml"));

        Parent root = loader.load();
        primaryStage.setTitle("Formini application");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("/Formation/addFormation.fxml"));
//        primaryStage.setTitle("Formini application");
//        primaryStage.setScene(new Scene(root, 600, 400));
//        primaryStage.show();
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
