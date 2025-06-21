package application;

import java.time.LocalDateTime;
import java.util.Objects; // For Objects.hash and Objects.equals

/**
 * Represents a customer's rating for a specific menu item (dish).
 * Includes rating ID, ID of the menu item, customer who rated, the rating value, and rating date.
 */
public class DishRating {
    private int ratingId;          // Unique identifier for the dish rating
    private int menuItemId;        // ID of the MenuItem that was rated
    private String customerUsername; // Username of the customer who gave the rating
    private int rating;            // The rating value (e.g., 1 to 5 stars)
    private LocalDateTime ratingDate; // Date and time when the rating was submitted

    /**
     * Full constructor for DishRating.
     * @param ratingId The unique ID of the rating.
     * @param menuItemId The ID of the menu item being rated.
     * @param customerUsername The username of the customer who rated.
     * @param rating The rating value (e.g., 1-5).
     * @param ratingDate The date and time of the rating.
     */
    public DishRating(int ratingId, int menuItemId, String customerUsername, int rating, LocalDateTime ratingDate) {
        this.ratingId = ratingId;
        this.menuItemId = menuItemId;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.ratingDate = ratingDate;
    }

    // --- Getters ---
    public int getRatingId() {
        return ratingId;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getRatingDate() {
        return ratingDate;
    }

    // --- Setters ---
    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setRatingDate(LocalDateTime ratingDate) {
        this.ratingDate = ratingDate;
    }

    /**
     * Provides a string representation of the DishRating object.
     * @return A formatted string displaying rating details.
     */
    @Override
    public String toString() {
        return "DishRating ID: " + ratingId +
               ", MenuItem ID: " + menuItemId +
               ", Customer: " + customerUsername +
               ", Rating: " + rating +
               ", Date: " + ratingDate;
    }

    /**
     * Checks if two DishRating objects are equal based on their ratingId.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishRating that = (DishRating) o;
        return ratingId == that.ratingId;
    }

    /**
     * Generates a hash code for the DishRating object based on ratingId.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ratingId);
    }
}
