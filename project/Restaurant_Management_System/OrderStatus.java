package application;

/**
 * Enum representing the different possible statuses of an order.
 */
public enum OrderStatus {
    PENDING("Pending"),           // Order has been placed, awaiting confirmation/preparation
    PREPARING("Preparing"),       // Order is being prepared in the kitchen
    READY_FOR_PICKUP("Ready for Pickup"), // Order is ready for customer pickup/delivery dispatch
    DELIVERED("Delivered"),       // Order has been successfully delivered/picked up
    CANCELLED("Cancelled");       // Order has been cancelled

    private final String displayValue; // A user-friendly string representation of the status

    /**
     * Constructor for OrderStatus enum.
     * @param displayValue The string representation of the order status to be displayed.
     */
    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the user-friendly display value of the order status.
     * @return The display string for the status.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to its corresponding OrderStatus enum.
     * This is useful when loading status from a database or external input.
     *
     * @param text The string value to convert (case-insensitive).
     * @return The matching OrderStatus enum, or null if no match is found.
     */
    public static OrderStatus fromString(String text) {
        for (OrderStatus b : OrderStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Return null if no matching status is found
    }

    /**
     * Overrides the default toString method to return the display value.
     * @return The display string of the order status.
     */
    @Override
    public String toString() {
        return displayValue;
    }
}
