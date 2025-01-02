package models;

public class UserProfile {
    private int userId;
    private String username;
    private String nickname;
    private String gender;
    private int age;
    private String email;
    private String instagram;
    private String twitter;
    private String bio;

    public UserProfile(int userId, String username, String nickname, String gender, int age, String email, String instagram, String twitter, String bio) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.email = email;
        this.instagram = instagram;
        this.twitter = twitter;
        this.bio = bio;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public String getInstagram() { return instagram; }
    public String getTwitter() { return twitter; }
    public String getBio() { return bio; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAge(int age) { this.age = age; }
    public void setEmail(String email) { this.email = email; }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    public void setTwitter(String twitter) { this.twitter = twitter; }
    public void setBio(String bio) { this.bio = bio; }
}
