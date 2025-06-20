package application;

import java.time.LocalDateTime;

/**
 * Represents a rating given by a customer to a specific menu item.
 */
public class DishRating {
    private int ratingId;
    private int menuItemId;
    private String customerUsername; // Linked to username in the users table
    private int rating; // 1-5 stars
    private LocalDateTime ratingDate;

    /**
     * Constructor for a new DishRating.
     * @param ratingId The unique ID of the rating.
     * @param menuItemId The ID of the menu item being rated.
     * @param customerUsername The username of the customer who gave the rating.
     * @param rating The rating value (1-5).
     * @param ratingDate The date and time the rating was given.
     */
    public DishRating(int ratingId, int menuItemId, String customerUsername, int rating, LocalDateTime ratingDate) {
        this.ratingId = ratingId;
        this.menuItemId = menuItemId;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.ratingDate = ratingDate;
    }

    // Getters
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

    // Setters (if needed)
    public void setRating(int rating) {
        this.rating = rating;
    }

    // Setter for ratingId (if it's auto-generated and needs to be set after insertion)
    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    @Override
    public String toString() {
        return "DishRating ID: " + ratingId +
               ", MenuItem ID: " + menuItemId +
               ", User: " + customerUsername +
               ", Rating: " + rating + " stars" +
               ", Date: " + ratingDate.toLocalDate();
    }
}
