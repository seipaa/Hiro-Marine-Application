package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.UserProfile;

public class UserProfileDAO extends BaseDAO {

    public UserProfile getUserProfile(int userId) throws SQLException {
        String query = "SELECT p.*, u.name as username FROM user_profiles p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.user_id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserProfile(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("nickname"),
                            rs.getString("gender"),
                            rs.getInt("age"),
                            rs.getString("email"),
                            rs.getString("instagram"),
                            rs.getString("twitter"),
                            rs.getString("bio")
                    );
                }
            }
        }
        return null;
    }

    public void saveUserProfile(UserProfile profile) throws SQLException {
        String upsertQuery = "INSERT INTO user_profiles (user_id, nickname, gender, age, email, instagram, twitter, bio) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "nickname = VALUES(nickname), " +
                "gender = VALUES(gender), " +
                "age = VALUES(age), " +
                "email = VALUES(email), " +
                "instagram = VALUES(instagram), " +
                "twitter = VALUES(twitter), " +
                "bio = VALUES(bio)";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(upsertQuery)) {

            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getNickname());
            stmt.setString(3, profile.getGender());
            stmt.setInt(4, profile.getAge());
            stmt.setString(5, profile.getEmail());
            stmt.setString(6, profile.getInstagram());
            stmt.setString(7, profile.getTwitter());
            stmt.setString(8, profile.getBio());

            stmt.executeUpdate();
        }
    }
}