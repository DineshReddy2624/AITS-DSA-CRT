package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections; // Import Collections for frequency counting
import java.util.LinkedHashMap; // To maintain insertion order for items in bill

/**
 * Represents a customer order in the restaurant system.
 * Includes items, status, payment details, customer information, and order time.
 */
public class Order {
    private int orderId; // Changed to private to enforce getter/setter usage
    private List<MenuItem> items;
    private OrderStatus status; // Changed to private
    private PaymentStatus paymentStatus; // Changed to private
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
        this.status = OrderStatus.PENDING; // Default status for new order
        this.paymentStatus = PaymentStatus.PENDING; // Default payment status
        this.discountApplied = 0.0; // Default no discount
        this.paymentMethod = PaymentMethod.CASH; // Default to cash
        this.orderTime = LocalDateTime.now(); // Set order time to current time
    }

    /**
     * Full constructor for Order.
     * @param orderId The unique ID of the order.
     * @param items A list of MenuItem objects in the order.
     * @param status The current status of the order.
     * @param paymentStatus The payment status of the order.
     * @param discountApplied The amount of discount applied.
     * @param paymentMethod The payment method used.
     * @param customerUsername The username of the customer who placed the order.
     * @param orderTime The timestamp when the order was placed.
     */
    public Order(int orderId, List<MenuItem> items, OrderStatus status, PaymentStatus paymentStatus,
                 double discountApplied, PaymentMethod paymentMethod, String customerUsername, LocalDateTime orderTime) {
        this.orderId = orderId;
        this.items = new ArrayList<>(items); // Defensive copy
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.discountApplied = discountApplied;
        this.paymentMethod = paymentMethod;
        this.customerUsername = customerUsername;
        this.orderTime = orderTime;
    }

    // --- Getters ---
    public int getOrderId() {
        return orderId;
    }

    public List<MenuItem> getItems() {
        // Return a new ArrayList to prevent external modification of the internal list
        return new ArrayList<>(items);
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

    // --- Setters ---
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setItems(List<MenuItem> items) {
        this.items = new ArrayList<>(items); // Defensive copy
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
     * Adds a single MenuItem to the order.
     * @param item The MenuItem to add.
     */
    public void addItem(MenuItem item) {
        this.items.add(item);
    }

    /**
     * Calculates the subtotal of all items in the order before any discounts or GST.
     * @return The subtotal amount.
     */
    public double getSubtotal() {
        return items.stream().mapToDouble(MenuItem::getPrice).sum();
    }

    /**
     * Calculates the final price before GST, applying any discounts.
     * @return The final price after discount, before GST.
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
     * Calculates the total price of the order including GST and after discounts.
     * @return The total payable amount.
     */
    public double getTotalWithGST() {
        return getFinalPriceBeforeGST() + getGSTAmount();
    }

    /**
     * Returns a map of menu items and their quantities in the order.
     * This is useful for displaying the order summary with counts.
     * Uses LinkedHashMap to preserve insertion order for consistent bill generation.
     * @return A Map where keys are MenuItems and values are their quantities.
     */
    public Map<MenuItem, Integer> getItemsWithQuantities() {
        Map<MenuItem, Integer> itemCounts = new LinkedHashMap<>();
        for (MenuItem item : items) {
            itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
        }
        return itemCounts;
    }

    /**
     * Generates a formatted string representing the bill content for the order.
     * @return A detailed bill string.
     */
    public String generateBillContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("===============================\n");
        sb.append("      RESTAURANT ORDER BILL      \n");
        sb.append("===============================\n");
        sb.append(String.format("Order ID: %d\n", orderId));
        sb.append(String.format("Customer: %s\n", customerUsername));
        sb.append(String.format("Order Time: %s\n", orderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sb.append("-----------------\n");
        sb.append("Items:\n");

        if (items != null && !items.isEmpty()) {
            Map<MenuItem, Integer> itemCounts = getItemsWithQuantities(); // Use the existing method

            itemCounts.forEach((item, count) -> {
                sb.append(String.format("- %s x %d (Rs.%.2f each)\n", item.getName(), count, item.getPrice()));
            });
        }
        sb.append("-----------------\n");
        sb.append(String.format("Subtotal: Rs.%.2f\n", getSubtotal()));
        sb.append(String.format("Discount Applied: Rs.%.2f\n", discountApplied));
        sb.append(String.format("Net Amount (Before GST): Rs.%.2f\n", getFinalPriceBeforeGST()));
        sb.append(String.format("GST (%.0f%%): Rs.%.2f\n", GST_RATE * 100, getGSTAmount()));
        sb.append(String.format("Final Amount (With GST): Rs.%.2f\n", getTotalWithGST()));
        sb.append(String.format("Payment Status: %s\n", paymentStatus.getDisplayValue()));
        sb.append(String.format("Payment Method: %s\n", paymentMethod.getDisplayValue()));
        return sb.toString();
    }

    /**
     * Overrides the default toString method for debugging and logging.
     * @return A concise string representation of the order.
     */
    @Override
    public String toString() {
        return "Order{" +
               "orderId=" + orderId +
               ", items=" + items.size() + " items" +
               ", status=" + status +
               ", paymentStatus=" + paymentStatus +
               ", discountApplied=" + discountApplied +
               ", paymentMethod=" + paymentMethod +
               ", customerUsername='" + customerUsername + '\'' +
               ", orderTime=" + orderTime +
               '}';
    }

    /**
     * Checks if two Order objects are equal based on their orderId.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId;
    }

    /**
     * Generates a hash code for the Order object based on orderId.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
