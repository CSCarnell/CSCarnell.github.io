/**
 * Christopher Carnell
 * CS-360
 *
 * This is the main activity of the application, handling user authentication.
 * It allows users to log in or register for an account.
 * During registration, users can opt-in for SMS notifications and provide their phone number.
 * It also handles permission requests for sending SMS messages if the user opts in.
 *
 */

package com.cs360.weightwatcher;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;

    private Button buttonLogin, buttonRegister;
    private DatabaseManager dbManager;

    private static final int REQUEST_SMS_PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        dbManager.open();

        // Handle Login button click
        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            //hash password before comparing
            String hashedPassword = SecurityUtils.hashPassword(password);

            User user = dbManager.loginUser(username, hashedPassword);
            if (user != null) {
                //successful login
                boolean isSmsSetupCompleted = PreferenceUtils.isSmsSetupCompleted(this, user.getId());

                // check if SMS permission has been given or not to prevent asking the user on every login
                if (isSmsSetupCompleted) {
                    // SMS setup already completed, proceed to DataDisplayActivity
                    Intent intent = new Intent(MainActivity.this, DataDisplayActivity.class);
                    intent.putExtra("user_id", user.getId());
                    startActivity(intent);
                    finish();
                } else {
                    // SMS setup not completed, redirect to SmsPermissionActivity
                    Intent intent = new Intent(MainActivity.this, SmsPermissionActivity.class);
                    intent.putExtra("user_id", user.getId());
                    startActivity(intent);
                    finish();
                }
            } else {
                // Login failed
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
        // handle register button click
        buttonRegister.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            //hash password before storing
            String hashedPassword = SecurityUtils.hashPassword(password);

            long result = dbManager.registerUser(username, hashedPassword);
            if (result != -1) {
                Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                //automatically log in the user, not working as expected currently
                User user = dbManager.loginUser(username, hashedPassword);
                if (user != null) {
                    //since it's a new user, SMS setup is not completed
                    Intent intent = new Intent(MainActivity.this, SmsPermissionActivity.class);
                    intent.putExtra("user_id", user.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Auto-login failed. Please try logging in.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Registration failed: Username may already exist", Toast.LENGTH_SHORT).show();
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }
}
