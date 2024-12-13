/**
 * Christopher Carnell
 * CS-360
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

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    //db register new user
    public long registerUser(String username, String hashedPassword) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, ""); //
        values.put(DatabaseHelper.COLUMN_GOAL_WEIGHT, -1); //

        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    //db login
    public User loginUser(String username, String hashedPassword) {
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { username, hashedPassword };

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_NUMBER)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_WEIGHT))
            );
            cursor.close();
            return user;
        }
        return null;
    }

    //update the goal weight in the DB
    public int updateGoalWeight(long userId, double goalWeight) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_GOAL_WEIGHT, goalWeight);

        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        return db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);
    }
    //get goal weight from the DB
    public double getGoalWeight(long userId) {
        String[] columns = { DatabaseHelper.COLUMN_GOAL_WEIGHT };
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            double goalWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_WEIGHT));
            cursor.close();
            return goalWeight;
        }
        return -1;
    }

    //add a entry to the DB
    public long addWeightEntry(long userId, String date, double weight) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_WEIGHT, weight);

        return db.insert(DatabaseHelper.TABLE_ENTRIES, null, values);
    }
    //get the all weight entries from the DB
    public List<WeightEntry> getWeightEntries(long userId) {
        List<WeightEntry> entries = new ArrayList<>();

        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        Cursor cursor = db.query(DatabaseHelper.TABLE_ENTRIES, null, selection, selectionArgs, null, null, DatabaseHelper.COLUMN_DATE + " DESC");

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
            cursor.close();
        }
        return entries;
    }
    //remove a weight entry from the DB
    public int deleteWeightEntry(long entryId) {
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(entryId) };
        return db.delete(DatabaseHelper.TABLE_ENTRIES, selection, selectionArgs);
    }
    //update the phone number in the DB
    public int updateUserPhoneNumber(long userId, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);

        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(userId) };

        return db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
    }
    //get the user phone number from the DB
    public String getUserPhoneNumber(long userId) {
        String[] columns = { DatabaseHelper.COLUMN_PHONE_NUMBER };
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_NUMBER));
            cursor.close();
            return phoneNumber;
        }
        return null;
    }

}
