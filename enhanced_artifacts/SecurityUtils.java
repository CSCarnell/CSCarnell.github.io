/**
 * Christopher Carnell
 *
 * Provides security-related functions for the application, such as password hashing with salting.
 * This class uses PBKDF2WithHmacSHA256 to hash passwords securely.
 * 
 */


package com.cs360.weightwatcher;
import android.util.Log;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class SecurityUtils {

    private static final String TAG = "SecurityUtils";
    
    /**
     * Generates a unique salt for each user, ensuring that the same password
     * produces different hashes.
     *
     * @return A newly generated 16-byte salt.
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password using the PBKDF2WithHmacSHA256 algorithm, combined with a unique salt.
     * This method uses 10,000 iterations and a 256-bit derived key length, which is considered secure.
     *
     * @param password The plain-text password to be hashed.
     * @param salt The salt associated with this user/password, provided as a 16-byte array.
     * @return A Base64-encoded hash of the password, or null if an error occurs during hashing.
     * @throws IllegalArgumentException if the password is null or empty, or if salt is null/empty.
     */
    public static String hashPassword(String password, byte[] salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }

        try {
            int iterations = 10000;
            int keyLength = 256; // bits
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Log the exception using Android's logging system instead of printing stack trace.
            Log.e(TAG, "Failed to hash password", e);
            return null;
        }
    }

    /**
     * @param candidatePassword The plain-text password entered by the user at login.
     * @param storedHash The Base64-encoded hashed password from the database.
     * @param storedSalt The salt used when hashing the password, retrieved from the database.
     * @return true if the candidatePassword hashes to the storedHash, false otherwise.
     */
    public static boolean verifyPassword(String candidatePassword, String storedHash, byte[] storedSalt) {
        if (candidatePassword == null || candidatePassword.isEmpty()) {
            return false;
        }
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (storedSalt == null || storedSalt.length == 0) {
            return false;
        }

        String candidateHash = hashPassword(candidatePassword, storedSalt);
        return candidateHash != null && candidateHash.equals(storedHash);
    }
}

