package com.example.kidapp.models;

public class ImageItem {
    private final int imageResource;
    private final String name;
    private final String value;
    
    public ImageItem(int imageResource, String name, String value) {
        this.imageResource = imageResource;
        this.name = name;
        this.value = value;
    }
    
    public int getImageResource() {
        return imageResource;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
} 