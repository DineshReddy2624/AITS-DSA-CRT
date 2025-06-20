package application;

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

    public static OrderStatus fromString(String text) {
        for (OrderStatus b : OrderStatus.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
