package com.example.kidapp.models;

import java.io.Serializable;
import java.util.List;

public class WordGuessLevel implements Serializable {
    private int id;
    private String name;
    private String title;
    private List<WordGuessStage> stages;

    public WordGuessLevel() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<WordGuessStage> getStages() { return stages; }
    public void setStages(List<WordGuessStage> stages) { this.stages = stages; }
} 