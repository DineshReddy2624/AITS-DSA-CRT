package application;

import java.time.LocalDateTime;

public class DishRating {
    private int ratingId;
    private int menuItemId;
    private String customerUsername;
    private int rating;
    private LocalDateTime ratingDate;

    public DishRating(int ratingId, int menuItemId, String customerUsername, int rating, LocalDateTime ratingDate) {
        this.ratingId = ratingId;
        this.menuItemId = menuItemId;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.ratingDate = ratingDate;
    }

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

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "DishRating ID: " + ratingId +
               ", MenuItem ID: " + menuItemId +
               ", Customer: " + customerUsername +
               ", Rating: " + rating +
               ", Date: " + ratingDate;
    }
}
