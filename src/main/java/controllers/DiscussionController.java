package controllers;

import dao.RecomDiscussDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.VBox;
import models.RecomDiscuss;
import models.User;
import utils.AlertUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DiscussionController {
    @FXML private Label locationLabel;
    @FXML private ImageView recommendationImage;
    @FXML private TextArea recommendationDescription;
    @FXML private VBox discussionVBox;
    @FXML private TextArea descriptionArea;
    @FXML private Button uploadImageBtn;
    @FXML private Button submitButton;

    private RecomDiscussDAO discussionDAO = new RecomDiscussDAO();
    private String currentLocationName;
    private User currentUser;
    private File selectedImageFile;
    private byte[] imageData;

    @FXML
    public void initialize() {
        uploadImageBtn.setOnAction(e -> chooseImage());
        submitButton.setOnAction(e -> submitDiscussion());

        // Set default styles
        discussionVBox.setSpacing(15);
    }

    public void setLocationDetails(String locationName, String description, byte[] imageData) {
        this.currentLocationName = locationName;
        this.imageData = imageData;
        locationLabel.setText(locationName);
        recommendationDescription.setText(description);

        try {
            if (imageData != null && imageData.length > 0) {
                Image image = new Image(new ByteArrayInputStream(imageData));
                if (!image.isError()) {
                    recommendationImage.setImage(image);
                    recommendationImage.setFitWidth(400);  // Set appropriate size
                    recommendationImage.setFitHeight(300);
                    recommendationImage.setPreserveRatio(true);
                } else {
                    loadDefaultImage();
                }
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            loadDefaultImage();
        }

        loadDiscussions();
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_location.jpg"));
            if (!defaultImage.isError()) {
                recommendationImage.setImage(defaultImage);
                recommendationImage.setFitWidth(400);
                recommendationImage.setFitHeight(300);
                recommendationImage.setPreserveRatio(true);
            }
        } catch (Exception ex) {
            System.err.println("Error loading default image: " + ex.getMessage());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadDiscussions() {
        try {
            List<RecomDiscuss> discussions = discussionDAO.getDiscussionsByLocation(currentLocationName);
            discussionVBox.getChildren().clear();

            for (RecomDiscuss discussion : discussions) {
                discussionVBox.getChildren().add(createDiscussionCard(discussion));
            }
        } catch (Exception e) {
            System.err.println("Error loading discussions: " + e.getMessage());
            AlertUtils.showError("Error", "Failed to load discussions");
        }
    }

    private VBox createDiscussionCard(RecomDiscuss discussion) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // User info and timestamp
        Label userLabel = new Label(discussion.getUserName());
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a2980;");

        Label timeLabel = new Label(formatTimestamp(discussion.getTimestamp()));
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        // Add user info first
        card.getChildren().addAll(userLabel, timeLabel);

        // Add image if exists (sekarang di posisi atas)
        if (discussion.getImageData() != null) {
            try {
                Image image = new Image(new ByteArrayInputStream(discussion.getImageData()));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setPreserveRatio(true);
                card.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error loading discussion image: " + e.getMessage());
            }
        }

        // Discussion content (sekarang di posisi bawah)
        Label contentLabel = new Label(discussion.getDescription());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #2c3e50;");
        card.getChildren().add(contentLabel);

        return card;
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            AlertUtils.showInfo("Success", "Image selected successfully");
        }
    }

    private void submitDiscussion() {
        if (currentUser == null) {
            AlertUtils.showError("Error", "Please log in to post a discussion");
            return;
        }

        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            AlertUtils.showError("Error", "Please enter a comment");
            return;
        }

        try {
            byte[] imageData = null;
            if (selectedImageFile != null) {
                imageData = Files.readAllBytes(selectedImageFile.toPath());
            }

            RecomDiscuss discussion = new RecomDiscuss(
                    0, // ID will be set by database
                    currentUser.getId(),
                    currentUser.getName(),
                    currentLocationName,
                    description,
                    imageData,
                    LocalDateTime.now()
            );

            discussionDAO.addDiscussion(discussion);

            // Clear inputs
            descriptionArea.clear();
            selectedImageFile = null;

            // Refresh discussions
            loadDiscussions();

            AlertUtils.showInfo("Success", "Discussion posted successfully");
        } catch (Exception e) {
            System.err.println("Error submitting discussion: " + e.getMessage());
            AlertUtils.showError("Error", "Failed to post discussion");
        }
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }
}
