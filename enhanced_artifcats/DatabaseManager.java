/**
 * Christopher Carnell
 *
 * This class manages all database operations for the application.
 * It provides methods to open and close the database connection.
 * It includes methods to register users, log in users, and perform CRUD operations on weight entries and user data.
 * It interacts with the SQLite database through the DatabaseHelper class.
 */


package com.cs360.weightwatcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages database operations for the WeightWatcher application.
 * Handles user registration, login, and CRUD operations for weight entries.
 */
public class DatabaseManager {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    // Cache for goal weights to reduce database calls
    private final Map<Long, Double> goalWeightCache;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        goalWeightCache = new HashMap<>();
    }

    /**
     * Opens the database for writing.
     */
    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Closes the database.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Registers a new user in the database.
     *
     * @param username       The user's username.
     * @param hashedPassword The user's hashed password.
     * @return The new user's ID or -1 if an error occurred.
     */
    public long registerUser(String username, String hashedPassword) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, "");
        values.putNull(DatabaseHelper.COLUMN_GOAL_WEIGHT);

        long userId = -1;
        try {
            userId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred in register user.", e);
        }
        return userId;
    }


    /**
     * Logs in a user by verifying the username and hashed password.
     *
     * @param username       The user's username.
     * @param hashedPassword The user's hashed password.
     * @return A User object if login is successful, null otherwise.
     */
    public User loginUser(String username, String hashedPassword) {
        User user = null;
        String[] columns = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_PHONE_NUMBER,
                DatabaseHelper.COLUMN_GOAL_WEIGHT
        };
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, hashedPassword};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                user = new User(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_NUMBER)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_WEIGHT))
                );
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred in user login.", e);
        }
        return user;
    }

    /**
     * Updates the user's goal weight in the database.
     *
     * @param userId     The user's ID.
     * @param goalWeight The new goal weight.
     * @return The number of rows affected.
     */
    public int updateGoalWeight(long userId, double goalWeight) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_GOAL_WEIGHT, goalWeight);

        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);
            // Update the cache
            goalWeightCache.put(userId, goalWeight);
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred in update goal weight.", e);
        }
        return rowsAffected;
    }

    /**
     * Retrieves the user's goal weight from the database.
     *
     * @param userId The user's ID.
     * @return The goal weight or -1 if not set or an error occurs.
     */
    public double getGoalWeight(long userId) {
        // Check cache first
        if (goalWeightCache.containsKey(userId)) {
            return goalWeightCache.get(userId);
        }

        double goalWeight = -1;
        String[] columns = {DatabaseHelper.COLUMN_GOAL_WEIGHT};
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                goalWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_WEIGHT));
                // Update cache
                goalWeightCache.put(userId, goalWeight);
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred in get goal weight.", e);
            goalWeight = -1;
        }
        return goalWeight;
    }

    /**
     * Adds a new weight entry for the user.
     *
     * @param userId The user's ID.
     * @param date   The date of the entry.
     * @param weight The weight value.
     * @return The new entry's ID or -1 if an error occurred.
     */
    public long addWeightEntry(long userId, String date, double weight) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_WEIGHT, weight);

        long entryId = -1;
        try {
            entryId = db.insert(DatabaseHelper.TABLE_ENTRIES, null, values);
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred in add weight entry.", e);
        }
        return entryId;
    }

    /**
     * Retrieves all weight entries for the user.
     *
     * @param userId The user's ID.
     * @return A list of WeightEntry objects.
     */
    public List<WeightEntry> getWeightEntries(long userId) {
        List<WeightEntry> entries = new ArrayList<>();
        String[] columns = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_DATE,
                DatabaseHelper.COLUMN_WEIGHT
        };
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = DatabaseHelper.COLUMN_DATE + " DESC";

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ENTRIES, columns, selection, selectionArgs, null, null, orderBy)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    WeightEntry entry = new WeightEntry(
                            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHT))
                    );
                    entries.add(entry);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred get weight entries.", e);
            entries = new ArrayList<>();
        }
        return entries;
    }

    /**
     * Deletes a weight entry from the database.
     *
     * @param entryId The entry's ID.
     * @return The number of rows affected.
     */
    public int deleteWeightEntry(long entryId) {
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(entryId)};

        int rowsDeleted = 0;
        try {
            rowsDeleted = db.delete(DatabaseHelper.TABLE_ENTRIES, selection, selectionArgs);
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred in delete weight entry.", e);
        }
        return rowsDeleted;
    }

    /**
     * Updates the user's phone number in the database.
     *
     * @param userId      The user's ID.
     * @param phoneNumber The new phone number.
     * @return The number of rows affected.
     */
    public int updateUserPhoneNumber(long userId, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);

        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred update user phone number.", e);
        }
        return rowsAffected;
    }

    /**
     * Retrieves the user's phone number from the database.
     *
     * @param userId The user's ID.
     * @return The phone number or null if not set or an error occurs.
     */
    public String getUserPhoneNumber(long userId) {
        String phoneNumber = null;
        String[] columns = {DatabaseHelper.COLUMN_PHONE_NUMBER};
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_NUMBER));
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Unexpected error occurred get user phone numbers.", e);
        }
        return phoneNumber;
    }
}

