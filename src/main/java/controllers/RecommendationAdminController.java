package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Recommendation;
import dao.RecommendationDAO;
import utils.AlertUtils;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;

public class RecommendationAdminController {
    @FXML private TextField locationNameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageUrlField;
    @FXML private TextField ratingField;
    @FXML private TableView<Recommendation> recommendationTable;
    @FXML private TableColumn<Recommendation, Integer> idColumn;
    @FXML private TableColumn<Recommendation, String> locationColumn;
    @FXML private TableColumn<Recommendation, String> descriptionColumn;
    @FXML private TableColumn<Recommendation, Double> ratingColumn;
    @FXML private TableColumn<Recommendation, Void> actionColumn;

    private RecommendationDAO recommendationDAO = new RecommendationDAO();
    private MainController mainController;
    private Recommendation selectedRecommendation;

    @FXML
    public void initialize() {
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> {
                    Recommendation recommendation = getTableView().getItems().get(getIndex());
                    loadForEdit(recommendation);
                });

                deleteBtn.setOnAction(event -> {
                    Recommendation recommendation = getTableView().getItems().get(getIndex());
                    deleteRecommendation(recommendation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadForEdit(Recommendation recommendation) {
    }

    @FXML
    private void addRecommendation() {
        try {
            validateInputs();

            Recommendation recommendation = new Recommendation(
                    0,
                    locationNameField.getText(),
                    descriptionField.getText(),
                    imageUrlField.getText(),
                    Double.parseDouble(ratingField.getText())
            );

            recommendationDAO.addRecommendation(recommendation);
            AlertUtils.showInfo("Sukses", "Rekomendasi berhasil ditambahkan");
            clearFields();
            refreshTable();

            if (mainController != null) {
                mainController.refreshRecommendations();
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", e.getMessage());
        }
    }

    private void validateInputs() throws Exception {
        if (locationNameField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() ||
                imageUrlField.getText().isEmpty() ||
                ratingField.getText().isEmpty()) {
            throw new Exception("Semua field harus diisi");
        }

        try {
            double rating = Double.parseDouble(ratingField.getText());
            if (rating < 0 || rating > 5) {
                throw new Exception("Rating harus antara 0-5");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Rating harus berupa angka");
        }
    }

    @FXML
    private void clearFields() {
        selectedRecommendation = null;
        locationNameField.clear();
        descriptionField.clear();
        imageUrlField.clear();
        ratingField.clear();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void refreshTable() {
        try {
            recommendationTable.setItems(FXCollections.observableArrayList(
                    recommendationDAO.getAllRecommendations()
            ));
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    private void deleteRecommendation(Recommendation recommendation) {
        try {
            recommendationDAO.deleteRecommendation(recommendation);
            AlertUtils.showInfo("Sukses", "Rekomendasi berhasil dihapus");
            refreshTable();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Gagal menghapus rekomendasi: " + e.getMessage());
        }
    }
}