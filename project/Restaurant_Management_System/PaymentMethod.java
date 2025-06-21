package application;

/**
 * Enum representing the different payment methods supported by the system.
 */
public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    ONLINE_PAYMENT("Online Payment"); // Could include UPI, Net Banking etc.

    private final String displayValue; // User-friendly string representation of the method

    /**
     * Constructor for PaymentMethod enum.
     * @param displayValue The string representation of the payment method.
     */
    PaymentMethod(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the user-friendly display value of the payment method.
     * @return The display string for the payment method.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to its corresponding PaymentMethod enum.
     * Useful for loading methods from a database or external input.
     *
     * @param text The string value to convert (case-insensitive).
     * @return The matching PaymentMethod enum, or null if no match is found.
     */
    public static PaymentMethod fromString(String text) {
        for (PaymentMethod b : PaymentMethod.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Return null if no matching method is found
    }

    /**
     * Overrides the default toString method to return the display value.
     * @return The display string of the payment method.
     */
    @Override
    public String toString() {
        return displayValue;
    }
}
