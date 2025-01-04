package dao;

import models.RecomDiscuss;
import utils.DatabaseHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecomDiscussDAO {

    public void addDiscussion(RecomDiscuss discussion) throws SQLException {
        String query = "INSERT INTO recom_discussions (user_id, user_name, location_name, description, image_url, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, discussion.getUserId());
            stmt.setString(2, discussion.getUserName());
            stmt.setString(3, discussion.getLocationName());
            stmt.setString(4, discussion.getDescription());
            stmt.setString(5, discussion.getImageUrl());
            stmt.setTimestamp(6, Timestamp.valueOf(discussion.getTimestamp()));

            stmt.executeUpdate();
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
                RecomDiscuss discussion = new RecomDiscuss(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("location_name"),
                        rs.getString("description"),
                        rs.getString("image_url"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                );
                discussions.add(discussion);
            }
        }
        return discussions;
    }
}
