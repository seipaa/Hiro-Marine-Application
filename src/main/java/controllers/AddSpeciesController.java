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
        try {
            // Validasi input
            if (nameField.getText().isEmpty() || latinNameField.getText().isEmpty() || 
                typeField.getText().isEmpty() || descriptionField.getText().isEmpty() || 
                imageUrlField.getText().isEmpty()) {
                AlertUtils.showError("Error", "Semua field harus diisi!");
                return;
            }

            MarineSpecies newSpecies = new MarineSpecies(
                0, // ID akan di-generate oleh database
                imageUrlField.getText(),
                nameField.getText(),
                latinNameField.getText(),
                descriptionField.getText(),
                typeField.getText()
            );

            System.out.println("Attempting to add species: " + newSpecies.getName());
            marineSpeciesDAO.addPendingSpecies(newSpecies);
            
            AlertUtils.showInfo("Sukses", "Species berhasil ditambahkan dan menunggu persetujuan admin!");
            
            if (mainController != null) {
                mainController.refreshSpeciesView();
            }
            
            ((Stage) nameField.getScene().getWindow()).close();
            
        } catch (SQLException e) {
            System.err.println("Error saving species: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal menyimpan species: " + e.getMessage());
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