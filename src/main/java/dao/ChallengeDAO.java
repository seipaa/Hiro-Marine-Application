package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.Challenge;

public class ChallengeDAO extends BaseDAO {
    private static final Logger LOGGER = Logger.getLogger(ChallengeDAO.class.getName());

    // Membuat objek Challenge dari ResultSet
    private Challenge createChallengeFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        int points = rs.getInt("points");
        String imageUrl = rs.getString("image_url");
        String qrCodeUrl = rs.getString("qr_code_url");
        String startDate = rs.getString("start_date") != null ? rs.getString("start_date") : "defaultStartDate";
        String endDate = rs.getString("end_date") != null ? rs.getString("end_date") : "defaultEndDate";
        // Inisialisasi Challenge dengan data dari ResultSet
        Challenge challenge = new Challenge(id, title, description, points, imageUrl, qrCodeUrl, startDate, endDate);
        return challenge;
    }

    // Mendapatkan Challenge berdasarkan ID
    public Challenge getChallengeById(int id) {
        if (id <= 0) {
            LOGGER.log(Level.WARNING, "Invalid ID provided: " + id);
            return null;
        }

        String query = "SELECT * FROM challenges WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Mengembalikan Challenge jika ditemukan
                    return createChallengeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching challenge by ID: " + e.getMessage(), e);
        }
        return null;
    }

    // Tambahkan metode CRUD lainnya sesuai kebutuhan

    public void addChallenge(Challenge challenge) throws SQLException {
        String query = "INSERT INTO challenges (title, description, points, image_url, qr_code_url, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, challenge.getTitle());
            stmt.setString(2, challenge.getDescription());
            stmt.setInt(3, challenge.getPoints());
            stmt.setString(4, challenge.getImageUrl());
            stmt.setString(5, challenge.getQrCodeUrl());
            stmt.setString(6, challenge.getStartDate());
            stmt.setString(7, challenge.getEndDate());
            stmt.executeUpdate();
        }
    }

    public void updateChallenge(Challenge challenge) throws SQLException {
        String query = "UPDATE challenges SET title = ?, description = ?, points = ?, image_url = ?, qr_code_url = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, challenge.getTitle());
            stmt.setString(2, challenge.getDescription());
            stmt.setInt(3, challenge.getPoints());
            stmt.setString(4, challenge.getImageUrl());
            stmt.setString(5, challenge.getQrCodeUrl());
            stmt.setString(6, challenge.getStartDate());
            stmt.setString(7, challenge.getEndDate());
            stmt.setInt(8, challenge.getId());
            stmt.executeUpdate();
        }
    }

    // Tambahkan method baru untuk menghapus komentar
    private void deleteComments(int challengeId) throws SQLException {
        String query = "DELETE FROM comments WHERE challenge_id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, challengeId);
            stmt.executeUpdate();
        }
    }

    // Update method deleteChallenge untuk menghapus komentar terlebih dahulu
    public void deleteChallenge(int challengeId) throws SQLException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false); // Start transaction
            
            // 1. Hapus dari user_challenges jika ada
            String deleteUserChallengesQuery = "DELETE FROM user_challenges WHERE challenge_id = ?";
            try (PreparedStatement stmt = con.prepareStatement(deleteUserChallengesQuery)) {
                stmt.setInt(1, challengeId);
                stmt.executeUpdate();
            }
            
            // 2. Hapus comments
            String deleteCommentsQuery = "DELETE FROM comments WHERE challenge_id = ?";
            try (PreparedStatement stmt = con.prepareStatement(deleteCommentsQuery)) {
                stmt.setInt(1, challengeId);
                stmt.executeUpdate();
            }
            
            // 3. Hapus challenge
            String deleteChallengeQuery = "DELETE FROM challenges WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(deleteChallengeQuery)) {
                stmt.setInt(1, challengeId);
                stmt.executeUpdate();
            }
            
            con.commit(); // Commit transaction
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Rollback jika terjadi error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Reset auto-commit
                    con.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }
    }

    public List<String> getComments(int challengeId) {
        List<String> comments = new ArrayList<>();
        String query = "SELECT comment FROM comments WHERE challenge_id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(rs.getString("comment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<Challenge> getAllChallenges() throws SQLException {
        List<Challenge> challenges = new ArrayList<>();
        String query = "SELECT * FROM challenges";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                challenges.add(createChallengeFromResultSet(rs));
            }
        }
        return challenges;
    }

    public void markChallengeAsCompleted(int userId, int challengeId) throws SQLException {
        String query = "INSERT INTO completed_challenges (user_id, challenge_id, completion_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            stmt.executeUpdate();
        }
    }

    public List<Challenge> getCompletedChallenges(int userId) throws SQLException {
        List<Challenge> completedChallenges = new ArrayList<>();
        String query = "SELECT c.* FROM challenges c " +
                      "INNER JOIN user_challenges uc ON c.id = uc.challenge_id " +
                      "WHERE uc.user_id = ? AND uc.status = 'COMPLETED' " +
                      "ORDER BY c.start_date DESC";
                      
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
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
                completedChallenges.add(challenge);
            }
        }
        return completedChallenges;
    }
}
