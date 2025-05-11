package com.example.kidapp.models;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.Exclude;

public class StoryElement {
    private String id;
    
    @PropertyName("name")
    private String name;
    
    @PropertyName("imageUrl")
    private String imageUrl;
    
    @PropertyName("type")
    private String type; // Changed from ElementType to String

    public StoryElement() {
        // Required empty constructor for Firestore
    }

    public StoryElement(String name, String imageUrl, ElementType type) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type.name();
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }

    @Exclude
    public ElementType getElementType() {
        return type != null ? ElementType.valueOf(type) : null;
    }

    @Exclude
    public void setElementType(ElementType type) {
        this.type = type != null ? type.name() : null;
    }

    public enum ElementType {
        CHARACTER, SETTING, ITEM
    }
} 