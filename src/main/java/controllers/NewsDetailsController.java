package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.News;
import javafx.scene.control.Button;
import javafx.stage.Window;

public class NewsDetailsController {
    @FXML private ImageView newsImage;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label adminLabel;
    @FXML private Label dateLabel;
    @FXML private HBox titleBar;
    @FXML private Button closeButton;

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage;

    @FXML
    public void initialize() {
        // Make window draggable
        if (titleBar != null) {
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            
            titleBar.setOnMouseDragged(event -> {
                if (stage != null) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
        }
        
        // Add hover effect for close button
        if (closeButton != null) {
            closeButton.setOnMouseEntered(e -> 
                closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;")
            );
            closeButton.setOnMouseExited(e -> 
                closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;")
            );
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.println("Stage set in NewsDetailsController: " + (stage != null));
    }

    public void setNews(News news) {
        // Set text content
        titleLabel.setText(news.getTitle());
        descriptionLabel.setText(news.getDescription());
        adminLabel.setText(String.valueOf(news.getAdminId()));
        dateLabel.setText(news.getCreatedAt().toString());

        // Set image
        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            try {
                String imagePath = getClass().getResource("/images/" + news.getImageUrl()).toExternalForm();
                newsImage.setImage(new Image(imagePath));
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                newsImage.setImage(null);
            }
        }
    }

    @FXML
    private void closeDialog() {
        if (stage != null) {
            stage.close();
        } else {
            System.err.println("Stage is null in closeDialog");
            // Fallback: try to get stage from scene
            if (titleBar != null && titleBar.getScene() != null) {
                Window window = titleBar.getScene().getWindow();
                if (window instanceof Stage) {
                    ((Stage) window).close();
                }
            }
        }
    }
}
