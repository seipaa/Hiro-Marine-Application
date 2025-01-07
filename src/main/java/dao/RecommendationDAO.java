package dao;

import models.Recommendation;
import utils.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class RecommendationDAO {

    public void updateRating(int recommendationId, int rating, int userId) throws SQLException {
        String checkQuery = "SELECT * FROM user_ratings WHERE recommendation_id = ? AND user_id = ?";
        String insertQuery = "INSERT INTO user_ratings (recommendation_id, user_id, rating) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE user_ratings SET rating = ? WHERE recommendation_id = ? AND user_id = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            // Check if user has already rated
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, recommendationId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Update existing rating
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, rating);
                        updateStmt.setInt(2, recommendationId);
                        updateStmt.setInt(3, userId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert new rating
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, recommendationId);
                        insertStmt.setInt(2, userId);
                        insertStmt.setInt(3, rating);
                        insertStmt.executeUpdate();
                    }
                }
            }

            // Update average rating in recommendations table
            updateAverageRating(recommendationId);
        }
    }

    public int getUserRating(int recommendationId, int userId) throws SQLException {
        String query = "SELECT rating FROM user_ratings WHERE recommendation_id = ? AND user_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, recommendationId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating");
            }
            return 0; // No rating found
        }
    }

    public double getAverageRating(int recommendationId) throws SQLException {
        String query = "SELECT AVG(rating) as avg_rating FROM user_ratings WHERE recommendation_id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, recommendationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
            return 0.0;
        }
    }

    private void updateAverageRating(int recommendationId) throws SQLException {
        String updateQuery = "UPDATE recommendations SET rating = ? WHERE id = ?";
        double avgRating = getAverageRating(recommendationId);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setDouble(1, avgRating);
            stmt.setInt(2, recommendationId);
            stmt.executeUpdate();
        }
    }

    public List<Recommendation> getAllRecommendations() throws SQLException {
        List<Recommendation> recommendations = new ArrayList<>();
        String query = "SELECT * FROM recommendations ORDER BY id DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Recommendation recommendation = new Recommendation(
                        rs.getInt("id"),
                        rs.getString("location_name"),
                        rs.getString("description"),
                        null, // We don't use image_url
                        rs.getDouble("rating")
                );

                // Get image data
                try {
                    Blob blob = rs.getBlob("image_data");
                    if (blob != null) {
                        byte[] imageData = blob.getBytes(1, (int) blob.length());
                        recommendation.setImageData(imageData);
                        blob.free(); // Release the blob resource
                    }
                } catch (SQLException e) {
                    System.err.println("Error reading image BLOB: " + e.getMessage());
                }

                recommendations.add(recommendation);
            }
        }
        return recommendations;
    }

    public void addRecommendation(Recommendation recommendation) throws SQLException {
        String query = "INSERT INTO recommendations (location_name, description, image_data, rating) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, recommendation.getLocationName());
            stmt.setString(2, recommendation.getDescription());

            // Handle image data
            if (recommendation.getImageData() != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(recommendation.getImageData());
                stmt.setBinaryStream(3, bais, recommendation.getImageData().length);
            } else {
                stmt.setNull(3, Types.LONGVARBINARY);
            }

            stmt.setDouble(4, 0.0); // Default rating

            stmt.executeUpdate();

            // Get generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recommendation.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void deleteRecommendation(Recommendation recommendation) throws SQLException {
        // First delete related ratings
        String deleteRatingsQuery = "DELETE FROM user_ratings WHERE recommendation_id = ?";
        String deleteRecommendationQuery = "DELETE FROM recommendations WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            // Delete ratings first
            try (PreparedStatement stmt = conn.prepareStatement(deleteRatingsQuery)) {
                stmt.setInt(1, recommendation.getId());
                stmt.executeUpdate();
            }

            // Then delete recommendation
            try (PreparedStatement stmt = conn.prepareStatement(deleteRecommendationQuery)) {
                stmt.setInt(1, recommendation.getId());
                stmt.executeUpdate();
            }
        }
    }

    public byte[] getImageData(int recommendationId) throws SQLException {
        String query = "SELECT image_data FROM recommendations WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, recommendationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Blob blob = rs.getBlob("image_data");
                if (blob != null) {
                    return blob.getBytes(1, (int) blob.length());
                }
            }
            return null;
        }
    }
}