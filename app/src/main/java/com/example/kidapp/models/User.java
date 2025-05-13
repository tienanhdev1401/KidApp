package com.example.kidapp.models;

import java.util.List;
import java.util.Map;

public class User {
    private String email;
    private String password;
    private String username;
    private java.util.List<String> achievements;
    private java.util.List<String> storyIds;
    private int totalListeningTime;
    private java.util.Map<String, com.example.kidapp.models.GameProgress> gameProgress;

    // Default constructor for Firebase
    public User() {
    }

    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public User(String email, String password, String username, java.util.List<String> achievements, java.util.List<String> storyIds, int totalListeningTime, java.util.Map<String, com.example.kidapp.models.GameProgress> gameProgress) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.achievements = achievements;
        this.storyIds = storyIds;
        this.totalListeningTime = totalListeningTime;
        this.gameProgress = gameProgress;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public java.util.List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(java.util.List<String> achievements) {
        this.achievements = achievements;
    }

    public List<String> getStoryIds() {
        return storyIds;
    }

    public void setStoryIds(java.util.List<String> storyIds) {
        this.storyIds = storyIds;
    }

    public int getTotalListeningTime() {
        return totalListeningTime;
    }

    public void setTotalListeningTime(int totalListeningTime) {
        this.totalListeningTime = totalListeningTime;
    }

    public Map<String,GameProgress> getGameProgress() {
        return gameProgress;
    }

    public void setGameProgress(Map<String, GameProgress> gameProgress) {
        this.gameProgress = gameProgress;
    }
}

