package com.example.kidapp.models;

import java.io.Serializable;
import java.util.List;

public class FlipCardLevel implements Serializable {
    private int id;
    private String topic;
    private List<FlipCard> cards;

    // Constructor rỗng cho Firebase
    public FlipCardLevel() {
    }

    public FlipCardLevel(int id, String topic, List<FlipCard> cards) {
        this.id = id;
        this.topic = topic;
        this.cards = cards;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<FlipCard> getCards() {
        return cards;
    }

    public void setCards(List<FlipCard> cards) {
        this.cards = cards;
    }
} 