package com.example.kidapp.models;

import java.io.Serializable;
import java.util.List;

public class WordGuessStage implements Serializable {
    private int id;
    private String answer;
    private String imageUrl;
    private List<String> suggestion;

    public WordGuessStage() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getSuggestion() { return suggestion; }
    public void setSuggestion(List<String> suggestion) { this.suggestion = suggestion; }
} 