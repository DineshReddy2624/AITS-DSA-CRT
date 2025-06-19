package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors; // For Java 8 stream API usage

/**
 * Main application class for the Restaurant Management System.
 * Handles UI, business logic, and interaction with the DatabaseManager.
 */
public class Main extends Application {

    // --- Data & state ---
    private List<MenuItem> menuList = new ArrayList<>();
    private Map<Integer, MenuItem> menuMap = new HashMap<>(); // Maps ID to MenuItem for quick lookup
    private ObservableList<Order> allOrders = FXCollections.observableArrayList(); // Use ObservableList for TableView
    private ObservableList<TableBooking> allBookings = FXCollections.observableArrayList(); // Use ObservableList for TableView

    private int orderCounter = 1; // Used for new order IDs
    private int customerIdCounter = 1001; // Used for new customer IDs for table bookings
    private int menuItemIdCounter = 101; // Used for new menu item IDs

    // Table availability state (using Map for better organization and future proofing)
    // Key: TableType, Value: boolean array indicating availability of 10 tables for that type
    private Map<TableType, boolean[]> tableAvailability = new HashMap<>();

    // For current session customer order (cart)
    private Order currentOrder = null;

    // --- UI Controls ---
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Initialize table availability maps
        for (TableType type : TableType.values()) {
            tableAvailability.put(type, new boolean[10]); // 10 tables of each type, initially all available (false)
        }

