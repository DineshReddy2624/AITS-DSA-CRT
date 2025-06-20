package application;

public enum TableType {
    SMALL("Small", 2),
    MEDIUM("Medium", 4),
    LARGE("Large", 6),
    PRIVATE_DINING("Private Dining", 10);

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

    public static TableType fromString(String text) {
        for (TableType b : TableType.values()) {
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
