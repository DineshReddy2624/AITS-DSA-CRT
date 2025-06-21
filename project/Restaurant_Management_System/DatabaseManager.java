package application;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap; // For ordered map in getItemsWithQuantities
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date; // For old Date object conversion if needed (less preferred)
import java.util.Arrays; // For Arrays.asList
import java.util.UUID; // Added for UUID generation

/**
 * Manages all database interactions for the Restaurant Management System.
 * Handles menu items, users, orders, table bookings, feedback, and dish ratings.
 * This class provides methods for creating tables, adding, retrieving, updating,
 * and deleting data for various entities.
 */
public class DatabaseManager {

    // Admin credentials provided by the user
    private static final String ADMIN_USERNAME = "Dinesh Reddy";
    private static final String ADMIN_PASSWORD_PLAIN = "Dinesh@2624"; // This will be hashed before storage

    // --- Static map to represent all physical tables in the restaurant for availability checks ---
    // In a real application, this would ideally be loaded from a persistent 'tables' database table.
    private static final Map<TableType, List<Integer>> ALL_RESTAURANT_TABLES = new HashMap<>();

    static {
        // Initialize the fixed set of tables available in the restaurant
        ALL_RESTAURANT_TABLES.put(TableType.SMALL, Arrays.asList(1, 2, 3, 4, 5)); // 5 small tables
        ALL_RESTAURANT_TABLES.put(TableType.MEDIUM, Arrays.asList(10, 11, 12)); // 3 medium tables
        ALL_RESTAURANT_TABLES.put(TableType.LARGE, Arrays.asList(20, 21));    // 2 large tables
        ALL_RESTAURANT_TABLES.put(TableType.PRIVATE_DINING, Arrays.asList(30)); // 1 private dining table
    }


