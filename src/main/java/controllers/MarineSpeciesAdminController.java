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
        setupListView();
        refreshList();
    }

    private void setupListView() {
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

        approvedSpeciesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSpecies = newVal;
                populateFields(newVal);
                loadSpeciesImage(newVal.getId());
            }
        });
    }

    private void refreshList() {
        try {
            ObservableList<MarineSpecies> speciesList = marineSpeciesDAO.getAllMarineSpecies();
            approvedSpeciesListView.setItems(speciesList);
        } catch (Exception e) {
            System.err.println("Error refreshing species list: " + e.getMessage());
            e.printStackTrace();
        }
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
    private void updateSpecies() {
        try {
            MarineSpecies selected = approvedSpeciesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError("Error", "Pilih species yang akan diupdate!");
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
            
            refreshList();
            clearFields();
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

        if (AlertUtils.showConfirmation("Konfirmasi Hapus", 
                "Apakah Anda yakin ingin menghapus species '" + selected.getName() + "'?")) {
            try {
                marineSpeciesDAO.deleteSpecies(selected.getId());
                refreshList();
                clearFields();
                AlertUtils.showInfo("Sukses", "Species berhasil dihapus!");
            } catch (SQLException e) {
                AlertUtils.showError("Error", "Gagal menghapus species: " + e.getMessage());
                e.printStackTrace();
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

    private void loadSpeciesImage(int speciesId) {
        try {
            byte[] imageData = marineSpeciesDAO.getSpeciesImage(speciesId);
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