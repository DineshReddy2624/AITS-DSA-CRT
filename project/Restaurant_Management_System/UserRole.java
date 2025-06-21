package application;

/**
 * Enum representing the different roles a user can have in the system.
 * This helps in managing permissions and access control.
 */
public enum UserRole {
    ADMIN("Admin"),       // Administrator role with full access
    CUSTOMER("Customer"); // Regular customer role with limited access

    private final String displayValue; // A user-friendly string representation of the role

    /**
     * Constructor for UserRole enum.
     * @param displayValue The string representation of the role to be displayed in UI or reports.
     */
    UserRole(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the user-friendly display value of the role.
     * @return The display string for the role.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Converts a string value to its corresponding UserRole enum.
     * This is useful when loading roles from a database or external input.
     *
     * @param text The string value to convert (case-insensitive).
     * @return The matching UserRole enum, or null if no match is found.
     */
    public static UserRole fromString(String text) {
        for (UserRole r : UserRole.values()) {
            if (r.displayValue.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return null; // Return null if no matching role is found
    }

    /**
     * Overrides the default toString method to return the display value.
     * @return The display string of the role.
     */
    @Override
    public String toString() {
        return displayValue;
    }
}
