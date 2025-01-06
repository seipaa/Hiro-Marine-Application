package models;

public class Recommendation {
    private int id;
    private String locationName;
    private String description;
    private String imageUrl;
    private byte[] imageData;
    private double rating;

    // Default constructor
    public Recommendation() {
        this.id = 0;
        this.locationName = "";
        this.description = "";
        this.imageUrl = "";
        this.imageData = null;
        this.rating = 0.0;
    }

    // Constructor with parameters
    public Recommendation(int id, String locationName, String description, String imageUrl, double rating) {
        this.id = id;
        this.locationName = locationName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imageData = null;
        this.rating = rating;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public double getRating() {
        return rating;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
