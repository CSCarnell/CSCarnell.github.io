/**
 * Christopher Carnell
 *
 * This activity handles all the SMS permission functionality and validates the phone number format.
 */

package com.cs360.weightwatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 100;

    private EditText editTextPhoneNumber;

    private long userId;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        dbManager.open();
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

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
        Button buttonAllowSms = findViewById(R.id.buttonAllowSms);
        Button buttonDenySms = findViewById(R.id.buttonDenySms);
        TextView textViewSmsPrompt = findViewById(R.id.textViewSmsPrompt);
        TextView textViewSmsDescription = findViewById(R.id.textViewSmsDescription);

        buttonAllowSms.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate the phone number format
            if (!isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(this, "Invalid phone number format. Please enter a valid phone number.", Toast.LENGTH_LONG).show();
                return;
            }

            // Save phone number to database
            int result = dbManager.updateUserPhoneNumber(userId, phoneNumber);
            if (result > 0) {
                PreferenceUtils.setSmsOptIn(this, userId, true);

                // Mark SMS setup as completed
                PreferenceUtils.setSmsSetupCompleted(this, userId, true);

                checkSmsPermission();
            } else {
                Toast.makeText(this, "Failed to save phone number", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDenySms.setOnClickListener(v -> {
            // User does not want SMS notifications
            PreferenceUtils.setSmsOptIn(this, userId, false);

            // Mark SMS setup as completed
            PreferenceUtils.setSmsSetupCompleted(this, userId, true);

            proceedToDataDisplay();
        });

    }

    /**
     * Validates the phone number format.
     *
     * @param phoneNumber The phone number input by the user.
     * @return True if the phone number is valid, false otherwise.
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Define a regex pattern for valid phone numbers
        String PHONE_NUMBER_PATTERN = "^[+]?[0-9]{10,13}$";
        return phoneNumber != null && phoneNumber.matches(PHONE_NUMBER_PATTERN);
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS_PERMISSION);
        } else {
            // Permission already granted
            proceedToDataDisplay();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            // Proceed regardless of the permission result
            proceedToDataDisplay();
        }
    }

    private void proceedToDataDisplay() {
        Intent intent = new Intent(SmsPermissionActivity.this, DataDisplayActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }
}
