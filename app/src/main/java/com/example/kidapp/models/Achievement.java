package com.example.kidapp.models;

public class Achievement {
    private String id;
    private String name;
    private String description;
    private String imageUrl; // Đường dẫn ảnh thành tựu
    private Integer minListeningTime; // Thời gian nghe tối thiểu (phút)
    private Integer minStoriesRead;   // Số truyện đã đọc tối thiểu
    private Integer minGameLevel;     // Level game cao nhất đã đạt

    public Achievement() {}

    public Achievement(String id, String name, String description, String imageUrl,
                       Integer minListeningTime, Integer minStoriesRead, Integer minGameLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.minListeningTime = minListeningTime;
        this.minStoriesRead = minStoriesRead;
        this.minGameLevel = minGameLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getMinListeningTime() {
        return minListeningTime;
    }

    public void setMinListeningTime(Integer minListeningTime) {
        this.minListeningTime = minListeningTime;
    }

    public Integer getMinStoriesRead() {
        return minStoriesRead;
    }

    public void setMinStoriesRead(Integer minStoriesRead) {
        this.minStoriesRead = minStoriesRead;
    }

    public Integer getMinGameLevel() {
        return minGameLevel;
    }

    public void setMinGameLevel(Integer minGameLevel) {
        this.minGameLevel = minGameLevel;
    }
}