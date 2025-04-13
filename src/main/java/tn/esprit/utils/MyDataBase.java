package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/forminiahmed3";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;

    private MyDataBase() {
        connect(); // Utilise une méthode dédiée pour se connecter
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();

        return instance;
    }

    // 🔁 Se reconnecte automatiquement si la connexion est fermée
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Connexion fermée ou nulle, reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cnx;
    }

    // ✅ Méthode privée pour gérer la connexion
    private void connect() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected .....");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
