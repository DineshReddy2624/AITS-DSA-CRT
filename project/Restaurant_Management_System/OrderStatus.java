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
     * @param text The string value to convert.
     * @return The corresponding OrderStatus, or null if no match is found.
     */
    public static OrderStatus fromString(String text) {
        for (OrderStatus b : OrderStatus.values()) {
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
