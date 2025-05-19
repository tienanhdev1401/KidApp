package com.example.kidapp.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class storyAiHistoryModel implements Serializable {
    private String id;
    private String title;
    private String imageUrl;
    private int sceneCount;
    private Timestamp createdAt;
    private List<SceneModel> scenes;

    // Constructor rỗng cần thiết cho Firestore
    public storyAiHistoryModel() {
        this.scenes = new ArrayList<>();
    }

    public storyAiHistoryModel(String id, String title, String imageUrl, int sceneCount, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.sceneCount = sceneCount;
        this.createdAt = createdAt;
        this.scenes = new ArrayList<>();
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getSceneCount() {
        return sceneCount;
    }

    public void setSceneCount(int sceneCount) {
        this.sceneCount = sceneCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<SceneModel> getScenes() {
        return scenes;
    }

    public void setScenes(List<SceneModel> scenes) {
        this.scenes = scenes;
    }

    // Class để lưu thông tin cho từng cảnh trong truyện
    public static class SceneModel implements Serializable {
        private String imageUrl;
        private String content;

        // Constructor rỗng cần thiết cho Firestore
        public SceneModel() {
        }

        public SceneModel(String imageUrl, String content) {
            this.imageUrl = imageUrl;
            this.content = content;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
} 