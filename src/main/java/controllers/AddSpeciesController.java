package controllers;

import dao.MarineSpeciesDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

public class AddSpeciesController {
    @FXML private TextField nameField;
    @FXML private TextField latinNameField;
    @FXML private TextField typeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageUrlField;

    private MainController mainController;
    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Species");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) nameField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                Path destination = Paths.get("src/main/resources/images/" + fileName);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                imageUrlField.setText("/images/" + fileName);
            } catch (IOException e) {
                AlertUtils.showError("Error", "Gagal menyalin file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveSpecies() {
        String imageUrl = imageUrlField.getText();
        String name = nameField.getText();
        String latinName = latinNameField.getText();
        String description = descriptionField.getText();
        String type = typeField.getText();         // Flora/Fauna

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

        // Buat objek MarineSpecies dengan urutan sesuai struktur tabel
        MarineSpecies newSpecies = new MarineSpecies(
            0,          // id
            imageUrl,   // image_url
            name,       // name
            latinName,  // latin_name
            description,// description
            type        // type (Flora/Fauna)
        );

        try {
            marineSpeciesDAO.addPendingSpecies(newSpecies);
            if (mainController != null) {
                mainController.refreshSpeciesView();
            }
            AlertUtils.showInfo("Sukses", "Species berhasil ditambahkan dan menunggu persetujuan admin!");
            clearFields();
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Gagal menambahkan species: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        latinNameField.clear();
        typeField.clear();
        descriptionField.clear();
        imageUrlField.clear();
    }

    @FXML
    private void initialize() {
        // Add any initialization code here
        System.out.println("Initializing AddSpeciesController...");
    }
} 