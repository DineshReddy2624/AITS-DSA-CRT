package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
public class DatabaseManager {
    public static void initializeDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create menu_items table
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                                          "id INT PRIMARY KEY," +
                                          "name VARCHAR(255) NOT NULL," +
                                          "price DECIMAL(10, 2) NOT NULL" +
                                          ")";
            stmt.execute(createMenuItemsTable);
            System.out.println("Table 'menu_items' checked/created.");

            // Create orders table - UPDATED for enums and amounts
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                                       "order_id INT PRIMARY KEY," +
                                       "status VARCHAR(50) NOT NULL," + // Will store OrderStatus enum string
                                       "payment_status VARCHAR(50) NOT NULL," + // Will store PaymentStatus enum string
                                       "discount_applied DECIMAL(10, 2)," +
                                       "total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +
                                       "net_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +
                                       "order_time DATETIME" +
                                       ")";
            stmt.execute(createOrdersTable);
            System.out.println("Table 'orders' checked/created.");

            // Create order_items table (many-to-many relationship)
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                                           "order_item_id INT PRIMARY KEY AUTO_INCREMENT," +
                                           "order_id INT NOT NULL," +
                                           "menu_item_id INT NOT NULL," +
                                           "quantity INT NOT NULL," +
                                           "item_price_at_order DECIMAL(10, 2) NOT NULL," +
                                           "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," + // Add ON DELETE CASCADE
                                           "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE" + // Add ON DELETE CASCADE
                                           ")";
            stmt.execute(createOrderItemsTable);
            System.out.println("Table 'order_items' checked/created.");

