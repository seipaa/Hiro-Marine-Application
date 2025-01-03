package controllers;

import java.sql.SQLException;

import dao.RecommendationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.Recommendation;
import utils.AlertUtils;

public class RecommendationAdminController {
    @FXML
    private TextField locationNameField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private TextField ratingField;

    private RecommendationDAO recommendationDAO = new RecommendationDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void addRecommendation() {
        try {
            String locationName = locationNameField.getText();
            String description = descriptionField.getText();
            String imageUrl = imageUrlField.getText();
            double rating = Double.parseDouble(ratingField.getText());

            if (locationName.isEmpty() || description.isEmpty()) {
                AlertUtils.showError("Error", "Location name and description cannot be empty.");
                return;
            }

            if (rating < 0 || rating > 5) {
                AlertUtils.showError("Error", "Rating must be between 0 and 5.");
                return;
            }

            Recommendation recommendation = new Recommendation(0, locationName, description, imageUrl, rating);
            recommendationDAO.addRecommendation(recommendation);
            AlertUtils.showInfo("Success", "Recommendation added successfully.");
            clearFields();
        } catch (NumberFormatException e) {
            AlertUtils.showError("Error", "Rating must be a valid number.");
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to add recommendation: " + e.getMessage());
        }
    }

    @FXML
    private void updateRecommendation() {
        // Implement update logic
    }

    @FXML
    private void deleteRecommendation() {
        // Implement delete logic
    }

    private void clearFields() {
        locationNameField.clear();
        descriptionField.clear();
        imageUrlField.clear();
        ratingField.clear();
    }
}