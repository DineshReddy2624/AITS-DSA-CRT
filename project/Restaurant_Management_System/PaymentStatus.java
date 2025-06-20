package application;

/**
 * Enum representing the payment status of an order or booking.
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    REFUNDED("Refunded");

    private final String displayValue;

    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to a PaymentStatus enum.
     * @param text The string value to convert.
     * @return The corresponding PaymentStatus, or null if no match is found.
     */
    public static PaymentStatus fromString(String text) {
        for (PaymentStatus b : PaymentStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Or throw an IllegalArgumentException
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
