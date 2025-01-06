package models;

import java.time.LocalDateTime;

public class RecomDiscuss {
    private int id;
    private int userId;
    private String userName;
    private String locationName;
    private String description;
    private byte[] imageData;
    private LocalDateTime timestamp;

    public RecomDiscuss(int id, int userId, String userName, String locationName, String description, byte[] imageData, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.locationName = locationName;
        this.description = description;
        this.imageData = imageData;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp =timestamp;}
}
