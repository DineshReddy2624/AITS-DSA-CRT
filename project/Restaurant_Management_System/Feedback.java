package application;

import java.time.LocalDateTime;

/**
 * Represents a customer feedback entry.
 */
public class Feedback {
    private int feedbackId;
    private String customerUsername; // Linked to username in the users table
    private int rating; // 1-5 stars
    private String comments;
    private LocalDateTime feedbackDate;

    /**
     * Constructor for a new Feedback entry.
     * @param feedbackId The unique ID of the feedback.
     * @param customerUsername The username of the customer submitting feedback.
     * @param rating The rating (1-5).
     * @param comments Any additional comments.
     * @param feedbackDate The date and time the feedback was submitted.
     */
    public Feedback(int feedbackId, String customerUsername, int rating, String comments, LocalDateTime feedbackDate) {
        this.feedbackId = feedbackId;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
    }

    // Getters
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

    // Setters (if needed for updates, though feedback is often immutable)
    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Feedback ID: " + feedbackId +
               ", User: " + customerUsername +
               ", Rating: " + rating + " stars" +
               ", Date: " + feedbackDate.toLocalDate() +
               ", Comments: " + (comments.length() > 50 ? comments.substring(0, 47) + "..." : comments);
    }
}
