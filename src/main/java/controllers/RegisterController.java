package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.AlertUtils;
import utils.DatabaseConnection;
import utils.SceneUtils;

public class RegisterController {
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;

    @FXML
    private void handleRegister() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();

        // Validasi input dasar
        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showError("Error", "Semua field harus diisi!");
            return;
        }

        // Validasi panjang username
        if (username.length() < 4 || username.length() > 20) {
            AlertUtils.showError("Error", "Username harus antara 4-20 karakter!");
            return;
        }

        // Validasi karakter username
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            AlertUtils.showError("Error", "Username hanya boleh mengandung huruf, angka, dan underscore!");
            return;
        }

        // Validasi panjang password
        if (password.length() < 6) {
            AlertUtils.showError("Error", "Password minimal 6 karakter!");
            return;
        }

        // Cek apakah username sudah ada
        if (isUsernameExists(username)) {
            AlertUtils.showError("Error", "Username sudah digunakan!");
            return;
        }

        try {
            // Insert user baru
            String query = "INSERT INTO users (username, name, user_password, total_points, status, join_date) " +
                         "VALUES (?, ?, ?, 0, 'ACTIVE', CURRENT_TIMESTAMP)";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, username); // Nama awal sama dengan username
                stmt.setString(3, password);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    AlertUtils.showInfo("Sukses", "Registrasi berhasil! Silakan login.");
                    clearFields();
                    goToLogin();
                } else {
                    AlertUtils.showError("Error", "Gagal mendaftar: Tidak ada data yang tersimpan");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal mendaftar: " + e.getMessage());
        }
    }

    private boolean isUsernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal memeriksa username: " + e.getMessage());
        }
        return false;
    }

    @FXML
    private void goToLogin() {
        try {
            SceneUtils.changeScene(newUsernameField, "/fxml/login.fxml");
        } catch (Exception e) {
            System.err.println("Error navigating to login: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal membuka halaman login: " + e.getMessage());
        }
    }

    private void clearFields() {
        newUsernameField.clear();
        newPasswordField.clear();
    }
}
