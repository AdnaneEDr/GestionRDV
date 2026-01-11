package com.example.gestionrdv.models;

/**
 * User Model - Base class for all users
 */
public class User {
    private long id;
    private String email;
    private String password;
    private String userType;
    private String createdAt;

    public User() {}

    public User(long id, String email, String userType) {
        this.id = id;
        this.email = email;
        this.userType = userType;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}