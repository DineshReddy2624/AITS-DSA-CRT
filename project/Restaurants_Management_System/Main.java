package application;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


public class Main extends Application
{

    // --- Data Classes (moved to separate files for better organization) ---
    // You'll create MenuItem.java, Order.java, TableBooking.java
    // Their content remains the same as your original classes.

    // --- Data & state ---
    private List<MenuItem> menuList = new ArrayList<>();
    private Map<Integer, MenuItem> menuMap = new HashMap<>(); // Maps ID to MenuItem for quick lookup
    private List<Order> allOrders = new ArrayList<>();
    private List<TableBooking> bookings = new ArrayList<>();
    private List<TableBooking> paidBookings = new ArrayList<>(); // You'll need to load/manage this from DB if needed
    private int orderCounter = 1; // Used for new order IDs
    private int customerIdCounter = 1001; // Used for new customer IDs for table bookings
    private int menuItemIdCounter = 101; // Used for new menu item IDs

    // Table availability (volatile, re-derived from bookings on startup)
    private boolean[] tables2 = new boolean[10];
    private boolean[] tables4 = new boolean[10];
    private boolean[] tables6 = new boolean[10];
    private boolean[] tables8 = new boolean[10];
    private boolean[] tables10 = new boolean[10];

    // For current session customer order
    private Order currentOrder = null;

    // --- UI Controls ---
    private Stage primaryStage;
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        // Test database connection
        Connection testConn = DBConnection.getConnection();
        if (testConn != null) {
            System.out.println("✅ Connected to MySQL database!");
            try {
                testConn.close(); // Close the test connection
            } catch (SQLException e) {
                System.err.println("Error closing test connection: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Failed to connect to MySQL database. Please check DBConnection.java and MySQL server.");
            // Optionally, show an alert and exit if connection fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Failed to connect to the database!");
            alert.setContentText("Please ensure your MySQL server is running and DBConnection.java details are correct.");
            alert.showAndWait();
            System.exit(1); // Exit application if no DB connection
        }

        DatabaseManager.initializeDatabase(); // Create tables if they don't exist
        loadAllDataFromDatabase(); // Load all existing data from DB
        showAccessSelection();
    }

    // Loads all data from the database into memory
    private void loadAllDataFromDatabase() {
        // Load Menu Items
        menuList = DatabaseManager.loadMenuItems();
        menuMap.clear();
        for (MenuItem item : menuList) {
            menuMap.put(item.getId(), item);
            if (item.getId() >= menuItemIdCounter) {
                menuItemIdCounter = item.getId() + 1; // Ensure counter is higher than existing IDs
            }
        }
        if (menuList.isEmpty()) {
            // If no menu items exist in DB, initialize default ones and save them
            initializeDefaultMenuAndSave();
        }

        // Load Orders
        allOrders = DatabaseManager.loadOrders(menuMap); // Pass menuMap for linking items
        if (!allOrders.isEmpty()) {
            orderCounter = allOrders.stream().mapToInt(o -> o.orderId).max().orElse(0) + 1;
        }

        // Load Table Bookings
        bookings = DatabaseManager.loadTableBookings();
        if (!bookings.isEmpty()) {
            customerIdCounter = bookings.stream()
                                    .map(b -> b.customerId.replace("CUST", ""))
                                    .filter(s -> s.matches("\\d+")) // Ensure it's a number
                                    .mapToInt(Integer::parseInt)
                                    .max().orElse(0) + 1;

            // Reconstruct table availability based on loaded bookings
            resetTableAvailability(); // Reset all tables to false first
            for (TableBooking booking : bookings) {
                // Mark the table as occupied if the booking exists
                switch (booking.tableType) {
                    case "Table2": tables2[booking.tableNumber - 1] = true; break;
                    case "Table4": tables4[booking.tableNumber - 1] = true; break;
                    case "Table6": tables6[booking.tableNumber - 1] = true; break;
                    case "Table8": tables8[booking.tableNumber - 1] = true; break;
                    case "Table10": tables10[booking.tableNumber - 1] = true; break;
                }
            }
        }
    }

