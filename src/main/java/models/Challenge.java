// File: /src/models/Challenge.java
//nambahin adfatar challenge yang telah diselesaikan user. challenge dapat menampung user mana saja yang telah diselesaikan user
package models;

public class Challenge {
    private int id;
    private String title;
    private String description;
    private int points;
    private String imageUrl;
    private String qrCodeUrl;
    private String startDate;
    private String endDate;

    public Challenge(int id, String title, String description, int points, String imageUrl, String qrCodeUrl, String startDate, String endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.points = points;
        this.imageUrl = imageUrl;
        this.qrCodeUrl = qrCodeUrl;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPoints() { return points; }
    public String getImageUrl() { return imageUrl; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPoints(int points) { this.points = points; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return title;
    }
}
