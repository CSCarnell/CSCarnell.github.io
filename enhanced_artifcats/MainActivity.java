/**
 * Christopher Carnell
 *
 * This is the main activity of the application, handling user authentication.
 * It allows users to log in or register for an account.
 * During registration, users can opt in for SMS notifications and provide their phone number.
 * It also handles permission requests for sending SMS messages if the user opts in.
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
        buttonLogin.setOnClickListener(v -> handleLogin());

        // Handle Register button click
        buttonRegister.setOnClickListener(v -> handleRegistration());
    }

    /**
     * Handles the login process by validating inputs, hashing the password,
     * and attempting to log in the user.
     */
    private void handleLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        if (!validateInputs(username, password)) {
            return;
        }

        String hashedPassword = SecurityUtils.hashPassword(password);

        try {
            User user = dbManager.loginUser(username, hashedPassword);
            if (user != null) {
                proceedAfterLogin(user);
            } else {
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "An error occurred during login", Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // Consider using proper logging in production
        }
    }

    /**
     * Handles the registration process by validating inputs, hashing the password,
     * and attempting to register the user.
     */
    private void handleRegistration() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        if (!validateInputs(username, password)) {
            return;
        }

        String hashedPassword = SecurityUtils.hashPassword(password);

        try {
            long userId = dbManager.registerUser(username, hashedPassword);
            if (userId != -1) {
                Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                // Automatically log in the user after successful registration
                User user = new User(userId, username, hashedPassword, null, 0.0);
                proceedAfterLogin(user);
            } else {
                Toast.makeText(MainActivity.this, "Registration failed: Username may already exist", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "An error occurred during registration", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Proceeds to the next activity after a successful login or registration.
     *
     * @param user The logged-in user.
     */
    private void proceedAfterLogin(User user) {
        boolean isSmsSetupCompleted = PreferenceUtils.isSmsSetupCompleted(this, user.getId());

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
    }

    /**
     * Validates the username and password inputs.
     *
     * @param username The entered username.
     * @param password The entered password.
     * @return True if inputs are valid, false otherwise.
     */
    private boolean validateInputs(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }
}
