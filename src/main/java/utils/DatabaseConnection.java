package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database credentials
    private static final String JDBC_URL = "jdbc:mysql://34.44.81.201:3306/hiros_marine";
    private static final String USERNAME = "hiro";
    private static final String PASSWORD = "hiro";
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(
                "jdbc:mysql://34.44.81.201:3306/hiros_marine",
                "hiro",
                "hiro"
            );
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
    }
    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
        }
    }
}
