/**
 * Christopher Carnell
 *
 * This class extends SQLiteOpenHelper and is responsible for creating and updating the database schema.
 * It defines the tables and columns used in the database.
 * It handles the creation of the 'users' and 'entries' tables, manages database version upgrades,
 * adds indexes for performance, and enforces data integrity through constraints.
 */

package com.cs360.weightwatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "WeightWatcher.db";
    private static final int DATABASE_VERSION = 3;

    // Tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ENTRIES = "entries";

    // Common column
    public static final String COLUMN_ID = "_id";

    // Users table columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_GOAL_WEIGHT = "goal_weight";

    // Entries table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_WEIGHT = "weight";

    // Create table statements with constraints and indexes
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_PHONE_NUMBER + " TEXT, "
            + COLUMN_GOAL_WEIGHT + " REAL, "
            + "CHECK(" + COLUMN_GOAL_WEIGHT + " IS NULL OR " + COLUMN_GOAL_WEIGHT + " > 0)"
            + ");";


    private static final String CREATE_TABLE_ENTRIES = "CREATE TABLE " + TABLE_ENTRIES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_DATE + " TEXT NOT NULL, "
            + COLUMN_WEIGHT + " REAL NOT NULL CHECK(" + COLUMN_WEIGHT + " > 0), "
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE ON UPDATE CASCADE"
            + ");";

    // Index creation statements
    private static final String CREATE_INDEX_USER_ID = "CREATE INDEX idx_user_id ON "
            + TABLE_ENTRIES + "(" + COLUMN_USER_ID + ");";

    private static final String CREATE_INDEX_ENTRY_DATE = "CREATE INDEX idx_entry_date ON "
            + TABLE_ENTRIES + "(" + COLUMN_DATE + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ENTRIES);

        // Create indexes
        db.execSQL(CREATE_INDEX_USER_ID);
        db.execSQL(CREATE_INDEX_ENTRY_DATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema upgrades
        if (oldVersion < 2) {
            // Version 2 upgrade logic
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHONE_NUMBER + " TEXT;");
        }
        if (oldVersion < 3) {
            // Version 3 upgrade logic
            // Since we added constraints and indexes, it's simpler to recreate the tables
            db.execSQL("PRAGMA foreign_keys=OFF;");

            // Rename existing tables
            db.execSQL("ALTER TABLE " + TABLE_USERS + " RENAME TO temp_" + TABLE_USERS + ";");
            db.execSQL("ALTER TABLE " + TABLE_ENTRIES + " RENAME TO temp_" + TABLE_ENTRIES + ";");

            // Create new tables
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_ENTRIES);

            // Copy data from temp tables to new tables
            db.execSQL("INSERT INTO " + TABLE_USERS + " (" + COLUMN_ID + ", " + COLUMN_USERNAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_GOAL_WEIGHT + ") "
                    + "SELECT " + COLUMN_ID + ", " + COLUMN_USERNAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_GOAL_WEIGHT + " FROM temp_" + TABLE_USERS + ";");

            db.execSQL("INSERT INTO " + TABLE_ENTRIES + " (" + COLUMN_ID + ", " + COLUMN_USER_ID + ", " + COLUMN_DATE + ", " + COLUMN_WEIGHT + ") "
                    + "SELECT " + COLUMN_ID + ", " + COLUMN_USER_ID + ", " + COLUMN_DATE + ", " + COLUMN_WEIGHT + " FROM temp_" + TABLE_ENTRIES + ";");

            // Drop temp tables
            db.execSQL("DROP TABLE temp_" + TABLE_USERS + ";");
            db.execSQL("DROP TABLE temp_" + TABLE_ENTRIES + ";");

            // Create indexes
            db.execSQL(CREATE_INDEX_USER_ID);
            db.execSQL(CREATE_INDEX_ENTRY_DATE);

            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
