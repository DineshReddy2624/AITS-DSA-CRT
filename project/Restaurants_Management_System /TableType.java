package application;

/**
 * Enum representing the different types of tables based on seating capacity.
 * This standardizes table types across the application.
 */
public enum TableType {
    TABLE_2("Table2", 2),
    TABLE_4("Table4", 4),
    TABLE_6("Table6", 6),
    TABLE_8("Table8", 8),
    TABLE_10("Table10", 10);

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
     * Useful when loading from the database or user input.
     * @param text The string representation of the table type.
     * @return The corresponding TableType enum, or null if not found.
     */
    public static TableType fromString(String text) {
        for (TableType b : TableType.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // Return null if no matching table type is found
    }

    /**
     * Returns a string representation including type and seats.
     */
    @Override
    public String toString() {
        return displayValue + " (Seats: " + seats + ")";
    }
}
