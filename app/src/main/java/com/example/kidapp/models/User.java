package com.example.kidapp.models;

public class User {
    private String email;
    private String password;
    private String Name;

    // Default constructor for Firebase
    public User() {
    }

    public User(String email, String password, String Name) {
        this.email = email;
        this.password = password;
        this.Name = Name;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
}

