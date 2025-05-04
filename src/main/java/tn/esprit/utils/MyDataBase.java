package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;

    private final String URL = "jdbc:mysql://127.0.0.1:3306/forminifix";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected .....");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Connection is null or closed. Re-establishing...");
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Reconnected .....");
            }
            return cnx;
        } catch (SQLException e) {
            System.out.println("Error re-establishing connection: " + e.getMessage());
            return null; // Return null to allow calling code to handle the failure
        }
    }
}