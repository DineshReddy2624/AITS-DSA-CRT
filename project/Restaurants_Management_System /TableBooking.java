package application;

/**
 * Represents a table booking in the restaurant.
 * Stores customer and table details, along with booking fee and payment status.
 */
public class TableBooking {
    // Properties using enums for type safety
    public TableType tableType;
    public int tableNumber; // Specific table number within its type (e.g., Table2 #1)
    public String customerName;
    public String phone;
    public int seats; // Redundant if using TableType.getSeats(), but kept for clarity/flexibility
    public String customerId; // Unique ID assigned to the customer for this booking
    public PaymentStatus paymentStatus = PaymentStatus.PENDING; // Default status
    public double bookingFee;

    /**
     * Constructor for TableBooking.
     * @param customerId The unique ID for the customer making the booking.
     * @param customerName The name of the customer.
     * @param phone The phone number of the customer.
     * @param tableType The type of table booked (e.g., TABLE_2, TABLE_4).
     * @param tableNumber The specific number of the table (1-10).
     * @param bookingFee The fee for the booking.
     */
    public TableBooking(String customerId, String customerName, String phone,
                        TableType tableType, int tableNumber, double bookingFee) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.seats = tableType.getSeats(); // Derive seats from tableType for consistency
        this.bookingFee = bookingFee;
    }

    // --- Getters for PropertyValueFactory and external access ---

    /**
     * Retrieves the customer ID for this booking.
     * @return The customer ID.
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Retrieves the customer's name for this booking.
     * @return The customer's name.
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Retrieves the table type for this booking.
     * @return The table type enum.
     */
    public TableType getTableType() {
        return tableType;
    }

    /**
     * Retrieves the specific table number for this booking.
     * @return The table number.
     */
    public int getTableNumber() {
        return tableNumber;
    }

    /**
     * Retrieves the number of seats for the booked table.
     * @return The number of seats.
     */
    public int getSeats() {
        return seats;
    }

    /**
     * Retrieves the customer's phone number for this booking.
     * @return The phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Retrieves the payment status of this booking.
     * @return The payment status enum.
     */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * Retrieves the booking fee for this table.
     * @return The booking fee.
     */
    public double getBookingFee() {
        return bookingFee;
    }

    /**
     * Provides a detailed string representation of the table booking.
     */
    public String toDetailedString() {
        return "Customer ID: " + customerId +
                " | Table: " + tableType.getDisplayValue() + " #" + tableNumber +
                " | Seats: " + seats +
                " | Name: " + customerName +
                " | Phone: " + phone +
                " | Booking Fee: Rs." + String.format("%.2f", bookingFee) +
                " | Payment Status: " + paymentStatus.getDisplayValue();
    }
}