    // Initializes default menu items and saves them to the database
    private void initializeDefaultMenuAndSave() {
        String[] names = {"chicken biryani", "mutton biryani", "veg biryani", "chicken curry", "mutton curry",
                "veg curry", "chicken tikka", "mutton tikka", "veg tikka", "chicken kebab",
                "mutton kebab", "veg kebab", "soft drink", "water", "salad", "dessert"};
        double[] prices = {150.00, 200.00, 100.00, 180.00, 220.00,
                120.00, 160.00, 210.00, 130.00, 170.00,
                230.00, 140.00, 50.00, 20.00, 30.00, 80.00};
        for (int i = 0; i < names.length; i++) {
            MenuItem item = new MenuItem(menuItemIdCounter, names[i], prices[i]);
            menuList.add(item);
            menuMap.put(menuItemIdCounter, item);
            DatabaseManager.saveMenuItem(item); // Save default items to DB
            menuItemIdCounter++;
        }
    }

    // Resets all table availability to false (before loading from bookings)
    private void resetTableAvailability() {
        Arrays.fill(tables2, false);
        Arrays.fill(tables4, false);
        Arrays.fill(tables6, false);
        Arrays.fill(tables8, false);
        Arrays.fill(tables10, false);
    }

    private void showAccessSelection() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome to Shield Restaurant");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button adminBtn = new Button("Admin Access");
        Button customerBtn = new Button("Customer Access");
        Button exitBtn = new Button("Exit");

        adminBtn.setPrefWidth(150);
        customerBtn.setPrefWidth(150);
        exitBtn.setPrefWidth(150);

