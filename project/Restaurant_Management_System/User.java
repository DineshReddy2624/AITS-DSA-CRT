package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID; // Import for UUID generation

/**
 * Represents a user in the restaurant management system.
 * This class stores user details such as ID, username, hashed password,
 * full name, email, phone number, role, and a list of saved credit cards.
 */
public class User {
    private String userId;       // New: Unique identifier for the user (e.g., UUID)
    private String username;
    private String passwordHash; // Stores the hashed password
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRole role;       // Role of the user (e.g., ADMIN, CUSTOMER)

    // A list to simulate saved credit cards for this user
    // IMPORTANT: In a real application, never store full credit card details directly.
    // Use a secure payment gateway and store only tokenized information.
    // This is for demonstration purposes only.
    private List<CreditCard> savedCards; // New: To store mock saved card details

    /**
     * Full constructor for User, including userId.
     * @param userId A unique identifier for the user (UUID recommended).
     * @param username The user's chosen username.
     * @param passwordHash The hashed password of the user.
     * @param fullName The full name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param role The role of the user (e.g., ADMIN, CUSTOMER).
     */
    public User(String userId, String username, String passwordHash, String fullName, String email, String phoneNumber, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.savedCards = new ArrayList<>(); // Initialize the list
    }

    /**
     * Constructor for User without a specific userId. A new UUID will be generated.
     * This might be used during initial registration before the ID is set by the database.
     * @param username The user's chosen username.
     * @param passwordHash The hashed password of the user.
     * @param fullName The full name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param role The role of the user.
     */
    public User(String username, String passwordHash, String fullName, String email, String phoneNumber, UserRole role) {
        this(UUID.randomUUID().toString(), username, passwordHash, fullName, email, phoneNumber, role);
    }

    // --- Getters ---
    public String getUserId() {
        return userId;
    }

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

    public List<CreditCard> getSavedCards() {
        return new ArrayList<>(savedCards); // Return a copy to prevent external modification
    }

    // --- Setters ---
    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    /**
     * Sets the list of saved credit cards for this user.
     * @param savedCards The list of CreditCard objects.
     */
    public void setSavedCards(List<CreditCard> savedCards) {
        this.savedCards = new ArrayList<>(savedCards); // Defensive copy
    }

    /**
     * Adds a single credit card to the user's saved cards list.
     * @param card The CreditCard object to add.
     */
    public void addSavedCard(CreditCard card) {
        if (this.savedCards == null) {
            this.savedCards = new ArrayList<>();
        }
        this.savedCards.add(card);
    }

    /**
     * Removes a single credit card from the user's saved cards list.
     * @param card The CreditCard object to remove.
     * @return true if the card was removed, false otherwise.
     */
    public boolean removeSavedCard(CreditCard card) {
        if (this.savedCards != null) {
            return this.savedCards.remove(card);
        }
        return false;
    }


    /**
     * Provides a string representation of the User object.
     * Excludes passwordHash for security reasons in toString().
     * @return A formatted string displaying user details.
     */
    @Override
    public String toString() {
        return "User{" +
               "userId='" + userId + '\'' +
               ", username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", role=" + role +
               '}';
    }

    /**
     * Checks if two User objects are equal based on their userId.
     * The userId is considered the primary key for equality.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // userId is the primary key and should be unique, so it's sufficient for equality
        return Objects.equals(userId, user.userId);
    }

    /**
     * Generates a hash code for the User object based on userId.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
