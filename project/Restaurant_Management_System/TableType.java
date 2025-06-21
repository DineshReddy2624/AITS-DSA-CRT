package application;

/**
 * Enum representing the different types of tables available in the restaurant.
 * Each table type has a display value and a maximum number of seats it can accommodate.
 */
public enum TableType {
    SMALL("Small", 2),
    MEDIUM("Medium", 4),
    LARGE("Large", 6),
    PRIVATE_DINING("Private Dining", 10); // Example for a larger, special table

    private final String displayValue; // User-friendly name for the table type
    private final int seats;             // Number of seats for this table type

    /**
     * Constructor for TableType enum.
     * @param displayValue The string representation of the table type.
     * @param seats The number of seats associated with this table type.
     */
    TableType(String displayValue, int seats) {
        this.displayValue = displayValue;
        this.seats = seats;
    }

    /**
     * Returns the user-friendly display value of the table type.
     * @return The display string for the table type.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Returns the number of seats for this table type.
     * @return The number of seats.
     */
    public int getSeats() {
        return seats;
    }

    /**
     * Converts a string value to its corresponding TableType enum.
     * Useful for loading table types from a database or external input.
     *
     * @param text The string value to convert (case-insensitive).
     * @return The matching TableType enum, or null if no match is found.
     */
    public static TableType fromString(String text) {
        for (TableType b : TableType.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Return null if no matching type is found
    }

    /**
     * Overrides the default toString method to return the display value.
     * @return The display string of the table type.
     */
    @Override
    public String toString() {
        return displayValue;
    }
}
