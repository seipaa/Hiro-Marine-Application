package controllers;

import dao.MarineSpeciesDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import models.MarineSpecies;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import utils.AlertUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.ByteArrayInputStream;

public class MarineSpeciesController {
    @FXML private ImageView speciesImage;
    @FXML private Label speciesName;
    @FXML private Label speciesLatinName;
    @FXML private Label speciesType;
    @FXML private TextArea speciesDescription;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    @FXML private FontAwesomeIconView backIcon;

    private MainController mainController;
    private MarineSpecies currentSpecies;
    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setSpeciesData(MarineSpecies species) {
        this.currentSpecies = species;
        try {
            // Ambil data gambar dari database
            byte[] imageData = marineSpeciesDAO.getSpeciesImage(species.getId());
            if (imageData != null && imageData.length > 0) {
                Image image = new Image(new ByteArrayInputStream(imageData));
                speciesImage.setImage(image);
            } else {
                // Jika tidak ada gambar, tampilkan placeholder
                speciesImage.setStyle("-fx-background-color: #cccccc;");
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            speciesImage.setStyle("-fx-background-color: #cccccc;");
        }
        
        speciesName.setText(species.getName());
        speciesLatinName.setText(species.getLatinName());
        speciesType.setText(species.getType());
        speciesDescription.setText(species.getDescription());
    }

    @FXML
    public void initialize() {
        // Pastikan icon sudah diinisialisasi dengan benar
        if (backIcon != null) {
            backIcon.setSize("24");
            backIcon.setStyleClass("back-button-icon");
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_marinespecies.fxml"));
            Parent root = loader.load();
            
            MarineSpeciesAdminController controller = loader.getController();
            controller.setMainController(mainController);
            controller.setSpeciesForEdit(currentSpecies);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Species");
            stage.setScene(new Scene(root));
            
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            
            stage.show();
            
            ((Stage) editButton.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal membuka form edit: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (AlertUtils.showConfirmation("Konfirmasi", "Apakah Anda yakin ingin menghapus species ini?")) {
            try {
                marineSpeciesDAO.deleteMarineSpecies(currentSpecies.getId());
                AlertUtils.showInfo("Sukses", "Species berhasil dihapus!");
                
                if (mainController != null) {
                    mainController.refreshSpeciesView();
                }
                
                ((Stage) deleteButton.getScene().getWindow()).close();
            } catch (Exception e) {
                AlertUtils.showError("Error", "Gagal menghapus species: " + e.getMessage());
            }
        }
    }
}