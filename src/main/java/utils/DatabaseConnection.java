package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3300/hiros_marine?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "humility";
    private static Connection connection = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver berhasil diload");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver tidak ditemukan!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Berhasil terhubung ke database!");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Error koneksi database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Koneksi database ditutup.");
            } catch (SQLException e) {
                System.err.println("Error menutup koneksi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static boolean testConnection() {
        try {
            getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Error mengecek koneksi: " + e.getMessage());
            return false;
        }
    }
} 