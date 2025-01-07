package controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import dao.ChallengeDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import models.Challenge;
import utils.AlertUtils;
import utils.DatabaseConnection;

public class ChallengeAdminController {
    // Form fields
    @FXML private TextField challengeNameField;
    @FXML private TextArea challengeDescriptionField;
    @FXML private TextField pointsField;
    @FXML private TextField imageUrlField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    // Search and filter
    @FXML private TextField userSearchField;
    @FXML private TextField challengeSearchField;
    @FXML private ComboBox<String> challengeFilterCombo;
    
    // Tables
    @FXML private TableView<Challenge> challengeTable;
    @FXML private TableColumn<Challenge, Integer> challengeIdColumn;
    @FXML private TableColumn<Challenge, String> challengeTitleColumn;
    @FXML private TableColumn<Challenge, Integer> challengePointValueColumn;
    @FXML private TableColumn<Challenge, String> startDateColumn;
    @FXML private TableColumn<Challenge, String> endDateColumn;
    @FXML private TableColumn<Challenge, Void> challengeActionsColumn;
    
    @FXML private TableView<VerificationItem> verificationTable;
    @FXML private TableColumn<VerificationItem, String> userColumn;
    @FXML private TableColumn<VerificationItem, Integer> userPointsColumn;
    @FXML private TableColumn<VerificationItem, String> challengeColumn;
    @FXML private TableColumn<VerificationItem, Integer> challengePointsColumn;
    @FXML private TableColumn<VerificationItem, Void> verifyColumn;

    private ChallengeDAO challengeDAO = new ChallengeDAO();
    private UserDAO userDAO = new UserDAO();
    private MainController mainController;
    private Challenge selectedChallenge;

    @FXML
    public void initialize() {
        setupTables();
        setupFilters();
        loadData();

        // Set default tab ke Verifikasi Challenge
        if (tabPane != null) {
            tabPane.getSelectionModel().select(0);
        }
    }

    private void setupTables() {
        // Setup Challenge Table
        challengeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        challengeTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        challengePointValueColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        setupChallengeActionsColumn();

        // Setup Verification Table
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userPointsColumn.setCellValueFactory(new PropertyValueFactory<>("userPoints"));
        challengeColumn.setCellValueFactory(new PropertyValueFactory<>("challengeTitle"));
        challengePointsColumn.setCellValueFactory(new PropertyValueFactory<>("challengePoints"));
        setupVerifyColumn();
    }

    private void setupChallengeActionsColumn() {
        challengeActionsColumn.setCellFactory(column -> {
            return new TableCell<Challenge, Void>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Hapus");
                private final HBox pane = new HBox(5, editBtn, deleteBtn);

                {
                    editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    
                    editBtn.setOnAction(event -> {
                        Challenge challenge = getTableView().getItems().get(getIndex());
                        editChallenge(challenge);
                    });
                    
                    deleteBtn.setOnAction(event -> {
                        Challenge challenge = getTableView().getItems().get(getIndex());
                        deleteChallenge(challenge);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        });
    }

    private void setupVerifyColumn() {
        verifyColumn.setCellFactory(column -> {
            return new TableCell<VerificationItem, Void>() {
                private final Button verifyBtn = new Button("Verifikasi");

                {
                    verifyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    verifyBtn.setOnAction(event -> {
                        VerificationItem item = getTableView().getItems().get(getIndex());
                        verifyChallenge(item);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : verifyBtn);
                }
            };
        });
    }

    private void setupFilters() {
        challengeFilterCombo.setItems(FXCollections.observableArrayList("Semua", "Active", "Completed"));
        challengeFilterCombo.setValue("Active");
        
        challengeFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                loadVerificationItems();
            } catch (SQLException e) {
                AlertUtils.showError("Error", "Gagal memuat data: " + e.getMessage());
            }
        });
        
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterVerificationTable();
        });
        
        challengeSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterChallengeTable(challengeFilterCombo.getValue());
        });
    }

    private void filterChallengeTable(String filter) {
        String searchText = challengeSearchField.getText().toLowerCase().trim();
        
        try {
            // Muat ulang data sesuai filter
            if ("Completed".equals(filter)) {
                loadCompletedChallenges();
            } else if ("Active".equals(filter)) {
                loadActiveChallenges();
            } else {
                loadAllChallenges();
            }
            
            // Buat filtered list baru
            FilteredList<Challenge> filteredData = new FilteredList<>(challengeTable.getItems());
            
            // Set predicate untuk filtering
            filteredData.setPredicate(challenge -> {
                if (searchText == null || searchText.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = searchText.toLowerCase();
                
                return challenge.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                       challenge.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
            
            // Set hasil filter ke table
            challengeTable.setItems(filteredData);
            
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Gagal memfilter challenge: " + e.getMessage());
        }
    }

    private void loadCompletedChallenges() throws SQLException {
        String query = "SELECT DISTINCT c.* FROM challenges c " +
                      "INNER JOIN user_challenges uc ON c.id = uc.challenge_id " +
                      "WHERE uc.status = 'COMPLETED' " +
                      "ORDER BY c.title";
                      
        loadChallengesWithQuery(query);
    }

    private void loadActiveChallenges() throws SQLException {
        String query = "SELECT c.* FROM challenges c " +
                      "WHERE NOT EXISTS (" +
                      "    SELECT 1 FROM user_challenges uc " +
                      "    WHERE uc.challenge_id = c.id AND uc.status = 'COMPLETED'" +
                      ") ORDER BY c.title";
                      
        loadChallengesWithQuery(query);
    }

    private void loadAllChallenges() throws SQLException {
        String query = "SELECT * FROM challenges ORDER BY title";
        loadChallengesWithQuery(query);
    }

    private void loadChallengesWithQuery(String query) throws SQLException {
        ObservableList<Challenge> challenges = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Challenge challenge = new Challenge(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("points"),
                    rs.getString("image_url"),
                    rs.getString("qr_code_url"),
                    rs.getString("start_date"),
                    rs.getString("end_date")
                );
                challenges.add(challenge);
            }
        }
        
        challengeTable.setItems(challenges);
    }

    private void loadData() {
        try {
            loadChallenges();
            loadVerificationItems();
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    private void loadChallenges() throws SQLException {
        List<Challenge> challenges = challengeDAO.getAllChallenges();
        challengeTable.setItems(FXCollections.observableArrayList(challenges));
    }

    private void loadVerificationItems() throws SQLException {
        String query;
        if ("Completed".equals(challengeFilterCombo.getValue())) {
            // Query untuk challenge yang sudah selesai
            query = "SELECT u.id as user_id, u.name as user_name, u.total_points, " +
                   "c.id as challenge_id, c.title as challenge_title, c.points as challenge_points, " +
                   "uc.completed_at " +
                   "FROM users u " +
                   "INNER JOIN user_challenges uc ON u.id = uc.user_id " +
                   "INNER JOIN challenges c ON uc.challenge_id = c.id " +
                   "WHERE uc.status = 'COMPLETED' " +
                   "ORDER BY uc.completed_at DESC";
        } else {
            // Query untuk challenge yang belum selesai
            query = "SELECT u.id as user_id, u.name as user_name, u.total_points, " +
                   "c.id as challenge_id, c.title as challenge_title, c.points as challenge_points " +
                   "FROM users u " +
                   "CROSS JOIN challenges c " +
                   "WHERE NOT EXISTS ( " +
                   "    SELECT 1 FROM user_challenges uc " +
                   "    WHERE uc.user_id = u.id " +
                   "    AND uc.challenge_id = c.id " +
                   "    AND uc.status = 'COMPLETED'" +
                   ") " +
                   "ORDER BY u.name ASC, c.title ASC";
        }

        ObservableList<VerificationItem> items = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                VerificationItem item = new VerificationItem(
                    rs.getInt("user_id"),
                    rs.getString("user_name"),
                    rs.getInt("total_points"),
                    rs.getInt("challenge_id"),
                    rs.getString("challenge_title"),
                    rs.getInt("challenge_points")
                );
                items.add(item);
            }
        }

        verificationTable.setItems(items);
        
        // Update placeholder text berdasarkan filter
        String placeholderText = "Completed".equals(challengeFilterCombo.getValue()) ?
            "Tidak ada challenge yang telah diselesaikan" :
            "Tidak ada challenge yang perlu diverifikasi";
            
        if (items.isEmpty()) {
            Label noDataLabel = new Label(placeholderText);
            noDataLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");
            verificationTable.setPlaceholder(noDataLabel);
        }
    }

    private void filterVerificationTable() {
        String searchText = userSearchField.getText().toLowerCase().trim();
        
        try {
            // Muat ulang data original
            ObservableList<VerificationItem> originalItems = FXCollections.observableArrayList();
            loadVerificationItems(); // Muat data asli
            
            // Buat filtered list baru
            FilteredList<VerificationItem> filteredData = new FilteredList<>(verificationTable.getItems());
            
            // Set predicate untuk filtering
            filteredData.setPredicate(item -> {
                if (searchText == null || searchText.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = searchText.toLowerCase();
                
                return item.getUserName().toLowerCase().contains(lowerCaseFilter) ||
                       item.getChallengeTitle().toLowerCase().contains(lowerCaseFilter);
            });
            
            // Set hasil filter ke table
            verificationTable.setItems(filteredData);
            
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Gagal memfilter data: " + e.getMessage());
        }
    }

    @FXML
    private void showAddChallengeForm() {
        // Reset form untuk input challenge baru
        selectedChallenge = null;
        resetForm();
        
        // Pindah ke tab Kelola Challenge jika belum aktif
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1); // index 1 adalah tab Kelola Challenge
        }
    }

    @FXML
    private TabPane tabPane;

    @FXML
    private void resetForm() {
        selectedChallenge = null;
        challengeNameField.clear();
        challengeDescriptionField.clear();
        pointsField.clear();
        imageUrlField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        
        // Set focus ke field pertama
        challengeNameField.requestFocus();
    }

    @FXML
    private void saveChallenge() {
        try {
            String title = challengeNameField.getText();
            String description = challengeDescriptionField.getText();
            String points = pointsField.getText();
            String imageUrl = imageUrlField.getText();
            // Generate QR Code URL otomatis
            String qrCodeUrl = "challenges/" + title.toLowerCase().replace(" ", "-") + ".qr";
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Validasi input
            if (title.isEmpty() || description.isEmpty() || points.isEmpty() || startDate == null) {
                AlertUtils.showError("Error", "Judul, deskripsi, points, dan tanggal mulai harus diisi!");
                return;
            }

            // Validasi tanggal
            if (endDate != null && startDate.isAfter(endDate)) {
                AlertUtils.showError("Error", "Tanggal mulai tidak boleh setelah tanggal selesai!");
                return;
            }
            
            String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDateStr = endDate != null ? endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
            
            // Jika ini challenge baru
            if (selectedChallenge == null) {
                Challenge challenge = new Challenge(
                    0, // id akan di-generate oleh database
                    title,
                    description,
                    Integer.parseInt(points),
                    imageUrl,
                    qrCodeUrl,  // Tambahkan QR Code URL
                    startDateStr,
                    endDateStr
                );
                challengeDAO.addChallenge(challenge);
                AlertUtils.showInfo("Success", "Challenge berhasil ditambahkan!");
                resetForm();
                loadChallengeData();
                
                // Refresh challenge list di main window jika ada
                if (mainController != null) {
                    mainController.refreshChallenges();
                }
            } else {
                // Update challenge yang sudah ada
                String query = "UPDATE challenges SET title=?, description=?, points=?, image_url=?, qr_code_url=?, start_date=?, end_date=? WHERE id=?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setInt(3, Integer.parseInt(points));
                    stmt.setString(4, imageUrl);
                    stmt.setString(5, qrCodeUrl);
                    stmt.setString(6, startDateStr);
                    stmt.setString(7, endDateStr);
                    stmt.setInt(8, selectedChallenge.getId());
                    
                    stmt.executeUpdate();
                    AlertUtils.showInfo("Success", "Challenge berhasil diupdate!");
                    resetForm();
                    loadChallengeData();
                    
                    // Refresh challenge list di main window jika ada
                    if (mainController != null) {
                        mainController.refreshChallenges();
                    }
                }
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Error", "Points harus berupa angka!");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal menyimpan challenge: " + e.getMessage());
        }
    }

    private void loadChallengeData() {
        try {
            // Clear existing items
            challengeTable.getItems().clear();
            
            // Load challenges from database
            String query = "SELECT * FROM challenges ORDER BY start_date DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Challenge challenge = new Challenge(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("points"),
                        rs.getString("image_url"),
                        rs.getString("qr_code_url"),
                        rs.getString("start_date"),
                        rs.getString("end_date")
                    );
                    challengeTable.getItems().add(challenge);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Gagal memuat data challenge: " + e.getMessage());
        }
    }

    @FXML
    private void browseImage() {
        String filePath = showFileChooser("Select Image");
        if (filePath != null) {
            imageUrlField.setText(filePath);
        }
    }

    @FXML
    private void refreshVerificationTable() {
        try {
            loadVerificationItems();
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Gagal memuat data verifikasi: " + e.getMessage());
        }
    }

    private String showFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(challengeNameField.getScene().getWindow());
        return file != null ? file.getAbsolutePath() : null;
    }

    private void editChallenge(Challenge challenge) {
        selectedChallenge = challenge;
        challengeNameField.setText(challenge.getTitle());
        challengeDescriptionField.setText(challenge.getDescription());
        pointsField.setText(String.valueOf(challenge.getPoints()));
        imageUrlField.setText(challenge.getImageUrl());
    }

    private void deleteChallenge(Challenge challenge) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi");
        alert.setHeaderText("Hapus Challenge");
        alert.setContentText("Apakah Anda yakin ingin menghapus challenge ini?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                challengeDAO.deleteChallenge(challenge.getId());
                loadAllChallenges();
                AlertUtils.showInfo("Success", "Challenge berhasil dihapus!");
                
                // Refresh tampilan challenge di main window
                if (mainController != null) {
                    mainController.refreshChallenges();
                }
            } catch (Exception e) {
                AlertUtils.showError("Error", "Gagal menghapus challenge: " + e.getMessage());
            }
        }
    }

    private void verifyChallenge(VerificationItem item) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Update points user
            String updatePointsQuery = "UPDATE users SET total_points = total_points + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updatePointsQuery)) {
                stmt.setInt(1, item.getChallengePoints());
                stmt.setInt(2, item.getUserId());
                int updated = stmt.executeUpdate();
                if (updated == 0) throw new SQLException("Gagal menambahkan poin");
            }

            // 2. Insert ke user_challenges
            String insertQuery = "INSERT INTO user_challenges (user_id, challenge_id, status, completed_at) VALUES (?, ?, 'COMPLETED', CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, item.getUserId());
                stmt.setInt(2, item.getChallengeId());
                int inserted = stmt.executeUpdate();
                if (inserted == 0) throw new SQLException("Gagal mencatat challenge");
            }

            conn.commit();
            
            // Refresh data setelah commit berhasil
            Platform.runLater(() -> {
                try {
                    loadVerificationItems();
                    filterChallengeTable(challengeFilterCombo.getValue());
                    
                    // Update leaderboard di main window
                    if (mainController != null) {
                        mainController.updateLeaderboard();
                        mainController.refreshChallenges();
                    }
                    
                    AlertUtils.showInfo("Success", String.format(
                        "Challenge berhasil diverifikasi!\nUser: %s\nPoin bertambah: +%d",
                        item.getUserName(),
                        item.getChallengePoints()
                    ));
                } catch (SQLException ex) {
                    AlertUtils.showError("Error", "Gagal memperbarui tampilan: " + ex.getMessage());
                }
            });
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            AlertUtils.showError("Error", "Gagal memverifikasi challenge: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Helper class untuk item verifikasi
    public static class VerificationItem {
        private final int userId;
        private final String userName;
        private final int userPoints;
        private final int challengeId;
        private final String challengeTitle;
        private final int challengePoints;

        public VerificationItem(int userId, String userName, int userPoints, 
                              int challengeId, String challengeTitle, int challengePoints) {
            this.userId = userId;
            this.userName = userName;
            this.userPoints = userPoints;
            this.challengeId = challengeId;
            this.challengeTitle = challengeTitle;
            this.challengePoints = challengePoints;
        }

        // Getters
        public int getUserId() { return userId; }
        public String getUserName() { return userName; }
        public int getUserPoints() { return userPoints; }
        public int getChallengeId() { return challengeId; }
        public String getChallengeTitle() { return challengeTitle; }
        public int getChallengePoints() { return challengePoints; }
    }
} 