        // Test database connection and initialize schema
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
            showAlert(Alert.AlertType.ERROR, "Database Connection Error", "Failed to connect to the database!",
                    "Please ensure your MySQL server is running and DBConnection.java details are correct.");
            System.exit(1); // Exit application if no DB connection
        }

        DatabaseManager.initializeDatabase(); // Create tables if they don't exist
        loadAllDataFromDatabase(); // Load all existing data from DB
        showAccessSelection(); // Show the main access selection screen
    }

    /**
     * Loads all existing data (menu items, orders, bookings) from the database
     * into the application's memory.
     */
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
        allOrders.clear();
        allOrders.addAll(DatabaseManager.loadOrders(menuMap)); // Pass menuMap for linking items
        if (!allOrders.isEmpty()) {
            orderCounter = allOrders.stream().mapToInt(o -> o.orderId).max().orElse(0) + 1;
        }

        // Load Table Bookings
        allBookings.clear();
        allBookings.addAll(DatabaseManager.loadTableBookings());
        if (!allBookings.isEmpty()) {
            customerIdCounter = allBookings.stream()
                                    .map(b -> b.getCustomerId().replace("CUST", "")) // Use getter
                                    .filter(s -> s.matches("\\d+")) // Ensure it's a number
                                    .mapToInt(Integer::parseInt)
                                    .max().orElse(0) + 1;

            // Reconstruct table availability based on loaded bookings
            resetTableAvailability(); // Reset all tables to available first
            for (TableBooking booking : allBookings) {
                // Mark the table as occupied if the booking exists and is not cancelled
                if (booking.getPaymentStatus() != PaymentStatus.CANCELLED) { // Use getter
                    boolean[] tables = tableAvailability.get(booking.getTableType()); // Use getter
                    if (tables != null && booking.getTableNumber() > 0 && booking.getTableNumber() <= tables.length) { // Use getter
                        tables[booking.getTableNumber() - 1] = true;
                    }
                }
            }
        }
    }

    /**
     * Initializes a set of default menu items and saves them to the database
     * if no items are found during initial load.
     */
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

    /**
     * Resets all table availability to 'available' (false).
     * Used before reconstructing availability from loaded bookings.
     */
    private void resetTableAvailability() {
        for (boolean[] tables : tableAvailability.values()) {
            Arrays.fill(tables, false);
        }
    }

    /**
     * Displays the initial access selection screen (Admin vs Customer).
     */
    private void showAccessSelection() {
        VBox root = new VBox(20); // Increased spacing
        root.setPadding(new Insets(30)); // Increased padding
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f4f4f4;"); // Light background

        Label welcomeLabel = new Label("Welcome to Shield Restaurant");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Button adminBtn = createStyledButton("Admin Access");
        Button customerBtn = createStyledButton("Customer Access");
        Button exitBtn = createStyledButton("Exit");

        adminBtn.setOnAction(e -> showAdminLogin());
        customerBtn.setOnAction(e -> showCustomerMenu());
        exitBtn.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(welcomeLabel, adminBtn, customerBtn, exitBtn);

        Scene scene = new Scene(root, 400, 350); // Adjusted size
        primaryStage.setScene(scene);
        primaryStage.setTitle("Shield Restaurant - Access Selection");
        primaryStage.show();
    }

    /**
     * Helper method to create a styled button.
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px; -fx-background-radius: 5px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px; -fx-background-radius: 5px;"));
        return button;
    }

    /**
     * Helper method to create a styled secondary button.
     */
    private Button createSecondaryStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(30);
        button.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #007bb5; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-background-radius: 5px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5px; -fx-background-radius: 5px;"));
        return button;
    }

    /**
     * Helper method to create a styled small button for dialogs.
     */
    private Button createSmallDialogButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(80);
        button.setPrefHeight(25);
        button.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-size: 13px; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #4cae4c; -fx-text-fill: white; -fx-font-size: 13px; -fx-border-radius: 3px; -fx-background-radius: 3px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-size: 13px; -fx-border-radius: 3px; -fx-background-radius: 3px;"));
        return button;
    }

    /**
     * Displays a custom alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Admin Login Screen ---
    private void showAdminLogin() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #e0f2f7;"); // Light blue background

        Label prompt = new Label("Enter 4-digit admin PIN:");
        prompt.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #01579B;");

        PasswordField pinField = new PasswordField();
        pinField.setMaxWidth(200);
        pinField.setPromptText("PIN");
        pinField.setStyle("-fx-border-color: #01579B; -fx-border-radius: 5px;");

        Button loginBtn = createStyledButton("Login");
        Button backBtn = createSecondaryStyledButton("Back");

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        loginBtn.setOnAction(e -> {
            String pin = pinField.getText();
            if ("2004".equals(pin)) {
                showAdminMenu();
            } else {
                msgLabel.setText("Invalid PIN. Access denied.");
                pinField.clear();
            }
        });

        backBtn.setOnAction(e -> showAccessSelection());

        root.getChildren().addAll(prompt, pinField, loginBtn, backBtn, msgLabel);

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Login");
    }

    // --- Admin Menu Screen ---
    private void showAdminMenu() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #e8eaf6;"); // Lighter blue/purple background

        Label titleLabel = new Label("Admin Portal");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        VBox menuButtons = new VBox(10);
        menuButtons.setPadding(new Insets(10));
        menuButtons.setAlignment(Pos.TOP_LEFT);
        menuButtons.setStyle("-fx-background-color: #c5cae9; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Button viewMenuBtn = createStyledButton("View Menu Items");
        Button editItemBtn = createStyledButton("Add Menu Item"); // New functionality
        Button removeItemBtn = createStyledButton("Remove Menu Item by ID");
        Button viewBookingsBtn = createStyledButton("View All Table Bookings");
        Button updateBookingStatusBtn = createStyledButton("Update Table Booking Payment Status"); // New functionality
        Button viewOrdersBtn = createStyledButton("View All Orders");
        Button checkPaymentStatusBtn = createStyledButton("Update Order Payment Status by ID");
        Button searchMenuBtn = createStyledButton("Search Menu Items by Name");
        Button dailyReportBtn = createStyledButton("Generate Daily Sales Report");
        Button logoutBtn = createSecondaryStyledButton("Logout");

        menuButtons.getChildren().addAll(viewMenuBtn, editItemBtn, removeItemBtn,
                                         viewBookingsBtn, updateBookingStatusBtn, viewOrdersBtn,
                                         checkPaymentStatusBtn, searchMenuBtn, dailyReportBtn,
                                         new Separator(), logoutBtn); // Separator for visual grouping

        root.setLeft(menuButtons);

        // Central display area using TabPane for different views
        TabPane displayTabs = new TabPane();
        displayTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        displayTabs.setPadding(new Insets(10));

        // Tab for Menu Items
        Tab menuTab = new Tab("Menu Items");
        menuTab.setContent(createMenuItemTableView()); // Use TableView for menu items
        displayTabs.getTabs().add(menuTab);

        // Tab for Orders
        Tab ordersTab = new Tab("Orders");
        ordersTab.setContent(createOrderTableView()); // Use TableView for orders
        displayTabs.getTabs().add(ordersTab);

        // Tab for Bookings
        Tab bookingsTab = new Tab("Table Bookings");
        bookingsTab.setContent(createTableBookingTableView()); // Use TableView for bookings
        displayTabs.getTabs().add(bookingsTab);

        // Tab for Reports/Search (initially empty, filled on action)
        Tab reportTab = new Tab("Reports / Search Results");
        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPromptText("Reports and search results will appear here...");
        reportTab.setContent(reportArea);
        displayTabs.getTabs().add(reportTab);

        root.setCenter(displayTabs);

        // Event handlers
        viewMenuBtn.setOnAction(e -> displayTabs.getSelectionModel().select(menuTab));
        editItemBtn.setOnAction(e -> showAddEditMenuItemDialog(getMenuTableView().getSelectionModel().getSelectedItem())); // Pass selected for edit mode
        removeItemBtn.setOnAction(e -> showRemoveMenuItem());
        viewBookingsBtn.setOnAction(e -> displayTabs.getSelectionModel().select(bookingsTab));
        updateBookingStatusBtn.setOnAction(e -> showUpdateTableBookingPaymentStatus());
        viewOrdersBtn.setOnAction(e -> displayTabs.getSelectionModel().select(ordersTab));
        checkPaymentStatusBtn.setOnAction(e -> showUpdateOrderPaymentStatus());
        searchMenuBtn.setOnAction(e -> showSearchMenuItem(reportArea)); // Direct output to reportArea
        dailyReportBtn.setOnAction(e -> generateDailySalesReport(reportArea)); // Direct output to reportArea
        logoutBtn.setOnAction(e -> showAccessSelection());

        Scene scene = new Scene(root, 1000, 700); // Increased overall size for better layout
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Dashboard");
    }

    /**
     * Creates and configures a TableView for displaying MenuItems.
     */
    private TableView<MenuItem> createMenuItemTableView() {
        TableView<MenuItem> tableView = new TableView<>();
        tableView.setEditable(false); // Make it non-editable by default

        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price (Rs.)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, nameCol, priceCol);
        tableView.setItems(FXCollections.observableArrayList(menuList)); // Set initial data

        return tableView;
    }

    /**
     * Helper to get the MenuItem TableView instance from the Admin Menu layout.
     * This is a workaround as TableView is inside a Tab.
     */
    @SuppressWarnings("unchecked")
    private TableView<MenuItem> getMenuTableView() {
        BorderPane adminRoot = (BorderPane) primaryStage.getScene().getRoot();
        TabPane displayTabs = (TabPane) adminRoot.getCenter();
        Tab menuTab = displayTabs.getTabs().get(0); // Assuming Menu Items is the first tab
        return (TableView<MenuItem>) menuTab.getContent();
    }

    /**
     * Creates and configures a TableView for displaying Orders.
     */
    private TableView<Order> createOrderTableView() {
        TableView<Order> tableView = new TableView<>();
        tableView.setEditable(false);

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId")); // Uses getOrderId()
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        // Uses getStatus().getDisplayValue()
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().getDisplayValue()));
        statusCol.setPrefWidth(100);

        TableColumn<Order, String> paymentStatusCol = new TableColumn<>("Payment Status");
        // Uses getPaymentStatus().getDisplayValue()
        paymentStatusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus().getDisplayValue()));
        paymentStatusCol.setPrefWidth(120);

        TableColumn<Order, Double> totalAmountCol = new TableColumn<>("Total (Rs.)");
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("price")); // Uses getPrice()
        totalAmountCol.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.2f", item));
            }
        });
        totalAmountCol.setPrefWidth(90);

        TableColumn<Order, Double> discountCol = new TableColumn<>("Discount (Rs.)");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountApplied")); // Uses getDiscountApplied()
        discountCol.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.2f", item));
            }
        });
        discountCol.setPrefWidth(90);

        TableColumn<Order, Double> netAmountCol = new TableColumn<>("Net Amount (Rs.)");
        netAmountCol.setCellValueFactory(new PropertyValueFactory<>("finalPrice")); // Uses getFinalPrice()
        netAmountCol.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.2f", item));
            }
        });
        netAmountCol.setPrefWidth(100);

        tableView.getColumns().addAll(orderIdCol, statusCol, paymentStatusCol, totalAmountCol, discountCol, netAmountCol);
        tableView.setItems(allOrders);
        return tableView;
    }

    /**
     * Creates and configures a TableView for displaying TableBookings.
     */
    private TableView<TableBooking> createTableBookingTableView() {
        TableView<TableBooking> tableView = new TableView<>();
        tableView.setEditable(false);

        TableColumn<TableBooking, String> customerIdCol = new TableColumn<>("Cust ID");
        // FIX: Changed to use "customerId" which implies getCustomerId()
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerIdCol.setPrefWidth(80);

        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Customer Name");
        // FIX: Changed to use "customerName" which implies getCustomerName()
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setPrefWidth(150);

        TableColumn<TableBooking, String> tableTypeCol = new TableColumn<>("Table Type");
        // Uses getTableType().getDisplayValue()
        tableTypeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTableType().getDisplayValue()));
        tableTypeCol.setPrefWidth(100);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        // FIX: Changed to use "tableNumber" which implies getTableNumber()
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(80);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        // FIX: Changed to use "seats" which implies getSeats()
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(60);

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        // FIX: Changed to use "phone" which implies getPhone()
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<TableBooking, String> paymentStatusCol = new TableColumn<>("Payment Status");
        // Uses getPaymentStatus().getDisplayValue()
        paymentStatusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus().getDisplayValue()));
        paymentStatusCol.setPrefWidth(120);

        TableColumn<TableBooking, Double> bookingFeeCol = new TableColumn<>("Fee (Rs.)");
        // FIX: Changed to use "bookingFee" which implies getBookingFee()
        bookingFeeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        bookingFeeCol.setCellFactory(column -> new TableCell<TableBooking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.2f", item));
            }
        });
        bookingFeeCol.setPrefWidth(90);

        tableView.getColumns().addAll(customerIdCol, customerNameCol, tableTypeCol, tableNumberCol, seatsCol, phoneCol, paymentStatusCol, bookingFeeCol);
        tableView.setItems(allBookings);
        return tableView;
    }

    /**
     * Helper to get the TableBooking TableView instance from the Admin Menu layout.
     */
    @SuppressWarnings("unchecked")
    private TableView<TableBooking> getBookingTableView() {
        BorderPane adminRoot = (BorderPane) primaryStage.getScene().getRoot();
        TabPane displayTabs = (TabPane) adminRoot.getCenter();
        Tab bookingsTab = displayTabs.getTabs().get(2); // Assuming Table Bookings is the third tab
        return (TableView<TableBooking>) bookingsTab.getContent();
    }


    // --- Admin: Add/Edit Menu Item Dialog ---
    private void showAddEditMenuItemDialog(MenuItem itemToEdit) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Block parent window
        stage.setTitle(itemToEdit == null ? "Add New Menu Item" : "Edit Menu Item");

        GridPane root = new GridPane();
        root.setVgap(10);
        root.setHgap(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f4c3; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label title = new Label(itemToEdit == null ? "Enter new menu item details:" : "Edit menu item details:");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        root.add(title, 0, 0, 2, 1);

        Label nameLabel = new Label("Item Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Chicken Biryani");
        nameField.setText(itemToEdit != null ? itemToEdit.getName() : "");
        root.add(nameLabel, 0, 1);
        root.add(nameField, 1, 1);

        Label priceLabel = new Label("Item Price:");
        TextField priceField = new TextField();
        priceField.setPromptText("e.g., 250.00");
        priceField.setText(itemToEdit != null ? String.valueOf(itemToEdit.getPrice()) : "");
        root.add(priceLabel, 0, 2);
        root.add(priceField, 1, 2);

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        root.add(msgLabel, 0, 4, 2, 1);

        Button saveBtn = createSmallDialogButton(itemToEdit == null ? "Add" : "Save");
        Button cancelBtn = createSmallDialogButton("Cancel");

        HBox buttonBar = new HBox(10, saveBtn, cancelBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        root.add(buttonBar, 1, 3);

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();

            if (name.isEmpty() || priceText.isEmpty()) {
                msgLabel.setText("Fill all fields.");
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    msgLabel.setText("Price must be positive.");
                    return;
                }

                if (itemToEdit == null) { // Add mode
                    MenuItem newItem = new MenuItem(menuItemIdCounter, name, price);
                    menuList.add(newItem);
                    menuMap.put(newItem.getId(), newItem);
                    DatabaseManager.saveMenuItem(newItem); // Save to database
                    menuItemIdCounter++; // Increment after successful addition
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Added", "Item added: " + newItem.toString());
                } else { // Edit mode
                    itemToEdit.setName(name);
                    itemToEdit.setPrice(price);
                    DatabaseManager.saveMenuItem(itemToEdit); // Update in database (upsert logic)
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Updated", "Item updated: " + itemToEdit.toString());
                }
                // Refresh the TableView
                getMenuTableView().setItems(FXCollections.observableArrayList(menuList));
                stage.close();
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid price. Please enter a number.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 400, 250);
        stage.setScene(scene);
        stage.showAndWait(); // Wait for this dialog to close
    }


    // --- Admin: Remove Menu Item ---
    private void showRemoveMenuItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Menu Item");
        dialog.setHeaderText("Enter the ID of the menu item to remove:");
        dialog.setContentText("Item ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(idText -> {
            try {
                int id = Integer.parseInt(idText.trim());
                MenuItem removedItem = null;
                Iterator<MenuItem> iterator = menuList.iterator();
                while (iterator.hasNext()) {
                    MenuItem item = iterator.next();
                    if (item.getId() == id) {
                        removedItem = item;
                        iterator.remove(); // Remove from local list
                        menuMap.remove(id); // Remove from map
                        DatabaseManager.deleteMenuItem(id); // Delete from database
                        break;
                    }
                }
                if (removedItem != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Item Removed", "Removed item: " + removedItem.toString());
                    getMenuTableView().setItems(FXCollections.observableArrayList(menuList)); // Refresh TableView
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Removal Failed", "No item with ID " + id + " found.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Error", "Invalid ID. Please enter a number.");
            }
        });
    }

    // --- Admin: Update Order Payment Status ---
    private void showUpdateOrderPaymentStatus() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Update Order Payment Status");
        idDialog.setHeaderText("Enter the Order ID:");
        idDialog.setContentText("Order ID:");

        Optional<String> orderIdResult = idDialog.showAndWait();
        orderIdResult.ifPresent(idText -> {
            try {
                int orderId = Integer.parseInt(idText.trim());
                Order foundOrder = allOrders.stream()
                                            .filter(o -> o.getOrderId() == orderId) // Use getter
                                            .findFirst()
                                            .orElse(null);

                if (foundOrder != null) {
                    ChoiceDialog<PaymentStatus> statusDialog = new ChoiceDialog<>(foundOrder.getPaymentStatus(), PaymentStatus.values()); // Use getter
                    statusDialog.setTitle("Update Order Payment Status");
                    statusDialog.setHeaderText("Order ID: " + orderId + "\nCurrent Payment Status: " + foundOrder.getPaymentStatus().getDisplayValue()); // Use getter
                    statusDialog.setContentText("Select new status:");

                    Optional<PaymentStatus> newStatus = statusDialog.showAndWait();
                    newStatus.ifPresent(statusToSet -> {
                        foundOrder.paymentStatus = statusToSet; // Directly update public field, or use setter if available
                        DatabaseManager.updateOrderPaymentStatus(foundOrder.getOrderId(), statusToSet); // Use getter
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Status Updated", "Order ID: " + orderId + "\nPayment Status Updated to: " + statusToSet.getDisplayValue());
                        // Refresh the TableView directly
                        TableView<Order> orderTableView = (TableView<Order>) ((TabPane)((BorderPane) primaryStage.getScene().getRoot()).getCenter()).getTabs().get(1).getContent();
                        orderTableView.refresh();
                    });
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Order Not Found", "Order ID " + orderId + " not found.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Error", "Invalid Order ID. Please enter a number.");
            }
        });
    }

    // --- Admin: Update Table Booking Payment Status ---
    private void showUpdateTableBookingPaymentStatus() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Update Table Booking Payment Status");
        idDialog.setHeaderText("Enter the Customer ID for the booking:");
        idDialog.setContentText("Customer ID:");

        Optional<String> customerIdResult = idDialog.showAndWait();
        customerIdResult.ifPresent(custIdText -> {
            String customerId = custIdText.trim();
            TableBooking foundBooking = allBookings.stream()
                                                .filter(b -> b.getCustomerId().equalsIgnoreCase(customerId)) // Use getter
                                                .findFirst()
                                                .orElse(null);

            if (foundBooking != null) {
                ChoiceDialog<PaymentStatus> statusDialog = new ChoiceDialog<>(foundBooking.getPaymentStatus(), PaymentStatus.values()); // Use getter
                statusDialog.setTitle("Update Table Booking Payment Status");
                statusDialog.setHeaderText("Customer ID: " + customerId + "\nCurrent Payment Status: " + foundBooking.getPaymentStatus().getDisplayValue()); // Use getter
                statusDialog.setContentText("Select new status:");

                Optional<PaymentStatus> newStatus = statusDialog.showAndWait();
                newStatus.ifPresent(statusToSet -> {
                    foundBooking.paymentStatus = statusToSet; // Directly update public field, or use setter if available
                    DatabaseManager.updateTableBookingPaymentStatus(foundBooking.getCustomerId(), statusToSet); // Use getter
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Status Updated", "Booking for Customer ID: " + customerId + "\nPayment Status Updated to: " + statusToSet.getDisplayValue());
                    getBookingTableView().refresh(); // Refresh the TableView
                    loadAllDataFromDatabase(); // Reload data to update table availability
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found", "Booking Not Found", "Booking for Customer ID " + customerId + " not found.");
            }
        });
    }


    // --- Admin: Search Menu Items ---
    private void showSearchMenuItem(TextArea adminOutput) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
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

        Button searchBtn = createSmallDialogButton("Search");
        Button cancelBtn = createSmallDialogButton("Done");

        searchBtn.setOnAction(e -> {
            String keyword = keywordField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                resultsArea.setText("Enter a keyword to search.");
                return;
            }
            StringBuilder sb = new StringBuilder("Search Results for '" + keyword + "':\n--------------------------\n");
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

        Scene scene = new Scene(root, 350, 350);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // --- Admin: Daily Sales Report ---
    private void generateDailySalesReport(TextArea adminOutput) {
        double totalSales = 0;
        int paidOrdersCount = 0;
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("--- Daily Sales Report ---\n");
        reportBuilder.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        reportBuilder.append("----------------------------\n");

        // Filter for paid orders and calculate sales
        List<Order> paidOrders = allOrders.stream()
                                          .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID) // Use getter
                                          .collect(Collectors.toList());

        for (Order order : paidOrders) {
            totalSales += order.getFinalPrice(); // Uses getter
            paidOrdersCount++;
            reportBuilder.append(String.format("Order ID: %d, Net Amount: Rs.%.2f\n", order.getOrderId(), order.getFinalPrice())); // Use getters
        }

        reportBuilder.append("\nSummary:\n");
        reportBuilder.append("Total Number of Paid Orders: ").append(paidOrdersCount).append("\n");
        reportBuilder.append("Total Sales (Net Amount): Rs.").append(String.format("%.2f", totalSales)).append("\n");
        reportBuilder.append("----------------------------\n");

        adminOutput.setText(reportBuilder.toString());
        showAlert(Alert.AlertType.INFORMATION, "Report Generated", "Daily Sales Report", "Report generated in the output area.");
    }

    // --- Customer Menu ---
    private void showCustomerMenu() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #e3f2fd;"); // Light blue background

        Label titleLabel = new Label("Customer Menu");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0D47A1;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        VBox menuButtons = new VBox(10);
        menuButtons.setPadding(new Insets(10));
        menuButtons.setAlignment(Pos.TOP_LEFT);
        menuButtons.setStyle("-fx-background-color: #bbdefb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Button viewMenuBtn = createStyledButton("View Menu Items");
        Button placeOrderBtn = createStyledButton("Place New Order");
        Button viewCartBtn = createStyledButton("View Current Cart");
        Button confirmOrderBtn = createStyledButton("Confirm & Pay Order");
        Button reserveTableBtn = createStyledButton("Reserve Table");
        Button searchOrderBtn = createStyledButton("Search Order by ID");
        Button backBtn = createSecondaryStyledButton("Back to Access Selection");

        menuButtons.getChildren().addAll(viewMenuBtn, placeOrderBtn, viewCartBtn,
                                         confirmOrderBtn, reserveTableBtn, searchOrderBtn,
                                         new Separator(), backBtn);

        root.setLeft(menuButtons);

        // Central display area
        TabPane customerDisplayTabs = new TabPane();
        customerDisplayTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        customerDisplayTabs.setPadding(new Insets(10));

        Tab menuTab = new Tab("Menu");
        menuTab.setContent(createMenuItemTableView()); // Re-use menu item table view
        customerDisplayTabs.getTabs().add(menuTab);

        Tab cartTab = new Tab("Your Cart");
        ListView<String> currentCartDisplay = new ListView<>();
        currentCartDisplay.setPlaceholder(new Label("Your cart is empty."));
        currentCartDisplay.setEditable(false);
        cartTab.setContent(currentCartDisplay);
        customerDisplayTabs.getTabs().add(cartTab);

        Tab outputTab = new Tab("Messages / Order Info");
        TextArea customerOutputArea = new TextArea();
        customerOutputArea.setEditable(false);
        customerOutputArea.setPromptText("Messages and order details will appear here...");
        outputTab.setContent(customerOutputArea);
        customerDisplayTabs.getTabs().add(outputTab);

        root.setCenter(customerDisplayTabs);

        // Event handlers
        viewMenuBtn.setOnAction(e -> customerDisplayTabs.getSelectionModel().select(menuTab));
        placeOrderBtn.setOnAction(e -> showPlaceOrder(customerOutputArea, currentCartDisplay));
        viewCartBtn.setOnAction(e -> {
            updateCartDisplay(currentCartDisplay);
            customerDisplayTabs.getSelectionModel().select(cartTab);
        });
        confirmOrderBtn.setOnAction(e -> confirmOrder(customerOutputArea, currentCartDisplay));
        reserveTableBtn.setOnAction(e -> showReserveTable(customerOutputArea));
        searchOrderBtn.setOnAction(e -> showSearchOrderById(customerOutputArea));
        backBtn.setOnAction(e -> showAccessSelection());

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Customer Menu");
    }


    /**
     * Updates the display of items in the current customer cart.
     * @param display The ListView used to show cart items.
     */
    private void updateCartDisplay(ListView<String> display) {
        ObservableList<String> itemsInCart = FXCollections.observableArrayList();
        if (currentOrder != null && !currentOrder.items.isEmpty()) {
            // Use a map to count quantities of each unique item
            Map<Integer, Integer> itemQuantities = new HashMap<>();
            Map<Integer, MenuItem> uniqueItems = new HashMap<>(); // To get the name and price once

            for (MenuItem item : currentOrder.items) {
                itemQuantities.put(item.getId(), itemQuantities.getOrDefault(item.getId(), 0) + 1);
                uniqueItems.put(item.getId(), item);
            }

            for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
                MenuItem item = uniqueItems.get(entry.getKey());
                // FIX: Corrected string concatenation for displaying item ID
                itemsInCart.add(item.getName() + " (x" + entry.getValue() + ") - Rs." + String.format("%.2f", entry.getValue() * item.getPrice()) + " [ID: " + item.getId() + "]");
            }
            itemsInCart.add("-------------------------");
            itemsInCart.add("Subtotal: Rs." + String.format("%.2f", currentOrder.getPrice())); // Uses getPrice()
            itemsInCart.add("Discount: Rs." + String.format("%.2f", currentOrder.getDiscountApplied())); // Uses getDiscountApplied()
            itemsInCart.add("Final Total: Rs." + String.format("%.2f", currentOrder.getFinalPrice())); // Uses getFinalPrice()
        } else {
            itemsInCart.add("Cart is empty.");
        }
        display.setItems(itemsInCart);
    }

    // --- Customer: Place Order ---
    private void showPlaceOrder(TextArea customerOutput, ListView<String> currentCartDisplay) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Place Order");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffe0b2;"); // Light orange background

        Label title = new Label("Select menu items to add to your cart:");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Use a ComboBox to select items
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
        menuComboBox.setPrefWidth(250);

        Button addBtn = createSmallDialogButton("Add to Cart");
        Button removeBtn = createSmallDialogButton("Remove Last Item"); // New: Remove last added item
        Button doneBtn = createSmallDialogButton("Done");

        HBox itemActions = new HBox(10, addBtn, removeBtn);
        itemActions.setAlignment(Pos.CENTER);

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        // Initialize current order if null
        if (currentOrder == null) {
            currentOrder = new Order(orderCounter); // Don't increment yet, just get next ID
        }
        updateCartDisplay(currentCartDisplay); // Refresh cart display in main customer menu

        addBtn.setOnAction(e -> {
            MenuItem selectedItem = menuComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                msgLabel.setText("Please select an item.");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            currentOrder.addItem(selectedItem);
            msgLabel.setText(selectedItem.getName() + " added to cart.");
            msgLabel.setStyle("-fx-text-fill: green;");
            updateCartDisplay(currentCartDisplay);
        });

        removeBtn.setOnAction(e -> {
            if (currentOrder != null && !currentOrder.items.isEmpty()) {
                MenuItem removed = currentOrder.items.remove(currentOrder.items.size() - 1);
                msgLabel.setText(removed.getName() + " removed from cart.");
                msgLabel.setStyle("-fx-text-fill: orange;");
                updateCartDisplay(currentCartDisplay);
            } else {
                msgLabel.setText("Cart is already empty.");
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });

        doneBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, menuComboBox, itemActions, new Separator(), msgLabel, doneBtn);

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.showAndWait(); // Use showAndWait to keep this window active
    }

    /**
     * Handles the confirmation and payment process for the current order.
     * Includes coupon application and OTP simulation.
     */
    private void confirmOrder(TextArea customerOutput, ListView<String> currentCartDisplay) {
        if (currentOrder == null || currentOrder.items.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart Empty", "No items to confirm", "Please add items to your cart before confirming.");
            return;
        }

        // --- Coupon Input ---
        TextInputDialog couponDialog = new TextInputDialog();
        couponDialog.setTitle("Coupon Code");
        couponDialog.setHeaderText("Apply Coupon Code (optional):");
        couponDialog.setContentText("Coupon:");

        Optional<String> couponInput = couponDialog.showAndWait();
        String coupon = couponInput.orElse("").trim();

        double discount = 0;
        String couponMessage = "No coupon entered.";

        if (!coupon.isEmpty()) {
            if (currentOrder.getPrice() >= 500 && coupon.equalsIgnoreCase("SHIELD2026")) {
                discount = currentOrder.getPrice() * 0.20;
                couponMessage = "Coupon 'SHIELD2026' applied: 20% off!";
            } else {
                couponMessage = "Invalid coupon code or order total less than Rs.500. No discount applied.";
            }
        }
        currentOrder.discountApplied = discount; // Update the discount
        double finalPrice = currentOrder.getFinalPrice(); // Recalculate final price

        // --- Order Summary Confirmation ---
        Alert summaryAlert = new Alert(Alert.AlertType.CONFIRMATION); // Changed to CONFIRMATION
        summaryAlert.setTitle("Order Summary");
        summaryAlert.setHeaderText("Please review your order details:");
        StringBuilder summaryContent = new StringBuilder();
        summaryContent.append("Items Total: Rs.").append(String.format("%.2f", currentOrder.getPrice())).append("\n");
        summaryContent.append("Discount: Rs.").append(String.format("%.2f", currentOrder.getDiscountApplied())).append("\n"); // Use getter
        summaryContent.append(couponMessage).append("\n");
        summaryContent.append("Final Amount Due: Rs.").append(String.format("%.2f", finalPrice)).append("\n\n");
        summaryContent.append("Click OK to proceed to payment, or Cancel to go back.");
        summaryAlert.setContentText(summaryContent.toString());

        Optional<ButtonType> summaryResult = summaryAlert.showAndWait();
        if (summaryResult.orElse(ButtonType.CANCEL) != ButtonType.OK) {
            customerOutput.setText("Order confirmation cancelled.");
            return; // User cancelled summary
        }

        // --- OTP Simulation ---
        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000); // 4-digit OTP

        TextInputDialog otpDialog = new TextInputDialog();
        otpDialog.setTitle("Payment Confirmation");
        otpDialog.setHeaderText("Your OTP for payment is: " + otp + "\nEnter OTP to confirm payment for Rs." + String.format("%.2f", finalPrice));
        otpDialog.setContentText("OTP:");

        Optional<String> result = otpDialog.showAndWait();
        if (result.isPresent() && result.get().equals(String.valueOf(otp))) {
            currentOrder.status = OrderStatus.CONFIRMED;
            currentOrder.paymentStatus = PaymentStatus.PAID;
            currentOrder.orderId = orderCounter++; // Assign and increment order ID
            allOrders.add(currentOrder); // Add to observable list
            DatabaseManager.saveOrder(currentOrder); // Save the confirmed order to DB

            customerOutput.setText("Order Confirmed! Your Order ID is: " + currentOrder.getOrderId() + // Use getter
                                   "\nTotal Amount Paid: Rs." + String.format("%.2f", finalPrice) +
                                   "\n" + couponMessage);
            showAlert(Alert.AlertType.INFORMATION, "Payment Successful", null, "Payment successful! Order ID: " + currentOrder.getOrderId()); // Use getter
            currentOrder = null; // Clear current order after confirmation
            updateCartDisplay(currentCartDisplay); // Clear cart display
        } else {
            customerOutput.setText("Payment failed or OTP incorrect. Order not confirmed.");
            showAlert(Alert.AlertType.ERROR, "Payment Failed", null, "Payment failed or OTP incorrect.");
        }
    }

    // --- Customer: Reserve Table ---
    private void showReserveTable(TextArea customerOutput) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Reserve Table");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #e8f5e9;"); // Light green background

        Label title = new Label("Table Reservation");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        // Availability Display Area
        TextArea availabilityArea = new TextArea();
        availabilityArea.setEditable(false);
        availabilityArea.setPrefHeight(200);
        availabilityArea.setPromptText("Click 'Check Available Tables' to see current availability.");
        availabilityArea.setStyle("-fx-control-inner-background: #ffffff; -fx-font-family: 'Monospaced';");

        Button checkAvailabilityBtn = createSmallDialogButton("Check Available Tables");

        HBox tableTypeSelection = new HBox(10);
        tableTypeSelection.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("Table Type (Seats):");
        ComboBox<TableType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(TableType.values()); // Populate with enum values
        typeComboBox.setPromptText("Select type");
        typeComboBox.setPrefWidth(150);
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
        msgLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button reserveBtn = createSmallDialogButton("Reserve & Pay");
        Button cancelBtn = createSmallDialogButton("Close");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(reserveBtn, cancelBtn);


        checkAvailabilityBtn.setOnAction(e -> updateAvailabilityDisplay(availabilityArea));

        reserveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            TableType type = typeComboBox.getValue();
            String numberText = numberField.getText().trim();

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

                boolean[] tablesArray = tableAvailability.get(type);
                if (tablesArray == null) {
                    msgLabel.setText("Invalid table type selected."); // Should not happen with ComboBox
                    return;
                }

                if (tablesArray[number - 1]) {
                    msgLabel.setText("Table " + type.getDisplayValue() + " (number " + number + ") is already booked.");
                } else {
                    // Proceed to payment for booking
                    double bookingFee = 100.00; // Example fixed booking fee

                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("Confirm Table Booking");
                    confirmationAlert.setHeaderText("Confirm booking for " + name + " at " + type.getDisplayValue() + " #" + number + "?");
                    confirmationAlert.setContentText("A booking fee of Rs." + String.format("%.2f", bookingFee) + " will be charged.");

                    Optional<ButtonType> confirmResult = confirmationAlert.showAndWait();
                    if (confirmResult.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        // Simulate OTP payment for booking fee
                        Random rand = new Random();
                        int otp = 1000 + rand.nextInt(9000); // 4-digit OTP

                        TextInputDialog otpDialog = new TextInputDialog();
                        otpDialog.setTitle("Booking Payment Confirmation");
                        otpDialog.setHeaderText("Your OTP is: " + otp + "\nEnter OTP to confirm payment for Rs." + String.format("%.2f", bookingFee));
                        otpDialog.setContentText("OTP:");

                        Optional<String> otpResult = otpDialog.showAndWait();
                        if (otpResult.isPresent() && otpResult.get().equals(String.valueOf(otp))) {
                            // Payment successful, proceed with booking
                            tablesArray[number - 1] = true; // Mark as booked
                            String customerId = "CUST" + (customerIdCounter++); // Generate customer ID
                            TableBooking newBooking = new TableBooking(customerId, name, phone, type, number, bookingFee);
                            newBooking.paymentStatus = PaymentStatus.PAID; // Set to paid upon successful payment
                            allBookings.add(newBooking); // Add to observable list
                            DatabaseManager.saveTableBooking(newBooking); // Save to database

                            msgLabel.setStyle("-fx-text-fill: green;");
                            msgLabel.setText("Table " + type.getDisplayValue() + " (number " + number + ") reserved and paid for " + name +
                                    ". Your Customer ID: " + newBooking.getCustomerId()); // Use getter
                            customerOutput.setText("Table reservation successful for " + name + " (ID: " + newBooking.getCustomerId() + ")"); // Use getter
                            updateAvailabilityDisplay(availabilityArea); // Refresh availability after booking
                            showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed", null, "Table booked and paid! Customer ID: " + newBooking.getCustomerId()); // Use getter
                            stage.close(); // Close reservation dialog on success
                        } else {
                            msgLabel.setText("Payment failed or OTP incorrect. Table not reserved.");
                            showAlert(Alert.AlertType.ERROR, "Payment Failed", null, "Payment failed or OTP incorrect. Table not reserved.");
                        }
                    } else {
                        msgLabel.setText("Table booking cancelled by user.");
                    }
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid table number.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, checkAvailabilityBtn, availabilityArea, new Separator(),
                                  new Label("Reserve a table:"), tableTypeSelection, tableNumberSelection,
                                  nameField, phoneField, buttonBox, msgLabel);

        Scene scene = new Scene(new ScrollPane(root), 500, 700); // Adjusted size
        stage.setScene(scene);
        stage.showAndWait(); // Use showAndWait for this dialog
    }

    /**
     * Helper method to update the table availability display in a TextArea.
     * Iterates through all table types and their numbers.
     * @param availabilityArea The TextArea to update.
     */
    private void updateAvailabilityDisplay(TextArea availabilityArea) {
        StringBuilder sb = new StringBuilder("--- Current Table Availability ---\n");
        for (TableType type : TableType.values()) {
            sb.append("\n").append(type.getDisplayValue()).append(" (Seats: ").append(type.getSeats()).append("):\n");
            boolean[] tables = tableAvailability.get(type);
            if (tables != null) {
                for (int i = 0; i < tables.length; i++) {
                    sb.append("  - Table ").append(i + 1).append(": ").append(tables[i] ? "Booked" : "Available").append("\n");
                }
            }
        }
        availabilityArea.setText(sb.toString());
    }

    // --- Customer: Search Order by ID ---
    private void showSearchOrderById(TextArea customerOutput) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Search Order");
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #fce4ec;"); // Light pink background

        TextField orderIdField = new TextField();
        orderIdField.setPromptText("Enter your Order ID");
        orderIdField.setPrefWidth(150);

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-weight: bold;");

        Button searchBtn = createSmallDialogButton("Search");
        Button cancelBtn = createSmallDialogButton("Cancel");

        HBox buttonBar = new HBox(10, searchBtn, cancelBtn);
        buttonBar.setAlignment(Pos.CENTER);

        searchBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(orderIdField.getText().trim());
                Order foundOrder = allOrders.stream()
                                            .filter(o -> o.getOrderId() == id) // Use getter
                                            .findFirst()
                                            .orElse(null);

                if (foundOrder != null) {
                    msgLabel.setStyle("-fx-text-fill: #4CAF50;");
                    msgLabel.setText(foundOrder.toDetailedString()); // Use the detailed string for comprehensive info
                    customerOutput.setText("Order ID: " + id + " details:\n" + foundOrder.toDetailedString()); // Also update main customer output
                } else {
                    msgLabel.setStyle("-fx-text-fill: red;");
                    msgLabel.setText("Order ID " + id + " not found.");
                    customerOutput.setText("Order ID " + id + " not found.");
                }
            } catch (NumberFormatException ex) {
                msgLabel.setStyle("-fx-text-fill: red;");
                msgLabel.setText("Invalid Order ID. Please enter a number.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(new Label("Search for your order details:"), orderIdField, buttonBar, msgLabel);

        Scene scene = new Scene(root, 350, 300);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
