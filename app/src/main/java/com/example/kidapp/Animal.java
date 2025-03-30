package com.example.kidapp;

public class Animal {
    private String name;
    private int imageResId;
    private int soundResId;
    private  int animalAvt;

    public Animal(String name, int imageResId,  int animalAvt) {
        this.name = name;
        this.imageResId = imageResId;
//        this.soundResId = soundResId;
        this.animalAvt = animalAvt;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getSoundResId() {
        return soundResId;
    }

    public int getAnimalAvt() {
        return animalAvt;
    }
}
