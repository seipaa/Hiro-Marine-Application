// File: /src/models/User.java
package models;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String name;
    private String userPassword;
    private int totalPoints;
    private String status;
    private LocalDateTime joinDate;

    public User(int id, String username, String name, String userPassword) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.userPassword = userPassword;
        this.totalPoints = 0;
        this.status = "ACTIVE";
        this.joinDate = LocalDateTime.now();
    }

    public User(int id, String username, String name, String userPassword, int totalPoints, String status, LocalDateTime joinDate) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.userPassword = userPassword;
        this.totalPoints = totalPoints;
        this.status = status;
        this.joinDate = joinDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public boolean isFrozen() {
        return "FROZEN".equalsIgnoreCase(this.status);
    }

    public boolean isBanned() {
        return "BANNED".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return name;
    }
}
