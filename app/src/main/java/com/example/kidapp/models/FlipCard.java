package com.example.kidapp.models;

import java.io.Serializable;

public class FlipCard implements Serializable {
    private String cardText;
    private String cardImageUrl;

    // Constructor rỗng cho Firebase
    public FlipCard() {
    }

    public FlipCard(String cardText, String cardImageUrl) {
        this.cardText = cardText;
        this.cardImageUrl = cardImageUrl;
    }

    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }
} 