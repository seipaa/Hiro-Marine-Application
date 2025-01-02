package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import models.User;
import utils.DatabaseConnection;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public UserDAO() {
    }

    public List<User> getTopThreeUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, name, total_points, status, join_date FROM users ORDER BY total_points DESC LIMIT 3";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                try {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String name = rs.getString("name");
                    int totalPoints = rs.getInt("total_points");
                    String status = rs.getString("status");
                    LocalDateTime joinDate = rs.getTimestamp("join_date") != null ?
                            rs.getTimestamp("join_date").toLocalDateTime() :
                            LocalDateTime.now();

                    User user = new User(id, username, name, null, totalPoints, status, joinDate);
                    users.add(user);
                } catch (SQLException e) {
                    LOGGER.severe("Error reading user data: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting top three users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, name, total_points, status, join_date FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocalDateTime joinDate = rs.getTimestamp("join_date") != null ?
                        rs.getTimestamp("join_date").toLocalDateTime() :
                        LocalDateTime.now();

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        null,
                        rs.getInt("total_points"),
                        rs.getString("status"),
                        joinDate
                );
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public User getUserByUsername(String username) {
        String query = "SELECT id, username, name, total_points, status, join_date FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LocalDateTime joinDate = rs.getTimestamp("join_date") != null ?
                        rs.getTimestamp("join_date").toLocalDateTime() :
                        LocalDateTime.now();

                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        null,
                        rs.getInt("total_points"),
                        rs.getString("status"),
                        joinDate
                );
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByUsernameAndPassword(String username, String password) {
        String query = "SELECT id, username, name, total_points, status, join_date FROM users WHERE username = ? AND user_password = ? AND status != 'DELETED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LocalDateTime joinDate = rs.getTimestamp("join_date") != null ?
                        rs.getTimestamp("join_date").toLocalDateTime() :
                        LocalDateTime.now();

                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        null,
                        rs.getInt("total_points"),
                        rs.getString("status"),
                        joinDate
                );
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting user by username and password: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND user_password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            LOGGER.severe("Error validating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void updateUserPoints(int userId, int points) {
        String query = "UPDATE users SET total_points = total_points + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, points);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error updating user points: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateUserStatus(int userId, String status) {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error updating user status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
