/**
 * Christopher Carnell
 * This model class represents a weight entry made by the user.
 * It contains data such as the entry ID, user ID, date of the entry, and the weight value.
 * It provides constructors and getters/setters for creating and accessing weight entries.
 */


package com.cs360.weightwatcher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a weight entry for a user, including the date and weight.
 */
public class WeightEntry implements Comparable<WeightEntry> {
    private long id;
    private final long userId;
    private LocalDate date;
    private double weight;

    // Date formatter for parsing and formatting dates
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor without ID (for new entries).
     *
     * @param userId The user's ID.
     * @param date   The date as a String in "yyyy-MM-dd" format.
     * @param weight The weight value.
     * @throws IllegalArgumentException if date parsing fails.
     */
    public WeightEntry(long userId, String date, double weight) {
        this.userId = userId;
        this.date = parseDate(date);
        this.weight = weight;
    }

    /**
     * Constructor with ID (used when retrieving from the database).
     *
     * @param id     The entry ID.
     * @param userId The user's ID.
     * @param date   The date as a String in "yyyy-MM-dd" format.
     * @param weight The weight value.
     * @throws IllegalArgumentException if date parsing fails.
     */
    public WeightEntry(long id, long userId, String date, double weight) {
        this.id = id;
        this.userId = userId;
        this.date = parseDate(date);
        this.weight = weight;
    }

    /**
     * Parses a date String into a LocalDate object.
     *
     * @param dateStr The date String in "yyyy-MM-dd" format.
     * @return The LocalDate object.
     * @throws IllegalArgumentException if date parsing fails.
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd.", e);
        }
    }




    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public String getDate() {
        return date.format(DATE_FORMATTER);
    }
    public void setDate(String dateStr) {
        this.date = parseDate(dateStr);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(WeightEntry other) {
        // For descending order (most recent dates first)
        return other.date.compareTo(this.date);
    }
}
