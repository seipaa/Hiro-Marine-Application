package controllers;

import dao.NewsDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.News;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;

public class NewsAdminController {
    private static final Logger LOGGER = Logger.getLogger(NewsAdminController.class.getName());

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
    @FXML private TableColumn<News, Void> actionColumn;
    @FXML private Pagination pagination;
    @FXML private Button saveButton;
    @FXML private HBox titleBar;

    private NewsDAO newsDAO;
    private ObservableList<News> allNews;
    private News currentEditingNews;
    private MainController mainController;
    private double xOffset;
    private double yOffset;

    // Event type untuk update berita
    public static final EventType<Event> NEWS_UPDATED = 
        new EventType<>(Event.ANY, "NEWS_UPDATED");

    @FXML
    public void initialize() {
        try {
            // Initialize newsDAO
            newsDAO = new NewsDAO();
            allNews = FXCollections.observableArrayList();

            // Make window draggable
            if (titleBar != null) {
                titleBar.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                
                titleBar.setOnMouseDragged(event -> {
                    Stage stage = (Stage) titleBar.getScene().getWindow();
                    if (stage != null) {
                        stage.setX(event.getScreenX() - xOffset);
                        stage.setY(event.getScreenY() - yOffset);
                    }
                });
            }

            // Setup table columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            adminIdColumn.setCellValueFactory(new PropertyValueFactory<>("adminId"));

            // Setup action column
            setupActionColumn();

            // Load initial data
            loadAdminIds();
            refreshNewsList();

            // Add search listener
            searchField.textProperty().addListener((observable, oldValue, newValue) -> 
                handleSearch()
            );

            // Setup save button action
            saveButton.setOnAction(event -> {
                if (currentEditingNews == null) {
                    handleAddNews();
                } else {
                    handleSaveEdit();
                }
            });

            // Set window style
            Platform.runLater(() -> {
                Stage stage = (Stage) titleBar.getScene().getWindow();
                if (stage != null) {
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setTitle("Hiro's Marine - SEA EDUCATION");
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing NewsAdminController", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize News Admin panel: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void fireEvent(Event event) {
        if (titleField != null && titleField.getScene() != null) {
            Event.fireEvent(titleField.getScene().getWindow(), event);
        }
        // Notify MainController
        if (mainController != null) {
            mainController.handleNewsUpdated();
        }
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<News, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Hapus");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    News news = getTableView().getItems().get(getIndex());
                    if (news != null) {
                        handleEdit(news);
                    }
                });

                deleteButton.setOnAction(event -> {
                    News news = getTableView().getItems().get(getIndex());
                    if (news != null) {
                        handleDelete(news);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    @FXML
    private void handleSearch() {
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

    @FXML
    private void handleReset() {
        searchField.clear();
        newsTableView.setItems(allNews);
        refreshNewsList();
    }

    private void loadAdminIds() {
        // TODO: Load actual admin IDs from database
        adminIdComboBox.getItems().addAll("1", "2", "3");
    }

    private void refreshNewsList() {
        try {
            allNews.clear();
            allNews.addAll(newsDAO.getAllNews());
            newsTableView.setItems(allNews);
            
            // Update pagination
            int itemsPerPage = 10;
            int pageCount = Math.max(1, (int) Math.ceil(allNews.size() * 1.0 / itemsPerPage));
            pagination.setPageCount(pageCount);
            pagination.setCurrentPageIndex(0);
            
            // Set up page factory
            pagination.setPageFactory(pageIndex -> {
                int fromIndex = pageIndex * itemsPerPage;
                int toIndex = Math.min(fromIndex + itemsPerPage, allNews.size());
                newsTableView.setItems(FXCollections.observableArrayList(
                    allNews.subList(fromIndex, toIndex)
                ));
                return newsTableView;
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing news list", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh news list: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleAddNews() {
        if (!validasiForm()) {
            return;
        }
        LOGGER.info("Validasi form berhasil, melanjutkan untuk menyimpan berita.");
        try {
            String judul = titleField.getText().trim();
            String deskripsi = descriptionField.getText().trim();
            String urlGambar = imageUrlField.getText().trim();
            int idAdmin = Integer.parseInt(adminIdComboBox.getValue());
            boolean isBeritaUtama = breakingNewsCheckBox.isSelected();

            News berita = new News(0, idAdmin, judul, deskripsi, urlGambar,
                    new Timestamp(System.currentTimeMillis()), isBeritaUtama);

            if (newsDAO.addNews(berita)) {
                bersihkanForm();
                refreshNewsList();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil ditambahkan");
                
                // Kirim event untuk refresh berita
                fireEvent(new Event(NEWS_UPDATED));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menambahkan berita");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "ID Admin tidak valid");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private boolean validasiForm() {
        StringBuilder errors = new StringBuilder();

        if (titleField.getText().trim().isEmpty()) {
            errors.append("- Judul berita harus diisi\n");
        }
        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("- Deskripsi berita harus diisi\n");
        }
        if (imageUrlField.getText().trim().isEmpty()) {
            errors.append("- URL gambar harus diisi\n");
        }
        if (adminIdComboBox.getValue() == null) {
            errors.append("- ID Admin harus dipilih\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validasi Error", errors.toString());
            return false;
        }
        return true;
    }

    private void bersihkanForm() {
        titleField.clear();
        descriptionField.clear();
        imageUrlField.clear();
        adminIdComboBox.setValue(null);
        breakingNewsCheckBox.setSelected(false);
        currentEditingNews = null;
        saveButton.setText("Tambah Berita");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) titleField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Copy file to project's image directory
                String projectDir = System.getProperty("user.dir");
                String imageDir = projectDir + "/src/main/resources/images/news/";
                File directory = new File(imageDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                
                String fileName = selectedFile.getName();
                String targetPath = imageDir + fileName;
                File targetFile = new File(targetPath);
                
                // Copy file
                Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Set the relative path to the image URL field
                String relativePath = "/images/news/" + fileName;
                imageUrlField.setText(relativePath);
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyalin file gambar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        // Clear all input fields
        titleField.clear();
        descriptionField.clear();
        imageUrlField.clear();
        adminIdComboBox.getSelectionModel().clearSelection();
        breakingNewsCheckBox.setSelected(false);
        
        // Reset current editing news
        currentEditingNews = null;
        
        // Update save button text
        saveButton.setText("Simpan");
    }

    private void handleEdit(News berita) {
        currentEditingNews = berita;
        titleField.setText(berita.getTitle());
        descriptionField.setText(berita.getDescription());
        imageUrlField.setText(berita.getImageUrl());
        adminIdComboBox.setValue(String.valueOf(berita.getAdminId()));
        breakingNewsCheckBox.setSelected(berita.isBreakingNews());
        saveButton.setText("Simpan Perubahan");
    }

    private void handleSaveEdit() {
        if (currentEditingNews == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada berita yang sedang diedit.");
            return;
        }
        if (!validasiForm()) {
            return;
        }
        LOGGER.info("Validasi form berhasil, melanjutkan untuk menyimpan berita.");
        try {
            currentEditingNews.setTitle(titleField.getText().trim());
            currentEditingNews.setDescription(descriptionField.getText().trim());
            currentEditingNews.setImageUrl(imageUrlField.getText().trim());
            currentEditingNews.setAdminId(Integer.parseInt(adminIdComboBox.getValue()));
            currentEditingNews.setBreakingNews(breakingNewsCheckBox.isSelected());

            if (newsDAO.updateNews(currentEditingNews)) {
                bersihkanForm();
                refreshNewsList();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil diperbarui");
                
                // Kirim event untuk refresh berita
                fireEvent(new Event(NEWS_UPDATED));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui berita");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validasiForm()) {
            return;
        }

        try {
            if (currentEditingNews == null) {
                // Create new news
                News news = new News();
                news.setTitle(titleField.getText().trim());
                news.setDescription(descriptionField.getText().trim());
                news.setImageUrl(imageUrlField.getText().trim());
                news.setAdminId(Integer.parseInt(adminIdComboBox.getValue()));
                news.setBreakingNews(breakingNewsCheckBox.isSelected());
                news.setCreatedAt(new Timestamp(System.currentTimeMillis()));

                if (newsDAO.addNews(news)) {  
                    refreshNewsList();
                    bersihkanForm();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil ditambahkan");
                    fireEvent(new Event(NEWS_UPDATED));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menambahkan berita");
                }
            } else {
                // Update existing news
                currentEditingNews.setTitle(titleField.getText().trim());
                currentEditingNews.setDescription(descriptionField.getText().trim());
                currentEditingNews.setImageUrl(imageUrlField.getText().trim());
                currentEditingNews.setAdminId(Integer.parseInt(adminIdComboBox.getValue()));
                currentEditingNews.setBreakingNews(breakingNewsCheckBox.isSelected());

                if (newsDAO.updateNews(currentEditingNews)) {  
                    refreshNewsList();
                    bersihkanForm();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil diperbarui");
                    fireEvent(new Event(NEWS_UPDATED));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui berita");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "ID Admin tidak valid");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saat menyimpan berita", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan berita: " + e.getMessage());
        }
    }

    private void handleDelete(News berita) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus Berita");
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus berita ini?");

        konfirmasi.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (newsDAO.deleteNews(berita.getId())) {
                    refreshNewsList();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berita berhasil dihapus");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus berita");
                }
            }
        });
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}