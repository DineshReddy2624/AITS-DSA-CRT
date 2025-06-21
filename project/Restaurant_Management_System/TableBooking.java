package application;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a table booking in the restaurant system.
 * Includes details about the customer, table, time, duration, and payment.
 */
public class TableBooking {
    private int bookingId; // Added bookingId field for unique identification
    private String customerId; // Linked to User's userId
    private String customerName; // Changed to private
    private String phone; // Changed to private
    private TableType tableType; // Changed to private
    private int tableNumber; // Changed to private
    private int seats; // New: Storing seats for convenience, derived from TableType (changed to private)
    private double bookingFee; // Changed to private
    private PaymentStatus paymentStatus; // Changed to private
    private PaymentMethod paymentMethod; // Changed to private
    private LocalDateTime bookingTime;
    private int durationMinutes;

    /**
     * Constructor for a new booking, setting default payment status/method.
     * @param customerId The ID of the user making the booking.
     * @param customerName The name of the customer.
     * @param phone The phone number of the customer.
     * @param tableType The type of table being booked.
     * @param tableNumber The specific table number.
     * @param seats The number of seats booked (should match tableType.getSeats()).
     * @param bookingTime The date and time of the booking.
     * @param durationMinutes The duration of the booking in minutes.
     * @param bookingFee The fee for the booking.
     */
    public TableBooking(String customerId, String customerName, String phone,
                        TableType tableType, int tableNumber, int seats,
                        LocalDateTime bookingTime, int durationMinutes, double bookingFee) {
        this(0, customerId, customerName, phone, tableType, tableNumber, seats, bookingTime, durationMinutes, bookingFee, PaymentStatus.PENDING, PaymentMethod.CASH);
    }

    /**
     * Full constructor for TableBooking, including bookingId and payment details.
     * @param bookingId The unique identifier for the booking.
     * @param customerId The ID of the user making the booking.
     * @param customerName The name of the customer.
     * @param phone The phone number of the customer.
     * @param tableType The type of table being booked.
     * @param tableNumber The specific table number.
     * @param seats The number of seats booked.
     * @param bookingTime The date and time of the booking.
     * @param durationMinutes The duration of the booking in minutes.
     * @param bookingFee The fee for the booking.
     * @param paymentStatus The payment status of the booking.
     * @param paymentMethod The payment method used for the booking.
     */
    public TableBooking(int bookingId, String customerId, String customerName, String phone,
                        TableType tableType, int tableNumber, int seats,
                        LocalDateTime bookingTime, int durationMinutes, double bookingFee,
                        PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.seats = seats;
        this.bookingTime = bookingTime;
        this.durationMinutes = durationMinutes;
        this.bookingFee = bookingFee;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }

    // --- Getters ---
    public int getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public TableType getTableType() {
        return tableType;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getSeats() {
        return seats;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getBookingFee() {
        return bookingFee;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    // --- Setters ---
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setBookingFee(double bookingFee) {
        this.bookingFee = bookingFee;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Provides a string representation of the TableBooking object.
     * @return A formatted string displaying booking details.
     */
    @Override
    public String toString() {
        return "Booking ID: " + bookingId +
               ", Customer: " + customerName +
               ", Table Type: " + tableType.getDisplayValue() +
               ", Table No: " + tableNumber +
               ", Seats: " + seats +
               ", Time: " + bookingTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
               ", Duration: " + durationMinutes + " minutes" +
               ", Fee: Rs." + String.format("%.2f", bookingFee) +
               ", Payment: " + paymentStatus.getDisplayValue();
    }

    /**
     * Checks if two TableBooking objects are equal.
     * Equality is based on customerId, tableNumber, and bookingTime to prevent duplicate logical bookings.
     * bookingId is not used for equality check here as it's an auto-generated primary key in DB,
     * while logical equality relies on the unique combination of these fields.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableBooking that = (TableBooking) o;
        // Consider a booking unique by customerId, tableNumber, and bookingTime
        // This prevents duplicate bookings for the same customer at the same table and time
        return tableNumber == that.tableNumber &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(bookingTime, that.bookingTime);
    }

    /**
     * Generates a hash code for the TableBooking object.
     * The hash code is based on customerId, tableNumber, and bookingTime.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(customerId, tableNumber, bookingTime);
    }
}
