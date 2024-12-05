package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Challenge;

public class ChallengeDetailsFetcher {

    public Challenge getChallengeDetails(String challengeTitle) {
        Challenge challenge = null;
        String query = "SELECT * FROM challenges WHERE title = ?";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, challengeTitle);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Challenge found: " + resultSet.getString("title"));
                challenge = new Challenge(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getInt("points"),
                        resultSet.getString("image_url"),
                        resultSet.getString("qr_code_url"),
                        resultSet.getString("start_date"),
                        resultSet.getString("end_date")
                );
            } else {
                System.out.println("No challenge found with title: " + challengeTitle);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching challenge details: " + e.getMessage());
        }

        return challenge;
    }

    public List<Challenge> getAllChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        String query = "SELECT * FROM challenges";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Challenge challenge = new Challenge(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getInt("points"),
                        resultSet.getString("image_url"),
                        resultSet.getString("qr_code_url"),
                        resultSet.getString("start_date"),
                        resultSet.getString("end_date")
                );
                challenges.add(challenge);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching challenges: " + e.getMessage());
        }

        return challenges;
    }
}
