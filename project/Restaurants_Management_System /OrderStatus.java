package application;

/**
 * Enum representing the possible statuses of an order.
 * This helps in enforcing valid states and improving code readability.
 */
public enum OrderStatus {
    PLACED("placed"),
    CONFIRMED("confirmed"),
    PREPARING("preparing"), // Added for future expansion/tracking
    READY_FOR_PICKUP("ready for pickup"), // Added for future expansion/tracking
    COMPLETED("completed"),
    CANCELLED("cancelled"); // Added for future expansion/tracking

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to an OrderStatus enum.
     * Useful when loading from the database.
     * @param text The string representation of the order status.
     * @return The corresponding OrderStatus enum, or PLACED if not found (as a default).
     */
    public static OrderStatus fromString(String text) {
        for (OrderStatus b : OrderStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Default to PLACED if an unknown status is encountered (e.g., from old data)
        return PLACED;
    }
}
