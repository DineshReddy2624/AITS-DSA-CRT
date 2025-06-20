package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime; // Added for order_time, though not directly stored in this class now

/**
 * Represents a customer order in the restaurant system.
 * Includes items, status, payment details, and customer information.
 */
public class Order {
    public int orderId;
    public List<MenuItem> items;
    public OrderStatus status;
    public PaymentStatus paymentStatus;
    public double discountApplied;
    public PaymentMethod paymentMethod;
    private String customerUsername; // New: Link to the User who placed the order

    private static final double GST_RATE = 0.05; // 5% GST

    /**
     * Constructor for a new order without a specific customer username yet.
     * Useful for initial creation before linking to a logged-in user.
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
    }

    /**
     * Constructor for a new order with a specific customer username.
     * @param orderId The unique identifier for the order.
     * @param customerUsername The username of the customer placing the order.
     */
    public Order(int orderId, String customerUsername) {
        this(orderId); // Call the default constructor
        this.customerUsername = customerUsername;
    }

    // --- Methods to manage order items and calculations ---

    public void addItem(MenuItem item) {
        this.items.add(item);
    }

    public void removeItem(MenuItem item) {
        this.items.remove(item);
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

    /**
     * Provides a detailed string representation of the order, including items and all financial breakdowns.
     * @return A formatted string with order details.
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Customer: ").append(customerUsername != null ? customerUsername : "N/A").append("\n");
        sb.append("Status: ").append(status.getDisplayValue()).append("\n");
        sb.append("Payment Status: ").append(paymentStatus.getDisplayValue()).append("\n");
        sb.append("Payment Method: ").append(paymentMethod.getDisplayValue()).append("\n");
        sb.append("--- Items ---\n");
        if (items.isEmpty()) {
            sb.append("No items in this order.\n");
        } else {
            // Group items to show quantity
            items.stream()
                 .collect(Collectors.groupingBy(item -> item.getName(), Collectors.counting()))
                 .forEach((name, count) -> {
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
               ", Final Amount: Rs." + String.format("%.2f", getTotalWithGST());
    }
}
