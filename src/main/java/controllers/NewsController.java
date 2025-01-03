package controllers;

import dao.NewsDAO;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.News;

import java.util.List;

public class NewsController {
    @FXML private TextField searchField;
    @FXML private GridPane newsGrid;
    @FXML private VBox breakingNewsContainer;
    
    private NewsDAO newsDAO;
    private static final int COLUMNS = 3;
    
    @FXML
    public void initialize() {
        newsDAO = new NewsDAO();
        
        // Setup search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchNews(newValue);
        });
        
        // Initial load
        loadAllNews();
        
        // Load breaking news
        loadBreakingNews();
    }
    
    private void loadAllNews() {
        List<News> newsList = newsDAO.getAllNews();
        displayNewsInGrid(newsList);
    }

    private void loadBreakingNews() {
        List<News> breakingNews = newsDAO.getBreakingNews();
        if (!breakingNews.isEmpty()) {
            News latestBreakingNews = breakingNews.get(0);
            displayBreakingNews(latestBreakingNews);
        }
    }
    
    private void searchNews(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllNews();
            return;
        }
        
        List<News> searchResults = newsDAO.searchNews(keyword.trim().toLowerCase());
        displayNewsInGrid(searchResults);
    }
    
    private void displayNewsInGrid(List<News> newsList) {
        Platform.runLater(() -> {
            newsGrid.getChildren().clear();
            
            int row = 0;
            int col = 0;
            
            for (News news : newsList) {
                if (news.isBreakingNews()) continue; // Skip breaking news
                
                StackPane newsCard = createNewsCard(news);
                newsGrid.add(newsCard, col, row);
                
                col++;
                if (col == COLUMNS) {
                    col = 0;
                    row++;
                }
            }
        });
    }
    
    private void displayBreakingNews(News news) {
        Platform.runLater(() -> {
            breakingNewsContainer.getChildren().clear();
            
            StackPane breakingNewsCard = createBreakingNewsCard(news);
            breakingNewsContainer.getChildren().add(breakingNewsCard);
        });
    }
    
    private StackPane createNewsCard(News news) {
        StackPane card = new StackPane();
        card.getStyleClass().add("news-card");
        
        // Background image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(news.getImageUrl());
            imageView.setImage(image);
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            // Use placeholder image if loading fails
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.jpg")));
        }
        
        // Overlay with gradient
        VBox overlay = new VBox();
        overlay.getStyleClass().add("news-card-overlay");
        
        // Title
        Label titleLabel = new Label(news.getTitle());
        titleLabel.getStyleClass().add("news-card-title");
        
        overlay.getChildren().addAll(titleLabel);
        card.getChildren().addAll(imageView, overlay);
        
        // Add hover effect
        card.setOnMouseEntered(this::handleCardHover);
        card.setOnMouseExited(this::handleCardHover);
        
        return card;
    }
    
    private StackPane createBreakingNewsCard(News news) {
        StackPane card = new StackPane();
        card.getStyleClass().add("breaking-news");
        
        // Background image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(news.getImageUrl());
            imageView.setImage(image);
            imageView.setFitWidth(800);
            imageView.setFitHeight(400);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.jpg")));
        }
        
        // Overlay with gradient
        VBox overlay = new VBox();
        overlay.getStyleClass().add("news-card-overlay");
        
        // Title and description
        Label titleLabel = new Label(news.getTitle());
        titleLabel.getStyleClass().add("breaking-news-title");
        
        Label descLabel = new Label(news.getDescription());
        descLabel.getStyleClass().add("breaking-news-description");
        
        overlay.getChildren().addAll(titleLabel, descLabel);
        card.getChildren().addAll(imageView, overlay);
        
        return card;
    }
    
    private void handleCardHover(MouseEvent event) {
        Node source = (Node) event.getSource();
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), source);
        
        if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
        } else {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
        }
        
        scaleTransition.play();
    }
}
