package com.example.kidapp.models;

public class SkillItem {
    private String name;
    private int imageResId;

    public SkillItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
