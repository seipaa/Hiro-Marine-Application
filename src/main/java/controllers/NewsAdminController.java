package controllers;

import dao.NewsDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import models.News;
import java.io.File;
import java.util.List;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.sql.Timestamp;

public class NewsAdminController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageUrlField;
    @FXML private ComboBox<String> adminIdComboBox;
    @FXML private CheckBox breakingNewsCheckBox;
    @FXML private TextField searchField;
    @FXML private TableView<News> newsTableView;
    @FXML private TableColumn<News, Integer> idColumn;
    @FXML private TableColumn<News, String> titleColumn;
    @FXML private TableColumn<News, String> descriptionColumn;
    @FXML private TableColumn<News, Integer> adminIdColumn;
    @FXML private TableColumn<News, Timestamp> createdAtColumn;
    @FXML private TableColumn<News, Boolean> breakingNewsColumn;
    @FXML private TableColumn<News, Void> actionColumn;
    @FXML private Pagination pagination;
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    private NewsDAO newsDAO;
    private static final int ITEMS_PER_PAGE = 10;
    private ObservableList<News> allNews = FXCollections.observableArrayList();
    private News currentEditingNews = null;

    @FXML
    public void initialize() {
        newsDAO = new NewsDAO();
        setupTable();
        setupPagination();
        loadAdminIds();
        refreshNewsList();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });

        saveButton.setOnAction(event -> {
            if (currentEditingNews == null) {
                handleAddNews();
            } else {
                handleSaveEdit();
            }
        });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        adminIdColumn.setCellValueFactory(new PropertyValueFactory<>("adminId"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        breakingNewsColumn.setCellValueFactory(new PropertyValueFactory<>("breakingNews"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Hapus");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    News news = getTableView().getItems().get(getIndex());
                    handleEdit(news);
                });

                deleteButton.setOnAction(event -> {
                    News news = getTableView().getItems().get(getIndex());
                    handleDelete(news);
                });

                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-secondary");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupPagination() {
        pagination.setPageCount((int) Math.ceil(allNews.size() * 1.0 / ITEMS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<News> createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allNews.size());
        newsTableView.setItems(FXCollections.observableArrayList(
                allNews.subList(fromIndex, toIndex)
        ));
        return newsTableView;
    }

    private void loadAdminIds() {
        // Implement loading admin IDs into adminIdComboBox
        adminIdComboBox.setItems(FXCollections.observableArrayList("1", "2", "3"));
    }

    private void refreshNewsList() {
        allNews.clear();
        allNews.addAll(newsDAO.getAllNews());
        setupPagination();
        createPage(pagination.getCurrentPageIndex());
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            newsTableView.setItems(allNews);
        } else {
            ObservableList<News> filteredNews = allNews.filtered(news ->
                news.getTitle().toLowerCase().contains(keyword) ||
                news.getDescription().toLowerCase().contains(keyword)
            );
            newsTableView.setItems(filteredNews);
        }
    }

    private void handleAddNews() {
        try {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String imageUrl = imageUrlField.getText().trim();
            int adminId = Integer.parseInt(adminIdComboBox.getValue());
            boolean isBreakingNews = breakingNewsCheckBox.isSelected();

            News news = new News(0, adminId, title, description, imageUrl,
                    new Timestamp(System.currentTimeMillis()), isBreakingNews);

            if (newsDAO.addNews(news)) {
                clearForm();
                refreshNewsList();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil ditambahkan");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menambahkan berita");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void handleEdit(News news) {
        currentEditingNews = news;
        titleField.setText(news.getTitle());
        descriptionField.setText(news.getDescription());
        imageUrlField.setText(news.getImageUrl());
        adminIdComboBox.setValue(String.valueOf(news.getAdminId()));
        breakingNewsCheckBox.setSelected(news.isBreakingNews());
        saveButton.setText("Update");
    }

    private void handleSaveEdit() {
        try {
            currentEditingNews.setTitle(titleField.getText().trim());
            currentEditingNews.setDescription(descriptionField.getText().trim());
            currentEditingNews.setImageUrl(imageUrlField.getText().trim());
            currentEditingNews.setAdminId(Integer.parseInt(adminIdComboBox.getValue()));
            currentEditingNews.setBreakingNews(breakingNewsCheckBox.isSelected());

            if (newsDAO.updateNews(currentEditingNews)) {
                clearForm();
                refreshNewsList();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil diupdate");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal mengupdate berita");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void handleDelete(News news) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Hapus");
        confirmation.setHeaderText("Hapus Berita");
        confirmation.setContentText("Apakah Anda yakin ingin menghapus berita ini?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            if (newsDAO.deleteNews(news.getId())) {
                refreshNewsList();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil dihapus");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus berita");
            }
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        imageUrlField.clear();
        adminIdComboBox.getSelectionModel().clearSelection();
        breakingNewsCheckBox.setSelected(false);
        currentEditingNews = null;
        saveButton.setText("Simpan");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setMainController(MainController mainController) {
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        newsTableView.setItems(allNews);
        refreshNewsList();
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) titleField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            imageUrlField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}