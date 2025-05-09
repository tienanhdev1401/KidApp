package com.example.kidapp.models;

public class Puzzle {
    private  String Id;
    private String URLimage;
    private String name;

    public String getURLimage() {
        return URLimage;
    }

    public void setURLimage(String URLimage) {
        this.URLimage = URLimage;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
