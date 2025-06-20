package application;

import java.time.LocalDateTime;

/**
 * Represents a table booking made by a customer.
 * Updated to include booking time and duration.
 */
public class TableBooking {
    String customerId; // Linked to username in the users table
    public String customerName;
    public String phone;
    public TableType tableType;
    public int tableNumber;
    public int seats;
    public double bookingFee;
    public PaymentStatus paymentStatus;
    public LocalDateTime bookingTime;   // NEW: When the booking is for
    public int durationMinutes;         // NEW: Duration of the booking

    /**
     * Constructor for a new TableBooking with full details.
     *
     * @param customerId   The ID (username) of the customer making the booking.
     * @param customerName The full name of the customer.
     * @param phone        The customer's phone number.
     * @param tableType    The type of table booked.
     * @param tableNumber  The specific table number.
     * @param bookingTime  The date and time of the booking.
     * @param durationMinutes The duration of the booking in minutes.
     * @param bookingFee   The fee for the booking.
     */
    public TableBooking(String customerId, String customerName, String phone, TableType tableType, int tableNumber, LocalDateTime bookingTime, int durationMinutes, double bookingFee) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.bookingTime = bookingTime;
        this.durationMinutes = durationMinutes;
        this.bookingFee = bookingFee;
        this.paymentStatus = PaymentStatus.PENDING; // Default status

        // Set seats based on tableType, with a defensive check
        if (tableType != null) {
            this.seats = tableType.getSeats();
        } else {
            this.seats = 0; // Default if tableType is null
        }
    }

    /**
     * Constructor for a new TableBooking (older version, now delegates to the new one).
     * This constructor should ideally be phased out or used with care.
     *
     * @param customerId   The ID (username) of the customer making the booking.
     * @param customerName The full name of the customer.
     * @param phone        The customer's phone number.
     * @param tableType    The type of table booked.
     * @param tableNumber  The specific table number.
     * @param bookingFee   The fee for the booking.
     */
    public TableBooking(String customerId, String customerName, String phone, TableType tableType, int tableNumber, double bookingFee) {
        // Delegate to the more complete constructor with default values for new fields
        this(customerId, customerName, phone, tableType, tableNumber, LocalDateTime.now(), 90, bookingFee);
    }


    // Getters
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

    public double getBookingFee() {
        return bookingFee;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    // Setters (if specific properties can be modified after creation)
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
        if (tableType != null) {
            this.seats = tableType.getSeats(); // Update seats if table type changes
        } else {
            this.seats = 0; // Default if new table type is null
        }
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setBookingFee(double bookingFee) {
        this.bookingFee = bookingFee;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    @Override
    public String toString() {
        return "Booking by " + customerName + " (ID: " + customerId + ") for " +
               (tableType != null ? tableType.getDisplayValue() : "Unknown Type") +
               " (Table " + tableNumber + ", " + seats + " seats) " +
               " at " + (bookingTime != null ? bookingTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A") +
               " for " + durationMinutes + " mins";
    }
}