        adminBtn.setOnAction(e -> showAdminLogin());
        customerBtn.setOnAction(e -> showCustomerMenu());
        exitBtn.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(welcomeLabel, adminBtn, customerBtn, exitBtn);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Access Selection");
        primaryStage.show();
    }

    // --- Admin Login Screen ---
    private void showAdminLogin() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        Label prompt = new Label("Enter 4-digit admin PIN:");
        PasswordField pinField = new PasswordField();
        pinField.setMaxWidth(150);

        Button loginBtn = new Button("Login");
        Button backBtn = new Button("Back");
        loginBtn.setPrefWidth(100);
        backBtn.setPrefWidth(100);

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            String pin = pinField.getText();
            if ("2004".equals(pin)) {
                showAdminMenu();
            } else {
                msgLabel.setText("Invalid PIN. Access denied.");
            }
        });

        backBtn.setOnAction(e -> showAccessSelection());

        root.getChildren().addAll(prompt, pinField, loginBtn, backBtn, msgLabel);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Login");
    }

    // --- Admin Menu Screen ---
    private void showAdminMenu() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_LEFT);

        Label label = new Label("Admin Menu");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button viewMenuBtn = new Button("View Menu Items");
        Button addItemBtn = new Button("Add New Menu Item");
        Button removeItemBtn = new Button("Remove Menu Item by ID");
        Button viewBookingsBtn = new Button("View All Table Bookings");
        Button viewOrdersBtn = new Button("View All Orders");
        Button checkPaymentStatusBtn = new Button("Check Order Payment Status by ID");
        Button searchMenuBtn = new Button("Search Menu Items by Name");
        Button dailyReportBtn = new Button("Generate Daily Sales Report");
        Button logoutBtn = new Button("Logout");

        // Set preferred width for buttons for uniform appearance
        double buttonWidth = 250;
        viewMenuBtn.setPrefWidth(buttonWidth);
        addItemBtn.setPrefWidth(buttonWidth);
        removeItemBtn.setPrefWidth(buttonWidth);
        viewBookingsBtn.setPrefWidth(buttonWidth);
        viewOrdersBtn.setPrefWidth(buttonWidth);
        checkPaymentStatusBtn.setPrefWidth(buttonWidth);
        searchMenuBtn.setPrefWidth(buttonWidth);
        dailyReportBtn.setPrefWidth(buttonWidth);
        logoutBtn.setPrefWidth(buttonWidth);

        TextArea adminOutput = new TextArea();
        adminOutput.setEditable(false);
        adminOutput.setPrefHeight(300);
        adminOutput.setPromptText("Admin operations output will appear here...");

        viewMenuBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("Menu Items:\n");
            if (menuList.isEmpty()) {
                sb.append("No menu items available.");
            } else {
                for (MenuItem mi : menuList) {
                    sb.append(mi.toString()).append("\n");
                }
            }
            adminOutput.setText(sb.toString());
        });

        addItemBtn.setOnAction(e -> showAddMenuItem(adminOutput));
        removeItemBtn.setOnAction(e -> showRemoveMenuItem(adminOutput));
        viewBookingsBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("All Table Bookings:\n");
            if (bookings.isEmpty()) {
                sb.append("No table bookings yet.");
            } else {
                for (TableBooking tb : bookings) {
                    sb.append(tb.toDetailedString()).append("\n");
                }
            }
            adminOutput.setText(sb.toString());
        });

        viewOrdersBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("All Orders:\n");
            if (allOrders.isEmpty()) {
                sb.append("No orders yet.");
            } else {
                for (Order o : allOrders) {
                    sb.append(o.toDetailedString()).append("\n-----------------\n");
                }
            }
            adminOutput.setText(sb.toString());
        });

        checkPaymentStatusBtn.setOnAction(e -> showCheckPaymentStatus(adminOutput));
        searchMenuBtn.setOnAction(e -> showSearchMenuItem(adminOutput));
        dailyReportBtn.setOnAction(e -> generateDailySalesReport(adminOutput));

        logoutBtn.setOnAction(e -> showAccessSelection());

        root.getChildren().addAll(label, viewMenuBtn, addItemBtn, removeItemBtn, viewBookingsBtn,
                viewOrdersBtn, checkPaymentStatusBtn, searchMenuBtn, dailyReportBtn, logoutBtn, adminOutput);

        Scene scene = new Scene(new ScrollPane(root), 400, 650); // Increased height for more buttons
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Menu");
    }

    // --- Admin: Add Menu Item ---
    private void showAddMenuItem(TextArea adminOutput) {
        Stage stage = new Stage();
        stage.setTitle("Add Menu Item");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        TextField priceField = new TextField();
        priceField.setPromptText("Item Price");

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red;");

        Button addBtn = new Button("Add");
        Button cancelBtn = new Button("Cancel");

        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            if (name.isEmpty() || priceText.isEmpty()) {
                msgLabel.setText("Fill all fields");
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                MenuItem newItem = new MenuItem(menuItemIdCounter, name, price); // Use current counter, then increment
                menuList.add(newItem);
                menuMap.put(newItem.getId(), newItem);
                DatabaseManager.saveMenuItem(newItem); // Save to database
                adminOutput.setText("Item added: " + newItem.toString()); // Update main admin output
                msgLabel.setText("Item added: " + newItem.toString());
                menuItemIdCounter++; // Increment after successful addition and save
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid price");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(new Label("Enter new menu item details:"), nameField, priceField, addBtn, cancelBtn, msgLabel);

        stage.setScene(new Scene(root, 300, 200));
        stage.show();
    }

    // --- Admin: Remove Menu Item ---
    private void showRemoveMenuItem(TextArea adminOutput) {
        Stage stage = new Stage();
        stage.setTitle("Remove Menu Item");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        TextField idField = new TextField();
        idField.setPromptText("Enter Menu Item ID");

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red;");

        Button removeBtn = new Button("Remove");
        Button cancelBtn = new Button("Cancel");

        removeBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                MenuItem removed = null;
                Iterator<MenuItem> iterator = menuList.iterator();
                while (iterator.hasNext()) {
                    MenuItem item = iterator.next();
                    if (item.getId() == id) {
                        removed = item;
                        iterator.remove();
                        menuMap.remove(id);
                        DatabaseManager.deleteMenuItem(id); // Delete from database
                        break;
                    }
                }
                if (removed != null) {
                    adminOutput.setText("Removed item: " + removed.toString()); // Update main admin output
                    msgLabel.setText("Removed item: " + removed.toString());
                } else {
                    msgLabel.setText("No item with ID " + id);
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid ID");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(new Label("Remove menu item by ID:"), idField, removeBtn, cancelBtn, msgLabel);

        stage.setScene(new Scene(root, 300, 200));
        stage.show();
    }

    // --- Admin: Check Payment Status ---
    private void showCheckPaymentStatus(TextArea adminOutput) {
        Stage stage = new Stage();
        stage.setTitle("Check Payment Status");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        TextField orderIdField = new TextField();
        orderIdField.setPromptText("Enter Order ID");

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);

        Button checkBtn = new Button("Check");
        Button cancelBtn = new Button("Cancel");

        checkBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(orderIdField.getText().trim());
                Order foundOrder = null;
                for (Order o : allOrders) {
                    if (o.orderId == id) {
                        foundOrder = o;
                        break;
                    }
                }
                if (foundOrder != null) {
                    // Option to update status
                    TextInputDialog updateStatusDialog = new TextInputDialog(foundOrder.paymentStatus);
                    updateStatusDialog.setTitle("Update Payment Status");
                    updateStatusDialog.setHeaderText("Current Status: " + foundOrder.paymentStatus + "\nEnter new payment status (e.g., paid, pending, cancelled):");
                    Optional<String> newStatus = updateStatusDialog.showAndWait();
                    if (newStatus.isPresent() && !newStatus.get().trim().isEmpty()) {
                        String statusToSet = newStatus.get().trim().toLowerCase();
                        foundOrder.paymentStatus = statusToSet;
                        DatabaseManager.updateOrderPaymentStatus(foundOrder.orderId, statusToSet); // Update DB
                        msgLabel.setText("Order ID: " + id + "\nPayment Status Updated to: " + foundOrder.paymentStatus +
                                "\nOrder Status: " + foundOrder.status);
                        adminOutput.setText("Order ID: " + id + "\nPayment Status Updated to: " + foundOrder.paymentStatus +
                                "\nOrder Status: " + foundOrder.status);
                    } else {
                        msgLabel.setText("Order ID: " + id + "\nPayment Status: " + foundOrder.paymentStatus +
                                "\nOrder Status: " + foundOrder.status);
                    }
                } else {
                    msgLabel.setText("Order ID " + id + " not found.");
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid Order ID");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(new Label("Check payment status for Order ID:"), orderIdField, checkBtn, cancelBtn, msgLabel);

        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }

    // --- Admin: Search Menu Items ---
    private void showSearchMenuItem(TextArea adminOutput) {
        Stage stage = new Stage();
        stage.setTitle("Search Menu Items");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        TextField keywordField = new TextField();
        keywordField.setPromptText("Enter keyword");

        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(200);
        resultsArea.setPromptText("Search results will appear here...");

        Button searchBtn = new Button("Search");
        Button cancelBtn = new Button("Cancel");

        searchBtn.setOnAction(e -> {
            String keyword = keywordField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                resultsArea.setText("Enter a keyword to search");
                return;
            }
            StringBuilder sb = new StringBuilder("Search Results:\n");
            boolean found = false;
            for (MenuItem item : menuList) {
                if (item.getName().toLowerCase().contains(keyword)) {
                    sb.append(item.toString()).append("\n");
                    found = true;
                }
            }
            if (!found) {
                sb.append("No menu items found with keyword: ").append(keyword);
            }
            resultsArea.setText(sb.toString());
            adminOutput.setText(sb.toString()); // Also update main admin output
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(new Label("Search menu items by name:"), keywordField, searchBtn, cancelBtn, resultsArea);

        stage.setScene(new Scene(root, 350, 350));
        stage.show();
    }

    // --- Admin: Daily Sales Report ---
    private void generateDailySalesReport(TextArea adminOutput) {
        double totalSales = 0;
        int paidOrdersCount = 0;
        for (Order order : allOrders) {
            if ("paid".equals(order.paymentStatus)) {
                // Now directly use the net_amount from the order object (which corresponds to DB field)
                totalSales += order.getFinalPrice(); // Assuming getFinalPrice() returns the net amount
                paidOrdersCount++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Daily Sales Report\n");
        sb.append("---------------------\n");
        sb.append("Total Paid Orders: ").append(paidOrdersCount).append("\n");
        sb.append("Total Sales (Net Amount): Rs.").append(String.format("%.2f", totalSales)).append("\n");
        adminOutput.setText(sb.toString());
    }

    // --- Customer Menu ---
    private void showCustomerMenu() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_LEFT);

        Label label = new Label("Customer Menu");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button viewMenuBtn = new Button("View Menu Items");
        Button placeOrderBtn = new Button("Place New Order");
        Button viewCartBtn = new Button("View Current Cart");
        Button confirmOrderBtn = new Button("Confirm & Pay Order");
        Button reserveTableBtn = new Button("Reserve Table");
        Button searchOrderBtn = new Button("Search Order by ID");
        Button backBtn = new Button("Back to Access Selection");

        // Set preferred width for buttons for uniform appearance
        double buttonWidth = 250;
        viewMenuBtn.setPrefWidth(buttonWidth);
        placeOrderBtn.setPrefWidth(buttonWidth);
        viewCartBtn.setPrefWidth(buttonWidth);
        confirmOrderBtn.setPrefWidth(buttonWidth);
        reserveTableBtn.setPrefWidth(buttonWidth);
        searchOrderBtn.setPrefWidth(buttonWidth);
        backBtn.setPrefWidth(buttonWidth);

        TextArea customerOutput = new TextArea();
        customerOutput.setEditable(false);
        customerOutput.setPrefHeight(300);
        customerOutput.setPromptText("Customer operations output will appear here...");

        viewMenuBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("Menu Items:\n");
            if (menuList.isEmpty()) {
                sb.append("No menu items available.");
            } else {
                for (MenuItem mi : menuList) {
                    sb.append(mi.toString()).append("\n");
                }
            }
            customerOutput.setText(sb.toString());
        });

        placeOrderBtn.setOnAction(e -> showPlaceOrder(customerOutput));
        viewCartBtn.setOnAction(e -> showCurrentCart(customerOutput));
        confirmOrderBtn.setOnAction(e -> confirmOrder(customerOutput));
        reserveTableBtn.setOnAction(e -> showReserveTable(customerOutput));
        searchOrderBtn.setOnAction(e -> showSearchOrderById(customerOutput));
        backBtn.setOnAction(e -> showAccessSelection());

        root.getChildren().addAll(label, viewMenuBtn, placeOrderBtn, viewCartBtn,
                confirmOrderBtn, reserveTableBtn, searchOrderBtn, backBtn, customerOutput);

        Scene scene = new Scene(new ScrollPane(root), 400, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Customer Menu");
    }

    // --- Customer: Place Order ---
    private void showPlaceOrder(TextArea customerOutput) {
        Stage stage = new Stage();
        stage.setTitle("Place Order");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        Label label = new Label("Select menu items to add to your cart:");
        label.setStyle("-fx-font-weight: bold;");

        // Use a ComboBox instead of ListView for simpler selection and less space
        ComboBox<MenuItem> menuComboBox = new ComboBox<>();
        menuComboBox.setItems(FXCollections.observableArrayList(menuList));
        menuComboBox.setPromptText("Select an item");
        menuComboBox.setConverter(new javafx.util.StringConverter<MenuItem>() {
            @Override
            public String toString(MenuItem object) {
                return object != null ? object.toString() : "";
            }
            @Override
            public MenuItem fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        Button addBtn = new Button("Add to Cart");
        Button cancelBtn = new Button("Done");

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: green;");

        // Display current cart items in a small area or alert
        ListView<String> currentCartDisplay = new ListView<>();
        currentCartDisplay.setPrefHeight(100);
        currentCartDisplay.setEditable(false);
        currentCartDisplay.setPlaceholder(new Label("Cart is empty."));

        // Initialize current order if null
        if (currentOrder == null) {
            currentOrder = new Order(orderCounter); // Don't increment yet, just get next ID
        }
        updateCartDisplay(currentCartDisplay);

        addBtn.setOnAction(e -> {
            MenuItem selectedItem = menuComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                msgLabel.setText("Please select an item.");
                return;
            }

            currentOrder.addItem(selectedItem);
            msgLabel.setText(selectedItem.getName() + " added to cart.");
            updateCartDisplay(currentCartDisplay);
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(label, menuComboBox, addBtn, new Separator(), new Label("Items in Cart:"), currentCartDisplay, cancelBtn, msgLabel);

        stage.setScene(new Scene(root, 350, 450));
        stage.show();
    }

    private void updateCartDisplay(ListView<String> display) {
        ObservableList<String> itemsInCart = FXCollections.observableArrayList();
        if (currentOrder != null && !currentOrder.items.isEmpty()) {
            Map<String, Integer> itemCounts = new HashMap<>();
            Map<String, Double> itemPrices = new HashMap<>();
            for (MenuItem item : currentOrder.items) {
                itemCounts.put(item.getName(), itemCounts.getOrDefault(item.getName(), 0) + 1);
                itemPrices.put(item.getName(), item.getPrice());
            }

            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                itemsInCart.add(entry.getKey() + " (x" + entry.getValue() + ") - Rs." + String.format("%.2f", entry.getValue() * itemPrices.get(entry.getKey())));
            }
            itemsInCart.add("-------------------------");
            itemsInCart.add("Total: Rs." + String.format("%.2f", currentOrder.getPrice()));
        }
        display.setItems(itemsInCart);
    }


    // --- Customer: Show Current Cart ---
    private void showCurrentCart(TextArea customerOutput) {
        if (currentOrder == null || currentOrder.items.isEmpty()) {
            customerOutput.setText("Cart is empty.");
            return;
        }
        StringBuilder sb = new StringBuilder("Current Cart:\n");
        Map<String, Integer> itemCounts = new HashMap<>();
        Map<String, Double> itemPrices = new HashMap<>();
        for (MenuItem item : currentOrder.items) {
            itemCounts.put(item.getName(), itemCounts.getOrDefault(item.getName(), 0) + 1);
            itemPrices.put(item.getName(), item.getPrice());
        }

        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            sb.append(" - ").append(entry.getKey()).append(" (x").append(entry.getValue()).append("): Rs.")
              .append(String.format("%.2f", entry.getValue() * itemPrices.get(entry.getKey()))).append("\n");
        }
        sb.append("Total Price: Rs.").append(String.format("%.2f", currentOrder.getPrice()));
        customerOutput.setText(sb.toString());
    }

    // --- Customer: Confirm Order with Payment & Coupon ---
    private void confirmOrder(TextArea customerOutput) {
        if (currentOrder == null || currentOrder.items.isEmpty()) {
            customerOutput.setText("No items in cart to confirm.");
            return;
        }

        // Coupon input dialog
        TextInputDialog couponDialog = new TextInputDialog();
        couponDialog.setTitle("Coupon Code");
        couponDialog.setHeaderText("Do you have a coupon code? Enter or leave blank:");
        couponDialog.setHeaderText("The Coupon is applied only for Ordering More than 500");
        couponDialog.setContentText("Coupon:");

        Optional<String> couponInput = couponDialog.showAndWait();
        String coupon = couponInput.orElse("").trim();

        double discount = 0;
        String couponMessage = "";

        if (currentOrder.getPrice() >= 500 && coupon.equalsIgnoreCase("SHIELD2026")) {
            discount = currentOrder.getPrice() * 0.20;
            couponMessage = "Coupon applied: 20% off. Discount: Rs." + String.format("%.2f", discount);
        } else if (!coupon.isEmpty()) {
            couponMessage = "Invalid coupon code or order total less than Rs.500. No discount applied.";
        } else {
            couponMessage = "No coupon entered.";
        }

        currentOrder.discountApplied = discount; // This field already represents the discount amount
        double finalPrice = currentOrder.getFinalPrice(); // This is the net amount

        // Show order summary before OTP
        Alert summaryAlert = new Alert(Alert.AlertType.INFORMATION);
        summaryAlert.setTitle("Order Summary");
        summaryAlert.setHeaderText("Your Order Details:");
        StringBuilder summaryContent = new StringBuilder();
        summaryContent.append("Items Total: Rs.").append(String.format("%.2f", currentOrder.getPrice())).append("\n");
        summaryContent.append("Discount: Rs.").append(String.format("%.2f", currentOrder.discountApplied)).append("\n");
        summaryContent.append(couponMessage).append("\n");
        summaryContent.append("Final Price (Net): Rs.").append(String.format("%.2f", finalPrice)).append("\n\n");
        summaryContent.append("Click OK to proceed with payment.");
        summaryAlert.setContentText(summaryContent.toString());
        summaryAlert.showAndWait();


        // OTP simulation
        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000); // 4-digit OTP

        TextInputDialog otpDialog = new TextInputDialog();
        otpDialog.setTitle("Payment Confirmation");
        otpDialog.setHeaderText("Your OTP is: " + otp + "\nEnter OTP to confirm payment for Rs." + String.format("%.2f", finalPrice));
        otpDialog.setContentText("OTP:");

        Optional<String> result = otpDialog.showAndWait();
        if (result.isPresent() && result.get().equals(String.valueOf(otp))) {
            currentOrder.status = "Confirmed";
            currentOrder.paymentStatus = "paid";
            currentOrder.orderId = orderCounter++; // Assign and increment order ID
            allOrders.add(currentOrder);
            DatabaseManager.saveOrder(currentOrder); // Save the confirmed order to DB

            customerOutput.setText("Order Confirmed! Your Order ID is: " + currentOrder.orderId +
                                   "\nTotal Amount Paid: Rs." + String.format("%.2f", finalPrice) +
                                   "\n" + couponMessage);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Payment Successful");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Payment successful! Order ID: " + currentOrder.orderId);
            successAlert.showAndWait();
            currentOrder = null; // Clear current order after confirmation
        } else {
            customerOutput.setText("Payment failed or OTP incorrect. Order not confirmed.");
            Alert failAlert = new Alert(Alert.AlertType.ERROR);
            failAlert.setTitle("Payment Failed");
            failAlert.setHeaderText(null);
            failAlert.setContentText("Payment failed or OTP incorrect.");
            failAlert.showAndWait();
        }
    }

    // --- Customer: Reserve Table ---
    private void showReserveTable(TextArea customerOutput) {
        Stage stage = new Stage();
        stage.setTitle("Reserve Table");
        VBox root = new VBox(15); // Increased spacing
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Table Reservation");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Availability Display Area
        TextArea availabilityArea = new TextArea();
        availabilityArea.setEditable(false);
        availabilityArea.setPrefHeight(150);
        availabilityArea.setPromptText("Click 'Check Available Tables' to see current availability.");

        Button checkAvailabilityBtn = new Button("Check Available Tables");
        checkAvailabilityBtn.setPrefWidth(200);

        // Call the helper method to update the display
        checkAvailabilityBtn.setOnAction(e -> updateAvailabilityDisplay(availabilityArea));

        HBox tableTypeSelection = new HBox(10);
        tableTypeSelection.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("Table Type (Seats):");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Table2", "Table4", "Table6", "Table8", "Table10");
        typeComboBox.setPromptText("Select type");
        tableTypeSelection.getChildren().addAll(typeLabel, typeComboBox);

        HBox tableNumberSelection = new HBox(10);
        tableNumberSelection.setAlignment(Pos.CENTER_LEFT);
        Label numberLabel = new Label("Table Number (1-10):");
        TextField numberField = new TextField();
        numberField.setPromptText("Enter number");
        numberField.setPrefWidth(80);
        tableNumberSelection.getChildren().addAll(numberLabel, numberField);

        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Your Phone Number");

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: red;");

        Button reserveBtn = new Button("Reserve");
        Button cancelBtn = new Button("Done");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(reserveBtn, cancelBtn);


        reserveBtn.setOnAction(e -> {
            String type = typeComboBox.getValue();
            String numberText = numberField.getText().trim();
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (type == null || numberText.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                msgLabel.setText("Please fill all fields.");
                return;
            }

            try {
                int number = Integer.parseInt(numberText);
                if (number < 1 || number > 10) {
                    msgLabel.setText("Table number must be between 1 and 10.");
                    return;
                }

                boolean[] tablesArray;
                String tableTypeString;
                switch (type) {
                    case "Table2": tablesArray = tables2; tableTypeString = "Table of 2"; break;
                    case "Table4": tablesArray = tables4; tableTypeString = "Table of 4"; break;
                    case "Table6": tablesArray = tables6; tableTypeString = "Table of 6"; break;
                    case "Table8": tablesArray = tables8; tableTypeString = "Table of 8"; break;
                    case "Table10": tablesArray = tables10; tableTypeString = "Table of 10"; break;
                    default:
                        msgLabel.setText("Invalid table type selected.");
                        return;
                }

                if (tablesArray[number - 1]) {
                    msgLabel.setText("Table " + type + " (number " + number + ") is already booked.");
                } else {
                    tablesArray[number - 1] = true; // Mark as booked
                    String customerId = "CUST" + (customerIdCounter++); // Generate customer ID
                    TableBooking newBooking = new TableBooking(customerId, orderCounter, name, phone, number, type, number);
                    bookings.add(newBooking);
                    DatabaseManager.saveTableBooking(newBooking); // Save to database

                    msgLabel.setStyle("-fx-text-fill: green;");
                    msgLabel.setText("Table " + type + " (number " + number + ") reserved for " + name +
                            ". Your Customer ID: " + customerId);
                    customerOutput.setText("Table reservation successful for " + name + " (ID: " + customerId + ")");
                    updateAvailabilityDisplay(availabilityArea); // Refresh availability after booking
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid table number.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, checkAvailabilityBtn, availabilityArea, new Separator(),
                                  new Label("Reserve a table:"), tableTypeSelection, tableNumberSelection,
                                  nameField, phoneField, buttonBox, msgLabel);

        stage.setScene(new Scene(new ScrollPane(root), 450, 600)); // Adjusted height
        stage.show();
        updateAvailabilityDisplay(availabilityArea); // Initial display of availability
    }

    // Helper method to update the table availability display
    private void updateAvailabilityDisplay(TextArea availabilityArea) {
        StringBuilder sb = new StringBuilder("Current Table Availability:\n");
        appendTableStatus(sb, "Table of 2 (Table2)", tables2);
        appendTableStatus(sb, "Table of 4 (Table4)", tables4);
        appendTableStatus(sb, "Table of 6 (Table6)", tables6);
        appendTableStatus(sb, "Table of 8 (Table8)", tables8);
        appendTableStatus(sb, "Table of 10 (Table10)", tables10);
        availabilityArea.setText(sb.toString());
    }

    private void appendTableStatus(StringBuilder sb, String typeLabel, boolean[] tables) {
        sb.append("\n").append(typeLabel).append(":\n");
        for (int i = 0; i < tables.length; i++) {
            sb.append("  - Table ").append(i + 1).append(": ").append(tables[i] ? "Booked" : "Available").append("\n");
        }
    }


    // --- Customer: Search Order by ID ---
    private void showSearchOrderById(TextArea customerOutput) {
        Stage stage = new Stage();
        stage.setTitle("Search Order");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        TextField orderIdField = new TextField();
        orderIdField.setPromptText("Enter your Order ID");

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);

        Button searchBtn = new Button("Search");
        Button cancelBtn = new Button("Cancel");

        searchBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(orderIdField.getText().trim());
                Order foundOrder = null;
                for (Order o : allOrders) {
                    if (o.orderId == id) {
                        foundOrder = o;
                        break;
                    }
                }
                if (foundOrder != null) {
                    msgLabel.setText("Order ID: " + id +
                            "\nOrder Status: " + foundOrder.status +
                            "\nPayment Status: " + foundOrder.paymentStatus +
                            "\nTotal Items: " + foundOrder.items.size() +
                            "\nTotal Price (after discount): Rs." + String.format("%.2f", foundOrder.getFinalPrice()));
                    customerOutput.setText(msgLabel.getText()); // Also update main customer output
                } else {
                    msgLabel.setText("Order ID " + id + " not found.");
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid Order ID");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(new Label("Search for your order details:"), orderIdField, searchBtn, cancelBtn, msgLabel);

        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
