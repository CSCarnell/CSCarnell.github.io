/**
 * Christopher Carnell
 * CS-360
 *
 * This activity handles all the SMS permission functionality
 */

package com.cs360.weightwatcher;

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

    private Button buttonAllowSms;
    private Button buttonDenySms;
    private TextView textViewSmsPrompt;
    private EditText editTextPhoneNumber;

    private TextView textViewSmsDescription;

    private long userId;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        //initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        dbManager.open();
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        //get user ID from intent
        userId = getIntent().getLongExtra("user_id", -1);

        if (userId == -1) {
            //invalid user ID, redirect to login
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //initialize UI components
        buttonAllowSms = findViewById(R.id.buttonAllowSms);
        buttonDenySms = findViewById(R.id.buttonDenySms);
        textViewSmsPrompt = findViewById(R.id.textViewSmsPrompt);
        textViewSmsDescription = findViewById(R.id.textViewSmsDescription);

        buttonAllowSms.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            //save phone number to database
            int result = dbManager.updateUserPhoneNumber(userId, phoneNumber);
            if (result > 0) {
                PreferenceUtils.setSmsOptIn(this, userId, true);

                //mark SMS setup as completed
                PreferenceUtils.setSmsSetupCompleted(this, userId, true);

                checkSmsPermission();
            } else {
                Toast.makeText(this, "Failed to save phone number", Toast.LENGTH_SHORT).show();
            }
        });


        buttonDenySms.setOnClickListener(v -> {
            //user does not want SMS notifications
            PreferenceUtils.setSmsOptIn(this, userId, false);

            //mark SMS setup as completed
            PreferenceUtils.setSmsSetupCompleted(this, userId, true);

            proceedToDataDisplay();
        });

    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS_PERMISSION);
        } else {
            //permission already granted
            proceedToDataDisplay();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            //proceed regardless of the permission result
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
