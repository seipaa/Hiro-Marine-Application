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

import java.io.ByteArrayInputStream;
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

    // Tambahkan field untuk MainController
    private MainController mainController;

    @FXML
    public void initialize() {
        setupListViews();
        refreshLists(); // Load kedua daftar species saat inisialisasi
    }

    private void setupListViews() {
        // Setup cell factories untuk kedua ListView
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

        // Add selection listeners untuk kedua ListView
        pendingSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSpecies = newVal;
                populateFields(newVal);
                loadSpeciesImage(newVal.getId());
                // Clear selection di approved list
                approvedSpeciesListView.getSelectionModel().clearSelection();
            }
        });

        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSpecies = newVal;
                populateFields(newVal);
                loadSpeciesImage(newVal.getId());
                // Clear selection di pending list
                pendingSpeciesListView.getSelectionModel().clearSelection();
            }
        });
    }

    private void loadSpeciesImage(int speciesId) {
        try {
            byte[] imageData = marineSpeciesDAO.loadSpeciesImage(speciesId);
            if (imageData != null && imageData.length > 0) {
                Image image = new Image(new ByteArrayInputStream(imageData));
                previewImage.setImage(image);
            } else {
                // Load default image jika tidak ada gambar
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_species.jpg"));
                previewImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading species image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadApprovedSpecies() {
        try {
            ObservableList<MarineSpecies> approvedSpecies = marineSpeciesDAO.getAllApprovedSpecies();
            approvedSpeciesListView.setItems(approvedSpecies);
        } catch (Exception e) {
            System.err.println("Error loading approved species: " + e.getMessage());
            e.printStackTrace();
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
                // Baca file gambar sebagai byte array
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                
                // Simpan path gambar untuk preview
                String fileName = selectedFile.getName();
                Path destination = Paths.get("src/main/resources/images/temp/" + fileName);
                Files.createDirectories(destination.getParent());
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                
                speciesImageUrlField.setText("/images/temp/" + fileName);
                
                // Update preview image
                Image image = new Image(destination.toUri().toString());
                previewImage.setImage(image);
                
            } catch (IOException e) {
                AlertUtils.showError("Error", "Gagal memproses file gambar: " + e.getMessage());
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
        try {
            // Refresh kedua daftar species
            ObservableList<MarineSpecies> pendingSpecies = marineSpeciesDAO.getAllPendingSpecies();
            ObservableList<MarineSpecies> approvedSpecies = marineSpeciesDAO.getAllApprovedSpecies();
            
            // Update kedua ListView
            pendingSpeciesListView.setItems(pendingSpecies);
            approvedSpeciesListView.setItems(approvedSpecies);
            
            // Clear selection dan fields
            pendingSpeciesListView.getSelectionModel().clearSelection();
            approvedSpeciesListView.getSelectionModel().clearSelection();
            clearFields();
            
            // Clear preview image
            previewImage.setImage(null);
        } catch (Exception e) {
            System.err.println("Error refreshing lists: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal memperbarui daftar species");
        }
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
        try {
            // Validasi input
            String name = speciesNameField.getText();
            String latinName = speciesLatinNameField.getText();
            String description = speciesDescriptionField.getText();
            String type = speciesTypeField.getText();
            String imageUrl = speciesImageUrlField.getText();

            if (name.isEmpty() || latinName.isEmpty() || description.isEmpty() || type.isEmpty()) {
                AlertUtils.showError("Error", "Semua field harus diisi!");
                return;
            }

            // Baca data gambar
            byte[] imageData = null;
            if (!imageUrl.isEmpty()) {
                try {
                    Path imagePath = Paths.get("src/main/resources" + imageUrl);
                    imageData = Files.readAllBytes(imagePath);
                } catch (IOException e) {
                    System.err.println("Error reading image file: " + e.getMessage());
                }
            }

            // Buat objek MarineSpecies
            MarineSpecies newSpecies = new MarineSpecies(
                0, imageUrl, name, latinName, description, type
            );

            // Simpan species dengan gambar (langsung approved)
            marineSpeciesDAO.addSpecies(newSpecies, imageData);
            
            AlertUtils.showInfo("Sukses", "Species berhasil ditambahkan!");
            clearFields();
            refreshLists();
            
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal menambahkan species: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateSpecies() {
        try {
            MarineSpecies selected = approvedSpeciesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError("Error", "Pilih species yang akan diupdate dari daftar yang disetujui!");
                return;
            }

            // Validasi input
            String name = speciesNameField.getText();
            String latinName = speciesLatinNameField.getText();
            String description = speciesDescriptionField.getText();
            String type = speciesTypeField.getText();
            String imageUrl = speciesImageUrlField.getText();

            if (name.isEmpty() || latinName.isEmpty() || description.isEmpty() || type.isEmpty()) {
                AlertUtils.showError("Error", "Semua field harus diisi!");
                return;
            }

            // Baca data gambar baru jika ada perubahan
            byte[] imageData = null;
            if (!imageUrl.isEmpty() && !imageUrl.equals(selected.getImageUrl())) {
                try {
                    Path imagePath = Paths.get("src/main/resources" + imageUrl);
                    imageData = Files.readAllBytes(imagePath);
                } catch (IOException e) {
                    System.err.println("Error reading image file: " + e.getMessage());
                }
            }

            // Update objek MarineSpecies
            selected.setName(name);
            selected.setLatinName(latinName);
            selected.setDescription(description);
            selected.setType(type);
            selected.setImageUrl(imageUrl);

            // Update di database
            marineSpeciesDAO.updateSpecies(selected, imageData);
            
            // Refresh lists dan tampilan
            refreshLists();
            clearFields();
            previewImage.setImage(null);
            
            AlertUtils.showInfo("Sukses", "Species berhasil diupdate!");
            
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal mengupdate species: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSpecies() {
        MarineSpecies selected = approvedSpeciesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Error", "Pilih species yang akan dihapus!");
            return;
        }

        // Konfirmasi penghapusan
        if (AlertUtils.showConfirmation("Konfirmasi Hapus", 
                "Apakah Anda yakin ingin menghapus species '" + selected.getName() + "'?")) {
            try {
                // Hapus dari database
                marineSpeciesDAO.deleteSpecies(selected.getId());
                
                // Refresh tampilan
                refreshLists();
                clearFields();
                
                // Refresh tampilan utama jika ada
                if (mainController != null) {
                    mainController.refreshSpeciesView();
                }
                
                AlertUtils.showInfo("Sukses", "Species berhasil dihapus!");
            } catch (SQLException e) {
                System.err.println("Error saat menghapus species: " + e.getMessage());
                e.printStackTrace();
                AlertUtils.showError("Error", "Gagal menghapus species: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        speciesNameField.clear();
        speciesLatinNameField.clear();
        speciesDescriptionField.clear();
        speciesImageUrlField.clear();
        speciesTypeField.clear();
        previewImage.setImage(null);
    }

    // Tambahkan setter untuk MainController
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
} 