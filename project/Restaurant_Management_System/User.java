package application;

/**
 * Represents a user in the system (e.g., a customer who registers).
 * Updated to include full name, email, and phone number for a more complete profile.
 */
public class User {
    private String username;
    private String passwordHash; // Stores the hashed password
    private String fullName;     // New: Full name of the user
    private String email;        // New: Email address of the user
    private String phoneNumber;  // New: Phone number of the user

    /**
     * Constructor for a User with full profile details.
     * @param username The unique username for the user.
     * @param passwordHash The hashed password for the user.
     * @param fullName The full name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     */
    public User(String username, String passwordHash, String fullName, String email, String phoneNumber) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Constructor for loading a User from the database without password hash.
     * This is useful when fetching user details for display, where the hash is not needed.
     * @param username The unique username.
     * @param fullName The full name.
     * @param email The email address.
     * @param phoneNumber The phone number.
     */
    public User(String username, String fullName, String email, String phoneNumber) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = null; // Password hash is not loaded with this constructor
    }


    // Getters
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() { // New Getter
        return fullName;
    }

    public String getEmail() { // New Getter
        return email;
    }

    public String getPhoneNumber() { // New Getter
        return phoneNumber;
    }

    // Setters (if needed for updates)
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) { // New Setter
        this.fullName = fullName;
    }

    public void setEmail(String email) { // New Setter
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) { // New Setter
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               '}';
    }
}
