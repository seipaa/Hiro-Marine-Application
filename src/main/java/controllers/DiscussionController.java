package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DiscussionController {
    @FXML
    private ImageView postingImageView;
    @FXML
    private Label postingTitleLabel;
    @FXML
    private TextArea postingDescriptionArea;
    @FXML
    private VBox discussionVBox;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button uploadImageBtn;
    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        // Setup event handlers
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        submitButton.setOnAction(e -> handleSubmitComment());
        uploadImageBtn.setOnAction(e -> handleImageUpload());
    }

    public void setLocationInfo(String locationName) {
        // Set informasi lokasi ke dalam tampilan
        postingTitleLabel.setText(locationName);

        // Contoh deskripsi default (bisa diganti dengan data dari database)
        String description = "Deskripsi untuk " + locationName + "\n" +
                "Silakan tambahkan komentar atau diskusi Anda tentang lokasi ini.";
        postingDescriptionArea.setText(description);

        // Load gambar sesuai lokasi (implementasi sesuai kebutuhan)
        loadLocationImage(locationName);
    }

    private void loadLocationImage(String locationName) {
        // Implementasi loading gambar sesuai lokasi
        // Contoh sederhana:
        String imagePath = "/images/" + locationName.toLowerCase().replace(" ", "_") + ".jpg";
        try {
            // Load dan set gambar ke postingImageView
            // Image image = new Image(getClass().getResourceAsStream(imagePath));
            // postingImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image for " + locationName + ": " + e.getMessage());
        }
    }

    private void handleSubmitComment() {
        String title = titleField.getText();
        String description = descriptionArea.getText();

        if (!title.isEmpty() && !description.isEmpty()) {
            // Tambahkan komentar ke discussionVBox
            addCommentToDiscussion(title, description);

            // Clear input fields
            titleField.clear();
            descriptionArea.clear();
        }
    }

    private void handleImageUpload() {
        // Implementasi upload gambar
        System.out.println("Image upload clicked");
    }

    private void addCommentToDiscussion(String title, String description) {
        // Buat komponen untuk komentar baru
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);

        commentBox.getChildren().addAll(titleLabel, descLabel);

        // Tambahkan ke discussion area
        discussionVBox.getChildren().add(0, commentBox);
    }

    public void someMethod(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
        formatTimestamp(dateTime);
    }

    private void formatTimestamp(LocalDateTime dateTime) {
    }
}
