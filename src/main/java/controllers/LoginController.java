package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dao.AdminDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Admin;
import models.User;
import utils.AlertUtils;
import utils.DatabaseHelper;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private AdminDAO adminDAO = new AdminDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showError("Error", "Silakan masukkan username dan password");
            return;
        }

        try {
            // Coba login sebagai admin terlebih dahulu
            Admin admin = adminDAO.getAdminByUsernameAndPassword(username, password);
            if (admin != null) {
                System.out.println("Admin login berhasil: " + admin.getName());
                loadMainScreen(null, admin);
                return;
            }

            // Kalau bukan admin, coba login sebagai user
            User user = userDAO.getUserByUsernameAndPassword(username, password);
            if (user != null) {
                if ("FROZEN".equals(user.getStatus())) {
                    AlertUtils.showError("Login Gagal", "Akun Anda telah dibekukan.");
                    return;
                }
                System.out.println("User login berhasil: " + user.getName());
                loadMainScreen(user, null);
                return;
            }

            // Kalau keduanya gagal
            AlertUtils.showError("Login Gagal", "Username atau password tidak valid");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMainScreen(User user, Admin admin) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();
        if (user != null) {
            mainController.setCurrentUser(user);
            mainController.setAdmin(false);
        } else if (admin != null) {
            mainController.setCurrentAdmin(admin);
            mainController.setAdmin(true);
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Error", "Gagal memuat tampilan registrasi.");
        }
    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    private boolean isAdmin(String username, String password) {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Terjadi kesalahan saat memeriksa admin: " + e.getMessage());
            return false;
        }
    }

    public void setMainController(MainController mainController) {
    }
}
