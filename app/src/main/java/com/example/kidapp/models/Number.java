package com.example.kidapp.Model;

public class Number {
    private int id;
    private String name;
    private String imageUrl;
    private String soundUrl;

    public Number() {}

    public Number(int id, String name, String imageUrl, String soundUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.soundUrl = soundUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSoundUrl() {
        return soundUrl;
    }

    public void setSoundUrl(String soundUrl) {
        this.soundUrl = soundUrl;
    }
} 