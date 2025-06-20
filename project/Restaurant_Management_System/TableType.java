package application;

import javafx.util.Callback; // Keep if still used, otherwise remove
import java.time.LocalDateTime; // Keep if still used, otherwise remove


/**
 * Enum representing the different types of tables available in the restaurant.
 * Each table type has a display value and a default number of seats.
 */
public enum TableType {
    SMALL("Small", 2),
    MEDIUM("Medium", 4),
    LARGE("Large", 6),
    PRIVATE_DINING("Private Dining", 10); // Added PRIVATE_DINING

    private final String displayValue;
    private final int seats; // Number of seats for this table type

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
        return displayValue;
    }
}
