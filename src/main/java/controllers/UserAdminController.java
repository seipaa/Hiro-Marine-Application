package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import dao.UserDAO;
import utils.AlertUtils;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import java.time.format.DateTimeFormatter;

public class UserAdminController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, Integer> pointsColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> joinDateColumn;
    @FXML private TableColumn<User, Void> actionColumn;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;

    private UserDAO userDAO = new UserDAO();
    private MainController mainController;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadUsers();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Setup join date column
        joinDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getJoinDate() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> cellData.getValue().getJoinDate().format(dateFormatter)
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });

        setupActionColumn();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> {
            return new TableCell<User, Void>() {
                private final Button freezeButton = new Button();
                private final Button unfreezeButton = new Button();

                {
                    freezeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    unfreezeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    
                    freezeButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        freezeUser(user);
                    });
                    
                    unfreezeButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        unfreezeUser(user);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        User user = getTableView().getItems().get(getIndex());
                        if ("FROZEN".equals(user.getStatus())) {
                            unfreezeButton.setText("Unfreeze");
                            setGraphic(unfreezeButton);
                        } else {
                            freezeButton.setText("Freeze");
                            setGraphic(freezeButton);
                        }
                    }
                }
            };
        });
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "ACTIVE", "FROZEN"));
        statusFilter.setValue("All");
        
        statusFilter.setOnAction(e -> filterUsers());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
    }

    private void filterUsers() {
        try {
            String status = statusFilter.getValue();
            String search = searchField.getText().toLowerCase();
            
            ObservableList<User> allUsers = FXCollections.observableArrayList(userDAO.getAllUsers());
            ObservableList<User> filteredUsers = FXCollections.observableArrayList();
            
            for (User user : allUsers) {
                boolean statusMatch = "All".equals(status) || status.equals(user.getStatus());
                boolean searchMatch = search.isEmpty() || 
                                    user.getUsername().toLowerCase().contains(search) ||
                                    user.getName().toLowerCase().contains(search);
                
                if (statusMatch && searchMatch) {
                    filteredUsers.add(user);
                }
            }
            
            userTable.setItems(filteredUsers);
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to filter users: " + e.getMessage());
        }
    }

    private void freezeUser(User user) {
        try {
            userDAO.updateUserStatus(user.getId(), "FROZEN");
            AlertUtils.showInfo("Success", "User account has been frozen");
            refreshTable();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to freeze user: " + e.getMessage());
        }
    }

    private void unfreezeUser(User user) {
        try {
            userDAO.updateUserStatus(user.getId(), "ACTIVE");
            AlertUtils.showInfo("Success", "User account has been unfrozen");
            refreshTable();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to unfreeze user: " + e.getMessage());
        }
    }

    @FXML
    private void refreshTable() {
        loadUsers();
        filterUsers();
    }

    private void loadUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(userDAO.getAllUsers());
            userTable.setItems(users);
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to load users: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
} 