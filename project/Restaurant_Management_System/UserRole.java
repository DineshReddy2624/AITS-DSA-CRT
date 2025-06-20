// UserRole.java
package application;

/**
 * Enum representing the different roles a user can have in the system.
 */
public enum UserRole {
    ADMIN("Admin"),
    CUSTOMER("Customer");

    private final String displayValue;

    UserRole(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to a UserRole enum.
     * This method is case-insensitive, checking both display value and enum name.
     * @param text The string value to convert.
     * @return The corresponding UserRole, or null if no match is found.
     */
    public static UserRole fromString(String text) {
        if (text == null) {
            return null;
        }
        for (UserRole r : UserRole.values()) {
            if (r.displayValue.equalsIgnoreCase(text) || r.name().equalsIgnoreCase(text)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name(); // Use the enum name (e.g., "ADMIN") for storing in DB
    }
}
