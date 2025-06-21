package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Import for formatting date in toString()
import java.util.Objects;

/**
 * Represents customer feedback submitted to the restaurant.
 * Includes feedback ID, customer who submitted it, a rating, comments, and submission date.
 */
public class Feedback {
    private int feedbackId; // Unique identifier for the feedback entry
    private String customerUsername; // Username of the customer who submitted the feedback
    private int rating; // Rating given by the customer (e.g., 1-5 stars)
    private String comments; // Textual comments from the customer
    private LocalDateTime feedbackDate; // Date and time when the feedback was submitted

    /**
     * Full constructor for Feedback.
     * @param feedbackId The unique ID of the feedback entry.
     * @param customerUsername The username of the customer.
     * @param rating The rating provided (e.g., 1 to 5).
     * @param comments The textual comments.
     * @param feedbackDate The date and time of submission.
     */
    public Feedback(int feedbackId, String customerUsername, int rating, String comments, LocalDateTime feedbackDate) {
        this.feedbackId = feedbackId;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
    }

    // --- Getters ---
    public int getFeedbackId() {
        return feedbackId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public int getRating() {
        return rating;
    }

    public String getComments() {
        return comments;
    }

    public LocalDateTime getFeedbackDate() {
        return feedbackDate;
    }

    // --- Setters ---
    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setFeedbackDate(LocalDateTime feedbackDate) {
        this.feedbackDate = feedbackDate;
    }

    /**
     * Provides a string representation of the Feedback object.
     * @return A formatted string displaying feedback details.
     */
    @Override
    public String toString() {
        return "Feedback ID: " + feedbackId +
               ", User: " + customerUsername +
               ", Rating: " + rating + " stars" +
               ", Date: " + feedbackDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
               ", Comments: '" + comments + "'";
    }

    /**
     * Checks if two Feedback objects are equal based on their feedbackId.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return feedbackId == feedback.feedbackId;
    }

    /**
     * Generates a hash code for the Feedback object based on feedbackId.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(feedbackId);
    }
}
