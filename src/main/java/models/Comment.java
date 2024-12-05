package models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int challengeId;
    private int userId;
    private String comment;
    private LocalDateTime createdAt;
    private String userName;

    public Comment(int id, int challengeId, int userId, String comment, LocalDateTime createdAt, String userName) {
        this.id = id;
        this.challengeId = challengeId;
        this.userId = userId;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userName = userName;
    }

    // Getters dan Setters
    public int getId() { return id; }
    public int getChallengeId() { return challengeId; }
    public int getUserId() { return userId; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUserName() { return userName; }
}
