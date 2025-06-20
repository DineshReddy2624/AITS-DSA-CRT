package application;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.Arrays;

/**
 * Manages all database interactions for the Restaurant Management System.
 * Handles menu items, users, orders, table bookings, feedback, and dish ratings.
 */
public class DatabaseManager {

    // Admin credentials provided by the user
    private static final String ADMIN_USERNAME = "Dinesh Reddy";
    private static final String ADMIN_PASSWORD_PLAIN = "Dinesh@2624"; // This will be hashed before storage

    /**
     * Initializes the database by creating tables if they don't exist,
     * and inserts the default admin user and default menu items if not already present.
     */
    public static void initializeDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // IMPORTANT: For development, the following DROP TABLE statements were used to ensure fresh schema.
            // For persistence across application runs, these should be commented out or removed.
            // DO NOT uncomment these in a production environment unless you have a robust migration strategy.
            // stmt.execute("DROP TABLE IF EXISTS order_items");
            // stmt.execute("DROP TABLE IF EXISTS orders");
            // stmt.execute("DROP TABLE IF EXISTS table_bookings");
            // stmt.execute("DROP TABLE IF EXISTS feedback");
            // stmt.execute("DROP TABLE IF EXISTS dish_ratings");
            // stmt.execute("DROP TABLE IF EXISTS menu_items");
            // stmt.execute("DROP TABLE IF EXISTS users");


            // Create menu_items table - UPDATED for image_url
            // Using CREATE TABLE IF NOT EXISTS to prevent errors if table already exists
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                                          "id INT PRIMARY KEY AUTO_INCREMENT," +
                                          "name VARCHAR(255) NOT NULL UNIQUE," +
                                          "price DOUBLE NOT NULL," +
                                          "image_url VARCHAR(500)" +
                                          ")";
            stmt.execute(createMenuItemsTable);

            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                                      "username VARCHAR(255) PRIMARY KEY," +
                                      "password_hash VARCHAR(255) NOT NULL," +
                                      "full_name VARCHAR(255) NOT NULL," +
                                      "email VARCHAR(255)," +
                                      "phone_number VARCHAR(20)," +
                                      "role VARCHAR(50) NOT NULL" +
                                      ")";
            stmt.execute(createUsersTable);

            // Create orders table
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                                       "order_id INT PRIMARY KEY AUTO_INCREMENT," +
                                       "customer_username VARCHAR(255)," +
                                       "order_time DATETIME NOT NULL," +
                                       "total_amount DOUBLE NOT NULL," +
                                       "status VARCHAR(50) NOT NULL," +
                                       "payment_status VARCHAR(50) NOT NULL," +
                                       "payment_method VARCHAR(50) NOT NULL," +
                                       "discount_applied DOUBLE DEFAULT 0.0," +
                                       "FOREIGN KEY (customer_username) REFERENCES users(username)" +
                                       ")";
            stmt.execute(createOrdersTable);

            // Create order_items table
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                                           "order_item_id INT PRIMARY KEY AUTO_INCREMENT," +
                                           "order_id INT NOT NULL," +
                                           "menu_item_id INT NOT NULL," +
                                           "quantity INT NOT NULL," +
                                           "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
                                           "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
                                           ")";
            stmt.execute(createOrderItemsTable);

            // Create table_bookings table
            String createTableBookingsTable = "CREATE TABLE IF NOT EXISTS table_bookings (" +
                                              "booking_id INT PRIMARY KEY AUTO_INCREMENT," +
                                              "customer_id VARCHAR(255) NOT NULL," +
                                              "customer_name VARCHAR(255) NOT NULL," +
                                              "phone VARCHAR(20) NOT NULL," +
                                              "table_type VARCHAR(50) NOT NULL," +
                                              "table_number INT NOT NULL," +
                                              "seats INT NOT NULL," +
                                              "booking_time DATETIME NOT NULL," +
                                              "duration_minutes INT NOT NULL," +
                                              "booking_fee DOUBLE NOT NULL," +
                                              "payment_status VARCHAR(50) NOT NULL," +
                                              "payment_method VARCHAR(50) NOT NULL," +
                                              "FOREIGN KEY (customer_id) REFERENCES users(username)" +
                                              ")";
            stmt.execute(createTableBookingsTable);

