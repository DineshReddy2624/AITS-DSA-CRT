package application;

/**
 * Enum representing different payment methods.
 */
public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    ONLINE_PAYMENT("Online Payment");

    private final String displayValue;

    PaymentMethod(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to a PaymentMethod enum.
     * This method is case-insensitive.
     * @param text The string value to convert.
     * @return The corresponding PaymentMethod, or null if no match is found.
     */
    public static PaymentMethod fromString(String text) {
        if (text == null) {
            return null;
        }
        for (PaymentMethod b : PaymentMethod.values()) {
            if (b.displayValue.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Or throw an IllegalArgumentException
    }

    @Override
    public String toString() {
        return name(); // Using name() for database storage (e.g., "CASH")
    }
}
