package application;

/**
 * Utility class for securely hashing and verifying passwords using BCrypt.
 * You need to add the jbcrypt library to your project's dependencies.
 * For Maven:
 * <dependency>
 * <groupId>org.mindrot</groupId>
 * <artifactId>jbcrypt</artifactId>
 * <version>0.4</version>
 * </dependency>
 *
 * For Gradle:
 * implementation 'org.mindrot:jbcrypt:0.4'
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param plainPassword The password in plain text.
     * @return The hashed password, or null if an error occurs.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        try {
            // Generate a salt and hash the password with a work factor of 12 (default is 10)
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifies a plain text password against a hashed password.
     *
     * @param plainPassword The plain text password entered by the user.
     * @param hashedPassword The hashed password retrieved from the database.
     * @return true if the plain password matches the hashed password, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
}