            // Create feedback table
            String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedback (" +
                                         "feedback_id INT PRIMARY KEY AUTO_INCREMENT," +
                                         "customer_username VARCHAR(255) NOT NULL," +
                                         "rating INT NOT NULL," +
                                         "comments TEXT," +
                                         "feedback_date DATETIME NOT NULL," +
                                         "FOREIGN KEY (customer_username) REFERENCES users(username)" +
                                         ")";
            stmt.execute(createFeedbackTable);

            // Create dish_ratings table
            String createDishRatingsTable = "CREATE TABLE IF NOT EXISTS dish_ratings (" +
                                            "rating_id INT PRIMARY KEY AUTO_INCREMENT," +
                                            "menu_item_id INT NOT NULL," +
                                            "customer_username VARCHAR(255) NOT NULL," +
                                            "rating INT NOT NULL," +
                                            "rating_date DATETIME NOT NULL," +
                                            "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)," +
                                            "FOREIGN KEY (customer_username) REFERENCES users(username)" +
                                            ")";
            stmt.execute(createDishRatingsTable);

            // Insert default admin user and menu items only if tables were freshly created or empty
            insertDefaultAdmin(conn);
            insertDefaultMenuItems(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts the default admin user if no users exist.
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void insertDefaultAdmin(Connection conn) throws SQLException {
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkAdminSql)) {
            checkStmt.setString(1, ADMIN_USERNAME);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                String insertAdminSql = "INSERT INTO users (username, password_hash, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAdminSql)) {
                    insertStmt.setString(1, ADMIN_USERNAME);
                    insertStmt.setString(2, PasswordUtil.hashPassword(ADMIN_PASSWORD_PLAIN));
                    insertStmt.setString(3, "Dinesh Reddy");
                    insertStmt.setString(4, "dinesh.admin@example.com");
                    insertStmt.setString(5, "9876543210");
                    insertStmt.setString(6, UserRole.ADMIN.name());
                    insertStmt.executeUpdate();
                    System.out.println("Default admin user inserted.");
                }
            }
        }
    }

    /**
     * Inserts default menu items if the menu_items table is empty.
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void insertDefaultMenuItems(Connection conn) throws SQLException {
        // Check if menu_items table is empty
        String checkMenuItemSql = "SELECT COUNT(*) FROM menu_items";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkMenuItemSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Menu items already exist, skipping default insertion.");
                return; // Items already exist, do not re-insert
            }
        }

        // List of default menu items
        List<MenuItem> defaultItems = Arrays.asList(
                new MenuItem(0, "chicken biryani", 150.00, "https://placehold.co/100x100/FF5733/FFFFFF?text=CB"),
                new MenuItem(0, "mutton biryani", 200.00, "https://placehold.co/100x100/33FF57/FFFFFF?text=MB"),
                new MenuItem(0, "veg biryani", 100.00, "https://placehold.co/100x100/3357FF/FFFFFF?text=VB"),
                new MenuItem(0, "chicken curry", 180.00, "https://placehold.co/100x100/FF33A1/FFFFFF?text=CC"),
                new MenuItem(0, "mutton curry", 220.00, "https://placehold.co/100x100/A133FF/FFFFFF?text=MC"),
                new MenuItem(0, "veg curry", 120.00, "https://placehold.co/100x100/33FFF2/FFFFFF?text=VC"),
                new MenuItem(0, "chicken tikka", 160.00, "https://placehold.co/100x100/FFC133/FFFFFF?text=CT"),
                new MenuItem(0, "mutton tikka", 210.00, "https://placehold.co/100x100/8B33FF/FFFFFF?text=MT"),
                new MenuItem(0, "veg tikka", 130.00, "https://placehold.co/100x100/33FFD4/FFFFFF?text=VT"),
                new MenuItem(0, "chicken kebab", 170.00, "https://placehold.co/100x100/FF338A/FFFFFF?text=CK"),
                new MenuItem(0, "mutton kebab", 230.00, "https://placehold.co/100x100/33A1FF/FFFFFF?text=MK"),
                new MenuItem(0, "veg kebab", 140.00, "https://placehold.co/100x100/57FF33/FFFFFF?text=VK"),
                new MenuItem(0, "soft drink", 50.00, "https://placehold.co/100x100/FF5733/FFFFFF?text=SD"),
                new MenuItem(0, "water", 20.00, "https://placehold.co/100x100/33FF57/FFFFFF?text=W"),
                new MenuItem(0, "salad", 30.00, "https://placehold.co/100x100/3357FF/FFFFFF?text=S"),
                new MenuItem(0, "dessert", 80.00, "https://placehold.co/100x100/FF33A1/FFFFFF?text=D")
        );

        String insertMenuItemSql = "INSERT INTO menu_items (name, price, image_url) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertMenuItemSql)) {
            for (MenuItem item : defaultItems) {
                insertStmt.setString(1, item.getName());
                insertStmt.setDouble(2, item.getPrice());
                insertStmt.setString(3, item.getImageUrl());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
            System.out.println("Default menu items inserted.");
        }
    }

    /**
     * Validates user credentials against the database.
     * @param username The username to validate.
     * @param password The plain text password.
     * @return The User object if credentials are valid, otherwise null.
     */
    public static User validateUser(String username, String password) {
        String sql = "SELECT username, password_hash, full_name, email, phone_number, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    return new User(
                            rs.getString("username"),
                            storedHash, // Password hash is loaded here for completeness, but typically wouldn't be passed around
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            UserRole.fromString(rs.getString("role"))
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a new user with the specified details and role.
     * @param username The unique username.
     * @param password The plain text password.
     * @param fullName The full name of the user.
     * @param email The email address.
     * @param phoneNumber The phone number.
     * @param role The role of the user (e.g., CUSTOMER).
     * @return true if registration is successful, false otherwise.
     */
    public static boolean registerUser(String username, String password, String fullName, String email, String phoneNumber, UserRole role) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password_hash, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Registration failed: Username already exists.");
                    return false; // Username already exists
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, PasswordUtil.hashPassword(password)); // Hash the password
                insertStmt.setString(3, fullName);
                insertStmt.setString(4, email);
                insertStmt.setString(5, phoneNumber);
                insertStmt.setString(6, role.name());
                int rowsAffected = insertStmt.executeUpdate();
                conn.commit(); // Commit transaction
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all menu items from the database.
     * @return A list of MenuItem objects.
     */
    public static List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, name, price, image_url FROM menu_items ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                menuItems.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching menu items: " + e.getMessage());
            e.printStackTrace();
        }
        return menuItems;
    }

    /**
     * Retrieves a single menu item by its ID.
     * @param id The ID of the menu item.
     * @return The MenuItem object if found, otherwise null.
     */
    public static MenuItem getMenuItemById(int id) {
        String sql = "SELECT id, name, price, image_url FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("image_url")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching menu item by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new menu item to the database.
     * @param name The name of the menu item.
     * @param price The price of the menu item.
     * @param imageUrl The URL for the item's image.
     * @return true if the item was added successfully, false otherwise.
     */
    public static boolean addMenuItem(String name, double price, String imageUrl) {
        String sql = "INSERT INTO menu_items (name, price, image_url) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, imageUrl);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing menu item in the database.
     * @param id The ID of the menu item to update.
     * @param name The new name.
     * @param price The new price.
     * @param imageUrl The new image URL.
     * @return true if the item was updated successfully, false otherwise.
     */
    public static boolean updateMenuItem(int id, String name, double price, String imageUrl) {
        String sql = "UPDATE menu_items SET name = ?, price = ?, image_url = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, imageUrl);
            pstmt.setInt(4, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a menu item from the database by its ID.
     * @param id The ID of the menu item to delete.
     * @return true if the item was deleted successfully, false otherwise.
     */
    public static boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the next available order ID.
     * @return The next order ID.
     */
    public static int getNextOrderId() {
        String sql = "SELECT MAX(order_id) FROM orders";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1; // Increment max ID by 1
            }
        } catch (SQLException e) {
            System.err.println("Error getting next order ID: " + e.getMessage());
            e.printStackTrace();
        }
        return 1; // Default starting ID if no orders exist
    }

    /**
     * Adds a new order and its associated items to the database.
     * @param order The Order object to add.
     * @return true if the order and items were added successfully, false otherwise.
     */
    public static boolean addOrder(Order order) {
        Connection conn = null; // Declare conn outside try-with-resources
        String orderSql = "INSERT INTO orders (order_id, customer_username, order_time, total_amount, status, payment_status, payment_method, discount_applied) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String orderItemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Get next order ID and set it to the order object
            int orderId = getNextOrderId();
            order.orderId = orderId;

            try (PreparedStatement orderPstmt = conn.prepareStatement(orderSql)) {
                orderPstmt.setInt(1, order.orderId);
                orderPstmt.setString(2, order.getCustomerUsername());
                orderPstmt.setTimestamp(3, Timestamp.valueOf(order.getOrderTime()));
                orderPstmt.setDouble(4, order.getTotalWithGST());
                orderPstmt.setString(5, order.getStatus().name());
                orderPstmt.setString(6, order.getPaymentStatus().name());
                orderPstmt.setString(7, order.getPaymentMethod().name());
                orderPstmt.setDouble(8, order.getDiscountApplied());
                orderPstmt.executeUpdate();
            }

            // Aggregate quantities for order items
            Map<Integer, Integer> itemQuantities = new HashMap<>();
            for (MenuItem item : order.getItems()) {
                itemQuantities.put(item.getId(), itemQuantities.getOrDefault(item.getId(), 0) + 1);
            }

            try (PreparedStatement orderItemPstmt = conn.prepareStatement(orderItemSql)) {
                for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
                    orderItemPstmt.setInt(1, order.orderId);
                    orderItemPstmt.setInt(2, entry.getKey());
                    orderItemPstmt.setInt(3, entry.getValue());
                    orderItemPstmt.addBatch(); // Add to batch for efficient insertion
                }
                orderItemPstmt.executeBatch(); // Execute all batched inserts
            }

            conn.commit(); // Commit transaction if all successful
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) { // Check if connection is not null before rolling back
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit to true
                    conn.close(); // Close the connection
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves all orders from the database, including their associated menu items.
     * @return A list of Order objects.
     */
    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_username, o.order_time, o.total_amount, o.status, o.payment_status, o.payment_method, o.discount_applied, " +
                     "oi.menu_item_id, oi.quantity FROM orders o JOIN order_items oi ON o.order_id = oi.order_id ORDER BY o.order_id DESC, oi.order_item_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Use a LinkedHashMap to maintain insertion order for orders and avoid duplicates
            Map<Integer, Order> orderMap = new LinkedHashMap<>();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Order order = orderMap.get(orderId);
                if (order == null) {
                    // Create new Order object if not already in map
                    order = new Order(orderId);
                    order.setCustomerUsername(rs.getString("customer_username"));
                    order.setOrderTime(rs.getTimestamp("order_time").toLocalDateTime());
                    order.setTotalWithGST(rs.getDouble("total_amount")); // Set total based on DB value
                    order.setStatus(OrderStatus.fromString(rs.getString("status")));
                    order.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
                    order.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
                    order.setDiscountApplied(rs.getDouble("discount_applied"));
                    orderMap.put(orderId, order);
                }
                // Add menu item to the current order
                int menuItemId = rs.getInt("menu_item_id");
                int quantity = rs.getInt("quantity");
                MenuItem item = getMenuItemById(menuItemId); // Fetch MenuItem details
                if (item != null) {
                    for (int i = 0; i < quantity; i++) {
                        order.addItem(item); // Add multiple times for quantity
                    }
                }
            }
            orders.addAll(orderMap.values()); // Add all unique orders to the list
        } catch (SQLException e) {
            System.err.println("Error fetching all orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Updates the status of an existing order.
     * @param orderId The ID of the order to update.
     * @param newStatus The new OrderStatus.
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateOrderStatus(int orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.name()); // Store enum name
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the payment status of an existing order.
     * @param orderId The ID of the order to update.
     * @param newPaymentStatus The new PaymentStatus.
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateOrderPaymentStatus(int orderId, PaymentStatus newPaymentStatus) {
        String sql = "UPDATE orders SET payment_status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPaymentStatus.name()); // Store enum name
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order payment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the next available table booking ID.
     * @return The next booking ID.
     */
    public static int getNextTableBookingId() {
        String sql = "SELECT MAX(booking_id) FROM table_bookings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error getting next booking ID: " + e.getMessage());
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Adds a new table booking to the database.
     * @param booking The TableBooking object to add.
     * @return true if the booking was added successfully, false otherwise.
     */
    public static boolean addTableBooking(TableBooking booking) {
        Connection conn = null; // Declare conn outside try-with-resources
        String sql = "INSERT INTO table_bookings (booking_id, customer_id, customer_name, phone, table_type, table_number, seats, booking_time, duration_minutes, booking_fee, payment_status, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, getNextTableBookingId());
                pstmt.setString(2, booking.customerId);
                pstmt.setString(3, booking.customerName);
                pstmt.setString(4, booking.phone);
                pstmt.setString(5, booking.tableType.name()); // Store enum name
                pstmt.setInt(6, booking.tableNumber);
                pstmt.setInt(7, booking.seats);
                pstmt.setTimestamp(8, Timestamp.valueOf(booking.getBookingTime()));
                pstmt.setInt(9, booking.getDurationMinutes());
                pstmt.setDouble(10, booking.bookingFee);
                pstmt.setString(11, PaymentStatus.PAID.name()); // Default to PAID
                pstmt.setString(12, PaymentMethod.ONLINE_PAYMENT.name()); // Default to ONLINE_PAYMENT
                int rowsAffected = pstmt.executeUpdate();
                conn.commit();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding table booking: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Checks for available tables given booking criteria.
     * @param bookingStartTime The desired start time for the booking.
     * @param durationMinutes The duration of the booking.
     * @param requiredSeats The minimum number of seats required.
     * @param preferredTableType The preferred table type (can be null for any type).
     * @return A list of available tables (represented as Maps).
     */
    public static List<Map<String, Object>> checkTableAvailability(LocalDateTime bookingStartTime, int durationMinutes, int requiredSeats, TableType preferredTableType) {
        List<Map<String, Object>> availableTables = new ArrayList<>();
        List<Map<String, Object>> allPossibleTables = new ArrayList<>();

        // Define all tables (hardcoded for now, could be fetched from DB)
        allPossibleTables.add(createTableMap(101, TableType.SMALL, 2));
        allPossibleTables.add(createTableMap(102, TableType.SMALL, 2));
        allPossibleTables.add(createTableMap(201, TableType.MEDIUM, 4));
        allPossibleTables.add(createTableMap(202, TableType.MEDIUM, 4));
        allPossibleTables.add(createTableMap(301, TableType.LARGE, 6));
        allPossibleTables.add(createTableMap(302, TableType.LARGE, 6));
        allPossibleTables.add(createTableMap(401, TableType.PRIVATE_DINING, 10));

        // Filter by preferred table type if specified
        if (preferredTableType != null) {
            allPossibleTables = allPossibleTables.stream()
                    .filter(table -> TableType.fromString((String) table.get("tableType")) == preferredTableType)
                    .collect(Collectors.toList());
        }

        LocalDateTime bookingEndTime = bookingStartTime.plusMinutes(durationMinutes);
        List<Integer> occupiedTableNumbers = new ArrayList<>();

        // Check existing PAID bookings for overlaps
        String sql = "SELECT table_number, booking_time, duration_minutes FROM table_bookings WHERE payment_status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PaymentStatus.PAID.name()); // Only consider paid bookings as occupied
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int tableNumber = rs.getInt("table_number");
                LocalDateTime existingBookingStartTime = rs.getTimestamp("booking_time").toLocalDateTime();
                int existingDurationMinutes = rs.getInt("duration_minutes");
                LocalDateTime existingBookingEndLDT = existingBookingStartTime.plusMinutes(existingDurationMinutes);

                // Check for time overlap
                boolean overlaps = (bookingStartTime.isBefore(existingBookingEndLDT) && bookingEndTime.isAfter(existingBookingStartTime));

                if (overlaps) {
                    if (!occupiedTableNumbers.contains(tableNumber)) {
                        occupiedTableNumbers.add(tableNumber);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking table availability: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }

        // Filter all possible tables to find available ones that meet seat requirements
        for (Map<String, Object> table : allPossibleTables) {
            int tableNumber = (Integer) table.get("tableNumber");
            int seats = (Integer) table.get("seats");
            if (!occupiedTableNumbers.contains(tableNumber) && seats >= requiredSeats) {
                availableTables.add(table);
            }
        }
        return availableTables;
    }

    /**
     * Retrieves all table bookings from the database.
     * @return A list of TableBooking objects.
     */
    public static List<TableBooking> getAllTableBookings() {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT booking_id, customer_id, customer_name, phone, table_type, table_number, seats, booking_time, duration_minutes, booking_fee, payment_status, payment_method FROM table_bookings ORDER BY booking_time DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookings.add(new TableBooking(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        TableType.fromString(rs.getString("table_type")), // Convert string to enum
                        rs.getInt("table_number"),
                        rs.getTimestamp("booking_time").toLocalDateTime(),
                        rs.getInt("duration_minutes"),
                        rs.getDouble("booking_fee"),
                        PaymentStatus.fromString(rs.getString("payment_status")), // Convert string to enum
                        PaymentMethod.fromString(rs.getString("payment_method")) // Convert string to enum
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all table bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Retrieves table bookings made by a specific customer.
     * @param customerId The username of the customer.
     * @return A list of TableBooking objects for the given customer.
     */
    public static List<TableBooking> getCustomerTableBookings(String customerId) {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT booking_id, customer_id, customer_name, phone, table_type, table_number, seats, booking_time, duration_minutes, booking_fee, payment_status, payment_method FROM table_bookings WHERE customer_id = ? ORDER BY booking_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookings.add(new TableBooking(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        TableType.fromString(rs.getString("table_type")),
                        rs.getInt("table_number"),
                        rs.getTimestamp("booking_time").toLocalDateTime(),
                        rs.getInt("duration_minutes"),
                        rs.getDouble("booking_fee"),
                        PaymentStatus.fromString(rs.getString("payment_status")),
                        PaymentMethod.fromString(rs.getString("payment_method"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer table bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Updates the payment status of a specific table booking.
     * @param tableNumber The table number of the booking.
     * @param bookingTime The exact booking time.
     * @param newStatus The new PaymentStatus.
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateBookingPaymentStatus(int tableNumber, LocalDateTime bookingTime, PaymentStatus newStatus) {
        String sql = "UPDATE table_bookings SET payment_status = ? WHERE table_number = ? AND booking_time = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setInt(2, tableNumber);
            pstmt.setTimestamp(3, Timestamp.valueOf(bookingTime));
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating booking payment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to create a map representing a table.
     */
    private static Map<String, Object> createTableMap(int tableNumber, TableType type, int seats) {
        Map<String, Object> table = new HashMap<>();
        table.put("tableNumber", tableNumber);
        table.put("tableType", type.getDisplayValue()); // Store display value
        table.put("seats", seats);
        return table;
    }

    /**
     * Adds a new customer feedback entry to the database.
     * @param customerUsername The username of the customer submitting feedback.
     * @param rating The rating (1-5).
     * @param comments Any additional comments.
     * @return true if feedback was added successfully, false otherwise.
     */
    public static boolean addFeedback(String customerUsername, int rating, String comments) {
        String sql = "INSERT INTO feedback (customer_username, rating, comments, feedback_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerUsername);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comments);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Record current timestamp
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all customer feedback entries from the database.
     * @return A list of Feedback objects.
     */
    public static List<Feedback> getAllFeedback() {
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
            System.err.println("Error fetching all feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return feedbackList;
    }

    /**
     * Adds a new dish rating to the database.
     * @param menuItemId The ID of the menu item being rated.
     * @param customerUsername The username of the customer who gave the rating.
     * @param rating The rating value (1-5).
     * @return true if the rating was added successfully, false otherwise.
     */
    public static boolean addDishRating(int menuItemId, String customerUsername, int rating) {
        String sql = "INSERT INTO dish_ratings (menu_item_id, customer_username, rating, rating_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            pstmt.setString(2, customerUsername);
            pstmt.setInt(3, rating);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Record current timestamp
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding dish rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all dish ratings from the database.
     * @return A list of DishRating objects.
     */
    public static List<DishRating> getAllDishRatings() {
        List<DishRating> dishRatings = new ArrayList<>();
        String sql = "SELECT rating_id, menu_item_id, customer_username, rating, rating_date FROM dish_ratings ORDER BY rating_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dishRatings.add(new DishRating(
                        rs.getInt("rating_id"),
                        rs.getInt("menu_item_id"),
                        rs.getString("customer_username"),
                        rs.getInt("rating"),
                        rs.getTimestamp("rating_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all dish ratings: " + e.getMessage());
            e.printStackTrace();
        }
        return dishRatings;
    }

    /**
     * Retrieves all users from the database.
     * @return A list of User objects.
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, password_hash, full_name, email, phone_number, role FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("password_hash"), // Include hash for internal operations if needed
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        UserRole.fromString(rs.getString("role")) // Convert string to enum
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Updates the role of an existing user.
     * @param username The username of the user to update.
     * @param newRole The new UserRole for the user.
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateUserRole(String username, UserRole newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.name()); // Store enum name
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
