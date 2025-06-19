package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
public class Order {
    public int orderId;
    public List<MenuItem> items = new ArrayList<>();
    // Using enums for status and payment status for better type safety and clarity
    public OrderStatus status = OrderStatus.PLACED;
    public PaymentStatus paymentStatus = PaymentStatus.PENDING;
    public PaymentMethod paymentMethod = PaymentMethod.CASH; // New: Default payment method
    public double discountApplied = 0; // This explicitly represents the discount amount

    /**
     * Constructor for Order.
     * @param orderId The unique identifier for the order.
     */
    public Order(int orderId) {
        this.orderId = orderId;
    }

    // --- Getters for PropertyValueFactory and external access ---
    public int getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() { // New: Getter for payment method
        return paymentMethod;
    }

    public double getDiscountApplied() { // Added getter for discountApplied
        return discountApplied;
    }

    /**
     * Adds a MenuItem to the order.
     * @param item The MenuItem to add.
     */
    public void addItem(MenuItem item) {
        items.add(item);
    }

    /**
     * Calculates the total price of all items in the order before any discount.
     * @return The total price.
     */
    public double getPrice() { // This method is already used by PropertyValueFactory for "price"
        double total = 0;
        for (MenuItem item : items) {
            total += item.getPrice();
        }
        return total;
    }

    /**
     * Calculates the final price of the order after applying the discount.
     * @return The net amount to be paid.
     */
    public double getFinalPrice() { // This method is already used by PropertyValueFactory for "finalPrice"
        return getPrice() - discountApplied;
    }

    /**
     * Provides a detailed string representation of the order,
     * including grouped items, total, discount, and statuses.
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");

        // Group items for a cleaner display
        Map<String, Integer> itemCounts = new HashMap<>();
        Map<String, Double> itemPrices = new HashMap<>(); // Store original item prices at time of order

        for (MenuItem item : items) {
            itemCounts.put(item.getName(), itemCounts.getOrDefault(item.getName(), 0) + 1);
            if (!itemPrices.containsKey(item.getName())) {
                itemPrices.put(item.getName(), item.getPrice());
            }
        }

        sb.append("Items:\n");
        if (items.isEmpty()) {
            sb.append("  (No items in this order)\n");
        } else {
            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                sb.append(" - ").append(entry.getKey()).append(" (x").append(entry.getValue()).append("): Rs.")
                  .append(String.format("%.2f", entry.getValue() * itemPrices.get(entry.getKey()))).append("\n");
            }
        }

        sb.append("-------------------------\n");
        sb.append("Total Amount (Before Discount): Rs.").append(String.format("%.2f", getPrice())).append("\n");
        sb.append("Discount Applied: Rs.").append(String.format("%.2f", discountApplied)).append("\n");
        sb.append("Final Price (Net Amount): Rs.").append(String.format("%.2f", getFinalPrice())).append("\n");
        sb.append("Status: ").append(status.getDisplayValue()).append("\n");
        sb.append("Payment Status: ").append(paymentStatus.getDisplayValue()).append("\n");
        sb.append("Payment Method: ").append(paymentMethod.getDisplayValue()).append("\n"); // New: Include payment method
        return sb.toString();
    }
}
