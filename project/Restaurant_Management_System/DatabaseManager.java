// DatabaseManager.java
package application;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; 
import java.util.HashSet; 
import java.util.stream.Collectors;

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
     * IMPORTANT: For development, this version includes DROP TABLE statements for specific tables
     * to ensure schema updates are applied. In production, consider ALTER TABLE for schema evolution.
     */
    public static void initializeDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // --- Drop tables if they exist (for development/testing to ensure fresh schema) ---
            // Drop dependent tables first to avoid foreign key constraints issues
            System.out.println("Attempting to drop existing tables for schema update...");
            try { stmt.execute("DROP TABLE IF EXISTS table_bookings"); System.out.println("Dropped 'table_bookings'."); } catch (SQLException e) { System.err.println("Error dropping table_bookings: " + e.getMessage()); }
            try { stmt.execute("DROP TABLE IF EXISTS order_items"); System.out.println("Dropped 'order_items'."); } catch (SQLException e) { System.err.println("Error dropping order_items: " + e.getMessage()); }
            try { stmt.execute("DROP TABLE IF EXISTS orders"); System.out.println("Dropped 'orders'."); } catch (SQLException e) { System.err.println("Error dropping orders: " + e.getMessage()); }
            try { stmt.execute("DROP TABLE IF EXISTS dish_ratings"); System.out.println("Dropped 'dish_ratings'."); } catch (SQLException e) { System.err.println("Error dropping dish_ratings: " + e.getMessage()); }
            try { stmt.execute("DROP TABLE IF EXISTS feedback"); System.out.println("Dropped 'feedback'."); } catch (SQLException e) { System.err.println("Error dropping feedback: " + e.getMessage()); }
            try { stmt.execute("DROP TABLE IF EXISTS menu_items"); System.out.println("Dropped 'menu_items'."); } catch (SQLException e) { System.err.println("Error dropping menu_items: " + e.getMessage()); }
            try { stmt.execute("DROP TABLE IF EXISTS users"); System.out.println("Dropped 'users'."); } catch (SQLException e) { System.err.println("Error dropping users: " + e.getMessage()); }
            System.out.println("Finished dropping tables (if they existed).");


            // Create menu_items table - UPDATED for image_url
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                                          "id INT PRIMARY KEY," +
                                          "name VARCHAR(255) NOT NULL," +
                                          "price DECIMAL(10, 2) NOT NULL," +
                                          "image_url VARCHAR(255)" + // Added image_url column
                                          ")";
            stmt.execute(createMenuItemsTable);
            System.out.println("Table 'menu_items' checked/created.");

            // Insert default menu items if the table is empty
            String checkMenuItems = "SELECT COUNT(*) FROM menu_items";
            try (ResultSet rs = stmt.executeQuery(checkMenuItems)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Inserting default menu items...");
                    String insertItem1 = "INSERT INTO menu_items (id, name, price, image_url) VALUES (1, 'Margherita Pizza', 350.00, 'https://placehold.co/100x100/FF5733/FFFFFF?text=Pizza')";
                    String insertItem2 = "INSERT INTO menu_items (id, name, price, image_url) VALUES (2, 'Chicken Biryani', 280.00, 'https://placehold.co/100x100/33FF57/FFFFFF?text=Biryani')";
                    String insertItem3 = "INSERT INTO menu_items (id, name, price, image_url) VALUES (3, 'Veg Burger', 150.00, 'https://placehold.co/100x100/3357FF/FFFFFF?text=Burger')";
                    String insertItem4 = "INSERT INTO menu_items (id, name, price, image_url) VALUES (4, 'French Fries', 90.00, 'https://placehold.co/100x100/A0A0A0/FFFFFF?text=Fries')";
                    String insertItem5 = "INSERT INTO menu_items (id, name, price, image_url) VALUES (5, 'Coca-Cola', 60.00, 'https://placehold.co/100x100/C0C0C0/FFFFFF?text=Cola')";

                    stmt.execute(insertItem1);
                    stmt.execute(insertItem2);
                    stmt.execute(insertItem3);
                    stmt.execute(insertItem4);
                    stmt.execute(insertItem5);
                    System.out.println("Default menu items inserted.");
                } else {
                    System.out.println("Menu items already present.");
                }
            }


            // Create users table - UPDATED for full profile and role
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                                      "username VARCHAR(255) PRIMARY KEY," +
                                      "password_hash VARCHAR(255) NOT NULL," +
                                      "full_name VARCHAR(255) NOT NULL," +
                                      "email VARCHAR(255)," +
                                      "phone_number VARCHAR(20)," +
                                      "role VARCHAR(50) NOT NULL" + // 'Admin' or 'Customer'
                                      ")";
            stmt.execute(createUsersTable);
            System.out.println("Table 'users' checked/created.");

            // Insert default admin user if not exists
            if (!checkIfUserExists(ADMIN_USERNAME)) {
                System.out.println("Inserting default admin user...");
                String hashedPassword = PasswordUtil.hashPassword(ADMIN_PASSWORD_PLAIN);
                String insertAdminSql = "INSERT INTO users (username, password_hash, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
                    pstmt.setString(1, ADMIN_USERNAME);
                    pstmt.setString(2, hashedPassword);
                    pstmt.setString(3, "Dinesh Reddy (Admin)"); // Full name for admin
                    pstmt.setString(4, "admin@restaurant.com");
                    pstmt.setString(5, "9876543210");
                    pstmt.setString(6, UserRole.ADMIN.toString());
                    pstmt.executeUpdate();
                    System.out.println("Default admin user '" + ADMIN_USERNAME + "' inserted.");
                }
            }

            // Create orders table
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                                       "id INT AUTO_INCREMENT PRIMARY KEY," +
                                       "customer_username VARCHAR(255) NOT NULL," + // Link to users table
                                       "status VARCHAR(50) NOT NULL," + // PENDING, PREPARING, DELIVERED, CANCELLED
                                       "payment_status VARCHAR(50) NOT NULL," + // PENDING, PAID, REFUNDED
                                       "discount_applied DECIMAL(10, 2) DEFAULT 0.00," +
                                       "payment_method VARCHAR(50) NOT NULL," +
                                       "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + // When the order was placed
                                       "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE CASCADE" +
                                       ")";
            stmt.execute(createOrdersTable);
            System.out.println("Table 'orders' checked/created.");

            // Create order_items table (many-to-many relationship) - ADDED price_at_order
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                                           "order_id INT," +
                                           "menu_item_id INT," +
                                           "quantity INT NOT NULL DEFAULT 1," +
                                           "price_at_order DECIMAL(10, 2) NOT NULL," + // NEW: Price at the time of order
                                           "PRIMARY KEY (order_id, menu_item_id)," +
                                           "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE," +
                                           "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE" +
                                           ")";
            stmt.execute(createOrderItemsTable);
            System.out.println("Table 'order_items' checked/created.");

            // Create feedback table
            String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedback (" +
                                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                                         "customer_username VARCHAR(255) NOT NULL," +
                                         "rating INT NOT NULL," + // 1-5 stars
                                         "comments TEXT," +
                                         "feedback_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                         "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE CASCADE" +
                                         ")";
            stmt.execute(createFeedbackTable);
            System.out.println("Table 'feedback' checked/created.");

            // Create dish_ratings table
            String createDishRatingsTable = "CREATE TABLE IF NOT EXISTS dish_ratings (" +
                                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                                            "menu_item_id INT NOT NULL," +
                                            "customer_username VARCHAR(255) NOT NULL," +
                                            "rating INT NOT NULL," + // 1-5 stars
                                            "rating_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                            "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE," +
                                            "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE CASCADE" +
                                            ")";
            stmt.execute(createDishRatingsTable);
            System.out.println("Table 'dish_ratings' checked/created.");

            // Create table_bookings table - ADDED booking_time and duration_minutes
            String createTableBookingsTable = "CREATE TABLE IF NOT EXISTS table_bookings (" +
                                              "customer_id VARCHAR(255) NOT NULL," + // Link to users table username
                                              "customer_name VARCHAR(255) NOT NULL," +
                                              "phone VARCHAR(20)," +
                                              "table_type VARCHAR(50) NOT NULL," +
                                              "table_number INT NOT NULL," +
                                              "seats INT NOT NULL," +
                                              "booking_fee DECIMAL(10, 2) NOT NULL," +
                                              "payment_status VARCHAR(50) NOT NULL," + // PENDING, PAID, REFUNDED
                                              "booking_time TIMESTAMP NOT NULL," +     // NEW: Date and time of booking
                                              "duration_minutes INT NOT NULL," +       // NEW: Duration of booking in minutes
                                              "PRIMARY KEY (customer_id, table_number, booking_time)," + // Composite PK including booking_time
                                              "FOREIGN KEY (customer_id) REFERENCES users(username) ON DELETE CASCADE" +
                                              ")";
            stmt.execute(createTableBookingsTable);
            System.out.println("Table 'table_bookings' checked/created.");


        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user exists in the database.
     * @param username The username to check.
     * @return true if the user exists, false otherwise.
     */
    public static boolean checkIfUserExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registers a new user in the database.
     * @param user The User object containing new user details.
     * @return true if registration is successful, false otherwise.
     */
    public static boolean registerUser(User user) { // Removed plainPassword as User object already has hashed password
        if (checkIfUserExists(user.getUsername())) {
            System.err.println("Registration failed: Username already exists.");
            return false;
        }

        // The User object should already have the hashed password
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
             System.err.println("Registration failed: Password hash is missing.");
             return false;
        }

        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash()); // Use hashed password from User object
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhoneNumber());
            pstmt.setString(6, user.getRole().toString());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates user credentials for login.
     * @param username The username entered by the user.
     * @param plainPassword The plain text password entered by the user.
     * @return The User object if credentials are valid, null otherwise.
     */
    public static User validateUser(String username, String plainPassword) {
        String sql = "SELECT username, password_hash, full_name, email, phone_number, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                    // Password matches, create and return User object
                    return new User(
                        rs.getString("username"),
                        storedHash, // Include password hash for completeness, though not strictly needed for this constructor
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
        return null; // Invalid credentials or error
    }

    /**
     * Loads all users from the database.
     * @return A list of User objects.
     */
    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, password_hash, full_name, email, phone_number, role FROM users"; // Select password_hash too
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                    rs.getString("username"),
                    rs.getString("password_hash"), // Get password_hash
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    UserRole.fromString(rs.getString("role")) // Reconstruct UserRole
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Deletes a user from the database.
     * @param username The username of the user to delete.
     * @return true if successful, false otherwise.
     */
    public static boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a user's profile information (full name, email, phone number).
     * @param user The User object with updated details (username used for identification).
     * @return true if update is successful, false otherwise.
     */
    public static boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setString(4, user.getUsername());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Loads all menu items from the database.
     * @return A list of MenuItem objects.
     */
    public static List<MenuItem> getMenuItems() { 
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
            System.err.println("Error loading menu items: " + e.getMessage());
            e.printStackTrace();
        }
        return menuItems;
    }

    /**
     * Saves a menu item to the database (inserts if new, updates if exists).
     * @param item The MenuItem object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean addMenuItem(MenuItem item) {
        String checkSql = "SELECT COUNT(*) FROM menu_items WHERE id = ?";
        String upsertSql;
        try (Connection conn = DBConnection.getConnection()) {
            // Check if item exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, item.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Item exists, update it
                    upsertSql = "UPDATE menu_items SET name = ?, price = ?, image_url = ? WHERE id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
                        pstmt.setString(1, item.getName());
                        pstmt.setDouble(2, item.getPrice());
                        pstmt.setString(3, item.getImageUrl());
                        pstmt.setInt(4, item.getId());
                        pstmt.executeUpdate();
                        return true;
                    }
                } else {
                    // Item does not exist, insert it
                    upsertSql = "INSERT INTO menu_items (id, name, price, image_url) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
                        pstmt.setInt(1, item.getId());
                        pstmt.setString(2, item.getName());
                        pstmt.setDouble(3, item.getPrice());
                        pstmt.setString(4, item.getImageUrl());
                        pstmt.executeUpdate();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a menu item from the database.
     * @param id The ID of the menu item to delete.
     * @return true if successful, false otherwise.
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
     * @return The next available order ID.
     */
    public static int getNextAvailableOrderId() {
        String sql = "SELECT MAX(id) FROM orders";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                // If there are no orders, MAX(id) will return 0, so add 1 to start from 1.
                // If there are orders, it returns the max ID, so add 1 for the next ID.
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error getting next order ID: " + e.getMessage());
            e.printStackTrace();
        }
        return 1; // Default to 1 if no orders exist or on error
    }

    /**
     * Saves a new order to the database, including its items.
     * @param order The Order object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean addOrder(Order order) {
        String insertOrderSql = "INSERT INTO orders (id, customer_username, status, payment_status, discount_applied, payment_method, order_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";

        Connection conn = null; 
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into orders table
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql)) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setString(2, order.getCustomerUsername());
                pstmt.setString(3, order.getStatus().toString());
                pstmt.setString(4, order.getPaymentStatus().toString());
                pstmt.setDouble(5, order.getDiscountApplied());
                pstmt.setString(6, order.getPaymentMethod().toString());
                pstmt.setTimestamp(7, Timestamp.valueOf(order.getOrderTime())); // Convert LocalDateTime
                pstmt.executeUpdate();
            }

            // 2. Insert into order_items table for each item in the order
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderItemSql)) {
                // Group items by ID to handle quantity, and get the price for price_at_order
                Map<Integer, Long> itemCounts = order.getItems().stream()
                    .collect(Collectors.groupingBy(MenuItem::getId, Collectors.counting()));

                for (Map.Entry<Integer, Long> entry : itemCounts.entrySet()) {
                    int menuItemId = entry.getKey();
                    int quantity = entry.getValue().intValue();
                    // Retrieve the MenuItem to get its current price
                    MenuItem item = order.getItems().stream()
                                         .filter(mi -> mi.getId() == menuItemId)
                                         .findFirst()
                                         .orElse(null);

                    if (item != null) {
                        pstmt.setInt(1, order.getOrderId());
                        pstmt.setInt(2, menuItemId);
                        pstmt.setInt(3, quantity);
                        pstmt.setDouble(4, item.getPrice()); // Set price_at_order
                        pstmt.addBatch();
                    } else {
                        System.err.println("Warning: Menu item with ID " + menuItemId + " not found in order items list. Skipping.");
                    }
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            System.out.println("Order " + order.getOrderId() + " saved successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) { 
                    System.err.println("Attempting to rollback transaction.");
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close(); // Close the connection
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Loads all orders from the database.
     * @param menuMap A map of MenuItem ID to MenuItem object for reconstructing order items.
     * @return A list of Order objects.
     */
    public static List<Order> getAllOrders(Map<Integer, MenuItem> menuMap) { 
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.customer_username, o.status, o.payment_status, o.discount_applied, o.payment_method, o.order_time, " +
                     "oi.menu_item_id, oi.quantity, oi.price_at_order " + 
                     "FROM orders o LEFT JOIN order_items oi ON o.id = oi.order_id ORDER BY o.id, oi.menu_item_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            Map<Integer, Order> orderMap = new HashMap<>();
            ResultSet rs = stmt.executeQuery(sql); 

            while (rs.next()) {
                int orderId = rs.getInt("id"); 
                Order order = orderMap.get(orderId);

                if (order == null) {
                    // Create new Order object
                    order = new Order(orderId);
                    order.setCustomerUsername(rs.getString("customer_username"));
                    order.setStatus(OrderStatus.fromString(rs.getString("status")));
                    order.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
                    order.setDiscountApplied(rs.getDouble("discount_applied"));
                    order.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
                    Timestamp orderTimestamp = rs.getTimestamp("order_time");
                    if (orderTimestamp != null) {
                        order.setOrderTime(orderTimestamp.toLocalDateTime());
                    }
                    orders.add(order);
                    orderMap.put(orderId, order);
                }

                int menuItemId = rs.getInt("menu_item_id");
                int quantity = rs.getInt("quantity");

                if (menuItemId != 0 && menuMap.containsKey(menuItemId)) { 
                    MenuItem item = menuMap.get(menuItemId);
                    for (int i = 0; i < quantity; i++) {
                        order.addItem(item);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading all orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Loads orders placed by a specific customer.
     * @param customerUsername The username of the customer.
     * @param menuMap A map of MenuItem ID to MenuItem object.
     * @return A list of Order objects for the given customer.
     */
    public static List<Order> loadOrdersByCustomer(String customerUsername, Map<Integer, MenuItem> menuMap) {
        List<Order> customerOrders = new ArrayList<>();
        String sql = "SELECT o.id, o.customer_username, o.status, o.payment_status, o.discount_applied, o.payment_method, o.order_time, " +
                     "oi.menu_item_id, oi.quantity, oi.price_at_order " + 
                     "FROM orders o LEFT JOIN order_items oi ON o.id = oi.order_id " +
                     "WHERE o.customer_username = ? ORDER BY o.order_time DESC, o.id, oi.menu_item_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerUsername);
            ResultSet rs = pstmt.executeQuery();

            Map<Integer, Order> orderMap = new HashMap<>();

            while (rs.next()) {
                int orderId = rs.getInt("id"); 
                Order order = orderMap.get(orderId);

                if (order == null) {
                    order = new Order(orderId);
                    order.setCustomerUsername(rs.getString("customer_username"));
                    order.setStatus(OrderStatus.fromString(rs.getString("status")));
                    order.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
                    order.setDiscountApplied(rs.getDouble("discount_applied"));
                    order.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
                    Timestamp orderTimestamp = rs.getTimestamp("order_time");
                    if (orderTimestamp != null) {
                        order.setOrderTime(orderTimestamp.toLocalDateTime());
                    }
                    customerOrders.add(order);
                    orderMap.put(orderId, order);
                }

                int menuItemId = rs.getInt("menu_item_id");
                int quantity = rs.getInt("quantity");

                if (menuItemId != 0 && menuMap.containsKey(menuItemId)) { 
                    MenuItem item = menuMap.get(menuItemId);
                    for (int i = 0; i < quantity; i++) {
                        order.addItem(item);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading orders by customer: " + e.getMessage());
            e.printStackTrace();
        }
        return customerOrders;
    }


    /**
     * Updates the status of an existing order.
     * @param orderId The ID of the order to update.
     * @param newStatus The new order status.
     * @return true if successful, false otherwise.
     */
    public static boolean updateOrderStatus(int orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.toString());
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
     * @param newPaymentStatus The new payment status.
     * @return true if successful, false otherwise.
     */
    public static boolean updateOrderPaymentStatus(int orderId, PaymentStatus newPaymentStatus) {
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPaymentStatus.toString());
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
     * Updates the payment method of an existing order.
     * @param orderId The ID of the order to update.
     * @param newPaymentMethod The new payment method.
     * @return true if successful, false otherwise.
     */
    public static boolean updateOrderPaymentMethod(int orderId, PaymentMethod newPaymentMethod) {
        String sql = "UPDATE orders SET payment_method = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPaymentMethod.toString());
            pstmt.setInt(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order payment method: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an order and its associated items from the database.
     * Uses CASCADE DELETE from foreign keys.
     * @param orderId The ID of the order to delete.
     * @return true if successful, false otherwise.
     */
    public static boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Saves a new customer feedback entry to the database.
     * @param feedback The Feedback object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean addFeedback(Feedback feedback) {
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads all feedback entries from the database.
     * @return A list of Feedback objects.
     */
    public static List<Feedback> getAllFeedback() { 
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT id, customer_username, rating, comments, feedback_date FROM feedback ORDER BY feedback_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                feedbackList.add(new Feedback(
                    rs.getInt("id"),
                    rs.getString("customer_username"),
                    rs.getInt("rating"),
                    rs.getString("comments"),
                    rs.getTimestamp("feedback_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return feedbackList;
    }

    /**
     * Saves a new dish rating to the database.
     * @param rating The DishRating object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean addDishRating(DishRating rating) {
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads all dish ratings from the database.
     * @return A list of DishRating objects.
     */
    public static List<DishRating> loadDishRatings() {
        List<DishRating> ratings = new ArrayList<>();
        String sql = "SELECT id, menu_item_id, customer_username, rating, rating_date FROM dish_ratings ORDER BY rating_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ratings.add(new DishRating(
                    rs.getInt("id"),
                    rs.getInt("menu_item_id"),
                    rs.getString("customer_username"),
                    rs.getInt("rating"),
                    rs.getTimestamp("rating_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading dish ratings: " + e.getMessage());
            e.printStackTrace();
        }
        return ratings;
    }

    /**
     * Calculates the average rating for a specific dish.
     * @param menuItemId The ID of the menu item.
     * @return The average rating, or 0.0 if no ratings exist.
     */
    public static double getAverageDishRating(int menuItemId) {
        String sql = "SELECT AVG(rating) FROM dish_ratings WHERE menu_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1); // AVG returns 0.0 if no rows
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average dish rating: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Saves a new table booking to the database.
     * @param booking The TableBooking object to save.
     * @return true if successful, false otherwise.
     */
    public static boolean addTableBooking(TableBooking booking) {
        // First, check if a booking already exists for this customer and table number and time slot
        // This is crucial to prevent double bookings for the same table at overlapping times.
        String checkSql = "SELECT COUNT(*) FROM table_bookings WHERE table_number = ? AND " +
                          "( (booking_time < ? AND DATE_ADD(booking_time, INTERVAL duration_minutes MINUTE) > ?) OR " +
                          "  (? < DATE_ADD(booking_time, INTERVAL duration_minutes MINUTE) AND DATE_ADD(?, INTERVAL ? MINUTE) > booking_time) )";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, booking.getTableNumber());
            // Convert LocalDateTime to Timestamp before setting
            checkStmt.setTimestamp(2, Timestamp.valueOf(booking.getBookingTime().plusNanos(1))); // Add 1 nanosecond to make '<=' exclusive for start time boundary check
            checkStmt.setTimestamp(3, Timestamp.valueOf(booking.getBookingTime()));
            checkStmt.setTimestamp(4, Timestamp.valueOf(booking.getBookingTime()));
            checkStmt.setTimestamp(5, Timestamp.valueOf(booking.getBookingTime()));
            checkStmt.setInt(6, booking.getDurationMinutes());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.err.println("Booking conflict: Table " + booking.getTableNumber() + " is already booked for an overlapping time.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking for existing booking conflict: " + e.getMessage());
            e.printStackTrace();
            return false;
        }


        // Modified INSERT statement to include booking_time and duration_minutes
        String sql = "INSERT INTO table_bookings (customer_id, customer_name, phone, table_type, table_number, seats, booking_fee, payment_status, booking_time, duration_minutes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, booking.getCustomerId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getPhone());
            pstmt.setString(4, booking.getTableType().toString());
            pstmt.setInt(5, booking.getTableNumber());
            pstmt.setInt(6, booking.getSeats());
            pstmt.setDouble(7, booking.getBookingFee());
            pstmt.setString(8, booking.getPaymentStatus().toString());
            pstmt.setTimestamp(9, Timestamp.valueOf(booking.getBookingTime()));     // Set booking_time
            pstmt.setInt(10, booking.getDurationMinutes());                           // Set duration_minutes
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error saving table booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads all table bookings from the database.
     * @return A list of TableBooking objects.
     */
    public static List<TableBooking> getAllTableBookings() { 
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT customer_id, customer_name, phone, table_type, table_number, seats, booking_fee, payment_status, booking_time, duration_minutes FROM table_bookings ORDER BY booking_time DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookings.add(new TableBooking(
                    rs.getString("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("phone"),
                    TableType.fromString(rs.getString("table_type")),
                    rs.getInt("table_number"),
                    rs.getTimestamp("booking_time").toLocalDateTime(), 
                    rs.getInt("duration_minutes"),                       
                    rs.getDouble("booking_fee")
                ));
                // Set payment status explicitly as it's not in the constructor if using the shorter one
                bookings.get(bookings.size() - 1).setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading all table bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Loads table bookings made by a specific customer.
     * @param customerId The ID (username) of the customer.
     * @return A list of TableBooking objects for the given customer.
     */
    public static List<TableBooking> loadTableBookingsByCustomer(String customerId) {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT customer_id, customer_name, phone, table_type, table_number, seats, booking_fee, payment_status, booking_time, duration_minutes FROM table_bookings WHERE customer_id = ? ORDER BY booking_time DESC";
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
                    rs.getDouble("booking_fee")
                ));
                // Set payment status explicitly
                bookings.get(bookings.size() - 1).setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
            }
        } catch (SQLException e) {
            System.err.println("Error loading table bookings by customer: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Updates the payment status of a specific table booking.
     * @param customerId The ID of the customer who made the booking.
     * @param tableNumber The table number of the booking.
     * @param newStatus The new PaymentStatus.
     * @return true if successful, false otherwise.
     */
    public static boolean updateTableBookingPaymentStatus(String customerId, int tableNumber, PaymentStatus newStatus) {
        String sql = "UPDATE table_bookings SET payment_status = ? WHERE customer_id = ? AND table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.toString());
            pstmt.setString(2, customerId);
            pstmt.setInt(3, tableNumber);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table booking payment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a table booking from the database.
     * @param customerId The ID of the customer who made the booking.
     * @param tableNumber The table number of the booking to delete.
     * @return true if successful, false otherwise.
     */
    public static boolean deleteTableBooking(String customerId, int tableNumber) {
        String sql = "DELETE FROM table_bookings WHERE customer_id = ? AND table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            pstmt.setInt(2, tableNumber);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting table booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a list of available tables for a given date, time, duration, and minimum seats.
     * This method assumes that 'table_bookings' table stores all bookings.
     * It checks for overlaps with existing bookings to determine availability.
     *
     * @param desiredDateTime The desired start date and time for the booking.
     * @param durationMinutes The desired duration of the booking in minutes.
     * @param requiredSeats The minimum number of seats required.
     * @return A list of maps, where each map represents an available table with its number, type, and seats.
     */
    public static List<Map<String, Object>> getAvailableTables(LocalDateTime desiredDateTime, int durationMinutes, int requiredSeats) {
        List<Map<String, Object>> allTables = new ArrayList<>();
        // Define available tables manually or fetch from a 'tables' configuration if it existed.
        // For simplicity, let's hardcode a few table configurations.
        // In a real system, you'd have a 'tables' table.
        allTables.add(createTableMap(1, TableType.SMALL, 2));
        allTables.add(createTableMap(2, TableType.SMALL, 2));
        allTables.add(createTableMap(3, TableType.MEDIUM, 4));
        allTables.add(createTableMap(4, TableType.MEDIUM, 4));
        allTables.add(createTableMap(5, TableType.LARGE, 6));
        allTables.add(createTableMap(6, TableType.LARGE, 6));
        allTables.add(createTableMap(7, TableType.PRIVATE_DINING, 10)); // Example large table

        List<Map<String, Object>> availableTables = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null, cannot check table availability.");
                return availableTables;
            }

            // SQL to find all bookings that overlap with the desired time slot for any table
            // This query is complex as it needs to check for various overlap scenarios.
            // A booking (B) overlaps with desired (D) if:
            // (B_start < D_end AND B_end > D_start) OR (D_start < B_end AND D_end > B_start)
            // Where B_end = B_start + B_duration, and D_end = D_start + D_duration
            String sql = "SELECT table_number FROM table_bookings WHERE " +
                         "(booking_time < ? AND DATE_ADD(booking_time, INTERVAL duration_minutes MINUTE) > ?) OR " +
                         "(? < DATE_ADD(booking_time, INTERVAL duration_minutes MINUTE) AND DATE_ADD(?, INTERVAL ? MINUTE) > booking_time)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Timestamp desiredStart = Timestamp.valueOf(desiredDateTime);
                Timestamp desiredEnd = Timestamp.valueOf(desiredDateTime.plusMinutes(durationMinutes));

                // Parameters for (B_start < D_end AND B_end > D_start)
                pstmt.setTimestamp(1, desiredEnd); 
                pstmt.setTimestamp(2, desiredStart); 

                // Parameters for (D_start < B_end AND D_end > B_start)
                pstmt.setTimestamp(3, desiredStart); 
                pstmt.setTimestamp(4, desiredStart); 
                pstmt.setInt(5, durationMinutes); 


                ResultSet rs = pstmt.executeQuery();
                Set<Integer> bookedTableNumbers = new HashSet<>();
                while (rs.next()) {
                    bookedTableNumbers.add(rs.getInt("table_number"));
                }

                // Filter out booked tables and tables that don't meet seat requirements
                for (Map<String, Object> table : allTables) {
                    int tableNumber = (int) table.get("tableNumber");
                    int seats = (int) table.get("seats");
                    if (!bookedTableNumbers.contains(tableNumber) && seats >= requiredSeats) {
                        availableTables.add(table);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting available tables: " + e.getMessage());
            e.printStackTrace();
        }
        return availableTables;
    }

    /**
     * Helper method to create a map for a table entry.
     */
    private static Map<String, Object> createTableMap(int tableNumber, TableType tableType, int seats) {
        Map<String, Object> tableMap = new HashMap<>();
        tableMap.put("tableNumber", tableNumber);
        tableMap.put("tableType", tableType.getDisplayValue());
        tableMap.put("seats", seats);
        return tableMap;
    }
}
