package application;

/**
 * Enum representing different types of tables available for booking.
 */
public enum TableType {
    TWO_SEATER("2-Seater", 2),
    FOUR_SEATER("4-Seater", 4),
    SIX_SEATER("6-Seater", 6),
    PRIVATE_ROOM("Private Room", 10); // Example: private room seats 10

    private final String displayValue;
    private final int seats;

    TableType(String displayValue, int seats) {
        this.displayValue = displayValue;
        this.seats = seats;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public int getSeats() {
        return seats;
    }

    /**
     * Converts a string value to a TableType enum.
     * @param text The string value to convert.
     * @return The corresponding TableType, or null if no match is found.
     */
    public static TableType fromString(String text) {
        for (TableType b : TableType.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Or throw an IllegalArgumentException
    }

    @Override
    public String toString() {
        return displayValue + " (" + seats + " seats)";
    }
}
