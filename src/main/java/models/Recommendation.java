package models;

public class Recommendation {
    private int id;
    private String locationName;
    private String description;
    private String imageUrl;
    private double rating;
    private String category;

    public Recommendation(int id, String locationName, String description, String imageUrl, double rating) {
        this.id = id;
        this.locationName = locationName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

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

    public double getRating() {
        return rating;
    }

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

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return locationName;
    }
}