package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Recommendation;

public class RecommendationDAO extends BaseDAO {

    public void addRecommendation(Recommendation recommendation) throws SQLException {
        String query = "INSERT INTO recommendations (location_name, description, image_url, rating) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, recommendation.getLocationName());
            stmt.setString(2, recommendation.getDescription());
            stmt.setString(3, recommendation.getImageUrl());
            stmt.setDouble(4, recommendation.getRating());
            stmt.executeUpdate();
        }
    }

    public void updateRecommendation(Recommendation recommendation) throws SQLException {
        String query = "UPDATE recommendations SET location_name = ?, description = ?, image_url = ?, rating = ? WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, recommendation.getLocationName());
            stmt.setString(2, recommendation.getDescription());
            stmt.setString(3, recommendation.getImageUrl());
            stmt.setDouble(4, recommendation.getRating());
            stmt.setInt(5, recommendation.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteRecommendation(int id) throws SQLException {
        String query = "DELETE FROM recommendations WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Recommendation> getAllRecommendations() throws SQLException {
        List<Recommendation> recommendations = new ArrayList<>();
        String query = "SELECT * FROM recommendations";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recommendations.add(new Recommendation(
                    rs.getInt("id"),
                    rs.getString("location_name"),
                    rs.getString("description"),
                    rs.getString("image_url"),
                    rs.getDouble("rating")
                ));
            }
        }
        return recommendations;
    }

    public Recommendation getRecommendationById(int id) throws SQLException {
        String query = "SELECT * FROM recommendations WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Recommendation(
                        rs.getInt("id"),
                        rs.getString("location_name"),
                        rs.getString("description"),
                        rs.getString("image_url"),
                        rs.getDouble("rating")
                    );
                }
            }
        }
        return null;
    }
}
