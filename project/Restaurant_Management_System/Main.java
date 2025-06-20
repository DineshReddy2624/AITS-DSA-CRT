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
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Main application class for the Restaurant Management System.
 * Handles UI, business logic, and interaction with the DatabaseManager.
 * This version includes user login, registration, and personalized customer profiles.
 */
public class Main extends Application {

    // --- Data & state ---
    private List<MenuItem> menuList = new ArrayList<>();
    private Map<Integer, MenuItem> menuMap = new HashMap<>(); // Maps ID to MenuItem for quick lookup
    private ObservableList<Order> allOrders = FXCollections.observableArrayList(); // Use ObservableList for TableView
    private ObservableList<TableBooking> allBookings = FXCollections.observableArrayList(); // Use ObservableList for TableView
    private ObservableList<Feedback> allFeedback = FXCollections.observableArrayList(); // New: For feedback display
    private ObservableList<DishRating> allDishRatings = FXCollections.observableArrayList(); // New: For dish rating display

    // User session management
    private User loggedInUser; // Stores the currently logged-in user

    // JavaFX Stages and Scenes
    private Stage primaryStage;
    private Scene loginScene, registerScene, adminScene, customerScene;

    // --- Constants ---
    private static final double ADMIN_DISCOUNT_PERCENTAGE = 0.10; // 10% discount for admin orders

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Restaurant Management System");

        // Initialize database and load initial data
        DatabaseManager.initializeDatabase();
        loadInitialData(); // Load menu items, orders, bookings etc.

        // Show the initial login screen
        showLoginScreen();

