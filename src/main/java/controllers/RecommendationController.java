package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Recommendation;
import java.io.IOException;
import javafx.event.ActionEvent;

public class RecommendationController {
    @FXML
    private HBox scrollPaneContent;
    @FXML
    private VBox scrollPane2Content;
    
    // Tambahkan semua button yang ada di FXML
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
    public void initialize() {
        System.out.println("Initializing RecommendationController...");
        setupDiscussionButtons();
    }

    @FXML
    private void setupDiscussionButtons() {
        try {
            // Setup untuk semua button diskusi dengan pengecekan null
            setupButton(discussionButton, "Pantai Pangandaran");
            setupButton(discussionButton1, "Pantai Anyer");
            setupButton(discussionButton2, "Pantai Carita");
            setupButton(discussionButton11, "Pantai Anyer");
            setupButton(discussionButton21, "Pantai Carita");
            setupButton(discussionButton12, "Pantai Anyer");
            setupButton(discussionButton22, "Pantai Carita");
            setupButton(discussionButton111, "Pantai Pangandaran");
        } catch (Exception e) {
            System.err.println("Error setting up discussion buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupButton(Button button, String locationName) {
        if (button != null) {
            button.setOnAction(event -> openDiscussion(locationName));
            System.out.println("Setup button for: " + locationName);
        } else {
            System.err.println("Warning: Button for " + locationName + " is null");
        }
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

    public void addCardToScrollPane(AnchorPane card) {
        if (scrollPaneContent != null) {
            scrollPaneContent.getChildren().add(card);
        }
    }

    public void addCardToScrollPane2(AnchorPane card) {
        if (scrollPane2Content != null) {
            scrollPane2Content.getChildren().add(card);
        }
    }

    @FXML
    private void handleDiscussionButton(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String locationName = "";
        
        // Tentukan lokasi berdasarkan button yang diklik
        if (sourceButton == discussionButton || sourceButton == discussionButton111) {
            locationName = "Pantai Pangandaran";
        } else if (sourceButton == discussionButton1 || sourceButton == discussionButton11 || sourceButton == discussionButton12) {
            locationName = "Pantai Anyer";
        } else if (sourceButton == discussionButton2 || sourceButton == discussionButton21 || sourceButton == discussionButton22) {
            locationName = "Pantai Carita";
        }
        
        openDiscussion(locationName);
    }
}
