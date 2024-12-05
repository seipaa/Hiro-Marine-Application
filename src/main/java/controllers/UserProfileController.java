package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import dao.ChallengeDAO;
import dao.UserProfileDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Challenge;
import models.User;
import models.UserProfile;
import utils.AlertUtils;
import utils.DatabaseConnection;

public class UserProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label totalPointsLabel;
    @FXML private Label joinDateLabel;
    @FXML private TextField nicknameField;
    @FXML private TextField ageField;
    @FXML private TextField emailField;
    @FXML private TextField instagramField;
    @FXML private TextField twitterField;
    @FXML private TextArea bioField;
    @FXML private VBox completedChallengesContainer;

    private User currentUser;
    private UserProfileDAO profileDAO = new UserProfileDAO();
    private ChallengeDAO challengeDAO = new ChallengeDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setUser(User user) {
        if (user == null) {
            AlertUtils.showError("Error", "Cannot load profile: User is null");
            return;
        }
        this.currentUser = user;
        usernameLabel.setText("Username: " + user.getName());
        totalPointsLabel.setText("Total Poin: " + user.getTotalPoints());
        
        // Format dan tampilkan tanggal bergabung
        LocalDateTime joinDate = user.getJoinDate();
        if (joinDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            String formattedDate = joinDate.format(formatter);
            
            // Hitung berapa lama user sudah bergabung
            long days = ChronoUnit.DAYS.between(joinDate, LocalDateTime.now());
            String duration;
            if (days == 0) {
                duration = "Hari ini";
            } else if (days < 30) {
                duration = days + " hari";
            } else if (days < 365) {
                long months = days / 30;
                duration = months + " bulan";
            } else {
                long years = days / 365;
                duration = years + " tahun";
            }
            
            joinDateLabel.setText("Bergabung sejak " + formattedDate + " (" + duration + ")");
        }
        
        loadUserProfile();
        loadCompletedChallenges();
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            AlertUtils.showError("Error", "Cannot load profile: No user set");
            return;
        }

        try {
            UserProfile profile = profileDAO.getUserProfile(currentUser.getId());
            if (profile != null) {
                nicknameField.setText(profile.getNickname());
                ageField.setText(String.valueOf(profile.getAge()));
                emailField.setText(profile.getEmail());
                instagramField.setText(profile.getInstagram());
                twitterField.setText(profile.getTwitter());
                bioField.setText(profile.getBio());
            } else {
                // Set default values for new users
                nicknameField.setText(currentUser.getName());
                ageField.setText("0");
                emailField.setText("");
                instagramField.setText("");
                twitterField.setText("");
                bioField.setText("");
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load user profile: " + e.getMessage());
        }
    }

    private void loadCompletedChallenges() {
        try {
            List<Challenge> completedChallenges = challengeDAO.getCompletedChallenges(currentUser.getId());
            completedChallengesContainer.getChildren().clear();
            
            if (completedChallenges.isEmpty()) {
                Label emptyLabel = new Label("Belum ada challenge yang diselesaikan");
                emptyLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
                completedChallengesContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Challenge challenge : completedChallenges) {
                // Buat card untuk setiap challenge
                VBox card = new VBox(10);
                card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-padding: 15;" +
                    "-fx-background-radius: 8;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                );

                // Title
                Label titleLabel = new Label(challenge.getTitle());
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // Points earned
                Label pointsLabel = new Label("🏆 " + challenge.getPoints() + " points earned");
                pointsLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                // Completion date
                Label dateLabel = new Label("📅 Completed on: " + challenge.getStartDate());
                dateLabel.setStyle("-fx-text-fill: #666666;");

                // Description
                Label descLabel = new Label(challenge.getDescription());
                descLabel.setStyle("-fx-text-fill: #333333;");
                descLabel.setWrapText(true);

                card.getChildren().addAll(titleLabel, pointsLabel, dateLabel, descLabel);
                completedChallengesContainer.getChildren().add(card);
            }
            
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal memuat completed challenges: " + e.getMessage());
        }
    }

    @FXML
    private void saveChanges() {
        try {
            // Update nama user di tabel users
            String updateNameQuery = "UPDATE users SET name = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateNameQuery)) {
                stmt.setString(1, nicknameField.getText());
                stmt.setInt(2, currentUser.getId());
                stmt.executeUpdate();
            }

            // Update profil user
            UserProfile profile = new UserProfile(
                currentUser.getId(),
                currentUser.getUsername(), // Tetap gunakan username asli
                nicknameField.getText(),   // Nama baru dari nickname field
                Integer.parseInt(ageField.getText()),
                emailField.getText(),
                instagramField.getText(),
                twitterField.getText(),
                bioField.getText()
            );
            
            profileDAO.saveUserProfile(profile);
            
            // Update tampilan nama di main window
            if (mainController != null) {
                mainController.updateUsername(nicknameField.getText());
            }
            
            // Update currentUser object
            currentUser.setName(nicknameField.getText());
            
            AlertUtils.showInfo("Success", "Profil berhasil diupdate!");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Error", "Usia harus berupa angka!");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal menyimpan profil: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            
            // Get current stage and set new scene
            Stage currentStage = (Stage) nicknameField.getScene().getWindow();
            Stage mainStage = (Stage) mainController.getContentPane().getScene().getWindow();
            
            mainStage.setScene(loginScene);
            currentStage.close();
        } catch (IOException e) {
            AlertUtils.showError("Error", "Failed to logout: " + e.getMessage());
        }
    }
} 