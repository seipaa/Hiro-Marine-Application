package controllers;

import java.sql.SQLException;
import java.sql.Timestamp;

import dao.ChallengeDAO;
import dao.NewsDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Challenge;
import models.News;
import models.User;
import utils.ChallengeDetailsFetcher;
import utils.LeaderboardFetcher;

public class AdminController {

    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private ComboBox<Challenge> challengeComboBox;
    @FXML
    private Button verifyButton;
    @FXML
    private TextField newsTitleField;
    @FXML
    private TextField newsDescriptionField;

    private LeaderboardFetcher leaderboardFetcher = new LeaderboardFetcher();
    private ChallengeDetailsFetcher challengeDetailsFetcher = new ChallengeDetailsFetcher();
    private NewsDAO newsDAO = new NewsDAO();
    private ChallengeDAO challengeDAO = new ChallengeDAO();

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        ObservableList<User> users = FXCollections.observableArrayList(leaderboardFetcher.getAllUsers());
        ObservableList<Challenge> challenges = FXCollections.observableArrayList(challengeDetailsFetcher.getAllChallenges());

        userComboBox.setItems(users);
        challengeComboBox.setItems(challenges);
    }

    @FXML
    private void verifyAndAddPoints() {
        User selectedUser = userComboBox.getValue();
        Challenge selectedChallenge = challengeComboBox.getValue();

        if (selectedUser == null || selectedChallenge == null) {
            showError("Invalid Selection", "Please select both a user and a challenge.");
            return;
        }

        // Periksa apakah tantangan sudah diverifikasi
        if (leaderboardFetcher.isChallengeVerified(selectedUser.getId(), selectedChallenge.getId())) {
            showError("Error", "Challenge already verified for this user.");
            return;
        }

        boolean success = leaderboardFetcher.verifyAndAddPoints(selectedUser.getName(), selectedChallenge.getPoints());
        if (success) {
            leaderboardFetcher.markChallengeAsVerified(selectedUser.getId(), selectedChallenge.getId());
            showAlert("Success", "Points added successfully for user: " + selectedUser.getName());
            if (mainController != null) {
                mainController.updateLeaderboard();
            }
        } else {
            showError("Error", "Failed to update points for user: " + selectedUser.getName());
        }
    }

    @FXML
    private void addNews() {
        String title = newsTitleField.getText();
        String description = newsDescriptionField.getText();
        if (title.isEmpty() || description.isEmpty()) {
            showError("Error", "Title and description cannot be empty.");
            return;
        }
      
       News news = new News(
           0,                    // id
           1,                    // adminId
           title,               // title
           description,         // description
           "",                  // imageUrl
           new Timestamp(System.currentTimeMillis()), // createdAt
           false                // isBreakingNews
       );
       news.setPosition(0);     // position default
    }

    @FXML
    private void updateNews() {
        // Implement update logic
    }

    @FXML
    private void deleteNews() {
        // Implement delete logic
    }

    private void refreshLeaderboard() {
        // Logic to refresh leaderboard
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void addChallenge() {
        Challenge newChallenge = showAddChallengeDialog();
        if (newChallenge != null) {
            try {
                challengeDAO.addChallenge(newChallenge);
                challengeComboBox.getItems().add(newChallenge);
                showInfo("Success", "Challenge added successfully");
            } catch (SQLException e) {
                showError("Error", "Failed to add challenge: " + e.getMessage());
            }
        }
    }

    @FXML
    private void updateChallenge() {
        Challenge selectedChallenge = challengeComboBox.getValue();
        if (selectedChallenge != null) {
            Challenge updatedChallenge = showUpdateChallengeDialog(selectedChallenge);
            if (updatedChallenge != null) {
                try {
                    challengeDAO.updateChallenge(updatedChallenge);
                    challengeComboBox.getItems().set(challengeComboBox.getSelectionModel().getSelectedIndex(), updatedChallenge);
                    showInfo("Success", "Challenge updated successfully");
                } catch (SQLException e) {
                    showError("Error", "Failed to update challenge: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void deleteChallenge() {
        Challenge selectedChallenge = challengeComboBox.getValue();
        if (selectedChallenge != null) {
            try {
                challengeDAO.deleteChallenge(selectedChallenge.getId());
                challengeComboBox.getItems().remove(selectedChallenge);
                showInfo("Success", "Challenge deleted successfully");
            } catch (SQLException e) {
                showError("Error", "Failed to delete challenge: " + e.getMessage());
            }
        }
    }

    private Challenge showAddChallengeDialog() {
        // Implementasi dialog untuk menambah challenge baru
        return new Challenge(0, "New Challenge", "Description", 10, "imageUrl", "qrCodeUrl", "startDate", "endDate");
    }

    private Challenge showUpdateChallengeDialog(Challenge challenge) {
        // Implementasi dialog untuk mengupdate challenge
        return challenge;
    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    @FXML
    public void showNewsAdmin() {
        showPopup("/fxml/admin_news.fxml", "News Admin Panel");
    }

    public void showMarineSpeciesAdmin() {
        showPopup("/fxml/admin_marinespecies.fxml", "Marine Species Admin Panel");
    }

    public void showRecommendationAdmin() {
        showPopup("/fxml/admin_recommendation.fxml", "Recommendation Admin Panel");
    }

    public void showChallengeAdmin() {
        showPopup("/fxml/admin_challenge.fxml", "Challenge Admin Panel");
    }

    private void showPopup(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Set controller untuk popup
            Object controller = loader.getController();
            if (controller instanceof AdminController) {
                ((AdminController) controller).setMainController(mainController);
            }
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load " + title + " panel.");
        }
    }
}
