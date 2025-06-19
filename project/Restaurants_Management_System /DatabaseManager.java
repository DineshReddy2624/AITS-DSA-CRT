package application;

import java.sql.*;
import java.time.LocalDateTime;
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

            // Create orders table - UPDATED for enums, amounts, payment method, GST
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                                       "order_id INT PRIMARY KEY," +
                                       "status VARCHAR(50) NOT NULL," +
                                       "payment_status VARCHAR(50) NOT NULL," +
                                       "discount_applied DECIMAL(10, 2)," +
                                       "subtotal_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," + // New: Subtotal
                                       "net_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," + // After discount, before GST
                                       "gst_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +    // New: GST Amount
                                       "final_amount_with_gst DECIMAL(10, 2) NOT NULL DEFAULT 0.00," + // New: Final Amount
                                       "order_time DATETIME," +
                                       "payment_method VARCHAR(50)" +
                                       ")";
            stmt.execute(createOrdersTable);
            System.out.println("Table 'orders' checked/created.");

            // Add new columns to 'orders' if they don't exist
            addColumnIfNotExists(conn, "orders", "subtotal_amount", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00");
            addColumnIfNotExists(conn, "orders", "net_amount", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00");
            addColumnIfNotExists(conn, "orders", "gst_amount", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00");
            addColumnIfNotExists(conn, "orders", "final_amount_with_gst", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00");
            addColumnIfNotExists(conn, "orders", "payment_method", "VARCHAR(50) DEFAULT 'Cash'");


            // Create order_items table (many-to-many relationship)
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                                           "order_item_id INT PRIMARY KEY AUTO_INCREMENT," +
                                           "order_id INT NOT NULL," +
                                           "menu_item_id INT NOT NULL," +
                                           "quantity INT NOT NULL," +
                                           "item_price_at_order DECIMAL(10, 2) NOT NULL," +
                                           "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
                                           "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE" +
                                           ")";
            stmt.execute(createOrderItemsTable);
            System.out.println("Table 'order_items' checked/created.");

            // Create table_bookings table - UPDATED for enums
            String createTableBookingsTable = "CREATE TABLE IF NOT EXISTS table_bookings (" +
                                              "booking_id INT PRIMARY KEY AUTO_INCREMENT," +
                                              "table_type VARCHAR(50) NOT NULL," +
                                              "table_number INT NOT NULL," +
                                              "customer_name VARCHAR(255) NOT NULL," +
                                              "phone VARCHAR(20) NOT NULL," +
                                              "seats INT NOT NULL," +
                                              "customer_id VARCHAR(50) NOT NULL," + // Changed to NOT UNIQUE, customer can have multiple bookings
                                              "payment_status VARCHAR(50) NOT NULL," +
                                              "booking_fee DECIMAL(10, 2) NOT NULL" +
                                              ")";
            stmt.execute(createTableBookingsTable);
            System.out.println("Table 'table_bookings' checked/created.");

            // New: Create users table for login/registration
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                                      "username VARCHAR(255) PRIMARY KEY," + // Username as primary key for uniqueness
                                      "password_hash VARCHAR(255) NOT NULL," +
                                      "created_at DATETIME NOT NULL" +
                                      ")";
            stmt.execute(createUsersTable);
            System.out.println("Table 'users' checked/created.");

            // New: Create feedback table
            String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedback (" +
                                         "feedback_id INT PRIMARY KEY AUTO_INCREMENT," +
                                         "customer_username VARCHAR(255) NOT NULL," +
                                         "rating INT NOT NULL," +
                                         "comments TEXT," +
                                         "feedback_date DATETIME NOT NULL," +
                                         "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE CASCADE" +
                                         ")";
            stmt.execute(createFeedbackTable);
            System.out.println("Table 'feedback' checked/created.");

            // New: Create dish_ratings table
            String createDishRatingsTable = "CREATE TABLE IF NOT EXISTS dish_ratings (" +
                                            "rating_id INT PRIMARY KEY AUTO_INCREMENT," +
                                            "menu_item_id INT NOT NULL," +
                                            "customer_username VARCHAR(255) NOT NULL," +
                                            "rating INT NOT NULL," +
                                            "rating_date DATETIME NOT NULL," +
                                            "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE," +
                                            "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE CASCADE" +
                                            ")";
            stmt.execute(createDishRatingsTable);
            System.out.println("Table 'dish_ratings' checked/created.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // Fatal error, consider throwing a runtime exception or showing a critical alert
        }
    }

    /**
     * Helper method to add a column to a table if it doesn't already exist.
     */
    private static void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'")) {
            if (!rs.next()) {
                String addColumnSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
                conn.createStatement().execute(addColumnSql);
                System.out.println("Added '" + columnName + "' column to '" + tableName + "' table.");
            }
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
     * This version now correctly handles subtotal, net_amount, gst_amount, and final_amount_with_gst.
     * @param order The Order object to save.
     */
    public static void saveOrder(Order order) {
        String orderSql = "INSERT INTO orders (order_id, status, payment_status, discount_applied, subtotal_amount, net_amount, gst_amount, final_amount_with_gst, order_time, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String orderItemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, item_price_at_order) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Calculate amounts from the Order object
            double subtotal = order.getSubtotal();
            double netAmount = order.getFinalPriceBeforeGST();
            double gstAmount = order.getGSTAmount();
            double finalAmountWithGST = order.getTotalWithGST();

            // Save order details
            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql)) {
                pstmtOrder.setInt(1, order.orderId);
                pstmtOrder.setString(2, order.status.getDisplayValue());
                pstmtOrder.setString(3, order.paymentStatus.getDisplayValue());
                pstmtOrder.setDouble(4, order.discountApplied);
                pstmtOrder.setDouble(5, subtotal); // Save subtotal
                pstmtOrder.setDouble(6, netAmount); // Save net amount (after discount, before GST)
                pstmtOrder.setDouble(7, gstAmount); // Save GST amount
                pstmtOrder.setDouble(8, finalAmountWithGST); // Save final amount with GST
                pstmtOrder.setTimestamp(9, new Timestamp(new Date().getTime())); // Current timestamp
                pstmtOrder.setString(10, order.paymentMethod.getDisplayValue());
                pstmtOrder.executeUpdate();
            }

            // Save order items in a batch
            try (PreparedStatement pstmtOrderItem = conn.prepareStatement(orderItemSql)) {
                Map<Integer, Integer> itemQuantities = new HashMap<>();
                Map<Integer, Double> itemPricesAtOrder = new HashMap<>();

                for (MenuItem item : order.items) {
                    itemQuantities.put(item.getId(), itemQuantities.getOrDefault(item.getId(), 0) + 1);
                    itemPricesAtOrder.put(item.getId(), item.getPrice());
                }

                for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
                    pstmtOrderItem.setInt(1, order.orderId);
                    pstmtOrderItem.setInt(2, entry.getKey());
                    pstmtOrderItem.setInt(3, entry.getValue());
                    pstmtOrderItem.setDouble(4, itemPricesAtOrder.get(entry.getKey()));
                    pstmtOrderItem.addBatch();
                }
                pstmtOrderItem.executeBatch();
            }

            conn.commit(); // Commit transaction
            System.out.println("Saved order ID: " + order.orderId + " with final total: " + finalAmountWithGST);
        } catch (SQLException e) {
            System.err.println("Error saving order: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Order save transaction rolled back.");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection after order save: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Loads all orders and their associated items from the database.
     * This version now correctly reconstructs all amounts.
     * @param menuMap A map of MenuItem IDs to MenuItem objects for reconstructing order items.
     * @return A List of Order objects.
     */
    public static List<Order> loadOrders(Map<Integer, MenuItem> menuMap) {
        List<Order> orders = new ArrayList<>();
        String orderSql = "SELECT order_id, status, payment_status, discount_applied, subtotal_amount, net_amount, gst_amount, final_amount_with_gst, payment_method FROM orders ORDER BY order_id";
        String orderItemSql = "SELECT menu_item_id, quantity, item_price_at_order FROM order_items WHERE order_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement();
                 ResultSet rsOrders = stmt.executeQuery(orderSql)) {

                while (rsOrders.next()) {
                    int orderId = rsOrders.getInt("order_id");
                    Order order = new Order(orderId);
                    order.status = OrderStatus.fromString(rsOrders.getString("status"));
                    order.paymentStatus = PaymentStatus.fromString(rsOrders.getString("payment_status"));
                    order.discountApplied = rsOrders.getDouble("discount_applied");
                    // New: Although calculated in Order object, these are loaded for historical accuracy
                    // order.subtotal = rsOrders.getDouble("subtotal_amount"); // No setter for this in Order
                    // order.netAmount = rsOrders.getDouble("net_amount");
                    // order.gstAmount = rsOrders.getDouble("gst_amount");
                    // order.finalAmountWithGST = rsOrders.getDouble("final_amount_with_gst");
                    order.paymentMethod = PaymentMethod.fromString(rsOrders.getString("payment_method"));


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
            }
            conn.commit();
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
            pstmt.setString(1, newStatus.getDisplayValue());
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

    /**
     * Updates the general order status of a specific order.
     * @param orderId The ID of the order to update.
     * @param newStatus The new OrderStatus enum value.
     */
    public static void updateOrderStatus(int orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.getDisplayValue());
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated order " + orderId + " status to: " + newStatus.getDisplayValue());
            } else {
                System.out.println("Order " + orderId + " not found for status update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
    }

    /**
     * Updates the payment method of a specific order.
     * @param orderId The ID of the order to update.
     * @param newMethod The new PaymentMethod enum value.
     */
    public static void updateOrderPaymentMethod(int orderId, PaymentMethod newMethod) {
        String sql = "UPDATE orders SET payment_method = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newMethod.getDisplayValue());
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated order " + orderId + " payment method to: " + newMethod.getDisplayValue());
            } else {
                System.out.println("Order " + orderId + " not found for method update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating order payment method: " + e.getMessage());
        }
    }


    // --- TableBooking operations ---

    /**
     * Saves a new table booking to the database.
     * @param booking The TableBooking object to save.
     */
    public static void saveTableBooking(TableBooking booking) {
        String sql = "INSERT INTO table_bookings (table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, booking.tableType.getDisplayValue());
            pstmt.setInt(2, booking.tableNumber);
            pstmt.setString(3, booking.customerName);
            pstmt.setString(4, booking.phone);
            pstmt.setInt(5, booking.seats);
            pstmt.setString(6, booking.customerId);
            pstmt.setString(7, booking.paymentStatus.getDisplayValue());
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
        String sql = "SELECT booking_id, table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee FROM table_bookings ORDER BY booking_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TableType type = TableType.fromString(rs.getString("table_type"));
                if (type == null) {
                    System.err.println("Warning: Unknown table type '" + rs.getString("table_type") + "' found in DB. Skipping booking.");
                    continue;
                }

                TableBooking booking = new TableBooking(
                    rs.getString("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("phone"),
                    type,
                    rs.getInt("table_number"),
                    rs.getDouble("booking_fee")
                );
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
     * Loads table bookings for a specific customer ID from the database.
     * @param customerId The customer ID to search for.
     * @return A List of TableBooking objects matching the customer ID.
     */
    public static List<TableBooking> getTableBookingsByCustomerId(String customerId) {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT booking_id, table_type, table_number, customer_name, phone, seats, customer_id, payment_status, booking_fee FROM table_bookings WHERE customer_id = ? ORDER BY booking_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableType type = TableType.fromString(rs.getString("table_type"));
                    if (type == null) {
                        System.err.println("Warning: Unknown table type '" + rs.getString("table_type") + "' found for customer " + customerId + ". Skipping booking.");
                        continue;
                    }

                    TableBooking booking = new TableBooking(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        type,
                        rs.getInt("table_number"),
                        rs.getDouble("booking_fee")
                    );
                    booking.paymentStatus = PaymentStatus.fromString(rs.getString("payment_status"));
                    bookings.add(booking);
                }
            }
            System.out.println("Loaded " + bookings.size() + " table bookings for customer ID: " + customerId);
        } catch (SQLException e) {
            System.err.println("Error loading table bookings by customer ID: " + e.getMessage());
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
            pstmt.setString(1, newStatus.getDisplayValue());
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

    // --- User (Login/Registration) Operations ---

    /**
     * Registers a new user in the database.
     * @param username The username for the new user.
     * @param passwordHash The hashed password for the new user.
     * @return true if registration is successful, false otherwise (e.g., username already exists).
     */
    public static boolean registerUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            // Check for duplicate key error (SQLSTATE 23000 for MySQL)
            if (e.getSQLState().startsWith("23")) { // SQLSTATE 23xxx for integrity constraint violation
                System.err.println("Username '" + username + "' already exists.");
            }
            return false;
        }
    }

    /**
     * Authenticates a user.
     * @param username The username to authenticate.
     * @param plainPassword The plain text password to verify.
     * @return The User object if authentication is successful, null otherwise.
     */
    public static User authenticateUser(String username, String plainPassword) {
        String sql = "SELECT username, password_hash FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        return new User(username, storedHash, plainPassword, storedHash);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null; // Authentication failed
    }

    // --- Feedback Operations ---

    /**
     * Saves new customer feedback to the database.
     * @param feedback The Feedback object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean saveFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedback (customer_username, rating, comments, feedback_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feedback.getCustomerUsername());
            pstmt.setInt(2, feedback.getRating());
            pstmt.setString(3, feedback.getComments());
            pstmt.setTimestamp(4, Timestamp.valueOf(feedback.getFeedbackDate()));
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error saving feedback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all feedback entries from the database.
     * @return A list of Feedback objects.
     */
    public static List<Feedback> loadFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT feedback_id, customer_username, rating, comments, feedback_date FROM feedback ORDER BY feedback_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                feedbackList.add(new Feedback(
                    rs.getInt("feedback_id"),
                    rs.getString("customer_username"),
                    rs.getInt("rating"),
                    rs.getString("comments"),
                    rs.getTimestamp("feedback_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading feedback: " + e.getMessage());
        }
        return feedbackList;
    }

    // --- Dish Rating Operations ---

    /**
     * Saves a new dish rating to the database.
     * @param rating The DishRating object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean saveDishRating(DishRating rating) {
        String sql = "INSERT INTO dish_ratings (menu_item_id, customer_username, rating, rating_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating.getMenuItemId());
            pstmt.setString(2, rating.getCustomerUsername());
            pstmt.setInt(3, rating.getRating());
            pstmt.setTimestamp(4, Timestamp.valueOf(rating.getRatingDate()));
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error saving dish rating: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all dish ratings from the database.
     * @return A list of DishRating objects.
     */
    public static List<DishRating> loadDishRatings() {
        List<DishRating> ratingsList = new ArrayList<>();
        String sql = "SELECT rating_id, menu_item_id, customer_username, rating, rating_date FROM dish_ratings ORDER BY rating_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ratingsList.add(new DishRating(
                    rs.getInt("rating_id"),
                    rs.getInt("menu_item_id"),
                    rs.getString("customer_username"),
                    rs.getInt("rating"),
                    rs.getTimestamp("rating_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading dish ratings: " + e.getMessage());
        }
        return ratingsList;
    }

    /**
     * Calculates the average rating for a specific menu item.
     * @param menuItemId The ID of the menu item.
     * @return The average rating, or 0.0 if no ratings exist for the item.
     */
    public static double getAverageDishRating(int menuItemId) {
        String sql = "SELECT AVG(rating) AS average_rating FROM dish_ratings WHERE menu_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("average_rating"); // Returns 0.0 if no ratings
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average dish rating: " + e.getMessage());
        }
        return 0.0;
    }
}
