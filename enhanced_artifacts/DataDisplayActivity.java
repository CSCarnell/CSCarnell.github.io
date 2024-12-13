/**
 * Christopher Carnell
 *
 *
 * This activity displays the user's weight entries and goal weight.
 * Users can add new weight entries, view their weight history, and set or change their goal weight.
 * It checks for goal achievement and sends an SMS notification if the user has opted in and permissions are granted.
 * It also handles SMS permissions required for sending notifications.
 */

package com.cs360.weightwatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class DataDisplayActivity extends AppCompatActivity {

    // Goal Weight UI Components
    private TextView textViewGoalWeight;

    // Data list for RecyclerView
    private List<WeightEntry> weightEntries;
    private DataAdapter dataAdapter;

    private DatabaseManager dbManager;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        dbManager.open();

        // Get user ID from intent
        userId = getIntent().getLongExtra("user_id", -1);

        if (userId == -1) {
            // Invalid user ID, redirect to login
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI components
        RecyclerView recyclerViewData = findViewById(R.id.recyclerViewData);
        Button buttonAddEntry = findViewById(R.id.buttonAddEntry);
        textViewGoalWeight = findViewById(R.id.textViewGoalWeight);
        ImageButton buttonChangeGoal = findViewById(R.id.buttonChangeGoal);

        // Initialize data list
        weightEntries = new ArrayList<>();

        // Set layout manager
        recyclerViewData.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with weightEntries
        dataAdapter = new DataAdapter(weightEntries, dbManager);
        recyclerViewData.setAdapter(dataAdapter);

        // Add Entry button click
        buttonAddEntry.setOnClickListener(view -> showAddEntryDialog());

        // Change Goal button click
        buttonChangeGoal.setOnClickListener(view -> showSetGoalDialog());

        // Check and display goal weight
        checkAndDisplayGoalWeight();

        // Load weight entries
        loadWeightEntries();
    }

    /**
     * Checks if the goal weight is set and displays it.
     * If not set, prompts the user to set it.
     */
    private void checkAndDisplayGoalWeight() {
        try {
            Double goalWeight = dbManager.getGoalWeight(userId);
            if (goalWeight <= 0) {
                // Goal weight not set, prompt the user
                showSetGoalDialog();
            } else {
                // Goal weight is set, display it
                textViewGoalWeight.setText(String.valueOf(goalWeight));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to retrieve goal weight", Toast.LENGTH_SHORT).show();
            Log.e("DataDisplayActivity", "Error retrieving goal weight for user ID: " + userId, e);
        }
    }

    /**
     * Displays a dialog to set or change the goal weight.
     */
    private void showSetGoalDialog() {
        SetGoalDialog setGoalDialog = new SetGoalDialog();
        setGoalDialog.setOnGoalSetListener(goalWeight -> {
            try {
                // Save the goal weight to the database
                int result = dbManager.updateGoalWeight(userId, goalWeight);
                if (result > 0) {
                    // Update the displayed goal weight
                    textViewGoalWeight.setText(String.valueOf(goalWeight));
                    Toast.makeText(DataDisplayActivity.this, "Goal weight set to " + goalWeight, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DataDisplayActivity.this, "Failed to set goal weight", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(DataDisplayActivity.this, "An error occurred while setting goal weight", Toast.LENGTH_SHORT).show();
                Log.e("DataDisplayActivity", "Error setting goal weight for user ID: " + userId, e);
            }
        });
        setGoalDialog.show(getSupportFragmentManager(), "SetGoalDialog");
    }

    /**
     * Displays a dialog to add a new weight entry.
     */
    private void showAddEntryDialog() {
        AddEntryDialog addEntryDialog = new AddEntryDialog();
        addEntryDialog.setUserId(userId);
        addEntryDialog.setOnEntryAddedListener(weightEntry -> {
            try {
                long entryId = dbManager.addWeightEntry(weightEntry.getUserId(), weightEntry.getDate(), weightEntry.getWeight());

                if (entryId != -1) {
                    weightEntry.setId(entryId);

                    // Insert the new entry in sorted order
                    int insertPosition = findInsertPosition(weightEntry);
                    weightEntries.add(insertPosition, weightEntry);
                    dataAdapter.notifyItemInserted(insertPosition);

                    // Check if goal weight is reached
                    checkGoalAchievement(weightEntry.getWeight());
                } else {
                    Toast.makeText(DataDisplayActivity.this, "Failed to add weight entry", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(DataDisplayActivity.this, "An error occurred while adding weight entry", Toast.LENGTH_SHORT).show();
                Log.e("DataDisplayActivity", "Error adding weight entry for user ID: " + userId, e);
            }
        });
        addEntryDialog.show(getSupportFragmentManager(), "AddEntryDialog");
    }

    /**
     * Finds the correct position to insert the new weight entry to keep the list sorted.
     *
     * @param newEntry The new weight entry to insert.
     * @return The index at which to insert the new entry.
     */
    private int findInsertPosition(WeightEntry newEntry) {
        for (int i = 0; i < weightEntries.size(); i++) {
            if (newEntry.compareTo(weightEntries.get(i)) > 0) {
                return i;
            }
        }
        return weightEntries.size();
    }

    /**
     * Checks if the user has achieved their goal weight and sends an SMS notification if they have opted in.
     *
     * @param currentWeight The user's current weight.
     */
    private void checkGoalAchievement(double currentWeight) {
        try {
            double goalWeight = dbManager.getGoalWeight(userId);
            if (goalWeight != -1 && currentWeight <= goalWeight) {
                // User has reached or passed the goal weight
                Toast.makeText(this, "Congratulations! You've reached your goal weight!", Toast.LENGTH_LONG).show();

                // Check if user has opted in for SMS notifications
                boolean smsOptIn = PreferenceUtils.isSmsOptIn(this, userId);

                if (smsOptIn) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                            == PackageManager.PERMISSION_GRANTED) {
                        sendSmsNotification();
                    } else {
                        // SMS permission not granted
                        Toast.makeText(this, "SMS permission not granted. Unable to send notification.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred while checking goal achievement", Toast.LENGTH_SHORT).show();
            Log.e("DataDisplayActivity", "Error checking goal weight achievement for user ID: " + userId, e);
        }
    }

    /**
     * Loads the user's weight entries from the database and updates the RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadWeightEntries() {
        try {
            List<WeightEntry> entries = dbManager.getWeightEntries(userId);
            weightEntries.clear();
            weightEntries.addAll(entries);

            // Sort the entries
            weightEntries.sort(Collections.reverseOrder());

            dataAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load weight entries", Toast.LENGTH_SHORT).show();
            Log.e("DataDisplayActivity", "Error loading weight entries for user ID: " + userId, e);
        }
    }

    /**
     * Sends an SMS notification to the user upon reaching the goal weight.
     */
    private void sendSmsNotification() {
        try {
            // Get user's phone number from the database
            String phoneNumber = dbManager.getUserPhoneNumber(userId);
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
                Log.e("SMS", "Phone number is null or empty.");
                return;
            }

            String message = "Congratulations! You've reached your goal weight! Keep up the great work!";

            // Log the attempt to send SMS
            Log.d("SMS", "Attempting to send SMS to " + phoneNumber);

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS notification sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

            // Log successful SMS send
            Log.d("SMS", "SMS sent successfully to " + phoneNumber);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS notification", Toast.LENGTH_SHORT).show();
            Log.e("SMS", "Failed to send SMS", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }
}

