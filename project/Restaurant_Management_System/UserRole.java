package application;

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

    public static UserRole fromString(String text) {
        for (UserRole r : UserRole.values()) {
            if (r.displayValue.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
