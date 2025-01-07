package controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import dao.MarineSpeciesDAO;
import dao.NewsDAO;
import dao.RecommendationDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Cursor;
import models.*;
import utils.AlertUtils;
import utils.ChallengeDetailsFetcher;
import utils.DatabaseHelper;
import utils.LeaderboardFetcher;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import dao.ChallengeDAO;

public class MainController {

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab adminPanelTab;
    @FXML
    private AnchorPane adminPane;
    @FXML
    private AnchorPane contentPane;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button adminButton;
    @FXML
    private Button profileButton;
    @FXML
    private VBox leaderboardVBox;
    @FXML
    private Label firstPlaceLabel;
    @FXML
    private Label secondPlaceLabel;
    @FXML
    private Label thirdPlaceLabel;
    @FXML
    private Label firstPlacePoints;
    @FXML
    private Label secondPlacePoints;
    @FXML
    private Label thirdPlacePoints;
    @FXML
    private Label nextRankLabel;
    @FXML
    private Label newsLabel1;
    @FXML
    private Label newsLabel2;
    @FXML
    private Label newsLabel3;
    @FXML
    private StackPane newsBox1;
    @FXML
    private StackPane newsBox2;
    @FXML
    private StackPane newsBox3;
    @FXML
    private Rectangle newsRect1;
    @FXML
    private Rectangle newsRect2;
    @FXML
    private Rectangle newsRect3;
    @FXML
    private Button newsDetailButton;
    @FXML
    private ListView<String> newsListView;
    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private VBox challengeListContainer;
    @FXML
    private ScrollPane newsScrollPane;
    @FXML
    private VBox newsContainer;
    @FXML
    private ListView<MarineSpecies> speciesListView;
    @FXML
    private FlowPane speciesFlowPane;
    @FXML
    private TextField speciesSearchField;
    @FXML
    private Button newsAdminButton;
    @FXML
    private Button marineSpeciesAdminButton;
    @FXML
    private Button recommendationAdminButton;
    @FXML
    private Button challengeAdminButton;
    @FXML
    private Button userAdminButton;
    @FXML
    private Button discussionButton;
    @FXML
    private Button discussionButton1;
    @FXML
    private Button discussionButton2;
    @FXML
    private Button discussionButton11;
    @FXML
    private Button discussionButton21;
    @FXML
    private Button discussionButton12;
    @FXML
    private Button discussionButton22;
    @FXML
    private Button discussionButton111;
    @FXML
    private Button clearSearchButton;

    private User currentUser;
    private Admin currentAdmin;
    private boolean isAdmin = false;
    private ChallengeDetailsFetcher detailsFetcher = new ChallengeDetailsFetcher();
    private LeaderboardFetcher leaderboardFetcher = new LeaderboardFetcher();
    private NewsDAO newsDAO;
    private AdminController adminController;
    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();
    private ObservableList<MarineSpecies> speciesList;

    private double xOffset = 0;
    private double yOffset = 0;

    private List<News> newsCache;
    private Map<Integer, Image> imageCache = new HashMap<>();

    private Timeline leaderboardRefreshTimeline;

