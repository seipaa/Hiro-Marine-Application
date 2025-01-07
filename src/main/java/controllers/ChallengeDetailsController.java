package controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.zxing.WriterException;

import dao.CommentDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Challenge;
import models.Comment;
import models.User;
import utils.QRCodeGenerator;

public class ChallengeDetailsController {

    @FXML private ImageView challengeImage;
    @FXML private Label challengeTitle;
    @FXML private Text challengeDescription;
    @FXML private Label challengePoints;
    @FXML private ImageView qrCodeImage;
    @FXML private Button closeButton;
    @FXML private ScrollPane commentScrollPane;
    @FXML private VBox commentContainer;
    @FXML private TextArea commentArea;
    @FXML private Button submitCommentButton;

    private CommentDAO commentDAO = new CommentDAO();
    private Challenge currentChallenge;
    private User currentUser;
    private MainController mainController;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadChallengeDetails(Challenge challenge) {
        this.currentChallenge = challenge;
        challengeTitle.setText(challenge.getTitle());
        challengeDescription.setText(challenge.getDescription());
        challengePoints.setText("Poin yang dapat diraih: " + challenge.getPoints());

        // Load challenge image
        try {
            String imagePath = "/images/" + challenge.getImageUrl();
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                image = new Image(getClass().getResourceAsStream("/images/default_challenge.jpg"));
            }
            challengeImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading challenge image: " + e.getMessage());
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_challenge.jpg"));
                challengeImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Error loading default image: " + ex.getMessage());
            }
        }

        // Generate QR code
        try {
            File qrDirectory = new File("qrcodes");
            if (!qrDirectory.exists()) {
                qrDirectory.mkdirs();
            }
            
            String qrCodePath = "qrcodes/challenge_" + challenge.getId() + ".png";
            File qrFile = new File(qrCodePath);
            
            if (!qrFile.exists()) {
                QRCodeGenerator.generateQRCodeImage(
                    challenge.getQrCodeUrl() != null ? challenge.getQrCodeUrl() : "https://hiromarine.com/challenge/" + challenge.getId(),
                    150, 
                    150, 
                    qrCodePath
                );
            }
            
            if (qrFile.exists()) {
                Image qrImage = new Image(qrFile.toURI().toString());
                qrCodeImage.setImage(qrImage);
            }
        } catch (WriterException | IOException e) {
            System.err.println("Error generating/loading QR code: " + e.getMessage());
            e.printStackTrace();
        }

        loadComments();
    }

    private void loadComments() {
        try {
            List<Comment> comments = commentDAO.getCommentsByChallengeId(currentChallenge.getId());
            
            // Pastikan kita menjalankan UI updates di JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    // Clear container sebelum menambahkan komentar baru
                    commentContainer.getChildren().clear();
                    
                    if (comments.isEmpty()) {
                        // Pesan default jika tidak ada komentar
                        VBox defaultBox = new VBox();
                        defaultBox.setStyle(
                            "-fx-padding: 15; " +
                            "-fx-background-color: #e3f2fd; " +
                            "-fx-border-color: #90caf9; " +
                            "-fx-border-width: 1; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-radius: 8;"
                        );

                        Label messageLabel = new Label(
                            "Kamu adalah orang pertama yang sangat excited untuk challenge ini! 🎉\n" +
                            "Yuk berikan komentar dan ajak teman-temanmu untuk ikut berkontribusi dalam menjaga kelestarian laut! 🌊"
                        );
                        messageLabel.setWrapText(true);
                        messageLabel.setStyle(
                            "-fx-text-fill: #1976d2; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-style: italic;"
                        );
                        
                        defaultBox.getChildren().add(messageLabel);
                        commentContainer.getChildren().add(defaultBox);
                    } else {
                        // Tambahkan setiap komentar ke container
                        for (Comment comment : comments) {
                            VBox commentBox = createCommentBox(comment);
                            commentContainer.getChildren().add(commentBox);
                        }
                    }

                    // Scroll ke komentar terbaru setelah semua komentar ditambahkan
                    commentScrollPane.setVvalue(1.0);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", "Terjadi kesalahan saat menampilkan komentar");
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Gagal memuat komentar: " + e.getMessage());
        }
    }

    private VBox createCommentBox(Comment comment) {
        // Container untuk satu komentar
        VBox commentBox = new VBox(5);
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
        );

        // Header komentar (nama user dan waktu)
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Label untuk nama user
        Label nameLabel = new Label(comment.getUserName());
        nameLabel.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #1976d2; " +
            "-fx-font-size: 14px;"
        );

        // Label untuk waktu
        Label timeLabel = new Label(formatDateTime(comment.getCreatedAt()));
        timeLabel.setStyle(
            "-fx-text-fill: #757575; " +
            "-fx-font-size: 12px;"
        );

        header.getChildren().addAll(nameLabel, timeLabel);

        // Isi komentar
        Label commentLabel = new Label(comment.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle(
            "-fx-text-fill: #333333; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 5 0 0 0;"
        );

        // Tambahkan semua komponen ke container
        commentBox.getChildren().addAll(header, commentLabel);
        
        return commentBox;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) return "Baru saja";
        if (minutes < 60) return minutes + " menit yang lalu";
        if (minutes < 1440) return (minutes / 60) + " jam yang lalu";
        if (minutes < 10080) return (minutes / 1440) + " hari yang lalu";
        
        return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }

    @FXML
    private void submitComment() {
        if (currentUser == null) {
            showError("Error", "Silakan login terlebih dahulu untuk memberikan komentar");
            return;
        }

        String commentText = commentArea.getText().trim();
        if (commentText.isEmpty()) {
            showError("Error", "Komentar tidak boleh kosong");
            return;
        }

        try {
            // Tambahkan komentar ke database
            commentDAO.addComment(currentChallenge.getId(), currentUser.getId(), commentText);
            
            // Clear input field
            commentArea.clear();
            
            // Refresh tampilan komentar
            loadComments();
            
            // Scroll ke bawah untuk melihat komentar baru
            Platform.runLater(() -> commentScrollPane.setVvalue(1.0));
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Gagal menambahkan komentar: " + e.getMessage());
        }
    }

    @FXML
    private void closeChallengeDetails() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
