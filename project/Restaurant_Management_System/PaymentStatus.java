package application;

/**
 * Enum representing the payment status of an order or booking.
 * Defines common states for payment processing.
 */
public enum PaymentStatus {
    PENDING("Pending"),   // Payment is awaited
    PAID("Paid"),         // Payment has been successfully completed
    REFUNDED("Refunded"); // Payment has been refunded

    private final String displayValue; // User-friendly string representation of the status

    /**
     * Constructor for PaymentStatus enum.
     * @param displayValue The string representation of the payment status.
     */
    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the user-friendly display value of the payment status.
     * @return The display string for the payment status.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to its corresponding PaymentStatus enum.
     * Useful for loading status from a database or external input.
     *
     * @param text The string value to convert (case-insensitive).
     * @return The matching PaymentStatus enum, or null if no match is found.
     */
    public static PaymentStatus fromString(String text) {
        for (PaymentStatus b : PaymentStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Return null if no matching status is found
    }

    /**
     * Overrides the default toString method to return the display value.
     * @return The display string of the payment status.
     */
    @Override
    public String toString() {
        return displayValue;
    }
}
