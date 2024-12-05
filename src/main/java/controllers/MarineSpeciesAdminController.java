package controllers;

import dao.MarineSpeciesDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.MarineSpecies;
import utils.AlertUtils;

public class MarineSpeciesAdminController {
    @FXML
    private TextField speciesNameField;
    @FXML
    private TextField speciesDescriptionField;
    @FXML
    private TextField speciesImageUrlField;

    private MarineSpeciesDAO marineSpeciesDAO = new MarineSpeciesDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void addMarineSpecies() {
        String name = speciesNameField.getText();
        String description = speciesDescriptionField.getText();
        String imageUrl = speciesImageUrlField.getText();

        if (name.isEmpty() || description.isEmpty()) {
            AlertUtils.showError("Error", "Name and description cannot be empty.");
            return;
        }

        MarineSpecies species = new MarineSpecies(0, name, description, imageUrl);
        marineSpeciesDAO.addMarineSpecies(species);
        AlertUtils.showInfo("Success", "Marine Species added successfully.");
        clearFields();
    }

    @FXML
    private void updateMarineSpecies() {
        // Implement update logic
    }

    @FXML
    private void deleteMarineSpecies() {
        // Implement delete logic
    }

    private void clearFields() {
        speciesNameField.clear();
        speciesDescriptionField.clear();
        speciesImageUrlField.clear();
    }
} 