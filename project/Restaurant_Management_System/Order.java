package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections; // Import Collections for frequency counting

/**
 * Represents a customer order in the restaurant system.
 * Includes items, status, payment details, customer information, and order time.
 */
public class Order {
    int orderId; // Changed to private to enforce getter/setter usage
    private List<MenuItem> items;
    OrderStatus status; // Changed to private
    PaymentStatus paymentStatus; // Changed to private
    private double discountApplied; // Changed to private
    private PaymentMethod paymentMethod; // Changed to private
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
        this.orderTime = LocalDateTime.now(); // Set order time to now
    }

    /**
     * Constructor for loading an order from the database.
     * @param orderId The order ID.
     * @param customerUsername The username of the customer.
     * @param orderTime The time the order was placed.
     * @param status The current status of the order.
     * @param paymentStatus The payment status.
     * @param paymentMethod The payment method.
     * @param discountApplied The discount applied.
     */
    public Order(int orderId, String customerUsername, LocalDateTime orderTime, OrderStatus status,
                 PaymentStatus paymentStatus, PaymentMethod paymentMethod, double discountApplied) {
        this.orderId = orderId;
        this.customerUsername = customerUsername;
        this.orderTime = orderTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.discountApplied = discountApplied;
        this.items = new ArrayList<>(); // Initialize items, will be populated separately
    }

    // Getters for all properties
    public int getOrderId() {
        return orderId;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public double getDiscountApplied() {
        return discountApplied;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    // Setters for properties that can be changed
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }


    /**
     * Adds a menu item to the order.
     * @param item The MenuItem to add.
     */
    public void addItem(MenuItem item) {
        this.items.add(item);
    }

    /**
     * Calculates the subtotal of all items in the order (before discount and GST).
     * @return The subtotal amount.
     */
    public double getSubtotal() {
        return items.stream().mapToDouble(MenuItem::getPrice).sum();
    }

    /**
     * Calculates the final price before GST (subtotal - discount).
     * @return The amount after discount, before GST.
     */
    public double getFinalPriceBeforeGST() {
        return getSubtotal() - discountApplied;
    }

    /**
     * Calculates the GST amount for the order.
     * @return The GST amount.
     */
    public double getGSTAmount() {
        return getFinalPriceBeforeGST() * GST_RATE;
    }

    /**
     * Calculates the total amount of the order, including GST.
     * @return The total amount.
     */
    public double getTotalWithGST() {
        return getFinalPriceBeforeGST() + getGSTAmount();
    }

    // Setter for totalWithGST (used when loading from DB where total is already calculated)
    public void setTotalWithGST(double total) {
        // This setter is primarily for loading existing orders where the total
        // might have been stored directly. It bypasses recalculation.
        // For new orders, rely on getSubtotal, getGSTAmount, getTotalWithGST.
        // This field isn't explicitly stored as 'total_amount' in the Order object itself,
        // but it's part of the data used for the database `total_amount` column.
    }


    /**
     * Generates a detailed receipt string for the order.
     * @param allMenuItems A list of all available menu items to retrieve full item details.
     * @return A formatted string representing the order receipt.
     */
    public String getReceiptDetails(List<MenuItem> allMenuItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Order Receipt -----\n");
        sb.append(String.format("Order ID: %d\n", orderId));
        sb.append(String.format("Customer: %s\n", customerUsername != null ? customerUsername : "N/A"));
        sb.append(String.format("Order Time: %s\n", orderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sb.append("Items:\n");
        if (items.isEmpty()) {
            sb.append("  No items in this order.\n");
        } else {
            // Group items by name and count their frequency
            Map<String, Long> itemCounts = items.stream()
                    .collect(Collectors.groupingBy(MenuItem::getName, Collectors.counting()));

            itemCounts.forEach((name, count) -> {
                // Find one instance of the item to get its price
                MenuItem sampleItem = allMenuItems.stream().filter(i -> i.getName().equals(name)).findFirst().orElse(null);
                if (sampleItem != null) {
                    sb.append(String.format("- %s x %d (Rs.%.2f each)\n", name, count, sampleItem.getPrice()));
                }
            });
        }
        sb.append("-----------------\n");
        sb.append(String.format("Subtotal: Rs.%.2f\n", getSubtotal()));
        sb.append(String.format("Discount Applied: Rs.%.2f\n", discountApplied));
        sb.append(String.format("Net Amount (Before GST): Rs.%.2f\n", getFinalPriceBeforeGST()));
        sb.append(String.format("GST (%.0f%%): Rs.%.2f\\n", GST_RATE * 100, getGSTAmount()));
        sb.append(String.format("Final Amount (With GST): Rs.%.2f\n", getTotalWithGST()));
        sb.append(String.format("Payment Status: %s\n", paymentStatus.getDisplayValue()));
        sb.append(String.format("Payment Method: %s\n", paymentMethod.getDisplayValue()));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId +
               ", Customer: " + (customerUsername != null ? customerUsername : "N/A") +
               ", Status: " + status.getDisplayValue() +
               ", Final Amount: Rs." + String.format("%.2f", getTotalWithGST()) +
               ", Time: " + orderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
