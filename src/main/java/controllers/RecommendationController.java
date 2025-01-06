package controllers;

import dao.RecommendationDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Recommendation;
import models.User;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import utils.AlertUtils;

import java.util.List;

public class RecommendationController {

    @FXML
    private VBox recommendationsContainer;

    private RecommendationDAO recommendationDAO = new RecommendationDAO();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        loadRecommendations();
    }

    public void loadRecommendations() {
        try {
            List<Recommendation> recommendations = recommendationDAO.getAllRecommendations();
            recommendationsContainer.getChildren().clear();

            for (Recommendation recommendation : recommendations) {
                VBox card = createRecommendationCard(recommendation);
                recommendationsContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load recommendations: " + e.getMessage());
        }
    }

    private VBox createRecommendationCard(Recommendation recommendation) {
        System.out.println("Creating card for recommendation: " + recommendation.getLocationName());
        
        // Main card container
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        card.setPadding(new Insets(15));
        card.setMaxWidth(800);
        card.setMinHeight(400);

        try {
            // Image container
            ImageView imageView = new ImageView();
            imageView.setFitWidth(770);
            imageView.setFitHeight(300);

            byte[] imageData = recommendation.getImageData();
            if (imageData != null && imageData.length > 0) {
                Image image = new Image(new ByteArrayInputStream(imageData));
                imageView.setImage(image);
            } else {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_location.jpg"));
                imageView.setImage(defaultImage);
            }

            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            // Location name
            Label nameLabel = new Label(recommendation.getLocationName());
            nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
            nameLabel.setWrapText(true);

            // Description
            Label descLabel = new Label(recommendation.getDescription());
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-wrap-text: true;");
            descLabel.setMaxWidth(770);

            // Create separate containers for rating and button
            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(Pos.CENTER_LEFT);

            // Rating stars
            double rating = recommendation.getRating();
            for (int i = 0; i < 5; i++) {
                Label star = new Label(i < rating ? "★" : "☆");
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (i < rating ? "#FFD700" : "#666666") + ";");
                ratingBox.getChildren().add(star);
            }

            // Rating value
            Label ratingLabel = new Label(String.format("%.1f", rating));
            ratingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            ratingBox.getChildren().add(ratingLabel);

            // Create button container
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setMinHeight(40); // Ensure minimum height
            System.out.println("Created button container");

            // Discussion button
            Button discussButton = new Button("Diskusi Rekomendasi");
            discussButton.setStyle("-fx-background-color: #2196F3; " +
                                 "-fx-text-fill: white; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-padding: 10 20; " +
                                 "-fx-background-radius: 20; " +
                                 "-fx-cursor: hand;" +
                                 "-fx-min-width: 150;"); // Set minimum width
            discussButton.setOnAction(e -> {
                System.out.println("Discussion button clicked for: " + recommendation.getLocationName());
                openDiscussion(recommendation);
            });
            
            System.out.println("Created discussion button");
            buttonBox.getChildren().add(discussButton);

            // Bottom container combining rating and button
            HBox bottomBox = new HBox();
            bottomBox.setAlignment(Pos.CENTER);
            bottomBox.setPadding(new Insets(10));
            bottomBox.setSpacing(20);
            bottomBox.setMinHeight(50); // Ensure minimum height
            
            // Add rating box to left and button box to right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            bottomBox.getChildren().addAll(ratingBox, spacer, buttonBox);
            
            System.out.println("Added button to bottom box");

            // Add all components to card with explicit spacing
            card.getChildren().addAll(
                imageView,
                nameLabel,
                descLabel,
                bottomBox
            );
            
            System.out.println("Completed card creation");

            // Add margin between cards
            VBox.setMargin(card, new Insets(0, 0, 20, 0));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating recommendation card: " + e.getMessage());
        }

        return card;
    }

    private void openDiscussion(Recommendation recommendation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecomDiscuss.fxml"));
            Parent root = loader.load();

            DiscussionController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            // Pass the image data directly
            controller.setLocationDetails(
                recommendation.getLocationName(),
                recommendation.getDescription(),
                recommendation.getImageData()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Diskusi - " + recommendation.getLocationName());

            // Set minimum size for discussion window
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to open discussion: " + e.getMessage());
        }
    }

    public void refreshRecommendations() {
        loadRecommendations();
    }
}
