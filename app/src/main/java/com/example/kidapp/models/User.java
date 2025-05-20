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
    private String gender;
    private String dateOfBirth;
    private String avatarUrl;
    private String phone;
    private int scoreRanking;
    private int totalMatches;

    // Default constructor for Firebase
    public User() {
    }

    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.gender = null;
        this.dateOfBirth = null;
        this.avatarUrl = null;
        this.phone = null;
        this.scoreRanking = 0;
        this.totalMatches = 0;
    }

    public User(String email, String password, String username, String gender, String dateOfBirth, String avatarUrl, String phone) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.scoreRanking = 0;
        this.totalMatches = 0;
    }

    public User(String email, String password, String username, java.util.List<String> achievements, java.util.List<String> storyIds, int totalListeningTime, java.util.Map<String, com.example.kidapp.models.GameProgress> gameProgress, String gender, String dateOfBirth, String avatarUrl, String phone, int scoreRanking, int totalMatches) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.achievements = achievements;
        this.storyIds = storyIds;
        this.totalListeningTime = totalListeningTime;
        this.gameProgress = gameProgress;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.scoreRanking = scoreRanking;
        this.totalMatches = totalMatches;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getScoreRanking() {
        return scoreRanking;
    }

    public void setScoreRanking(int scoreRanking) {
        this.scoreRanking = scoreRanking;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
}

