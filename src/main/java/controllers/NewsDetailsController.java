package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.News;

public class NewsDetailsController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label adminLabel;
    @FXML
    private ImageView newsImageView;

    public void setNews(News news) {
        titleLabel.setText(news.getTitle());
        descriptionLabel.setText(news.getDescription());
        adminLabel.setText("Admin ID: " + news.getAdminId());

        String imagePath;
        if (news.getTitle().contains("Hiu")) {
            imagePath = "/images/hiu.jpg";
        } else if (news.getTitle().contains("Terumbu Karang")) {
            imagePath = "/images/mangrove.jpg";
        } else if (news.getTitle().contains("Singa Laut")) {
            imagePath = "/images/singalaut.jpg";
        } else {
            imagePath = "/images/background.jpg";
        }

        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            newsImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
        }
    }
}
