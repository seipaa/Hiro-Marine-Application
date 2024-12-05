package controllers;

import dao.NewsDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.News;
import utils.AlertUtils;

public class NewsAdminController {
    @FXML
    private TextField newsTitleField;
    @FXML
    private TextField newsDescriptionField;

    private NewsDAO newsDAO = new NewsDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void addNews() {
        String title = newsTitleField.getText();
        String description = newsDescriptionField.getText();
        if (title.isEmpty() || description.isEmpty()) {
            AlertUtils.showError("Error", "Title and description cannot be empty.");
            return;
        }
        News news = new News(0, 1, title, description, null); // Assuming adminId is 1
        newsDAO.addNews(news);
        AlertUtils.showInfo("Success", "News added successfully.");
        clearFields();
    }

    @FXML
    private void updateNews() {
        // Implement update logic
    }

    @FXML
    private void deleteNews() {
        // Implement delete logic
    }

    private void clearFields() {
        newsTitleField.clear();
        newsDescriptionField.clear();
    }
} 