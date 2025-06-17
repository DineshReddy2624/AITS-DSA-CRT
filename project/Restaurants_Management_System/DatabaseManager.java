package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date; // For timestamps

public class DatabaseManager {

    // Initializes the database by creating tables if they don't exist
    public static void initializeDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create menu_items table
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                                          "id INT PRIMARY KEY," + // Use INT for ID
                                          "name VARCHAR(255) NOT NULL," +
                                          "price DECIMAL(10, 2) NOT NULL" +
                                          ")";
            stmt.execute(createMenuItemsTable);
            System.out.println("Table 'menu_items' checked/created.");

            // Create orders table - UPDATED!
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                                       "order_id INT PRIMARY KEY," +
                                       "status VARCHAR(50) NOT NULL," +
                                       "payment_status VARCHAR(50) NOT NULL," +
                                       "discount_applied DECIMAL(10, 2)," +
                                       "total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," + // New Field
                                       "net_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +   // New Field
                                       "order_time DATETIME" + // Use DATETIME for timestamps
                                       ")";
            stmt.execute(createOrdersTable);
            System.out.println("Table 'orders' checked/created.");

            // Create order_items table (many-to-many relationship)
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                                           "order_item_id INT PRIMARY KEY AUTO_INCREMENT," + // Auto-incrementing ID
                                           "order_id INT NOT NULL," +
                                           "menu_item_id INT NOT NULL," +
                                           "quantity INT NOT NULL," +
                                           "item_price_at_order DECIMAL(10, 2) NOT NULL," + // Store price at time of order
                                           "FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
                                           "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
                                           ")";
            stmt.execute(createOrderItemsTable);
            System.out.println("Table 'order_items' checked/created.");

