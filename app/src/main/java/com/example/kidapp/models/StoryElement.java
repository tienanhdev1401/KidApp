package com.example.musicai.features.aistory.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class StoryElement {
    @DocumentId
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

    public String getId() {
        return id;
    }

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

    public ElementType getElementType() {
        return ElementType.valueOf(type);
    }

    public void setElementType(ElementType type) {
        this.type = type.name();
    }

    public enum ElementType {
        CHARACTER, SETTING, ITEM
    }
} 