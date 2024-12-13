/**
 * Christopher Carnell
 * CS-360
 *
 * This model class represents a user.
 * It contains user-related data such as ID, username, hashed password, phone number, and goal weight.
 * It provides getters and setters for accessing and modifying user data.
 */


package com.cs360.weightwatcher;

public class User {
    private long id;
    private String username;
    private String password;
    private String phoneNumber;
    private double goalWeight;

    public User(long id, String username, String password, String phoneNumber, double goalWeight) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.goalWeight = goalWeight;
    }

    //getters and setters
    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public double getGoalWeight() { return goalWeight; }

    public void setId(long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setGoalWeight(double goalWeight) { this.goalWeight = goalWeight; }
}