        primaryStage.show();
    }

    /**
     * Loads initial data from the database into the application's memory.
     * This method is called once at the start of the application.
     */
    private void loadInitialData() {
        menuList.clear();
        menuList.addAll(DatabaseManager.loadMenuItems());
        menuMap.clear();
        for (MenuItem item : menuList) {
            menuMap.put(item.getId(), item);
        }

        allOrders.clear();
        allOrders.addAll(DatabaseManager.loadOrders(menuMap));

        allBookings.clear();
        allBookings.addAll(DatabaseManager.loadTableBookings());

        allFeedback.clear();
        allFeedback.addAll(DatabaseManager.loadFeedback());

        allDishRatings.clear();
        allDishRatings.addAll(DatabaseManager.loadDishRatings());

        System.out.println("Initial data loaded: " + menuList.size() + " menu items, " +
                           allOrders.size() + " orders, " + allBookings.size() + " bookings, " +
                           allFeedback.size() + " feedback entries, " + allDishRatings.size() + " dish ratings.");
    }

    // --- UI Navigation ---

    /**
     * Displays the login screen for users.
     */
    private void showLoginScreen() {
        if (loginScene == null) {
            loginScene = new Scene(createLoginPane(), 800, 600);
            // FIX: Correctly load stylesheet from classpath
            loginScene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
        }
        primaryStage.setScene(loginScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Displays the registration screen for new users.
     */
    private void showRegisterScreen() {
        if (registerScene == null) {
            registerScene = new Scene(createRegisterPane(), 800, 650);
            // FIX: Correctly load stylesheet from classpath
            registerScene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
        }
        primaryStage.setScene(registerScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Displays the admin main menu after successful admin login.
     */
    private void showAdminMenu() {
        if (adminScene == null) {
            adminScene = new Scene(createAdminMainPane(), 1000, 700);
            // FIX: Correctly load stylesheet from classpath
            adminScene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
        }
        primaryStage.setScene(adminScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Displays the customer main menu after successful customer login.
     */
    private void showCustomerMenu() {
        if (customerScene == null) {
            customerScene = new Scene(createCustomerMainPane(), 1000, 700);
            // FIX: Correctly load stylesheet from classpath
            customerScene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
        }
        primaryStage.setScene(customerScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Handles user logout, clearing the session and returning to the login screen.
     */
    private void logoutUser() {
        loggedInUser = null; // Clear the logged-in user session
        showLoginScreen();
        // Clear any user-specific data from observable lists if necessary, or reload on next login
        loadInitialData(); // Re-load data for a clean state or next user
    }

    // --- Login & Registration UI Panes ---

    /**
     * Creates the UI for the login screen.
     */
    private BorderPane createLoginPane() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root"); // Apply general root styling

        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(50));
        loginBox.setMaxWidth(400); // Max width for the login box
        loginBox.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");

        Label title = new Label("Welcome Back!");
        title.getStyleClass().add("title-label"); // Use title-label for main titles
        title.setStyle("-fx-font-size: 28px; -fx-padding: 0 0 20px 0;"); // Override padding for this specific title

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("button");

        Button registerBtn = new Button("Don't have an account? Register");
        registerBtn.getStyleClass().add("secondary-button");
        registerBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 15px; -fx-background-color: transparent; -fx-text-fill: #007bff; -fx-underline: true; -fx-effect: none;");
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 15px; -fx-background-color: transparent; -fx-text-fill: #0056b3; -fx-underline: true; -fx-effect: none;"));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 15px; -fx-background-color: transparent; -fx-text-fill: #007bff; -fx-underline: true; -fx-effect: none;"));


        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Username and password cannot be empty.");
                return;
            }

            User user = DatabaseManager.authenticateUser(username, password);
            if (user != null) {
                loggedInUser = user;
                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Login successful! Welcome, " + loggedInUser.getFullName() + "!");

                // Simulate admin check (e.g., username "admin")
                if ("admin".equalsIgnoreCase(loggedInUser.getUsername())) { // Assuming 'admin' is the admin username
                    showAdminMenu();
                } else {
                    showCustomerMenu();
                }
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Invalid username or password.");
            }
        });

        registerBtn.setOnAction(e -> showRegisterScreen());

        loginBox.getChildren().addAll(title, usernameField, passwordField, loginBtn, registerBtn, messageLabel);
        root.setCenter(loginBox);

        return root;
    }

    /**
     * Creates the UI for the registration screen.
     */
    private BorderPane createRegisterPane() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        VBox registerBox = new VBox(15);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(50));
        registerBox.setMaxWidth(450);
        registerBox.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");

        Label title = new Label("Register New Account");
        title.getStyleClass().add("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-padding: 0 0 20px 0;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (unique)");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.getStyleClass().add("password-field");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.getStyleClass().add("text-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Email (optional)");
        emailField.getStyleClass().add("text-field");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number (optional)");
        phoneField.getStyleClass().add("text-field");

        Button registerBtn = new Button("Register Account");
        registerBtn.getStyleClass().add("button");

        Button backToLoginBtn = new Button("Back to Login");
        backToLoginBtn.getStyleClass().add("secondary-button");
        backToLoginBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 15px; -fx-background-color: transparent; -fx-text-fill: #007bff; -fx-underline: true; -fx-effect: none;");
        backToLoginBtn.setOnMouseEntered(e -> backToLoginBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 15px; -fx-background-color: transparent; -fx-text-fill: #0056b3; -fx-underline: true; -fx-effect: none;"));
        backToLoginBtn.setOnMouseExited(e -> backToLoginBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 15px; -fx-background-color: transparent; -fx-text-fill: #007bff; -fx-underline: true; -fx-effect: none;"));


        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Username, Password, and Full Name are required.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Passwords do not match.");
                return;
            }

            String hashedPassword = PasswordUtil.hashPassword(password);
            if (hashedPassword == null) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Error hashing password. Please try again.");
                return;
            }

            // Attempt to register the user with full details
            if (DatabaseManager.registerUser(username, hashedPassword, fullName, email, phone)) {
                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Registration successful! You can now log in.");
                // Clear fields
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                fullNameField.clear();
                emailField.clear();
                phoneField.clear();
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Registration failed. Username might already exist or a database error occurred.");
            }
        });

        backToLoginBtn.setOnAction(e -> showLoginScreen());

        registerBox.getChildren().addAll(title, usernameField, passwordField, confirmPasswordField,
                                         fullNameField, emailField, phoneField,
                                         registerBtn, backToLoginBtn, messageLabel);
        root.setCenter(registerBox);

        return root;
    }


    // --- Admin Main Menu ---

    /**
     * Creates the main pane for the admin interface.
     */
    private BorderPane createAdminMainPane() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Top section with title and logout button
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #007bff, #0056b3); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label welcomeLabel = new Label("Admin Dashboard - Welcome, " + loggedInUser.getFullName() + "!");
        welcomeLabel.getStyleClass().add("title-label");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-padding: 0;"); // Override padding

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("secondary-button");
        logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-font-size: 14px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 20px;"); // Red logout button
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #c82333; -fx-font-size: 14px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 20px;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-font-size: 14px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 20px;"));
        logoutBtn.setOnAction(e -> logoutUser());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Push logout button to the right

        topBar.getChildren().addAll(welcomeLabel, spacer, logoutBtn);
        root.setTop(topBar);

        // Tab Pane for different admin functionalities
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Prevent tabs from being closed

        // Tabs
        Tab menuTab = new Tab("Menu Management", createMenuManagementPane());
        menuTab.getStyleClass().add("tab");
        Tab ordersTab = new Tab("Order Management", createOrderManagementPane());
        ordersTab.getStyleClass().add("tab");
        Tab bookingsTab = new Tab("Table Bookings", createBookingManagementPane());
        bookingsTab.getStyleClass().add("tab");
        Tab feedbackTab = new Tab("Customer Feedback", createFeedbackManagementPane());
        feedbackTab.getStyleClass().add("tab");
        Tab dishRatingsTab = new Tab("Dish Ratings", createDishRatingManagementPane());
        dishRatingsTab.getStyleClass().add("tab");
        Tab dataExportTab = new Tab("Data Export", createDataExportPane());
        dataExportTab.getStyleClass().add("tab");


        tabPane.getTabs().addAll(menuTab, ordersTab, bookingsTab, feedbackTab, dishRatingsTab, dataExportTab);
        root.setCenter(tabPane);

        return root;
    }

    // --- Customer Main Menu ---

    /**
     * Creates the main pane for the customer interface.
     */
    private BorderPane createCustomerMainPane() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Top section with title and logout button
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #007bff, #0056b3); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label welcomeLabel = new Label("Customer Portal - Welcome, " + loggedInUser.getFullName() + "!");
        welcomeLabel.getStyleClass().add("title-label");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-padding: 0;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("secondary-button");
        logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-font-size: 14px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 20px;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #c82333; -fx-font-size: 14px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 20px;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-font-size: 14px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 20px;"));
        logoutBtn.setOnAction(e -> logoutUser());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(welcomeLabel, spacer, logoutBtn);
        root.setTop(topBar);

        // Tab Pane for different customer functionalities
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tabs
        Tab orderFoodTab = new Tab("Order Food", createOrderFoodPaneForCustomer());
        orderFoodTab.getStyleClass().add("tab");
        Tab bookTableTab = new Tab("Book Table", createTableBookingPaneForCustomer());
        bookTableTab.getStyleClass().add("tab");
        Tab giveFeedbackTab = new Tab("Give Feedback", createFeedbackPaneForCustomer());
        giveFeedbackTab.getStyleClass().add("tab");
        Tab rateDishTab = new Tab("Rate a Dish", createDishRatingPaneForCustomer());
        rateDishTab.getStyleClass().add("tab");
        Tab myProfileTab = new Tab("My Profile", createCustomerProfilePane());
        myProfileTab.getStyleClass().add("tab");
        Tab myOrdersTab = new Tab("My Orders", createMyOrdersPaneForCustomer());
        myOrdersTab.getStyleClass().add("tab");
        Tab myBookingsTab = new Tab("My Bookings", createMyBookingsPaneForCustomer());
        myBookingsTab.getStyleClass().add("tab");


        tabPane.getTabs().addAll(orderFoodTab, bookTableTab, giveFeedbackTab, rateDishTab, myProfileTab, myOrdersTab, myBookingsTab);
        root.setCenter(tabPane);

        return root;
    }


    // --- Admin Functionality Panes ---

    /**
     * Creates the pane for Menu Management (Admin).
     */
    private VBox createMenuManagementPane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Menu Item Management");
        heading.getStyleClass().add("prompt-label");

        // Form for adding/editing menu items
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 20, 0));
        formGrid.setAlignment(Pos.CENTER);

        TextField idField = new TextField();
        idField.setPromptText("ID (e.g., 101)");
        idField.getStyleClass().add("text-field");
        TextField nameField = new TextField();
        nameField.setPromptText("Name (e.g., Pizza)");
        nameField.getStyleClass().add("text-field");
        TextField priceField = new TextField();
        priceField.setPromptText("Price (e.g., 12.99)");
        priceField.getStyleClass().add("text-field");

        formGrid.add(new Label("Item ID:"), 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(new Label("Item Name:"), 0, 1);
        formGrid.add(nameField, 1, 1);
        formGrid.add(new Label("Price:"), 0, 2);
        formGrid.add(priceField, 1, 2);

        Button addUpdateBtn = new Button("Add/Update Item");
        addUpdateBtn.getStyleClass().add("button");
        Button deleteBtn = new Button("Delete Selected Item");
        deleteBtn.getStyleClass().add("secondary-button");

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");


        // Table View for Menu Items
        TableView<MenuItem> menuTable = new TableView<>();
        menuTable.getStyleClass().add("table-view");
        menuTable.setPrefHeight(300); // Set a preferred height for the table

        TableColumn<MenuItem, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);
        TableColumn<MenuItem, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        TableColumn<MenuItem, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(100);

        menuTable.getColumns().addAll(idColumn, nameColumn, priceColumn);
        menuTable.setItems(FXCollections.observableArrayList(menuList)); // Bind to a new ObservableList for updates

        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                idField.setText(String.valueOf(newSelection.getId()));
                nameField.setText(newSelection.getName());
                priceField.setText(String.format("%.2f", newSelection.getPrice()));
            } else {
                idField.clear();
                nameField.clear();
                priceField.clear();
            }
        });

        addUpdateBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());

                if (name.isEmpty() || price <= 0) {
                    messageLabel.getStyleClass().setAll("message-label", "warning");
                    messageLabel.setText("Name cannot be empty and price must be positive.");
                    return;
                }

                MenuItem newItem = new MenuItem(id, name, price);
                DatabaseManager.saveMenuItem(newItem);
                loadInitialData(); // Reload all data to refresh tables
                menuTable.setItems(FXCollections.observableArrayList(menuList)); // Update table view

                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Menu item saved/updated successfully!");
                idField.clear();
                nameField.clear();
                priceField.clear();
                menuTable.getSelectionModel().clearSelection();
            } catch (NumberFormatException ex) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Invalid ID or Price format.");
            }
        });

        deleteBtn.setOnAction(e -> {
            MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                DatabaseManager.deleteMenuItem(selectedItem.getId());
                loadInitialData(); // Reload all data to refresh tables
                menuTable.setItems(FXCollections.observableArrayList(menuList)); // Update table view

                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Menu item deleted successfully!");
            } else {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please select an item to delete.");
            }
        });


        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(addUpdateBtn, deleteBtn);

        contentPane.getChildren().addAll(heading, formGrid, buttonBox, new Separator(), menuTable, messageLabel);
        return contentPane;
    }

    /**
     * Creates the pane for Order Management (Admin).
     */
    private VBox createOrderManagementPane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Order Management");
        heading.getStyleClass().add("prompt-label");

        // Filter and Search
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10));

        ComboBox<OrderStatus> orderStatusFilter = new ComboBox<>();
        orderStatusFilter.setPromptText("Filter by Status");
        orderStatusFilter.getStyleClass().add("combo-box");
        orderStatusFilter.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        ComboBox<PaymentStatus> paymentStatusFilter = new ComboBox<>();
        paymentStatusFilter.setPromptText("Filter by Payment Status");
        paymentStatusFilter.getStyleClass().add("combo-box");
        paymentStatusFilter.setItems(FXCollections.observableArrayList(PaymentStatus.values()));

        ComboBox<PaymentMethod> paymentMethodFilter = new ComboBox<>();
        paymentMethodFilter.setPromptText("Filter by Payment Method");
        paymentMethodFilter.getStyleClass().add("combo-box");
        paymentMethodFilter.setItems(FXCollections.observableArrayList(PaymentMethod.values()));


        TextField searchField = new TextField();
        searchField.setPromptText("Search by Order ID or Customer Username");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS); // Allow search field to grow

        Button applyFiltersBtn = new Button("Apply Filters");
        applyFiltersBtn.getStyleClass().add("small-dialog-button");
        Button clearFiltersBtn = new Button("Clear Filters");
        clearFiltersBtn.getStyleClass().add("small-dialog-button");

        filterBox.getChildren().addAll(orderStatusFilter, paymentStatusFilter, paymentMethodFilter, searchField, applyFiltersBtn, clearFiltersBtn);

        // Table View for Orders
        TableView<Order> orderTable = new TableView<>();
        orderTable.getStyleClass().add("table-view");
        orderTable.setPrefHeight(400);

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, String> customerUsernameCol = new TableColumn<>("Customer");
        customerUsernameCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerUsernameCol.setPrefWidth(120);

        TableColumn<Order, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subtotalCol.setPrefWidth(90);

        TableColumn<Order, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountApplied"));
        discountCol.setPrefWidth(80);

        TableColumn<Order, Double> netAmountCol = new TableColumn<>("Net Amount");
        netAmountCol.setCellValueFactory(new PropertyValueFactory<>("finalPriceBeforeGST"));
        netAmountCol.setPrefWidth(90);

        TableColumn<Order, Double> gstAmountCol = new TableColumn<>("GST");
        gstAmountCol.setCellValueFactory(new PropertyValueFactory<>("GSTAmount"));
        gstAmountCol.setPrefWidth(70);

        TableColumn<Order, Double> finalAmountCol = new TableColumn<>("Final Amount");
        finalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalWithGST"));
        finalAmountCol.setPrefWidth(100);


        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);

        TableColumn<Order, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);


        orderTable.getColumns().addAll(orderIdCol, customerUsernameCol, subtotalCol, discountCol, netAmountCol, gstAmountCol, finalAmountCol, statusCol, paymentStatusCol, paymentMethodCol);
        orderTable.setItems(allOrders); // Bind to allOrders for admin view

        // Update actions
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);

        ComboBox<OrderStatus> updateOrderStatusCombo = new ComboBox<>();
        updateOrderStatusCombo.setPromptText("Update Order Status");
        updateOrderStatusCombo.getStyleClass().add("combo-box");
        updateOrderStatusCombo.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        ComboBox<PaymentStatus> updatePaymentStatusCombo = new ComboBox<>();
        updatePaymentStatusCombo.setPromptText("Update Payment Status");
        updatePaymentStatusCombo.getStyleClass().add("combo-box");
        updatePaymentStatusCombo.setItems(FXCollections.observableArrayList(PaymentStatus.values()));

        ComboBox<PaymentMethod> updatePaymentMethodCombo = new ComboBox<>();
        updatePaymentMethodCombo.setPromptText("Update Payment Method");
        updatePaymentMethodCombo.getStyleClass().add("combo-box");
        updatePaymentMethodCombo.setItems(FXCollections.observableArrayList(PaymentMethod.values()));


        Button updateBtn = new Button("Update Selected Order");
        updateBtn.getStyleClass().add("button");
        Button viewDetailsBtn = new Button("View Order Details");
        viewDetailsBtn.getStyleClass().add("small-dialog-button");

        actionBox.getChildren().addAll(updateOrderStatusCombo, updatePaymentStatusCombo, updatePaymentMethodCombo, updateBtn, viewDetailsBtn);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        // Filter logic
        Runnable applyOrderFilters = () -> {
            ObservableList<Order> filteredOrders = allOrders.stream()
                .filter(order -> {
                    boolean matchesStatus = true;
                    if (orderStatusFilter.getValue() != null) {
                        matchesStatus = order.getStatus() == orderStatusFilter.getValue();
                    }
                    boolean matchesPaymentStatus = true;
                    if (paymentStatusFilter.getValue() != null) {
                        matchesPaymentStatus = order.getPaymentStatus() == paymentStatusFilter.getValue();
                    }
                    boolean matchesPaymentMethod = true;
                    if (paymentMethodFilter.getValue() != null) {
                        matchesPaymentMethod = order.getPaymentMethod() == paymentMethodFilter.getValue();
                    }
                    boolean matchesSearch = true;
                    String searchText = searchField.getText().toLowerCase();
                    if (!searchText.isEmpty()) {
                        matchesSearch = String.valueOf(order.getOrderId()).contains(searchText) ||
                                        (order.getCustomerUsername() != null && order.getCustomerUsername().toLowerCase().contains(searchText));
                    }
                    return matchesStatus && matchesPaymentStatus && matchesPaymentMethod && matchesSearch;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            orderTable.setItems(filteredOrders);
        };

        applyFiltersBtn.setOnAction(e -> applyOrderFilters.run());
        clearFiltersBtn.setOnAction(e -> {
            orderStatusFilter.getSelectionModel().clearSelection();
            paymentStatusFilter.getSelectionModel().clearSelection();
            paymentMethodFilter.getSelectionModel().clearSelection();
            searchField.clear();
            orderTable.setItems(allOrders); // Reset to all orders
        });

        // Update logic
        updateBtn.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                boolean updated = false;
                if (updateOrderStatusCombo.getValue() != null && selectedOrder.getStatus() != updateOrderStatusCombo.getValue()) {
                    DatabaseManager.updateOrderStatus(selectedOrder.getOrderId(), updateOrderStatusCombo.getValue());
                    selectedOrder.setStatus(updateOrderStatusCombo.getValue()); // Update local object
                    updated = true;
                }
                if (updatePaymentStatusCombo.getValue() != null && selectedOrder.getPaymentStatus() != updatePaymentStatusCombo.getValue()) {
                    DatabaseManager.updateOrderPaymentStatus(selectedOrder.getOrderId(), updatePaymentStatusCombo.getValue());
                    selectedOrder.setPaymentStatus(updatePaymentStatusCombo.getValue()); // Update local object
                    updated = true;
                }
                if (updatePaymentMethodCombo.getValue() != null && selectedOrder.getPaymentMethod() != updatePaymentMethodCombo.getValue()) {
                    DatabaseManager.updateOrderPaymentMethod(selectedOrder.getOrderId(), updatePaymentMethodCombo.getValue());
                    selectedOrder.setPaymentMethod(updatePaymentMethodCombo.getValue()); // Update local object
                    updated = true;
                }

                if (updated) {
                    orderTable.refresh(); // Refresh the table to show updated status
                    messageLabel.getStyleClass().setAll("message-label", "success");
                    messageLabel.setText("Order ID " + selectedOrder.getOrderId() + " updated successfully!");
                } else {
                    messageLabel.getStyleClass().setAll("message-label", "warning");
                    messageLabel.setText("No changes to apply or no selection.");
                }
                updateOrderStatusCombo.getSelectionModel().clearSelection();
                updatePaymentStatusCombo.getSelectionModel().clearSelection();
                updatePaymentMethodCombo.getSelectionModel().clearSelection();
            } else {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please select an order to update.");
            }
        });

        viewDetailsBtn.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                showOrderDetailsDialog(selectedOrder);
            } else {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please select an order to view details.");
            }
        });


        contentPane.getChildren().addAll(heading, filterBox, new Separator(), orderTable, actionBox, messageLabel);
        return contentPane;
    }

    /**
     * Shows a dialog with detailed information about a selected order.
     * @param order The Order object to display.
     */
    private void showOrderDetailsDialog(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details");
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm()); // FIX: Load CSS for dialog
        dialogPane.getStyleClass().add("alert"); // Apply custom alert styling to dialog pane

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.TOP_LEFT);

        Label orderDetailsLabel = new Label(order.toDetailedString());
        orderDetailsLabel.getStyleClass().add("label");
        orderDetailsLabel.setWrapText(true); // Allow text wrapping

        content.getChildren().add(orderDetailsLabel);
        dialogPane.setContent(content);

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButtonType);

        Button closeButton = (Button) dialogPane.lookupButton(closeButtonType);
        closeButton.getStyleClass().add("small-dialog-button"); // Apply custom button style

        dialog.showAndWait();
    }


    /**
     * Creates the pane for Table Booking Management (Admin).
     */
    private VBox createBookingManagementPane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Table Booking Management");
        heading.getStyleClass().add("prompt-label");

        // Filter and Search for bookings
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10));

        TextField bookingSearchField = new TextField();
        bookingSearchField.setPromptText("Search by Customer ID or Name");
        bookingSearchField.getStyleClass().add("text-field");
        HBox.setHgrow(bookingSearchField, Priority.ALWAYS);

        ComboBox<PaymentStatus> bookingPaymentStatusFilter = new ComboBox<>();
        bookingPaymentStatusFilter.setPromptText("Filter by Payment Status");
        bookingPaymentStatusFilter.getStyleClass().add("combo-box");
        bookingPaymentStatusFilter.setItems(FXCollections.observableArrayList(PaymentStatus.values()));

        Button applyBookingFiltersBtn = new Button("Apply Filters");
        applyBookingFiltersBtn.getStyleClass().add("small-dialog-button");
        Button clearBookingFiltersBtn = new Button("Clear Filters");
        clearBookingFiltersBtn.getStyleClass().add("small-dialog-button");

        filterBox.getChildren().addAll(bookingSearchField, bookingPaymentStatusFilter, applyBookingFiltersBtn, clearBookingFiltersBtn);


        // Table View for Bookings
        TableView<TableBooking> bookingTable = new TableView<>();
        bookingTable.getStyleClass().add("table-view");
        bookingTable.setPrefHeight(400);

        TableColumn<TableBooking, String> customerIdCol = new TableColumn<>("Customer ID");
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerIdCol.setPrefWidth(120);

        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setPrefWidth(150);

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(100);

        TableColumn<TableBooking, TableType> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));
        tableTypeCol.setPrefWidth(100);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(80);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(70);

        TableColumn<TableBooking, Double> bookingFeeCol = new TableColumn<>("Booking Fee");
        bookingFeeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        bookingFeeCol.setPrefWidth(100);

        TableColumn<TableBooking, PaymentStatus> bookingPaymentStatusCol = new TableColumn<>("Payment Status");
        bookingPaymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        bookingPaymentStatusCol.setPrefWidth(120);

        bookingTable.getColumns().addAll(customerIdCol, customerNameCol, phoneCol, tableTypeCol, tableNumberCol, seatsCol, bookingFeeCol, bookingPaymentStatusCol);
        bookingTable.setItems(allBookings);

        // Update actions
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);

        ComboBox<PaymentStatus> updateBookingPaymentStatusCombo = new ComboBox<>();
        updateBookingPaymentStatusCombo.setPromptText("Update Payment Status");
        updateBookingPaymentStatusCombo.getStyleClass().add("combo-box");
        updateBookingPaymentStatusCombo.setItems(FXCollections.observableArrayList(PaymentStatus.values()));

        Button updateBookingBtn = new Button("Update Selected Booking");
        updateBookingBtn.getStyleClass().add("button");

        actionBox.getChildren().addAll(updateBookingPaymentStatusCombo, updateBookingBtn);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        // Filter logic for bookings
        Runnable applyBookingFilters = () -> {
            ObservableList<TableBooking> filteredBookings = allBookings.stream()
                .filter(booking -> {
                    boolean matchesSearch = true;
                    String searchText = bookingSearchField.getText().toLowerCase();
                    if (!searchText.isEmpty()) {
                        matchesSearch = booking.getCustomerId().toLowerCase().contains(searchText) ||
                                        booking.getCustomerName().toLowerCase().contains(searchText);
                    }
                    boolean matchesPaymentStatus = true;
                    if (bookingPaymentStatusFilter.getValue() != null) {
                        matchesPaymentStatus = booking.getPaymentStatus() == bookingPaymentStatusFilter.getValue();
                    }
                    return matchesSearch && matchesPaymentStatus;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            bookingTable.setItems(filteredBookings);
        };

        applyBookingFiltersBtn.setOnAction(e -> applyBookingFilters.run());
        clearBookingFiltersBtn.setOnAction(e -> {
            bookingSearchField.clear();
            bookingPaymentStatusFilter.getSelectionModel().clearSelection();
            bookingTable.setItems(allBookings); // Reset to all bookings
        });


        // Update booking logic
        updateBookingBtn.setOnAction(e -> {
            TableBooking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
            if (selectedBooking != null) {
                if (updateBookingPaymentStatusCombo.getValue() != null && selectedBooking.getPaymentStatus() != updateBookingPaymentStatusCombo.getValue()) {
                    DatabaseManager.updateTableBookingPaymentStatus(selectedBooking.getCustomerId(), updateBookingPaymentStatusCombo.getValue());
                    selectedBooking.paymentStatus = updateBookingPaymentStatusCombo.getValue(); // Update local object
                    bookingTable.refresh(); // Refresh the table
                    messageLabel.getStyleClass().setAll("message-label", "success");
                    messageLabel.setText("Booking for Customer ID " + selectedBooking.getCustomerId() + " updated successfully!");
                } else {
                    messageLabel.getStyleClass().setAll("message-label", "warning");
                    messageLabel.setText("No changes to apply or no payment status selected.");
                }
                updateBookingPaymentStatusCombo.getSelectionModel().clearSelection();
            } else {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please select a booking to update.");
            }
        });

        contentPane.getChildren().addAll(heading, filterBox, new Separator(), bookingTable, actionBox, messageLabel);
        return contentPane;
    }

    /**
     * Creates the pane for Customer Feedback Management (Admin).
     */
    private VBox createFeedbackManagementPane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Customer Feedback");
        heading.getStyleClass().add("prompt-label");

        // Table View for Feedback
        TableView<Feedback> feedbackTable = new TableView<>();
        feedbackTable.getStyleClass().add("table-view");
        feedbackTable.setPrefHeight(400);

        TableColumn<Feedback, Integer> feedbackIdCol = new TableColumn<>("ID");
        feedbackIdCol.setCellValueFactory(new PropertyValueFactory<>("feedbackId"));
        feedbackIdCol.setPrefWidth(50);

        TableColumn<Feedback, String> feedbackUserCol = new TableColumn<>("Customer Username");
        feedbackUserCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        feedbackUserCol.setPrefWidth(150);

        TableColumn<Feedback, Integer> feedbackRatingCol = new TableColumn<>("Rating");
        feedbackRatingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        feedbackRatingCol.setPrefWidth(70);

        TableColumn<Feedback, String> feedbackCommentsCol = new TableColumn<>("Comments");
        feedbackCommentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        feedbackCommentsCol.setPrefWidth(300);

        TableColumn<Feedback, LocalDateTime> feedbackDateCol = new TableColumn<>("Date");
        feedbackDateCol.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));
        feedbackDateCol.setPrefWidth(150);

        feedbackTable.getColumns().addAll(feedbackIdCol, feedbackUserCol, feedbackRatingCol, feedbackCommentsCol, feedbackDateCol);
        feedbackTable.setItems(allFeedback);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        // Filter/Search (Optional, can be added later if complexity increases)

        contentPane.getChildren().addAll(heading, new Separator(), feedbackTable, messageLabel);
        return contentPane;
    }

    /**
     * Creates the pane for Dish Rating Management (Admin).
     */
    private VBox createDishRatingManagementPane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Dish Ratings");
        heading.getStyleClass().add("prompt-label");

        // Table View for Dish Ratings
        TableView<DishRating> dishRatingTable = new TableView<>();
        dishRatingTable.getStyleClass().add("table-view");
        dishRatingTable.setPrefHeight(400);

        TableColumn<DishRating, Integer> ratingIdCol = new TableColumn<>("ID");
        ratingIdCol.setCellValueFactory(new PropertyValueFactory<>("ratingId"));
        ratingIdCol.setPrefWidth(50);

        TableColumn<DishRating, Integer> menuItemIdRatingCol = new TableColumn<>("Menu Item ID");
        menuItemIdRatingCol.setCellValueFactory(new PropertyValueFactory<>("menuItemId"));
        menuItemIdRatingCol.setPrefWidth(100);

        TableColumn<DishRating, String> menuItemNameRatingCol = new TableColumn<>("Dish Name");
        menuItemNameRatingCol.setCellValueFactory(cellData -> {
            int itemId = cellData.getValue().getMenuItemId();
            MenuItem item = menuMap.get(itemId);
            return new javafx.beans.property.SimpleStringProperty(item != null ? item.getName() : "Unknown");
        });
        menuItemNameRatingCol.setPrefWidth(150);


        TableColumn<DishRating, String> customerUserRatingCol = new TableColumn<>("Customer Username");
        customerUserRatingCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerUserRatingCol.setPrefWidth(150);

        TableColumn<DishRating, Integer> ratingValueCol = new TableColumn<>("Rating (1-5)");
        ratingValueCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingValueCol.setPrefWidth(100);

        TableColumn<DishRating, LocalDateTime> ratingDateCol = new TableColumn<>("Date");
        ratingDateCol.setCellValueFactory(new PropertyValueFactory<>("ratingDate"));
        ratingDateCol.setPrefWidth(150);


        dishRatingTable.getColumns().addAll(ratingIdCol, menuItemIdRatingCol, menuItemNameRatingCol, customerUserRatingCol, ratingValueCol, ratingDateCol);
        dishRatingTable.setItems(allDishRatings);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        contentPane.getChildren().addAll(heading, new Separator(), dishRatingTable, messageLabel);
        return contentPane;
    }


    /**
     * Creates the pane for Data Export (Admin).
     */
    private VBox createDataExportPane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Data Export");
        heading.getStyleClass().add("prompt-label");

        Button exportOrdersBtn = new Button("Export Orders to CSV");
        exportOrdersBtn.getStyleClass().add("button");
        Button exportBookingsBtn = new Button("Export Bookings to CSV");
        exportBookingsBtn.getStyleClass().add("button");
        Button exportFeedbackBtn = new Button("Export Feedback to CSV");
        exportFeedbackBtn.getStyleClass().add("button");
        Button exportDishRatingsBtn = new Button("Export Dish Ratings to CSV");
        exportDishRatingsBtn.getStyleClass().add("button");
        Button exportUsersBtn = new Button("Export Users to CSV"); // New export option for users
        exportUsersBtn.getStyleClass().add("button");

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        exportOrdersBtn.setOnAction(e -> exportDataToCsv(allOrders, "orders.csv", messageLabel, Order.class));
        exportBookingsBtn.setOnAction(e -> exportDataToCsv(allBookings, "bookings.csv", messageLabel, TableBooking.class));
        exportFeedbackBtn.setOnAction(e -> exportDataToCsv(allFeedback, "feedback.csv", messageLabel, Feedback.class));
        exportDishRatingsBtn.setOnAction(e -> exportDataToCsv(allDishRatings, "dish_ratings.csv", messageLabel, DishRating.class));
        exportUsersBtn.setOnAction(e -> exportDataToCsv(DatabaseManager.loadAllUsers(), "users.csv", messageLabel, User.class)); // Load all users for export


        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(exportOrdersBtn, exportBookingsBtn, exportFeedbackBtn, exportDishRatingsBtn, exportUsersBtn);


        contentPane.getChildren().addAll(heading, new Separator(), buttonBox, messageLabel);
        return contentPane;
    }

    /**
     * Generic method to export data from an ObservableList to a CSV file.
     * @param data The ObservableList containing the data objects.
     * @param fileName The default file name for the CSV.
     * @param messageLabel The label to display status messages.
     * @param type The Class type of the objects in the list (used for reflection for headers).
     * @param <T> The type of the objects.
     */
    private <T> void exportDataToCsv(List<T> data, String fileName, Label messageLabel, Class<T> type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save " + fileName + " File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(fileName);

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                if (data.isEmpty()) {
                    messageLabel.getStyleClass().setAll("message-label", "warning");
                    messageLabel.setText("No data to export for " + fileName + ".");
                    return;
                }

                // Write CSV header based on properties of the object type
                writer.append(getCSVHeader(type)).append("\n");

                // Write data rows
                for (T item : data) {
                    writer.append(toCSVRow(item)).append("\n");
                }

                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText(fileName + " exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Error exporting " + fileName + ": " + e.getMessage());
                System.err.println("Error exporting CSV: " + e.getMessage());
            }
        } else {
            messageLabel.getStyleClass().setAll("message-label", "warning");
            messageLabel.setText("Export cancelled.");
        }
    }

    /**
     * Generates a CSV header string for a given class type using reflection.
     * This is a simplified approach; for complex objects, more detailed property mapping would be needed.
     * @param type The class type.
     * @return A comma-separated string of property names.
     */
    private <T> String getCSVHeader(Class<T> type) {
        // This is a simplified approach. For production, consider using a library
        // or a more robust custom mapping to handle property names, nested objects, etc.
        if (type == Order.class) {
            return "Order ID,Customer Username,Status,Payment Status,Payment Method,Discount Applied,Subtotal,Net Amount,GST Amount,Final Amount With GST";
        } else if (type == TableBooking.class) {
            return "Customer ID,Customer Name,Phone,Table Type,Table Number,Seats,Booking Fee,Payment Status";
        } else if (type == Feedback.class) {
            return "Feedback ID,Customer Username,Rating,Comments,Feedback Date";
        } else if (type == DishRating.class) {
            return "Rating ID,Menu Item ID,Customer Username,Rating Value,Rating Date";
        } else if (type == User.class) {
            return "Username,Full Name,Email,Phone Number"; // Exclude password hash and created_at if not directly accessible via getter for export
        }
        // Fallback for unhandled types - might not be useful
        return Arrays.stream(type.getDeclaredFields())
                     .map(field -> field.getName())
                     .collect(Collectors.joining(","));
    }

    /**
     * Converts an object to a CSV row string.
     * This is a simplified approach; for complex objects, more detailed property mapping would be needed.
     * @param item The object to convert.
     * @return A comma-separated string of property values.
     */
    private <T> String toCSVRow(T item) {
        if (item instanceof Order) {
            Order order = (Order) item;
            return String.format("%d,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                    order.getOrderId(),
                    escapeCsv(order.getCustomerUsername()),
                    order.getStatus().getDisplayValue(),
                    order.getPaymentStatus().getDisplayValue(),
                    order.getPaymentMethod().getDisplayValue(),
                    order.getDiscountApplied(),
                    order.getSubtotal(),
                    order.getFinalPriceBeforeGST(),
                    order.getGSTAmount(),
                    order.getTotalWithGST());
        } else if (item instanceof TableBooking) {
            TableBooking booking = (TableBooking) item;
            return String.format("%s,%s,%s,%s,%d,%d,%.2f,%s",
                    escapeCsv(booking.getCustomerId()),
                    escapeCsv(booking.getCustomerName()), // Escape commas in name
                    escapeCsv(booking.getPhone()),
                    booking.getTableType().getDisplayValue(),
                    booking.getTableNumber(),
                    booking.getSeats(),
                    booking.getBookingFee(),
                    booking.getPaymentStatus().getDisplayValue());
        } else if (item instanceof Feedback) {
            Feedback feedback = (Feedback) item;
            return String.format("%d,%s,%d,\"%s\",%s", // Wrap comments in quotes
                    feedback.getFeedbackId(),
                    escapeCsv(feedback.getCustomerUsername()),
                    feedback.getRating(),
                    escapeCsv(feedback.getComments()),
                    feedback.getFeedbackDate().toLocalDate());
        } else if (item instanceof DishRating) {
            DishRating rating = (DishRating) item;
            return String.format("%d,%d,%s,%d,%s",
                    rating.getRatingId(),
                    rating.getMenuItemId(),
                    escapeCsv(rating.getCustomerUsername()),
                    rating.getRating(),
                    rating.getRatingDate().toLocalDate());
        } else if (item instanceof User) {
            User user = (User) item;
            // Assuming we don't expose password hash in CSV for security
            return String.format("%s,%s,%s,%s",
                    escapeCsv(user.getUsername()),
                    escapeCsv(user.getFullName()),
                    escapeCsv(user.getEmail()),
                    escapeCsv(user.getPhoneNumber()));
        }
        // Fallback
        return escapeCsv(item.toString()); // Not ideal, but a fallback
    }

    // Helper to escape commas and quotes for CSV
    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\""); // Escape double quotes
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\""; // Enclose in quotes if contains comma, quote, or newline
        }
        return escaped;
    }


    // --- Customer Functionality Panes ---

    /**
     * Creates the pane for customers to order food.
     * This pane uses the loggedInUser's username for order placement.
     */
    private VBox createOrderFoodPaneForCustomer() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Place a New Order");
        heading.getStyleClass().add("prompt-label");

        // Display available menu items
        ListView<MenuItem> menuListView = new ListView<>();
        menuListView.getStyleClass().add("list-view");
        menuListView.setItems(FXCollections.observableArrayList(menuList)); // Show all menu items
        menuListView.setPrefHeight(200);

        Label selectedItemsLabel = new Label("Your Order:");
        selectedItemsLabel.getStyleClass().add("label");

        TextArea orderSummaryArea = new TextArea();
        orderSummaryArea.getStyleClass().add("text-area");
        orderSummaryArea.setEditable(false);
        orderSummaryArea.setPromptText("Add items to see your order summary here...");
        orderSummaryArea.setPrefHeight(150);

        ObservableList<MenuItem> currentOrderItems = FXCollections.observableArrayList();

        Button addToOrderBtn = new Button("Add to Order");
        addToOrderBtn.getStyleClass().add("button");
        addToOrderBtn.setOnAction(e -> {
            MenuItem selectedItem = menuListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                currentOrderItems.add(selectedItem);
                updateOrderSummary(orderSummaryArea, currentOrderItems);
            }
        });

        Button removeFromOrderBtn = new Button("Remove from Order");
        removeFromOrderBtn.getStyleClass().add("secondary-button");
        removeFromOrderBtn.setOnAction(e -> {
            // Remove the last added instance of the selected item if multiple exist
            MenuItem selectedItem = menuListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                boolean removed = currentOrderItems.remove(selectedItem); // Removes first occurrence
                if (removed) {
                    updateOrderSummary(orderSummaryArea, currentOrderItems);
                } else {
                    // Item not in current order, or already removed all instances
                }
            }
        });


        HBox orderItemActions = new HBox(10);
        orderItemActions.setAlignment(Pos.CENTER);
        orderItemActions.getChildren().addAll(addToOrderBtn, removeFromOrderBtn);

        // Payment Method selection
        ComboBox<PaymentMethod> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.setPromptText("Select Payment Method");
        paymentMethodCombo.getStyleClass().add("combo-box");
        paymentMethodCombo.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodCombo.getSelectionModel().select(PaymentMethod.CASH); // Default to Cash

        Button placeOrderBtn = new Button("Place Order");
        placeOrderBtn.getStyleClass().add("button");

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        placeOrderBtn.setOnAction(e -> {
            // IMPORTANT: Check if a user is logged in before attempting to place an order
            if (loggedInUser == null) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Please log in to place an order.");
                return;
            }

            if (currentOrderItems.isEmpty()) {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please add items to your order before placing.");
                return;
            }

            int newOrderId = DatabaseManager.getNextAvailableOrderIdFromDB(); // Get next ID from DB
            Order newOrder = new Order(newOrderId, loggedInUser.getUsername()); // Link order to logged-in user
            for (MenuItem item : currentOrderItems) {
                newOrder.addItem(item);
            }
            newOrder.setPaymentMethod(paymentMethodCombo.getValue());

            // Admin orders could get a discount, but customer orders usually don't by default
            // If you want to add customer-specific discounts, this is where you'd implement it.

            if (DatabaseManager.saveOrder(newOrder)) {
                allOrders.add(newOrder); // Add to local list for admin view
                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Order ID " + newOrder.orderId + " placed successfully!");
                currentOrderItems.clear();
                updateOrderSummary(orderSummaryArea, currentOrderItems);
                paymentMethodCombo.getSelectionModel().select(PaymentMethod.CASH); // Reset
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Failed to place order. Please try again.");
            }
        });

        contentPane.getChildren().addAll(
            heading,
            new Label("Available Menu Items:"),
            menuListView,
            orderItemActions,
            new Separator(),
            selectedItemsLabel,
            orderSummaryArea,
            paymentMethodCombo,
            placeOrderBtn,
            messageLabel
        );
        return contentPane;
    }

    /**
     * Updates the order summary TextArea with current items and totals.
     */
    private void updateOrderSummary(TextArea summaryArea, ObservableList<MenuItem> items) {
        Order tempOrder = new Order(0); // Temporary order to calculate totals
        for (MenuItem item : items) {
            tempOrder.addItem(item);
        }
        summaryArea.setText(tempOrder.toDetailedString());
    }

    /**
     * Helper to get the next available order ID.
     */
    private int getNextOrderId() {
        return allOrders.stream()
                .mapToInt(Order::getOrderId)
                .max()
                .orElse(0) + 1;
    }


    /**
     * Creates the pane for customers to book a table.
     * This pane uses the loggedInUser's username for booking placement.
     */
    private VBox createTableBookingPaneForCustomer() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Book a Table");
        heading.getStyleClass().add("prompt-label");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 20, 0));
        formGrid.setAlignment(Pos.CENTER);

        TextField customerNameField = new TextField(); // No pre-fill, ensure loggedInUser is used below
        customerNameField.setPromptText("Your Full Name");
        customerNameField.getStyleClass().add("text-field");

        TextField phoneField = new TextField(); // No pre-fill, ensure loggedInUser is used below
        phoneField.setPromptText("Your Phone Number");
        phoneField.getStyleClass().add("text-field");

        // Pre-fill if user is logged in
        if (loggedInUser != null) {
            customerNameField.setText(loggedInUser.getFullName());
            phoneField.setText(loggedInUser.getPhoneNumber());
        }


        ComboBox<TableType> tableTypeCombo = new ComboBox<>();
        tableTypeCombo.setPromptText("Select Table Type (Seats)");
        tableTypeCombo.getStyleClass().add("combo-box");
        tableTypeCombo.setItems(FXCollections.observableArrayList(TableType.values()));

        ComboBox<Integer> tableNumberCombo = new ComboBox<>();
        tableNumberCombo.setPromptText("Select Table Number");
        tableNumberCombo.getStyleClass().add("combo-box");
        // Populate table numbers based on type selection later

        Label bookingFeeLabel = new Label("Booking Fee: Rs.0.00");
        bookingFeeLabel.getStyleClass().add("label");

        // Listen for table type selection to populate table numbers and update fee
        tableTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // For simplicity, let's assume 10 tables of each type for now.
                // In a real app, you'd check availability.
                ObservableList<Integer> availableTableNumbers = FXCollections.observableArrayList();
                for (int i = 1; i <= 10; i++) { // Assuming 10 tables of each type
                    availableTableNumbers.add(i);
                }
                tableNumberCombo.setItems(availableTableNumbers);
                tableNumberCombo.getSelectionModel().clearSelection(); // Clear previous selection

                // Example: booking fee calculation based on seats
                double fee = newVal.getSeats() * 20.0; // Rs.20 per seat
                bookingFeeLabel.setText("Booking Fee: Rs." + String.format("%.2f", fee));
            } else {
                tableNumberCombo.setItems(FXCollections.emptyObservableList());
                bookingFeeLabel.setText("Booking Fee: Rs.0.00");
            }
        });

        formGrid.add(new Label("Your Name:"), 0, 0);
        formGrid.add(customerNameField, 1, 0);
        formGrid.add(new Label("Phone Number:"), 0, 1);
        formGrid.add(phoneField, 1, 1);
        formGrid.add(new Label("Table Type:"), 0, 2);
        formGrid.add(tableTypeCombo, 1, 2);
        formGrid.add(new Label("Table Number:"), 0, 3);
        formGrid.add(tableNumberCombo, 1, 3);
        formGrid.add(bookingFeeLabel, 0, 4, 2, 1);


        Button bookTableBtn = new Button("Confirm Booking");
        bookTableBtn.getStyleClass().add("button");

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        bookTableBtn.setOnAction(e -> {
            // IMPORTANT: Check if a user is logged in before attempting to book a table
            if (loggedInUser == null) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Please log in to book a table.");
                return;
            }

            String customerName = customerNameField.getText().trim();
            String phone = phoneField.getText().trim();
            TableType selectedTableType = tableTypeCombo.getValue();
            Integer selectedTableNumber = tableNumberCombo.getValue();
            double bookingFee = 0.0;
            if (selectedTableType != null) {
                bookingFee = selectedTableType.getSeats() * 20.0; // Re-calculate to ensure consistency
            }

            if (customerName.isEmpty() || phone.isEmpty() || selectedTableType == null || selectedTableNumber == null) {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please fill all booking details.");
                return;
            }

            // Create a new TableBooking instance, linking to loggedInUser.getUsername()
            TableBooking newBooking = new TableBooking(
                    loggedInUser.getUsername(), // Use logged-in user's username as customerId
                    customerName,
                    phone,
                    selectedTableType,
                    selectedTableNumber,
                    bookingFee
            );

            if (DatabaseManager.saveTableBooking(newBooking)) {
                allBookings.add(newBooking); // Add to local list for admin view
                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Table booking confirmed for " + customerName + "!");
                // Clear fields (except pre-filled user details)
                tableTypeCombo.getSelectionModel().clearSelection();
                tableNumberCombo.getSelectionModel().clearSelection();
                bookingFeeLabel.setText("Booking Fee: Rs.0.00");
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Failed to book table. Please try again.");
            }
        });

        contentPane.getChildren().addAll(heading, formGrid, bookTableBtn, messageLabel);
        return contentPane;
    }

    /**
     * Creates the pane for customers to give feedback.
     * This pane uses the loggedInUser's username for feedback submission.
     */
    private VBox createFeedbackPaneForCustomer() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Give Us Your Feedback");
        heading.getStyleClass().add("prompt-label");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 20, 0));
        formGrid.setAlignment(Pos.CENTER);

        Label ratingLabel = new Label("Overall Rating (1-5):");
        Slider ratingSlider = new Slider(1, 5, 3); // Min, Max, Default
        ratingSlider.setBlockIncrement(1);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.getStyleClass().add("slider");


        Label commentsLabel = new Label("Comments:");
        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Enter your comments here...");
        commentsArea.setWrapText(true);
        commentsArea.setPrefHeight(100);
        commentsArea.getStyleClass().add("text-area");

        formGrid.add(ratingLabel, 0, 0);
        formGrid.add(ratingSlider, 1, 0);
        formGrid.add(commentsLabel, 0, 1);
        formGrid.add(commentsArea, 1, 1);

        Button submitFeedbackBtn = new Button("Submit Feedback");
        submitFeedbackBtn.getStyleClass().add("button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");


        submitFeedbackBtn.setOnAction(e -> {
            // IMPORTANT: Check if a user is logged in before attempting to submit feedback
            if (loggedInUser == null) {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Please log in to submit feedback.");
                return;
            }

            int rating = (int) ratingSlider.getValue();
            String comments = commentsArea.getText().trim();

            if (comments.isEmpty()) {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please enter your comments.");
                return;
            }

            // Create Feedback object linked to logged-in user
            Feedback newFeedback = new Feedback(0, loggedInUser.getUsername(), rating, comments, LocalDateTime.now());
            if (DatabaseManager.saveFeedback(newFeedback)) {
                allFeedback.add(newFeedback); // Add to local list for admin view
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Feedback submitted successfully! Thank you.");
                commentsArea.clear();
                ratingSlider.setValue(3);
            } else {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Failed to submit feedback. Please try again.");
            }
        });

        contentPane.getChildren().addAll(heading, formGrid, submitFeedbackBtn, msgLabel);
        return contentPane;
    }

    /**
     * Creates the pane for customers to rate a dish.
     * This pane uses the loggedInUser's username for rating submission.
     */
    private VBox createDishRatingPaneForCustomer() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("Rate a Dish");
        heading.getStyleClass().add("prompt-label");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 20, 0));
        formGrid.setAlignment(Pos.CENTER);

        Label dishLabel = new Label("Select Dish:");
        ComboBox<MenuItem> dishCombo = new ComboBox<>();
        dishCombo.setPromptText("Choose a dish to rate");
        dishCombo.getStyleClass().add("combo-box");
        dishCombo.setItems(FXCollections.observableArrayList(menuList)); // Populate with available dishes

        Label ratingLabel = new Label("Rating (1-5 Stars):");
        Slider ratingSlider = new Slider(1, 5, 3); // Min, Max, Default
        ratingSlider.setBlockIncrement(1);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.getStyleClass().add("slider");

        formGrid.add(dishLabel, 0, 0);
        formGrid.add(dishCombo, 1, 0);
        formGrid.add(ratingLabel, 0, 1);
        formGrid.add(ratingSlider, 1, 1);

        Button submitRatingBtn = new Button("Submit Rating");
        submitRatingBtn.getStyleClass().add("button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        submitRatingBtn.setOnAction(e -> {
            // IMPORTANT: Check if a user is logged in before attempting to rate a dish
            if (loggedInUser == null) {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Please log in to rate a dish.");
                return;
            }

            MenuItem selectedDish = dishCombo.getValue();
            int rating = (int) ratingSlider.getValue();

            if (selectedDish == null) {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select a dish to rate.");
                return;
            }

            // Create DishRating object linked to logged-in user
            DishRating newRating = new DishRating(0, selectedDish.getId(), loggedInUser.getUsername(), rating, LocalDateTime.now());
            if (DatabaseManager.saveDishRating(newRating)) {
                allDishRatings.add(newRating); // Add to local list for admin view
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Rating submitted for " + selectedDish.getName() + "!");
                dishCombo.getSelectionModel().clearSelection(); // Clear form
                ratingSlider.setValue(3);
            } else {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Failed to submit rating. Please try again.");
            }
        });

        contentPane.getChildren().addAll(heading, formGrid, submitRatingBtn, msgLabel);
        return contentPane;
    }


    /**
     * Creates the pane for the customer's profile, displaying their details
     * and allowing them to view their past orders and bookings.
     */
    private VBox createCustomerProfilePane() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("My Profile");
        heading.getStyleClass().add("prompt-label");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(10);
        profileGrid.setVgap(10);
        profileGrid.setPadding(new Insets(10, 0, 20, 0));
        profileGrid.setAlignment(Pos.CENTER_LEFT); // Align left within the tab

        TextField usernameField = new TextField(loggedInUser.getUsername());
        usernameField.setEditable(false); // Username cannot be changed
        usernameField.getStyleClass().add("text-field");

        TextField fullNameField = new TextField(loggedInUser.getFullName());
        fullNameField.setPromptText("Full Name");
        fullNameField.getStyleClass().add("text-field");

        TextField emailField = new TextField(loggedInUser.getEmail());
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field");

        TextField phoneField = new TextField(loggedInUser.getPhoneNumber());
        phoneField.setPromptText("Phone Number");
        phoneField.getStyleClass().add("text-field");

        profileGrid.add(new Label("Username:"), 0, 0);
        profileGrid.add(usernameField, 1, 0);
        profileGrid.add(new Label("Full Name:"), 0, 1);
        profileGrid.add(fullNameField, 1, 1);
        profileGrid.add(new Label("Email:"), 0, 2);
        profileGrid.add(emailField, 1, 2);
        profileGrid.add(new Label("Phone:"), 0, 3);
        profileGrid.add(phoneField, 1, 3);

        Button saveProfileBtn = new Button("Save Profile Changes");
        saveProfileBtn.getStyleClass().add("button");

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");


        saveProfileBtn.setOnAction(e -> {
            String newFullName = fullNameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newFullName.isEmpty()) {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Full Name cannot be empty.");
                return;
            }

            // Create a temporary User object with updated details
            User updatedUser = new User(loggedInUser.getUsername(), loggedInUser.getPasswordHash(), newFullName, newEmail, newPhone);

            if (DatabaseManager.updateUserProfile(updatedUser)) {
                // Update the loggedInUser object in memory
                loggedInUser.setFullName(newFullName);
                loggedInUser.setEmail(newEmail);
                loggedInUser.setPhoneNumber(newPhone);

                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Profile updated successfully!");
                // Optionally refresh the welcome label in the main customer menu if needed
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Failed to update profile. Please try again.");
            }
        });

        contentPane.getChildren().addAll(heading, profileGrid, saveProfileBtn, new Separator(), messageLabel);
        return contentPane;
    }


    /**
     * Creates the pane to display the logged-in customer's orders.
     */
    private VBox createMyOrdersPaneForCustomer() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("My Orders");
        heading.getStyleClass().add("prompt-label");

        TableView<Order> myOrdersTable = new TableView<>();
        myOrdersTable.getStyleClass().add("table-view");
        myOrdersTable.setPrefHeight(400);

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subtotalCol.setPrefWidth(90);

        TableColumn<Order, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountApplied"));
        discountCol.setPrefWidth(80);

        TableColumn<Order, Double> finalAmountCol = new TableColumn<>("Final Amount");
        finalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalWithGST"));
        finalAmountCol.setPrefWidth(100);

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);

        TableColumn<Order, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);

        myOrdersTable.getColumns().addAll(orderIdCol, subtotalCol, discountCol, finalAmountCol, statusCol, paymentStatusCol, paymentMethodCol);

        // Load and filter orders specific to the logged-in user
        // Ensure loggedInUser is not null before attempting to get username
        if (loggedInUser != null) {
            ObservableList<Order> customerOrders = FXCollections.observableArrayList(
                    DatabaseManager.loadOrdersByCustomerUsername(loggedInUser.getUsername(), menuMap)
            );
            myOrdersTable.setItems(customerOrders);
        } else {
            myOrdersTable.setItems(FXCollections.emptyObservableList()); // Show empty table if no user logged in
            // Maybe add a label here saying "Please log in to see your orders"
        }


        Button viewDetailsBtn = new Button("View Order Details");
        viewDetailsBtn.getStyleClass().add("small-dialog-button");
        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        viewDetailsBtn.setOnAction(e -> {
            Order selectedOrder = myOrdersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                showOrderDetailsDialog(selectedOrder); // Reuse existing dialog
            } else {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Please select an order to view details.");
            }
        });


        contentPane.getChildren().addAll(heading, new Separator(), myOrdersTable, viewDetailsBtn, messageLabel);
        return contentPane;
    }

    /**
     * Creates the pane to display the logged-in customer's bookings.
     */
    private VBox createMyBookingsPaneForCustomer() {
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("tab-content-area");

        Label heading = new Label("My Table Bookings");
        heading.getStyleClass().add("prompt-label");

        TableView<TableBooking> myBookingsTable = new TableView<>();
        myBookingsTable.getStyleClass().add("table-view");
        myBookingsTable.setPrefHeight(400);

        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Your Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setPrefWidth(150);

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(100);

        TableColumn<TableBooking, TableType> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));
        tableTypeCol.setPrefWidth(100);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(80);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(70);

        TableColumn<TableBooking, Double> bookingFeeCol = new TableColumn<>("Booking Fee");
        bookingFeeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        bookingFeeCol.setPrefWidth(100);

        TableColumn<TableBooking, PaymentStatus> bookingPaymentStatusCol = new TableColumn<>("Payment Status");
        bookingPaymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        bookingPaymentStatusCol.setPrefWidth(120);

        myBookingsTable.getColumns().addAll(customerNameCol, phoneCol, tableTypeCol, tableNumberCol, seatsCol, bookingFeeCol, bookingPaymentStatusCol);

        // Load bookings specific to the logged-in user
        // Ensure loggedInUser is not null before attempting to get username
        if (loggedInUser != null) {
            ObservableList<TableBooking> customerBookings = FXCollections.observableArrayList(
                    DatabaseManager.getTableBookingsByCustomerId(loggedInUser.getUsername())
            );
            myBookingsTable.setItems(customerBookings);
        } else {
            myBookingsTable.setItems(FXCollections.emptyObservableList()); // Show empty table if no user logged in
            // Maybe add a label here saying "Please log in to see your bookings"
        }

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        contentPane.getChildren().addAll(heading, new Separator(), myBookingsTable, messageLabel);
        return contentPane;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
