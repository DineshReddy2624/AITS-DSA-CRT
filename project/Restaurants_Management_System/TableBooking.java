package application;

public class TableBooking {
    public String tableType;
    public int tableNumber;
    public String customerName;
    public String phone;
    public int seats;
    public String customerId;
    public String paymentStatus = "pending";
    public double bookingFee;

    public TableBooking(String tableType, int tableNumber, String customerName, String phone,
                        int seats, String customerId, double bookingFee) {
        this.tableType = tableType;
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.phone = phone;
        this.seats = seats;
        this.customerId = customerId;
        this.bookingFee = bookingFee;
    }

    public String toDetailedString() {
        return "Customer ID: " + customerId +
                " | Table: " + tableType + " #" + tableNumber +
                " | Seats: " + seats +
                " | Name: " + customerName +
                " | Phone: " + phone +
                " | Booking Fee: Rs." + String.format("%.2f", bookingFee) +
                " | Payment Status: " + paymentStatus;
    }
}
