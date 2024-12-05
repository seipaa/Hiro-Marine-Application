package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.User;

public class LeaderboardFetcher {

    public List<User> getTopUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY total_points DESC LIMIT 3";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("user_password")
                );
                user.setTotalPoints(rs.getInt("total_points"));
                String status = rs.getString("status");
                if (status != null) {
                    user.setStatus(status);
                }
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("user_password")
                );
                user.setTotalPoints(rs.getInt("total_points"));
                String status = rs.getString("status");
                if (status != null) {
                    user.setStatus(status);
                }
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUserByName(String name) {
        String query = "SELECT * FROM users WHERE name = ?";
        
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("user_password")
                    );
                    user.setTotalPoints(rs.getInt("total_points"));
                    String status = rs.getString("status");
                    if (status != null) {
                        user.setStatus(status);
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyAndAddPoints(String userName, int points) {
        String query = "UPDATE users SET total_points = total_points + ? WHERE name = ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, points);
            preparedStatement.setString(2, userName);
            int rowsUpdated = preparedStatement.executeUpdate();

            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user points: " + e.getMessage());
            return false;
        }
    }

    public boolean isChallengeVerified(int userId, int challengeId) {
        String query = "SELECT * FROM user_challenges WHERE user_id = ? AND challenge_id = ?";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking challenge verification: " + e.getMessage());
            return false;
        }
    }

    public void markChallengeAsVerified(int userId, int challengeId) {
        String query = "INSERT INTO user_challenges (user_id, challenge_id) VALUES (?, ?)";
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking challenge as verified: " + e.getMessage());
        }
    }
}