            // Create table_bookings table
            String createTableBookingsTable = "CREATE TABLE IF NOT EXISTS table_bookings (" +
                                              "booking_id INT PRIMARY KEY AUTO_INCREMENT," +
                                              "table_type VARCHAR(50) NOT NULL," +
                                              "table_number INT NOT NULL," +
                                              "customer_name VARCHAR(255) NOT NULL," +
                                              "phone VARCHAR(20) NOT NULL," +
                                              "seats INT NOT NULL," +
                                              "customer_id VARCHAR(50) NOT NULL UNIQUE," + // Customer ID should be unique
                                              "payment_status VARCHAR(50) NOT NULL," +
                                              "booking_fee DECIMAL(10, 2) NOT NULL" +
                                              ")";
            stmt.execute(createTableBookingsTable);
            System.out.println("Table 'table_bookings' checked/created.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // In a real app, you might throw a runtime exception or handle gracefully
        }
    }

    // --- MenuItem operations ---

    public static void saveMenuItem(MenuItem item) {
        // Use INSERT ... ON DUPLICATE KEY UPDATE for upsert functionality (if ID exists, update)
        // This is useful if you manually set IDs and want to update existing items.
        // If IDs are auto-incremented by DB, you'd use plain INSERT.
        String sql = "INSERT INTO menu_items (id, name, price) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), price = VALUES(price)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getPrice());
            pstmt.executeUpdate();
            System.out.println("Saved/Updated menu item: " + item.getName());
        } catch (SQLException e) {
            System.err.println("Error saving menu item: " + e.getMessage());
        }
    }

    public static List<MenuItem> loadMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, name, price FROM menu_items ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                menuItems.add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("price")));
            }
            System.out.println("Loaded " + menuItems.size() + " menu items.");
        } catch (SQLException e) {
            System.err.println("Error loading menu items: " + e.getMessage());
        }
        return menuItems;
    }

    public static void deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deleted menu item with ID: " + id);
            } else {
                System.out.println("No menu item found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
        }
    }

    // --- Order operations ---

    public static void saveOrder(Order order) {
        // UPDATED: Added total_amount and net_amount to the INSERT statement
        String orderSql = "INSERT INTO orders (order_id, status, payment_status, discount_applied, total_amount, net_amount, order_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String orderItemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, item_price_at_order) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Calculate amounts from the Order object
            double calculatedTotalAmount = order.getPrice();
            double calculatedNetAmount = order.getFinalPrice(); // This is total - discountApplied

            // Save order details - UPDATED PreparedStatement
            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql)) {
                pstmtOrder.setInt(1, order.orderId);
                pstmtOrder.setString(2, order.status);
                pstmtOrder.setString(3, order.paymentStatus);
                pstmtOrder.setDouble(4, order.discountApplied);
                pstmtOrder.setDouble(5, calculatedTotalAmount); // Set total_amount
                pstmtOrder.setDouble(6, calculatedNetAmount);   // Set net_amount
                pstmtOrder.setTimestamp(7, new Timestamp(new Date().getTime())); // Current timestamp
                pstmtOrder.executeUpdate();
            }

            // Save order items in a batch (this part remains the same)
            try (PreparedStatement pstmtOrderItem = conn.prepareStatement(orderItemSql)) {
                Map<Integer, Integer> itemQuantities = new HashMap<>();
                Map<Integer, Double> itemPrices = new HashMap<>();
                for (MenuItem item : order.items) {
                    itemQuantities.put(item.getId(), itemQuantities.getOrDefault(item.getId(), 0) + 1);
                    itemPrices.put(item.getId(), item.getPrice());
                }

                for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
                    pstmtOrderItem.setInt(1, order.orderId);
                    pstmtOrderItem.setInt(2, entry.getKey());
                    pstmtOrderItem.setInt(3, entry.getValue());
                    pstmtOrderItem.setDouble(4, itemPrices.get(entry.getKey()));
                    pstmtOrderItem.addBatch();
                }
                pstmtOrderItem.executeBatch();
            }

            conn.commit(); // Commit transaction
            System.out.println("Saved order ID: " + order.orderId + " with total: " + calculatedTotalAmount + ", net: " + calculatedNetAmount);
        } catch (SQLException e) {
            System.err.println("Error saving order: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                    System.err.println("Order save transaction rolled back.");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection after order save: " + e.getMessage());
                }
            }
        }
    }

    public static List<Order> loadOrders(Map<Integer, MenuItem> menuMap) {
        List<Order> orders = new ArrayList<>();
        // UPDATED: Select total_amount and net_amount (even if not directly used in Order.java fields)
        String orderSql = "SELECT order_id, status, payment_status, discount_applied, total_amount, net_amount FROM orders ORDER BY order_id";
        String orderItemSql = "SELECT menu_item_id, quantity, item_price_at_order FROM order_items WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsOrders = stmt.executeQuery(orderSql)) {

            while (rsOrders.next()) {
                int orderId = rsOrders.getInt("order_id");
                Order order = new Order(orderId);
                order.status = rsOrders.getString("status");
                order.paymentStatus = rsOrders.getString("payment_status");
                order.discountApplied = rsOrders.getDouble("discount_applied");
                // The Order object can calculate total and final price,
                // so we don't *strictly* need to store them as fields in Order.java
                // unless you want to preserve the exact historical calculated value
                // without re-deriving it from items (which is what getPrice() and getFinalPrice() do).
                // For simplicity, we won't add new fields to Order.java just for these.
                // You can access them directly from the ResultSet here if needed for reporting.
                // double loadedTotalAmount = rsOrders.getDouble("total_amount");
                // double loadedNetAmount = rsOrders.getDouble("net_amount");


                // Load items for each order (as before)
                try (PreparedStatement pstmtItems = conn.prepareStatement(orderItemSql)) {
                    pstmtItems.setInt(1, orderId);
                    ResultSet rsItems = pstmtItems.executeQuery();
                    while (rsItems.next()) {
                        int menuItemId = rsItems.getInt("menu_item_id");
                        int quantity = rsItems.getInt("quantity");
                        double itemPriceAtOrder = rsItems.getDouble("item_price_at_order");

                        MenuItem item = menuMap.get(menuItemId);
                        String itemName = (item != null) ? item.getName() : "Unknown Item (ID: " + menuItemId + ")";

                        for (int i = 0; i < quantity; i++) {
                            order.addItem(new MenuItem(menuItemId, itemName, itemPriceAtOrder));
                        }
                    }
                }
                orders.add(order);
            }
            System.out.println("Loaded " + orders.size() + " orders.");
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        return orders;
    }

    public static void updateOrderPaymentStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET payment_status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated order " + orderId + " payment status to: " + newStatus);
            } else {
                System.out.println("Order " + orderId + " not found for status update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating order payment status: " + e.getMessage());
        }
    }

    // --- TableBooking operations ---

    public static void saveTableBooking(TableBooking booking) {
        String sql = "INSERT INTO table_bookings (table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, booking.tableType);
            pstmt.setInt(2, booking.tableNumber);
            pstmt.setString(3, booking.customerName);
            pstmt.setString(4, booking.phone);
            pstmt.setInt(5, booking.seats);
            pstmt.setString(6, booking.customerId);
            pstmt.setString(7, booking.paymentStatus);
            pstmt.setDouble(8, booking.bookingFee);
            pstmt.executeUpdate();
            System.out.println("Saved table booking for Customer ID: " + booking.customerId);
        } catch (SQLException e) {
            System.err.println("Error saving table booking: " + e.getMessage());
        }
    }

    public static List<TableBooking> loadTableBookings() {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee FROM table_bookings ORDER BY booking_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TableBooking booking = new TableBooking(
                    rs.getString("table_type"),
                    rs.getInt("table_number"),
                    rs.getString("customer_name"),
                    rs.getString("phone"),
                    rs.getInt("seats"),
                    rs.getString("customer_id"),
                    rs.getDouble("booking_fee")
                );
                booking.paymentStatus = rs.getString("payment_status"); // Set payment status after creation
                bookings.add(booking);
            }
            System.out.println("Loaded " + bookings.size() + " table bookings.");
        } catch (SQLException e) {
            System.err.println("Error loading table bookings: " + e.getMessage());
        }
        return bookings;
    }

    public static void updateTableBookingPaymentStatus(String customerId, String newStatus) {
        String sql = "UPDATE table_bookings SET payment_status = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, customerId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated booking for Customer ID " + customerId + " payment status to: " + newStatus);
            } else {
                System.out.println("Booking for Customer ID " + customerId + " not found for status update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating table booking payment status: " + e.getMessage());
        }
    }
}
