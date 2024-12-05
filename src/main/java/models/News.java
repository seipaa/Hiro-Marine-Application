package models;

public class News {
    private int id;
    private int adminId;
    private String title;
    private String description;
    private String imageUrl;

    // Constructor
    public News(int id, int adminId, String title, String description, String imageUrl) {
        this.id = id;
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getter untuk id
    public int getNewsId() {
        return id;
    }

    // Getter lainnya
    public int getAdminId() {
        return adminId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "Image: " + imageUrl + "\n" +
                "Title: " + title + "\n" +
                "Description: " + description + "\n" +
                "Admin: " + adminId + "\n";
    }
}
