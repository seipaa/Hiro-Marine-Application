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
import javafx.collections.ObservableList;
import java.io.ByteArrayInputStream;

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
    @FXML
    private ImageView previewImage;

    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();
    private MainController mainController;
    private MarineSpecies selectedSpecies;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        refreshLists();
        setupListViews();

        // Add listener for selection changes
        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateSpeciesPreview(newValue);
            }
        });
    }

    private void updateSpeciesPreview(MarineSpecies newValue) {
        if (newValue != null) {
            try {
                byte[] imageData = marineSpeciesDAO.getSpeciesImage(newValue.getId());
                if (imageData != null && imageData.length > 0) {
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    previewImage.setImage(image);
                } else {
                    previewImage.setImage(null);
                }
            } catch (SQLException e) {
                System.err.println("Error loading preview image: " + e.getMessage());
                previewImage.setImage(null);
            }
        }
    }

    private void refreshLists() {
        ObservableList<MarineSpecies> pendingSpecies = marineSpeciesDAO.getAllPendingSpecies();
        ObservableList<MarineSpecies> approvedSpecies = marineSpeciesDAO.getAllApprovedSpecies();
        
        pendingSpeciesListView.setItems(pendingSpecies);
        approvedSpeciesListView.setItems(approvedSpecies);
        
        // Clear selection dan fields
        pendingSpeciesListView.getSelectionModel().clearSelection();
        approvedSpeciesListView.getSelectionModel().clearSelection();
        selectedSpecies = null;
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
                selectedSpecies = newVal;
                approvedSpeciesListView.getSelectionModel().clearSelection();
                populateFields(newVal);
            }
        });
        
        // Add selection listener untuk approved species
        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSpecies = newVal;
                pendingSpeciesListView.getSelectionModel().clearSelection();
                populateFields(newVal);
            }
        });
    }

    private void populateFields(MarineSpecies species) {
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
        refreshLists();
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
            refreshLists();
            
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
                refreshLists();
                
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
            refreshLists();
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
                refreshLists();
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
        try {
            // Validasi input
            String name = speciesNameField.getText();
            String latinName = speciesLatinNameField.getText();
            String description = speciesDescriptionField.getText();
            String type = speciesTypeField.getText();
            String imageUrl = speciesImageUrlField.getText();

            // Validasi field
            if (name.isEmpty() || latinName.isEmpty() || description.isEmpty() || type.isEmpty()) {
                AlertUtils.showError("Error", "Semua field harus diisi!");
                return;
            }

            // Validasi tipe species
            if (!type.equalsIgnoreCase("Flora") && !type.equalsIgnoreCase("Fauna")) {
                AlertUtils.showError("Error", "Tipe species harus 'Flora' atau 'Fauna'!");
                return;
            }

            // Baca data gambar jika ada
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
                0,          // id
                imageUrl,   // image_url
                name,       // name
                latinName,  // latin_name
                description,// description
                type       // type
            );

            // Simpan ke database
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
            // Ambil species yang dipilih
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

            // Baca data gambar baru jika ada
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
            
            AlertUtils.showInfo("Sukses", "Species berhasil diupdate!");
            clearFields();
            refreshLists();
            
            if (mainController != null) {
                mainController.refreshSpeciesView();
            }
            
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal mengupdate species: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSpecies() {
        try {
            // Ambil species yang dipilih
            MarineSpecies selected = approvedSpeciesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError("Error", "Pilih species yang akan dihapus dari daftar yang disetujui!");
                return;
            }

            if (AlertUtils.showConfirmation("Konfirmasi", "Apakah Anda yakin ingin menghapus species ini?")) {
                marineSpeciesDAO.deleteSpecies(selected.getId());
                AlertUtils.showInfo("Sukses", "Species berhasil dihapus!");
                clearFields();
                refreshLists();
                
                if (mainController != null) {
                    mainController.refreshSpeciesView();
                }
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal menghapus species: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tambahkan validasi untuk memastikan tipe yang dimasukkan benar
    private void validateSpeciesType(String type) {
        if (!type.equalsIgnoreCase("Flora") && !type.equalsIgnoreCase("Fauna")) {
            throw new IllegalArgumentException("Tipe species harus 'Flora' atau 'Fauna'!");
        }
    }
}