    private ChallengeDAO challengeDAO = new ChallengeDAO();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && usernameLabel != null) {
            usernameLabel.setText(user.getName());
            System.out.println("Set current user: " + user.getName());
            usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

            // Refresh challenges setelah user diset
            refreshChallenges();
            // Load news after login
            loadNewsIfNeeded();
        }
        updateUsernameLabel();
    }

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        updateUsernameLabel();
        if (admin != null) {
            refreshChallenges();  // Refresh challenges setelah admin login
            // Load news after admin login
            loadNewsIfNeeded();
        }
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (adminButton != null) {
            adminButton.setVisible(isAdmin);
        }
        if (!isAdmin) {
            tabPane.getTabs().remove(adminPanelTab);
        } else {
            if (!tabPane.getTabs().contains(adminPanelTab)) {
                tabPane.getTabs().add(adminPanelTab);
            }
        }
    }

    private void updateUsernameLabel() {
        if (usernameLabel != null) {
            if (isAdmin && currentAdmin != null) {
                usernameLabel.setText(currentAdmin.getName());
            } else if (currentUser != null) {
                usernameLabel.setText(currentUser.getName());
            } else {
                usernameLabel.setText("");
            }
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing MainController...");
        
        // Initialize components
        setupTabListeners();
        setupSearchListeners();
        setupButtonStyles();
        
        // Load initial leaderboard
        updateLeaderboard();
        
        // Setup auto-refresh leaderboard setiap 30 detik
        leaderboardRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), event -> updateLeaderboard())
        );
        leaderboardRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        leaderboardRefreshTimeline.play();
    }

    private void setupTabListeners() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                switch(newTab.getText()) {
                    case "MARINE SPECIES":
                        refreshSpeciesView();
                        break;
                    case "NEWS":
                        loadNewsIfNeeded();
                        break;
                }
            }
        });
    }

    private void loadNewsIfNeeded() {
        if (newsCache == null) {
            if (newsDAO == null) {
                newsDAO = new NewsDAO();
            }
            newsCache = newsDAO.getAllNews();
            displayNews(newsCache);
        }
    }

    private void filterSpecies(String newValue) {
        speciesFlowPane.getChildren().clear();
        ObservableList<MarineSpecies> filteredSpecies;

        if (newValue == null || newValue.isEmpty()) {
            if (speciesList == null) {
                speciesList = marineSpeciesDAO.getAllMarineSpecies();
            }
            filteredSpecies = speciesList;
        } else {
            filteredSpecies = marineSpeciesDAO.searchSpecies(newValue);
        }

        for (MarineSpecies species : filteredSpecies) {
            speciesFlowPane.getChildren().add(createSpeciesCard(species));
        }

        speciesFlowPane.setAlignment(Pos.TOP_LEFT);
    }

    private void setupButtonHoverEffect(Button button, String color1, String color2) {
        button.setStyle("-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + ");" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(to right, " + color2 + ", " + color1 + ");" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + ");" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"));
    }

    void refreshSpeciesView() {
        if (speciesFlowPane != null) {
            speciesFlowPane.getChildren().clear();
            if (speciesList == null) {
                speciesList = marineSpeciesDAO.getAllMarineSpecies();
            }

            for (MarineSpecies species : speciesList) {
                VBox card = createSpeciesCard(species);
                if (card != null) {
                    speciesFlowPane.getChildren().add(card);
                }
            }
        }
    }

    private VBox createSpeciesCard(MarineSpecies species) {
        // Card container
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setPrefHeight(250);
        card.setMaxWidth(200);
        card.setMinWidth(200);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 15;");

        // Image container
        StackPane imageContainer = new StackPane();
        imageContainer.setMinHeight(150);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        try {
            Image cachedImage = imageCache.get(species.getId());
            if (cachedImage != null) {
                imageView.setImage(cachedImage);
            } else {
                byte[] imageData = marineSpeciesDAO.getSpeciesImage(species.getId());
                if (imageData != null && imageData.length > 0) {
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    imageCache.put(species.getId(), image);
                    imageView.setImage(image);
                } else {
                    loadDefaultImage(imageView);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image for species " + species.getName() + ": " + e.getMessage());
            loadDefaultImage(imageView);
        }

        imageContainer.getChildren().add(imageView);

        // Species name
        Label nameLabel = new Label(species.getName());
        nameLabel.setStyle("-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white; " +
                "-fx-alignment: center; " +
                "-fx-text-alignment: center;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        // Add components to card
        card.getChildren().addAll(imageContainer, nameLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 15; " +
                    "-fx-cursor: hand;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 15;");
        });

        // Add click event
        card.setOnMouseClicked(e -> showMarineSpeciesDetails(species));

        return card;
    }

    private void loadDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_species.jpg"));
            imageView.setImage(defaultImage);
        } catch (Exception ex) {
            System.err.println("Error loading default image");
        }
    }

    private void showMarineSpeciesDetails(MarineSpecies species) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/marinespecies.fxml"));
            Parent root = loader.load();

            MarineSpeciesController controller = loader.getController();
            controller.setSpeciesData(species);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(species.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load species details: " + e.getMessage());
        }

        System.out.print("Initializing MainController...\n");

        // Initialize containers
        if (challengeListContainer == null) {
            System.err.println("Warning: challengeListContainer is null!");
            return;
        }

        // Set custom scrollbar style
        if (challengeListContainer.getParent() != null && challengeListContainer.getParent().getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) challengeListContainer.getParent().getParent();

            // Basic transparent style
            scrollPane.setStyle(
                    "-fx-background: transparent; " +
                            "-fx-background-color: transparent; " +
                            "-fx-control-inner-background: transparent; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-width: 0; " +
                            "-fx-padding: 0;"
            );

            // Make viewport transparent
            Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }

            // Make corner transparent
            Node corner = scrollPane.lookup(".corner");
            if (corner != null) {
                corner.setStyle("-fx-background-color: transparent;");
            }

            // Simple scrollbar style
            String scrollBarStyle =
                    ".scroll-pane { -fx-background-color: transparent; } " +
                            ".scroll-pane > .viewport { -fx-background-color: transparent; } " +
                            ".scroll-pane > .corner { -fx-background-color: transparent; } " +
                            ".scroll-bar:vertical { -fx-background-color: transparent; } " +
                            ".scroll-bar:vertical .track { -fx-background-color: transparent; } " +
                            ".scroll-bar:vertical .thumb { -fx-background-color: rgba(255,255,255,0.2); } " +
                            ".scroll-bar:vertical .increment-button, " +
                            ".scroll-bar:vertical .decrement-button { -fx-background-color: transparent; }";

            scrollPane.setStyle(scrollPane.getStyle() + scrollBarStyle);
        }

        // Load initial data
        updateLeaderboard();
        newsDAO = new NewsDAO();
        loadNewsFromDatabase();
        loadAllNewsToView();
        loadLeaderboard();
        refreshChallenges();

        // Setup news click handlers
        newsBox1.setOnMouseClicked(event -> showNewsDetails("Waktu Hampir Habis untuk Selamatkan Hiu"));
        newsBox2.setOnMouseClicked(event -> showNewsDetails("Pemutihan Terumbu Karang Akibat Panas Laut"));
        newsBox3.setOnMouseClicked(event -> showNewsDetails("Penembak Singa Laut Diburu, yang Menemukan Dihadiahi Rp312 Juta"));

        // Set style untuk ScrollPane
        if (challengeListContainer != null && challengeListContainer.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) challengeListContainer.getParent();
            scrollPane.setStyle("-fx-background-color: transparent;");
            scrollPane.getStyleClass().add("transparent-scroll");
        }

        // Tambahkan listener untuk resize
        contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        Stage stage = (Stage) newWindow;

                        if (!contentPane.getChildren().isEmpty() && contentPane.getChildren().get(0) instanceof Pane) {
                            Pane mainPane = (Pane) contentPane.getChildren().get(0);
                            mainPane.prefWidthProperty().bind(stage.widthProperty());
                            mainPane.prefHeightProperty().bind(stage.heightProperty());

                            if (!mainPane.getChildren().isEmpty()) {
                                // Bind background image
                                if (mainPane.getChildren().get(0) instanceof ImageView) {
                                    ImageView backgroundImage = (ImageView) mainPane.getChildren().get(0);
                                    backgroundImage.fitWidthProperty().bind(stage.widthProperty());
                                    backgroundImage.fitHeightProperty().bind(stage.heightProperty());
                                }

                                // Bind overlay rectangle
                                if (mainPane.getChildren().size() > 1 && mainPane.getChildren().get(1) instanceof Rectangle) {
                                    Rectangle overlay = (Rectangle) mainPane.getChildren().get(1);
                                    overlay.widthProperty().bind(stage.widthProperty());
                                    overlay.heightProperty().bind(stage.heightProperty());
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    @FXML
    private void handleDiscussionButtonClick(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String locationName;

        // Tentukan lokasi berdasarkan fx:id button
        switch (sourceButton.getId()) {
            case "discussionButton":
            case "discussionButton111":
                locationName = "Pantai Pangandaran";
                break;
            case "discussionButton1":
            case "discussionButton11":
            case "discussionButton12":
                locationName = "Pantai Anyer";
                break;
            case "discussionButton2":
            case "discussionButton21":
            case "discussionButton22":
                locationName = "Pantai Carita";
                break;
            default:
                locationName = "Unknown Location";
                break;
        }

        openDiscussion(locationName);
    }

    private void displayNews() {
        News news1 = newsDAO.getNewsById(1);
        News news2 = newsDAO.getNewsById(2);
        News news3 = newsDAO.getNewsById(3);

        if (news1 != null) newsLabel1.setText(news1.getTitle());
        if (news2 != null) newsLabel2.setText(news2.getTitle());
        if (news3 != null) newsLabel3.setText(news3.getTitle());
    }

    private void showNewsDetails(String title) {
        System.out.print("Attempting to show details for: " + title + "\n");
        try {
            NewsDAO newsDAO = new NewsDAO();
            News news = newsDAO.getNewsByTitle(title);

            if (news != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/news_details.fxml"));
                Parent root = loader.load();

                NewsDetailsController controller = loader.getController();
                controller.setNews(news);

                Stage stage = new Stage();
                stage.setTitle("News Details");
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                System.out.print("News not found: " + title + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        AlertUtils.showError(title, message);
    }

    private void displayLeaderboard() {
        List<User> topUsers = leaderboardFetcher.getTopUsers();

        if (topUsers.size() >= 3) {
            firstPlaceLabel.setText("1. " + topUsers.get(0).getName());
            secondPlaceLabel.setText("2. " + topUsers.get(1).getName());
            thirdPlaceLabel.setText("3. " + topUsers.get(2).getName());
        }
    }

    @FXML
    private void displayChallengeDetails(Challenge challenge) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/challenge_details.fxml"));
            Parent root = loader.load();

            ChallengeDetailsController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setMainController(this);
            controller.loadChallengeDetails(challenge);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(challenge.getTitle());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal menampilkan detail challenge: " + e.getMessage());
        }
    }

    @FXML
    private void showUserDetails(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String buttonText = sourceButton.getText();
        String userName = buttonText.split("\\. ")[1];

        User user = leaderboardFetcher.getUserByName(userName);
        if (user != null) {
            showAlert("User Details", user.getName() + " memiliki " + user.getTotalPoints() + " poin.");
        } else {
            showAlert("Error", "User tidak ditemukan.");
        }
    }

    private void showAlert(String title, String message) {
        AlertUtils.showInfo(title, message);
    }

    // Metode untuk membuka panel admin
    @FXML
    private void openAdminPanel(ActionEvent event) {
        if (!isAdmin) {
            showError("Access Denied", "You do not have permission to access the admin panel.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_view.fxml"));
            Parent root = loader.load();

            adminController = loader.getController();
            adminController.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Admin Panel");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load admin panel.");
        }
    }

    // Metode untuk memperbarui leaderboard
    public void updateLeaderboard() {
        Platform.runLater(() -> {
            try {
                List<User> topUsers = leaderboardFetcher.getTopUsers();
                
                if (topUsers.size() >= 1) {
                    firstPlaceLabel.setText(topUsers.get(0).getName());
                    firstPlacePoints.setText(topUsers.get(0).getTotalPoints() + " pts");
                }
                
                if (topUsers.size() >= 2) {
                    secondPlaceLabel.setText(topUsers.get(1).getName());
                    secondPlacePoints.setText(topUsers.get(1).getTotalPoints() + " pts");
                }
                
                if (topUsers.size() >= 3) {
                    thirdPlaceLabel.setText(topUsers.get(2).getName());
                    thirdPlacePoints.setText(topUsers.get(2).getTotalPoints() + " pts");
                }
                
                // Update next rank untuk current user
                if (currentUser != null) {
                    updateNextRank(currentUser.getId());
                }
                
                // Refresh challenges juga
                refreshChallenges();
                
            } catch (Exception e) {
                System.err.println("Error updating leaderboard: " + e.getMessage());
            }
        });
    }

    private void updateNextRank(int userId) {
        try {
            String query = "SELECT name, total_points, " +
                          "(SELECT COUNT(*) + 1 FROM users u2 WHERE u2.total_points > u1.total_points) as rank " +
                          "FROM users u1 " +
                          "WHERE id = ?";
                          
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int rank = rs.getInt("rank");
                    nextRankLabel.setText("Peringkat Kamu: #" + rank);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating next rank: " + e.getMessage());
        }
    }

    @FXML
    public void showMarineSpecies() {
        try {
            AnchorPane marineSpeciesPane = FXMLLoader.load(getClass().getResource("/fxml/MarineSpecies.fxml"));
            contentPane.getChildren().setAll(marineSpeciesPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserProfile() {
        if (currentUser == null) {
            AlertUtils.showError("Error", "No user is currently logged in");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_profile.fxml"));
            Parent root = loader.load();

            UserProfileController controller = loader.getController();
            controller.setMainController(this);
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Profile");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.showError("Error", "Failed to open profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateUsername(String newUsername) {
        if (usernameLabel != null) {
            usernameLabel.setText(newUsername);
        }
    }

    public AnchorPane getContentPane() {
        return contentPane;
    }

    private void loadNewsFromDatabase() {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String query = "SELECT * FROM news ORDER BY id ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            StringBuilder newsOutput = new StringBuilder();
            while (resultSet.next()) {
                News news = new News(
                        resultSet.getInt("id"),
                        resultSet.getInt("admin_id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("image_url"),
                        resultSet.getTimestamp("created_at"),
                        resultSet.getBoolean("is_breaking_news")
                );
                if (newsOutput.length() > 0) newsOutput.append("\n");
                newsOutput.append("Loaded news: ").append(news.getTitle());
            }
            System.out.print(newsOutput.toString());
        } catch (SQLException e) {
            System.err.print("Error fetching news: " + e.getMessage());
        }
    }

    @FXML
    private void showNewsDetails() {
        try {
            // Ambil berita dari database
            NewsDAO newsDAO = new NewsDAO();
            News news = newsDAO.getNewsById(1); // Ganti dengan ID yang sesuai

            // Load FXML untuk detail berita
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/news_details.fxml"));
            Parent root = loader.load();

            // Set berita di controller detail
            NewsDetailsController controller = loader.getController();
            controller.setNews(news);

            // Tampilkan popup
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("News Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNews() {
        try {
            List<News> newsList = newsDAO.getAllNews();
            displayNews(newsList);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Gagal memuat berita: " + e.getMessage());
        }
    }

    public void loadRecommendations() {
        try {
            RecommendationDAO recommendationDAO = new RecommendationDAO();
            List<Recommendation> recommendations = recommendationDAO.getAllRecommendations();
            // Display recommendations in the UI
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load recommendations.");
        }
    }

    public void showNewsDetails(News news) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/news_details.fxml"));
            Parent root = loader.load();

            NewsDetailsController controller = loader.getController();
            controller.setNews(news);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("News Details");

            Scene scene = new Scene(root);
            scene.setFill(null);
            stage.setScene(scene);

            // Center the window
            stage.setOnShown(e -> {
                Stage mainStage = (Stage) contentPane.getScene().getWindow();
                stage.setX(mainStage.getX() + (mainStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(mainStage.getY() + (mainStage.getHeight() - stage.getHeight()) / 2);
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to show news details: " + e.getMessage());
        }
    }

    public void onNewsItemClicked(News selectedNews) {
        if (selectedNews != null) {
            showNewsDetails(selectedNews);
        }
    }

    @FXML
    private void handleNewsClick() {
        String selectedTitle = newsListView.getSelectionModel().getSelectedItem();
        if (selectedTitle != null) {
            showNewsDetails(selectedTitle);
        }
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    private void loadLeaderboard() {
        List<User> topUsers = leaderboardFetcher.getTopUsers();
        if (topUsers.size() > 0) {
            firstPlaceLabel.setText("1. " + topUsers.get(0).getName());
        }
        if (topUsers.size() > 1) {
            secondPlaceLabel.setText("2. " + topUsers.get(1).getName());
        }
        if (topUsers.size() > 2) {
            thirdPlaceLabel.setText("3. " + topUsers.get(2).getName());
        }
    }

    @FXML
    private void applyFilterAndSort() {
        String selectedFilter = filterComboBox.getValue();
        String selectedSort = sortComboBox.getValue();
        // Implementasi logika filter dan sort
    }

    @FXML
    private void startChallenge(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String challengeTitle = sourceButton.getText();
        // Logika untuk memulai challenge dan mengupdate progress
    }

    @FXML
    private void refreshData() {
        System.out.print("Data refreshed\n");
        updateLeaderboard();
    }

    @FXML
    private void showHelp() {
        // Menampilkan informasi bantuan
        AlertUtils.showInfo("Help", "Ini adalah panduan untuk menggunakan aplikasi ini...");
    }

    @FXML
    private void openSettings() {
        System.out.print("Settings opened\n");
    }

    @FXML
    private void logout() {
        System.out.print("User logged out\n");
    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    private void loadAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_news.fxml"));
            Parent adminRoot = loader.load();
            adminPane.getChildren().setAll(adminRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showNewsAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_news.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);

            // Set window size
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            // Get the controller and set the main controller reference
            NewsAdminController controller = loader.getController();
            controller.setMainController(this);

            // Add window dragging
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load News Admin panel: " + e.getMessage());
        }
    }

    @FXML
    private void showMarineSpeciesAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_marinespecies.fxml"));
            Parent root = loader.load();

            AdminMarineSpeciesController controller = loader.getController();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Admin Marine Species");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading admin marine species: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load admin marine species: " + e.getMessage());
        }
    }

    @FXML
    private void showRecommendationAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_recommendation.fxml"));
            Parent root = loader.load();

            RecommendationAdminController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Recommendation Admin Panel");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load Recommendation Admin panel.");
        }
    }

    @FXML
    private void showChallengeAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_challenge.fxml"));
            Parent root = loader.load();

            ChallengeAdminController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Challenge Admin Panel");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load Challenge Admin panel: " + e.getMessage());
        }
    }

    @FXML
    private void showUserAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user.fxml"));
            Parent root = loader.load();

            UserAdminController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Management");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load User Admin panel.");
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void refreshChallenges() {
        System.out.println("Refreshing challenges...");
        
        if (currentUser == null && currentAdmin == null) {
            System.out.println("No user or admin logged in!");
            return;
        }

        Platform.runLater(() -> {
            try {
                // Clear existing challenges
                if (challengeListContainer != null) {
                    challengeListContainer.getChildren().clear();
                }

                // Get challenges from database
                List<Challenge> challenges = challengeDAO.getAllChallenges();
                
                if (challenges.isEmpty()) {
                    Label noDataLabel = new Label("Tidak ada challenge yang tersedia");
                    noDataLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                    challengeListContainer.getChildren().add(noDataLabel);
                    return;
                }

                // Create and add challenge cards
                for (Challenge challenge : challenges) {
                    VBox challengeCard = createChallengeCard(challenge);
                    if (challengeListContainer != null) {
                        challengeListContainer.getChildren().add(challengeCard);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error refreshing challenges: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private VBox createChallengeCard(Challenge challenge) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); " +
                      "-fx-background-radius: 10; " +
                      "-fx-padding: 15;");

        // Setup background image dengan fallback
        try {
            String imageUrl = challenge.getImageUrl();
            Image backgroundImage = null;
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    if (imageUrl.startsWith("http") || imageUrl.startsWith("file:")) {
                        backgroundImage = new Image(imageUrl);
                    } else {
                        backgroundImage = new Image(getClass().getResourceAsStream("/images/" + imageUrl));
                    }
                } catch (Exception e) {
                    System.err.println("Error loading challenge image: " + e.getMessage());
                }
            }
            
            // Jika gagal load image atau tidak ada image, gunakan default
            if (backgroundImage == null || backgroundImage.isError()) {
                try {
                    backgroundImage = new Image(getClass().getResourceAsStream("/images/default_challenge.jpg"));
                } catch (Exception e) {
                    System.err.println("Error loading default challenge image: " + e.getMessage());
                }
            }

            // Set background jika image berhasil dimuat
            if (backgroundImage != null && !backgroundImage.isError()) {
                BackgroundImage bgImage = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, true, true)
                );
                card.setBackground(new Background(bgImage));
            }
        } catch (Exception e) {
            System.err.println("Error setting challenge card background: " + e.getMessage());
        }

        // Challenge title
        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Points badge
        Label pointsLabel = new Label(challenge.getPoints() + " Points");
        pointsLabel.setStyle("-fx-background-color: #4CAF50; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 5 10; " +
                            "-fx-background-radius: 15;");

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);");
            card.setCursor(Cursor.HAND);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);", ""));
            card.setCursor(Cursor.DEFAULT);
        });

        // Add click handler
        card.setOnMouseClicked(e -> displayChallengeDetails(challenge));

        // Add components to card
        card.getChildren().addAll(titleLabel, pointsLabel);
        return card;
    }

    private String formatDate(String dateStr) {
        try {
            // Assuming dateStr is in format "yyyy-MM-dd"
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        } catch (Exception e) {
            return dateStr; // Return original string if parsing fails
        }
    }

    private void loadAllNewsToView() {
        try {
            List<News> allNews = newsDAO.getAllNews();
            VBox newsContainer = new VBox(15); // spacing 15 pixels
            newsContainer.setPadding(new Insets(10));

            for (News news : allNews) {
                // Buat card berita
                StackPane newsCard = createNewsCardSimple(news);
                newsContainer.getChildren().add(newsCard);
            }

            // Tambahkan VBox ke dalam ScrollPane
            if (newsScrollPane != null) {
                newsScrollPane.setContent(newsContainer);
            }
        } catch (Exception e) {
            System.err.println("Error loading news: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private StackPane createNewsCardSimple(News news) {
        StackPane card = new StackPane();
        card.setPrefHeight(140);
        card.setPrefWidth(635);
        card.setStyle("-fx-cursor: hand;");

        // Background rectangle
        Rectangle background = new Rectangle(635, 140);
        background.setArcHeight(5);
        background.setArcWidth(5);
        background.setFill(Color.web("#a5d6dc", 0.55));

        // News title
        Label titleLabel = new Label(news.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-family: 'Maiandra GD'; -fx-font-size: 18px;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPrefWidth(606);

        // Add click event
        card.setOnMouseClicked(event -> showNewsDetails(news.getTitle()));

        // Add elements to card
        card.getChildren().addAll(background, titleLabel);

        return card;
    }

    public void refreshRecommendations() {
    }

    @FXML
    private void openDiscussion(String locationName) {
        try {
            System.out.println("Opening discussion for: " + locationName);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recomDiscuss.fxml"));
            Parent root = loader.load();

            DiscussionController controller = loader.getController();
            controller.setLocationInfo(locationName);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Diskusi " + locationName);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error opening discussion window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void clearSearch() {
        speciesSearchField.clear();
        filterSpecies("");
    }

    public void handleNewsUpdated() {
        Platform.runLater(() -> {
            try {
                newsCache = newsDAO.getAllNews();
                displayNews(newsCache);
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError("Error", "Gagal memperbarui tampilan berita: " + e.getMessage());
            }
        });
    }

    private void refreshNewsContent(List<News> newsList) {
        // Implement the UI update logic here
        // This should update your news display in the main view
        // For example, updating a ListView, GridPane, or whatever container you use to display news
    }

    private void displayNews(List<News> newsList) {
        newsContainer.getChildren().clear();
        
        // Pisahkan breaking news dan berita biasa
        List<News> breakingNews = newsList.stream()
                .filter(News::isBreakingNews)
                .toList();
        
        List<News> regularNews = newsList.stream()
                .filter(news -> !news.isBreakingNews())
                .toList();

        // Tampilkan breaking news sebagai headline
        if (!breakingNews.isEmpty()) {
            VBox headlineSection = new VBox(10);
            headlineSection.setPadding(new Insets(0, 0, 20, 0));
            
            Label breakingNewsLabel = new Label("BREAKING NEWS");
            breakingNewsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ff3d3d;");
            headlineSection.getChildren().add(breakingNewsLabel);

            for (News news : breakingNews) {
                StackPane newsCard = createBreakingNewsCard(news);
                headlineSection.getChildren().add(newsCard);
            }
            
            newsContainer.getChildren().add(headlineSection);
        }

        // Tampilkan berita reguler
        if (!regularNews.isEmpty()) {
            VBox regularNewsSection = new VBox(10);
            regularNewsSection.setPadding(new Insets(20, 0, 0, 0));
            
            Label latestNewsLabel = new Label("LATEST NEWS");
            latestNewsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
            regularNewsSection.getChildren().add(latestNewsLabel);

            // Buat grid untuk berita reguler
            GridPane newsGrid = new GridPane();
            newsGrid.setHgap(20);
            newsGrid.setVgap(20);

            int col = 0;
            int row = 0;
            for (News news : regularNews) {
                StackPane newsCard = createRegularNewsCard(news);
                newsGrid.add(newsCard, col, row);
                
                col++;
                if (col == 2) { // 2 kolom per baris
                    col = 0;
                    row++;
                }
            }
            
            regularNewsSection.getChildren().add(newsGrid);
            newsContainer.getChildren().add(regularNewsSection);
        }
    }

    private StackPane createBreakingNewsCard(News news) {
        StackPane card = new StackPane();
        card.setPrefHeight(300);
        card.setPrefWidth(900);
        card.setStyle("-fx-background-color: transparent;");

        // Image
        ImageView imageView = setupNewsImage(news, 900, 300);
        
        // Dark overlay dengan gradien merah untuk breaking news
        Rectangle overlay = new Rectangle(900, 300);
        overlay.setFill(Color.rgb(255, 0, 0, 0.3));
        
        // Content container
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.BOTTOM_LEFT);

        // Breaking news badge
        Label breakingBadge = new Label("BREAKING");
        breakingBadge.setStyle(
            "-fx-background-color: #ff3d3d;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 10;" +
            "-fx-background-radius: 5;"
        );

        // Title
        Label titleLabel = new Label(news.getTitle());
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(850);

        // Description preview
        Label descLabel = new Label(news.getDescription());
        descLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: white;"
        );
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(850);
        descLabel.setMaxHeight(60);

        content.getChildren().addAll(breakingBadge, titleLabel, descLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            overlay.setFill(Color.rgb(255, 0, 0, 0.4));
            card.setCursor(Cursor.HAND);
        });
        
        card.setOnMouseExited(e -> {
            overlay.setFill(Color.rgb(255, 0, 0, 0.3));
            card.setCursor(Cursor.DEFAULT);
        });

        // Click event
        card.setOnMouseClicked(e -> showNewsDetails(news));

        card.getChildren().addAll(imageView, overlay, content);
        return card;
    }

    private StackPane createRegularNewsCard(News news) {
        StackPane card = new StackPane();
        card.setPrefHeight(200);
        card.setPrefWidth(440);
        card.setStyle("-fx-background-color: transparent;");

        // Image
        ImageView imageView = setupNewsImage(news, 440, 200);
        
        // Dark overlay
        Rectangle overlay = new Rectangle(440, 200);
        overlay.setFill(Color.rgb(42, 60, 95, 0.85));

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.BOTTOM_LEFT);

        // Title
        Label titleLabel = new Label(news.getTitle());
        titleLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(410);

        // Date
        Label dateLabel = new Label(formatNewsDate(news.getCreatedAt()));
        dateLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #cccccc;"
        );

        content.getChildren().addAll(titleLabel, dateLabel);

        // Hover effect
        card.setOnMouseEntered(e -> {
            overlay.setFill(Color.rgb(42, 60, 95, 0.75));
            card.setCursor(Cursor.HAND);
        });
        
        card.setOnMouseExited(e -> {
            overlay.setFill(Color.rgb(42, 60, 95, 0.85));
            card.setCursor(Cursor.DEFAULT);
        });

        // Click event
        card.setOnMouseClicked(e -> showNewsDetails(news));

        card.getChildren().addAll(imageView, overlay, content);
        return card;
    }

    private ImageView setupNewsImage(News news, double width, double height) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        try {
            String imageUrl = news.getImageUrl();
            Image image;
            
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                if (imageUrl.startsWith("http") || imageUrl.startsWith("file:")) {
                    image = new Image(imageUrl, true);
                } else {
                    image = new Image(getClass().getResourceAsStream("/images/" + imageUrl));
                }
                
                if (image == null || image.isError()) {
                    image = new Image(getClass().getResourceAsStream("/images/default_news.jpg"));
                }
            } else {
                image = new Image(getClass().getResourceAsStream("/images/default_news.jpg"));
            }
            
            if (image != null && !image.isError()) {
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Error loading news image: " + e.getMessage());
            // Fallback to default image or leave empty
        }

        return imageView;
    }

    private String formatNewsDate(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "";
        LocalDate date = timestamp.toLocalDateTime().toLocalDate();
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }

    private void setupSearchListeners() {
        // Add search listener
        speciesSearchField.setPromptText("Cari nama species atau nama latin...");
        speciesSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearSearchButton.setVisible(!newValue.isEmpty());
            filterSpecies(newValue);
        });
    }

    private void setupButtonStyles() {
        // Setup button hover effects
        setupButtonHoverEffect(newsAdminButton, "#2196F3", "#21CBF3");
        setupButtonHoverEffect(marineSpeciesAdminButton, "#4CAF50", "#8BC34A");
        setupButtonHoverEffect(recommendationAdminButton, "#FF9800", "#FFC107");
        setupButtonHoverEffect(challengeAdminButton, "#F44336", "#E57373");
        setupButtonHoverEffect(userAdminButton, "#9C27B0", "#BA68C8");
    }

    public void dispose() {
        if (leaderboardRefreshTimeline != null) {
            leaderboardRefreshTimeline.stop();
        }
    }
}