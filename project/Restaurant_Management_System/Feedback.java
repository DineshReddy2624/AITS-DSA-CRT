package application;

import java.time.LocalDateTime;

public class Feedback {
    private int feedbackId;
    private String customerUsername;
    private int rating;
    private String comments;
    private LocalDateTime feedbackDate;

    public Feedback(int feedbackId, String customerUsername, int rating, String comments, LocalDateTime feedbackDate) {
        this.feedbackId = feedbackId;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.comments = comments;
        this.feedbackDate = feedbackDate;
    }

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
               ", Date: " + feedbackDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
               ", Comments: '" + comments + "'";
    }
}
