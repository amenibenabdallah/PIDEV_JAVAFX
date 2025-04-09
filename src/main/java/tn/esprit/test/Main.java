package tn.esprit.test;
import tn.esprit.utils.MyDataBase;
import java.sql.Connection;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {
        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
    }
}