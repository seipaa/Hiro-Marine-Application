package models;

import java.sql.Timestamp;

public class News {
    private int id;
    private int adminId;
    private String title;
    private String description;
    private String imageUrl;
    private int position;
    private Timestamp createdAt;
    private boolean isBreakingNews;

    // Default constructor
    public News() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isBreakingNews = false;
    }

    // Constructor with parameters
    public News(int id, int adminId, String title, String description, String imageUrl, Timestamp createdAt, boolean isBreakingNews) {
        this.id = id;
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isBreakingNews = isBreakingNews;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isBreakingNews() {
        return isBreakingNews;
    }

    public void setBreakingNews(boolean breakingNews) {
        isBreakingNews = breakingNews;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", adminId=" + adminId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", position=" + position +
                ", createdAt=" + createdAt +
                ", isBreakingNews=" + isBreakingNews +
                '}';
    }
}
