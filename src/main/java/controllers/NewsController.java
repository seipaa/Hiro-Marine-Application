package controllers;

import dao.NewsDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import models.News;
import java.io.IOException;
import java.util.*;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class NewsController {
    private static final int CACHE_SIZE = 20;
    private static final Duration REFRESH_INTERVAL = Duration.seconds(60);
    private static final Duration SEARCH_DEBOUNCE = Duration.millis(800);
    
    @FXML private VBox breakingNewsContainer;
    @FXML private VBox newsContainer;
    @FXML private TextField searchField;
    @FXML private ScrollPane scrollPane;
    
    private NewsDAO newsDAO;
    private Timeline searchDebounceTimeline;
    private Timeline autoRefreshTimeline;
    private LinkedHashMap<String, Image> imageCache;
    
    @FXML
    public void initialize() {
        newsDAO = new NewsDAO();
        imageCache = new LinkedHashMap<String, Image>(CACHE_SIZE + 1, .75F, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            }
        };
        
        setupSearch();
        setupAutoRefresh();
        refreshNews();
    }
    
    public void dispose() {
        if (searchDebounceTimeline != null) {
            searchDebounceTimeline.stop();
        }
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        imageCache.clear();
    }

    private void setupSearch() {
        searchDebounceTimeline = new Timeline(new KeyFrame(SEARCH_DEBOUNCE, event -> {
            Platform.runLater(() -> refreshNews(searchField.getText()));
        }));
        searchDebounceTimeline.setCycleCount(1);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchDebounceTimeline != null) {
                searchDebounceTimeline.stop();
            }
            searchDebounceTimeline.playFromStart();
        });
    }

    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(REFRESH_INTERVAL, event -> {
            Platform.runLater(this::refreshNews);
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    public void refreshNews() {
        refreshNews("");
    }

    private void refreshNews(String searchQuery) {
        Platform.runLater(() -> {
            try {
                List<News> allNews = newsDAO.getAllNews(searchQuery);
                updateNewsDisplay(allNews);
            } catch (Exception e) {
                System.err.println("Error refreshing news: " + e.getMessage());
            }
        });
    }
    
    private void updateNewsDisplay(List<News> allNews) {
        Platform.runLater(() -> {
            breakingNewsContainer.getChildren().clear();
            newsContainer.getChildren().clear();
            
            // Process breaking news first
            allNews.stream()
                  .filter(News::isBreakingNews)
                  .forEach(news -> {
                      VBox card = createNewsCard(news, true);
                      breakingNewsContainer.getChildren().add(card);
                  });
            
            // Then process regular news
            allNews.stream()
                  .filter(news -> !news.isBreakingNews())
                  .forEach(news -> {
                      VBox card = createNewsCard(news, false);
                      newsContainer.getChildren().add(card);
                  });
        });
    }

    private VBox createNewsCard(News news, boolean isBreakingNews) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); " +
                     "-fx-background-radius: 10; " +
                     "-fx-padding: 15; " +
                     "-fx-cursor: hand; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        
        // Set ukuran card
        if (isBreakingNews) {
            card.setPrefWidth(700);
            card.setPrefHeight(300);
        } else {
            card.setPrefWidth(300);
            card.setPrefHeight(200);
        }
        
        // Image container with center alignment
        StackPane imageContainer = new StackPane();
        imageContainer.setAlignment(Pos.CENTER);
        
        // Load and cache image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(isBreakingNews ? 650 : 270);
        imageView.setFitHeight(isBreakingNews ? 200 : 120);
        imageView.setPreserveRatio(true);
        
        try {
            Image image = imageCache.computeIfAbsent(news.getImageUrl(), url -> {
                try {
                    return new Image(url, true);
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + url);
                    return null;
                }
            });
            
            if (image != null) {
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        
        imageContainer.getChildren().add(imageView);
        
        // Create title label
        Label titleLabel = new Label(news.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: " + (isBreakingNews ? "18px" : "14px") + "; " +
                          "-fx-font-weight: bold; " +
                          "-fx-text-fill: white;");
        
        // Create description label with max 2 lines
        Label descLabel = new Label(news.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e0e0e0;");
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); " +
                         "-fx-background-radius: 10; " +
                         "-fx-padding: 15; " +
                         "-fx-cursor: hand; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 0);");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); " +
                         "-fx-background-radius: 10; " +
                         "-fx-padding: 15; " +
                         "-fx-cursor: hand; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        });
        
        // Add click handler
        card.setOnMouseClicked(e -> showNewsDetails(news));
        
        // Add all components to card
        card.getChildren().addAll(imageContainer, titleLabel, descLabel);
        return card;
    }

    private void showNewsDetails(News news) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/news_details.fxml"));
            Parent root = loader.load();
            
            NewsDetailsController controller = loader.getController();
            
            // Create stage with style before setting scene
            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.setFill(null);
            stage.setScene(scene);

            // Set news data after stage and scene are set
            controller.setStage(stage);
            controller.setNews(news);

            // Center the window
            Stage mainStage = (Stage) newsContainer.getScene().getWindow();
            stage.setX(mainStage.getX() + (mainStage.getWidth() - 600) / 2);
            stage.setY(mainStage.getY() + (mainStage.getHeight() - 500) / 2);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error showing news details: " + e.getMessage());
        }
    }
}