            // Create table_bookings table - UPDATED for enums
            String createTableBookingsTable = "CREATE TABLE IF NOT EXISTS table_bookings (" +
                                              "booking_id INT PRIMARY KEY AUTO_INCREMENT," +
                                              "table_type VARCHAR(50) NOT NULL," + // Will store TableType enum string
                                              "table_number INT NOT NULL," +
                                              "customer_name VARCHAR(255) NOT NULL," +
                                              "phone VARCHAR(20) NOT NULL," +
                                              "seats INT NOT NULL," + // Redundant with table_type, but kept for simplicity of previous structure
                                              "customer_id VARCHAR(50) NOT NULL UNIQUE," +
                                              "payment_status VARCHAR(50) NOT NULL," + // Will store PaymentStatus enum string
                                              "booking_fee DECIMAL(10, 2) NOT NULL" +
                                              ")";
            stmt.execute(createTableBookingsTable);
            System.out.println("Table 'table_bookings' checked/created.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // Fatal error, consider throwing a runtime exception or showing a critical alert
        }
    }

    // --- MenuItem operations ---

    /**
     * Saves a new menu item or updates an existing one if the ID already exists.
     * @param item The MenuItem object to save.
     */
    public static void saveMenuItem(MenuItem item) {
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

    /**
     * Loads all menu items from the database.
     * @return A List of MenuItem objects.
     */
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

    /**
     * Deletes a menu item by its ID.
     * @param id The ID of the menu item to delete.
     */
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

    /**
     * Saves an order and its associated items to the database within a transaction.
     * @param order The Order object to save.
     */
    public static void saveOrder(Order order) {
        // Corrected INSERT statement to use enum display values
        String orderSql = "INSERT INTO orders (order_id, status, payment_status, discount_applied, total_amount, net_amount, order_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String orderItemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, item_price_at_order) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Calculate amounts from the Order object
            double calculatedTotalAmount = order.getPrice();
            double calculatedNetAmount = order.getFinalPrice();

            // Save order details
            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql)) {
                pstmtOrder.setInt(1, order.orderId);
                pstmtOrder.setString(2, order.status.getDisplayValue()); // Save enum as string
                pstmtOrder.setString(3, order.paymentStatus.getDisplayValue()); // Save enum as string
                pstmtOrder.setDouble(4, order.discountApplied);
                pstmtOrder.setDouble(5, calculatedTotalAmount);
                pstmtOrder.setDouble(6, calculatedNetAmount);
                pstmtOrder.setTimestamp(7, new Timestamp(new Date().getTime())); // Current timestamp
                pstmtOrder.executeUpdate();
            }

            // Save order items in a batch
            try (PreparedStatement pstmtOrderItem = conn.prepareStatement(orderItemSql)) {
                // Group identical items to save correct quantity per unique item
                Map<Integer, Integer> itemQuantities = new HashMap<>();
                Map<Integer, Double> itemPricesAtOrder = new HashMap<>(); // Store price at time of order

                for (MenuItem item : order.items) {
                    itemQuantities.put(item.getId(), itemQuantities.getOrDefault(item.getId(), 0) + 1);
                    // Ensure we store the price from the MenuItem object, which might be different
                    // from the current menu price if menu prices change.
                    itemPricesAtOrder.put(item.getId(), item.getPrice());
                }

                for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
                    pstmtOrderItem.setInt(1, order.orderId);
                    pstmtOrderItem.setInt(2, entry.getKey());
                    pstmtOrderItem.setInt(3, entry.getValue());
                    pstmtOrderItem.setDouble(4, itemPricesAtOrder.get(entry.getKey())); // Use stored price at order time
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

    /**
     * Loads all orders and their associated items from the database.
     * @param menuMap A map of MenuItem IDs to MenuItem objects for reconstructing order items.
     * @return A List of Order objects.
     */
    public static List<Order> loadOrders(Map<Integer, MenuItem> menuMap) {
        List<Order> orders = new ArrayList<>();
        String orderSql = "SELECT order_id, status, payment_status, discount_applied FROM orders ORDER BY order_id";
        String orderItemSql = "SELECT menu_item_id, quantity, item_price_at_order FROM order_items WHERE order_id = ?";

        Connection conn = null; // Declare connection outside try-with-resources to use in finally
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction for loading orders and their items

            try (Statement stmt = conn.createStatement();
                 ResultSet rsOrders = stmt.executeQuery(orderSql)) {

                while (rsOrders.next()) {
                    int orderId = rsOrders.getInt("order_id");
                    Order order = new Order(orderId);
                    // Convert string from DB back to enum
                    order.status = OrderStatus.fromString(rsOrders.getString("status"));
                    order.paymentStatus = PaymentStatus.fromString(rsOrders.getString("payment_status"));
                    order.discountApplied = rsOrders.getDouble("discount_applied");

                    // Load items for each order
                    try (PreparedStatement pstmtItems = conn.prepareStatement(orderItemSql)) {
                        pstmtItems.setInt(1, orderId);
                        ResultSet rsItems = pstmtItems.executeQuery();
                        while (rsItems.next()) {
                            int menuItemId = rsItems.getInt("menu_item_id");
                            int quantity = rsItems.getInt("quantity");
                            double itemPriceAtOrder = rsItems.getDouble("item_price_at_order");

                            // Reconstruct MenuItem using the menuMap, but use the price stored at order time
                            MenuItem item = menuMap.get(menuItemId);
                            String itemName = (item != null) ? item.getName() : "Unknown Item (ID: " + menuItemId + ")";

                            // Add each item individually to the order based on quantity
                            for (int i = 0; i < quantity; i++) {
                                order.addItem(new MenuItem(menuItemId, itemName, itemPriceAtOrder));
                            }
                        }
                    } // rsItems and pstmtItems are closed here
                    orders.add(order);
                }
            } // rsOrders and stmt are closed here
            conn.commit(); // Commit transaction
            System.out.println("Loaded " + orders.size() + " orders.");
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Order load transaction rolled back.");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction during load: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection after order load: " + e.getMessage());
                }
            }
        }
        return orders;
    }

    /**
     * Updates the payment status of a specific order.
     * @param orderId The ID of the order to update.
     * @param newStatus The new PaymentStatus enum value.
     */
    public static void updateOrderPaymentStatus(int orderId, PaymentStatus newStatus) {
        String sql = "UPDATE orders SET payment_status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.getDisplayValue()); // Save enum as string
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated order " + orderId + " payment status to: " + newStatus.getDisplayValue());
            } else {
                System.out.println("Order " + orderId + " not found for status update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating order payment status: " + e.getMessage());
        }
    }

    // --- TableBooking operations ---

    /**
     * Saves a new table booking to the database.
     * @param booking The TableBooking object to save.
     */
    public static void saveTableBooking(TableBooking booking) {
        // Corrected SQL statement to match TableBooking constructor and enum usage
        String sql = "INSERT INTO table_bookings (table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, booking.tableType.getDisplayValue()); // Save enum as string
            pstmt.setInt(2, booking.tableNumber);
            pstmt.setString(3, booking.customerName);
            pstmt.setString(4, booking.phone);
            pstmt.setInt(5, booking.seats); // This is derived from tableType.getSeats() in TableBooking constructor
            pstmt.setString(6, booking.customerId);
            pstmt.setString(7, booking.paymentStatus.getDisplayValue()); // Save enum as string
            pstmt.setDouble(8, booking.bookingFee);
            pstmt.executeUpdate();
            System.out.println("Saved table booking for Customer ID: " + booking.customerId);
        } catch (SQLException e) {
            System.err.println("Error saving table booking: " + e.getMessage());
        }
    }

    /**
     * Loads all table bookings from the database.
     * @return A List of TableBooking objects.
     */
    public static List<TableBooking> loadTableBookings() {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee FROM table_bookings ORDER BY booking_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Convert string from DB back to enum
                TableType type = TableType.fromString(rs.getString("table_type"));
                if (type == null) {
                    System.err.println("Warning: Unknown table type '" + rs.getString("table_type") + "' found in DB. Skipping booking.");
                    continue; // Skip this booking if type is invalid
                }

                // Reconstruct TableBooking using the updated constructor
                TableBooking booking = new TableBooking(
                    rs.getString("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("phone"),
                    type, // Use the TableType enum
                    rs.getInt("table_number"),
                    rs.getDouble("booking_fee")
                );
                // Set payment status separately
                booking.paymentStatus = PaymentStatus.fromString(rs.getString("payment_status"));
                bookings.add(booking);
            }
            System.out.println("Loaded " + bookings.size() + " table bookings.");
        } catch (SQLException e) {
            System.err.println("Error loading table bookings: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Updates the payment status of a specific table booking.
     * @param customerId The customer ID associated with the booking to update.
     * @param newStatus The new PaymentStatus enum value.
     */
    public static void updateTableBookingPaymentStatus(String customerId, PaymentStatus newStatus) {
        String sql = "UPDATE table_bookings SET payment_status = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.getDisplayValue()); // Save enum as string
            pstmt.setString(2, customerId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated booking for Customer ID " + customerId + " payment status to: " + newStatus.getDisplayValue());
            } else {
                System.out.println("Booking for Customer ID " + customerId + " not found for status update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating table booking payment status: " + e.getMessage());
        }
    }

	public static void updateOrderStatus(int orderId, OrderStatus selectedOrderStatus) {
		// TODO Auto-generated method stub
		
	}
}
