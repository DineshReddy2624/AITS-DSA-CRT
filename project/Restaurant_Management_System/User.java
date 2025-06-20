// User.java
package application;

/**
 * Represents a user in the system (e.g., a customer who registers).
 * Updated to include full name, email, phone number, and a role for a more complete profile.
 */
public class User {
    private String username;
    private String passwordHash; // Stores the hashed password (can be null if not loaded)
    private String fullName;     // Full name of the user
    private String email;        // Email address of the user
    private String phoneNumber;  // Phone number of the user
    private UserRole role;       // New: Role of the user (e.g., Admin, Customer)

    /**
     * Constructor for a User with full profile details including password hash.
     * Used typically during registration or when retrieving a user for authentication.
     * @param username The unique username for the user.
     * @param passwordHash The hashed password for the user.
     * @param fullName The full name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param role The role of the user.
     */
    public User(String username, String passwordHash, String fullName, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    /**
     * Constructor for loading a User from the database without the password hash.
     * This is useful when fetching user details for display or other operations
     * where the hash is not needed, improving security by not exposing it unnecessarily.
     * @param username The unique username for the user.
     * @param fullName The full name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param role The role of the user.
     */
    public User(String username, String fullName, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.passwordHash = null; // Password hash is not loaded with this constructor
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    // Setters (if needed for updates)
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", role=" + role +
               '}';
    }
}
