package controllers;

import dao.MarineSpeciesDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import models.MarineSpecies;
import utils.AlertUtils;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.util.Callback;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.SQLException;

public class MarineSpeciesAdminController {
    @FXML
    private TextField speciesNameField;
    @FXML
    private TextField speciesLatinNameField;
    @FXML
    private TextArea speciesDescriptionField;
    @FXML
    private TextField speciesImageUrlField;
    @FXML
    private TextField speciesTypeField;
    @FXML
    private ListView<MarineSpecies> pendingSpeciesListView;
    @FXML
    private ListView<MarineSpecies> approvedSpeciesListView;

    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        refreshSpeciesLists();
        setupListViews();

        // Add listener for selection changes
        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateSpeciesPreview(newValue);
            }
        });
    }

    private void updateSpeciesPreview(MarineSpecies newValue) {
    }

    public void refreshSpeciesLists() {
        pendingSpeciesListView.setItems(marineSpeciesDAO.getAllPendingSpecies());
        approvedSpeciesListView.setItems(marineSpeciesDAO.getAllApprovedSpecies());
    }

    private void setupListViews() {
        // Set cell factory untuk kedua ListView
        Callback<ListView<MarineSpecies>, ListCell<MarineSpecies>> cellFactory = lv -> new ListCell<MarineSpecies>() {
            @Override
            protected void updateItem(MarineSpecies species, boolean empty) {
                super.updateItem(species, empty);
                if (empty || species == null) {
                    setText(null);
                } else {
                    setText(species.getName() + " (" + species.getLatinName() + ")");
                }
            }
        };
        
        pendingSpeciesListView.setCellFactory(cellFactory);
        approvedSpeciesListView.setCellFactory(cellFactory);
        
        // Add selection listener untuk pending species
        pendingSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFormFields(newVal);
                approvedSpeciesListView.getSelectionModel().clearSelection();
            }
        });
        
        // Add selection listener untuk approved species
        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFormFields(newVal);
                pendingSpeciesListView.getSelectionModel().clearSelection();
            }
        });
    }

    private void fillFormFields(MarineSpecies species) {
        if (species != null) {
            speciesNameField.setText(species.getName());
            speciesLatinNameField.setText(species.getLatinName());
            speciesTypeField.setText(species.getType());
            speciesDescriptionField.setText(species.getDescription());
            speciesImageUrlField.setText(species.getImageUrl());
        }
    }

    @FXML
    private void addMarineSpecies() {
        String name = speciesNameField.getText();
        String latinName = speciesLatinNameField.getText();
        String description = speciesDescriptionField.getText();
        String imageUrl = speciesImageUrlField.getText();
        String type = speciesTypeField.getText();

        if (name.isEmpty() || latinName.isEmpty() || description.isEmpty() || type.isEmpty()) {
            AlertUtils.showError("Error", "Semua field harus diisi!");
            return;
        }

        MarineSpecies species = new MarineSpecies(0, name, latinName, description, imageUrl, type);
        marineSpeciesDAO.addMarineSpecies(species);
        AlertUtils.showInfo("Sukses", "Species berhasil ditambahkan!");
        clearFields();
        refreshSpeciesLists();
    }

    @FXML
    private void updateMarineSpecies() {
        // Cek dari kedua ListView
        MarineSpecies selectedSpecies = pendingSpeciesListView.getSelectionModel().getSelectedItem();
        if (selectedSpecies == null) {
            selectedSpecies = approvedSpeciesListView.getSelectionModel().getSelectedItem();
        }
        
        if (selectedSpecies == null) {
            AlertUtils.showError("Error", "Pilih species yang akan diupdate!");
            return;
        }

        String name = speciesNameField.getText();
        String latinName = speciesLatinNameField.getText();
        String description = speciesDescriptionField.getText();
        String imageUrl = speciesImageUrlField.getText();
        String type = speciesTypeField.getText();

        if (name.isEmpty() || latinName.isEmpty() || description.isEmpty() || type.isEmpty()) {
            AlertUtils.showError("Error", "Semua field harus diisi!");
            return;
        }

        try {
            MarineSpecies updatedSpecies = new MarineSpecies(
                selectedSpecies.getId(), name, latinName, description, imageUrl, type
            );
            marineSpeciesDAO.updateMarineSpecies(updatedSpecies);
            AlertUtils.showInfo("Sukses", "Species berhasil diupdate!");
            clearFields();
            refreshSpeciesLists();
            
            if (mainController != null) {
                mainController.refreshSpeciesView();
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal mengupdate species: " + e.getMessage());
        }
    }

    @FXML
    private void deleteMarineSpecies() {
        // Cek dari kedua ListView
        MarineSpecies selectedSpecies = pendingSpeciesListView.getSelectionModel().getSelectedItem();
        if (selectedSpecies == null) {
            selectedSpecies = approvedSpeciesListView.getSelectionModel().getSelectedItem();
        }

        if (selectedSpecies == null) {
            AlertUtils.showError("Error", "Pilih species yang akan dihapus!");
            return;
        }

        if (AlertUtils.showConfirmation("Konfirmasi", "Apakah Anda yakin ingin menghapus species ini?")) {
            try {
                marineSpeciesDAO.deleteMarineSpecies(selectedSpecies.getId());
                AlertUtils.showInfo("Sukses", "Species berhasil dihapus!");
                clearFields();
                refreshSpeciesLists();
                
                if (mainController != null) {
                    mainController.refreshSpeciesView();
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Gagal menghapus species: " + e.getMessage());
            }
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Copy file to resources/images directory
                String fileName = selectedFile.getName();
                Path destinationPath = Paths.get("src/main/resources/images/" + fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Set the image URL with correct format
                speciesImageUrlField.setText("/images/" + fileName);
            } catch (IOException e) {
                AlertUtils.showError("Error", "Failed to copy image file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void approveSpecies() {
        MarineSpecies selectedSpecies = pendingSpeciesListView.getSelectionModel().getSelectedItem();
        if (selectedSpecies == null) {
            AlertUtils.showError("Error", "Pilih species yang akan disetujui dari daftar pending!");
            return;
        }

        try {
            marineSpeciesDAO.approveSpecies(selectedSpecies.getId());
            refreshSpeciesLists();
            AlertUtils.showInfo("Sukses", "Species berhasil disetujui!");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal menyetujui species: " + e.getMessage());
        }
    }

    @FXML
    private void rejectSpecies() {
        MarineSpecies selectedSpecies = pendingSpeciesListView.getSelectionModel().getSelectedItem();
        if (selectedSpecies == null) {
            AlertUtils.showError("Error", "Pilih species yang akan ditolak dari daftar pending!");
            return;
        }

        if (AlertUtils.showConfirmation("Konfirmasi", "Apakah Anda yakin ingin menolak species ini?")) {
            try {
                marineSpeciesDAO.rejectSpecies(selectedSpecies.getId());
                refreshSpeciesLists();
                AlertUtils.showInfo("Sukses", "Species berhasil ditolak!");
            } catch (Exception e) {
                AlertUtils.showError("Error", "Gagal menolak species: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        speciesNameField.clear();
        speciesLatinNameField.clear();
        speciesDescriptionField.clear();
        speciesImageUrlField.clear();
        speciesTypeField.clear();
    }

    public void setSpeciesForEdit(MarineSpecies species) {
        speciesNameField.setText(species.getName());
        speciesLatinNameField.setText(species.getLatinName());
        speciesTypeField.setText(species.getType());
        speciesDescriptionField.setText(species.getDescription());
        speciesImageUrlField.setText(species.getImageUrl());
    }

    @FXML
    private void addSpecies() {
        // Ambil data dari form dengan urutan yang benar
        String imageUrl = speciesImageUrlField.getText();
        String name = speciesNameField.getText();
        String latinName = speciesLatinNameField.getText();
        String description = speciesDescriptionField.getText();
        String type = speciesTypeField.getText();         // Flora/Fauna

        // Validasi input
        if (imageUrl.isEmpty() || name.isEmpty() || latinName.isEmpty() || 
            description.isEmpty() || type.isEmpty()) {
            AlertUtils.showError("Error", "Semua field harus diisi!");
            return;
        }

        // Validasi tipe species
        if (!type.equalsIgnoreCase("Flora") && !type.equalsIgnoreCase("Fauna")) {
            AlertUtils.showError("Error", "Tipe species harus 'Flora' atau 'Fauna'!");
            return;
        }

        // Format URL gambar
        if (!imageUrl.startsWith("/images/")) {
            imageUrl = "/images/" + imageUrl;
        }

        // Buat objek MarineSpecies dengan urutan yang benar
        MarineSpecies newSpecies = new MarineSpecies(
            0,          // id
            imageUrl,   // image_url
            name,       // name
            latinName,  // latin_name
            description,// description
            type        // type (Flora/Fauna)
        );

        try {
            marineSpeciesDAO.addSpecies(newSpecies);
            refreshSpeciesLists();
            clearFields();
            AlertUtils.showInfo("Sukses", "Species berhasil ditambahkan!");
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Gagal menambahkan species: " + e.getMessage());
        }
    }

    @FXML
    private void updateSpecies() {
        MarineSpecies selectedSpecies = approvedSpeciesListView.getSelectionModel().getSelectedItem();
        if (selectedSpecies == null) {
            AlertUtils.showError("Error", "Select a species to update.");
            return;
        }

        String name = speciesNameField.getText();
        String latinName = speciesLatinNameField.getText();
        String type = speciesTypeField.getText();
        String description = speciesDescriptionField.getText();
        String imageUrl = speciesImageUrlField.getText();

        // Ensure consistent image URL format
        if (!imageUrl.startsWith("/images/")) {
            imageUrl = "/images/" + imageUrl;
        }

        // Update the selected species with new values
        selectedSpecies.setName(name);
        selectedSpecies.setLatinName(latinName);
        selectedSpecies.setType(type);
        selectedSpecies.setDescription(description);
        selectedSpecies.setImageUrl(imageUrl);

        try {
            marineSpeciesDAO.updateSpecies(selectedSpecies);
            refreshSpeciesLists();
            clearFields();
            AlertUtils.showInfo("Success", "Species updated successfully.");
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Failed to update species: " + e.getMessage());
        }
    }

    @FXML
    private void deleteSpecies() {
        MarineSpecies selectedSpecies = approvedSpeciesListView.getSelectionModel().getSelectedItem();
        if (selectedSpecies == null) {
            AlertUtils.showError("Error", "Select a species to delete.");
            return;
        }

        try {
            marineSpeciesDAO.deleteSpecies(selectedSpecies.getId());
            refreshSpeciesLists();
            AlertUtils.showInfo("Success", "Species deleted successfully.");
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Failed to delete species: " + e.getMessage());
        }
    }

    // Tambahkan validasi untuk memastikan tipe yang dimasukkan benar
    private void validateSpeciesType(String type) {
        if (!type.equalsIgnoreCase("Flora") && !type.equalsIgnoreCase("Fauna")) {
            throw new IllegalArgumentException("Tipe species harus 'Flora' atau 'Fauna'!");
        }
    }
}