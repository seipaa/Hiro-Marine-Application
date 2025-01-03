package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import dao.NewsDAO;
import models.News;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;

public class NewsDetailsController {
    private static final Logger LOGGER = Logger.getLogger(NewsDetailsController.class.getName());

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label adminLabel;

    @FXML
    private ImageView newsImageView;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox contentVBox;

    @FXML
    public void initialize() {
        // Inisialisasi komponen UI jika diperlukan
        setupScrollPane();
    }

    private void setupScrollPane() {
        if (scrollPane != null) {
            // Membuat scrolling lebih smooth
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
    }

    public void setNews(News news) {
        if (news == null) {
            LOGGER.warning("Attempted to set null news object");
            return;
        }

        try {
            // Validasi data berita
            if (news.getTitle() == null || news.getTitle().isEmpty()) {
                LOGGER.warning("News title is empty");
                titleLabel.setText("No Title");
            } else {
                titleLabel.setText(news.getTitle());
            }

            if (news.getDescription() == null || news.getDescription().isEmpty()) {
                LOGGER.warning("News description is empty");
                descriptionLabel.setText("No Description Available");
            } else {
                descriptionLabel.setText(news.getDescription());
            }

            adminLabel.setText("Posted by Admin ID: " + news.getAdminId());

            // Handle image loading
            loadNewsImage(news);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting news details", e);
            // Set default values jika terjadi error
            titleLabel.setText("Error Loading News");
            descriptionLabel.setText("Failed to load news content");
            loadFallbackImage();
        }
    }

    private void loadNewsImage(News news) {
        if (news == null) {
            loadFallbackImage();
            return;
        }

        String imagePath = determineImagePath(news);

        try {
            Image image = new Image(getClass().getClassLoader().getResourceAsStream(imagePath));
            if (image.isError()) {
                loadFallbackImage();
            } else {
                newsImageView.setImage(image);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading image: " + imagePath, e);
            loadFallbackImage();
        }
    }

    private String determineImagePath(News news) {
        if (news == null || news.getTitle() == null) {
            return "images/default.jpg";
        }

        NewsDAO newsDAO = new NewsDAO();
        List<News> allNews = newsDAO.getAllNews();
        for (News currentNews : allNews) {
            if (currentNews.getTitle().equals(news.getTitle())) {
                return currentNews.getImageUrl();
            }
        }
        return "images/default.jpg"; // Default image if not found
    }

    private void loadFallbackImage() {
        try {
            Image fallbackImage = new Image(getClass().getClassLoader().getResourceAsStream("images/background.jpg"));
            newsImageView.setImage(fallbackImage);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load fallback image", e);
        }
    }

    @FXML
    private void onBackButtonClicked() {
        // Implementasi untuk kembali ke halaman sebelumnya
        // Bisa ditambahkan sesuai kebutuhan
    }

    public void clearContent() {
        titleLabel.setText("");
        descriptionLabel.setText("");
        adminLabel.setText("");
        newsImageView.setImage(null);
    }
}
