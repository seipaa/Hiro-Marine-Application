package models;

public class MarineSpecies {
    private int id;
    private String imageUrl;
    private String name;
    private String latinName;
    private String description;
    private String type;
    private String status;

    public MarineSpecies(int id, String imageUrl, String name, String latinName,
                         String description, String type) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.latinName = latinName;
        this.description = description;
        this.type = type;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLatinName() { return latinName; }
    public void setLatinName(String latinName) { this.latinName = latinName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
