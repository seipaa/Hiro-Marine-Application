package controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
import models.*;
import utils.AlertUtils;
import utils.ChallengeDetailsFetcher;
import utils.DatabaseHelper;
import utils.LeaderboardFetcher;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.scene.input.KeyCode;

public class MainController {

    private static final int maxPoints = 100; // Definisikan maxPoints

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
    @FXML
    private ScrollPane recommendationScrollPane;

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

    private RecommendationDAO recommendationDAO;
    @FXML
    private VBox recommendationContainer;
    @FXML
    private TextField recommendationSearchField;

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
        System.out.print("Initializing MainController...\n");

        // Initialize containers
        if (challengeListContainer == null) {
            System.err.println("Warning: challengeListContainer is null!");
        }

        // Load initial data
        try {
            // Initialize DAOs
            recommendationDAO = new RecommendationDAO();
            newsDAO = new NewsDAO();

            // Load recommendations first
            refreshRecommendations();

            // Load other data
            updateLeaderboard();
            loadNewsFromDatabase();
            loadAllNewsToView();
            loadLeaderboard();
            refreshChallenges();
        } catch (Exception e) {
            System.err.println("Error loading initial data: " + e.getMessage());
            e.printStackTrace();
        }

        // Add search field listener
        if (recommendationSearchField != null) {
            recommendationSearchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    searchRecommendations();
                }
            });
        }

        // Set custom scrollbar style for recommendation container
        if (recommendationScrollPane != null) {
            recommendationScrollPane.setStyle(
                "-fx-background: transparent; " +
                "-fx-background-color: transparent;"
            );
            
            Node viewport = recommendationScrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        }
    }

    private void setupNewsClickHandlers() {
        // Setup news click handlers
        if (newsBox1 != null) {
            newsBox1.setOnMouseClicked(event -> {
                System.out.println("Clicked newsBox1: Hiu");
                showNewsDetails("Waktu Hampir Habis untuk Selamatkan Hiu");
            });
        }
        
        if (newsBox2 != null) {
            newsBox2.setOnMouseClicked(event -> {
                System.out.println("Clicked newsBox2: Singa Laut");
                showNewsDetails("Penembak Singa Laut Diburu, yang Menemukan Dihadiahi Rp312 Juta");
            });
        }
        
        if (newsBox3 != null) {
            newsBox3.setOnMouseClicked(event -> {
                System.out.println("Clicked newsBox3: Terumbu Karang");
                showNewsDetails("Pemutihan Terumbu Karang Akibat Panas Laut yang Mematikan");
            });
        }

        // Add style classes if elements exist
        if (newsBox1 != null) newsBox1.getStyleClass().add("news-card");
        if (newsBox2 != null) newsBox2.getStyleClass().add("news-card");
        if (newsBox3 != null) newsBox3.getStyleClass().add("news-card");
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

            // Tambahkan kontrol input untuk mengatur poin
            TextField pointInput = new TextField();
            pointInput.setPromptText("Set points (0 - max)");
            pointInput.setOnAction(e -> {
                try {
                    int points = Integer.parseInt(pointInput.getText());
                    if (points < 0 || points > maxPoints) {
                        throw new NumberFormatException("Points out of range");
                    }
                    // Set points logic
                    // ... existing code ...
                } catch (NumberFormatException ex) {
                    showError("Invalid Input", "Please enter a valid number within the range.");
                }
            });

            // Tambahkan kontrol input ke panel admin
            VBox adminControls = new VBox(10);
            adminControls.getChildren().add(pointInput);
            ((VBox) root).getChildren().add(adminControls);

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
        System.out.print("Updating leaderboard...\n");
        try {
            UserDAO userDAO = new UserDAO();
            final List<User> topUsers = userDAO.getTopThreeUsers();

            // Filter out frozen or banned users
            List<User> filteredTopUsers = topUsers.stream()
                    .filter(user -> !user.isFrozen() && !user.isBanned())
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                if (filteredTopUsers.size() >= 1) {
                    User firstPlace = filteredTopUsers.get(0);
                    firstPlaceLabel.setText(firstPlace.getName());
                    firstPlacePoints.setText(firstPlace.getTotalPoints() + " pts");
                }
                if (filteredTopUsers.size() >= 2) {
                    User secondPlace = filteredTopUsers.get(1);
                    secondPlaceLabel.setText(secondPlace.getName());
                    secondPlacePoints.setText(secondPlace.getTotalPoints() + " pts");
                }
                if (filteredTopUsers.size() >= 3) {
                    User thirdPlace = filteredTopUsers.get(2);
                    thirdPlaceLabel.setText(thirdPlace.getName());
                    thirdPlacePoints.setText(thirdPlace.getTotalPoints() + " pts");
                }

                // Update next rank info for current user
                if (currentUser != null) {
                    updateNextRankInfo(currentUser, filteredTopUsers);
                }
            });

            System.out.print("Top users found: " + topUsers.size() + "\n");
            for (User user : topUsers) {
                System.out.print(user.getName() + ": " + user.getTotalPoints() + " poin\n");
            }
        } catch (Exception e) {
            System.err.print("Error updating leaderboard: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void updateNextRankInfo(User currentUser, List<User> topUsers) {
        int currentPoints = currentUser.getTotalPoints();
        String nextRankInfo = "";

        if (topUsers.isEmpty()) {
            nextRankInfo = "Jadilah yang pertama di leaderboard!";
        } else if (!topUsers.get(0).getName().equals(currentUser.getName())) {
            // Find next rank target
            for (int i = topUsers.size() - 1; i >= 0; i--) {
                User rankUser = topUsers.get(i);
                if (currentPoints < rankUser.getTotalPoints()) {
                    int pointsNeeded = rankUser.getTotalPoints() - currentPoints;
                    nextRankInfo = pointsNeeded + " poin lagi untuk naik peringkat!";
                    break;
                }
            }
        } else {
            nextRankInfo = "🎉 Anda di peringkat teratas!";
        }

        nextRankLabel.setText(nextRankInfo);
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
        
        // Panggil updateLeaderboard segera setelah tantangan dimulai
        updateLeaderboard();
        
        // Logika untuk memulai challenge dan mengupdate progress
        // ... existing code ...
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
        Platform.runLater(() -> {
            try {
                System.out.println("Refreshing challenges...");

                if (challengeListContainer == null) {
                    System.err.println("Error: challengeListContainer is null!");
                    return;
                }

                // Jangan hapus tantangan yang ada
                // challengeListContainer.getChildren().clear();

                // Query berbeda untuk admin dan user
                String query;
                if (isAdmin && currentAdmin != null) {  // Pastikan admin sudah login
                    query = "SELECT * FROM challenges ORDER BY start_date DESC";
                } else if (currentUser != null) {  // Pastikan user sudah login
                    query = "SELECT c.*, " +
                            "(SELECT status FROM user_challenges uc " +
                            "WHERE uc.challenge_id = c.id AND uc.user_id = ? AND uc.status = 'COMPLETED') as completion_status " +
                            "FROM challenges c ORDER BY c.start_date DESC";
                } else {
                    System.err.println("No user or admin logged in!");
                    return;  // Keluar jika tidak ada yang login
                }

                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    ResultSet rs;
                    if (!isAdmin && currentUser != null) {
                        stmt.setInt(1, currentUser.getId());
                    }
                    rs = stmt.executeQuery();

                    while (rs.next()) {
                        Challenge challenge = new Challenge(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("description"),
                                rs.getInt("points"),
                                rs.getString("image_url"),
                                rs.getString("qr_code_url"),
                                rs.getString("start_date"),
                                rs.getString("end_date")
                        );

                        boolean isVerified = false;
                        if (!isAdmin && currentUser != null) {
                            isVerified = rs.getString("completion_status") != null;
                        }

                        // Buat kartu tantangan
                        Node challengeCard = createChallengeCard(challenge, isVerified);

                        // Tambahkan tantangan baru ke bagian atas daftar
                        challengeListContainer.getChildren().add(0, challengeCard);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showError("Error", "Gagal memuat challenge: " + e.getMessage());
            }
        });
    }

    private Node createChallengeCard(Challenge challenge, boolean isVerified) {
        // Create challenge card
        StackPane card = new StackPane();
        card.setPrefWidth(600);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-radius: 10;");

        // Background image setup...
        ImageView backgroundImage = setupBackgroundImage(challenge);
        Rectangle overlay = new Rectangle(600, 200);
        overlay.setFill(Color.rgb(26, 34, 56, 0.85));

        // Content container
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Header with title and verification badge if verified
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Title
        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        header.getChildren().add(titleLabel);

        // Add verification badge if verified (hanya untuk user biasa)
        if (!isAdmin && isVerified) {
            HBox verifiedBadge = new HBox(5);
            verifiedBadge.setAlignment(Pos.CENTER);
            verifiedBadge.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-padding: 5 10;" +
                            "-fx-background-radius: 15;"
            );

            Label checkIcon = new Label("✓");
            checkIcon.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            Label verifiedText = new Label("Terverifikasi");
            verifiedText.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            verifiedBadge.getChildren().addAll(checkIcon, verifiedText);
            header.getChildren().add(verifiedBadge);
        }

        // Date
        Label dateLabel = new Label("📅 " + challenge.getStartDate());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        // Description
        Label descLabel = new Label(challenge.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a0a8c0;");
        descLabel.setWrapText(true);

        // Points
        Label pointsLabel = new Label("🏆 " + challenge.getPoints() + " points");
        pointsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        // View Details button
        Button detailsButton = new Button("View Details");
        detailsButton.setStyle(
                "-fx-background-color: #2196F3; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 20;"
        );
        detailsButton.setOnAction(e -> displayChallengeDetails(challenge));

        // Add all elements to content
        content.getChildren().addAll(header, dateLabel, descLabel, pointsLabel, detailsButton);

        // Add all layers to card
        card.getChildren().addAll(backgroundImage, overlay, content);

        // Return the card as a Node
        return card;
    }

    private ImageView setupBackgroundImage(Challenge challenge) {
        String imagePath = "/images/" + challenge.getImageUrl();
        Image image = null;
        try {
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                throw new Exception("Image not found");
            }
            image = new Image(imageStream);
        } catch (Exception e) {
            System.err.println("Error loading challenge image: " + e.getMessage());
            image = new Image(getClass().getResourceAsStream("/images/default_challenge.jpg"));
        }
        ImageView backgroundImage = new ImageView(image);
        backgroundImage.setFitWidth(600);
        backgroundImage.setFitHeight(200);
        backgroundImage.setPreserveRatio(false);
        return backgroundImage;
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
        try {
            if (recommendationContainer == null) {
                System.err.println("Warning: recommendationContainer is null!");
                return;
            }

            List<Recommendation> recommendations = recommendationDAO.getAllRecommendations();
            
            // Clear existing content
            recommendationContainer.getChildren().clear();
            
            // Create HBox for each row (2 recommendations per row)
            HBox currentRow = null;
            
            for (int i = 0; i < recommendations.size(); i++) {
                if (i % 2 == 0) {
                    // Start new row
                    currentRow = new HBox(40); // Increased spacing between cards
                    currentRow.setAlignment(Pos.CENTER);
                    currentRow.setPadding(new Insets(10, 20, 10, 20)); // Add padding around rows
                    recommendationContainer.getChildren().add(currentRow);
                }
                
                VBox post = createRecommendationPost(recommendations.get(i));
                if (currentRow != null) {
                    currentRow.getChildren().add(post);
                }
            }
            
            // If the last row has only one card, add an empty spacer
            if (currentRow != null && currentRow.getChildren().size() == 1) {
                Region spacer = new Region();
                spacer.setPrefWidth(360); // Match new card width
                currentRow.getChildren().add(spacer);
            }
            
            // Add padding at the bottom of the container
            recommendationContainer.setPadding(new Insets(10, 0, 20, 0));
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to refresh recommendations: " + e.getMessage());
        }
    }

    private VBox createRecommendationPost(Recommendation recommendation) {
        try {
            VBox post = new VBox();
            post.setSpacing(10); // Consistent spacing
            post.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); " + 
                         "-fx-padding: 15; " + 
                         "-fx-background-radius: 15; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 4);");
            post.setPrefWidth(360); // Even smaller width for better fit
            post.setMaxWidth(360);
            post.setMinHeight(480); // Adjusted height
            post.setAlignment(Pos.TOP_CENTER); // Center align all content

            // Title with container
            VBox titleContainer = new VBox(); // Changed to VBox for better centering
            titleContainer.setAlignment(Pos.CENTER);
            titleContainer.setPadding(new Insets(0, 0, 5, 0));
            
            Label titleLabel = new Label(recommendation.getLocationName());
            titleLabel.setStyle("-fx-font-size: 18px; " +
                             "-fx-font-weight: bold; " +
                              "-fx-text-fill: #1a2980; " +
                              "-fx-text-alignment: center;");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(320);
            titleContainer.getChildren().add(titleLabel);

            // Image container
            VBox imageContainer = new VBox();
            imageContainer.setStyle("-fx-background-color: rgba(248, 249, 250, 0.5); " +
                                  "-fx-padding: 8; " +
                                  "-fx-background-radius: 12; " +
                                  "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 2);");
            imageContainer.setAlignment(Pos.CENTER);
            
            ImageView imageView = new ImageView();
            imageView.setFitWidth(320);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);

            // Load image
            byte[] imageData = recommendation.getImageData();
            if (imageData != null && imageData.length > 0) {
                try {
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    if (!image.isError()) {
                        imageView.setImage(image);
                    } else {
                        loadDefaultImage(imageView);
                    }
                } catch (Exception e) {
                    loadDefaultImage(imageView);
                }
            } else {
                loadDefaultImage(imageView);
            }
            
            imageContainer.getChildren().add(imageView);

            // Description container
            VBox descContainer = new VBox();
            descContainer.setStyle("-fx-background-color: rgba(248, 249, 250, 0.5); " +
                                 "-fx-padding: 10; " +
                                 "-fx-background-radius: 12;");
            descContainer.setAlignment(Pos.CENTER);
            
            TextArea descArea = new TextArea(recommendation.getDescription());
            descArea.setWrapText(true);
            descArea.setEditable(false);
            descArea.setPrefRowCount(3);
            descArea.setPrefWidth(300);
            descArea.setStyle("-fx-background-color: transparent; " +
                            "-fx-text-fill: #2c3e50; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 0; " +
                            "-fx-background-insets: 0; " +
                            "-fx-focus-color: transparent; " +
                            "-fx-faint-focus-color: transparent;");
            descContainer.getChildren().add(descArea);

            // Rating Box
            HBox ratingBox = createRatingBox(recommendation);
            ratingBox.setStyle("-fx-padding: 8; " +
                             "-fx-background-color: rgba(248, 249, 250, 0.5); " +
                             "-fx-background-radius: 12;");
            ratingBox.setAlignment(Pos.CENTER); // Center align rating box

            // Button container
            HBox buttonContainer = new HBox();
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.setPadding(new Insets(5, 0, 0, 0));

            Button discussButton = new Button("Diskusi Rekomendasi");
            discussButton.setStyle("-fx-background-color: rgba(26, 41, 128, 0.8); " +
                                 "-fx-text-fill: white; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-padding: 8 20; " +
                                 "-fx-background-radius: 20; " +
                                 "-fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
            
            // Hover effects
            discussButton.setOnMouseEntered(e -> {
                discussButton.setStyle(discussButton.getStyle()
                    .replace("rgba(26, 41, 128, 0.8)", "rgba(42, 60, 95, 0.9)")
                    .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1)",
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2)"));
            });
            discussButton.setOnMouseExited(e -> {
                discussButton.setStyle(discussButton.getStyle()
                    .replace("rgba(42, 60, 95, 0.9)", "rgba(26, 41, 128, 0.8)")
                    .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2)",
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1)"));
            });

            discussButton.setOnAction(e -> openDiscussion(recommendation));
            buttonContainer.getChildren().add(discussButton);

            // Add all components with consistent spacing
            post.getChildren().addAll(
                titleContainer,
                imageContainer,
                descContainer,
                ratingBox,
                buttonContainer
            );

            // Add consistent spacing between elements
            VBox.setMargin(imageContainer, new Insets(5, 0, 5, 0));
            VBox.setMargin(descContainer, new Insets(5, 0, 5, 0));
            VBox.setMargin(ratingBox, new Insets(5, 0, 5, 0));
            VBox.setMargin(buttonContainer, new Insets(5, 0, 0, 0));

            return post;
        } catch (Exception e) {
            System.err.println("Error creating recommendation post: " + e.getMessage());
            e.printStackTrace();
            
            // Return a simple error card
            VBox errorCard = new VBox();
            errorCard.setStyle("-fx-background-color: rgba(255, 235, 238, 0.7); " +
                             "-fx-padding: 15; " +
                             "-fx-background-radius: 12;");
            errorCard.setAlignment(Pos.CENTER);
            Label errorLabel = new Label("Error loading recommendation");
            errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 13px;");
            errorCard.getChildren().add(errorLabel);
            return errorCard;
        }
    }

    private HBox createRatingBox(Recommendation recommendation) {
        // Main container with reduced height
        HBox ratingBox = new HBox(10); // Reduced spacing
        ratingBox.setAlignment(Pos.CENTER);
        ratingBox.setPrefHeight(60); // Reduced height
        ratingBox.setStyle("-fx-background-color: rgba(26, 41, 128, 0.05); " +
                          "-fx-padding: 8 12; " + // Reduced padding
                          "-fx-background-radius: 10; " +
                          "-fx-border-color: rgba(26, 41, 128, 0.1); " +
                          "-fx-border-radius: 10; " +
                          "-fx-border-width: 1;");

        // Left section: Current Rating Display (smaller)
        VBox ratingDisplay = new VBox(3); // Reduced spacing
        ratingDisplay.setAlignment(Pos.CENTER);
        ratingDisplay.setPrefWidth(80); // Reduced width
        ratingDisplay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); " +
                             "-fx-padding: 5; " + // Reduced padding
                             "-fx-background-radius: 8;");
        
        Label ratingLabel = new Label("Rating");
        ratingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-font-weight: bold;");
        
        Label ratingValueLabel = new Label(String.format("%.1f", recommendation.getRating()));
        ratingValueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a2980;");
        
        ratingDisplay.getChildren().addAll(ratingLabel, ratingValueLabel);

        // Center section: Stars display (adjusted size)
        HBox starsBox = new HBox(2); // Reduced spacing
        starsBox.setAlignment(Pos.CENTER);
        starsBox.setPrefWidth(100); // Reduced width
        starsBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); " +
                         "-fx-padding: 5; " + // Reduced padding
                         "-fx-background-radius: 8;");
        
        for (int i = 0; i < 5; i++) {
            Label starLabel = new Label(i < recommendation.getRating() ? "★" : "☆");
            starLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + // Reduced font size
                             (i < recommendation.getRating() ? "#FFD700" : "#CCCCCC") + ";");
            starsBox.getChildren().add(starLabel);
        }

        // Right section: Rating Input (smaller)
        VBox ratingInputSection = new VBox(3); // Reduced spacing
        ratingInputSection.setAlignment(Pos.CENTER);
        ratingInputSection.setPrefWidth(100); // Reduced width
        ratingInputSection.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); " +
                                  "-fx-padding: 5; " + // Reduced padding
                                  "-fx-background-radius: 8;");
        
        // Rating ComboBox with adjusted styling
        ComboBox<Integer> ratingInput = new ComboBox<>();
        ratingInput.getItems().addAll(1, 2, 3, 4, 5);
        ratingInput.setPromptText("Rate");
        ratingInput.setStyle("-fx-font-size: 11px; " + // Reduced font size
                           "-fx-background-color: white; " +
                           "-fx-background-radius: 6; " +
                           "-fx-border-radius: 6; " +
                           "-fx-border-color: #1a2980; " +
                           "-fx-border-width: 1;");
        ratingInput.setPrefWidth(60); // Reduced width

        // Submit Button (smaller)
        Button submitButton = new Button("Rate");
        submitButton.setStyle("-fx-background-color: #1a2980; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 11px; " + // Reduced font size
                            "-fx-padding: 4 10; " + // Reduced padding
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold;");

        // Hover effects for submit button
        submitButton.setOnMouseEntered(e -> {
            submitButton.setStyle(submitButton.getStyle()
                .replace("#1a2980", "#2a3c5f"));
            submitButton.setEffect(new javafx.scene.effect.DropShadow(3, Color.rgb(0, 0, 0, 0.2))); // Reduced shadow
        });
        submitButton.setOnMouseExited(e -> {
            submitButton.setStyle(submitButton.getStyle()
                .replace("#2a3c5f", "#1a2980"));
            submitButton.setEffect(null);
        });

        // Handle user login state
        if (currentUser != null) {
            try {
                // Get user's existing rating
                int userRating = recommendationDAO.getUserRating(recommendation.getId(), currentUser.getId());
                if (userRating > 0) {
                    ratingInput.setValue(userRating);
                }

                // Enable rating components
                ratingInput.setDisable(false);
                submitButton.setDisable(false);

                submitButton.setOnAction(e -> {
                    Integer selectedRating = ratingInput.getValue();
                    if (selectedRating != null) {
                        try {
                            // Update rating in database
                            recommendationDAO.updateRating(recommendation.getId(), selectedRating, currentUser.getId());
                            
                            // Update average rating display
                            double newAverage = recommendationDAO.getAverageRating(recommendation.getId());
                            ratingValueLabel.setText(String.format("%.1f", newAverage));
                            
                            // Update stars display
                            starsBox.getChildren().clear();
                            for (int i = 0; i < 5; i++) {
                                Label starLabel = new Label(i < newAverage ? "★" : "☆");
                                starLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + // Reduced font size
                                                 (i < newAverage ? "#FFD700" : "#CCCCCC") + ";");
                                starsBox.getChildren().add(starLabel);
                            }
                            
                            // Show success message with animation
                            showSuccessAnimation(ratingBox);
                            
                        } catch (SQLException ex) {
                            AlertUtils.showError("Error", "Gagal memberikan rating: " + ex.getMessage());
                        }
                    } else {
                        AlertUtils.showError("Error", "Silakan pilih nilai rating (1-5)");
                    }
                });
            } catch (SQLException e) {
                System.err.println("Error getting user rating: " + e.getMessage());
            }
        } else {
            // Disable rating for non-logged in users
            ratingInput.setDisable(true);
            submitButton.setDisable(true);
            
            // Add login prompt with icon (smaller)
            Label loginPrompt = new Label("⚠️ Login untuk rating");
            loginPrompt.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666; -fx-font-weight: bold;");
            ratingInputSection.getChildren().add(loginPrompt);
        }

        // Add rating input components
        HBox inputContainer = new HBox(4); // Reduced spacing
        inputContainer.setAlignment(Pos.CENTER);
        inputContainer.getChildren().addAll(ratingInput, submitButton);
        ratingInputSection.getChildren().add(0, inputContainer);

        // Add all sections to rating box
        ratingBox.getChildren().addAll(ratingDisplay, starsBox, ratingInputSection);

        return ratingBox;
    }

    private void showSuccessAnimation(HBox ratingBox) {
        // Create success label
        Label successLabel = new Label("✓ Rating berhasil!");
        successLabel.setStyle("-fx-font-size: 12px; " +
                            "-fx-text-fill: #4CAF50; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-color: rgba(76, 175, 80, 0.1); " +
                            "-fx-padding: 5 10; " +
                            "-fx-background-radius: 5;");
        
        // Add to rating box temporarily
        ratingBox.getChildren().add(successLabel);
        
        // Create fade out transition
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), successLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> ratingBox.getChildren().remove(successLabel));
        
        // Play animation
        fadeOut.play();
    }

    private void openDiscussion(Recommendation recommendation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecomDiscuss.fxml"));
            Parent root = loader.load();

            DiscussionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
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
            AlertUtils.showError("Error", "Gagal membuka diskusi: " + e.getMessage());
        }
    }

    @FXML
    private void searchRecommendations() {
        String searchQuery = recommendationSearchField.getText().trim().toLowerCase();
        
        try {
            List<Recommendation> allRecommendations = recommendationDAO.getAllRecommendations();
            List<Recommendation> filteredRecommendations = new ArrayList<>();
            
            // Filter recommendations based on search query
            for (Recommendation rec : allRecommendations) {
                if (rec.getLocationName().toLowerCase().contains(searchQuery) ||
                    rec.getDescription().toLowerCase().contains(searchQuery)) {
                    filteredRecommendations.add(rec);
                }
            }
            
            // Clear existing content
            recommendationContainer.getChildren().clear();
            
            if (filteredRecommendations.isEmpty()) {
                // Show "no results" message
                VBox noResultsBox = new VBox();
                noResultsBox.setAlignment(Pos.CENTER);
                noResultsBox.setPadding(new Insets(50, 0, 0, 0));
                
                Label noResultsLabel = new Label("Tidak ada hasil yang ditemukan");
                noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");
                
                Button clearSearchButton = new Button("Tampilkan Semua");
                clearSearchButton.setStyle("-fx-background-color: #1a2980; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-padding: 8 20; " +
                                        "-fx-background-radius: 20; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-font-size: 14px;");
                clearSearchButton.setOnAction(e -> {
                    recommendationSearchField.clear();
                    refreshRecommendations();
                });
                
                noResultsBox.getChildren().addAll(noResultsLabel, new Region() {{ setMinHeight(20); }}, clearSearchButton);
                recommendationContainer.getChildren().add(noResultsBox);
            } else {
                // Create HBox for each row (2 recommendations per row)
                HBox currentRow = null;
                
                for (int i = 0; i < filteredRecommendations.size(); i++) {
                    if (i % 2 == 0) {
                        // Start new row
                        currentRow = new HBox(40);
                        currentRow.setAlignment(Pos.CENTER);
                        currentRow.setPadding(new Insets(10, 20, 10, 20));
                        recommendationContainer.getChildren().add(currentRow);
                    }
                    
                    VBox post = createRecommendationPost(filteredRecommendations.get(i));
                    if (currentRow != null) {
                        currentRow.getChildren().add(post);
                    }
                }
                
                // If the last row has only one card, add an empty spacer
                if (currentRow != null && currentRow.getChildren().size() == 1) {
                    Region spacer = new Region();
                    spacer.setPrefWidth(360);
                    currentRow.getChildren().add(spacer);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal mencari rekomendasi: " + e.getMessage());
        }
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

    private StackPane createRegularNewsCard(News news) {

        return null;
    }

    private StackPane createBreakingNewsCard(News news) {
        return null;
    }

    public void clearSearch(ActionEvent actionEvent) {
    }
}