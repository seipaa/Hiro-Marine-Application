package dao;

import models.RecomDiscuss;
import utils.DatabaseHelper;
import utils.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecomDiscussDAO {

    public void addDiscussion(RecomDiscuss discussion) throws SQLException {
        String query = "INSERT INTO recom_discussions (user_id, user_name, location_name, description, image_data, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, discussion.getUserId());
            stmt.setString(2, discussion.getUserName());
            stmt.setString(3, discussion.getLocationName());
            stmt.setString(4, discussion.getDescription());
            
            // Handle image data
            if (discussion.getImageData() != null) {
                stmt.setBlob(5, new ByteArrayInputStream(discussion.getImageData()));
            } else {
                stmt.setNull(5, java.sql.Types.BLOB);
            }
            
            stmt.setTimestamp(6, Timestamp.valueOf(discussion.getTimestamp()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating discussion failed, no rows affected.");
            }

            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    discussion.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating discussion failed, no ID obtained.");
                }
            }

            System.out.println("Discussion added successfully with ID: " + discussion.getId());
        } catch (SQLException e) {
            System.err.println("Error adding discussion: " + e.getMessage());
            throw e;
        }
    }

    public List<RecomDiscuss> getDiscussionsByLocation(String locationName) throws SQLException {
        List<RecomDiscuss> discussions = new ArrayList<>();
        String query = "SELECT * FROM recom_discussions WHERE location_name = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, locationName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] imageData = null;
                Blob blob = rs.getBlob("image_data");
                if (blob != null) {
                    imageData = blob.getBytes(1, (int) blob.length());
                }

                RecomDiscuss discussion = new RecomDiscuss(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("user_name"),
                    rs.getString("location_name"),
                    rs.getString("description"),
                    imageData,
                    rs.getTimestamp("timestamp").toLocalDateTime()
                );
                discussions.add(discussion);
            }
        } catch (SQLException e) {
            System.err.println("Error getting discussions: " + e.getMessage());
            throw e;
        }
        return discussions;
    }

    public void deleteDiscussion(int discussionId) throws SQLException {
        String query = "DELETE FROM recom_discussions WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, discussionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting discussion: " + e.getMessage());
            throw e;
        }
    }

    public void updateDiscussion(RecomDiscuss discussion) throws SQLException {
        String query = "UPDATE recom_discussions SET description = ?, image_data = ? WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, discussion.getDescription());
            
            // Handle image data
            if (discussion.getImageData() != null) {
                stmt.setBlob(2, new ByteArrayInputStream(discussion.getImageData()));
            } else {
                stmt.setNull(2, java.sql.Types.BLOB);
            }
            
            stmt.setInt(3, discussion.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating discussion: " + e.getMessage());
            throw e;
        }
    }
}
