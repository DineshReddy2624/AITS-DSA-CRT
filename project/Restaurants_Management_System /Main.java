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
import java.time.LocalDate;
import java.time.LocalDateTime; // New import for LocalDateTime
import java.util.stream.Collectors;

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
    private ObservableList<Feedback> allFeedback = FXCollections.observableArrayList(); // New: ObservableList for Feedback
    private ObservableList<DishRating> allDishRatings = FXCollections.observableArrayList(); // New: ObservableList for DishRatings

    private int orderCounter = 1; // Simple counter for order IDs
    private String loggedInCustomerUsername = null; // New: To store the username of the logged-in customer

    // UI elements for Main Menu, accessible across methods for updates
    private Stage primaryStage;
    private BorderPane mainLayout;
    private TabPane adminTabPane;
    private Tab orderManagementTab;
    private Tab menuManagementTab;
    private Tab bookingManagementTab;
    private Tab userManagementTab; // New Tab for User management (Admin)
    private Tab feedbackManagementTab; // New Tab for Feedback management (Admin)
    private Tab dishRatingManagementTab; // New Tab for Dish Rating management (Admin)

    // UI elements specific to current order and booking for customer
    private Order currentOrder = new Order(orderCounter); // Customer's current order
    private Label currentOrderLabel = new Label("Current Order: Rs.0.00");
    private ListView<MenuItem> currentOrderListView = new ListView<>();
    private Label customerOutput = new Label(""); // For displaying messages to the customer

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Shield Restaurant Management System");
        primaryStage.setResizable(true); // Allow resizing

        // Initialize database and load initial data
        DatabaseManager.initializeDatabase();
        loadInitialData();

        // Show the initial choice screen (Admin/Customer)
        showInitialChoiceScreen();

        primaryStage.show();
    }

    /**
     * Loads menu items, orders, and bookings from the database.
     */
    private void loadInitialData() {
        menuList = DatabaseManager.loadMenuItems();
        menuMap.clear();
        for (MenuItem item : menuList) {
            menuMap.put(item.getId(), item);
        }

        allOrders.clear();
        allOrders.addAll(DatabaseManager.loadOrders(menuMap));
        // Find the next available order ID
        if (!allOrders.isEmpty()) {
            orderCounter = allOrders.stream().mapToInt(o -> o.orderId).max().orElse(0) + 1;
        } else {
            orderCounter = 1;
        }
        currentOrder = new Order(orderCounter); // Reset current order for new session

        allBookings.clear();
        allBookings.addAll(DatabaseManager.loadTableBookings());

        allFeedback.clear(); // Load feedback
        allFeedback.addAll(DatabaseManager.loadFeedback());

        allDishRatings.clear(); // Load dish ratings
        allDishRatings.addAll(DatabaseManager.loadDishRatings());
    }

    /**
     * Displays the initial screen for choosing Admin or Customer access.
     */
    private void showInitialChoiceScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.getStyleClass().add("root");

        Label title = new Label("Welcome to Shield Restaurant");
        title.getStyleClass().add("title-label");

        Button adminLoginBtn = new Button("Admin Login");
        adminLoginBtn.getStyleClass().add("button");
        adminLoginBtn.setOnAction(e -> showAdminLoginDialog());

        Button customerAccessBtn = new Button("Customer Access");
        customerAccessBtn.getStyleClass().add("button");
        customerAccessBtn.setOnAction(e -> showCustomerAccessScreen());

        root.getChildren().addAll(title, adminLoginBtn, customerAccessBtn);

        Scene scene = new Scene(root, 600, 450); // Adjusted size
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    /**
     * Displays the Admin Login dialog.
     */
    private void showAdminLoginDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Admin Login");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage); // Make it a child of the primary stage

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("alert"); // Apply custom alert styling
        dialogPane.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username:");
        userLabel.getStyleClass().add("label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("admin");
        usernameField.getStyleClass().add("text-field");

        Label passLabel = new Label("Password:");
        passLabel.getStyleClass().add("label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("admin123");
        passwordField.getStyleClass().add("password-field");

        // "See Password" functionality
        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("admin123");
        passwordVisibleField.getStyleClass().add("text-field");
        passwordVisibleField.setManaged(false); // Hide by default
        passwordVisibleField.setVisible(false); // Hide by default

        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.getStyleClass().add("label"); // Can apply label style
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordVisibleField.setText(passwordField.getText());
                passwordField.setManaged(false);
                passwordField.setVisible(false);
                passwordVisibleField.setManaged(true);
                passwordVisibleField.setVisible(true);
            } else {
                passwordField.setText(passwordVisibleField.getText());
                passwordVisibleField.setManaged(false);
                passwordVisibleField.setVisible(false);
                passwordField.setManaged(true);
                passwordField.setVisible(true);
            }
        });
        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty()); // Keep synced

        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(passwordVisibleField, 1, 1); // Overlap passwordField, manage visibility
        grid.add(showPasswordCheckbox, 1, 2);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");
        grid.add(messageLabel, 0, 3, 2, 1); // Span two columns

        dialogPane.setContent(grid);

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(loginButtonType, cancelButtonType);

        Button loginButton = (Button) dialogPane.lookupButton(loginButtonType);
        loginButton.getStyleClass().add("small-dialog-button");
        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("small-dialog-button");


        loginButton.addEventFilter(ActionEvent.ACTION, event -> {
            String username = usernameField.getText();
            String password = passwordField.getText(); // Get from the PasswordField

            if ("admin".equals(username) && "admin123".equals(password)) {
                dialog.close();
                showAdminMenu();
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Invalid credentials.");
                event.consume(); // Consume the event to prevent dialog from closing
            }
        });

        dialog.showAndWait();
    }

    /**
     * Displays the Customer Access choice screen (Login or Sign Up).
     */
    private void showCustomerAccessScreen() {
        Stage customerStage = new Stage();
        customerStage.setTitle("Customer Access");
        customerStage.initModality(Modality.APPLICATION_MODAL);
        customerStage.initOwner(primaryStage);
        customerStage.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root");

        Label title = new Label("Customer Portal");
        title.getStyleClass().add("prompt-label");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().addAll("button", "small-dialog-button");
        loginBtn.setOnAction(e -> {
            customerStage.close();
            showLoginDialog();
        });

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.getStyleClass().addAll("button", "small-dialog-button");
        signUpBtn.setOnAction(e -> {
            customerStage.close();
            showSignUpDialog();
        });

        Button backBtn = new Button("Back to Main");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> customerStage.close());

        root.getChildren().addAll(title, loginBtn, signUpBtn, backBtn);

        Scene scene = new Scene(root, 350, 300);
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        customerStage.setScene(scene);
        customerStage.showAndWait();
    }

    /**
     * Displays the Customer Login dialog.
     */
    private void showLoginDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Customer Login");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("alert");
        dialogPane.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username:");
        userLabel.getStyleClass().add("label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("text-field");

        Label passLabel = new Label("Password:");
        passLabel.getStyleClass().add("label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");

        // "See Password" functionality
        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Enter your password");
        passwordVisibleField.getStyleClass().add("text-field");
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);

        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.getStyleClass().add("label");
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordVisibleField.setText(passwordField.getText());
                passwordField.setManaged(false);
                passwordField.setVisible(false);
                passwordVisibleField.setManaged(true);
                passwordVisibleField.setVisible(true);
            } else {
                passwordField.setText(passwordVisibleField.getText());
                passwordVisibleField.setManaged(false);
                passwordVisibleField.setVisible(false);
                passwordField.setManaged(true);
                passwordField.setVisible(true);
            }
        });
        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty());

        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(passwordVisibleField, 1, 1);
        grid.add(showPasswordCheckbox, 1, 2);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");
        grid.add(messageLabel, 0, 3, 2, 1);

        dialogPane.setContent(grid);

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(loginButtonType, cancelButtonType);

        Button loginButton = (Button) dialogPane.lookupButton(loginButtonType);
        loginButton.getStyleClass().add("small-dialog-button");
        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("small-dialog-button");


        loginButton.addEventFilter(ActionEvent.ACTION, event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Username and password cannot be empty.");
                event.consume();
                return;
            }

            User authenticatedUser = DatabaseManager.authenticateUser(username, password);
            if (authenticatedUser != null) {
                loggedInCustomerUsername = authenticatedUser.getUsername();
                dialog.close();
                showCustomerMenu();
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Invalid username or password.");
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    /**
     * Displays the Customer Sign Up dialog.
     */
    private void showSignUpDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Customer Sign Up");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("alert");
        dialogPane.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username:");
        userLabel.getStyleClass().add("label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.getStyleClass().add("text-field");

        Label passLabel = new Label("Password:");
        passLabel.getStyleClass().add("label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");

        Label confirmPassLabel = new Label("Confirm Password:");
        confirmPassLabel.getStyleClass().add("label");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        confirmPasswordField.getStyleClass().add("password-field");

        // "See Password" functionality for both password fields
        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Enter your password");
        passwordVisibleField.getStyleClass().add("text-field");
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);

        TextField confirmPasswordVisibleField = new TextField();
        confirmPasswordVisibleField.setPromptText("Re-enter your password");
        confirmPasswordVisibleField.getStyleClass().add("text-field");
        confirmPasswordVisibleField.setManaged(false);
        confirmPasswordVisibleField.setVisible(false);

        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.getStyleClass().add("label");
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordVisibleField.setText(passwordField.getText());
                confirmPasswordVisibleField.setText(confirmPasswordField.getText());
                passwordField.setManaged(false); passwordField.setVisible(false);
                confirmPasswordField.setManaged(false); confirmPasswordField.setVisible(false);
                passwordVisibleField.setManaged(true); passwordVisibleField.setVisible(true);
                confirmPasswordVisibleField.setManaged(true); confirmPasswordVisibleField.setVisible(true);
            } else {
                passwordField.setText(passwordVisibleField.getText());
                confirmPasswordField.setText(confirmPasswordVisibleField.getText());
                passwordVisibleField.setManaged(false); passwordVisibleField.setVisible(false);
                confirmPasswordVisibleField.setManaged(false); confirmPasswordVisibleField.setVisible(false);
                passwordField.setManaged(true); passwordField.setVisible(true);
                confirmPasswordField.setManaged(true); confirmPasswordField.setVisible(true);
            }
        });
        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordVisibleField.textProperty());

        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(passwordVisibleField, 1, 1);
        grid.add(confirmPassLabel, 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(confirmPasswordVisibleField, 1, 2);
        grid.add(showPasswordCheckbox, 1, 3);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");
        grid.add(messageLabel, 0, 4, 2, 1);

        dialogPane.setContent(grid);

        ButtonType signUpButtonType = new ButtonType("Sign Up", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(signUpButtonType, cancelButtonType);

        Button signUpButton = (Button) dialogPane.lookupButton(signUpButtonType);
        signUpButton.getStyleClass().add("small-dialog-button");
        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("small-dialog-button");


        signUpButton.addEventFilter(ActionEvent.ACTION, event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("All fields are required.");
                event.consume();
                return;
            }
            if (!password.equals(confirmPassword)) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Passwords do not match.");
                event.consume();
                return;
            }
            if (password.length() < 6) {
                messageLabel.getStyleClass().setAll("message-label", "warning");
                messageLabel.setText("Password must be at least 6 characters long.");
                event.consume();
                return;
            }

            String hashedPassword = PasswordUtil.hashPassword(password);
            if (hashedPassword == null) {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Error hashing password. Please try again.");
                event.consume();
                return;
            }

            if (DatabaseManager.registerUser(username, hashedPassword)) {
                messageLabel.getStyleClass().setAll("message-label", "success");
                messageLabel.setText("Registration successful! You can now log in.");
                // Automatically log in the user after successful registration
                loggedInCustomerUsername = username;
                dialog.close();
                showCustomerMenu();
            } else {
                messageLabel.getStyleClass().setAll("message-label", "error");
                messageLabel.setText("Username already exists or registration failed.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    /**
     * Displays the Admin Main Menu (TabPane).
     */
    private void showAdminMenu() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root");

        adminTabPane = new TabPane();
        adminTabPane.getStyleClass().add("tab-pane");

        // --- Menu Management Tab ---
        menuManagementTab = new Tab("Menu Management");
        menuManagementTab.setClosable(false);
        VBox menuContent = new VBox(15);
        menuContent.setPadding(new Insets(20));
        menuContent.setAlignment(Pos.TOP_LEFT);
        menuManagementTab.setContent(menuContent);
        setupMenuManagement(menuContent);

        // --- Order Management Tab ---
        orderManagementTab = new Tab("Order Management");
        orderManagementTab.setClosable(false);
        VBox orderContent = new VBox(15);
        orderContent.setPadding(new Insets(20));
        orderContent.setAlignment(Pos.TOP_LEFT);
        orderManagementTab.setContent(orderContent);
        setupOrderManagement(orderContent);

        // --- Table Booking Management Tab ---
        bookingManagementTab = new Tab("Table Booking Management");
        bookingManagementTab.setClosable(false);
        VBox bookingContent = new VBox(15);
        bookingContent.setPadding(new Insets(20));
        bookingContent.setAlignment(Pos.TOP_LEFT);
        bookingManagementTab.setContent(bookingContent);
        setupBookingManagement(bookingContent);

        // --- New: Feedback Management Tab ---
        feedbackManagementTab = new Tab("Feedback");
        feedbackManagementTab.setClosable(false);
        VBox feedbackContent = new VBox(15);
        feedbackContent.setPadding(new Insets(20));
        feedbackContent.setAlignment(Pos.TOP_LEFT);
        feedbackManagementTab.setContent(feedbackContent);
        setupFeedbackManagement(feedbackContent);

        // --- New: Dish Rating Management Tab ---
        dishRatingManagementTab = new Tab("Dish Ratings");
        dishRatingManagementTab.setClosable(false);
        VBox dishRatingContent = new VBox(15);
        dishRatingContent.setPadding(new Insets(20));
        dishRatingContent.setAlignment(Pos.TOP_LEFT);
        dishRatingManagementTab.setContent(dishRatingContent);
        setupDishRatingManagement(dishRatingContent);

        adminTabPane.getTabs().addAll(
            menuManagementTab,
            orderManagementTab,
            bookingManagementTab,
            feedbackManagementTab, // Add new tab
            dishRatingManagementTab // Add new tab
        );

        mainLayout.setCenter(adminTabPane);

        // Admin Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().addAll("button", "secondary-button");
        logoutButton.setOnAction(e -> {
            loggedInCustomerUsername = null; // Clear logged in user
            showInitialChoiceScreen(); // Go back to the initial choice screen
        });

        HBox topBar = new HBox(10, new Label("Admin Panel"), logoutButton);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.getStyleClass().add("tab-header-area"); // Reusing this style for a consistent look
        mainLayout.setTop(topBar);

        Scene scene = new Scene(mainLayout, 1000, 700); // Increased size
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    /**
     * Displays the Customer Main Menu (TabPane).
     */
    private void showCustomerMenu() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root");

        TabPane customerTabPane = new TabPane();
        customerTabPane.getStyleClass().add("tab-pane");

        // --- Welcome Message ---
        Label welcomeLabel = new Label("Welcome to Shield Restaurant, " + loggedInCustomerUsername + "!");
        welcomeLabel.getStyleClass().add("title-label");
        welcomeLabel.setAlignment(Pos.CENTER);
        welcomeLabel.setMaxWidth(Double.MAX_VALUE); // Center the label

        // Customer Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().addAll("button", "secondary-button");
        logoutButton.setOnAction(e -> {
            loggedInCustomerUsername = null; // Clear logged in user
            showInitialChoiceScreen(); // Go back to the initial choice screen
        });

        HBox topBar = new HBox(20, welcomeLabel, logoutButton);
        HBox.setHgrow(welcomeLabel, Priority.ALWAYS); // Make welcomeLabel take available space
        topBar.setAlignment(Pos.CENTER_RIGHT); // Align to the right initially
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.getStyleClass().add("tab-header-area");
        mainLayout.setTop(topBar);


        // --- Order Placement Tab ---
        Tab orderTab = new Tab("Place Order");
        orderTab.setClosable(false);
        VBox orderContent = new VBox(15);
        orderContent.setPadding(new Insets(20));
        orderContent.setAlignment(Pos.TOP_LEFT);
        orderTab.setContent(orderContent);
        setupCustomerOrderPlacement(orderContent);

        // --- Table Booking Tab (Customer) ---
        Tab customerBookingTab = new Tab("Book Table");
        customerBookingTab.setClosable(false);
        VBox customerBookingContent = new VBox(15);
        customerBookingContent.setPadding(new Insets(20));
        customerBookingContent.setAlignment(Pos.TOP_LEFT);
        customerBookingTab.setContent(customerBookingContent);
        setupCustomerBooking(customerBookingContent);

        // --- New: View My Orders Tab ---
        Tab viewMyOrdersTab = new Tab("View My Orders");
        viewMyOrdersTab.setClosable(false);
        VBox viewMyOrdersContent = new VBox(15);
        viewMyOrdersContent.setPadding(new Insets(20));
        viewMyOrdersContent.setAlignment(Pos.TOP_LEFT);
        viewMyOrdersTab.setContent(viewMyOrdersContent);
        setupViewMyOrders(viewMyOrdersContent);


        // --- New: Provide Feedback Tab ---
        Tab provideFeedbackTab = new Tab("Provide Feedback");
        provideFeedbackTab.setClosable(false);
        VBox provideFeedbackContent = new VBox(15);
        provideFeedbackContent.setPadding(new Insets(20));
        provideFeedbackContent.setAlignment(Pos.TOP_LEFT);
        provideFeedbackTab.setContent(provideFeedbackContent);
        setupProvideFeedback(provideFeedbackContent);

        // --- New: Rate Dishes Tab ---
        Tab rateDishesTab = new Tab("Rate Dishes");
        rateDishesTab.setClosable(false);
        VBox rateDishesContent = new VBox(15);
        rateDishesContent.setPadding(new Insets(20));
        rateDishesContent.setAlignment(Pos.TOP_LEFT);
        rateDishesTab.setContent(rateDishesContent);
        setupRateDishes(rateDishesContent);


        customerTabPane.getTabs().addAll(
            orderTab,
            customerBookingTab,
            viewMyOrdersTab, // Add new tab
            provideFeedbackTab, // Add new tab
            rateDishesTab // Add new tab
        );

        mainLayout.setCenter(customerTabPane);

        Scene scene = new Scene(mainLayout, 1000, 700); // Increased size
        scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        primaryStage.setScene(scene);
    }


    // --- Setup Methods for Tabs ---

    /**
     * Sets up the Menu Management tab for admin.
     * @param contentPane The VBox to populate.
     */
    private void setupMenuManagement(VBox contentPane) {
        Label heading = new Label("Manage Menu Items");
        heading.getStyleClass().add("prompt-label");

        // Form for adding/editing menu items
        GridPane menuFormGrid = new GridPane();
        menuFormGrid.setHgap(10);
        menuFormGrid.setVgap(10);
        menuFormGrid.setPadding(new Insets(10, 0, 20, 0));

        TextField idField = new TextField();
        idField.setPromptText("ID");
        idField.getStyleClass().add("text-field");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.getStyleClass().add("text-field");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.getStyleClass().add("text-field");

        Button addSaveBtn = new Button("Add/Save Item");
        addSaveBtn.getStyleClass().add("button");
        Button clearBtn = new Button("Clear Form");
        clearBtn.getStyleClass().addAll("button", "secondary-button");
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().addAll("button", "secondary-button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        menuFormGrid.add(new Label("ID:"), 0, 0); menuFormGrid.add(idField, 1, 0);
        menuFormGrid.add(new Label("Name:"), 0, 1); menuFormGrid.add(nameField, 1, 1);
        menuFormGrid.add(new Label("Price:"), 0, 2); menuFormGrid.add(priceField, 1, 2);

        HBox formButtons = new HBox(10, addSaveBtn, clearBtn, deleteBtn);
        formButtons.setAlignment(Pos.CENTER_LEFT);
        menuFormGrid.add(formButtons, 0, 3, 2, 1);
        menuFormGrid.add(msgLabel, 0, 4, 2, 1);

        // TableView for displaying menu items
        TableView<MenuItem> menuTable = new TableView<>();
        menuTable.getStyleClass().add("table-view");
        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        menuTable.getColumns().addAll(idCol, nameCol, priceCol);
        menuTable.setItems(FXCollections.observableArrayList(menuList)); // Populate table

        // Actions
        addSaveBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());

                if (name.isEmpty()) {
                    msgLabel.getStyleClass().setAll("message-label", "error");
                    msgLabel.setText("Name cannot be empty.");
                    return;
                }
                if (price <= 0) {
                    msgLabel.getStyleClass().setAll("message-label", "error");
                    msgLabel.setText("Price must be positive.");
                    return;
                }

                MenuItem newItem = new MenuItem(id, name, price);
                DatabaseManager.saveMenuItem(newItem);
                loadInitialData(); // Reload all data
                menuTable.setItems(FXCollections.observableArrayList(menuList));
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Menu item saved successfully!");
                clearBtn.fire();
            } catch (NumberFormatException ex) {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Please enter valid numbers for ID and Price.");
            }
        });

        clearBtn.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            priceField.clear();
            msgLabel.setText("");
            menuTable.getSelectionModel().clearSelection();
        });

        deleteBtn.setOnAction(e -> {
            MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                DatabaseManager.deleteMenuItem(selectedItem.getId());
                loadInitialData(); // Reload all data
                menuTable.setItems(FXCollections.observableArrayList(menuList));
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Menu item deleted successfully!");
            } else {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select an item to delete.");
            }
        });

        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                idField.setText(String.valueOf(newSelection.getId()));
                nameField.setText(newSelection.getName());
                priceField.setText(String.valueOf(newSelection.getPrice()));
            }
        });

        contentPane.getChildren().addAll(heading, menuFormGrid, menuTable);
    }

    /**
     * Sets up the Order Management tab for admin.
     * @param contentPane The VBox to populate.
     */
    private void setupOrderManagement(VBox contentPane) {
        Label heading = new Label("Manage Orders");
        heading.getStyleClass().add("prompt-label");

        // TableView for displaying orders
        TableView<Order> orderTable = new TableView<>();
        orderTable.getStyleClass().add("table-view");
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Order, String> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        TableColumn<Order, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> {
            // Using a lambda for calculated properties
            return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject();
        });
        TableColumn<Order, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountApplied"));
        TableColumn<Order, Double> netAmountCol = new TableColumn<>("Net Amount");
        netAmountCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getFinalPriceBeforeGST()).asObject();
        });
        TableColumn<Order, Double> gstAmountCol = new TableColumn<>("GST");
        gstAmountCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getGSTAmount()).asObject();
        });
        TableColumn<Order, Double> finalAmountCol = new TableColumn<>("Final Amount");
        finalAmountCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalWithGST()).asObject();
        });
        TableColumn<Order, String> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));


        orderTable.getColumns().addAll(orderIdCol, statusCol, paymentStatusCol, subtotalCol, discountCol, netAmountCol, gstAmountCol, finalAmountCol, paymentMethodCol);
        orderTable.setItems(allOrders);

        // Controls for updating order status and payment
        HBox orderControls = new HBox(10);
        orderControls.setAlignment(Pos.CENTER_LEFT);
        ComboBox<OrderStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.values()));
        statusCombo.getStyleClass().add("combo-box");
        Button updateStatusBtn = new Button("Update Order Status");
        updateStatusBtn.getStyleClass().addAll("button", "small-dialog-button");

        ComboBox<PaymentStatus> paymentStatusCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentStatus.values()));
        paymentStatusCombo.getStyleClass().add("combo-box");
        Button updatePaymentStatusBtn = new Button("Update Payment Status");
        updatePaymentStatusBtn.getStyleClass().addAll("button", "small-dialog-button");

        ComboBox<PaymentMethod> paymentMethodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodCombo.getStyleClass().add("combo-box");
        Button updatePaymentMethodBtn = new Button("Update Payment Method");
        updatePaymentMethodBtn.getStyleClass().addAll("button", "small-dialog-button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        orderControls.getChildren().addAll(statusCombo, updateStatusBtn, paymentStatusCombo, updatePaymentStatusBtn, paymentMethodCombo, updatePaymentMethodBtn);

        // Actions
        updateStatusBtn.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            OrderStatus newStatus = statusCombo.getValue();
            if (selectedOrder != null && newStatus != null) {
                DatabaseManager.updateOrderStatus(selectedOrder.getOrderId(), newStatus);
                selectedOrder.status = newStatus; // Update local object
                orderTable.refresh(); // Refresh TableView to show changes
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Order " + selectedOrder.getOrderId() + " status updated to " + newStatus.getDisplayValue());
            } else {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select an order and a new status.");
            }
        });

        updatePaymentStatusBtn.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            PaymentStatus newStatus = paymentStatusCombo.getValue();
            if (selectedOrder != null && newStatus != null) {
                DatabaseManager.updateOrderPaymentStatus(selectedOrder.getOrderId(), newStatus);
                selectedOrder.paymentStatus = newStatus; // Update local object
                orderTable.refresh();
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Order " + selectedOrder.getOrderId() + " payment status updated to " + newStatus.getDisplayValue());

                // New: "Thank you" message on billing completion
                if (newStatus == PaymentStatus.PAID) {
                    showThankYouMessage(selectedOrder.getTotalWithGST());
                }
            } else {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select an order and a new payment status.");
            }
        });

        updatePaymentMethodBtn.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            PaymentMethod newMethod = paymentMethodCombo.getValue();
            if (selectedOrder != null && newMethod != null) {
                DatabaseManager.updateOrderPaymentMethod(selectedOrder.getOrderId(), newMethod);
                selectedOrder.paymentMethod = newMethod;
                orderTable.refresh();
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Order " + selectedOrder.getOrderId() + " payment method updated to " + newMethod.getDisplayValue());
            } else {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select an order and a new payment method.");
            }
        });

        contentPane.getChildren().addAll(heading, orderTable, orderControls, msgLabel);
    }

    /**
     * Sets up the Table Booking Management tab for admin.
     * @param contentPane The VBox to populate.
     */
    private void setupBookingManagement(VBox contentPane) {
        Label heading = new Label("Manage Table Bookings");
        heading.getStyleClass().add("prompt-label");

        // TableView for displaying bookings
        TableView<TableBooking> bookingTable = new TableView<>();
        bookingTable.getStyleClass().add("table-view");
        TableColumn<TableBooking, String> custIdCol = new TableColumn<>("Customer ID");
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<TableBooking, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        TableColumn<TableBooking, String> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));
        TableColumn<TableBooking, Integer> tableNumCol = new TableColumn<>("Table No.");
        tableNumCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<TableBooking, String> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        TableColumn<TableBooking, Double> feeCol = new TableColumn<>("Booking Fee");
        feeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));

        bookingTable.getColumns().addAll(custIdCol, nameCol, tableTypeCol, tableNumCol, seatsCol, phoneCol, paymentStatusCol, feeCol);
        bookingTable.setItems(allBookings);

        // Controls for updating booking payment status
        HBox bookingControls = new HBox(10);
        bookingControls.setAlignment(Pos.CENTER_LEFT);
        ComboBox<PaymentStatus> bookingPaymentStatusCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentStatus.values()));
        bookingPaymentStatusCombo.getStyleClass().add("combo-box");
        Button updateBookingPaymentStatusBtn = new Button("Update Booking Payment Status");
        updateBookingPaymentStatusBtn.getStyleClass().addAll("button", "small-dialog-button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        bookingControls.getChildren().addAll(bookingPaymentStatusCombo, updateBookingPaymentStatusBtn);

        // Actions
        updateBookingPaymentStatusBtn.setOnAction(e -> {
            TableBooking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
            PaymentStatus newStatus = bookingPaymentStatusCombo.getValue();
            if (selectedBooking != null && newStatus != null) {
                DatabaseManager.updateTableBookingPaymentStatus(selectedBooking.getCustomerId(), newStatus);
                selectedBooking.paymentStatus = newStatus; // Update local object
                bookingTable.refresh();
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Booking for Customer ID " + selectedBooking.getCustomerId() + " payment status updated to " + newStatus.getDisplayValue());
            } else {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select a booking and a new payment status.");
            }
        });

        // New: Search by Customer ID functionality
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchCustomerIdField = new TextField();
        searchCustomerIdField.setPromptText("Enter Customer ID to search");
        searchCustomerIdField.getStyleClass().add("text-field");
        Button searchBookingBtn = new Button("Search Bookings by Customer ID");
        searchBookingBtn.getStyleClass().addAll("button", "small-dialog-button");

        searchBookingBtn.setOnAction(e -> {
            String customerId = searchCustomerIdField.getText().trim();
            if (!customerId.isEmpty()) {
                ObservableList<TableBooking> filteredBookings = FXCollections.observableArrayList(
                    DatabaseManager.getTableBookingsByCustomerId(customerId)
                );
                bookingTable.setItems(filteredBookings);
                if (filteredBookings.isEmpty()) {
                    msgLabel.getStyleClass().setAll("message-label", "warning");
                    msgLabel.setText("No bookings found for Customer ID: " + customerId);
                } else {
                    msgLabel.getStyleClass().setAll("message-label", "success");
                    msgLabel.setText("Displaying bookings for Customer ID: " + customerId);
                }
            } else {
                bookingTable.setItems(allBookings); // Show all if search field is empty
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Search field is empty. Showing all bookings.");
            }
        });

        searchBox.getChildren().addAll(searchCustomerIdField, searchBookingBtn);


        contentPane.getChildren().addAll(heading, bookingTable, bookingControls, new Separator(), new Label("Search Bookings:"), searchBox, msgLabel);
    }

    /**
     * Sets up the Feedback Management tab for admin.
     * @param contentPane The VBox to populate.
     */
    private void setupFeedbackManagement(VBox contentPane) {
        Label heading = new Label("Customer Feedback");
        heading.getStyleClass().add("prompt-label");

        TableView<Feedback> feedbackTable = new TableView<>();
        feedbackTable.getStyleClass().add("table-view");

        TableColumn<Feedback, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("feedbackId"));
        idCol.setPrefWidth(50);
        TableColumn<Feedback, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        userCol.setPrefWidth(150);
        TableColumn<Feedback, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(80);
        TableColumn<Feedback, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        commentsCol.setPrefWidth(350);
        TableColumn<Feedback, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));
        dateCol.setPrefWidth(150);

        feedbackTable.getColumns().addAll(idCol, userCol, ratingCol, commentsCol, dateCol);
        feedbackTable.setItems(allFeedback);

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        // Refresh button for feedback (if new feedback comes in)
        Button refreshFeedbackBtn = new Button("Refresh Feedback");
        refreshFeedbackBtn.getStyleClass().addAll("button", "secondary-button");
        refreshFeedbackBtn.setOnAction(e -> {
            allFeedback.clear();
            allFeedback.addAll(DatabaseManager.loadFeedback());
            msgLabel.getStyleClass().setAll("message-label", "success");
            msgLabel.setText("Feedback data refreshed.");
        });

        contentPane.getChildren().addAll(heading, feedbackTable, refreshFeedbackBtn, msgLabel);
    }

    /**
     * Sets up the Dish Rating Management tab for admin.
     * @param contentPane The VBox to populate.
     */
    private void setupDishRatingManagement(VBox contentPane) {
        Label heading = new Label("Dish Ratings Overview");
        heading.getStyleClass().add("prompt-label");

        // Display average ratings for each menu item
        TableView<MenuItemAverageRating> avgRatingTable = new TableView<>();
        avgRatingTable.getStyleClass().add("table-view");

        TableColumn<MenuItemAverageRating, String> dishNameCol = new TableColumn<>("Dish Name");
        dishNameCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        dishNameCol.setPrefWidth(200);

        TableColumn<MenuItemAverageRating, Double> avgRatingCol = new TableColumn<>("Average Rating");
        avgRatingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        avgRatingCol.setPrefWidth(120);

        TableColumn<MenuItemAverageRating, Integer> totalRatingsCol = new TableColumn<>("Total Ratings");
        totalRatingsCol.setCellValueFactory(new PropertyValueFactory<>("totalRatings"));
        totalRatingsCol.setPrefWidth(120);

        avgRatingTable.getColumns().addAll(dishNameCol, avgRatingCol, totalRatingsCol);

        // Populate average ratings
        updateAverageDishRatings(avgRatingTable);

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        Button refreshRatingsBtn = new Button("Refresh Ratings");
        refreshRatingsBtn.getStyleClass().addAll("button", "secondary-button");
        refreshRatingsBtn.setOnAction(e -> {
            updateAverageDishRatings(avgRatingTable);
            msgLabel.getStyleClass().setAll("message-label", "success");
            msgLabel.setText("Dish rating data refreshed.");
        });

        contentPane.getChildren().addAll(heading, avgRatingTable, refreshRatingsBtn, msgLabel);
    }

    /**
     * Helper class for displaying average dish ratings in a TableView.
     */
    public static class MenuItemAverageRating {
        private String menuItemName;
        private double averageRating;
        private int totalRatings;

        public MenuItemAverageRating(String menuItemName, double averageRating, int totalRatings) {
            this.menuItemName = menuItemName;
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
        }

        public String getMenuItemName() { return menuItemName; }
        public double getAverageRating() { return averageRating; }
        public int getTotalRatings() { return totalRatings; }
    }

    /**
     * Updates the TableView with average dish ratings.
     */
    private void updateAverageDishRatings(TableView<MenuItemAverageRating> avgRatingTable) {
        ObservableList<MenuItemAverageRating> data = FXCollections.observableArrayList();
        Map<Integer, List<DishRating>> ratingsByMenuItem = allDishRatings.stream()
                .collect(Collectors.groupingBy(DishRating::getMenuItemId));

        for (MenuItem item : menuList) {
            List<DishRating> itemRatings = ratingsByMenuItem.getOrDefault(item.getId(), Collections.emptyList());
            double avg = itemRatings.stream().mapToInt(DishRating::getRating).average().orElse(0.0);
            data.add(new MenuItemAverageRating(item.getName(), avg, itemRatings.size()));
        }
        avgRatingTable.setItems(data);
    }


    /**
     * Sets up the Customer Order Placement tab.
     * @param contentPane The VBox to populate.
     */
    private void setupCustomerOrderPlacement(VBox contentPane) {
        Label heading = new Label("Place Your Order");
        heading.getStyleClass().add("prompt-label");

        // Menu items display
        ListView<MenuItem> menuListView = new ListView<>();
        menuListView.getStyleClass().add("list-view");
        menuListView.setItems(FXCollections.observableArrayList(menuList));
        menuListView.setCellFactory(lv -> new ListCell<MenuItem>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
            }
        });

        Button addToOrderBtn = new Button("Add to Order");
        addToOrderBtn.getStyleClass().add("button");

        // Current order display
        currentOrderListView.getStyleClass().add("list-view");
        currentOrderListView.setCellFactory(lv -> new ListCell<MenuItem>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName() + " (Rs." + String.format("%.2f", item.getPrice()) + ")");
            }
        });

        currentOrderLabel.getStyleClass().addAll("label", "prompt-label");
        Button removeFromOrderBtn = new Button("Remove Selected");
        removeFromOrderBtn.getStyleClass().add("button");
        Button clearOrderBtn = new Button("Clear Order");
        clearOrderBtn.getStyleClass().add("button");

        HBox currentOrderButtons = new HBox(10, removeFromOrderBtn, clearOrderBtn);
        currentOrderButtons.setAlignment(Pos.CENTER_LEFT);

        // Order completion
        Label discountLabel = new Label("Discount (%):");
        TextField discountField = new TextField("0");
        discountField.setPromptText("Enter discount percentage");
        discountField.getStyleClass().add("text-field");
        discountField.setMaxWidth(150);

        ComboBox<PaymentMethod> paymentMethodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodCombo.setValue(PaymentMethod.CASH); // Default
        paymentMethodCombo.getStyleClass().add("combo-box");

        Button placeOrderBtn = new Button("Place Order");
        placeOrderBtn.getStyleClass().add("button");

        customerOutput.getStyleClass().add("message-label");
        customerOutput.setWrapText(true);


        VBox leftPane = new VBox(10, new Label("Available Menu Items:"), menuListView, addToOrderBtn);
        VBox rightPane = new VBox(10, new Label("Your Current Order:"), currentOrderListView, currentOrderButtons,
                                   new Separator(), currentOrderLabel,
                                   new HBox(5, discountLabel, discountField),
                                   new HBox(5, new Label("Payment Method:"), paymentMethodCombo),
                                   placeOrderBtn, customerOutput);
        HBox mainContent = new HBox(20, leftPane, rightPane);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        // Actions
        addToOrderBtn.setOnAction(e -> {
            MenuItem selectedItem = menuListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                currentOrder.addItem(selectedItem);
                currentOrderListView.getItems().setAll(currentOrder.items);
                updateCurrentOrderLabel();
            }
        });

        removeFromOrderBtn.setOnAction(e -> {
            MenuItem selectedItem = currentOrderListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                currentOrder.items.remove(selectedItem);
                currentOrderListView.getItems().setAll(currentOrder.items);
                updateCurrentOrderLabel();
            }
        });

        clearOrderBtn.setOnAction(e -> {
            currentOrder = new Order(++orderCounter); // New order ID
            currentOrderListView.getItems().clear();
            updateCurrentOrderLabel();
            discountField.setText("0");
            customerOutput.setText("");
        });

        placeOrderBtn.setOnAction(e -> {
            if (currentOrder.items.isEmpty()) {
                customerOutput.getStyleClass().setAll("message-label", "warning");
                customerOutput.setText("Your order is empty. Please add items.");
                return;
            }

            try {
                double discountPercentage = Double.parseDouble(discountField.getText());
                if (discountPercentage < 0 || discountPercentage > 100) {
                    customerOutput.getStyleClass().setAll("message-label", "error");
                    customerOutput.setText("Discount percentage must be between 0 and 100.");
                    return;
                }
                currentOrder.discountApplied = currentOrder.getSubtotal() * (discountPercentage / 100.0);
            } catch (NumberFormatException ex) {
                customerOutput.getStyleClass().setAll("message-label", "error");
                customerOutput.setText("Invalid discount value. Please enter a number.");
                return;
            }

            currentOrder.status = OrderStatus.PLACED;
            currentOrder.paymentStatus = PaymentStatus.PENDING; // Initial payment status
            currentOrder.paymentMethod = paymentMethodCombo.getValue(); // Set selected payment method

            DatabaseManager.saveOrder(currentOrder);
            allOrders.add(currentOrder); // Add to the observable list for admin view

            customerOutput.getStyleClass().setAll("message-label", "success");
            customerOutput.setText("Order ID " + currentOrder.orderId + " placed successfully!\n" +
                                   "Total: Rs." + String.format("%.2f", currentOrder.getTotalWithGST()) +
                                   "\nPayment Method: " + currentOrder.getPaymentMethod().getDisplayValue());

            // Reset for a new order
            clearOrderBtn.fire();
        });

        contentPane.getChildren().addAll(heading, mainContent);
    }

    /**
     * Updates the label displaying the current order total.
     */
    private void updateCurrentOrderLabel() {
        double currentTotal = currentOrder.getSubtotal();
        currentOrderLabel.setText("Current Order Subtotal: Rs." + String.format("%.2f", currentTotal));
    }


    /**
     * Displays a "Thank you for visiting" message.
     * @param finalAmount The final billed amount.
     */
    private void showThankYouMessage(double finalAmount) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Billing Complete");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("alert");
        dialogPane.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label msg = new Label("Thank you for visiting Shield Restaurant!");
        msg.getStyleClass().add("prompt-label");
        Label amount = new Label("Total Billed: Rs." + String.format("%.2f", finalAmount));
        amount.getStyleClass().add("label");

        content.getChildren().addAll(msg, amount);
        dialogPane.getButtonTypes().add(ButtonType.OK); // Add an OK button
        dialog.showAndWait();
    }


    /**
     * Sets up the Customer Table Booking tab.
     * @param contentPane The VBox to populate.
     */
    private void setupCustomerBooking(VBox contentPane) {
        Label heading = new Label("Book a Table");
        heading.getStyleClass().add("prompt-label");

        GridPane bookingForm = new GridPane();
        bookingForm.setHgap(10);
        bookingForm.setVgap(10);
        bookingForm.setPadding(new Insets(10));

        // Assuming loggedInCustomerUsername is available
        Label customerIdLabel = new Label("Customer ID:");
        TextField customerIdField = new TextField(loggedInCustomerUsername != null ? loggedInCustomerUsername : "N/A");
        customerIdField.setEditable(false); // Customer ID is set by login
        customerIdField.getStyleClass().add("text-field");

        Label nameLabel = new Label("Your Name:");
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Enter your full name");
        customerNameField.getStyleClass().add("text-field");

        Label phoneLabel = new Label("Phone:");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        phoneField.getStyleClass().add("text-field");

        Label tableTypeLabel = new Label("Table Type:");
        ComboBox<TableType> tableTypeCombo = new ComboBox<>(FXCollections.observableArrayList(TableType.values()));
        tableTypeCombo.getStyleClass().add("combo-box");
        tableTypeCombo.setPromptText("Select Table Type");

        Label tableNumberLabel = new Label("Table Number:");
        TextField tableNumberField = new TextField();
        tableNumberField.setPromptText("e.g., 1-10 for selected type");
        tableNumberField.getStyleClass().add("text-field");

        Label bookingFeeLabel = new Label("Booking Fee (Rs.):");
        TextField bookingFeeField = new TextField("100.00"); // Default fee
        bookingFeeField.setEditable(false); // Fee is fixed
        bookingFeeField.getStyleClass().add("text-field");


        bookingForm.add(customerIdLabel, 0, 0); bookingForm.add(customerIdField, 1, 0);
        bookingForm.add(nameLabel, 0, 1); bookingForm.add(customerNameField, 1, 1);
        bookingForm.add(phoneLabel, 0, 2); bookingForm.add(phoneField, 1, 2);
        bookingForm.add(tableTypeLabel, 0, 3); bookingForm.add(tableTypeCombo, 1, 3);
        bookingForm.add(tableNumberLabel, 0, 4); bookingForm.add(tableNumberField, 1, 4);
        bookingForm.add(bookingFeeLabel, 0, 5); bookingForm.add(bookingFeeField, 1, 5);

        Button bookTableBtn = new Button("Book Table");
        bookTableBtn.getStyleClass().add("button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        // Action for booking
        bookTableBtn.setOnAction(e -> {
            String custId = loggedInCustomerUsername;
            String customerName = customerNameField.getText().trim();
            String phone = phoneField.getText().trim();
            TableType selectedTableType = tableTypeCombo.getValue();
            int tableNum;
            double bookingFee;

            if (custId == null) {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("You must be logged in to book a table.");
                return;
            }
            if (customerName.isEmpty() || phone.isEmpty() || selectedTableType == null || tableNumberField.getText().isEmpty()) {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Please fill in all booking details.");
                return;
            }

            try {
                tableNum = Integer.parseInt(tableNumberField.getText());
                if (tableNum <= 0) {
                     msgLabel.getStyleClass().setAll("message-label", "error");
                     msgLabel.setText("Table number must be positive.");
                     return;
                }
                bookingFee = Double.parseDouble(bookingFeeField.getText());
            } catch (NumberFormatException ex) {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Invalid number for Table Number or Booking Fee.");
                return;
            }

            // Basic validation for table number based on type (e.g., up to 10 tables of each type)
            // You might want a more sophisticated table availability system
            if (tableNum > 10) { // Example max, adjust as needed
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Table numbers typically go up to 10 for each type. Please verify.");
            }

            TableBooking newBooking = new TableBooking(custId, customerName, phone, selectedTableType, tableNum, bookingFee);
            DatabaseManager.saveTableBooking(newBooking);
            allBookings.add(newBooking); // Add to observable list for admin view

            msgLabel.getStyleClass().setAll("message-label", "success");
            msgLabel.setText("Table booked successfully!\n" + newBooking.toDetailedString());

            // Clear form
            customerNameField.clear();
            phoneField.clear();
            tableTypeCombo.getSelectionModel().clearSelection();
            tableNumberField.clear();
        });

        contentPane.getChildren().addAll(heading, bookingForm, bookTableBtn, msgLabel);
    }

    /**
     * Sets up the "View My Orders" tab for logged-in customers.
     * @param contentPane The VBox to populate.
     */
    private void setupViewMyOrders(VBox contentPane) {
        Label heading = new Label("Your Order History");
        heading.getStyleClass().add("prompt-label");

        TextArea orderDetailsArea = new TextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setWrapText(true);
        orderDetailsArea.getStyleClass().add("text-area");
        orderDetailsArea.setPrefHeight(200);

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        // Filter orders based on the logged-in user
        // IMPORTANT: The Order class does not currently store customerUsername.
        // To properly filter orders by the logged-in customer, the Order class
        // and the 'orders' table in the database need to be updated to include a
        // 'customer_username' field. For now, this will display ALL orders.
        // A placeholder for future implementation:
        ObservableList<Order> customerOrders = FXCollections.observableArrayList(
            allOrders.stream()
                // Assuming 'Order' class is updated with 'customerUsername' field and its getter:
                // .filter(order -> order.getCustomerUsername() != null && order.getCustomerUsername().equals(loggedInCustomerUsername))
                .collect(Collectors.toList())
        );

        // A list view to select orders
        ListView<Order> orderListView = new ListView<>(customerOrders);
        orderListView.getStyleClass().add("list-view");
        orderListView.setPrefHeight(300);

        orderListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                orderDetailsArea.setText(newSelection.toDetailedString());
            } else {
                orderDetailsArea.clear();
            }
        });

        if (loggedInCustomerUsername == null) {
            msgLabel.getStyleClass().setAll("message-label", "warning");
            msgLabel.setText("Please log in to view your orders.");
            contentPane.getChildren().addAll(heading, msgLabel);
        } else {
            // Re-filter orders if customer logs in/out, or if the Order class is updated.
            customerOrders.setAll(allOrders.stream()
                // Assuming 'Order' class is updated with 'customerUsername' field and its getter:
                // .filter(order -> order.getCustomerUsername() != null && order.getCustomerUsername().equals(loggedInCustomerUsername))
                // For now, it will simply show all orders if there's no direct link in the 'Order' class.
                .collect(Collectors.toList())
            );

            if (customerOrders.isEmpty()) {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("You have no past orders.");
            } else {
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Select an order to view details.");
            }
            contentPane.getChildren().addAll(heading, orderListView, orderDetailsArea, msgLabel);
        }
    }


    /**
     * Sets up the "Provide Feedback" tab for logged-in customers.
     * @param contentPane The VBox to populate.
     */
    private void setupProvideFeedback(VBox contentPane) {
        Label heading = new Label("Provide Your Feedback");
        heading.getStyleClass().add("prompt-label");

        if (loggedInCustomerUsername == null) {
            Label loginPrompt = new Label("Please log in to provide feedback.");
            loginPrompt.getStyleClass().addAll("message-label", "warning");
            contentPane.getChildren().addAll(heading, loginPrompt);
            return;
        }

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

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
        commentsArea.setPromptText("Enter your feedback here...");
        commentsArea.setWrapText(true);
        commentsArea.getStyleClass().add("text-area");
        commentsArea.setPrefRowCount(5);

        formGrid.add(ratingLabel, 0, 0); formGrid.add(ratingSlider, 1, 0);
        formGrid.add(commentsLabel, 0, 1); formGrid.add(commentsArea, 1, 1);

        Button submitFeedbackBtn = new Button("Submit Feedback");
        submitFeedbackBtn.getStyleClass().add("button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        submitFeedbackBtn.setOnAction(e -> {
            int rating = (int) ratingSlider.getValue();
            String comments = commentsArea.getText().trim();

            if (comments.isEmpty()) {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please enter some comments.");
                return;
            }

            Feedback newFeedback = new Feedback(0, loggedInCustomerUsername, rating, comments, LocalDateTime.now());
            if (DatabaseManager.saveFeedback(newFeedback)) {
                allFeedback.add(newFeedback); // Add to local list for admin view
                msgLabel.getStyleClass().setAll("message-label", "success");
                msgLabel.setText("Thank you for your feedback!");
                commentsArea.clear(); // Clear form
                ratingSlider.setValue(3);
            } else {
                msgLabel.getStyleClass().setAll("message-label", "error");
                msgLabel.setText("Failed to submit feedback. Please try again.");
            }
        });

        contentPane.getChildren().addAll(heading, formGrid, submitFeedbackBtn, msgLabel);
    }

    /**
     * Sets up the "Rate Dishes" tab for logged-in customers.
     * @param contentPane The VBox to populate.
     */
    private void setupRateDishes(VBox contentPane) {
        Label heading = new Label("Rate Your Favorite Dishes");
        heading.getStyleClass().add("prompt-label");

        if (loggedInCustomerUsername == null) {
            Label loginPrompt = new Label("Please log in to rate dishes.");
            loginPrompt.getStyleClass().addAll("message-label", "warning");
            contentPane.getChildren().addAll(heading, loginPrompt);
            return;
        }

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        Label dishLabel = new Label("Select Dish:");
        ComboBox<MenuItem> dishCombo = new ComboBox<>();
        dishCombo.setItems(FXCollections.observableArrayList(menuList)); // Populate with all menu items
        dishCombo.setPromptText("Choose a dish");
        dishCombo.getStyleClass().add("combo-box");
        dishCombo.setCellFactory(lv -> new ListCell<MenuItem>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        dishCombo.setButtonCell(new ListCell<MenuItem>() { // Display selected item's name
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        Label ratingLabel = new Label("Your Rating (1-5):");
        Slider ratingSlider = new Slider(1, 5, 3); // Min, Max, Default
        ratingSlider.setBlockIncrement(1);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.getStyleClass().add("slider");

        formGrid.add(dishLabel, 0, 0); formGrid.add(dishCombo, 1, 0);
        formGrid.add(ratingLabel, 0, 1); formGrid.add(ratingSlider, 1, 1);

        Button submitRatingBtn = new Button("Submit Rating");
        submitRatingBtn.getStyleClass().add("button");

        Label msgLabel = new Label("");
        msgLabel.getStyleClass().add("message-label");

        submitRatingBtn.setOnAction(e -> {
            MenuItem selectedDish = dishCombo.getValue();
            int rating = (int) ratingSlider.getValue();

            if (selectedDish == null) {
                msgLabel.getStyleClass().setAll("message-label", "warning");
                msgLabel.setText("Please select a dish to rate.");
                return;
            }

            DishRating newRating = new DishRating(0, selectedDish.getId(), loggedInCustomerUsername, rating, LocalDateTime.now());
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
    }


    public static void main(String[] args) {
        launch(args);
    }
}
