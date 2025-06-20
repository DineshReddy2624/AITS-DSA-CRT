// PasswordUtil.java
package application;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * !!! WARNING: INSECURE PASSWORD UTIL CLASS !!!
 * This class uses basic SHA-256 hashing without salting or sufficient iterations,
 * making it highly vulnerable to brute-force and rainbow table attacks.
 * It is provided ONLY as a demonstration of "no external package" functionality.
 * DO NOT USE THIS FOR REAL-WORLD PASSWORD STORAGE IN PRODUCTION.
 *
 * For secure password hashing, use a robust library like jbcrypt or PBKDF2 with proper salting and iterations.
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using SHA-256.
     * This method is NOT secure for passwords due to lack of salting and iterations.
     *
     * @param plainPassword The plain text password to hash.
     * @return The Base64 encoded SHA-256 hash of the password, or null if hashing fails.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        try {
            // Get an instance of the SHA-256 message digest algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Compute the hash of the password bytes
            byte[] hashedBytes = md.digest(plainPassword.getBytes());
            // Encode the hashed bytes to a Base64 string for storage
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password (SHA-256 algorithm not available): " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during password hashing: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies a plain text password against a stored hashed password using SHA-256.
     * This method is NOT secure for passwords due to lack of salting and iterations.
     *
     * @param plainPassword The plain text password entered by the user.
     * @param hashedPassword The SHA-256 hashed password retrieved from the database.
     * @return true if the plain password's SHA-256 hash matches the stored hashed password, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        // Re-hash the plain password entered by the user
        String rehashedPlainPassword = hashPassword(plainPassword);
        // Compare the newly generated hash with the stored hash
        // Using MessageDigest, the "hashedPassword" from the database IS just the hash.
        // For secure systems, it would contain both salt and hash, and a more complex verification process.
        return rehashedPlainPassword != null && rehashedPlainPassword.equals(hashedPassword);
    }
}
