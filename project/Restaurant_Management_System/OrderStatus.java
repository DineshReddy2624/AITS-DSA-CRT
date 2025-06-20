package application;

/**
 * Enum representing the possible statuses of an order.
 */
public enum OrderStatus {
    PENDING("Pending"),
    PREPARING("Preparing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to an OrderStatus enum.
     * This method is case-insensitive.
     * @param text The string value to convert.
     * @return The corresponding OrderStatus, or null if no match is found.
     */
    public static OrderStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        for (OrderStatus b : OrderStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Or throw an IllegalArgumentException if an invalid status string should be strictly rejected
    }

    @Override
    public String toString() {
        return name(); // Using name() for database storage (e.g., "PENDING")
    }
}
