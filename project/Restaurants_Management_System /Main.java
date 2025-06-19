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
import javafx.stage.FileChooser; // New import for FileChooser

import java.io.File; // New import for File
import java.io.FileWriter; // New import for FileWriter
import java.io.IOException; // New import for IOException
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDate; // New import for LocalDate
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
        welcomeLabel.getStyleClass().add("title-label"); // Apply CSS class

        Button adminBtn = createStyledButton("Admin Access");
        Button customerBtn = createStyledButton("Customer Access");
        Button exitBtn = createStyledButton("Exit");

        adminBtn.setOnAction(e -> showAdminLogin());
        customerBtn.setOnAction(e -> showCustomerMenu());
        exitBtn.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(welcomeLabel, adminBtn, customerBtn, exitBtn);

        Scene scene = new Scene(root, 400, 350); // Adjusted size
        // Add the CSS stylesheet to the scene
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
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
        button.getStyleClass().add("button"); // Apply CSS class
        return button;
    }

    /**
     * Helper method to create a styled secondary button.
     */
    private Button createSecondaryStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(30);
        button.getStyleClass().addAll("button", "secondary-button"); // Apply CSS classes
        return button;
    }

    /**
     * Helper method to create a styled small button for dialogs.
     */
    private Button createSmallDialogButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(80);
        button.setPrefHeight(25);
        button.getStyleClass().addAll("button", "small-dialog-button"); // Apply CSS classes
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
        // Apply CSS to the alert dialog (optional, might require custom dialog styling)
        // DialogPane dialogPane = alert.getDialogPane();
        // dialogPane.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        // dialogPane.getStyleClass().add("myDialog");
        alert.showAndWait();
    }

    // --- Admin Login Screen ---
    private void showAdminLogin() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #e0f2f7;"); // Light blue background

        Label prompt = new Label("Enter 4-digit admin PIN:");
        prompt.getStyleClass().add("prompt-label"); // Apply CSS class

        PasswordField pinField = new PasswordField();
        pinField.setMaxWidth(200);
        pinField.setPromptText("PIN");
        pinField.getStyleClass().add("password-field"); // Apply CSS class

        Button loginBtn = createStyledButton("Login");
        Button backBtn = createSecondaryStyledButton("Back");

        Label msgLabel = new Label();
        msgLabel.getStyleClass().add("message-label"); // Base message label class

        loginBtn.setOnAction(e -> {
            String pin = pinField.getText();
            if ("2004".equals(pin)) {
                showAdminMenu();
            } else {
                msgLabel.setText("Invalid PIN. Access denied.");
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                pinField.clear();
            }
        });

        backBtn.setOnAction(e -> showAccessSelection());

        root.getChildren().addAll(prompt, pinField, loginBtn, backBtn, msgLabel);

        Scene scene = new Scene(root, 400, 350);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Login");
    }

    // --- Admin Menu Screen ---
    private void showAdminMenu() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #e8eaf6;"); // Lighter blue/purple background

        Label titleLabel = new Label("Admin Portal");
        titleLabel.getStyleClass().add("title-label"); // Apply CSS class
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        VBox menuButtons = new VBox(10);
        menuButtons.setPadding(new Insets(10));
        menuButtons.setAlignment(Pos.TOP_LEFT);
        menuButtons.setStyle("-fx-background-color: #c5cae9; -fx-border-radius: 8px; -fx-background-radius: 8px;"); // Keep inline style for menu area background as it's specific

        Button viewMenuBtn = createStyledButton("View Menu Items");
        Button editItemBtn = createStyledButton("Add Menu Item"); // New functionality
        Button removeItemBtn = createStyledButton("Remove Menu Item by ID");
        Button viewBookingsBtn = createStyledButton("View All Table Bookings");
        Button updateBookingStatusBtn = createStyledButton("Update Table Booking Payment Status"); // New functionality
        Button viewOrdersBtn = createStyledButton("View All Orders");
        Button manageOrderStatusBtn = createStyledButton("Manage Order Status"); // Renamed and refactored
        Button addOfflineBookingBtn = createStyledButton("Add Offline Booking"); // New: Add offline booking
        Button searchMenuBtn = createStyledButton("Search Menu Items by Name");
        Button dailyReportBtn = createStyledButton("Generate Daily Sales Report");
        Button logoutBtn = createSecondaryStyledButton("Logout");

        menuButtons.getChildren().addAll(viewMenuBtn, editItemBtn, removeItemBtn,
                                         viewBookingsBtn, updateBookingStatusBtn, viewOrdersBtn,
                                         manageOrderStatusBtn, addOfflineBookingBtn, searchMenuBtn, dailyReportBtn, // Changed button
                                         new Separator(), logoutBtn); // Separator for visual grouping

        root.setLeft(menuButtons);

        // Central display area using TabPane for different views
        TabPane displayTabs = new TabPane();
        displayTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        displayTabs.setPadding(new Insets(10));
        displayTabs.getStyleClass().add("tab-pane"); // Apply CSS class

        // Tab for Menu Items
        Tab menuTab = new Tab("Menu Items");
        menuTab.getStyleClass().add("tab"); // Apply CSS class
        menuTab.setContent(createMenuItemTableView()); // Use TableView for menu items
        displayTabs.getTabs().add(menuTab);

        // Tab for Orders
        Tab ordersTab = new Tab("Orders");
        ordersTab.getStyleClass().add("tab"); // Apply CSS class
        ordersTab.setContent(createOrderTableView()); // Use TableView for orders
        displayTabs.getTabs().add(ordersTab);

        // Tab for Bookings
        Tab bookingsTab = new Tab("Table Bookings");
        bookingsTab.getStyleClass().add("tab"); // Apply CSS class
        bookingsTab.setContent(createTableBookingTableView()); // Use TableView for bookings
        displayTabs.getTabs().add(bookingsTab);

        // Tab for Reports/Search (initially empty, filled on action)
        Tab reportTab = new Tab("Reports / Search Results");
        reportTab.getStyleClass().add("tab"); // Apply CSS class
        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPromptText("Reports and search results will appear here...");
        reportArea.getStyleClass().add("text-area"); // Apply CSS class
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
        manageOrderStatusBtn.setOnAction(e -> showManageOrderStatusDialog()); // New action for the refactored button
        addOfflineBookingBtn.setOnAction(e -> showAddOfflineBookingDialog()); // New: Add offline booking dialog
        searchMenuBtn.setOnAction(e -> showSearchMenuItem(reportArea)); // Direct output to reportArea
        dailyReportBtn.setOnAction(e -> generateDailySalesReport(reportArea)); // Direct output to reportArea
        logoutBtn.setOnAction(e -> showAccessSelection());

        Scene scene = new Scene(root, 1000, 700); // Increased overall size for better layout
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Dashboard");
    }

    /**
     * Creates and configures a TableView for displaying MenuItems.
     */
    private TableView<MenuItem> createMenuItemTableView() {
        TableView<MenuItem> tableView = new TableView<>();
        tableView.setEditable(false); // Make it non-editable by default
        tableView.getStyleClass().add("table-view"); // Apply CSS class

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
        tableView.getStyleClass().add("table-view"); // Apply CSS class

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

        // New Column for Payment Method
        TableColumn<Order, String> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentMethod().getDisplayValue()));
        paymentMethodCol.setPrefWidth(120);

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

        tableView.getColumns().addAll(orderIdCol, statusCol, paymentStatusCol, paymentMethodCol, totalAmountCol, discountCol, netAmountCol);
        tableView.setItems(allOrders);
        return tableView;
    }

    /**
     * Helper to get the Order TableView instance from the Admin Menu layout.
     */
    @SuppressWarnings("unchecked")
    private TableView<Order> getOrderTableView() {
        BorderPane adminRoot = (BorderPane) primaryStage.getScene().getRoot();
        TabPane displayTabs = (TabPane) adminRoot.getCenter();
        Tab ordersTab = displayTabs.getTabs().get(1); // Assuming Orders is the second tab
        return (TableView<Order>) ordersTab.getContent();
    }


    /**
     * Creates and configures a TableView for displaying TableBookings.
     */
    private TableView<TableBooking> createTableBookingTableView() {
        TableView<TableBooking> tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.getStyleClass().add("table-view"); // Apply CSS class

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
        title.getStyleClass().add("title-label"); // Apply CSS class
        root.add(title, 0, 0, 2, 1);

        Label nameLabel = new Label("Item Name:");
        nameLabel.getStyleClass().add("label"); // Apply CSS class
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Chicken Biryani");
        nameField.getStyleClass().add("text-field"); // Apply CSS class
        nameField.setText(itemToEdit != null ? itemToEdit.getName() : "");
        root.add(nameLabel, 0, 1);
        root.add(nameField, 1, 1);

        Label priceLabel = new Label("Item Price:");
        priceLabel.getStyleClass().add("label"); // Apply CSS class
        TextField priceField = new TextField();
        priceField.setPromptText("e.g., 250.00");
        priceField.getStyleClass().add("text-field"); // Apply CSS class
        priceField.setText(itemToEdit != null ? String.valueOf(itemToEdit.getPrice()) : "");
        root.add(priceLabel, 0, 2);
        root.add(priceField, 1, 2);

        Label msgLabel = new Label();
        msgLabel.getStyleClass().add("message-label"); // Base message label class
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
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    msgLabel.setText("Price must be positive.");
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
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
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 400, 250);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        stage.setScene(scene);
        stage.showAndWait(); // Wait for this dialog to close
    }


    // --- Admin: Remove Menu Item ---
    private void showRemoveMenuItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Menu Item");
        dialog.setHeaderText("Enter the ID of the menu item to remove:");
        dialog.setContentText("Item ID:");
        // Apply CSS to the dialog (might need custom DialogPane styling if needed)
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane

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

    /**
     * Admin: Shows a dialog to manage both order status and payment status for an order.
     */
    private void showManageOrderStatusDialog() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Manage Order Status");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #fce4ec;"); // Light pink background

        Label title = new Label("Enter Order ID to manage:");
        title.getStyleClass().add("prompt-label"); // Apply CSS class

        TextField orderIdField = new TextField();
        orderIdField.setPromptText("Enter Order ID");
        orderIdField.setPrefWidth(150);
        orderIdField.getStyleClass().add("text-field"); // Apply CSS class

        Button searchBtn = createSmallDialogButton("Search");

        Label currentStatusLabel = new Label("Current Order Status: -");
        Label currentPaymentStatusLabel = new Label("Current Payment Status: -");
        Label currentPaymentMethodLabel = new Label("Current Payment Method: -");
        currentStatusLabel.getStyleClass().add("label"); // Apply CSS class
        currentPaymentStatusLabel.getStyleClass().add("label"); // Apply CSS class
        currentPaymentMethodLabel.getStyleClass().add("label"); // Apply CSS class


        ComboBox<OrderStatus> orderStatusComboBox = new ComboBox<>();
        orderStatusComboBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        orderStatusComboBox.setPromptText("Select New Order Status");
        orderStatusComboBox.setPrefWidth(200);
        orderStatusComboBox.getStyleClass().add("combo-box"); // Apply CSS class

        ComboBox<PaymentStatus> paymentStatusComboBox = new ComboBox<>();
        paymentStatusComboBox.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        paymentStatusComboBox.setPromptText("Select New Payment Status");
        paymentStatusComboBox.setPrefWidth(200);
        paymentStatusComboBox.getStyleClass().add("combo-box"); // Apply CSS class

        ComboBox<PaymentMethod> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.setPromptText("Select New Payment Method");
        paymentMethodComboBox.setPrefWidth(200);
        paymentMethodComboBox.getStyleClass().add("combo-box"); // Apply CSS class


        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("message-label"); // Base message label class

        Button updateBtn = createSmallDialogButton("Update");
        Button generateBillBtn = createSmallDialogButton("Generate Bill");
        Button cancelBtn = createSmallDialogButton("Close");

        HBox buttonBar = new HBox(10, updateBtn, generateBillBtn, cancelBtn);
        buttonBar.setAlignment(Pos.CENTER);

        // Initially disable status controls
        orderStatusComboBox.setDisable(true);
        paymentStatusComboBox.setDisable(true);
        paymentMethodComboBox.setDisable(true);
        updateBtn.setDisable(true);
        generateBillBtn.setDisable(true);


        final Order[] foundOrder = {null}; // Array to hold the found order, allowing modification in lambda

        searchBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(orderIdField.getText().trim());
                foundOrder[0] = allOrders.stream()
                                            .filter(o -> o.getOrderId() == id)
                                            .findFirst()
                                            .orElse(null);

                if (foundOrder[0] != null) {
                    currentStatusLabel.setText("Current Order Status: " + foundOrder[0].getStatus().getDisplayValue());
                    currentPaymentStatusLabel.setText("Current Payment Status: " + foundOrder[0].getPaymentStatus().getDisplayValue());
                    currentPaymentMethodLabel.setText("Current Payment Method: " + foundOrder[0].getPaymentMethod().getDisplayValue());
                    orderStatusComboBox.setValue(foundOrder[0].getStatus());
                    paymentStatusComboBox.setValue(foundOrder[0].getPaymentStatus());
                    paymentMethodComboBox.setValue(foundOrder[0].getPaymentMethod());

                    orderStatusComboBox.setDisable(false);
                    paymentStatusComboBox.setDisable(false);
                    paymentMethodComboBox.setDisable(false);
                    updateBtn.setDisable(false);
                    generateBillBtn.setDisable(false);
                    msgLabel.setText("");
                    msgLabel.getStyleClass().setAll("message-label", "success"); // Apply success class
                } else {
                    currentStatusLabel.setText("Current Order Status: -");
                    currentPaymentStatusLabel.setText("Current Payment Status: -");
                    currentPaymentMethodLabel.setText("Current Payment Method: -");
                    orderStatusComboBox.setDisable(true);
                    paymentStatusComboBox.setDisable(true);
                    paymentMethodComboBox.setDisable(true);
                    updateBtn.setDisable(true);
                    generateBillBtn.setDisable(true);
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    msgLabel.setText("Order ID " + id + " not found.");
                }
            } catch (NumberFormatException ex) {
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                msgLabel.setText("Invalid Order ID. Please enter a number.");
                currentStatusLabel.setText("Current Order Status: -");
                currentPaymentStatusLabel.setText("Current Payment Status: -");
                currentPaymentMethodLabel.setText("Current Payment Method: -");
                orderStatusComboBox.setDisable(true);
                paymentStatusComboBox.setDisable(true);
                paymentMethodComboBox.setDisable(true);
                updateBtn.setDisable(true);
                generateBillBtn.setDisable(true);
            }
        });

        updateBtn.setOnAction(e -> {
            if (foundOrder[0] != null) {
                OrderStatus selectedOrderStatus = orderStatusComboBox.getValue();
                PaymentStatus selectedPaymentStatus = paymentStatusComboBox.getValue();
                PaymentMethod selectedPaymentMethod = paymentMethodComboBox.getValue();

                if (selectedOrderStatus != null && selectedPaymentStatus != null && selectedPaymentMethod != null) {
                    foundOrder[0].status = selectedOrderStatus;
                    foundOrder[0].paymentStatus = selectedPaymentStatus;
                    foundOrder[0].paymentMethod = selectedPaymentMethod;

                    DatabaseManager.updateOrderStatus(foundOrder[0].getOrderId(), selectedOrderStatus);
                    DatabaseManager.updateOrderPaymentStatus(foundOrder[0].getOrderId(), selectedPaymentStatus);
                    DatabaseManager.updateOrderPaymentMethod(foundOrder[0].getOrderId(), selectedPaymentMethod);

                    showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Order Status Updated",
                            "Order ID " + foundOrder[0].getOrderId() + " status updated to " + selectedOrderStatus.getDisplayValue() +
                            ", payment status to " + selectedPaymentStatus.getDisplayValue() +
                            ", and payment method to " + selectedPaymentMethod.getDisplayValue() + ".");

                    getOrderTableView().refresh();
                    stage.close();
                } else {
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    msgLabel.setText("Please select Order Status, Payment Status, and Payment Method.");
                }
            }
        });

        generateBillBtn.setOnAction(e -> {
            if (foundOrder[0] != null && foundOrder[0].getPaymentStatus() == PaymentStatus.PAID) {
                showBillDialog(foundOrder[0]);
            } else if (foundOrder[0] != null) {
                showAlert(Alert.AlertType.WARNING, "Bill Not Available", "Payment Pending", "Bill can only be generated for PAID orders.");
            } else {
                showAlert(Alert.AlertType.ERROR, "No Order Selected", "Error", "Please search for an order first.");
            }
        });


        cancelBtn.setOnAction(e -> stage.close());

        // Create Labels separately, then add style class, then add to root
        Label newOrderStatusLabel = new Label("New Order Status:");
        newOrderStatusLabel.getStyleClass().add("label");
        Label newPaymentStatusLabel = new Label("New Payment Status:");
        newPaymentStatusLabel.getStyleClass().add("label");
        Label newPaymentMethodLabel = new Label("New Payment Method:");
        newPaymentMethodLabel.getStyleClass().add("label");

        root.getChildren().addAll(
            title,
            new HBox(10, orderIdField, searchBtn),
            new Separator(),
            currentStatusLabel,
            currentPaymentStatusLabel,
            currentPaymentMethodLabel,
            newOrderStatusLabel, // Corrected line
            orderStatusComboBox,
            newPaymentStatusLabel, // Corrected line
            paymentStatusComboBox,
            newPaymentMethodLabel, // Corrected line
            paymentMethodComboBox,
            msgLabel,
            buttonBar
        );

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        stage.setScene(scene);
        stage.showAndWait();
    }


    // --- Admin: Update Table Booking Payment Status ---
    private void showUpdateTableBookingPaymentStatus() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Update Table Booking Payment Status");
        idDialog.setHeaderText("Enter the Customer ID for the booking:");
        idDialog.setContentText("Item ID:");
        idDialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        idDialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane

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
                statusDialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
                statusDialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane

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
        root.getStyleClass().add("root"); // Apply root style

        TextField keywordField = new TextField();
        keywordField.setPromptText("Enter keyword");
        keywordField.getStyleClass().add("text-field"); // Apply CSS class

        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(200);
        resultsArea.setPromptText("Search results will appear here...");
        resultsArea.getStyleClass().add("text-area"); // Apply CSS class

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

        // Fix the error by separating Label creation and style class application
        Label searchLabel = new Label("Search menu items by name:");
        searchLabel.getStyleClass().add("label");
        root.getChildren().addAll(searchLabel, keywordField, searchBtn, cancelBtn, resultsArea);

        Scene scene = new Scene(root, 350, 350);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
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
            reportBuilder.append(String.format("Order ID: %d, Net Amount: Rs.%.2f, Payment Method: %s\n", // Added payment method
                                                order.getOrderId(), order.getFinalPrice(), order.getPaymentMethod().getDisplayValue())); // Use getters
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
        titleLabel.getStyleClass().add("title-label"); // Apply CSS class
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        VBox menuButtons = new VBox(10);
        menuButtons.setPadding(new Insets(10));
        menuButtons.setAlignment(Pos.TOP_LEFT);
        menuButtons.setStyle("-fx-background-color: #bbdefb; -fx-border-radius: 8px; -fx-background-radius: 8px;"); // Keep inline for specific section

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
        customerDisplayTabs.getStyleClass().add("tab-pane"); // Apply CSS class

        Tab menuTab = new Tab("Menu");
        menuTab.getStyleClass().add("tab"); // Apply CSS class
        menuTab.setContent(createMenuItemTableView()); // Re-use menu item table view
        customerDisplayTabs.getTabs().add(menuTab);

        Tab cartTab = new Tab("Your Cart");
        cartTab.getStyleClass().add("tab"); // Apply CSS class
        ListView<String> currentCartDisplay = new ListView<>();
        // Fix: Create the Label first, then add the style class, then set as placeholder
        Label placeholderLabel = new Label("Your cart is empty.");
        placeholderLabel.getStyleClass().add("label");
        currentCartDisplay.setPlaceholder(placeholderLabel);
        currentCartDisplay.setEditable(false);
        currentCartDisplay.getStyleClass().add("list-view"); // Apply CSS class
        cartTab.setContent(currentCartDisplay);
        customerDisplayTabs.getTabs().add(cartTab);

        Tab outputTab = new Tab("Messages / Order Info");
        outputTab.getStyleClass().add("tab"); // Apply CSS class
        TextArea customerOutputArea = new TextArea();
        customerOutputArea.setEditable(false);
        customerOutputArea.setPromptText("Messages and order details will appear here...");
        customerOutputArea.getStyleClass().add("text-area"); // Apply CSS class
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
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
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
            Map<String, Integer> itemQuantities = new HashMap<>(); // Changed key to String (item name) for display
            Map<String, MenuItem> uniqueItems = new HashMap<>(); // To get the name and price once

            for (MenuItem item : currentOrder.items) {
                itemQuantities.put(item.getName(), itemQuantities.getOrDefault(item.getName(), 0) + 1);
                uniqueItems.put(item.getName(), item);
            }

            for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
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
        root.getStyleClass().add("root"); // Apply root style

        Label title = new Label("Select menu items to add to your cart:");
        title.getStyleClass().add("prompt-label"); // Apply CSS class

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
        menuComboBox.getStyleClass().add("combo-box"); // Apply CSS class

        Button addBtn = createSmallDialogButton("Add to Cart");
        Button removeBtn = createSmallDialogButton("Remove Last Item"); // New: Remove last added item
        Button doneBtn = createSmallDialogButton("Done");

        HBox itemActions = new HBox(10, addBtn, removeBtn);
        itemActions.setAlignment(Pos.CENTER);

        Label msgLabel = new Label();
        msgLabel.getStyleClass().add("message-label"); // Base message label class

        // Initialize current order if null
        if (currentOrder == null) {
            currentOrder = new Order(orderCounter); // Don't increment yet, just get next ID
        }
        updateCartDisplay(currentCartDisplay); // Refresh cart display in main customer menu

        addBtn.setOnAction(e -> {
            MenuItem selectedItem = menuComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                msgLabel.setText("Please select an item.");
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                return;
            }

            currentOrder.addItem(selectedItem);
            msgLabel.setText(selectedItem.getName() + " added to cart.");
            msgLabel.getStyleClass().setAll("message-label", "success"); // Apply success class
            updateCartDisplay(currentCartDisplay);
        });

        removeBtn.setOnAction(e -> {
            if (currentOrder != null && !currentOrder.items.isEmpty()) {
                MenuItem removed = currentOrder.items.remove(currentOrder.items.size() - 1);
                msgLabel.setText(removed.getName() + " removed from cart.");
                msgLabel.getStyleClass().setAll("message-label", "warning"); // Apply warning class
                updateCartDisplay(currentCartDisplay);
            } else {
                msgLabel.setText("Cart is already empty.");
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
            }
        });

        doneBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, menuComboBox, itemActions, new Separator(), msgLabel, doneBtn);

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
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
        couponDialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        couponDialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane


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

        // --- Payment Method Selection ---
        ChoiceDialog<PaymentMethod> paymentMethodDialog = new ChoiceDialog<>(PaymentMethod.CASH, PaymentMethod.values());
        paymentMethodDialog.setTitle("Select Payment Method");
        paymentMethodDialog.setHeaderText("How would you like to pay?");
        paymentMethodDialog.setContentText("Choose your payment method:");
        paymentMethodDialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        paymentMethodDialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane

        Optional<PaymentMethod> selectedPaymentMethod = paymentMethodDialog.showAndWait();
        if (selectedPaymentMethod.isEmpty()) {
            customerOutput.setText("Payment method selection cancelled. Order not confirmed.");
            return; // User cancelled payment method selection
        }
        currentOrder.paymentMethod = selectedPaymentMethod.get(); // Set the chosen payment method

        // --- Order Summary Confirmation ---
        Alert summaryAlert = new Alert(Alert.AlertType.CONFIRMATION); // Changed to CONFIRMATION
        summaryAlert.setTitle("Order Summary");
        summaryAlert.setHeaderText("Please review your order details:");
        StringBuilder summaryContent = new StringBuilder();
        summaryContent.append("Items Total: Rs.").append(String.format("%.2f", currentOrder.getPrice())).append("\n");
        summaryContent.append("Discount: Rs.").append(String.format("%.2f", currentOrder.getDiscountApplied())).append("\n"); // Use getter
        summaryContent.append(couponMessage).append("\n");
        summaryContent.append("Final Amount Due: Rs.").append(String.format("%.2f", finalPrice)).append("\n");
        summaryContent.append("Payment Method: ").append(currentOrder.getPaymentMethod().getDisplayValue()).append("\n\n"); // New: Display payment method
        summaryContent.append("Click OK to proceed to payment, or Cancel to go back.");
        summaryAlert.setContentText(summaryContent.toString());
        summaryAlert.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        summaryAlert.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane


        Optional<ButtonType> summaryResult = summaryAlert.showAndWait();
        if (summaryResult.orElse(ButtonType.CANCEL) != ButtonType.OK) {
            customerOutput.setText("Order confirmation cancelled.");
            return; // User cancelled summary
        }

        // --- OTP Simulation (only for Online payment) ---
        boolean paymentSuccessful = false;
        if (currentOrder.getPaymentMethod() == PaymentMethod.ONLINE) {
            Random rand = new Random();
            int otp = 1000 + rand.nextInt(9000); // 4-digit OTP

            TextInputDialog otpDialog = new TextInputDialog();
            otpDialog.setTitle("Payment Confirmation");
            otpDialog.setHeaderText("Your OTP for payment is: " + otp + "\nEnter OTP to confirm payment for Rs." + String.format("%.2f", finalPrice));
            otpDialog.setContentText("OTP:");
            otpDialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
            otpDialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane


            Optional<String> result = otpDialog.showAndWait();
            if (result.isPresent() && result.get().equals(String.valueOf(otp))) {
                paymentSuccessful = true;
            } else {
                customerOutput.setText("Payment failed or OTP incorrect. Order not confirmed.");
                showAlert(Alert.AlertType.ERROR, "Payment Failed", null, "Payment failed or OTP incorrect.");
                return; // Exit if OTP fails
            }
        } else { // Cash Payment
            // For cash, payment is considered successful immediately
            paymentSuccessful = true;
            showAlert(Alert.AlertType.INFORMATION, "Payment Mode Selected", null, "Please pay Rs." + String.format("%.2f", finalPrice) + " at the counter.");
        }


        if (paymentSuccessful) {
            currentOrder.status = OrderStatus.CONFIRMED;
            currentOrder.paymentStatus = PaymentStatus.PAID;
            currentOrder.orderId = orderCounter++; // Assign and increment order ID
            allOrders.add(currentOrder); // Add to observable list
            DatabaseManager.saveOrder(currentOrder); // Save the confirmed order to DB

            customerOutput.setText("Order Confirmed! Your Order ID is: " + currentOrder.getOrderId() + // Use getter
                                   "\nTotal Amount Paid: Rs." + String.format("%.2f", finalPrice) +
                                   "\nPayment Method: " + currentOrder.getPaymentMethod().getDisplayValue() + // New
                                   "\n" + couponMessage);
            showAlert(Alert.AlertType.INFORMATION, "Order Confirmed", null, "Order successfully placed! Order ID: " + currentOrder.getOrderId()); // Use getter

            // New: Show the bill after successful payment
            showBillDialog(currentOrder);

            currentOrder = null; // Clear current order after confirmation
            updateCartDisplay(currentCartDisplay); // Clear cart display
        } else {
            customerOutput.setText("Payment failed. Order not confirmed.");
            showAlert(Alert.AlertType.ERROR, "Payment Failed", null, "Payment failed.");
        }
    }

    /**
     * Displays a detailed bill for a given order.
     * @param order The order for which to generate the bill.
     */
    private void showBillDialog(Order order) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Restaurant Bill - Order ID: " + order.getOrderId());

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #ffffff; -fx-border-color: #333; -fx-border-width: 2px; -fx-border-radius: 5px;");
        root.getStyleClass().add("root"); // Apply root style to the bill dialog


        Label header = new Label("--- Shield Restaurant Bill ---");
        header.getStyleClass().add("title-label"); // Apply CSS class
        header.setMaxWidth(Double.MAX_VALUE); // To center the text

        Label dateLabel = new Label("Date: " + LocalDate.now());
        dateLabel.getStyleClass().add("label"); // Apply CSS class
        dateLabel.setMaxWidth(Double.MAX_VALUE);

        Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
        orderIdLabel.getStyleClass().add("prompt-label"); // Apply CSS class
        orderIdLabel.setStyle("-fx-padding: 5px 0;"); // Keep specific padding

        Separator separator1 = new Separator();
        separator1.getStyleClass().add("separator"); // Apply CSS class

        Label itemsHeader = new Label("Items Ordered:");
        itemsHeader.getStyleClass().add("label"); // Apply CSS class
        itemsHeader.setStyle("-fx-padding: 5px 0;"); // Keep specific padding

        // Use a TextArea or ListView for items to handle potentially long lists
        TextArea itemsArea = new TextArea();
        itemsArea.setEditable(false);
        itemsArea.setPrefHeight(200); // Set a reasonable height
        itemsArea.getStyleClass().add("text-area"); // Apply CSS class

        StringBuilder itemDetails = new StringBuilder();
        Map<String, Integer> itemCounts = new HashMap<>();
        Map<String, Double> itemPrices = new HashMap<>();

        for (MenuItem item : order.items) {
            itemCounts.put(item.getName(), itemCounts.getOrDefault(item.getName(), 0) + 1);
            if (!itemPrices.containsKey(item.getName())) { // Store price at time of order
                itemPrices.put(item.getName(), item.getPrice());
            }
        }

        itemDetails.append(String.format("%-25s %-5s %s\n", "Item", "Qty", "Price"));
        itemDetails.append("--------------------------------------\n");
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            double lineTotal = entry.getValue() * itemPrices.get(entry.getKey());
            itemDetails.append(String.format("%-25s x%-3d Rs.%.2f\n", entry.getKey(), entry.getValue(), lineTotal));
        }
        itemsArea.setText(itemDetails.toString());

        Separator separator2 = new Separator();
        separator2.getStyleClass().add("separator"); // Apply CSS class

        Label subtotalLabel = new Label(String.format("Subtotal: Rs.%.2f", order.getPrice()));
        subtotalLabel.getStyleClass().add("label"); // Apply CSS class
        Label discountLabel = new Label(String.format("Discount: Rs.%.2f", order.getDiscountApplied()));
        discountLabel.getStyleClass().add("label"); // Apply CSS class
        Label finalTotalLabel = new Label(String.format("Final Amount Paid: Rs.%.2f", order.getFinalPrice()));
        finalTotalLabel.getStyleClass().add("prompt-label"); // Apply CSS class (stronger style)
        finalTotalLabel.setStyle("-fx-padding: 5px 0;"); // Keep specific padding
        Label paymentMethodLabel = new Label("Payment Method: " + order.getPaymentMethod().getDisplayValue()); // New
        paymentMethodLabel.getStyleClass().add("label"); // Apply CSS class


        Button downloadBillBtn = createSmallDialogButton("Download Bill"); // New: Download button
        Button closeBtn = createSmallDialogButton("Close");

        HBox buttonBar = new HBox(10, downloadBillBtn, closeBtn); // Add download button
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        downloadBillBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Bill");
            fileChooser.setInitialFileName("Order_Bill_" + order.getOrderId() + ".txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            // Set initial directory to user's home directory
            File userHome = new File(System.getProperty("user.home"));
            if (userHome.exists() && userHome.isDirectory()) {
                fileChooser.setInitialDirectory(userHome);
            }


            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write("--- Shield Restaurant Bill ---\n\n");
                    fileWriter.write("Date: " + LocalDate.now() + "\n");
                    fileWriter.write("Order ID: " + order.getOrderId() + "\n\n");
                    fileWriter.write("Items Ordered:\n");
                    fileWriter.write("--------------------------------------\n");
                    fileWriter.write(itemDetails.toString()); // Write formatted item details
                    fileWriter.write("--------------------------------------\n");
                    fileWriter.write(String.format("Subtotal: Rs.%.2f\n", order.getPrice()));
                    fileWriter.write(String.format("Discount: Rs.%.2f\n", order.getDiscountApplied()));
                    fileWriter.write(String.format("Final Amount Paid: Rs.%.2f\n", order.getFinalPrice()));
                    fileWriter.write("Payment Method: " + order.getPaymentMethod().getDisplayValue() + "\n");
                    fileWriter.write("--------------------------------------\n");
                    fileWriter.write("\nThank you for your order!\n");

                    showAlert(Alert.AlertType.INFORMATION, "Download Successful", "Bill Saved", "Bill for Order ID " + order.getOrderId() + " saved to:\n" + file.getAbsolutePath() + "\n\nNote: This is a plain text file. To get a PDF, open this file and use your system's 'Print to PDF' option.");
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Download Failed", "Error Saving Bill", "Failed to save bill: " + ex.getMessage());
                }
            }
        });

        // FIX: Ensure the close button works
        closeBtn.setOnAction(e -> stage.close());


        root.getChildren().addAll(header, dateLabel, orderIdLabel, separator1, itemsHeader, itemsArea,
                                  separator2, subtotalLabel, discountLabel, finalTotalLabel, paymentMethodLabel,
                                  new Separator(), buttonBar); // Use buttonBar

        Scene scene = new Scene(new ScrollPane(root), 450, 600);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        stage.setScene(scene);
        stage.showAndWait();
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
        root.getStyleClass().add("root"); // Apply root style

        Label title = new Label("Table Reservation");
        title.getStyleClass().add("title-label"); // Apply CSS class

        // Availability Display Area
        TextArea availabilityArea = new TextArea();
        availabilityArea.setEditable(false);
        availabilityArea.setPrefHeight(200);
        availabilityArea.setPromptText("Click 'Check Available Tables' to see current availability.");
        availabilityArea.getStyleClass().add("text-area"); // Apply CSS class


        Button checkAvailabilityBtn = createSmallDialogButton("Check Available Tables");

        HBox tableTypeSelection = new HBox(10);
        tableTypeSelection.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("Table Type (Seats):");
        typeLabel.getStyleClass().add("label"); // Apply CSS class
        ComboBox<TableType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(TableType.values()); // Populate with enum values
        typeComboBox.setPromptText("Select type");
        typeComboBox.setPrefWidth(150);
        typeComboBox.getStyleClass().add("combo-box"); // Apply CSS class
        tableTypeSelection.getChildren().addAll(typeLabel, typeComboBox);

        HBox tableNumberSelection = new HBox(10);
        tableNumberSelection.setAlignment(Pos.CENTER_LEFT);
        Label numberLabel = new Label("Table Number (1-10):");
        numberLabel.getStyleClass().add("label"); // Apply CSS class
        TextField numberField = new TextField();
        numberField.setPromptText("Enter number");
        numberField.setPrefWidth(80);
        numberField.getStyleClass().add("text-field"); // Apply CSS class
        tableNumberSelection.getChildren().addAll(numberLabel, numberField);

        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        nameField.getStyleClass().add("text-field"); // Apply CSS class
        TextField phoneField = new TextField();
        phoneField.setPromptText("Your Phone Number");
        phoneField.getStyleClass().add("text-field"); // Apply CSS class

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("message-label"); // Base message label class

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
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                return;
            }

            try {
                int number = Integer.parseInt(numberText);
                if (number < 1 || number > 10) {
                    msgLabel.setText("Table number must be between 1 and 10.");
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    return;
                }

                boolean[] tablesArray = tableAvailability.get(type);
                if (tablesArray == null) {
                    msgLabel.setText("Invalid table type selected."); // Should not happen with ComboBox
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    return;
                }

                if (tablesArray[number - 1]) {
                    msgLabel.setText("Table " + type.getDisplayValue() + " (number " + number + ") is already booked.");
                    msgLabel.getStyleClass().setAll("message-label", "warning"); // Apply warning class
                } else {
                    // Proceed to payment for booking
                    double bookingFee = 100.00; // Example fixed booking fee

                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("Confirm Table Booking");
                    confirmationAlert.setHeaderText("Confirm booking for " + name + " at " + type.getDisplayValue() + " #" + number + "?");
                    confirmationAlert.setContentText("A booking fee of Rs." + String.format("%.2f", bookingFee) + " will be charged.");
                    confirmationAlert.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
                    confirmationAlert.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane


                    Optional<ButtonType> confirmResult = confirmationAlert.showAndWait();
                    if (confirmResult.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        // Simulate OTP payment for booking fee
                        Random rand = new Random();
                        int otp = 1000 + rand.nextInt(9000); // 4-digit OTP

                        TextInputDialog otpDialog = new TextInputDialog();
                        otpDialog.setTitle("Booking Payment Confirmation");
                        otpDialog.setHeaderText("Your OTP is: " + otp + "\nEnter OTP to confirm payment for Rs." + String.format("%.2f", bookingFee));
                        otpDialog.setContentText("OTP:");
                        otpDialog.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
                        otpDialog.getDialogPane().getStyleClass().add("root"); // Apply root style to dialog pane


                        Optional<String> otpResult = otpDialog.showAndWait();
                        if (otpResult.isPresent() && otpResult.get().equals(String.valueOf(otp))) {
                            // Payment successful, proceed with booking
                            tablesArray[number - 1] = true; // Mark as booked
                            String customerId = "CUST" + (customerIdCounter++); // Generate customer ID
                            TableBooking newBooking = new TableBooking(customerId, name, phone, type, number, bookingFee);
                            newBooking.paymentStatus = PaymentStatus.PAID; // Set to paid upon successful payment
                            allBookings.add(newBooking); // Add to observable list
                            DatabaseManager.saveTableBooking(newBooking); // Save to database

                            msgLabel.getStyleClass().setAll("message-label", "success"); // Apply success class
                            msgLabel.setText("Table " + type.getDisplayValue() + " (number " + number + ") reserved and paid for " + name +
                                    ". Your Customer ID: " + newBooking.getCustomerId()); // Use getter
                            customerOutput.setText("Table reservation successful for " + name + " (ID: " + newBooking.getCustomerId() + ")"); // Use getter
                            updateAvailabilityDisplay(availabilityArea); // Refresh availability after booking
                            showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed", null, "Table booked and paid! Customer ID: " + newBooking.getCustomerId()); // Use getter
                            stage.close(); // Close reservation dialog on success
                        } else {
                            msgLabel.setText("Payment failed or OTP incorrect. Table not reserved.");
                            msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                            showAlert(Alert.AlertType.ERROR, "Payment Failed", null, "Payment failed or OTP incorrect. Table not reserved.");
                        }
                    } else {
                        msgLabel.setText("Table booking cancelled by user.");
                        msgLabel.getStyleClass().setAll("message-label", "warning"); // Apply warning class
                    }
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid table number.");
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        // Create Label separately, then add style class, then add to root
        Label reserveTableLabel = new Label("Reserve a table:");
        reserveTableLabel.getStyleClass().add("label");
        root.getChildren().addAll(title, checkAvailabilityBtn, availabilityArea, new Separator(),
                                  reserveTableLabel, tableTypeSelection, tableNumberSelection,
                                  nameField, phoneField, buttonBox, msgLabel);

        Scene scene = new Scene(new ScrollPane(root), 500, 700);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
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

    // --- Admin: Add Offline Booking Dialog ---
    private void showAddOfflineBookingDialog() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add Offline Table Booking");

        GridPane root = new GridPane();
        root.setVgap(10);
        root.setHgap(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #e0f7fa; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        root.getStyleClass().add("root"); // Apply root style

        Label title = new Label("Manually Add a Table Booking:");
        title.getStyleClass().add("title-label"); // Apply CSS class
        root.add(title, 0, 0, 2, 1);

        Label nameLabel = new Label("Customer Name:");
        nameLabel.getStyleClass().add("label"); // Apply CSS class
        TextField nameField = new TextField();
        nameField.setPromptText("Enter customer name");
        nameField.getStyleClass().add("text-field"); // Apply CSS class
        root.add(nameLabel, 0, 1);
        root.add(nameField, 1, 1);

        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.getStyleClass().add("label"); // Apply CSS class
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        phoneField.getStyleClass().add("text-field"); // Apply CSS class
        root.add(phoneLabel, 0, 2);
        root.add(phoneField, 1, 2);

        Label typeLabel = new Label("Table Type:");
        typeLabel.getStyleClass().add("label"); // Apply CSS class
        ComboBox<TableType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(TableType.values());
        typeComboBox.setPromptText("Select table type");
        typeComboBox.getStyleClass().add("combo-box"); // Apply CSS class
        root.add(typeLabel, 0, 3);
        root.add(typeComboBox, 1, 3);

        Label numberLabel = new Label("Table Number (1-10):");
        numberLabel.getStyleClass().add("label"); // Apply CSS class
        TextField numberField = new TextField();
        numberField.setPromptText("Enter table number");
        numberField.getStyleClass().add("text-field"); // Apply CSS class
        root.add(numberLabel, 0, 4);
        root.add(numberField, 1, 4);

        Label paymentStatusLabel = new Label("Payment Status:");
        paymentStatusLabel.getStyleClass().add("label"); // Apply CSS class
        ComboBox<PaymentStatus> paymentStatusComboBox = new ComboBox<>();
        paymentStatusComboBox.getItems().addAll(PaymentStatus.PAID, PaymentStatus.PENDING); // Admin can mark as paid or pending
        paymentStatusComboBox.setValue(PaymentStatus.PAID); // Default to paid for offline booking
        paymentStatusComboBox.getStyleClass().add("combo-box"); // Apply CSS class
        root.add(paymentStatusLabel, 0, 5);
        root.add(paymentStatusComboBox, 1, 5);

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("message-label"); // Base message label class
        root.add(msgLabel, 0, 7, 2, 1);

        Button addBtn = createSmallDialogButton("Add Booking");
        Button cancelBtn = createSmallDialogButton("Cancel");

        HBox buttonBar = new HBox(10, addBtn, cancelBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        root.add(buttonBar, 1, 6);

        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            TableType type = typeComboBox.getValue();
            String numberText = numberField.getText().trim();
            PaymentStatus paymentStatus = paymentStatusComboBox.getValue();

            if (name.isEmpty() || phone.isEmpty() || type == null || numberText.isEmpty() || paymentStatus == null) {
                msgLabel.setText("Please fill all fields.");
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                return;
            }

            try {
                int number = Integer.parseInt(numberText);
                if (number < 1 || number > 10) {
                    msgLabel.setText("Table number must be between 1 and 10.");
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    return;
                }

                boolean[] tablesArray = tableAvailability.get(type);
                if (tablesArray == null) {
                    msgLabel.setText("Invalid table type selected.");
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    return;
                }

                if (tablesArray[number - 1]) {
                    msgLabel.setText("Table " + type.getDisplayValue() + " (number " + number + ") is already booked.");
                    msgLabel.getStyleClass().setAll("message-label", "warning"); // Apply warning class
                } else {
                    double bookingFee = 100.00; // Fixed booking fee for simplicity in offline booking
                    String customerId = "CUST" + (customerIdCounter++);

                    TableBooking newBooking = new TableBooking(customerId, name, phone, type, number, bookingFee);
                    newBooking.paymentStatus = paymentStatus; // Set payment status based on admin input

                    // Only mark table as occupied if payment is PAID
                    if (paymentStatus == PaymentStatus.PAID) {
                        tablesArray[number - 1] = true; // Mark as booked
                    }

                    allBookings.add(newBooking);
                    DatabaseManager.saveTableBooking(newBooking);

                    showAlert(Alert.AlertType.INFORMATION, "Booking Added", "Offline Booking Successful",
                            "Table " + type.getDisplayValue() + " #" + number + " booked for " + name +
                            ". Customer ID: " + customerId + ". Payment Status: " + paymentStatus.getDisplayValue());

                    getBookingTableView().refresh(); // Refresh TableView
                    // No need to call loadAllDataFromDatabase() if allBookings is ObservableList and updated
                    stage.close();
                }
            } catch (NumberFormatException ex) {
                msgLabel.setText("Invalid table number. Please enter a number.");
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 450, 450);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        stage.setScene(scene);
        stage.showAndWait();
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
        root.getStyleClass().add("root"); // Apply root style

        TextField orderIdField = new TextField();
        orderIdField.setPromptText("Enter your Order ID");
        orderIdField.setPrefWidth(150);
        orderIdField.getStyleClass().add("text-field"); // Apply CSS class

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("message-label"); // Base message label class

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
                    msgLabel.getStyleClass().setAll("message-label", "success"); // Apply success class
                    msgLabel.setText(foundOrder.toDetailedString()); // Use the detailed string for comprehensive info
                    customerOutput.setText("Order ID: " + id + " details:\n" + foundOrder.toDetailedString()); // Also update main customer output
                } else {
                    msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                    msgLabel.setText("Order ID " + id + " not found.");
                    customerOutput.setText("Order ID " + id + " not found.");
                }
            } catch (NumberFormatException ex) {
                msgLabel.getStyleClass().setAll("message-label", "error"); // Apply error class
                msgLabel.setText("Invalid Order ID. Please enter a number.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        // Fix the error by separating Label creation and style class application
        Label searchOrderLabel = new Label("Search for your order details:");
        searchOrderLabel.getStyleClass().add("label");
        root.getChildren().addAll(searchOrderLabel, orderIdField, buttonBar, msgLabel);

        Scene scene = new Scene(root, 350, 300);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm()); // Apply CSS
        stage.setScene(scene);
        stage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
