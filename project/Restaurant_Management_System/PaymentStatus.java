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
     * This method is case-insensitive.
     * @param text The string value to convert.
     * @return The corresponding PaymentStatus, or null if no match is found.
     */
    public static PaymentStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        for (PaymentStatus b : PaymentStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Or throw an IllegalArgumentException
    }

    @Override
    public String toString() {
        return name(); // Using name() for database storage (e.g., "PENDING")
    }
}
