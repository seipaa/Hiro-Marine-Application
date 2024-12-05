package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import models.News;

public class NewsDetailController {
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ImageView newsImageView;
    
    public void setNews(News news) {
        titleLabel.setText(news.getTitle());
        descriptionLabel.setText(news.getDescription());
        // Set image jika ada
    }
} 