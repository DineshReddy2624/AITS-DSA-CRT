package application;

/**
 * Represents a table booking made by a customer.
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

    /**
     * Constructor for a new TableBooking.
     * @param customerId The ID (username) of the customer making the booking.
     * @param customerName The full name of the customer.
     * @param phone The customer's phone number.
     * @param tableType The type of table booked.
     * @param tableNumber The specific table number.
     * @param bookingFee The fee for the booking.
     */
    public TableBooking(String customerId, String customerName, String phone, TableType tableType, int tableNumber, double bookingFee) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.seats = tableType.getSeats(); // Seats determined by TableType
        this.bookingFee = bookingFee;
        this.paymentStatus = PaymentStatus.PENDING; // Default status
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

    // Setters (if specific properties can be modified after creation)
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
        this.seats = tableType.getSeats(); // Update seats if table type changes
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

    @Override
    public String toString() {
        return "Booking by " + customerName + " (ID: " + customerId + ") for " + tableType.getDisplayValue() +
               " Table No." + tableNumber + " (Seats: " + seats + ")" +
               ", Fee: Rs." + String.format("%.2f", bookingFee) +
               ", Status: " + paymentStatus.getDisplayValue();
    }
}
