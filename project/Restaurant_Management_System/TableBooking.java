package application;

import java.time.LocalDateTime;
import java.util.Objects;

public class TableBooking {
    String customerId;
    public String customerName;
    public String phone;
    public TableType tableType;
    public int tableNumber;
    public int seats;
    public double bookingFee;
    public PaymentStatus paymentStatus;
    public PaymentMethod paymentMethod;
    private LocalDateTime bookingTime;
    private int durationMinutes;

    public TableBooking(String customerId, String customerName, String phone, TableType tableType, int tableNumber,
                        LocalDateTime bookingTime, int durationMinutes, double bookingFee) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.seats = tableType != null ? tableType.getSeats() : 0;
        this.bookingTime = bookingTime;
        this.durationMinutes = durationMinutes;
        this.bookingFee = bookingFee;
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentMethod = PaymentMethod.ONLINE_PAYMENT;
    }

    public TableBooking(String customerId, String customerName, String phone, TableType tableType, int tableNumber,
                        LocalDateTime bookingTime, int durationMinutes, double bookingFee,
                        PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.seats = tableType != null ? tableType.getSeats() : 0;
        this.bookingTime = bookingTime;
        this.durationMinutes = durationMinutes;
        this.bookingFee = bookingFee;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
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

    public double getBookingFee() {
        return bookingFee;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
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
        if (tableType != null) {
            this.seats = tableType.getSeats();
        } else {
            this.seats = 0;
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

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
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
               " (Table " + tableNumber + ", " + seats + " seats)" +
               ", Time: " + bookingTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
               " for " + durationMinutes + " minutes" +
               ", Fee: Rs." + String.format("%.2f", bookingFee) +
               ", Payment: " + paymentStatus.getDisplayValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableBooking that = (TableBooking) o;
        return tableNumber == that.tableNumber &&
               durationMinutes == that.durationMinutes &&
               Double.compare(that.bookingFee, bookingFee) == 0 &&
               Objects.equals(customerId, that.customerId) &&
               tableType == that.tableType &&
               Objects.equals(bookingTime, that.bookingTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, tableType, tableNumber, bookingTime, durationMinutes, bookingFee);
    }
}
