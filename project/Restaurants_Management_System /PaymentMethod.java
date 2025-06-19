package application;

/**
 * Enum representing the possible payment methods for an order.
 * This ensures consistency and clear designation of how an order was paid.
 */
public enum PaymentMethod {
    CASH("Cash"),
    ONLINE("Online");

    private final String displayValue;

    PaymentMethod(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to a PaymentMethod enum.
     * Useful when loading from the database or user input.
     * @param text The string representation of the payment method.
     * @return The corresponding PaymentMethod enum, or CASH if not found (as a default).
     */
    public static PaymentMethod fromString(String text) {
        for (PaymentMethod b : PaymentMethod.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Default to CASH if an unknown method is encountered
        return CASH;
    }
}
