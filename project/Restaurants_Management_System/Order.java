package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Order {
    public int orderId;
    public List<MenuItem> items = new ArrayList<>();
    public String status = "placed";
    public String paymentStatus = "pending";
    public double discountApplied = 0; // This now explicitly represents the discount amount

    public Order(int orderId) {
        this.orderId = orderId;
    }

    public void addItem(MenuItem item) { // Changed to public
        items.add(item);
    }

    public double getPrice() { // This is the total amount before discount
        double total = 0;
        for (MenuItem item : items) {
            total += item.getPrice();
        }
        return total;
    }

    public double getFinalPrice() { // This is the net amount (total - discount)
        return getPrice() - discountApplied;
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");

        // Group items for a cleaner display in the detailed string
        Map<String, Integer> itemCounts = new HashMap<>();
        Map<String, Double> itemPrices = new HashMap<>(); // Store original item prices

        for (MenuItem item : items) {
            itemCounts.put(item.getName(), itemCounts.getOrDefault(item.getName(), 0) + 1);
            if (!itemPrices.containsKey(item.getName())) { // Store price only once
                itemPrices.put(item.getName(), item.getPrice());
            }
        }

        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            sb.append(" - ").append(entry.getKey()).append(" (x").append(entry.getValue()).append("): Rs.")
              .append(String.format("%.2f", entry.getValue() * itemPrices.get(entry.getKey()))).append("\n");
        }

        sb.append("-------------------------\n");
        sb.append("Total Amount (Before Discount): Rs.").append(String.format("%.2f", getPrice())).append("\n");
        sb.append("Discount Applied: Rs.").append(String.format("%.2f", discountApplied)).append("\n");
        sb.append("Final Price (Net Amount): Rs.").append(String.format("%.2f", getFinalPrice())).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Payment Status: ").append(paymentStatus).append("\n");
        return sb.toString();
    }
}
