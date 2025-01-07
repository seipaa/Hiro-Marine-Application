package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import models.Recommendation;
import utils.AlertUtils;
import dao.RecommendationDAO;
import java.io.*;
import java.nio.file.Files;
import java.util.Optional;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RecommendationAdminController {
    @FXML private TextField locationNameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageUrlField;
    @FXML private ImageView imagePreview;
    @FXML private TableView<Recommendation> recommendationTable;
    @FXML private TableColumn<Recommendation, Integer> idColumn;
    @FXML private TableColumn<Recommendation, String> locationColumn;
    @FXML private TableColumn<Recommendation, String> descriptionColumn;
    @FXML private TableColumn<Recommendation, String> imageColumn;
    @FXML private TableColumn<Recommendation, Void> actionColumn;

    private                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 MainController mainController;
    private File selectedImageFile;
    private ObservableList<Recommendation> recommendations;
    private RecommendationDAO recommendationDAO;
    private byte[] selectedImageData;

    @FXML
    public void initialize() {
        System.out.println("Initializing RecommendationAdminController...");

        // Initialize DAO
        recommendationDAO = new RecommendationDAO();

        // Initialize the table columns
        idColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(
                        cellData.getValue().getId()).asObject());

        locationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLocationName()));

        descriptionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDescription()));

        imageColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getImageUrl()));

        // Set up the action column with edit and delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Recommendation recommendation = getTableView().getItems().get(getIndex());
                    loadRecommendationForEdit(recommendation);
                });

                deleteButton.setOnAction(event -> {
                    Recommendation recommendation = getTableView().getItems().get(getIndex());
                    deleteRecommendation(recommendation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        // Initialize the recommendations list
        recommendations = FXCollections.observableArrayList();
        recommendationTable.setItems(recommendations);

        // Load initial data
        refreshTable();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            try {
                // Read image file into byte array
                selectedImageData = Files.readAllBytes(selectedImageFile.toPath());

                // Create preview from byte array
                Image image = new Image(new ByteArrayInputStream(selectedImageData));
                imagePreview.setImage(image);

                System.out.println("Selected image: " + selectedImageFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                AlertUtils.showError("Error", "Failed to load image.");
                imagePreview.setImage(null);
                selectedImageFile = null;
                selectedImageData = null;
            }
        }
    }

    @FXML
    private void addRecommendation() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create new recommendation
            Recommendation recommendation = new Recommendation();
            recommendation.setLocationName(locationNameField.getText());
            recommendation.setDescription(descriptionField.getText());

            // Set image data
            if (selectedImageData != null) {
                recommendation.setImageData(selectedImageData);
            }

            // Save to database
            recommendationDAO.addRecommendation(recommendation);

            // Clear fields
            clearFields();

            // Refresh table and main view
            refreshTable();
            if (mainController != null) {
                // Force a complete refresh of the recommendations view
                mainController.refreshRecommendations();
            }

            AlertUtils.showInfo("Success", "Recommendation added successfully!");
        } catch (Exception e) {
            System.err.println("Error adding recommendation: " + e.getMessage());
            AlertUtils.showError("Error", "Failed to add recommendation: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        locationNameField.clear();
        descriptionField.clear();
        imagePreview.setImage(null);
        selectedImageFile = null;
        selectedImageData = null;
    }

    @FXML
    private void refreshTable() {
        try {
            // Fetch from database
            if (recommendations == null) {
                recommendations = FXCollections.observableArrayList();
            }
            recommendations.clear();
            recommendations.addAll(recommendationDAO.getAllRecommendations());
            recommendationTable.setItems(recommendations);
            recommendationTable.refresh();
        } catch (SQLException e) {
            System.err.println("Error refreshing table: " + e.getMessage());
            AlertUtils.showError("Error", "Failed to refresh recommendations.");
        }
    }

    private void loadRecommendationForEdit(Recommendation recommendation) {
        locationNameField.setText(recommendation.getLocationName());
        descriptionField.setText(recommendation.getDescription());

        // Load image preview if available
        byte[] imageData = recommendation.getImageData();
        if (imageData != null) {
            try {
                Image image = new Image(new ByteArrayInputStream(imageData));
                imagePreview.setImage(image);
                selectedImageData = imageData;
            } catch (Exception e) {
                System.err.println("Error loading image for edit: " + e.getMessage());
                imagePreview.setImage(null);
                selectedImageData = null;
            }
        }
    }

    private void deleteRecommendation(Recommendation recommendation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Recommendation");
        alert.setContentText("Are you sure you want to delete this recommendation?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                recommendationDAO.deleteRecommendation(recommendation);

                // Remove from table
                recommendations.remove(recommendation);

                // Refresh main view
                if (mainController != null) {
                    mainController.refreshRecommendations();
                }

                AlertUtils.showInfo("Success", "Recommendation deleted successfully!");
            } catch (Exception e) {
                System.err.println("Error deleting recommendation: " + e.getMessage());
                AlertUtils.showError("Error", "Failed to delete recommendation.");
            }
        }
    }

    private boolean validateInput() {
        if (locationNameField.getText().trim().isEmpty()) {
            AlertUtils.showError("Validation Error", "Location name is required.");
            return false;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            AlertUtils.showError("Validation Error", "Description is required.");
            return false;
        }
        return true;
    }
}