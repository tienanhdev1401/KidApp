package com.example.kidapp.models;

import java.util.Map;

public class GameProgress {
    private int levelReached;
    private Map<String, Integer> scores;

    public GameProgress() {}

    public GameProgress(int levelReached, Map<String, Integer> scores) {
        this.levelReached = levelReached;
        this.scores = scores;
    }

    public int getLevelReached() {
        return levelReached;
    }

    public void setLevelReached(int levelReached) {
        this.levelReached = levelReached;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }
}