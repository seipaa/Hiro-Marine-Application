package controllers;

import dao.MarineSpeciesDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.MarineSpecies;
import utils.AlertUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AdminMarineSpeciesController {
    @FXML private TextField speciesNameField;
    @FXML private TextField speciesLatinNameField;
    @FXML private TextField speciesTypeField;
    @FXML private TextArea speciesDescriptionField;
    @FXML private TextField speciesImageUrlField;
    @FXML private ListView<MarineSpecies> pendingSpeciesListView;
    @FXML private ListView<MarineSpecies> approvedSpeciesListView;
    
    // Preview components
    @FXML private ImageView previewImage;
    @FXML private Label previewName;
    @FXML private Label previewLatinName;
    @FXML private Label previewType;
    @FXML private TextArea previewDescription;
    
    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();
    private MarineSpecies selectedSpecies;

    @FXML
    public void initialize() {
        // Setup cell factories for both lists
        pendingSpeciesListView.setCellFactory(lv -> new ListCell<MarineSpecies>() {
            @Override
            protected void updateItem(MarineSpecies species, boolean empty) {
                super.updateItem(species, empty);
                if (empty || species == null) {
                    setText(null);
                } else {
                    setText(species.getName() + " (" + species.getLatinName() + ")");
                }
            }
        });

        approvedSpeciesListView.setCellFactory(lv -> new ListCell<MarineSpecies>() {
            @Override
            protected void updateItem(MarineSpecies species, boolean empty) {
                super.updateItem(species, empty);
                if (empty || species == null) {
                    setText(null);
                } else {
                    setText(species.getName() + " (" + species.getLatinName() + ")");
                }
            }
        });

        // Add selection listeners for both lists
        pendingSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Clear selection in approved list when pending is selected
                approvedSpeciesListView.getSelectionModel().clearSelection();
                selectedSpecies = newVal;
                updatePreview(newVal);
                populateFields(newVal);
                System.out.println("Selected pending species: " + newVal.getName());
            }
        });

        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Clear selection in pending list when approved is selected
                pendingSpeciesListView.getSelectionModel().clearSelection();
                selectedSpecies = newVal;
                updatePreview(newVal);
                populateFields(newVal);
                System.out.println("Selected approved species: " + newVal.getName());
            }
        });

        // Add click listeners to clear other list's selection
        pendingSpeciesListView.setOnMouseClicked(event -> {
            if (pendingSpeciesListView.getSelectionModel().getSelectedItem() != null) {
                approvedSpeciesListView.getSelectionModel().clearSelection();
            }
        });

        approvedSpeciesListView.setOnMouseClicked(event -> {
            if (approvedSpeciesListView.getSelectionModel().getSelectedItem() != null) {
                pendingSpeciesListView.getSelectionModel().clearSelection();
            }
        });

        refreshLists();
    }

    private void updatePreview(MarineSpecies species) {
        if (species != null) {
            try {
                String imageUrl = species.getImageUrl();
                // Handle both formats: with and without "/images/" prefix
                String imagePath = imageUrl.startsWith("/images/") ? 
                    imageUrl : "/images/" + imageUrl;
                
                System.out.println("Loading preview image from: " + imagePath);
                
                // Use absolute path for debugging
                String absolutePath = getClass().getResource(imagePath).toExternalForm();
                System.out.println("Absolute image path: " + absolutePath);
                
                Image image = new Image(absolutePath);
                
                if (image.isError()) {
                    throw new Exception("Failed to load image: " + image.getException().getMessage());
                }
                
                previewImage.setImage(image);
                previewImage.setFitWidth(250);
                previewImage.setFitHeight(250);
                previewImage.setPreserveRatio(true);
                
                System.out.println("Successfully loaded image for: " + species.getName());
                
            } catch (Exception e) {
                System.err.println("Error loading preview image: " + e.getMessage());
                e.printStackTrace();
                try {
                    String defaultImagePath = "/images/default_species.jpg";
                    System.out.println("Attempting to load default image: " + defaultImagePath);
                    Image defaultImage = new Image(getClass().getResource(defaultImagePath).toExternalForm());
                    previewImage.setImage(defaultImage);
                } catch (Exception ex) {
                    System.err.println("Error loading default image: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Update text fields
            previewName.setText(species.getName());
            previewLatinName.setText(species.getLatinName());
            previewType.setText("Jenis: " + species.getType());
            previewDescription.setText(species.getDescription());
        } else {
            // Clear preview when no species is selected
            previewImage.setImage(null);
            previewName.setText("");
            previewLatinName.setText("");
            previewType.setText("");
            previewDescription.setText("");
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Species");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) speciesNameField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                Path destination = Paths.get("src/main/resources/images/" + fileName);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                speciesImageUrlField.setText("/images/" + fileName);
            } catch (IOException e) {
                AlertUtils.showError("Error", "Gagal menyalin file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void approveSpecies() {
        MarineSpecies selected = pendingSpeciesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                marineSpeciesDAO.approveSpecies(selected.getId());
                refreshLists();
                AlertUtils.showInfo("Sukses", "Species berhasil disetujui!");
            } catch (Exception e) {
                System.err.println("Error approving species: " + e.getMessage());
                e.printStackTrace();
                AlertUtils.showError("Error", "Gagal menyetujui species: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rejectSpecies() {
        MarineSpecies selected = pendingSpeciesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                marineSpeciesDAO.rejectSpecies(selected.getId());
                refreshLists();
                AlertUtils.showInfo("Sukses", "Species berhasil ditolak!");
            } catch (Exception e) {
                System.err.println("Error rejecting species: " + e.getMessage());
                e.printStackTrace();
                AlertUtils.showError("Error", "Gagal menolak species: " + e.getMessage());
            }
        }
    }

    private void refreshLists() {
        ObservableList<MarineSpecies> pendingSpecies = marineSpeciesDAO.getAllPendingSpecies();
        ObservableList<MarineSpecies> approvedSpecies = marineSpeciesDAO.getAllApprovedSpecies();
        
        pendingSpeciesListView.setItems(pendingSpecies);
        approvedSpeciesListView.setItems(approvedSpecies);
    }

    private void populateFields(MarineSpecies species) {
        speciesNameField.setText(species.getName());
        speciesLatinNameField.setText(species.getLatinName());
        speciesTypeField.setText(species.getType());
        speciesDescriptionField.setText(species.getDescription());
        speciesImageUrlField.setText(species.getImageUrl());
    }

    @FXML
    private void addSpecies() {
        // Implementation for adding new species
    }

    @FXML
    private void updateSpecies() {
        // Implementation for updating existing species
    }

    @FXML
    private void deleteSpecies() {
        // Implementation for deleting species
    }
} 