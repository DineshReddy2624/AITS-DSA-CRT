package application;

import java.util.Objects;

/**
 * Represents a simplified credit card entity for demonstration purposes.
 * IMPORTANT: In a real application, full credit card details should NEVER be stored.
 * Use tokenization and a secure payment gateway. This class is for mock data handling.
 * It stores only the last four digits, card type, and expiry for display purposes.
 */
public class CreditCard {
    private String cardId; // Unique ID for the saved card (e.g., last 4 digits + type)
    private String lastFourDigits;
    private String cardType; // e.g., "Visa", "MasterCard", "Amex"
    private String expiryMonth;
    private String expiryYear;

    /**
     * Constructor for a CreditCard object.
     * @param lastFourDigits The last four digits of the credit card number.
     * @param cardType The type of credit card (e.g., "Visa").
     * @param expiryMonth The expiry month (MM) of the card.
     * @param expiryYear The expiry year (YYYY) of the card.
     */
    public CreditCard(String lastFourDigits, String cardType, String expiryMonth, String expiryYear) {
        this.lastFourDigits = lastFourDigits;
        this.cardType = cardType;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        // Generate a simple card ID for demonstration; in real-world this would be a secure token
        this.cardId = cardType + "-" + lastFourDigits;
    }

    // Getters for all properties

    public String getCardId() {
        return cardId;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    // Setters for properties (note: cardId should generally be immutable after creation)
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
        // Update cardId if last four digits change to maintain consistency
        this.cardId = this.cardType + "-" + this.lastFourDigits;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
        // Update cardId if type changes to maintain consistency
        this.cardId = this.cardType + "-" + this.lastFourDigits;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    /**
     * Provides a string representation of the CreditCard object.
     * @return A formatted string displaying card type, last four digits, and expiry.
     */
    @Override
    public String toString() {
        return cardType + " ending in " + lastFourDigits + " (Exp: " + expiryMonth + "/" + expiryYear + ")";
    }

    /**
     * Checks if two CreditCard objects are equal.
     * Equality is based on last four digits, card type, expiry month, and expiry year.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditCard that = (CreditCard) o;
        return Objects.equals(lastFourDigits, that.lastFourDigits) &&
               Objects.equals(cardType, that.cardType) &&
               Objects.equals(expiryMonth, that.expiryMonth) &&
               Objects.equals(expiryYear, that.expiryYear);
    }

    /**
     * Generates a hash code for the CreditCard object.
     * The hash code is based on last four digits, card type, expiry month, and expiry year.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(lastFourDigits, cardType, expiryMonth, expiryYear);
    }
}
