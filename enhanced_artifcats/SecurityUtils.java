/**
 * Christopher Carnell
 *
 * This utility class provides security-related functions for the application.
 * It includes methods for hashing passwords using SHA-256 before storing them in the database.
 * It ensures that user passwords are stored securely.
 */


package com.cs360.weightwatcher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {
    //hash the password before storing in DB for security
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            //convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            //exceptions
            e.printStackTrace();
            return null;
        }
    }
}
