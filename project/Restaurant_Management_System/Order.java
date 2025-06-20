package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Represents a customer order in the restaurant system.
 * Includes items, status, payment details, customer information, and order time.
 */
public class Order {
    public int orderId;
    public List<MenuItem> items;
    public OrderStatus status;
    public PaymentStatus paymentStatus;
    public double discountApplied;
    public PaymentMethod paymentMethod;
    private String customerUsername; // Link to the User who placed the order
    private LocalDateTime orderTime; // New: Actual time the order was placed/recorded

    private static final double GST_RATE = 0.05; // 5% GST

    /**
     * Constructor for a new order without a specific customer username yet.
     * Useful for initial creation before linking to a logged-in user.
     * Order time is set to now.
     * @param orderId The unique identifier for the order.
     */
    public Order(int orderId) {
        this.orderId = orderId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING; // Default status
        this.paymentStatus = PaymentStatus.PENDING; // Default payment status
        this.discountApplied = 0.0;
        this.paymentMethod = PaymentMethod.CASH; // Default payment method
        this.customerUsername = null; // No customer associated initially
        this.orderTime = LocalDateTime.now(); // Set current time on creation
    }

    /**
     * Constructor for a new order with a specific customer username.
     * Order time is set to now.
     * @param orderId The unique identifier for the order.
     * @param customerUsername The username of the customer placing the order.
     */
    public Order(int orderId, String customerUsername) {
        this(orderId); // Call the default constructor
        this.customerUsername = customerUsername;
    }

    /**
     * Constructor for loading an order from the database.
     * @param orderId The unique identifier for the order.
     * @param customerUsername The username of the customer placing the order.
     * @param status The current status of the order.
     * @param paymentStatus The payment status of the order.
     * @param paymentMethod The payment method used.
     * @param discountApplied The discount applied to the order.
     * @param orderTime The timestamp when the order was placed.
     */
    public Order(int orderId, String customerUsername, OrderStatus status, PaymentStatus paymentStatus,
                 PaymentMethod paymentMethod, double discountApplied, LocalDateTime orderTime) {
        this.orderId = orderId;
        this.items = new ArrayList<>(); // Items will be loaded separately
        this.customerUsername = customerUsername;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.discountApplied = discountApplied;
        this.orderTime = orderTime;
    }

    // --- Methods to manage order items and calculations ---

    public void addItem(MenuItem item) {
        this.items.add(item);
    }

    public void removeItem(MenuItem item) {
        this.items.remove(item);
    }
    
    public void setItems(List<MenuItem> items) {
        this.items = new ArrayList<>(items);
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(MenuItem::getPrice).sum();
    }

    public double getFinalPriceBeforeGST() {
        return getSubtotal() - discountApplied;
    }

    public double getGSTAmount() {
        return getFinalPriceBeforeGST() * GST_RATE;
    }

    public double getTotalWithGST() {
        return getFinalPriceBeforeGST() + getGSTAmount();
    }

    // --- Getters and Setters ---

    public int getOrderId() {
        return orderId;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    /**
     * Provides a detailed string representation of the order, including items and all financial breakdowns.
     * @return A formatted string with order details.
     */
    public String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Customer: ").append(customerUsername != null ? customerUsername : "N/A").append("\n");
        sb.append("Order Time: ").append(orderTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Status: ").append(status.getDisplayValue()).append("\n");
        sb.append("Payment Status: ").append(paymentStatus.getDisplayValue()).append("\n");
        sb.append("Payment Method: ").append(paymentMethod.getDisplayValue()).append("\n");
        sb.append("--- Items ---\n");
        if (items.isEmpty()) {
            sb.append("No items in this order.\n");
        } else {
            // Group items to show quantity and price per item
            items.stream()
                 .collect(Collectors.groupingBy(item -> item.getName(), Collectors.counting()))
                 .forEach((name, count) -> {
                     // Find a sample item to get its price. Assuming all items with same name have same price.
                     MenuItem sampleItem = items.stream().filter(i -> i.getName().equals(name)).findFirst().orElse(null);
                     if (sampleItem != null) {
                         sb.append(String.format("%s x %d (Rs.%.2f each)\n", name, count, sampleItem.getPrice()));
                     }
                 });
        }
        sb.append("-----------------\n");
        sb.append(String.format("Subtotal: Rs.%.2f\n", getSubtotal()));
        sb.append(String.format("Discount Applied: Rs.%.2f\n", discountApplied));
        sb.append(String.format("Net Amount (Before GST): Rs.%.2f\n", getFinalPriceBeforeGST()));
        sb.append(String.format("GST (%.0f%%): Rs.%.2f\n", GST_RATE * 100, getGSTAmount()));
        sb.append(String.format("Final Amount (With GST): Rs.%.2f\n", getTotalWithGST()));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId +
               ", Customer: " + (customerUsername != null ? customerUsername : "N/A") +
               ", Status: " + status.getDisplayValue() +
               ", Final Amount: Rs." + String.format("%.2f", getTotalWithGST()) +
               ", Time: " + orderTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}
