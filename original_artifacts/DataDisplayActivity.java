/**
 * Christopher Carnell
 * CS-360
 *
 * This activity displays the user's weight entries and goal weight.
 * Users can add new weight entries, view their weight history, and set or change their goal weight.
 * It checks for goal achievement and sends an SMS notification if the user has opted in and permissions are granted.
 * It also handles SMS permissions required for sending notifications.
 *
 * TODO: Add a logout button
 * TODO: Add a visualization graph to show the trends
 */


package com.cs360.weightwatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.util.List;
import java.util.ArrayList;

public class DataDisplayActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 100;

    private RecyclerView recyclerViewData;
    private Button buttonAddEntry;

    // Goal Weight UI Components
    private TextView textViewGoalWeight;
    private ImageButton buttonChangeGoal;

    // Data list for RecyclerView
    private ArrayList<WeightEntry> weightEntries;
    private DataAdapter dataAdapter;

    private DatabaseManager dbManager;
    private long userId;


    private boolean smsPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        //init DatabaseManager
        dbManager = new DatabaseManager(this);
        dbManager.open();

        //get user ID from intent
        userId = getIntent().getLongExtra("user_id", -1);

        if (userId == -1) {
            // invalid user ID, redirect to login
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //init UI
        recyclerViewData = findViewById(R.id.recyclerViewData);
        buttonAddEntry = findViewById(R.id.buttonAddEntry);
        textViewGoalWeight = findViewById(R.id.textViewGoalWeight);
        buttonChangeGoal = findViewById(R.id.buttonChangeGoal);

        //init data list
        weightEntries = new ArrayList<>();

        //set layout manager
        recyclerViewData.setLayoutManager(new LinearLayoutManager(this));

        //init adapter with dbManager
        dataAdapter = new DataAdapter(weightEntries, dbManager);
        recyclerViewData.setAdapter(dataAdapter);

        //add Entry button click
        buttonAddEntry.setOnClickListener(view -> showAddEntryDialog());

        //change Goal button click
        buttonChangeGoal.setOnClickListener(view -> showSetGoalDialog());

        //check and display goal weight
        checkAndDisplayGoalWeight();

        //load weight entries
        loadWeightEntries();


    }


    private void checkAndDisplayGoalWeight() {
        double goalWeight = dbManager.getGoalWeight(userId);
        if (goalWeight == -1) {
            //goal weight not set, prompt the user
            showSetGoalDialog();
        } else {
            //goal weight is set, display it
            textViewGoalWeight.setText(String.valueOf(goalWeight));
        }
    }

    private void showSetGoalDialog() {
        SetGoalDialog setGoalDialog = new SetGoalDialog();
        setGoalDialog.setOnGoalSetListener(goalWeight -> {
            //save the goal weight to the database
            int result = dbManager.updateGoalWeight(userId, goalWeight);
            if (result > 0) {
                //update the displayed goal weight
                textViewGoalWeight.setText(String.valueOf(goalWeight));
                Toast.makeText(DataDisplayActivity.this, "Goal weight set to " + goalWeight, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DataDisplayActivity.this, "Failed to set goal weight", Toast.LENGTH_SHORT).show();
            }
        });
        setGoalDialog.show(getSupportFragmentManager(), "SetGoalDialog");
    }

    private void showAddEntryDialog() {
        AddEntryDialog addEntryDialog = new AddEntryDialog();
        addEntryDialog.setUserId(userId);
        addEntryDialog.setOnEntryAddedListener(weightEntry -> {
            long entryId = dbManager.addWeightEntry(weightEntry.getUserId(), weightEntry.getDate(), weightEntry.getWeight());

            if (entryId != -1) {
                weightEntry.setId(entryId);
                weightEntries.add(0, weightEntry);
                dataAdapter.notifyDataSetChanged();

                //check if goal weight is reached
                checkGoalAchievement(weightEntry.getWeight());
            } else {
                Toast.makeText(DataDisplayActivity.this, "Failed to add weight entry", Toast.LENGTH_SHORT).show();
            }
        });
        addEntryDialog.show(getSupportFragmentManager(), "AddEntryDialog");
    }

    private void checkGoalAchievement(double currentWeight) {
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
    }


    private void loadWeightEntries() {
        List<WeightEntry> entries = dbManager.getWeightEntries(userId);
        weightEntries.clear();
        weightEntries.addAll(entries);
        dataAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }

    private void sendSmsNotification() {
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

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS notification sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

            // Log successful SMS send
            Log.d("SMS", "SMS sent successfully to " + phoneNumber);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS notification", Toast.LENGTH_SHORT).show();

            // Log the exception
            Log.e("SMS", "Failed to send SMS", e);
        }
    }
}
