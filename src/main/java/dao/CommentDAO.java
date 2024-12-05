package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.Comment;
import utils.DatabaseConnection;

public class CommentDAO {
    
    public void addComment(int challengeId, int userId, String comment) throws SQLException {
        String query = "INSERT INTO comments (challenge_id, user_id, comment, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, challengeId);
            stmt.setInt(2, userId);
            stmt.setString(3, comment);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }
    
    public List<Comment> getCommentsByChallengeId(int challengeId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.name as user_name FROM comments c " +
                      "JOIN users u ON c.user_id = u.id " +
                      "WHERE c.challenge_id = ? " +
                      "ORDER BY c.created_at DESC";
                      
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getInt("id"),
                    rs.getInt("challenge_id"),
                    rs.getInt("user_id"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getString("user_name")
                );
                comments.add(comment);
            }
        }
        return comments;
    }
} 