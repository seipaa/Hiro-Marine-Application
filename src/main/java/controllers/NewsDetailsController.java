package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.News;

public class NewsDetailsController {
    @FXML private ImageView newsImage;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label adminLabel;
    @FXML private Label dateLabel;
    @FXML private HBox titleBar;

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
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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
        }
    }
}
