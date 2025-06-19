package application;

/**
 * Enum representing the possible payment statuses for orders and bookings.
 * This ensures consistency and prevents typos in status strings.
 */
public enum PaymentStatus {
    PENDING("pending"),
    PAID("paid"),
    CANCELLED("cancelled"), // For orders/bookings that might be cancelled before payment
    REFUNDED("refunded"); // For future expansion

    private final String displayValue;

    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to a PaymentStatus enum.
     * Useful when loading from the database.
     * @param text The string representation of the payment status.
     * @return The corresponding PaymentStatus enum, or PENDING if not found.
     */
    public static PaymentStatus fromString(String text) {
        for (PaymentStatus b : PaymentStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Default to PENDING if an unknown status is encountered
        return PENDING;
    }
}
