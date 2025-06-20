// Main.java
package application;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node; 
import javafx.stage.Window; 

import java.net.URL; 
import java.time.LocalDateTime;
import java.time.LocalTime; 
import java.time.format.DateTimeFormatter; 
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main application class for the Restaurant Management System.
 * Handles UI, business logic, and interaction with the DatabaseManager.
 * This version includes user login, registration, and personalized customer profiles,
 * as well as an admin portal for comprehensive management.
 */
public class Main extends Application {

    // --- Data & state ---
    private List<MenuItem> menuList = new ArrayList<>();
    private Map<Integer, MenuItem> menuMap = new HashMap<>(); // Maps ID to MenuItem for quick lookup
    private ObservableList<Order> allOrders = FXCollections.observableArrayList(); // Use ObservableList for TableView
    private ObservableList<TableBooking> allTableBookings = FXCollections.observableArrayList(); // ObservableList for TableView
    private ObservableList<Feedback> allFeedback = FXCollections.observableArrayList(); // ObservableList for TableView
    private ObservableList<DishRating> allDishRatings = FXCollections.observableArrayList(); // ObservableList for TableView
    private ObservableList<User> allUsers = FXCollections.observableArrayList(); // ObservableList for User Management

    private User currentUser; // Stores the currently logged-in user

    // --- UI Elements (for broader scope) ---
    private TableView<Order> ordersTable;
    private TableView<TableBooking> tableBookingsTable; // TableView for managing bookings
    private TableView<User> userManagementTable; // TableView for managing users
    private TableView<MenuItem> menuManagementTable; // TableView for managing menu items
    private TableView<Feedback> feedbackTable; // TableView for feedback
    private ListView<String> customerCartListView; // Reference for customer's cart display
    private Label customerCartTotalLabel; // Reference for customer's cart total label
    private ObservableList<MenuItem> currentOrderItems = FXCollections.observableArrayList(); // Items currently in customer's cart

    // Constants
    private static final int BOOKING_DURATION_MINUTES = 90; // Default booking slot duration
    private static final String ADMIN_USERNAME = "Dinesh Reddy"; // Admin username (from DatabaseManager)


    @Override
    public void start(Stage primaryStage) {
        // Initialize Database (ensure tables exist and admin user/default menu items are present)
        DatabaseManager.initializeDatabase();

        // Load initial data
        loadAllData();

        primaryStage.setTitle("Restaurant Management System");

        // --- Login Scene ---
        VBox loginLayout = createLoginRegisterForm(true); // true for login mode
        Scene loginScene = new Scene(loginLayout, 900, 700); // Set initial scene size
        
        // Load CSS for the initial scene
        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            loginScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: style.css not found for Login Scene. Expected path: application/style.css (relative to classpath)");
        }
        
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    /**
     * Loads all necessary data from the database into observable lists.
     * This method is called upon application start and after certain data modifications
     * to refresh the UI.
     */
    private void loadAllData() {
        // Clear existing data to ensure fresh load
        menuList.clear();
        menuMap.clear();
        allOrders.clear();
        allTableBookings.clear();
        allFeedback.clear();
        allDishRatings.clear();
        allUsers.clear();

        // Load Menu Items
        menuList.addAll(DatabaseManager.getMenuItems());
        menuList.forEach(item -> menuMap.put(item.getId(), item));

        // Load Orders, Bookings, Feedback, Dish Ratings, and Users
        allOrders.addAll(DatabaseManager.getAllOrders(menuMap)); // Pass menuMap
        allTableBookings.addAll(DatabaseManager.getAllTableBookings());
        allFeedback.addAll(DatabaseManager.getAllFeedback());
        allDishRatings.addAll(DatabaseManager.loadDishRatings()); // Load all dish ratings (DatabaseManager.getDishRatings(0) was not ideal)
        allUsers.addAll(DatabaseManager.loadAllUsers());
    }


    /**
     * Creates the common login/registration form.
     * @param isLoginMode true for login, false for registration.
     * @return VBox containing the form.
     */
    private VBox createLoginRegisterForm(boolean isLoginMode) {
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(50));
        // Apply different background images based on mode
        if (isLoginMode) {
            formContainer.getStyleClass().add("login-container"); // Custom style for login background
        } else {
            formContainer.getStyleClass().add("register-container"); // Custom style for register background
        }


        Label title = new Label(isLoginMode ? "Welcome Back!" : "Join Us!");
        title.getStyleClass().add("form-title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field-custom");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field-custom");

        // Additional fields for registration
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.getStyleClass().add("text-field-custom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field-custom");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        phoneField.getStyleClass().add("text-field-custom");

        Button actionButton = new Button(isLoginMode ? "Login" : "Register");
        actionButton.getStyleClass().add("button-primary"); // Use primary button style

        Label switchModeLabel = new Label(isLoginMode ? "Don't have an account?" : "Already have an account?");
        Hyperlink switchModeLink = new Hyperlink(isLoginMode ? "Register here." : "Login here.");
        switchModeLink.getStyleClass().add("hyperlink-custom");

        HBox switchModeBox = new HBox(5, switchModeLabel, switchModeLink);
        switchModeBox.setAlignment(Pos.CENTER);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");


        VBox fieldsBox = new VBox(10, usernameField, passwordField);
        if (!isLoginMode) {
            fieldsBox.getChildren().addAll(fullNameField, emailField, phoneField);
        }

        formContainer.getChildren().addAll(title, fieldsBox, actionButton, messageLabel, switchModeBox);

        actionButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Username and password cannot be empty.");
                messageLabel.getStyleClass().setAll("message-label", "error-label"); // Red for error
                return;
            }

            if (isLoginMode) {
                // Login logic
                currentUser = DatabaseManager.validateUser(username, password);
                if (currentUser != null) {
                    messageLabel.setText("Login successful!");
                    messageLabel.getStyleClass().setAll("message-label", "success-label"); // Green for success
                    showMainApplication(usernameField.getScene().getWindow()); // Pass the current window
                } else {
                    messageLabel.setText("Invalid username or password.");
                    messageLabel.getStyleClass().setAll("message-label", "error-label");
                }
            } else {
                // Register logic
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    messageLabel.setText("All fields are required for registration.");
                    messageLabel.getStyleClass().setAll("message-label", "error-label");
                    return;
                }

                if (DatabaseManager.checkIfUserExists(username)) { // Corrected method name
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists. Please choose a different one.");
                    return;
                }

                String hashedPassword = PasswordUtil.hashPassword(password);
                if (hashedPassword == null) {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "Error hashing password. Please try again.");
                    return;
                }

                // Default new users to CUSTOMER role
                User newUser = new User(username, hashedPassword, fullName, email, phone, UserRole.CUSTOMER);
                if (DatabaseManager.registerUser(newUser)) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Registration successful! You can now log in.");
                    // Optionally, clear fields or switch to login screen
                    usernameField.clear();
                    passwordField.clear();
                    fullNameField.clear();
                    emailField.clear();
                    phoneField.clear();
                    // Get the stage from any node in the current scene
                    Stage currentStage = (Stage) usernameField.getScene().getWindow();
                    currentStage.setScene(new Scene(createLoginRegisterForm(true), 900, 700)); // Go back to login after successful registration
                } else {
                    messageLabel.setText("Registration failed. Username might already exist.");
                    messageLabel.getStyleClass().setAll("message-label", "error-label");
                }
            }
        });

        switchModeLink.setOnAction(e -> {
            // Get the stage from any node in the current scene
            Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            if (isLoginMode) {
                currentStage.setScene(new Scene(createLoginRegisterForm(false), 900, 700)); // Switch to register
            } else {
                currentStage.setScene(new Scene(createLoginRegisterForm(true), 900, 700)); // Switch to login
            }
        });
        return formContainer;
    }


    /**
     * Displays the main application window after successful login.
     * The content of the main window depends on the logged-in user's role.
     */
    private void showMainApplication(Window currentWindow) {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane-custom"); // Apply custom style

        // Always load menu items as they are used across different views
        menuList.clear();
        menuMap.clear();
        menuList.addAll(DatabaseManager.getMenuItems());
        menuList.forEach(item -> menuMap.put(item.getId(), item));

        if (currentUser.getRole() == UserRole.ADMIN) {
            // Admin Tabs
            tabPane.getTabs().add(createTab("Dashboard", createAdminDashboardContent()));
            tabPane.getTabs().add(createTab("Menu Management", createMenuManagementTab()));
            tabPane.getTabs().add(createTab("Order Management", createOrderManagementTab()));
            tabPane.getTabs().add(createTab("Table Booking Management", createTableBookingManagementTab()));
            tabPane.getTabs().add(createTab("Feedback Management", createFeedbackManagementTab()));
            tabPane.getTabs().add(createTab("Dish Ratings Review", createDishRatingReviewTab()));
            tabPane.getTabs().add(createTab("User Management", createUserManagementTab())); // New tab for admin
            tabPane.getTabs().add(createTab("Table Availability Check", createTableAvailabilityTab())); // New tab for admin
        } else {
            // Customer Tabs
            tabPane.getTabs().add(createTab("My Dashboard", createCustomerDashboardContent())); // New: Customer Dashboard
            tabPane.getTabs().add(createTab("Order Food", createOrderFoodTab()));
            tabPane.getTabs().add(createTab("Book Table", createBookTableTab()));
            tabPane.getTabs().add(createTab("Give Feedback", createGiveFeedbackTab()));
            tabPane.getTabs().add(createTab("Rate a Dish", createRateDishTab()));
            tabPane.getTabs().add(createTab("My Profile", createMyProfileTab()));
            tabPane.getTabs().add(createTab("My Orders", createMyOrdersTab()));
            tabPane.getTabs().add(createTab("My Bookings", createMyBookingsTab()));
        }

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button-danger"); // Style for logout button
        logoutButton.setOnAction(e -> {
            currentUser = null; // Clear current user
            Stage currentStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(createLoginRegisterForm(true), 900, 700)); // Go back to login
            currentStage.setTitle("Restaurant Management System - Login");
        });

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.getChildren().addAll(new Label("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole().getDisplayValue() + ")"), logoutButton);
        topBar.getStyleClass().add("top-bar"); // Optional: style this bar

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setCenter(tabPane);

        Scene mainScene = new Scene(mainLayout, 1200, 800); // Adjust size as needed
        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            mainScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: style.css not found for Main Scene. UI may not be styled correctly. Expected path: application/style.css (relative to classpath)");
        }
        
        Stage currentStage = (Stage) currentWindow;
        currentStage.setScene(mainScene);
        currentStage.setTitle("Restaurant Management System - " + currentUser.getRole().getDisplayValue() + " Portal");
        currentStage.centerOnScreen();
    }

    /**
     * Helper method to create a Tab with content.
     * @param title The title of the tab.
     * @param content The Node to be set as the tab's content.
     * @return A configured Tab.
     */
    private Tab createTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setClosable(false); // Tabs cannot be closed
        return tab;
    }

    // --- Admin Dashboard Content ---
    private VBox createAdminDashboardContent() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setAlignment(Pos.TOP_CENTER);
        dashboard.getStyleClass().add("content-pane");

        Label heading = new Label("Admin Dashboard");
        heading.getStyleClass().add("heading-large");

        // Basic statistics or quick links
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(20));
        statsGrid.getStyleClass().add("stats-grid");
        statsGrid.setAlignment(Pos.CENTER);

        // Fetch fresh data for dashboard stats
        int totalUsers = DatabaseManager.loadAllUsers().size();
        int totalOrders = DatabaseManager.getAllOrders(menuMap).size(); // Pass menuMap
        int totalBookings = DatabaseManager.getAllTableBookings().size();
        double totalRevenue = DatabaseManager.getAllOrders(menuMap).stream() // Pass menuMap
                                .mapToDouble(Order::getTotalWithGST)
                                .sum();

        Label usersLabel = new Label("Total Users: " + totalUsers);
        Label ordersLabel = new Label("Total Orders: " + totalOrders);
        Label bookingsLabel = new Label("Total Bookings: " + totalBookings);
        Label revenueLabel = new Label(String.format("Total Revenue: Rs.%.2f", totalRevenue));

        usersLabel.getStyleClass().add("stat-label");
        ordersLabel.getStyleClass().add("stat-label");
        bookingsLabel.getStyleClass().add("stat-label");
        revenueLabel.getStyleClass().add("stat-label");

        statsGrid.add(usersLabel, 0, 0);
        statsGrid.add(ordersLabel, 1, 0);
        statsGrid.add(bookingsLabel, 0, 1);
        statsGrid.add(revenueLabel, 1, 1);

        dashboard.getChildren().addAll(heading, new Separator(), statsGrid);
        return dashboard;
    }

    // --- Customer Dashboard Content ---
    private VBox createCustomerDashboardContent() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setAlignment(Pos.TOP_CENTER);
        dashboard.getStyleClass().add("content-pane");

        Label heading = new Label("Welcome, " + currentUser.getFullName() + "!");
        heading.getStyleClass().add("heading-large");

        // Display recent orders
        Label recentOrdersLabel = new Label("Your Recent Orders:");
        recentOrdersLabel.getStyleClass().add("sub-heading");

        TableView<Order> recentOrdersTable = new TableView<>();
        recentOrdersTable.setEditable(false);
        recentOrdersTable.getStyleClass().add("data-table");
        recentOrdersTable.setPrefHeight(200); // Limit height for dashboard view

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalWithGST"));
        totalCol.setPrefWidth(120);

        TableColumn<Order, LocalDateTime> orderTimeCol = new TableColumn<>("Order Time");
        orderTimeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        orderTimeCol.setPrefWidth(150);

        recentOrdersTable.getColumns().addAll(orderIdCol, statusCol, totalCol, orderTimeCol);

        // Load recent orders (e.g., last 5)
        List<Order> customerOrders = DatabaseManager.loadOrdersByCustomer(currentUser.getUsername(), menuMap);
        ObservableList<Order> limitedOrders = FXCollections.observableArrayList(
            customerOrders.stream().limit(5).collect(Collectors.toList())
        );
        recentOrdersTable.setItems(limitedOrders);
        if (limitedOrders.isEmpty()) {
            recentOrdersTable.setPlaceholder(new Label("You haven't placed any orders yet."));
        }

        // Display recent bookings
        Label recentBookingsLabel = new Label("Your Recent Table Bookings:");
        recentBookingsLabel.getStyleClass().add("sub-heading");

        TableView<TableBooking> recentBookingsTable = new TableView<>();
        recentBookingsTable.setEditable(false);
        recentBookingsTable.getStyleClass().add("data-table");
        recentBookingsTable.setPrefHeight(150); // Limit height

        TableColumn<TableBooking, TableType> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));
        tableTypeCol.setPrefWidth(120);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(80);

        TableColumn<TableBooking, PaymentStatus> bookingPaymentStatusCol = new TableColumn<>("Payment Status");
        bookingPaymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        bookingPaymentStatusCol.setPrefWidth(120);

        recentBookingsTable.getColumns().addAll(tableTypeCol, tableNumberCol, bookingPaymentStatusCol);

        List<TableBooking> customerBookings = DatabaseManager.loadTableBookingsByCustomer(currentUser.getUsername());
        ObservableList<TableBooking> limitedBookings = FXCollections.observableArrayList(
            customerBookings.stream().limit(3).collect(Collectors.toList())
        );
        recentBookingsTable.setItems(limitedBookings);
        if (limitedBookings.isEmpty()) {
            recentBookingsTable.setPlaceholder(new Label("You haven't made any table bookings yet."));
        }


        dashboard.getChildren().addAll(heading, new Separator(), recentOrdersLabel, recentOrdersTable, new Separator(), recentBookingsLabel, recentBookingsTable);
        return dashboard;
    }


    // --- Menu Management Tab (Admin Only) ---
    private VBox createMenuManagementTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Menu Management");
        heading.getStyleClass().add("heading-medium");

        menuManagementTable = new TableView<>();
        menuManagementTable.setEditable(true);
        menuManagementTable.getStyleClass().add("data-table");

        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<MenuItem, String> imageUrlCol = new TableColumn<>("Image URL");
        imageUrlCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imageUrlCol.setPrefWidth(250);

        menuManagementTable.getColumns().addAll(idCol, nameCol, priceCol, imageUrlCol);

        // Load current menu items
        menuManagementTable.setItems(FXCollections.observableArrayList(menuList));

        // Form for adding/updating menu items
        TextField idField = new TextField();
        idField.setPromptText("ID");
        idField.getStyleClass().add("text-field-small");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.getStyleClass().add("text-field-small");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.getStyleClass().add("text-field-small");
        TextField imageUrlField = new TextField(); 
        imageUrlField.setPromptText("Image URL (e.g., placehold.co/100x100)");
        imageUrlField.getStyleClass().add("text-field-small");


        Button addButton = new Button("Add/Update Item");
        addButton.getStyleClass().add("button-success");
        Button deleteButton = new Button("Delete Item");
        deleteButton.getStyleClass().add("button-danger");

        HBox formBox = new HBox(10, idField, nameField, priceField, imageUrlField, addButton, deleteButton);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(10, 0, 0, 0));

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        // Event Handlers
        addButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                String imageUrl = imageUrlField.getText().trim();

                if (name.isEmpty() || price <= 0) {
                    messageLabel.setText("Please enter valid name and price.");
                    messageLabel.getStyleClass().setAll("message-label", "error-label");
                    return;
                }

                // Provide a default image URL if none is entered
                if (imageUrl.isEmpty()) {
                    imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
                }

                MenuItem newItem = new MenuItem(id, name, price, imageUrl);
                if (DatabaseManager.addMenuItem(newItem)) {
                    loadAllData(); // Reload all data to refresh tables
                    menuManagementTable.setItems(FXCollections.observableArrayList(menuList)); // Refresh menu table
                    messageLabel.setText("Menu item saved successfully!");
                    messageLabel.getStyleClass().setAll("message-label", "success-label");
                    idField.clear();
                    nameField.clear();
                    priceField.clear();
                    imageUrlField.clear();
                } else {
                    messageLabel.setText("Failed to save menu item.");
                    messageLabel.getStyleClass().setAll("message-label", "error-label");
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("Invalid ID or Price. Please enter numeric values.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
            }
        });

        deleteButton.setOnAction(e -> {
            MenuItem selectedItem = menuManagementTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Confirm deletion
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Deletion");
                confirmation.setHeaderText("Delete Menu Item: " + selectedItem.getName() + "?");
                confirmation.setContentText("Are you sure you want to delete this menu item?");
                showAlert(confirmation.getAlertType(), confirmation.getTitle(), confirmation.getContentText());

                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (DatabaseManager.deleteMenuItem(selectedItem.getId())) {
                        loadAllData(); // Reload all data
                        menuManagementTable.setItems(FXCollections.observableArrayList(menuList)); // Refresh menu table
                        messageLabel.setText("Menu item deleted successfully!");
                        messageLabel.getStyleClass().setAll("message-label", "success-label");
                    } else {
                        messageLabel.setText("Failed to delete menu item.");
                        messageLabel.getStyleClass().setAll("message-label", "error-label");
                    }
                }
            } else {
                messageLabel.setText("Please select an item to delete.");
                messageLabel.getStyleClass().setAll("message-label", "warning-label");
            }
        });

        contentPane.getChildren().addAll(heading, new Separator(), formBox, menuManagementTable, messageLabel);
        return contentPane;
    }

    // --- Order Food Tab (Customer Only) ---
    private VBox createOrderFoodTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Place a New Order");
        heading.getStyleClass().add("heading-medium");

        ListView<MenuItem> menuListView = new ListView<>();
        menuListView.setItems(FXCollections.observableArrayList(menuList));
        menuListView.setCellFactory(param -> new ListCell<MenuItem>() {
            private ImageView imageView = new ImageView();
            private Label nameLabel = new Label();
            private Label priceLabel = new Label();
            private VBox cellBox = new VBox(5);
            private HBox layout = new HBox(10); // Use HBox for horizontal layout

            {
                // Initialize layout for the cell
                layout.setAlignment(Pos.CENTER_LEFT);
                layout.getChildren().addAll(imageView, cellBox);
                cellBox.getChildren().addAll(nameLabel, priceLabel);
            }

            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nameLabel.setText(item.getName());
                    priceLabel.setText("Rs." + String.format("%.2f", item.getPrice()));
                    imageView.setFitWidth(80); // Smaller image
                    imageView.setFitHeight(80);
                    imageView.setPreserveRatio(true);

                    // Load image with error handling (placeholder on failure)
                    try {
                        Image image = new Image(item.getImageUrl(), true); // true for background loading
                        imageView.setImage(image);
                        image.errorProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                // Fallback to placeholder if image fails to load
                                imageView.setImage(new Image("https://placehold.co/80x80/CCCCCC/FFFFFF?text=Error"));
                            }
                        });
                    } catch (IllegalArgumentException e) {
                        // Handle invalid URL format
                        imageView.setImage(new Image("https://placehold.co/80x80/CCCCCC/FFFFFF?text=Invalid"));
                    }

                    setGraphic(layout);
                    getStyleClass().add("menu-item-cell"); // Custom style for menu item cells
                }
            }
        });
        menuListView.getStyleClass().add("list-view-custom");

        // Current Order Display
        Label currentOrderLabel = new Label("Your Current Order:");
        currentOrderLabel.getStyleClass().add("sub-heading");
        TextArea orderSummaryArea = new TextArea();
        orderSummaryArea.setEditable(false);
        orderSummaryArea.setPrefHeight(150);
        orderSummaryArea.getStyleClass().add("text-area-custom");

        Order currentOrder = new Order(DatabaseManager.getNextAvailableOrderId());
        if (currentUser != null) {
            currentOrder.setCustomerUsername(currentUser.getUsername());
        }

        // Keep a reference to the current order outside the lambda for continuous updates
        final Order[] mutableCurrentOrder = {currentOrder};

        // Function to update the order summary display
        Runnable updateOrderSummary = () -> {
            orderSummaryArea.setText(mutableCurrentOrder[0].generateReceipt());
        };

        Button addToOrderButton = new Button("Add to Order");
        addToOrderButton.getStyleClass().add("button-success");
        addToOrderButton.setOnAction(e -> {
            MenuItem selectedItem = menuListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                mutableCurrentOrder[0].addItem(selectedItem);
                updateOrderSummary.run();
                showAlert(Alert.AlertType.INFORMATION, "Item Added", selectedItem.getName() + " added to your order.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a menu item to add.");
            }
        });

        Button removeFromOrderButton = new Button("Remove Last Item");
        removeFromOrderButton.getStyleClass().add("button-warning");
        removeFromOrderButton.setOnAction(e -> {
            if (!mutableCurrentOrder[0].getItems().isEmpty()) {
                MenuItem removedItem = mutableCurrentOrder[0].getItems().remove(mutableCurrentOrder[0].getItems().size() - 1);
                updateOrderSummary.run();
                showAlert(Alert.AlertType.INFORMATION, "Item Removed", removedItem.getName() + " removed from your order.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Empty Order", "Your order is empty.");
            }
        });

        HBox orderButtons = new HBox(10, addToOrderButton, removeFromOrderButton);
        orderButtons.setAlignment(Pos.CENTER);

        ComboBox<PaymentMethod> paymentMethodComboBox = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.setPromptText("Select Payment Method");
        paymentMethodComboBox.getStyleClass().add("combo-box-custom");
        paymentMethodComboBox.setValue(PaymentMethod.CASH); // Default selection

        Button placeOrderButton = new Button("Place Order");
        placeOrderButton.getStyleClass().add("button-primary");
        placeOrderButton.setOnAction(e -> {
            if (mutableCurrentOrder[0].getItems().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Empty Order", "Cannot place an empty order. Please add items.");
                return;
            }
            if (paymentMethodComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Payment Required", "Please select a payment method.");
                return;
            }

            mutableCurrentOrder[0].setPaymentMethod(paymentMethodComboBox.getValue());
            mutableCurrentOrder[0].setPaymentStatus(PaymentStatus.PENDING); // Assume pending until paid
            mutableCurrentOrder[0].setStatus(OrderStatus.PENDING); // Initial order status
            mutableCurrentOrder[0].setOrderTime(LocalDateTime.now()); // Set the exact order time

            if (DatabaseManager.addOrder(mutableCurrentOrder[0])) {
                showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Your order (ID: " + mutableCurrentOrder[0].getOrderId() + ") has been placed successfully!");
                loadAllData(); // Refresh all data including orders
                // Start a new order for the next transaction
                mutableCurrentOrder[0] = new Order(DatabaseManager.getNextAvailableOrderId());
                if (currentUser != null) {
                    mutableCurrentOrder[0].setCustomerUsername(currentUser.getUsername());
                }
                updateOrderSummary.run(); // Clear summary for new order
            } else {
                showAlert(Alert.AlertType.ERROR, "Order Failed", "Failed to place order. Please try again.");
            }
        });

        contentPane.getChildren().addAll(heading, new Separator(), new Label("Available Menu Items:"), menuListView, orderButtons, currentOrderLabel, orderSummaryArea, paymentMethodComboBox, placeOrderButton);
        return contentPane;
    }


    // --- Book Table Tab (Customer Only) ---
    private VBox createBookTableTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Book a Table");
        heading.getStyleClass().add("heading-medium");

        // Date and Time selection for availability check
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");
        datePicker.getStyleClass().add("date-picker-custom");

        ComboBox<String> timeComboBox = new ComboBox<>();
        timeComboBox.setPromptText("Select Time");
        populateTimeComboBox(timeComboBox); // Populate with time slots
        timeComboBox.getStyleClass().add("combo-box-custom");

        ComboBox<TableType> tableTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(TableType.values()));
        tableTypeComboBox.setPromptText("Select Table Type (Optional)");
        tableTypeComboBox.getStyleClass().add("combo-box-custom");

        Spinner<Integer> seatsSpinner = new Spinner<>(1, 20, 2); // Min 1, Max 20, Default 2
        seatsSpinner.setEditable(true);
        seatsSpinner.setPromptText("Required Seats");
        seatsSpinner.getStyleClass().add("spinner-custom");

        Spinner<Integer> durationSpinner = new Spinner<>(30, 240, BOOKING_DURATION_MINUTES, 30); // 30 mins to 4 hours, step 30
        durationSpinner.setEditable(true);
        durationSpinner.setPromptText("Duration in minutes");
        durationSpinner.getStyleClass().add("spinner-custom");


        Button checkAvailabilityButton = new Button("Check Availability");
        checkAvailabilityButton.getStyleClass().add("button-info");

        TableView<Map<String, Object>> availableTablesTable = new TableView<>();
        availableTablesTable.getStyleClass().add("data-table");
        availableTablesTable.setPlaceholder(new Label("Select date, time, and seats to check table availability."));

        TableColumn<Map<String, Object>, Integer> tableNumCol = new TableColumn<>("Table Number");
        // Corrected to cast to Integer directly from the Map value
        tableNumCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue().get("tableNumber")).asObject());

        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get("tableType").toString()));

        TableColumn<Map<String, Object>, Integer> tableSeatsCol = new TableColumn<>("Seats");
        // Corrected to cast to Integer directly from the Map value
        tableSeatsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue().get("seats")).asObject());
        availableTablesTable.getColumns().addAll(tableNumCol, typeCol, tableSeatsCol);


        Label selectedTableLabel = new Label("Selected Table: None");
        selectedTableLabel.getStyleClass().add("info-label");

        // Event listener for table selection
        availableTablesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTableLabel.setText("Selected Table: Table " + newSelection.get("tableNumber") +
                                           " (" + newSelection.get("tableType") + ", " + newSelection.get("seats") + " seats)");
            } else {
                selectedTableLabel.setText("Selected Table: None");
            }
        });


        Button bookSelectedTableButton = new Button("Book Selected Table");
        bookSelectedTableButton.getStyleClass().add("button-primary");
        bookSelectedTableButton.setDisable(true); // Disable until a table is selected

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");


        checkAvailabilityButton.setOnAction(e -> {
            if (datePicker.getValue() == null || timeComboBox.getValue() == null || seatsSpinner.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Info", "Please select a date, time, and number of seats.");
                return;
            }
            try {
                LocalDateTime desiredTime = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(timeComboBox.getValue()));
                int requiredSeats = seatsSpinner.getValue();
                int duration = durationSpinner.getValue();

                List<Map<String, Object>> available = DatabaseManager.getAvailableTables(desiredTime, duration, requiredSeats);

                // Filter by specific table type if selected
                TableType selectedTypeFilter = tableTypeComboBox.getValue();
                if (selectedTypeFilter != null) {
                    available = available.stream()
                                         .filter(t -> ((String) t.get("tableType")).equalsIgnoreCase(selectedTypeFilter.getDisplayValue()))
                                         .collect(Collectors.toList());
                }

                availableTablesTable.setItems(FXCollections.observableArrayList(available));
                if (available.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "No Tables", "No tables available for your criteria.");
                    bookSelectedTableButton.setDisable(true);
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Tables Found", "Found " + available.size() + " available tables. Select one to book.");
                    bookSelectedTableButton.setDisable(false);
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error checking availability: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        bookSelectedTableButton.setOnAction(e -> {
            Map<String, Object> selectedTable = availableTablesTable.getSelectionModel().getSelectedItem();
            if (selectedTable == null) {
                showAlert(Alert.AlertType.WARNING, "No Table Selected", "Please select a table from the list to book.");
                return;
            }

            // Retrieve all necessary booking details
            LocalDateTime bookingDateTime = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(timeComboBox.getValue()));
            TableType bookedTableType = TableType.fromString((String) selectedTable.get("tableType"));
            int bookedTableNumber = (Integer) selectedTable.get("tableNumber");
            int bookedSeats = (Integer) selectedTable.get("seats");
            int duration = durationSpinner.getValue();
            double bookingFee = bookedSeats * 50.0; // Example booking fee

            TableBooking newBooking = new TableBooking(
                currentUser.getUsername(),
                currentUser.getFullName(),
                currentUser.getPhoneNumber(),
                bookedTableType,
                bookedTableNumber,
                bookingDateTime, // Set the actual booking time
                duration,      // Set the actual duration
                bookingFee
            );

            if (DatabaseManager.addTableBooking(newBooking)) {
                showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed!",
                          "Table " + bookedTableNumber + " (" + bookedTableType.getDisplayValue() + ") booked for " +
                          bookingDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " for " + duration + " minutes. " +
                          String.format("Booking Fee: Rs.%.2f", bookingFee));
                loadAllData(); // Refresh all bookings
                availableTablesTable.setItems(FXCollections.emptyObservableList()); // Clear availability table
                selectedTableLabel.setText("Selected Table: None");
                bookSelectedTableButton.setDisable(true);
            } else {
                showAlert(Alert.AlertType.ERROR, "Booking Failed", "Could not complete your booking. This time slot or table might have just become unavailable, or another error occurred.");
            }
        });

        HBox topInputRow = new HBox(10, datePicker, timeComboBox);
        HBox bottomInputRow = new HBox(10, tableTypeComboBox, seatsSpinner, durationSpinner, checkAvailabilityButton);
        bottomInputRow.setAlignment(Pos.CENTER_LEFT);

        HBox bookingActions = new HBox(10, selectedTableLabel, bookSelectedTableButton);
        bookingActions.setAlignment(Pos.CENTER);

        contentPane.getChildren().addAll(heading, new Separator(), new Label("1. Select your preferred time and seats:"),
                                         topInputRow, bottomInputRow,
                                         new Separator(), new Label("2. Available Tables:"), availableTablesTable,
                                         bookingActions, messageLabel);
        return contentPane;
    }


    private void populateTimeComboBox(ComboBox<String> comboBox) {
        // Populate with hourly slots from 8 AM to 10 PM
        for (int hour = 8; hour <= 22; hour++) {
            comboBox.getItems().add(String.format("%02d:00", hour));
            // Add half-hour slots for more granularity if needed
            // comboBox.getItems().add(String.format("%02d:30", hour));
        }
    }


    // --- Give Feedback Tab (Customer Only) ---
    private VBox createGiveFeedbackTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Give Us Your Feedback");
        heading.getStyleClass().add("heading-medium");

        Label ratingLabel = new Label("Overall Rating (1-5 Stars):");
        Slider ratingSlider = new Slider(1, 5, 3); // Min, Max, Default
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.getStyleClass().add("slider-custom");

        Label commentsLabel = new Label("Comments:");
        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Tell us about your experience...");
        commentsArea.setWrapText(true);
        commentsArea.setPrefRowCount(5);
        commentsArea.getStyleClass().add("text-area-custom");

        Button submitButton = new Button("Submit Feedback");
        submitButton.getStyleClass().add("button-primary");
        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        submitButton.setOnAction(e -> {
            int rating = (int) ratingSlider.getValue();
            String comments = commentsArea.getText().trim();

            if (currentUser == null) {
                messageLabel.setText("You must be logged in to submit feedback.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
                return;
            }

            if (comments.isEmpty()) {
                messageLabel.setText("Please provide some comments.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
                return;
            }

            Feedback newFeedback = new Feedback(0, currentUser.getUsername(), rating, comments, LocalDateTime.now());
            if (DatabaseManager.addFeedback(newFeedback)) {
                showAlert(Alert.AlertType.INFORMATION, "Feedback Submitted", "Thank you for your feedback!");
                commentsArea.clear();
                ratingSlider.setValue(3);
                loadAllData(); // Refresh feedback data for admin view if needed
                messageLabel.setText("");
            } else {
                messageLabel.setText("Failed to submit feedback. Please try again.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
            }
        });

        contentPane.getChildren().addAll(heading, new Separator(), ratingLabel, ratingSlider, commentsLabel, commentsArea, submitButton, messageLabel);
        return contentPane;
    }

    // --- Rate a Dish Tab (Customer Only) ---
    private VBox createRateDishTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Rate a Dish");
        heading.getStyleClass().add("heading-medium");

        Label dishSelectionLabel = new Label("Select a Dish to Rate:");
        ComboBox<MenuItem> dishComboBox = new ComboBox<>(FXCollections.observableArrayList(menuList));
        dishComboBox.setPromptText("Choose a dish");
        dishComboBox.getStyleClass().add("combo-box-custom");
        dishComboBox.setCellFactory(lv -> new ListCell<MenuItem>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        dishComboBox.setButtonCell(new ListCell<MenuItem>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        Label dishRatingLabel = new Label("Your Rating (1-5 Stars):");
        Slider dishRatingSlider = new Slider(1, 5, 3);
        dishRatingSlider.setMajorTickUnit(1);
        dishRatingSlider.setMinorTickCount(0);
        dishRatingSlider.setShowTickLabels(true);
        dishRatingSlider.setShowTickMarks(true);
        dishRatingSlider.setSnapToTicks(true);
        dishRatingSlider.getStyleClass().add("slider-custom");

        Button submitRatingButton = new Button("Submit Dish Rating");
        submitRatingButton.getStyleClass().add("button-primary");
        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        submitRatingButton.setOnAction(e -> {
            MenuItem selectedDish = dishComboBox.getValue();
            int rating = (int) dishRatingSlider.getValue();

            if (currentUser == null) {
                messageLabel.setText("You must be logged in to rate a dish.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
                return;
            }

            if (selectedDish == null) {
                messageLabel.setText("Please select a dish to rate.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
                return;
            }

            DishRating newDishRating = new DishRating(0, selectedDish.getId(), currentUser.getUsername(), rating, LocalDateTime.now());
            if (DatabaseManager.addDishRating(newDishRating)) {
                showAlert(Alert.AlertType.INFORMATION, "Rating Submitted", "Thank you for rating " + selectedDish.getName() + "!");
                dishComboBox.getSelectionModel().clearSelection();
                dishRatingSlider.setValue(3);
                loadAllData(); // Refresh dish ratings for admin view if needed
                messageLabel.setText("");
            } else {
                messageLabel.setText("Failed to submit dish rating. Please try again.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
            }
        });

        contentPane.getChildren().addAll(heading, new Separator(), dishSelectionLabel, dishComboBox, dishRatingLabel, dishRatingSlider, submitRatingButton, messageLabel);
        return contentPane;
    }

    // --- My Profile Tab (Customer Only) ---
    private VBox createMyProfileTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("My Profile");
        heading.getStyleClass().add("heading-medium");

        // Display current user details
        Label usernameDisplay = new Label("Username: " + currentUser.getUsername());
        usernameDisplay.getStyleClass().add("profile-label");

        TextField fullNameField = new TextField(currentUser.getFullName());
        fullNameField.setPromptText("Full Name");
        fullNameField.getStyleClass().add("text-field-custom");

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field-custom");

        TextField phoneField = new TextField(currentUser.getPhoneNumber());
        phoneField.setPromptText("Phone Number");
        phoneField.getStyleClass().add("text-field-custom");

        Button updateProfileButton = new Button("Update Profile");
        updateProfileButton.getStyleClass().add("button-primary");
        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        updateProfileButton.setOnAction(e -> {
            String newFullName = fullNameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newFullName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                messageLabel.setText("All fields must be filled.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
                return;
            }

            // Update the currentUser object
            currentUser.setFullName(newFullName);
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhone);

            if (DatabaseManager.updateUserProfile(currentUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your profile has been updated successfully!");
                messageLabel.setText(""); // Clear message
                // Refresh main application view to update top bar info if necessary
                // This is a bit heavy-handed, could just update the top bar label
                // For now, re-showing the main app will update the top bar
                showMainApplication(fullNameField.getScene().getWindow()); 
            } else {
                messageLabel.setText("Failed to update profile. Please try again.");
                messageLabel.getStyleClass().setAll("message-label", "error-label");
            }
        });

        contentPane.getChildren().addAll(heading, new Separator(), usernameDisplay, new Label("Edit your details:"), fullNameField, emailField, phoneField, updateProfileButton, messageLabel);
        return contentPane;
    }


    // --- My Orders Tab (Customer Only) ---
    private VBox createMyOrdersTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("My Orders");
        heading.getStyleClass().add("heading-medium");

        TableView<Order> myOrdersTable = new TableView<>();
        myOrdersTable.setEditable(false);
        myOrdersTable.getStyleClass().add("data-table");

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalWithGST"));
        totalCol.setPrefWidth(120);

        TableColumn<Order, LocalDateTime> orderTimeCol = new TableColumn<>("Order Time");
        orderTimeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        orderTimeCol.setPrefWidth(150);

        myOrdersTable.getColumns().addAll(orderIdCol, statusCol, paymentStatusCol, totalCol, orderTimeCol);

        // Load orders specific to the logged-in user
        if (currentUser != null) {
            ObservableList<Order> customerOrders = FXCollections.observableArrayList(
                    DatabaseManager.loadOrdersByCustomer(currentUser.getUsername(), menuMap)
            );
            myOrdersTable.setItems(customerOrders);
            if (customerOrders.isEmpty()) {
                myOrdersTable.setPlaceholder(new Label("You have no orders yet."));
            }
        } else {
            myOrdersTable.setItems(FXCollections.emptyObservableList());
            // This case should ideally not happen if tabs are shown after login
            myOrdersTable.setPlaceholder(new Label("Please log in to see your orders."));
        }

        // Add a refresh button for customer orders
        Button refreshButton = new Button("Refresh Orders");
        refreshButton.getStyleClass().add("button-info");
        refreshButton.setOnAction(e -> {
            if (currentUser != null) {
                myOrdersTable.setItems(FXCollections.observableArrayList(
                    DatabaseManager.loadOrdersByCustomer(currentUser.getUsername(), menuMap)
                ));
                if (myOrdersTable.getItems().isEmpty()) {
                    myOrdersTable.setPlaceholder(new Label("You have no orders yet."));
                }
                showAlert(Alert.AlertType.INFORMATION, "Orders Refreshed", "Your order list has been updated.");
            }
        });

        HBox actionButtons = new HBox(10, refreshButton);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));


        contentPane.getChildren().addAll(heading, new Separator(), myOrdersTable, actionButtons);
        return contentPane;
    }

    // --- My Bookings Tab (Customer Only) ---
    private VBox createMyBookingsTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("My Table Bookings");
        heading.getStyleClass().add("heading-medium");

        TableView<TableBooking> myBookingsTable = new TableView<>();
        myBookingsTable.setEditable(false);
        myBookingsTable.getStyleClass().add("data-table");

        // Table Columns
        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Booked By"); // Can keep for full name display
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setPrefWidth(150);

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<TableBooking, TableType> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));
        tableTypeCol.setPrefWidth(120);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(80);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(60);

        TableColumn<TableBooking, Double> bookingFeeCol = new TableColumn<>("Booking Fee");
        bookingFeeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        bookingFeeCol.setPrefWidth(100);
        
        TableColumn<TableBooking, String> bookingTimeCol = new TableColumn<>("Booking Time");
        bookingTimeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        bookingTimeCol.setPrefWidth(150);

        TableColumn<TableBooking, Integer> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        durationCol.setPrefWidth(100);

        TableColumn<TableBooking, PaymentStatus> bookingPaymentStatusCol = new TableColumn<>("Payment Status");
        bookingPaymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        bookingPaymentStatusCol.setPrefWidth(120);

        myBookingsTable.getColumns().addAll(customerNameCol, phoneCol, tableTypeCol, tableNumberCol, seatsCol, bookingFeeCol, bookingTimeCol, durationCol, bookingPaymentStatusCol);

        // Load bookings specific to the logged-in user
        if (currentUser != null) {
            ObservableList<TableBooking> customerBookings = FXCollections.observableArrayList(
                    DatabaseManager.loadTableBookingsByCustomer(currentUser.getUsername())
            );
            myBookingsTable.setItems(customerBookings);
            if (customerBookings.isEmpty()) {
                myBookingsTable.setPlaceholder(new Label("You have no table bookings yet."));
            }
        } else {
            myBookingsTable.setItems(FXCollections.emptyObservableList());
            myBookingsTable.setPlaceholder(new Label("Please log in to see your bookings."));
        }

        // Add a refresh button for customer bookings
        Button refreshButton = new Button("Refresh Bookings");
        refreshButton.getStyleClass().add("button-info");
        refreshButton.setOnAction(e -> {
            if (currentUser != null) {
                myBookingsTable.setItems(FXCollections.observableArrayList(
                    DatabaseManager.loadTableBookingsByCustomer(currentUser.getUsername())
                ));
                if (myBookingsTable.getItems().isEmpty()) {
                    myBookingsTable.setPlaceholder(new Label("You have no table bookings yet."));
                }
                showAlert(Alert.AlertType.INFORMATION, "Bookings Refreshed", "Your booking list has been updated.");
            }
        });

        HBox actionButtons = new HBox(10, refreshButton);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        contentPane.getChildren().addAll(heading, new Separator(), myBookingsTable, actionButtons);
        return contentPane;
    }

    // --- Order Management Tab (Admin Only) ---
    private VBox createOrderManagementTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Order Management (Admin)");
        heading.getStyleClass().add("heading-medium");

        ordersTable = new TableView<>();
        ordersTable.setEditable(true);
        ordersTable.getStyleClass().add("data-table");

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer Username");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerCol.setPrefWidth(150);

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(tc -> new ComboBoxTableCell<>(FXCollections.observableArrayList(OrderStatus.values())));
        statusCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            OrderStatus newStatus = event.getNewValue();
            if (DatabaseManager.updateOrderStatus(order.getOrderId(), newStatus)) {
                order.setStatus(newStatus); // Update local object
                showAlert(Alert.AlertType.INFORMATION, "Update Success", "Order status updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update order status.");
                ordersTable.refresh(); // Revert UI change
            }
        });

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);
        paymentStatusCol.setCellFactory(tc -> new ComboBoxTableCell<>(FXCollections.observableArrayList(PaymentStatus.values())));
        paymentStatusCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            PaymentStatus newPaymentStatus = event.getNewValue();
            if (DatabaseManager.updateOrderPaymentStatus(order.getOrderId(), newPaymentStatus)) {
                order.setPaymentStatus(newPaymentStatus); // Update local object
                showAlert(Alert.AlertType.INFORMATION, "Update Success", "Order payment status updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update order payment status.");
                ordersTable.refresh(); // Revert UI change
            }
        });

        TableColumn<Order, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);
        paymentMethodCol.setCellFactory(tc -> new ComboBoxTableCell<>(FXCollections.observableArrayList(PaymentMethod.values())));
        paymentMethodCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            PaymentMethod newPaymentMethod = event.getNewValue();
            if (DatabaseManager.updateOrderPaymentMethod(order.getOrderId(), newPaymentMethod)) {
                order.setPaymentMethod(newPaymentMethod); // Update local object
                showAlert(Alert.AlertType.INFORMATION, "Update Success", "Order payment method updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update order payment method.");
                ordersTable.refresh(); // Revert UI change
            }
        });

        TableColumn<Order, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subtotalCol.setPrefWidth(90);

        TableColumn<Order, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountApplied"));
        discountCol.setPrefWidth(80);

        TableColumn<Order, Double> gstCol = new TableColumn<>("GST");
        gstCol.setCellValueFactory(new PropertyValueFactory<>("GSTAmount"));
        gstCol.setPrefWidth(70);

        TableColumn<Order, Double> finalAmountCol = new TableColumn<>("Final Amount");
        finalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalWithGST"));
        finalAmountCol.setPrefWidth(110);

        TableColumn<Order, LocalDateTime> orderTimeCol = new TableColumn<>("Order Time");
        orderTimeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        orderTimeCol.setPrefWidth(150);

        ordersTable.getColumns().addAll(orderIdCol, customerCol, statusCol, paymentStatusCol, paymentMethodCol,
                                      subtotalCol, discountCol, gstCol, finalAmountCol, orderTimeCol);

        allOrders.setAll(DatabaseManager.getAllOrders(menuMap)); // Reload all orders - Pass menuMap
        ordersTable.setItems(allOrders);
        if (allOrders.isEmpty()) {
            ordersTable.setPlaceholder(new Label("No orders found."));
        }


        // Details Button (View Items in Order)
        Button viewDetailsButton = new Button("View Order Details");
        viewDetailsButton.getStyleClass().add("button-info");
        viewDetailsButton.setOnAction(e -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                showOrderDetailsDialog(selectedOrder);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order to view its details.");
            }
        });

        // Delete Button
        Button deleteOrderButton = new Button("Delete Order");
        deleteOrderButton.getStyleClass().add("button-danger");
        deleteOrderButton.setOnAction(e -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Deletion");
                confirmation.setHeaderText("Delete Order " + selectedOrder.getOrderId() + "?");
                confirmation.setContentText("Are you sure you want to delete this order? This action cannot be undone.");
                URL cssUrl = getClass().getResource("style.css");
                if (cssUrl != null) {
                    confirmation.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("WARNING: style.css not found for Confirmation Dialog. UI may not be styled correctly. Expected path: application/style.css (relative to classpath)");
                }
                confirmation.getDialogPane().getStyleClass().add("alert-dialog-custom");
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if (DatabaseManager.deleteOrder(selectedOrder.getOrderId())) {
                            allOrders.remove(selectedOrder); // Remove from ObservableList
                            showAlert(Alert.AlertType.INFORMATION, "Deletion Success", "Order deleted successfully.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete order.");
                        }
                    }
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order to delete.");
            }
        });

        HBox actionButtons = new HBox(10, viewDetailsButton, deleteOrderButton);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));


        contentPane.getChildren().addAll(heading, new Separator(), ordersTable, actionButtons);
        return contentPane;
    }

    /**
     * Shows a dialog with detailed information about a selected order.
     * @param order The Order object to display.
     */
    private void showOrderDetailsDialog(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details (ID: " + order.getOrderId() + ")");
        alert.setHeaderText("Order by " + order.getCustomerUsername());

        TextArea textArea = new TextArea(order.generateReceipt());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.setHgap(10);
        expContent.setVgap(5);
        expContent.setPadding(new Insets(10));

        expContent.add(new Label("Order Time:"), 0, 0);
        expContent.add(new Label(order.getOrderTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), 1, 0);
        expContent.add(new Label("Status:"), 0, 1);
        expContent.add(new Label(order.getStatus().getDisplayValue()), 1, 1);
        expContent.add(new Label("Payment Status:"), 0, 2);
        expContent.add(new Label(order.getPaymentStatus().getDisplayValue()), 1, 2);
        expContent.add(new Label("Payment Method:"), 0, 3);
        expContent.add(new Label(order.getPaymentMethod().getDisplayValue()), 1, 3);
        expContent.add(new Label("Discount:"), 0, 4);
        expContent.add(new Label(String.format("Rs.%.2f", order.getDiscountApplied())), 1, 4);
        expContent.add(new Label("Final Amount:"), 0, 5);
        expContent.add(new Label(String.format("Rs.%.2f", order.getTotalWithGST())), 1, 5);
        expContent.add(new Label("Full Receipt:"), 0, 6, 2, 1); // Span two columns for the label
        expContent.add(textArea, 0, 7, 2, 1); // Span two columns for the text area

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true); // Show details by default
        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: style.css not found for Order Details Dialog. UI may not be styled correctly. Expected path: application/style.css (relative to classpath)");
        }
        alert.getDialogPane().getStyleClass().add("alert-dialog-custom");

        alert.showAndWait();
    }


    // --- Table Booking Management Tab (Admin Only) ---
    private VBox createTableBookingManagementTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Table Booking Management (Admin)");
        heading.getStyleClass().add("heading-medium");

        tableBookingsTable = new TableView<>();
        tableBookingsTable.setEditable(true); // Allow editing payment status
        tableBookingsTable.getStyleClass().add("data-table");

        TableColumn<TableBooking, String> customerIdCol = new TableColumn<>("Customer Username");
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerIdCol.setPrefWidth(150);

        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setPrefWidth(150);

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<TableBooking, TableType> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));
        tableTypeCol.setPrefWidth(120);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(80);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(60);

        TableColumn<TableBooking, Double> bookingFeeCol = new TableColumn<>("Booking Fee");
        bookingFeeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        bookingFeeCol.setPrefWidth(100);
        
        TableColumn<TableBooking, String> bookingTimeCol = new TableColumn<>("Booking Time");
        bookingTimeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        bookingTimeCol.setPrefWidth(150);

        TableColumn<TableBooking, Integer> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        durationCol.setPrefWidth(100);

        TableColumn<TableBooking, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);
        paymentStatusCol.setCellFactory(tc -> new ComboBoxTableCell<>(FXCollections.observableArrayList(PaymentStatus.values())));
        paymentStatusCol.setOnEditCommit(event -> {
            TableBooking booking = event.getRowValue();
            PaymentStatus newStatus = event.getNewValue();
            // Need customerUsername and tableNumber to uniquely identify the booking for update
            if (DatabaseManager.updateTableBookingPaymentStatus(booking.getCustomerId(), booking.getTableNumber(), newStatus)) {
                booking.setPaymentStatus(newStatus); // Update local object
                showAlert(Alert.AlertType.INFORMATION, "Update Success", "Booking payment status updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update booking payment status.");
                tableBookingsTable.refresh(); // Revert UI change
            }
        });

        tableBookingsTable.getColumns().addAll(customerIdCol, customerNameCol, phoneCol, tableTypeCol, tableNumberCol, seatsCol, bookingFeeCol, bookingTimeCol, durationCol, paymentStatusCol);

        allTableBookings.setAll(DatabaseManager.getAllTableBookings()); // Reload all bookings
        tableBookingsTable.setItems(allTableBookings);
        if (allTableBookings.isEmpty()) {
            tableBookingsTable.setPlaceholder(new Label("No table bookings found."));
        }


        // Delete Button
        Button deleteBookingButton = new Button("Delete Booking");
        deleteBookingButton.getStyleClass().add("button-danger");
        deleteBookingButton.setOnAction(e -> {
            TableBooking selectedBooking = tableBookingsTable.getSelectionModel().getSelectedItem();
            if (selectedBooking != null) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Deletion");
                confirmation.setHeaderText("Delete Booking for " + selectedBooking.getCustomerName() + "?");
                confirmation.setContentText("Are you sure you want to delete this table booking?");
                URL cssUrl = getClass().getResource("style.css");
                if (cssUrl != null) {
                    confirmation.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("WARNING: style.css not found for Confirmation Dialog. UI may not be styled correctly. Expected path: application/style.css (relative to classpath)");
                }
                confirmation.getDialogPane().getStyleClass().add("alert-dialog-custom");
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if (DatabaseManager.deleteTableBooking(selectedBooking.getCustomerId(), selectedBooking.getTableNumber())) {
                            allTableBookings.remove(selectedBooking); // Remove from ObservableList
                            showAlert(Alert.AlertType.INFORMATION, "Deletion Success", "Table booking deleted successfully.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete table booking.");
                        }
                    }
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "No Booking Selected", "Please select a booking to delete.");
            }
        });

        HBox actionButtons = new HBox(10, deleteBookingButton);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        contentPane.getChildren().addAll(heading, new Separator(), tableBookingsTable, actionButtons);
        return contentPane;
    }

    // --- Feedback Management Tab (Admin Only) ---
    private VBox createFeedbackManagementTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Customer Feedback Review (Admin)");
        heading.getStyleClass().add("heading-medium");

        feedbackTable = new TableView<>();
        feedbackTable.setEditable(false);
        feedbackTable.getStyleClass().add("data-table");

        TableColumn<Feedback, Integer> feedbackIdCol = new TableColumn<>("ID");
        feedbackIdCol.setCellValueFactory(new PropertyValueFactory<>("feedbackId"));
        feedbackIdCol.setPrefWidth(60);

        TableColumn<Feedback, String> usernameCol = new TableColumn<>("Customer Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        usernameCol.setPrefWidth(150);

        TableColumn<Feedback, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(80);

        TableColumn<Feedback, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        commentsCol.setPrefWidth(350);

        TableColumn<Feedback, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));
        dateCol.setPrefWidth(150);

        feedbackTable.getColumns().addAll(feedbackIdCol, usernameCol, ratingCol, commentsCol, dateCol);

        allFeedback.setAll(DatabaseManager.getAllFeedback()); // Reload all feedback
        feedbackTable.setItems(allFeedback);
        if (allFeedback.isEmpty()) {
            feedbackTable.setPlaceholder(new Label("No customer feedback submitted yet."));
        }

        contentPane.getChildren().addAll(heading, new Separator(), feedbackTable);
        return contentPane;
    }

    // --- Dish Ratings Review Tab (Admin Only) ---
    private VBox createDishRatingReviewTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("Dish Ratings Review (Admin)");
        heading.getStyleClass().add("heading-medium");

        TableView<DishRating> dishRatingTable = new TableView<>();
        dishRatingTable.setEditable(false);
        dishRatingTable.getStyleClass().add("data-table");

        TableColumn<DishRating, Integer> ratingIdCol = new TableColumn<>("Rating ID");
        ratingIdCol.setCellValueFactory(new PropertyValueFactory<>("ratingId"));
        ratingIdCol.setPrefWidth(80);

        TableColumn<DishRating, Integer> menuItemIdCol = new TableColumn<>("Menu Item ID");
        menuItemIdCol.setCellValueFactory(new PropertyValueFactory<>("menuItemId"));
        menuItemIdCol.setPrefWidth(100);

        // Add a column to display MenuItem Name
        TableColumn<DishRating, String> dishNameCol = new TableColumn<>("Dish Name");
        dishNameCol.setCellValueFactory(cellData -> {
            int itemId = cellData.getValue().getMenuItemId();
            MenuItem item = menuMap.get(itemId);
            return new ReadOnlyStringWrapper(item != null ? item.getName() : "Unknown Dish");
        });
        dishNameCol.setPrefWidth(180);

        TableColumn<DishRating, String> customerUsernameCol = new TableColumn<>("Customer Username");
        customerUsernameCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerUsernameCol.setPrefWidth(150);

        TableColumn<DishRating, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(70);

        TableColumn<DishRating, LocalDateTime> dateCol = new TableColumn<>("Rating Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("ratingDate"));
        dateCol.setPrefWidth(150);

        dishRatingTable.getColumns().addAll(ratingIdCol, menuItemIdCol, dishNameCol, customerUsernameCol, ratingCol, dateCol);

        allDishRatings.setAll(DatabaseManager.loadDishRatings()); // Reload all dish ratings
        dishRatingTable.setItems(allDishRatings);
        if (allDishRatings.isEmpty()) {
            dishRatingTable.setPlaceholder(new Label("No dish ratings submitted yet."));
        }


        // Display average ratings for each dish
        Label averageRatingsLabel = new Label("Average Dish Ratings:");
        averageRatingsLabel.getStyleClass().add("sub-heading");

        TextArea averageRatingsArea = new TextArea();
        averageRatingsArea.setEditable(false);
        averageRatingsArea.setPrefHeight(100);
        averageRatingsArea.getStyleClass().add("text-area-custom");

        // Calculate and display averages
        StringBuilder sb = new StringBuilder();
        if (menuList.isEmpty()) {
            sb.append("No menu items available to calculate ratings.");
        } else {
            for (MenuItem item : menuList) {
                double avgRating = DatabaseManager.getAverageDishRating(item.getId());
                sb.append(String.format("%s: %.1f stars%n", item.getName(), avgRating));
            }
        }
        averageRatingsArea.setText(sb.toString());


        contentPane.getChildren().addAll(heading, new Separator(), dishRatingTable, averageRatingsLabel, averageRatingsArea);
        return contentPane;
    }

    // --- User Management Tab (Admin Only) ---
    private VBox createUserManagementTab() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("content-pane");

        Label heading = new Label("User Management (Admin)");
        heading.getStyleClass().add("heading-medium");

        userManagementTable = new TableView<>();
        userManagementTable.setEditable(false); // Users are not editable directly from this table for simplicity
        userManagementTable.getStyleClass().add("data-table");

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        fullNameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);

        TableColumn<User, UserRole> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        userManagementTable.getColumns().addAll(usernameCol, fullNameCol, emailCol, phoneCol, roleCol);

        allUsers.setAll(DatabaseManager.loadAllUsers()); // Reload all users
        userManagementTable.setItems(allUsers);
        if (allUsers.isEmpty()) {
            userManagementTable.setPlaceholder(new Label("No users registered yet."));
        }


        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.getStyleClass().add("button-danger");
        deleteUserButton.setOnAction(e -> {
            User selectedUser = userManagementTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Prevent admin from deleting themselves or the primary admin account (Dinesh Reddy)
                if (selectedUser.getUsername().equals(currentUser.getUsername()) ||
                    selectedUser.getUsername().equals(ADMIN_USERNAME)) {
                    showAlert(Alert.AlertType.ERROR, "Deletion Prohibited", "Cannot delete yourself or the primary admin account.");
                    return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Deletion");
                confirmation.setHeaderText("Delete User: " + selectedUser.getUsername() + "?");
                confirmation.setContentText("Are you sure you want to delete this user? All their orders, bookings, feedback, and ratings will also be deleted.");
                URL cssUrl = getClass().getResource("style.css");
                if (cssUrl != null) {
                    confirmation.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("WARNING: style.css not found for Confirmation Dialog. UI may not be styled correctly. Expected path: application/style.css (relative to classpath)");
                }
                confirmation.getDialogPane().getStyleClass().add("alert-dialog-custom");
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if (DatabaseManager.deleteUser(selectedUser.getUsername())) {
                            allUsers.remove(selectedUser); // Remove from ObservableList
                            showAlert(Alert.AlertType.INFORMATION, "Deletion Success", "User deleted successfully.");
                            loadAllData(); // Reload all data as dependent records might be gone
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete user.");
                        }
                    }
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "No User Selected", "Please select a user to delete.");
            }
        });

        HBox actionButtons = new HBox(10, deleteUserButton);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        contentPane.getChildren().addAll(heading, new Separator(), userManagementTable, actionButtons);
        return contentPane;
    }

    // NEW METHOD: Create Table Availability Tab (Admin Only)
    private Node createTableAvailabilityTab() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getStyleClass().add("content-pane");

        Label header = new Label("Check Table Availability");
        header.getStyleClass().add("heading-medium");

        // Input controls
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(10));

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");
        datePicker.getStyleClass().add("date-picker-custom");

        ComboBox<String> timeComboBox = new ComboBox<>();
        timeComboBox.setPromptText("Select Time");
        populateTimeComboBox(timeComboBox);
        timeComboBox.getStyleClass().add("combo-box-custom");

        ComboBox<Integer> seatsComboBox = new ComboBox<>();
        seatsComboBox.setPromptText("Min Seats");
        seatsComboBox.getItems().addAll(2, 4, 6, 10); // Populate with common seat counts
        seatsComboBox.getStyleClass().add("combo-box-custom");
        
        Spinner<Integer> durationSpinner = new Spinner<>(30, 240, BOOKING_DURATION_MINUTES, 30); // 30 mins to 4 hours, step 30
        durationSpinner.setEditable(true);
        durationSpinner.setPromptText("Duration in minutes");
        durationSpinner.getStyleClass().add("spinner-custom");

        inputGrid.add(new Label("Date:"), 0, 0);
        inputGrid.add(datePicker, 1, 0);
        inputGrid.add(new Label("Time:"), 0, 1);
        inputGrid.add(timeComboBox, 1, 1);
        inputGrid.add(new Label("Min Seats:"), 0, 2);
        inputGrid.add(seatsComboBox, 1, 2);
        inputGrid.add(new Label("Duration (min):"), 0, 3);
        inputGrid.add(durationSpinner, 1, 3);


        Button checkAvailabilityButton = new Button("Check Availability");
        checkAvailabilityButton.getStyleClass().add("button-info");
        
        HBox buttonBox = new HBox(10, checkAvailabilityButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));


        TableView<Map<String, Object>> availableTablesTable = new TableView<>();
        availableTablesTable.getStyleClass().add("data-table");
        availableTablesTable.setPlaceholder(new Label("Enter criteria and click 'Check Availability'."));

        TableColumn<Map<String, Object>, Integer> tableNumCol = new TableColumn<>("Table Number");
        tableNumCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue().get("tableNumber")).asObject());

        TableColumn<Map<String, Object>, String> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get("tableType").toString()));

        TableColumn<Map<String, Object>, Integer> tableSeatsCol = new TableColumn<>("Seats");
        tableSeatsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue().get("seats")).asObject());

        availableTablesTable.getColumns().addAll(tableNumCol, tableTypeCol, tableSeatsCol);


        checkAvailabilityButton.setOnAction(e -> {
            if (datePicker.getValue() == null || timeComboBox.getValue() == null || seatsComboBox.getValue() == null || durationSpinner.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a date, time, minimum seats, and duration.");
                return;
            }

            try {
                LocalDateTime desiredDateTime = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(timeComboBox.getValue()));
                int requiredSeats = seatsComboBox.getValue();
                int duration = durationSpinner.getValue();

                List<Map<String, Object>> tables = DatabaseManager.getAvailableTables(desiredDateTime, duration, requiredSeats);
                availableTablesTable.setItems(FXCollections.observableArrayList(tables));

                if (tables.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "No Tables Found", "No tables available for the selected criteria.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Tables Found", "Found " + tables.size() + " available tables.");
                }

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while checking availability: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(header, new Separator(), inputGrid, buttonBox, new Label("Available Tables:"), availableTablesTable);
        return layout;
    }


    /**
     * Helper method to show a custom styled Alert Dialog.
     * @param alertType The type of alert.
     * @param title The title of the alert.
     * @param message The content message of the alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);

        // Apply custom styling to the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: style.css not found for Alert Dialog. UI may not be styled correctly. Expected path: application/style.css (relative to classpath)");
        }
        dialogPane.getStyleClass().add("alert-dialog-custom");

        // Add an icon based on alert type for better visual feedback
        ImageView icon = null;
        if (alertType == Alert.AlertType.INFORMATION) {
            icon = new ImageView(new Image("https://placehold.co/32x32/8BC34A/FFFFFF?text=i")); // Green info icon
        } else if (alertType == Alert.AlertType.WARNING) {
            icon = new ImageView(new Image("https://placehold.co/32x32/FFC107/FFFFFF?text=!_")); // Yellow warning icon
        } else if (alertType == Alert.AlertType.ERROR) {
            icon = new ImageView(new Image("https://placehold.co/32x32/F44336/FFFFFF?text=X")); // Red error icon
        } else if (alertType == Alert.AlertType.CONFIRMATION) {
            icon = new ImageView(new Image("https://placehold.co/32x32/3F51B5/FFFFFF?text=?")); // Blue question icon
        }
        if (icon != null) {
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            dialogPane.setGraphic(icon);
        }

        alert.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
