/**
 * Christopher Carnell
 * CS-360
 *
 * This model class represents a weight entry made by the user.
 * It contains data such as the entry ID, user ID, date of the entry, and the weight value.
 * It provides constructors and getters/setters for creating and accessing weight entries.
 */


package com.cs360.weightwatcher;

public class WeightEntry {
    private long id;
    private long userId;
    private String date;
    private double weight;

    //constructor without id (for new entries)
    public WeightEntry(long userId, String date, double weight) {
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    //existing constructor with id (used when retrieving from database)
    public WeightEntry(long id, long userId, String date, double weight) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    //getters and setters

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getDate() { return date; }
    public double getWeight() { return weight; }

    public void setId(long id) { this.id = id; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setDate(String date) { this.date = date; }
    public void setWeight(double weight) { this.weight = weight; }
}