    /**
     * Initializes the database by creating necessary tables and adding an admin user if they don't exist.
     * This method should be called at application startup.
     */
    public static void initializeDatabase() {

        // Now, create tables in the correct order (independent first)
        createUsersTable();
        createUserCardsTable(); // Depends on users
        createMenuItemsTable();
        createOrdersTable(); // Depends on users
        createOrderItemsTable(); // Depends on orders and menu_items
        createTableBookingsTable(); // Depends on users
        createFeedbackTable(); // Depends on users
        createDishRatingsTable(); // Depends on menu_items and users


        // Add default admin user if not exists
        if (getUserByUsername(ADMIN_USERNAME) == null) {
            String adminPasswordHash = PasswordUtil.hashPassword(ADMIN_PASSWORD_PLAIN);
            if (adminPasswordHash != null) {
                // Generate a UUID for the admin user
                String adminUserId = UUID.randomUUID().toString();
                User adminUser = new User(adminUserId, ADMIN_USERNAME, adminPasswordHash, "Dinesh Reddy (Admin)", "admin@example.com", "9876543210", UserRole.ADMIN);
                addUser(adminUser);
                System.out.println("Default admin user added: " + ADMIN_USERNAME);
            } else {
                System.err.println("Failed to hash admin password during initialization.");
            }
        }
        
        // Add some dummy menu items if the menu is empty
        try {
            if (getAllMenuItems().isEmpty()) {
                addMenuItem(new MenuItem(0, "chicken biryani", 150.00, "https://drive.google.com/uc?export=download&id=1DOcsjdVSiHfFbw98KRlDNsEpxqJOkXWf", 100));
                addMenuItem(new MenuItem(0, "mutton biryani", 200.00, "https://drive.google.com/uc?export=download&id=1P7SH9HAPUHuvQ9wkcaM0YOYeINANg5QK", 80));
                addMenuItem(new MenuItem(0, "veg biryani", 100.00, "https://drive.google.com/uc?export=download&id=1c493CZ0Nu8P4qdnQ55XlKLbG2o9bx7J5", 120));
                addMenuItem(new MenuItem(0, "chicken curry", 180.00, "https://drive.google.com/uc?export=download&id=1i3jLFLraZaH6AH9dvkb-MrG_tbxVLNu1", 90));
                addMenuItem(new MenuItem(0, "mutton curry", 220.00, "https://drive.google.com/uc?export=download&id=1cQoE6W9_s7NY4GqxRIgZTtguO6uJkC7z", 70));
                addMenuItem(new MenuItem(0, "veg curry", 120.00, "https://drive.google.com/uc?export=download&id=1RDw0ymUbvk7ITx563nyV0LeWVrcXw5dQ", 110));
                addMenuItem(new MenuItem(0, "chicken tikka", 160.00, "https://drive.google.com/uc?export=download&id=105169q3LUgXDsuxw2U6uoI4A0-qeYdvO", 95));
                addMenuItem(new MenuItem(0, "mutton tikka", 210.00, "https://drive.google.com/uc?export=download&id=1m5yjJJkfQ_yJflgwBAhtGb6G2O3f3UEG", 65));
                addMenuItem(new MenuItem(0, "veg tikka", 130.00, "https://drive.google.com/uc?export=download&id=12OBzeP61PHbCTpBOI7NCTB7Xs7HMsioN", 105));
                addMenuItem(new MenuItem(0, "chicken kebab", 170.00, "https://drive.google.com/uc?export=download&id=1t4_FBXzgwBhclm_aKOiwlL-daqTyiOM2", 85));
                addMenuItem(new MenuItem(0, "mutton kebab", 230.00, "https://drive.google.com/uc?export=download&id=116_YvTUVPCRSecjuYlWtQgmp7hIEERlm", 60));
                addMenuItem(new MenuItem(0, "veg kebab", 140.00, "https://drive.google.com/uc?export=download&id=11kSyg2mxLkX4vaoJMVOeN4UB8ff5w-TC", 100));
                addMenuItem(new MenuItem(0, "soft drink", 50.00, "https://drive.google.com/uc?export=download&id=1oZGTjuzltsiSdyPlMHrwRt37fRnaAkTM", 200));
                addMenuItem(new MenuItem(0, "water", 20.00, "https://drive.google.com/uc?export=download&id=1EqsZddDosywdiN7k5atU-DCiYcYN6MvZ", 500));
                addMenuItem(new MenuItem(0, "salad", 30.00, "https://drive.google.com/uc?export=download&id=1D-srw_vsgd1UHmMS7D7mIu-RwgB7oZ4s", 150));
                addMenuItem(new MenuItem(0, "dessert", 80.00, "https://drive.google.com/uc?export=download&id=1Z271ej253HWtRdgMjFOpg-42z0GmlmgC", 130));
                System.out.println("Default biryani-style menu items inserted with food-related images.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing dummy menu items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Table Creation Methods ---

    /**
     * Creates the 'users' table in the database if it doesn't already exist.
     * Stores user details including their role and a unique UUID.
     */
    private static void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "user_id VARCHAR(255) PRIMARY KEY," + // Using UUID as primary key
                     "username VARCHAR(50) NOT NULL UNIQUE," +
                     "password_hash VARCHAR(255) NOT NULL," +
                     "full_name VARCHAR(100) NOT NULL," +
                     "email VARCHAR(100)," +
                     "phone_number VARCHAR(20)," +
                     "role VARCHAR(20) NOT NULL" + // e.g., 'ADMIN', 'CUSTOMER'
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'users' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropUsersTable() {
        String sql = "DROP TABLE IF EXISTS users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping users table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'menu_items' table in the database if it doesn't already exist.
     * Stores information about dishes/items available in the restaurant.
     */
    private static void createMenuItemsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS menu_items (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100) NOT NULL UNIQUE," + // Item names must be unique
                     "price DOUBLE NOT NULL," +
                     "image_url VARCHAR(255)," +
                     "stock INT DEFAULT 0" + // New: Stock quantity
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Menu items table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating menu_items table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'menu_items' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropMenuItemsTable() {
        String sql = "DROP TABLE IF EXISTS menu_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Menu items table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping menu_items table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'orders' table in the database if it doesn't already exist.
     * Stores general order information.
     */
    private static void createOrdersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS orders (" +
                     "order_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "customer_username VARCHAR(50)," + // Link to users table (username, not user_id directly for simplicity)
                     "order_time DATETIME NOT NULL," +
                     "status VARCHAR(50) NOT NULL," +
                     "payment_status VARCHAR(50) NOT NULL," +
                     "payment_method VARCHAR(50) NOT NULL," +
                     "discount_applied DOUBLE DEFAULT 0.0," +
                     "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE SET NULL" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Orders table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating orders table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'orders' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropOrdersTable() {
        String sql = "DROP TABLE IF EXISTS orders";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Orders table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping orders table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'order_items' table for the many-to-many relationship between orders and menu items.
     * This table records which menu items are part of which order and their quantity.
     */
    private static void createOrderItemsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS order_items (" +
                     "order_item_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "order_id INT NOT NULL," +
                     "menu_item_id INT NOT NULL," +
                     "quantity INT NOT NULL," +
                     "price_at_order DOUBLE NOT NULL," + // Price at the time of order
                     "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
                     "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT" + // RESTRICT to prevent deleting menu item if part of existing order
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Order items table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating order_items table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'order_items' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropOrderItemsTable() {
        String sql = "DROP TABLE IF EXISTS order_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Order items table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping order_items table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'table_bookings' table in the database if it doesn't already exist.
     * Stores information about restaurant table reservations.
     */
    private static void createTableBookingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS table_bookings (" +
                     "booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "customer_id VARCHAR(255) NOT NULL," + // Link to users table (user_id)
                     "customer_name VARCHAR(100) NOT NULL," +
                     "phone VARCHAR(20)," +
                     "table_type VARCHAR(50) NOT NULL," +
                     "table_number INT NOT NULL," +
                     "seats INT NOT NULL," + // Number of seats for this booking
                     "booking_time DATETIME NOT NULL," +
                     "duration_minutes INT NOT NULL," +
                     "booking_fee DOUBLE NOT NULL," +
                     "payment_status VARCHAR(50) NOT NULL," +
                     "payment_method VARCHAR(50) NOT NULL," +
                     "FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table bookings table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating table_bookings table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'table_bookings' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropTableBookingsTable() {
        String sql = "DROP TABLE IF EXISTS table_bookings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table bookings table dropped successfully (if it existed).");
        } 
        catch (SQLException e) { 
            System.err.println("Error dropping table_bookings table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'feedback' table for general customer feedback.
     */
    private static void createFeedbackTable() {
        String sql = "CREATE TABLE IF NOT EXISTS feedback (" +
                     "feedback_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "customer_username VARCHAR(50)," + // Link to users table
                     "rating INT NOT NULL," +          // e.g., 1-5 stars
                     "comments TEXT," +
                     "feedback_date DATETIME NOT NULL," +
                     "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE SET NULL" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Feedback table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating feedback table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'feedback' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropFeedbackTable() {
        String sql = "DROP TABLE IF EXISTS feedback";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Feedback table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping feedback table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'dish_ratings' table for specific dish ratings.
     */
    private static void createDishRatingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS dish_ratings (" +
                     "rating_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "menu_item_id INT NOT NULL," +
                     "customer_username VARCHAR(50)," + // Link to users table
                     "rating INT NOT NULL," +          // e.g., 1-5 stars
                     "rating_date DATETIME NOT NULL," +
                     "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE," +
                     "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE SET NULL" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Dish ratings table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating dish_ratings table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'dish_ratings' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropDishRatingsTable() {
        String sql = "DROP TABLE IF EXISTS dish_ratings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Dish ratings table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping dish_ratings table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the user_cards table to store saved (mock) credit card information for users.
     * This table is linked to the users table via user_id.
     */
    public static void createUserCardsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_cards (" +
                     "card_id VARCHAR(255) PRIMARY KEY," + // Using card_id as PK, useful for upsert
                     "user_id VARCHAR(255) NOT NULL," +
                     "last_four_digits VARCHAR(4) NOT NULL," +
                     "card_type VARCHAR(50) NOT NULL," +
                     "expiry_month VARCHAR(2) NOT NULL," +
                     "expiry_year VARCHAR(4) NOT NULL," +
                     "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("user_cards table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating user_cards table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * For development purposes: Drops the 'user_cards' table if it exists.
     * Use with caution as this will delete all data in the table.
     */
    private static void dropUserCardsTable() {
        String sql = "DROP TABLE IF EXISTS user_cards";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("user_cards table dropped successfully (if it existed).");
        } catch (SQLException e) {
            System.err.println("Error dropping user_cards table: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // --- User Management Methods ---

    /**
     * Adds a new user to the database.
     * @param user The User object to add.
     * @return true if the user was added successfully, false otherwise.
     */
    public static boolean addUser(User user) {
        String sql = "INSERT INTO users (user_id, username, password_hash, full_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setString(7, user.getRole().name()); // Store enum name as string
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a user from the database by username.
     * @param username The username to search for.
     * @return The User object if found, null otherwise.
     */
    public static User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, full_name, email, phone_number, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("user_id");
                String passwordHash = rs.getString("password_hash");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                UserRole role = UserRole.valueOf(rs.getString("role")); // Convert string to enum
                User user = new User(userId, username, passwordHash, fullName, email, phoneNumber, role);
                user.setSavedCards(getSavedCreditCards(userId)); // Load saved cards
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a user from the database by user ID.
     * @param userId The user ID to search for.
     * @return The User object if found, null otherwise.
     */
    public static User getUserByUserId(String userId) {
        String sql = "SELECT user_id, username, password_hash, full_name, email, phone_number, role FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                UserRole role = UserRole.valueOf(rs.getString("role"));
                User user = new User(userId, username, passwordHash, fullName, email, phoneNumber, role);
                user.setSavedCards(getSavedCreditCards(userId));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Authenticates a user by checking their username and password.
     * @param username The username provided by the user.
     * @param plainPassword The plain text password provided by the user.
     * @return The User object if authentication is successful, null otherwise.
     */
    public static User authenticateUser(String username, String plainPassword) {
        User user = getUserByUsername(username);
        if (user != null && PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    /**
     * Retrieves all users from the database.
     * @return A list of all User objects.
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, email, phone_number, role FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String userId = rs.getString("user_id");
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                UserRole role = UserRole.valueOf(rs.getString("role"));
                User user = new User(userId, username, passwordHash, fullName, email, phoneNumber, role);
                user.setSavedCards(getSavedCreditCards(userId)); // Load saved cards for each user
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Updates a user's details (full name, email, phone number) in the database.
     * @param user The User object with updated details.
     * @return true if the user was updated successfully, false otherwise.
     */
    public static boolean updateUserDetails(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setString(4, user.getUserId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } 
        catch (SQLException e) { 
            System.err.println("Error updating user details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a user's role in the database.
     * @param userId The ID of the user to update.
     * @param newRole The new role to set for the user.
     * @return true if the role was updated successfully, false otherwise.
     */
    public static boolean updateUserRole(String userId, UserRole newRole) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.name());
            pstmt.setString(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user from the database.
     * @param userId The ID of the user to delete.
     * @return true if the user was deleted successfully, false otherwise.
     */
    public static boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // --- Menu Item Management Methods ---

    /**
     * Adds a new menu item to the database.
     * @param item The MenuItem object to add. The ID will be auto-generated by the database.
     * @return true if the item was added successfully, false otherwise.
     */
    public static boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (name, price, image_url, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getImageUrl());
            pstmt.setInt(4, item.getStock()); // Include stock
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the auto-generated ID and set it back to the MenuItem object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all menu items from the database.
     * @return A list of all MenuItem objects.
     */
    public static List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, name, price, image_url, stock FROM menu_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String imageUrl = rs.getString("image_url");
                int stock = rs.getInt("stock"); // Retrieve stock
                menuItems.add(new MenuItem(id, name, price, imageUrl, stock));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all menu items: " + e.getMessage());
            e.printStackTrace();
        }
        return menuItems;
    }

    /**
     * Retrieves a specific menu item by its ID.
     * @param id The ID of the menu item to retrieve.
     * @return The MenuItem object if found, null otherwise.
     */
    public static MenuItem getMenuItemById(int id) {
        String sql = "SELECT id, name, price, image_url, stock FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String imageUrl = rs.getString("image_url");
                int stock = rs.getInt("stock");
                return new MenuItem(id, name, price, imageUrl, stock);
            }
        } catch (SQLException e) {
            System.err.println("Error getting menu item by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates an existing menu item in the database.
     * This method now updates all fields including stock.
     * @param item The MenuItem object with updated values.
     * @return true if the item was updated successfully, false otherwise.
     */
    public static boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE menu_items SET name = ?, price = ?, image_url = ?, stock = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getImageUrl());
            pstmt.setInt(4, item.getStock());
            pstmt.setInt(5, item.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
        // This catch block was missing curly braces in the provided Main.java
        catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a menu item from the database.
     * @param id The ID of the menu item to delete.
     * @return true if the item was deleted successfully, false otherwise.
     */
    public static boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Order Management Methods ---

    /**
     * Adds a new order to the database. This includes inserting into 'orders' table
     * and then populating 'order_items' table with each item and its quantity.
     *
     * @param order The Order object to add. The orderId will be auto-generated.
     * @return true if the order and its items were added successfully, false otherwise.
     */
    public static boolean addOrder(Order order) {
        String orderSql = "INSERT INTO orders (customer_username, order_time, status, payment_status, payment_method, discount_applied) VALUES (?, ?, ?, ?, ?, ?)";
        String orderItemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into orders table
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, order.getCustomerUsername());
                pstmt.setTimestamp(2, Timestamp.valueOf(order.getOrderTime()));
                pstmt.setString(3, order.getStatus().name());
                pstmt.setString(4, order.getPaymentStatus().name());
                pstmt.setString(5, order.getPaymentMethod().name());
                pstmt.setDouble(6, order.getDiscountApplied());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getInt(1)); // Set the auto-generated ID back to the object
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Insert into order_items table for each unique item in the order
            Map<MenuItem, Integer> itemQuantities = order.getItemsWithQuantities();
            try (PreparedStatement pstmt = conn.prepareStatement(orderItemSql)) {
                for (Map.Entry<MenuItem, Integer> entry : itemQuantities.entrySet()) {
                    MenuItem item = entry.getKey();
                    Integer quantity = entry.getValue();
                    pstmt.setInt(1, order.getOrderId());
                    pstmt.setInt(2, item.getId());
                    pstmt.setInt(3, quantity); // Corrected to use setInt for quantity as per schema
                    pstmt.setDouble(4, item.getPrice()); // Price at the time of order
                    pstmt.addBatch(); // Add to batch for efficient insertion
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) { // Catch block for closing connection
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves all orders from the database, including their associated menu items.
     *
     * @return A list of all Order objects.
     */
    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, customer_username, order_time, status, payment_status, payment_method, discount_applied FROM orders";
        String itemSql = "SELECT oi.menu_item_id, oi.quantity, mi.name, mi.price, mi.image_url, mi.stock FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id WHERE oi.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String customerUsername = rs.getString("customer_username");
                LocalDateTime orderTime = rs.getTimestamp("order_time").toLocalDateTime();
                OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                PaymentStatus paymentStatus = PaymentStatus.valueOf(rs.getString("payment_status"));
                PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("payment_method"));
                double discountApplied = rs.getDouble("discount_applied");

                Order order = new Order(orderId);
                order.setCustomerUsername(customerUsername);
                order.setOrderTime(orderTime);
                order.setStatus(status);
                order.setPaymentStatus(paymentStatus);
                order.setPaymentMethod(paymentMethod);
                order.setDiscountApplied(discountApplied);

                // Fetch items for this order
                try (PreparedStatement itemPstmt = conn.prepareStatement(itemSql)) {
                    itemPstmt.setInt(1, orderId);
                    ResultSet itemRs = itemPstmt.executeQuery();
                    while (itemRs.next()) {
                        int menuItemId = itemRs.getInt("menu_item_id");
                        int quantity = itemRs.getInt("quantity");
                        String itemName = itemRs.getString("name");
                        double itemPrice = itemRs.getDouble("price");
                        String imageUrl = itemRs.getString("image_url");
                        int stock = itemRs.getInt("stock"); // Note: This is current stock, not stock at order time

                        MenuItem item = new MenuItem(menuItemId, itemName, itemPrice, imageUrl, stock);
                        for (int i = 0; i < quantity; i++) {
                            order.addItem(item); // Add the item 'quantity' times
                        }
                    }
                }
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Retrieves orders from the database for a specific customer, including their associated menu items.
     *
     * @param customerUsername The username of the customer.
     * @return A list of Order objects for the specified customer.
     */
    public static List<Order> getOrdersByCustomerUsername(String customerUsername) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, customer_username, order_time, status, payment_status, payment_method, discount_applied FROM orders WHERE customer_username = ?";
        String itemSql = "SELECT oi.menu_item_id, oi.quantity, mi.name, mi.price, mi.image_url, mi.stock FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id WHERE oi.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                LocalDateTime orderTime = rs.getTimestamp("order_time").toLocalDateTime();
                OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
                PaymentStatus paymentStatus = PaymentStatus.valueOf(rs.getString("payment_status"));
                PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("payment_method"));
                double discountApplied = rs.getDouble("discount_applied");

                Order order = new Order(orderId);
                order.setCustomerUsername(customerUsername);
                order.setOrderTime(orderTime);
                order.setStatus(status);
                order.setPaymentStatus(paymentStatus);
                order.setPaymentMethod(paymentMethod);
                order.setDiscountApplied(discountApplied);

                // Fetch items for this order
                try (PreparedStatement itemPstmt = conn.prepareStatement(itemSql)) {
                    itemPstmt.setInt(1, orderId);
                    ResultSet itemRs = itemPstmt.executeQuery();
                    while (itemRs.next()) {
                        int menuItemId = itemRs.getInt("menu_item_id");
                        int quantity = itemRs.getInt("quantity");
                        String itemName = itemRs.getString("name");
                        double itemPrice = itemRs.getDouble("price");
                        String imageUrl = itemRs.getString("image_url");
                        int stock = itemRs.getInt("stock");

                        MenuItem item = new MenuItem(menuItemId, itemName, itemPrice, imageUrl, stock);
                        for (int i = 0; i < quantity; i++) {
                            order.addItem(item);
                        }
                    }
                }
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders by customer username: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }


    /**
     * Updates an existing order's status, payment status, and payment method in the database.
     *
     * @param order The Order object with updated values.
     * @return true if the order was updated successfully, false otherwise.
     */
    public static boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET status = ?, payment_status = ?, payment_method = ?, discount_applied = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, order.getStatus().name());
            pstmt.setString(2, order.getPaymentStatus().name());
            pstmt.setString(3, order.getPaymentMethod().name());
            pstmt.setDouble(4, order.getDiscountApplied());
            pstmt.setInt(5, order.getOrderId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an order from the database and returns its items to stock.
     *
     * @param orderId The ID of the order to delete.
     * @return true if the order was deleted successfully, false otherwise.
     */
    public static boolean deleteOrder(int orderId) {
        String selectItemsSql = "SELECT menu_item_id, quantity FROM order_items WHERE order_id = ?";
        String deleteOrderItemsSql = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM orders WHERE order_id = ?";
        String updateStockSql = "UPDATE menu_items SET stock = stock + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get items and quantities from the order to return stock
            Map<Integer, Integer> itemsToReturnToStock = new HashMap<>();
            try (PreparedStatement pstmt = conn.prepareStatement(selectItemsSql)) {
                pstmt.setInt(1, orderId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    itemsToReturnToStock.put(rs.getInt("menu_item_id"), rs.getInt("quantity"));
                }
            }

            // 2. Delete order items
            try (PreparedStatement pstmt = conn.prepareStatement(deleteOrderItemsSql)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }

            // 3. Delete the order itself
            try (PreparedStatement pstmt = conn.prepareStatement(deleteOrderSql)) {
                pstmt.setInt(1, orderId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 4. Return stock for each item
            try (PreparedStatement pstmt = conn.prepareStatement(updateStockSql)) {
                for (Map.Entry<Integer, Integer> entry : itemsToReturnToStock.entrySet()) {
                    pstmt.setInt(1, entry.getValue()); // quantity to add back
                    pstmt.setInt(2, entry.getKey());   // menu_item_id
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting order and returning stock: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) { // Catch block for closing connection
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }


    // --- Table Booking Management Methods ---

    /**
     * Adds a new table booking to the database.
     * @param booking The TableBooking object to add. The ID will be auto-generated.
     * @return true if the booking was added successfully, false otherwise.
     */
    public static boolean addTableBooking(TableBooking booking) {
        String sql = "INSERT INTO table_bookings (customer_id, customer_name, phone, table_type, table_number, seats, booking_time, duration_minutes, booking_fee, payment_status, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, booking.getCustomerId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getPhone());
            pstmt.setString(4, booking.getTableType().name()); // Store enum name
            pstmt.setInt(5, booking.getTableNumber());
            pstmt.setInt(6, booking.getSeats());
            pstmt.setTimestamp(7, Timestamp.valueOf(booking.getBookingTime()));
            pstmt.setInt(8, booking.getDurationMinutes());
            pstmt.setDouble(9, booking.getBookingFee());
            pstmt.setString(10, booking.getPaymentStatus().name());
            pstmt.setString(11, booking.getPaymentMethod().name());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        booking.setBookingId(generatedKeys.getInt(1)); // Set generated ID
                    }
                }
                return true;
            }
            return false;
        } 
        catch (SQLException e) { 
            System.err.println("Error adding table booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all table bookings from the database.
     * @return A list of all TableBooking objects.
     */
    public static List<TableBooking> getAllTableBookings() {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT booking_id, customer_id, customer_name, phone, table_type, table_number, seats, booking_time, duration_minutes, booking_fee, payment_status, payment_method FROM table_bookings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                String customerId = rs.getString("customer_id");
                String customerName = rs.getString("customer_name");
                String phone = rs.getString("phone");
                TableType tableType = TableType.valueOf(rs.getString("table_type"));
                int tableNumber = rs.getInt("table_number");
                int seats = rs.getInt("seats");
                LocalDateTime bookingTime = rs.getTimestamp("booking_time").toLocalDateTime();
                int durationMinutes = rs.getInt("duration_minutes");
                double bookingFee = rs.getDouble("booking_fee");
                PaymentStatus paymentStatus = PaymentStatus.valueOf(rs.getString("payment_status"));
                PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("payment_method"));

                bookings.add(new TableBooking(bookingId, customerId, customerName, phone, tableType, tableNumber, seats,
                                              bookingTime, durationMinutes, bookingFee, paymentStatus, paymentMethod));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all table bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Retrieves table bookings for a specific customer.
     * @param customerId The ID of the customer.
     * @return A list of TableBooking objects for the specified customer.
     */
    public static List<TableBooking> getTableBookingsByCustomerId(String customerId) {
        List<TableBooking> bookings = new ArrayList<>();
        String sql = "SELECT booking_id, customer_id, customer_name, phone, table_type, table_number, seats, booking_time, duration_minutes, booking_fee, payment_status, payment_method FROM table_bookings WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                String customerName = rs.getString("customer_name");
                String phone = rs.getString("phone");
                TableType tableType = TableType.valueOf(rs.getString("table_type"));
                int tableNumber = rs.getInt("table_number");
                int seats = rs.getInt("seats");
                LocalDateTime bookingTime = rs.getTimestamp("booking_time").toLocalDateTime();
                int durationMinutes = rs.getInt("duration_minutes");
                double bookingFee = rs.getDouble("booking_fee");
                PaymentStatus paymentStatus = PaymentStatus.valueOf(rs.getString("payment_status"));
                PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("payment_method"));

                bookings.add(new TableBooking(bookingId, customerId, customerName, phone, tableType, tableNumber, seats,
                                              bookingTime, durationMinutes, bookingFee, paymentStatus, paymentMethod));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching table bookings by customer ID: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Updates an existing table booking in the database.
     * @param booking The TableBooking object with updated values.
     * @return true if the booking was updated successfully, false otherwise.
     */
    public static boolean updateTableBooking(TableBooking booking) {
        String sql = "UPDATE table_bookings SET customer_name = ?, phone = ?, table_type = ?, table_number = ?, seats = ?, booking_time = ?, duration_minutes = ?, booking_fee = ?, payment_status = ?, payment_method = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, booking.getCustomerName());
            pstmt.setString(2, booking.getPhone());
            pstmt.setString(3, booking.getTableType().name());
            pstmt.setInt(4, booking.getTableNumber());
            pstmt.setInt(5, booking.getSeats());
            pstmt.setTimestamp(6, Timestamp.valueOf(booking.getBookingTime()));
            pstmt.setInt(7, booking.getDurationMinutes());
            pstmt.setDouble(8, booking.getBookingFee());
            pstmt.setString(9, booking.getPaymentStatus().name());
            pstmt.setString(10, booking.getPaymentMethod().name());
            pstmt.setInt(11, booking.getBookingId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a table booking from the database.
     * @param bookingId The ID of the booking to delete.
     * @return true if the booking was deleted successfully, false otherwise.
     */
    public static boolean deleteTableBooking(int bookingId) {
        String sql = "DELETE FROM table_bookings WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting table booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks for available tables given a desired time, duration, and required seats.
     * This method simulates finding suitable tables based on existing bookings.
     *
     * @param desiredTime The desired start time for the booking.
     * @param durationMinutes The duration of the booking in minutes.
     * @param requiredSeats The minimum number of seats required.
     * @return A Map where keys are TableType and values are lists of available table numbers.
     */
    public static Map<TableType, List<Integer>> getAvailableTables(LocalDateTime desiredTime, int durationMinutes, int requiredSeats) {
        Map<TableType, List<Integer>> availableTables = new HashMap<>();

        // Initialize available tables with all restaurant tables
        for (Map.Entry<TableType, List<Integer>> entry : ALL_RESTAURANT_TABLES.entrySet()) {
            // Only consider table types that can accommodate the required seats
            if (entry.getKey().getSeats() >= requiredSeats) {
                availableTables.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        // Get all existing bookings that might conflict with the desired time slot
        String sql = "SELECT table_type, table_number, booking_time, duration_minutes FROM table_bookings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TableType bookedTableType = TableType.valueOf(rs.getString("table_type"));
                int bookedTableNumber = rs.getInt("table_number");
                LocalDateTime bookedTime = rs.getTimestamp("booking_time").toLocalDateTime();
                int bookedDurationMinutes = rs.getInt("duration_minutes");

                LocalDateTime bookedEndTime = bookedTime.plusMinutes(bookedDurationMinutes);
                LocalDateTime desiredEndTime = desiredTime.plusMinutes(durationMinutes);

                // Check for overlap: [start1, end1) and [start2, end2) overlap if start1 < end2 and start2 < end1
                boolean overlap = desiredTime.isBefore(bookedEndTime) && bookedTime.isBefore(desiredEndTime);

                if (overlap) {
                    // If there's an overlap, remove this table from the available list for its type
                    if (availableTables.containsKey(bookedTableType)) {
                        availableTables.get(bookedTableType).remove(Integer.valueOf(bookedTableNumber));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking table availability: " + e.getMessage());
            e.printStackTrace();
        }
        return availableTables;
    }


    // --- Feedback Management Methods ---

    /**
     * Adds a new feedback entry to the database.
     * @param feedback The Feedback object to add.
     * @return true if the feedback was added successfully, false otherwise.
     */
    public static boolean addFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedback (customer_username, rating, comments, feedback_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, feedback.getCustomerUsername());
            pstmt.setInt(2, feedback.getRating());
            pstmt.setString(3, feedback.getComments());
            pstmt.setTimestamp(4, Timestamp.valueOf(feedback.getFeedbackDate()));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        feedback.setFeedbackId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all feedback entries from the database.
     * @return A list of all Feedback objects.
     */
    public static List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT feedback_id, customer_username, rating, comments, feedback_date FROM feedback";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int feedbackId = rs.getInt("feedback_id");
                String customerUsername = rs.getString("customer_username");
                int rating = rs.getInt("rating");
                String comments = rs.getString("comments");
                LocalDateTime feedbackDate = rs.getTimestamp("feedback_date").toLocalDateTime();
                feedbackList.add(new Feedback(feedbackId, customerUsername, rating, comments, feedbackDate));
            }
        } 
        catch (SQLException e) { 
            System.err.println("Error fetching all feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return feedbackList;
    }

    // --- Dish Rating Management Methods ---

    /**
     * Adds a new dish rating entry to the database.
     * @param dishRating The DishRating object to add.
     * @return true if the rating was added successfully, false otherwise.
     */
    public static boolean addDishRating(DishRating dishRating) {
        String sql = "INSERT INTO dish_ratings (menu_item_id, customer_username, rating, rating_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, dishRating.getMenuItemId());
            pstmt.setString(2, dishRating.getCustomerUsername());
            pstmt.setInt(3, dishRating.getRating());
            pstmt.setTimestamp(4, Timestamp.valueOf(dishRating.getRatingDate()));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        dishRating.setRatingId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding dish rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all dish ratings from the database.
     * @return A list of all DishRating objects.
     */
    public static List<DishRating> getAllDishRatings() {
        List<DishRating> dishRatings = new ArrayList<>();
        String sql = "SELECT rating_id, menu_item_id, customer_username, rating, rating_date FROM dish_ratings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int ratingId = rs.getInt("rating_id");
                int menuItemId = rs.getInt("menu_item_id");
                String customerUsername = rs.getString("customer_username");
                int rating = rs.getInt("rating");
                LocalDateTime ratingDate = rs.getTimestamp("rating_date").toLocalDateTime();
                dishRatings.add(new DishRating(ratingId, menuItemId, customerUsername, rating, ratingDate));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all dish ratings: " + e.getMessage());
            e.printStackTrace();
        }
        return dishRatings;
    }

    /**
     * Calculates the average rating for a specific menu item.
     * @param menuItemId The ID of the menu item.
     * @return The average rating as a double, or 0.0 if no ratings exist for the item.
     */
    public static double getAverageRatingForMenuItem(int menuItemId) {
        String sql = "SELECT AVG(rating) AS average_rating FROM dish_ratings WHERE menu_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("average_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating for menu item " + menuItemId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0; // Return 0.0 if no ratings or error
    }

    // --- Credit Card Management Methods ---

    /**
     * Saves a new credit card for a specific user.
     * This method acts as an "upsert": if a card with the same card_id (cardType-lastFourDigits)
     * already exists for the user, it will be updated. Otherwise, a new card will be inserted.
     *
     * @param userId The ID of the user to whom the card belongs.
     * @param card The CreditCard object to save.
     * @return true if the card was saved/updated successfully, false otherwise.
     */
    public static boolean saveCreditCard(String userId, CreditCard card) {
        // Use INSERT ... ON DUPLICATE KEY UPDATE for upsert behavior
        String sql = "INSERT INTO user_cards (card_id, user_id, last_four_digits, card_type, expiry_month, expiry_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "user_id = VALUES(user_id), " + // Ensure user_id isn't changed if it's the duplicate key
                     "last_four_digits = VALUES(last_four_digits), " +
                     "card_type = VALUES(card_type), " +
                     "expiry_month = VALUES(expiry_month), " +
                     "expiry_year = VALUES(expiry_year)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, card.getCardId()); // card_id is the primary key
            pstmt.setString(2, userId);
            pstmt.setString(3, card.getLastFourDigits());
            pstmt.setString(4, card.getCardType());
            pstmt.setString(5, card.getExpiryMonth());
            pstmt.setString(6, card.getExpiryYear());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Will be 1 for insert, 2 for update (row found + row updated)
        } catch (SQLException e) {
            System.err.println("Error saving credit card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all saved credit cards for a specific user.
     * @param userId The ID of the user.
     * @return A list of CreditCard objects.
     */
    public static List<CreditCard> getSavedCreditCards(String userId) {
        List<CreditCard> cards = new ArrayList<>();
        String sql = "SELECT card_id, last_four_digits, card_type, expiry_month, expiry_year FROM user_cards WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cards.add(new CreditCard(
                        rs.getString("last_four_digits"),
                        rs.getString("card_type"),
                        rs.getString("expiry_month"),
                        rs.getString("expiry_year")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching saved credit cards: " + e.getMessage());
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * Deletes a credit card from the database based on its unique cardId.
     * @param cardId The unique ID of the card to delete (e.g., "Visa-1234").
     * @return true if the card was deleted successfully, false otherwise.
     */
    public static boolean deleteCreditCard(String cardId) {
        String sql = "DELETE FROM user_cards WHERE card_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting credit card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
