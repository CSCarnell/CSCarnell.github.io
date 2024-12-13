/**
 * Christopher Carnell
 * CS-360
 *
 * This class extends SQLiteOpenHelper and is responsible for creating and updating the database schema.
 * It defines the tables and columns used in the database.
 * It handles the creation of the 'users' and 'weight_entries' tables, and manages database version upgrades.
 */




package com.cs360.weightwatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    //db name and version
    private static final String DATABASE_NAME = "WeightWatcher.db";
    private static final int DATABASE_VERSION = 2;

    //tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ENTRIES = "entries";


    public static final String COLUMN_ID = "_id";

    //users columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_GOAL_WEIGHT = "goal_weight";

    //entries columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_WEIGHT = "weight";

    //create table statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERNAME + " TEXT UNIQUE, "
            + COLUMN_PASSWORD + " TEXT, "
            + COLUMN_PHONE_NUMBER + " TEXT, "
            + COLUMN_GOAL_WEIGHT + " REAL);";

    private static final String CREATE_TABLE_ENTRIES = "CREATE TABLE " + TABLE_ENTRIES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID + " INTEGER, "
            + COLUMN_DATE + " TEXT, "
            + COLUMN_WEIGHT + " REAL, "
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            //add phone_number column to users table
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHONE_NUMBER + " TEXT;");
        }

    }
}
