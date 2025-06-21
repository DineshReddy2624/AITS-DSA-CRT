package application;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for hashing and verifying passwords.
 * Uses SHA-256 for hashing and Base64 encoding for storage safety.
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using SHA-256 algorithm and Base64 encoding.
     * This makes the password safe for storage as it's not stored in plain text.
     *
     * @param plainPassword The password in plain text.
     * @return The hashed password as a Base64 encoded string, or null if hashing fails.
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Get an instance of the SHA-256 message digest algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Compute the hash of the password bytes
            byte[] hashedBytes = md.digest(plainPassword.getBytes());
            // Encode the hashed bytes to a Base64 string to make it text-safe for storage
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // This exception means SHA-256 algorithm is not available, which is highly unlikely in a standard Java environment
            System.err.println("Error hashing password (SHA-256 not available): " + e.getMessage());
            return null;
        } catch (Exception e) {
            // Catch any other potential exceptions during the hashing process
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifies a plain text password against a stored hashed password.
     * This method hashes the provided plain password and compares it to the stored hash.
     *
     * @param plainPassword The plain text password entered by the user.
     * @param hashedPassword The hashed password retrieved from storage.
     * @return true if the plain password matches the hashed password, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Basic validation for null or empty inputs
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        // Hash the plain password provided by the user and compare it with the stored hash
        String rehashedPlainPassword = hashPassword(plainPassword);
        // Return true if the rehashed password is not null and matches the stored hashed password
        return rehashedPlainPassword != null && rehashedPlainPassword.equals(hashedPassword);
    }
}
