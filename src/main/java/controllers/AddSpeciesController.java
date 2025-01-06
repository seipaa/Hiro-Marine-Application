package controllers;

import dao.MarineSpeciesDAO;
import dao.ImageDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.MarineSpecies;
import utils.AlertUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileInputStream;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import utils.DatabaseConnection;

public class AddSpeciesController {
    @FXML private TextField nameField;
    @FXML private TextField latinNameField;
    @FXML private TextField typeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageUrlField;

    private MainController mainController;
    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();
    private ImageDAO imageDAO = new ImageDAO();
    private File selectedImageFile;

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
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            imageUrlField.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void saveSpecies() {
        try {
            if (!validateInput()) {
                return;
            }

            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                MarineSpecies newSpecies = new MarineSpecies(
                    0,
                    "pending_image",
                    nameField.getText(),
                    latinNameField.getText(),
                    descriptionField.getText(),
                    typeField.getText()
                );

                int speciesId = marineSpeciesDAO.addPendingSpecies(newSpecies);

                if (selectedImageFile != null) {
                    try (FileInputStream fis = new FileInputStream(selectedImageFile)) {
                        imageDAO.saveImage(
                            speciesId,
                            selectedImageFile.getName(),
                            fis
                        );
                    }
                }

                conn.commit();
                AlertUtils.showInfo("Sukses", "Species berhasil ditambahkan dan menunggu persetujuan admin!");
                
                if (mainController != null) {
                    mainController.refreshSpeciesView();
                }
                
                clearFields();
                ((Stage) nameField.getScene().getWindow()).close();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal menyimpan data: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("Initializing AddSpeciesController...");
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty() || 
            latinNameField.getText().isEmpty() || 
            typeField.getText().isEmpty() || 
            descriptionField.getText().isEmpty() || 
            selectedImageFile == null) {
            AlertUtils.showError("Error", "Semua field harus diisi dan gambar harus dipilih!");
            return false;
        }
        return true;
    }
} 