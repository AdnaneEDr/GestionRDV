package com.example.gestionrdv.utils;

import android.util.Base64;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password Utility class for secure password hashing and verification.
 * Uses PBKDF2 with HMAC-SHA256 algorithm.
 */
public class PasswordUtils {

    private static final String TAG = "PasswordUtils";

    // Algorithm configuration
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    // Separator for storing salt and hash together
    private static final String SEPARATOR = ":";

    /**
     * Hashes a password using PBKDF2 with a random salt.
     *
     * @param password The plain-text password to hash
     * @return A string containing the salt and hash separated by ":"
     *         Format: "base64Salt:base64Hash"
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash the password
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // Encode salt and hash to Base64 and combine
            String saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP);
            String hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP);

            return saltBase64 + SEPARATOR + hashBase64;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Error hashing password: " + e.getMessage());
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password The plain-text password to verify
     * @param storedHash The stored hash string (format: "salt:hash")
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        try {
            // Split the stored hash into salt and hash parts
            String[] parts = storedHash.split(SEPARATOR);
            if (parts.length != 2) {
                Log.e(TAG, "Invalid stored hash format");
                return false;
            }

            // Decode salt and hash from Base64
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);

            // Hash the input password with the same salt
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // Compare hashes using constant-time comparison
            return constantTimeEquals(expectedHash, actualHash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Error verifying password: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 encoding in stored hash: " + e.getMessage());
            return false;
        }
    }

    /**
     * Performs PBKDF2 key derivation.
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Constant-time byte array comparison to prevent timing attacks.
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * Checks if a stored value appears to be a hashed password.
     * Used for backward compatibility during migration.
     *
     * @param storedValue The value to check
     * @return true if it appears to be a hashed password
     */
    public static boolean isHashed(String storedValue) {
        if (storedValue == null || storedValue.isEmpty()) {
            return false;
        }

        // Hashed passwords contain a separator and have specific format
        String[] parts = storedValue.split(SEPARATOR);
        if (parts.length != 2) {
            return false;
        }

        // Check if both parts are valid Base64 and have expected lengths
        try {
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] hash = Base64.decode(parts[1], Base64.NO_WRAP);
            return salt.length == SALT_LENGTH && hash.length == KEY_LENGTH / 8;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
