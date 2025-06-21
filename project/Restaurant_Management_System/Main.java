package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image; // Keep Image import for now, as it might be transitively used, but remove direct usage.
import javafx.scene.image.ImageView; // Keep ImageView import for now, but remove direct usage.
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty; // For totalCol in orders
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.text.Text; // Import Text for word wrapping in TableView
import javafx.geometry.HPos; // Import HPos

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL; // Added for resource loading
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom; // For OTP generation

// New imports required for updated features
import application.DishRating;
import application.User;
import application.UserRole;
import application.CreditCard;
import javafx.beans.binding.Bindings; // Import Bindings for advanced property binding


public class Main extends Application {

    private Stage primaryStage;
    private User currentUser; // Stores the currently logged-in user
    private Scene loginScene;
    private Scene registerScene;
    private Scene mainScene; // mainScene will be created and set after successful login

    // ObservableLists for UI elements that might be shared or need frequent updates
    private ObservableList<MenuItem> allMenuItems = FXCollections.observableArrayList(); // Renamed from menuItems for clarity, used by both admin and customer for menu display/management
    private ObservableList<Order> allOrders = FXCollections.observableArrayList();
    private ObservableList<TableBooking> allTableBookings = FXCollections.observableArrayList();
    private ObservableList<Feedback> allFeedback = FXCollections.observableArrayList();
    private ObservableList<DishRating> allDishRatings = FXCollections.observableArrayList(); // For dish rating viewing by admin/customer

    // For current order being built by customer
    private Map<MenuItem, Integer> currentOrderItemQuantities = new HashMap<>();

    // Constant to hold the stylesheet URL string
    private String cssStylesheet;

    // Constants for payment (moved from old Main to top level for easy access)
    private static final double GST_RATE = 0.05; // 5% GST
    private static final double BOOKING_FEE_PER_HOUR = 50.0; // Rs. 50 per hour for table booking


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Restaurant Management System - SHIELD"); // Updated title

        // Load CSS
        URL cssResource = getClass().getResource("style.css");
        if (cssResource != null) {
            cssStylesheet = cssResource.toExternalForm(); // Store the URL string
            // Initial scene to load the CSS before showing login, ensures styles are available for dialogs
            Scene tempScene = new Scene(new StackPane());
            tempScene.getStylesheets().add(cssStylesheet);
            primaryStage.setScene(tempScene);
        } else {
            System.err.println("CSS file not found: style.css");
        }

        // Initialize Database (create tables, insert admin, menu items)
        DatabaseManager.initializeDatabase();

        // Create initial scenes (login and register)
        loginScene = createLoginScene(cssStylesheet);
        registerScene = createRegisterScene(cssStylesheet);

        primaryStage.setScene(loginScene); // Start with the login screen
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Displays a custom alert dialog.
     * Removed image loading for alert icons.
     * @param title The title of the dialog.
     * @param message The message content of the dialog.
     * @param alertType The type of alert (INFORMATION, WARNING, ERROR).
     */
    private void showCustomAlertDialog(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);

        // Apply custom styling directly to the DialogPane and add the stylesheet
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("alert"); // Apply general alert style
        if (cssStylesheet != null) {
            dialogPane.getStylesheets().add(cssStylesheet); // Apply the stylesheet to the dialog pane
        }

        // Removed all alert icon loading logic
        alert.setGraphic(null); // Explicitly ensure no graphic is set

        // Get the current window and center the dialog relative to it
        Window owner = primaryStage.getScene() != null ? primaryStage.getScene().getWindow() : null;
        if (owner != null) {
            alert.initOwner(owner);
            // Use Platform.runLater to ensure dialog's width/height are calculated before positioning
            Platform.runLater(() -> {
                Window alertWindow = dialogPane.getScene().getWindow();
                double x = owner.getX() + (owner.getWidth() - alertWindow.getWidth()) / 2;
                double y = owner.getY() + (owner.getHeight() - alertWindow.getHeight()) / 2;
                alertWindow.setX(x);
                alertWindow.setY(y);
            });
        }
        alert.showAndWait();
    }


    /**
     * Creates the login scene for the application.
     * @param css The path to the CSS file for styling.
     * @return The Scene object for the login interface.
     */
    private Scene createLoginScene(String css) {
        // Main layout container for login, styled with CSS
        VBox loginLayout = new VBox(20); // Spacing between elements
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(25));
        loginLayout.getStyleClass().add("login-register-box"); // Apply styling for the login/register box

        Label titleLabel = new Label("Welcome to SHIELD Restaurant"); // Updated title text
        titleLabel.getStyleClass().add("h1-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field-custom"); // Apply custom text field style
        usernameField.setMaxWidth(300);

        // Password field with show/hide functionality
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordField.setMaxWidth(300);

        TextField passwordShowField = new TextField();
        passwordShowField.setPromptText("Password");
        passwordShowField.getStyleClass().add("text-field-custom");
        passwordShowField.setMaxWidth(300);
        passwordShowField.setVisible(false); // Initially hidden

        StackPane passwordStack = new StackPane(passwordField, passwordShowField);
        passwordStack.setMaxWidth(300);

        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.getStyleClass().add("checkbox-custom");
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordShowField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordShowField.setVisible(true);
            } else {
                passwordField.setText(passwordShowField.getText());
                passwordField.setVisible(true);
                passwordShowField.setVisible(false);
            }
        });

        // Synchronize text changes between the two fields
        passwordField.textProperty().bindBidirectional(passwordShowField.textProperty());


        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button"); // Apply button styling
        loginButton.setMaxWidth(300);
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = showPasswordCheckbox.isSelected() ? passwordShowField.getText() : passwordField.getText();

            currentUser = DatabaseManager.authenticateUser(username, password);

            if (currentUser != null) {
                showCustomAlertDialog("Login Successful", "Welcome, " + currentUser.getFullName() + "!", Alert.AlertType.INFORMATION);
                showMainApplication(); // Call new method to show main app
            } else {
                showCustomAlertDialog("Login Failed", "Invalid username or password.", Alert.AlertType.ERROR);
            }
        });

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here.");
        registerLink.getStyleClass().add("body-text"); // Apply body text style for hyperlinks
        registerLink.setOnAction(e -> primaryStage.setScene(registerScene));

        loginLayout.getChildren().addAll(titleLabel, usernameField, passwordStack, showPasswordCheckbox, loginButton, registerLink);

        // Root container for the scene, applying background image
        StackPane root = new StackPane();
        root.getStyleClass().add("login-container"); // Apply background color/gradient from CSS
        root.getChildren().add(loginLayout);

        Scene scene = new Scene(root, 1000, 700);
        if (cssStylesheet != null) {
            scene.getStylesheets().add(cssStylesheet); // IMPORTANT: Apply CSS to the new scene
        }
        return scene;
    }

    /**
     * Creates the registration scene for new users.
     * @param css The path to the CSS file for styling.
     * @return The Scene object for the registration interface.
     */
    private Scene createRegisterScene(String css) {
        VBox registerLayout = new VBox(15);
        registerLayout.setAlignment(Pos.CENTER);
        registerLayout.setPadding(new Insets(25));
        registerLayout.getStyleClass().add("login-register-box");

        Label titleLabel = new Label("Register New Account");
        titleLabel.getStyleClass().add("h1-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field-custom");
        usernameField.setMaxWidth(300);

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.getStyleClass().add("text-field-custom");
        fullNameField.setMaxWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field-custom");
        emailField.setMaxWidth(300);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        phoneField.getStyleClass().add("text-field-custom");
        phoneField.setMaxWidth(300);

        // Password field with show/hide functionality for registration password
        PasswordField regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password");
        regPasswordField.getStyleClass().add("text-field-custom");
        regPasswordField.setMaxWidth(300);

        TextField regPasswordShowField = new TextField();
        regPasswordShowField.setPromptText("Password");
        regPasswordShowField.getStyleClass().add("text-field-custom");
        regPasswordShowField.setMaxWidth(300);
        regPasswordShowField.setVisible(false);

        StackPane regPasswordStack = new StackPane(regPasswordField, regPasswordShowField);
        regPasswordStack.setMaxWidth(300);

        // Confirm Password field with show/hide functionality
        PasswordField regConfirmPasswordField = new PasswordField();
        regConfirmPasswordField.setPromptText("Confirm Password");
        regConfirmPasswordField.getStyleClass().add("text-field-custom");
        regConfirmPasswordField.setMaxWidth(300);

        TextField regConfirmPasswordShowField = new TextField();
        regConfirmPasswordShowField.setPromptText("Confirm Password");
        regConfirmPasswordShowField.getStyleClass().add("text-field-custom");
        regConfirmPasswordShowField.setMaxWidth(300);
        regConfirmPasswordShowField.setVisible(false);

        StackPane regConfirmPasswordStack = new StackPane(regConfirmPasswordField, regConfirmPasswordShowField);
        regConfirmPasswordStack.setMaxWidth(300);

        CheckBox showPasswordCheckbox = new CheckBox("Show Passwords");
        showPasswordCheckbox.getStyleClass().add("checkbox-custom");
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                regPasswordShowField.setText(regPasswordField.getText());
                regPasswordField.setVisible(false);
                regPasswordShowField.setVisible(true);

                regConfirmPasswordShowField.setText(regConfirmPasswordField.getText());
                regConfirmPasswordField.setVisible(false);
                regConfirmPasswordShowField.setVisible(true);
            } else {
                regPasswordField.setText(regPasswordShowField.getText());
                regPasswordField.setVisible(true);
                regPasswordShowField.setVisible(false);

                regConfirmPasswordField.setText(regConfirmPasswordShowField.getText());
                regConfirmPasswordField.setVisible(true);
                regConfirmPasswordShowField.setVisible(false);
            }
        });
        // Synchronize text changes between the fields
        regPasswordField.textProperty().bindBidirectional(regPasswordShowField.textProperty());
        regConfirmPasswordField.textProperty().bindBidirectional(regConfirmPasswordShowField.textProperty());

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("button-primary"); // Use primary button style
        registerButton.setMaxWidth(300);
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = showPasswordCheckbox.isSelected() ? regPasswordShowField.getText() : regPasswordField.getText();
            String confirmPassword = showPasswordCheckbox.isSelected() ? regConfirmPasswordShowField.getText() : regConfirmPasswordField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
                showCustomAlertDialog("Registration Failed", "Username, Password, Confirm Password, and Full Name are required.", Alert.AlertType.WARNING);
                return;
            }

            if (!password.equals(confirmPassword)) {
                showCustomAlertDialog("Registration Failed", "Passwords do not match.", Alert.AlertType.WARNING);
                return;
            }

            if (DatabaseManager.getUserByUsername(username) != null) {
                showCustomAlertDialog("Registration Failed", "Username already exists. Please choose a different one.", Alert.AlertType.ERROR);
                return;
            }

            // Trigger OTP verification before proceeding with registration
            showOtpVerificationDialog(() -> {
                String hashedPassword = PasswordUtil.hashPassword(password);
                if (hashedPassword == null) {
                    showCustomAlertDialog("Registration Failed", "Error hashing password. Please try again.", Alert.AlertType.ERROR);
                    return;
                }

                // Generate a UUID for the new user
                String newUserId = UUID.randomUUID().toString();
                User newUser = new User(newUserId, username, hashedPassword, fullName, email, phone, UserRole.CUSTOMER);

                if (DatabaseManager.addUser(newUser)) {
                    showCustomAlertDialog("Registration Successful", "Account created for " + username + ". You can now log in.", Alert.AlertType.INFORMATION);
                    primaryStage.setScene(loginScene); // Go back to login after successful registration
                } else {
                    showCustomAlertDialog("Registration Failed", "Could not register user. Please try again later.", Alert.AlertType.ERROR);
                }
            });
        });

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("button-secondary"); // Use secondary button style
        backButton.setMaxWidth(300);
        backButton.setOnAction(e -> primaryStage.setScene(loginScene));

        registerLayout.getChildren().addAll(titleLabel, usernameField, fullNameField, emailField, phoneField,
                regPasswordStack, regConfirmPasswordStack, showPasswordCheckbox,
                registerButton, backButton);

        StackPane root = new StackPane();
        root.getStyleClass().add("register-container"); // Apply background color/gradient from CSS
        root.getChildren().add(registerLayout);

        Scene scene = new Scene(root, 1000, 700);
        if (cssStylesheet != null) {
            scene.getStylesheets().add(cssStylesheet); // IMPORTANT: Apply CSS to the new scene
        }
        return scene;
    }

    /**
     * Handles the user login process.
     * Authenticates credentials and navigates to the main scene if successful.
     * @param username The entered username.
     * @param password The entered password.
     */
    private void showMainApplication() {
        // Load initial data for the main scene based on user role
        allMenuItems.setAll(DatabaseManager.getAllMenuItems());
        allOrders.setAll(DatabaseManager.getAllOrders());
        allTableBookings.setAll(DatabaseManager.getAllTableBookings());
        allFeedback.setAll(DatabaseManager.getAllFeedback());
        allDishRatings.setAll(DatabaseManager.getAllDishRatings()); // For dish rating viewing by admin/customer

        // Reset current order
        currentOrderItemQuantities.clear();

        // Create and set the main scene after successful login
        mainScene = createMainScene(cssStylesheet);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }

    /**
     * Creates the main application scene with a TabPane for different functionalities.
     * The tabs displayed depend on the current user's role.
     * Removed logo from top bar.
     * @param css The path to the CSS file for styling.
     * @return The Scene object for the main application interface.
     */
    private Scene createMainScene(String css) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-app-background"); // Apply general background style

        // Top section with welcome message and logout button
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        // Removed logo and image view
        Label appName = new Label("SHIELD Restaurant"); // Keep app name as text
        appName.getStyleClass().add("app-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Spacer to push elements to ends

        Label welcomeLabel = new Label("Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole().getDisplayValue() + ")");
        welcomeLabel.getStyleClass().add("h3-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button-danger");
        logoutButton.setOnAction(e -> {
            // Clear current user and return to login screen
            currentUser = null;
            showCustomAlertDialog("Logged Out", "You have been successfully logged out.", Alert.AlertType.INFORMATION);
            primaryStage.setScene(loginScene);
            primaryStage.centerOnScreen();
        });

        topBar.getChildren().addAll(appName, spacer, welcomeLabel, logoutButton); // Removed logoView
        root.setTop(topBar);

        // Center content with TabPane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("custom-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Prevent tabs from being closed by user

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            tabPane.getTabs().addAll(
                    new Tab("Order Food", createOrderFoodTab()),
                    new Tab("My Orders", createMyOrdersTab()),
                    new Tab("Book Table", createTableBookingTab()),
                    new Tab("My Bookings", createMyBookingsTab()),
                    new Tab("Feedback & Ratings", createFeedbackAndRatingsTab())
            );
        } else if (currentUser.getRole() == UserRole.ADMIN) {
            tabPane.getTabs().addAll(
                    new Tab("Manage Menu", createAdminMenuManagementTab()),
                    new Tab("Manage Orders", createAdminOrderManagementTab()),
                    new Tab("Manage Bookings", createAdminBookingManagementTab()),
                    new Tab("Manage Users", createAdminUserManagementTab()),
                    new Tab("View Feedback", createAdminFeedbackTab()),
                    new Tab("View Dish Ratings", createAdminDishRatingsTab())
            );
        }

        // Add a common Profile tab for both roles
        tabPane.getTabs().add(new Tab("Profile", createProfileView()));

        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 800); // Set preferred size for main scene
        if (cssStylesheet != null) {
            scene.getStylesheets().add(cssStylesheet); // Apply CSS to the new scene
        }
        return scene;
    }


    /**
     * Helper to update the current order summary displayed in the "Order Food" tab.
     * This is now a private method of the Main class to improve accessibility.
     */
    private void updateCurrentOrderSummary(ListView<String> orderListView, Label subtotalLabel, Label gstLabel, Label totalLabel) {
        orderListView.getItems().clear();
        double subtotal = 0;
        for (Map.Entry<MenuItem, Integer> entry : currentOrderItemQuantities.entrySet()) {
            MenuItem item = entry.getKey();
            Integer quantity = entry.getValue();
            orderListView.getItems().add(String.format("%s x %d (Rs.%.2f each)", item.getName(), quantity, item.getPrice()));
            subtotal += item.getPrice() * quantity;
        }

        double gstAmount = subtotal * GST_RATE;
        double total = subtotal + gstAmount;

        subtotalLabel.setText("Subtotal: Rs." + String.format("%.2f", subtotal));
        gstLabel.setText("GST (5%): Rs." + String.format("%.2f", gstAmount));
        totalLabel.setText("Total: Rs." + String.format("%.2f", total));

        if (currentOrderItemQuantities.isEmpty()) {
            orderListView.setPlaceholder(new Label("No items in current order."));
        } else {
            orderListView.setPlaceholder(null);
        }
    }


    /**
     * Creates the "Order Food" tab content for customers.
     * Allows customers to browse menu items and add them to their current order.
     * Removed image display for menu items.
     */
    private ScrollPane createOrderFoodTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Place New Order");
        titleLabel.getStyleClass().add("h1-label");

        // Menu items display
        FlowPane menuFlowPane = new FlowPane();
        menuFlowPane.setHgap(15);
        menuFlowPane.setVgap(15);
        menuFlowPane.setAlignment(Pos.CENTER);
        menuFlowPane.setPadding(new Insets(10));

        // This list is updated on entry to the tab and on cart changes.
        // It reflects the current stock.
        allMenuItems.setAll(DatabaseManager.getAllMenuItems());


        // Current Order Summary
        VBox orderSummaryBox = new VBox(10);
        orderSummaryBox.getStyleClass().add("section-box"); // Apply section box style
        orderSummaryBox.setPadding(new Insets(15));
        orderSummaryBox.setPrefWidth(400); // Fixed width for order summary
        orderSummaryBox.setAlignment(Pos.TOP_LEFT);

        Label orderSummaryTitle = new Label("Your Current Order");
        orderSummaryTitle.getStyleClass().add("h2-label");

        ListView<String> orderListView = new ListView<>();
        orderListView.setCellFactory(lv -> new ListCell<String>() {
            private final HBox hbox = new HBox(10);
            private final Label itemDetailsLabel = new Label();
            private final Button removeButton = new Button("Remove");

            {
                removeButton.getStyleClass().add("button-danger-small");
                removeButton.setOnAction(event -> {
                    String itemText = getItem(); // e.g., "Burger x 2 (Rs.200.00)"
                    // Parse item name from the string to find the MenuItem object
                    if (itemText != null && !itemText.isEmpty()) {
                        String itemName = itemText.substring(0, itemText.indexOf(" x "));
                        MenuItem itemToRemove = allMenuItems.stream()
                                .filter(mi -> mi.getName().equals(itemName))
                                .findFirst()
                                .orElse(null);

                        if (itemToRemove != null) {
                            currentOrderItemQuantities.computeIfPresent(itemToRemove, (item, count) -> {
                                if (count > 1) {
                                    return count - 1; // Decrease quantity
                                }
                                return null; // Remove item if quantity becomes 0
                            });

                            // Return stock to DB for the single item removed
                            MenuItem itemInDb = DatabaseManager.getMenuItemById(itemToRemove.getId());
                            if (itemInDb != null) {
                                itemInDb.setStock(itemInDb.getStock() + 1);
                                DatabaseManager.updateMenuItem(itemInDb);
                                allMenuItems.setAll(DatabaseManager.getAllMenuItems()); // Refresh menu for stock update
                            }
                            // Call updateCurrentOrderSummary directly from here
                            updateCurrentOrderSummary(orderListView, (Label) orderSummaryBox.getChildren().get(2), (Label) orderSummaryBox.getChildren().get(3), (Label) orderSummaryBox.getChildren().get(4));
                        }
                    }
                });
                hbox.getChildren().addAll(itemDetailsLabel, removeButton);
                hbox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(itemDetailsLabel, Priority.ALWAYS); // Allow name to grow
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    itemDetailsLabel.setText(item);
                    setGraphic(hbox);
                }
            }
        });
        VBox.setVgrow(orderListView, Priority.ALWAYS);


        // Labels for order totals
        Label subtotalLabel = new Label("Subtotal: Rs.0.00");
        subtotalLabel.getStyleClass().add("body-text");

        Label gstLabel = new Label("GST (5%): Rs.0.00");
        gstLabel.getStyleClass().add("body-text");

        Label totalLabel = new Label("Total: Rs.0.00");
        totalLabel.getStyleClass().add("h3-label");


        // Initial update of totals (in case items are already in currentOrderItems on tab load)
        updateCurrentOrderSummary(orderListView, subtotalLabel, gstLabel, totalLabel);


        Button placeOrderButton = new Button("Place Order");
        placeOrderButton.getStyleClass().add("button-primary");
        placeOrderButton.setMaxWidth(Double.MAX_VALUE);
        placeOrderButton.setOnAction(e -> showPaymentDialogForOrder());


        orderSummaryBox.getChildren().addAll(orderSummaryTitle, orderListView, subtotalLabel, gstLabel, totalLabel, placeOrderButton);

        HBox mainContent = new HBox(20); // Spacing between menu and order summary
        mainContent.setPadding(new Insets(0, 20, 20, 20)); // Padding around the content
        mainContent.getChildren().addAll(menuFlowPane, orderSummaryBox); // Changed to menuFlowPane
        HBox.setHgrow(menuFlowPane, Priority.ALWAYS); // Allow menu area to grow


        // Populate menu items (removed image display)
        for (MenuItem item : allMenuItems) {
            VBox itemCard = new VBox(10);
            itemCard.getStyleClass().add("menu-item-card");
            itemCard.setAlignment(Pos.CENTER);
            itemCard.setPrefWidth(180);

            // Removed ImageView and image loading logic
            // Instead, you could use a simple text or a shape if desired, or nothing.

            Label nameLabel = new Label(item.getName());
            nameLabel.getStyleClass().add("menu-item-name");
            nameLabel.setWrapText(true);
            nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Label priceLabel = new Label(String.format("Rs.%.2f", item.getPrice()));
            priceLabel.getStyleClass().add("menu-item-price");

            // Display average rating
            double avgRating = DatabaseManager.getAverageRatingForMenuItem(item.getId());
            Label ratingLabel;
            if (avgRating > 0) {
                ratingLabel = new Label(String.format("Rating: %.1f ★", avgRating));
                ratingLabel.getStyleClass().add("menu-item-rating");
            } else {
                ratingLabel = new Label("No ratings yet");
                ratingLabel.getStyleClass().add("body-text");
            }

            Button addToCartButton = new Button("Add to Cart");
            addToCartButton.getStyleClass().add("button-add-to-cart");
            // Disable button if out of stock
            addToCartButton.setDisable(item.getStock() == 0);


            // Modified action to add one item at a time and deduct stock immediately
            addToCartButton.setOnAction(e -> {
                // Re-fetch item to ensure latest stock
                MenuItem itemInDb = DatabaseManager.getMenuItemById(item.getId());
                if (itemInDb != null && itemInDb.getStock() > 0) { // Check if at least one item is in stock
                    currentOrderItemQuantities.merge(itemInDb, 1, Integer::sum); // Add 1 to quantity
                    // Deduct stock in DB
                    itemInDb.setStock(itemInDb.getStock() - 1);
                    DatabaseManager.updateMenuItem(itemInDb); // Update stock in DB
                    allMenuItems.setAll(DatabaseManager.getAllMenuItems()); // Refresh all menu items to reflect new stock
                    showCustomAlertDialog("Item Added", item.getName() + " added to your cart.", Alert.AlertType.INFORMATION);
                    updateCurrentOrderSummary(orderListView, subtotalLabel, gstLabel, totalLabel);
                } else {
                    showCustomAlertDialog("Out of Stock", item.getName() + " is currently out of stock.", Alert.AlertType.WARNING);
                }
            });

            // Removed imageView from children.
            itemCard.getChildren().addAll(nameLabel, priceLabel, ratingLabel, addToCartButton);
            menuFlowPane.getChildren().add(itemCard);
        }

        ScrollPane menuScrollPane = new ScrollPane(menuFlowPane);
        menuScrollPane.setFitToWidth(true);
        menuScrollPane.getStyleClass().add("scroll-pane-custom"); // Apply scroll pane style
        HBox.setHgrow(menuScrollPane, Priority.ALWAYS); // Allow menu area to grow

        layout.getChildren().addAll(titleLabel, mainContent);

        ScrollPane mainScrollPane = new ScrollPane(layout);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.getStyleClass().add("scroll-pane-custom");
        return mainScrollPane;
    }

    /**
     * Helper to check if a string is a valid URL for image loading.
     * This method is no longer strictly needed as images are removed.
     */
    private boolean isValidImageUrl(String urlString) {
        try {
            new URL(urlString).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Shows the payment dialog for placing a food order.
     */
    private void showPaymentDialogForOrder() {
        if (currentOrderItemQuantities.isEmpty()) {
            showCustomAlertDialog("Order Empty", "Please add items to your cart before placing an order.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Order and Payment");
        dialog.setHeaderText("Choose Payment Method");
        dialog.getDialogPane().getStyleClass().add("alert"); // Apply custom alert style
        if (cssStylesheet != null) { // Apply CSS to this dialog as well
            dialog.getDialogPane().getStylesheets().add(cssStylesheet);
        }

        double subtotal = currentOrderItemQuantities.entrySet().stream()
                                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                                .sum();
        double gstAmount = subtotal * GST_RATE;
        double finalTotal = subtotal + gstAmount;

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER_LEFT);

        Label orderSummaryLabel = new Label(
                "Order Subtotal: Rs." + String.format("%.2f", subtotal) + "\n" +
                "GST (5%): Rs." + String.format("%.2f", gstAmount) + "\n" +
                "Total Payable: Rs." + String.format("%.2f", finalTotal)
        );
        orderSummaryLabel.getStyleClass().add("h3-label");

        // Payment method selection
        Label paymentMethodLabel = new Label("Select Payment Method:");
        paymentMethodLabel.getStyleClass().add("body-text");
        ComboBox<PaymentMethod> paymentMethodComboBox = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.getStyleClass().add("combo-box-custom");
        // Default to Credit Card, ensuring card details box shows if selected
        paymentMethodComboBox.getSelectionModel().select(PaymentMethod.CREDIT_CARD);

        VBox cardDetailsBox = new VBox(10);
        cardDetailsBox.setPadding(new Insets(10));
        cardDetailsBox.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label savedCardsLabel = new Label("Choose Saved Card:");
        savedCardsLabel.getStyleClass().add("body-text");
        ComboBox<CreditCard> savedCardComboBox = new ComboBox<>();
        savedCardComboBox.getStyleClass().add("combo-box-custom");
        // Convert CreditCard object to string for display in ComboBox
        savedCardComboBox.setConverter(new javafx.util.StringConverter<CreditCard>() {
            @Override
            public String toString(CreditCard card) {
                return card != null ? card.getCardType() + " ending in " + card.getLastFourDigits() : "";
            }
            @Override
            public CreditCard fromString(String string) {
                return null; // Not used for selection
            }
        });

        // Populate saved cards for the current user
        if (currentUser != null) {
            savedCardComboBox.setItems(FXCollections.observableArrayList(DatabaseManager.getSavedCreditCards(currentUser.getUserId())));
        }

        Button addNewCardButton = new Button("Add New Card");
        addNewCardButton.getStyleClass().add("button-secondary");
        // This button's action will show a separate dialog for adding a card
        addNewCardButton.setOnAction(e -> showAddCreditCardDialog(savedCardComboBox.getItems())); // Pass the observable list to update it


        cardDetailsBox.getChildren().addAll(savedCardsLabel, savedCardComboBox, addNewCardButton);

        // Toggle visibility of card details based on payment method
        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCardPayment = (newVal == PaymentMethod.CREDIT_CARD || newVal == PaymentMethod.DEBIT_CARD || newVal == PaymentMethod.ONLINE_PAYMENT);
            cardDetailsBox.setVisible(isCardPayment);
            cardDetailsBox.setManaged(isCardPayment); // Ensure it takes up space only when visible

            if (isCardPayment && savedCardComboBox.getItems().isEmpty()) {
                showCustomAlertDialog("No Saved Cards", "You have no saved cards. Please add one.", Alert.AlertType.INFORMATION);
            }
        });

        // Initial state for card details if default is card type
        boolean isInitialCardPayment = (paymentMethodComboBox.getSelectionModel().getSelectedItem() == PaymentMethod.CREDIT_CARD ||
                                        paymentMethodComboBox.getSelectionModel().getSelectedItem() == PaymentMethod.DEBIT_CARD ||
                                        paymentMethodComboBox.getSelectionModel().getSelectedItem() == PaymentMethod.ONLINE_PAYMENT);
        cardDetailsBox.setVisible(isInitialCardPayment);
        cardDetailsBox.setManaged(isInitialCardPayment);


        content.getChildren().addAll(orderSummaryLabel, paymentMethodLabel, paymentMethodComboBox, cardDetailsBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Confirm Order", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            // OTP verification before proceeding
            showOtpVerificationDialog(() -> {
                PaymentMethod selectedPaymentMethod = paymentMethodComboBox.getSelectionModel().getSelectedItem();
                // Create the order
                Order newOrder = new Order(0); // ID will be set by DB
                newOrder.setCustomerUsername(currentUser.getUsername()); // Link order to current user
                currentOrderItemQuantities.forEach((item, qty) -> {
                    for (int i = 0; i < qty; i++) {
                        newOrder.addItem(item);
                    }
                });

                newOrder.setStatus(OrderStatus.PENDING);
                newOrder.setPaymentStatus(PaymentStatus.PAID); // Mark as paid upon confirmation
                newOrder.setPaymentMethod(selectedPaymentMethod);
                newOrder.setOrderTime(LocalDateTime.now()); // Set current time

                if (DatabaseManager.addOrder(newOrder)) {
                    showCustomAlertDialog("Order Placed!", "Your order has been placed successfully. Total: Rs." + String.format("%.2f", finalTotal), Alert.AlertType.INFORMATION);
                    currentOrderItemQuantities.clear(); // Clear the cart
                    // Offer to download bill
                    String billContent = newOrder.generateBillContent(); // Corrected: calling existing method
                    showBillDownloadDialog(billContent, "order_" + newOrder.getOrderId() + "_bill.txt");
                    // Refresh menu items to reflect stock changes
                    allMenuItems.setAll(DatabaseManager.getAllMenuItems());
                    // Refresh my orders tab as well
                    allOrders.setAll(DatabaseManager.getOrdersByCustomerUsername(currentUser.getUsername()));

                } else {
                    showCustomAlertDialog("Order Failed", "There was an error placing your order. Please try again.", Alert.AlertType.ERROR);
                }
            });
        } else {
            showCustomAlertDialog("Order Cancelled", "Your order has been cancelled.", Alert.AlertType.INFORMATION);
        }
    }


    /**
     * Creates the "My Orders" tab content for customers.
     * Displays a list of the current user's past orders.
     */
    private VBox createMyOrdersTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("My Orders");
        titleLabel.getStyleClass().add("h1-label");

        TableView<Order> ordersTable = new TableView<>();
        ordersTable.getStyleClass().add("table-view-custom");
        ordersTable.setPlaceholder(new Label("No orders placed yet."));

        // Filter orders for the current user and set to ObservableList
        allOrders.setAll(DatabaseManager.getOrdersByCustomerUsername(currentUser.getUsername()));
        ordersTable.setItems(allOrders);


        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, LocalDateTime> orderTimeCol = new TableColumn<>("Order Time");
        orderTimeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        orderTimeCol.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        orderTimeCol.setPrefWidth(150);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> {
            String itemsSummary = cellData.getValue().getItemsWithQuantities().entrySet().stream()
                                    .map(entry -> entry.getKey().getName() + " x" + entry.getValue())
                                    .collect(Collectors.joining(", "));
            return new ReadOnlyStringWrapper(itemsSummary);
        });
        itemsCol.setCellFactory(tc -> {
            TableCell<Order, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(itemsCol.widthProperty().subtract(10)); // Adjust wrapping width
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });
        itemsCol.setPrefWidth(250);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total (Incl. GST)");
        // FIX: The error mentioned getTotalAmount(), but the code already uses getTotalWithGST().
        // This confirms the screenshot was from an older version. No change needed here.
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalWithGST()).asObject());
        totalCol.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Rs.%.2f", item));
                }
            }
        });
        totalCol.setPrefWidth(120);

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);

        TableColumn<Order, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);

        TableColumn<Order, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button printBillButton = new Button("Print Bill");

            {
                printBillButton.getStyleClass().add("button-info-small");
                printBillButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    printOrderBill(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(printBillButton);
                }
            }
        });


        ordersTable.getColumns().addAll(orderIdCol, orderTimeCol, itemsCol, totalCol, statusCol, paymentStatusCol, paymentMethodCol, actionsCol);
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshOrdersButton = new Button("Refresh Orders");
        refreshOrdersButton.getStyleClass().add("button-secondary");
        refreshOrdersButton.setOnAction(e -> {
            allOrders.setAll(DatabaseManager.getOrdersByCustomerUsername(currentUser.getUsername()));
            showCustomAlertDialog("Refreshed", "Your order list has been updated.", Alert.AlertType.INFORMATION);
        });


        layout.getChildren().addAll(titleLabel, ordersTable, refreshOrdersButton);
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        return layout;
    }

    /**
     * Creates the "Book Table" tab content for customers.
     * Allows customers to book a table at the restaurant.
     */
    private VBox createTableBookingTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Book a Table");
        titleLabel.getStyleClass().add("h1-label");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.getStyleClass().add("form-grid");
        formGrid.setAlignment(Pos.CENTER);

        // Input fields for booking
        Label nameLabel = new Label("Your Name:");
        nameLabel.getStyleClass().add("label");
        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Your Full Name");
        nameField.getStyleClass().add("text-field-custom");
        nameField.setEditable(false); // Customer's name cannot be changed here

        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.getStyleClass().add("label");
        TextField phoneField = new TextField(currentUser.getPhoneNumber());
        phoneField.setPromptText("Your Phone Number");
        phoneField.getStyleClass().add("text-field-custom");
        phoneField.setEditable(false); // Customer's phone cannot be changed here

        Label requiredSeatsLabel = new Label("Number of Guests:");
        requiredSeatsLabel.getStyleClass().add("label");
        Spinner<Integer> requiredSeatsSpinner = new Spinner<>(1, 10, 2); // For 1 to 10 guests, default 2
        requiredSeatsSpinner.getStyleClass().add("spinner-custom");
        requiredSeatsSpinner.setEditable(true);

        Label bookingDateLabel = new Label("Booking Date:");
        bookingDateLabel.getStyleClass().add("label");
        DatePicker bookingDatePicker = new DatePicker(java.time.LocalDate.now());
        bookingDatePicker.getStyleClass().add("date-picker-custom");

        Label bookingTimeLabel = new Label("Booking Time:");
        bookingTimeLabel.getStyleClass().add("label");
        ComboBox<String> bookingTimeComboBox = new ComboBox<>();
        bookingTimeComboBox.getStyleClass().add("combo-box-custom");
        // Populate time slots (e.g., every 30 minutes from 9 AM to 10 PM)
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 9; hour <= 22; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                timeSlots.add(String.format("%02d:%02d", hour, minute));
            }
        }
        bookingTimeComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        bookingTimeComboBox.getSelectionModel().select("19:00"); // Default to 7 PM

        Label durationLabel = new Label("Duration (minutes):");
        durationLabel.getStyleClass().add("label");
        Spinner<Integer> durationSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 240, 60, 30)); // 30-240 mins, default 60, step 30
        durationSpinner.getStyleClass().add("spinner-custom");
        durationSpinner.setEditable(true);

        // New fields for available tables
        Label availableTableTypeLabel = new Label("Available Table Type:");
        availableTableTypeLabel.getStyleClass().add("label");
        ComboBox<TableType> availableTableTypeComboBox = new ComboBox<>();
        availableTableTypeComboBox.setPromptText("Select Available Type");
        availableTableTypeComboBox.getStyleClass().add("combo-box-custom");

        Label availableTableNumberLabel = new Label("Available Table No.:");
        availableTableNumberLabel.getStyleClass().add("label");
        ComboBox<Integer> availableTableNumberComboBox = new ComboBox<>();
        availableTableNumberComboBox.setPromptText("Select Table Number");
        availableTableNumberComboBox.getStyleClass().add("combo-box-custom");

        Label bookingFeeLabel = new Label("Estimated Fee: Rs.0.00");
        bookingFeeLabel.getStyleClass().add("h3-label");
        bookingFeeLabel.getStyleClass().add("label");
        GridPane.setColumnSpan(bookingFeeLabel, 2);
        GridPane.setHalignment(bookingFeeLabel, HPos.CENTER); // Center align the fee label

        // Helper method to update available tables
        Runnable updateAvailableTables = () -> {
            availableTableTypeComboBox.getItems().clear();
            availableTableNumberComboBox.getItems().clear();

            java.time.LocalDate date = bookingDatePicker.getValue();
            String time = bookingTimeComboBox.getSelectionModel().getSelectedItem();
            Integer duration = durationSpinner.getValue();
            Integer seats = requiredSeatsSpinner.getValue();

            if (date != null && time != null && duration != null && seats != null) {
                LocalTime localTime = LocalTime.parse(time);
                LocalDateTime desiredDateTime = LocalDateTime.of(date, localTime);
                Map<TableType, List<Integer>> available = DatabaseManager.getAvailableTables(desiredDateTime, duration, seats);

                // Populate available table types
                List<TableType> sortedTableTypes = available.keySet().stream()
                                                    .sorted(Comparator.comparingInt(TableType::getSeats))
                                                    .collect(Collectors.toList());
                availableTableTypeComboBox.setItems(FXCollections.observableArrayList(sortedTableTypes));

                // If a table type is already selected and is still available, try to retain selection
                TableType currentlySelectedType = availableTableTypeComboBox.getSelectionModel().getSelectedItem();
                if (currentlySelectedType != null && available.containsKey(currentlySelectedType)) {
                    availableTableTypeComboBox.getSelectionModel().select(currentlySelectedType);
                    availableTableNumberComboBox.setItems(FXCollections.observableArrayList(available.get(currentlySelectedType)));
                } else if (!sortedTableTypes.isEmpty()) {
                    availableTableTypeComboBox.getSelectionModel().selectFirst();
                    if (!available.get(sortedTableTypes.get(0)).isEmpty()) {
                         availableTableNumberComboBox.setItems(FXCollections.observableArrayList(available.get(sortedTableTypes.get(0))));
                         availableTableNumberComboBox.getSelectionModel().selectFirst();
                    }
                } else {
                    showCustomAlertDialog("No Tables", "No tables available for selected criteria. Try adjusting date, time, duration, or number of guests.", Alert.AlertType.INFORMATION);
                }
            }
        };

        // Add listeners to trigger updateAvailableTables
        bookingDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables.run());
        bookingTimeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables.run());
        durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables.run());
        requiredSeatsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables.run());
        availableTableTypeComboBox.valueProperty().addListener((obs, oldVal, newType) -> {
            if (newType != null) {
                java.time.LocalDate date = bookingDatePicker.getValue();
                String time = bookingTimeComboBox.getSelectionModel().getSelectedItem();
                Integer duration = durationSpinner.getValue();
                Integer seats = requiredSeatsSpinner.getValue();
                if (date != null && time != null && duration != null && seats != null) {
                    LocalTime localTime = LocalTime.parse(time);
                    LocalDateTime desiredDateTime = LocalDateTime.of(date, localTime);
                    Map<TableType, List<Integer>> available = DatabaseManager.getAvailableTables(desiredDateTime, duration, seats);
                    if (available.containsKey(newType)) {
                        availableTableNumberComboBox.setItems(FXCollections.observableArrayList(available.get(newType)));
                        availableTableNumberComboBox.getSelectionModel().selectFirst();
                    } else {
                        availableTableNumberComboBox.getItems().clear();
                    }
                }
            } else {
                availableTableNumberComboBox.getItems().clear();
            }
        });


        // Listener to update booking fee based on duration
        durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double hours = newVal / 60.0;
                double fee = hours * BOOKING_FEE_PER_HOUR;
                bookingFeeLabel.setText(String.format("Estimated Fee: Rs.%.2f", fee));
            }
        });
        // Initial fee calculation
        double initialHours = durationSpinner.getValue() / 60.0;
        bookingFeeLabel.setText(String.format("Estimated Fee: Rs.%.2f", initialHours * BOOKING_FEE_PER_HOUR));


        Button bookTableButton = new Button("Confirm Booking");
        bookTableButton.getStyleClass().add("button-primary");
        GridPane.setColumnSpan(bookTableButton, 2); // Span across two columns
        GridPane.setHalignment(bookTableButton, HPos.CENTER); // Center align button

        // Initial call to populate available tables
        Platform.runLater(updateAvailableTables);


        bookTableButton.setOnAction(e -> {
            String customerName = nameField.getText();
            String phone = phoneField.getText();
            TableType selectedTableType = availableTableTypeComboBox.getSelectionModel().getSelectedItem();
            Integer selectedTableNumber = availableTableNumberComboBox.getSelectionModel().getSelectedItem();
            java.time.LocalDate bookingDate = bookingDatePicker.getValue();
            String timeString = bookingTimeComboBox.getSelectionModel().getSelectedItem();
            int duration = durationSpinner.getValue();
            int seats = requiredSeatsSpinner.getValue(); // Get seats from spinner

            if (customerName.isEmpty() || phone.isEmpty() || selectedTableType == null || selectedTableNumber == null || bookingDate == null || timeString == null) {
                showCustomAlertDialog("Missing Information", "Please fill in all booking details and select an available table.", Alert.AlertType.WARNING);
                return;
            }

            LocalTime bookingLocalTime = LocalTime.parse(timeString);
            LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingLocalTime);

            double fee = (duration / 60.0) * BOOKING_FEE_PER_HOUR;

            // Use currentUser.getUserId() for the customerId
            TableBooking newBooking = new TableBooking(
                    currentUser.getUserId(),
                    customerName,
                    phone,
                    selectedTableType,
                    selectedTableNumber,
                    seats, // Pass seats here
                    bookingDateTime,
                    duration,
                    fee
            );

            // Call the correct payment dialog method for booking
            showPaymentDialogForTableBooking(newBooking);
            // Refresh available tables after a booking attempt
            updateAvailableTables.run();
        });

        formGrid.addRow(0, nameLabel, nameField);
        formGrid.addRow(1, phoneLabel, phoneField);
        formGrid.addRow(2, requiredSeatsLabel, requiredSeatsSpinner);
        formGrid.addRow(3, bookingDateLabel, bookingDatePicker);
        formGrid.addRow(4, bookingTimeLabel, bookingTimeComboBox);
        formGrid.addRow(5, durationLabel, durationSpinner);
        formGrid.addRow(6, availableTableTypeLabel, availableTableTypeComboBox);
        formGrid.addRow(7, availableTableNumberLabel, availableTableNumberComboBox);
        formGrid.add(bookingFeeLabel, 0, 8); // Row 8, column 0, spans 2 columns
        formGrid.add(bookTableButton, 0, 9); // Row 9, column 0, spans 2 columns


        layout.getChildren().addAll(titleLabel, formGrid);
        VBox.setVgrow(formGrid, Priority.ALWAYS);
        return layout;
    }

    /**
     * Shows the payment dialog for a table booking.
     * @param booking The TableBooking object to process payment for.
     */
    private void showPaymentDialogForTableBooking(TableBooking booking) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Table Booking & Payment");
        dialog.setHeaderText("Choose Payment Method for Booking");
        dialog.getDialogPane().getStyleClass().add("alert");
        if (cssStylesheet != null) { // Apply CSS to this dialog as well
            dialog.getDialogPane().getStylesheets().add(cssStylesheet);
        }

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER_LEFT);

        Label bookingSummaryLabel = new Label(
                "Booking Details:\n" +
                "Table Type: " + booking.getTableType().getDisplayValue() + "\n" +
                "Table No: " + booking.getTableNumber() + "\n" +
                "Seats: " + booking.getSeats() + "\n" + // Display seats
                "Time: " + booking.getBookingTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                "Duration: " + booking.getDurationMinutes() + " minutes\n" +
                "Booking Fee: Rs." + String.format("%.2f", booking.getBookingFee())
        );
        bookingSummaryLabel.getStyleClass().add("h3-label");


        Label paymentMethodLabel = new Label("Select Payment Method:");
        paymentMethodLabel.getStyleClass().add("body-text");
        ComboBox<PaymentMethod> paymentMethodComboBox = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.getStyleClass().add("combo-box-custom");
        paymentMethodComboBox.getSelectionModel().select(PaymentMethod.ONLINE_PAYMENT); // Default to Online Payment to show options directly

        VBox cardDetailsBox = new VBox(10);
        cardDetailsBox.setPadding(new Insets(10));
        cardDetailsBox.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label savedCardsLabel = new Label("Choose Saved Card:");
        savedCardsLabel.getStyleClass().add("body-text");
        ComboBox<CreditCard> savedCardComboBox = new ComboBox<>();
        savedCardComboBox.getStyleClass().add("combo-box-custom");
        // Convert CreditCard object to string for display in ComboBox
        savedCardComboBox.setConverter(new javafx.util.StringConverter<CreditCard>() {
            @Override
            public String toString(CreditCard card) {
                return card != null ? card.getCardType() + " ending in " + card.getLastFourDigits() : "";
            }
            @Override
            public CreditCard fromString(String string) {
                return null; // Not used for selection
            }
        });

        // Populate saved cards for the current user
        if (currentUser != null) {
            savedCardComboBox.setItems(FXCollections.observableArrayList(DatabaseManager.getSavedCreditCards(currentUser.getUserId())));
        }

        Button addNewCardButton = new Button("Add New Card");
        addNewCardButton.getStyleClass().add("button-secondary");
        addNewCardButton.setOnAction(e -> showAddCreditCardDialog(savedCardComboBox.getItems())); // Reusing the same add card dialog

        cardDetailsBox.getChildren().addAll(savedCardsLabel, savedCardComboBox, addNewCardButton);

        // Toggle visibility of card details based on payment method
        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCardPayment = (newVal == PaymentMethod.CREDIT_CARD || newVal == PaymentMethod.DEBIT_CARD || newVal == PaymentMethod.ONLINE_PAYMENT);
            cardDetailsBox.setVisible(isCardPayment);
            cardDetailsBox.setManaged(isCardPayment);

            if (isCardPayment && savedCardComboBox.getItems().isEmpty()) {
                showCustomAlertDialog("No Saved Cards", "You have no saved cards. Please add one.", Alert.AlertType.INFORMATION);
            }
        });

        // Initial state for card details if default is card type
        boolean isInitialCardPayment = (paymentMethodComboBox.getSelectionModel().getSelectedItem() == PaymentMethod.CREDIT_CARD ||
                                        paymentMethodComboBox.getSelectionModel().getSelectedItem() == PaymentMethod.DEBIT_CARD ||
                                        paymentMethodComboBox.getSelectionModel().getSelectedItem() == PaymentMethod.ONLINE_PAYMENT);
        cardDetailsBox.setVisible(isInitialCardPayment);
        cardDetailsBox.setManaged(isInitialCardPayment);


        content.getChildren().addAll(bookingSummaryLabel, paymentMethodLabel, paymentMethodComboBox, cardDetailsBox);

        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            // OTP verification before proceeding
            showOtpVerificationDialog(() -> {
                PaymentMethod selectedPaymentMethod = paymentMethodComboBox.getSelectionModel().getSelectedItem();
                booking.setPaymentStatus(PaymentStatus.PAID); // Mark as paid upon confirmation
                booking.setPaymentMethod(selectedPaymentMethod);

                if (DatabaseManager.addTableBooking(booking)) {
                    showCustomAlertDialog("Booking Confirmed!", "Your table booking has been confirmed for Table " + booking.getTableNumber() + ".", Alert.AlertType.INFORMATION);
                    // Refresh my bookings tab
                    allTableBookings.setAll(DatabaseManager.getTableBookingsByCustomerId(currentUser.getUserId()));
                } else {
                    showCustomAlertDialog("Booking Failed", "There was an error confirming your booking, possibly due to table unavailability. Please try again.", Alert.AlertType.ERROR);
                }
            });
        } else {
            showCustomAlertDialog("Booking Cancelled", "Your table booking has been cancelled.", Alert.AlertType.INFORMATION);
        }
    }


    /**
     * Creates the "My Bookings" tab content for customers.
     * Displays a list of the current user's past and upcoming table bookings.
     */
    private VBox createMyBookingsTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("My Table Bookings");
        titleLabel.getStyleClass().add("h1-label");

        TableView<TableBooking> bookingsTable = new TableView<>();
        bookingsTable.getStyleClass().add("table-view-custom");
        bookingsTable.setPlaceholder(new Label("No table bookings yet."));

        // Filter bookings for the current user and set to ObservableList
        allTableBookings.setAll(DatabaseManager.getTableBookingsByCustomerId(currentUser.getUserId()));
        bookingsTable.setItems(allTableBookings);

        TableColumn<TableBooking, String> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTableType().getDisplayValue()));
        tableTypeCol.setPrefWidth(120);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(100);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(80);

        TableColumn<TableBooking, LocalDateTime> bookingTimeCol = new TableColumn<>("Booking Time");
        bookingTimeCol.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        bookingTimeCol.setCellFactory(column -> new TableCell<TableBooking, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        bookingTimeCol.setPrefWidth(150);

        TableColumn<TableBooking, Integer> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        durationCol.setPrefWidth(100);

        TableColumn<TableBooking, Double> feeCol = new TableColumn<>("Fee");
        feeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        feeCol.setCellFactory(column -> new TableCell<TableBooking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Rs.%.2f", item));
                }
            }
        });
        feeCol.setPrefWidth(80);

        TableColumn<TableBooking, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);
        paymentStatusCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentStatus.values()));
        paymentStatusCol.setOnEditCommit(event -> {
            TableBooking booking = event.getRowValue();
            booking.setPaymentStatus(event.getNewValue());
            if (DatabaseManager.updateTableBooking(booking)) {
                showCustomAlertDialog("Updated", "Booking " + booking.getBookingId() + " payment status updated to " + booking.getPaymentStatus().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update booking payment status.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<TableBooking, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);
        paymentMethodCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentMethod.values()));
        paymentMethodCol.setOnEditCommit(event -> {
            TableBooking booking = event.getRowValue();
            booking.setPaymentMethod(event.getNewValue());
            if (DatabaseManager.updateTableBooking(booking)) {
                showCustomAlertDialog("Updated", "Booking " + booking.getBookingId() + " payment method updated to " + booking.getPaymentMethod().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update booking payment method.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<TableBooking, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(80);
        actionsCol.setCellFactory(param -> new TableCell<TableBooking, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-danger-small");
                deleteButton.setOnAction(event -> {
                    TableBooking booking = getTableView().getItems().get(getIndex());
                    if (showConfirmationDialog("Confirm Deletion", "Are you sure you want to delete Booking ID " + booking.getBookingId() + "?")) {
                        if (DatabaseManager.deleteTableBooking(booking.getBookingId())) {
                            allTableBookings.remove(booking);
                            showCustomAlertDialog("Success", "Booking " + booking.getBookingId() + " deleted.", Alert.AlertType.INFORMATION);
                        } else {
                            showCustomAlertDialog("Error", "Failed to delete booking.", Alert.AlertType.ERROR);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        bookingsTable.getColumns().addAll(tableTypeCol, tableNumberCol, seatsCol, bookingTimeCol, durationCol, feeCol, paymentStatusCol, paymentMethodCol, actionsCol); // Removed bookingIdCol, customerNameCol, phoneCol for customer view, as they are not needed for my bookings tab.
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshBookingsButton = new Button("Refresh Bookings");
        refreshBookingsButton.getStyleClass().add("button-secondary");
        refreshBookingsButton.setOnAction(e -> {
            allTableBookings.setAll(DatabaseManager.getTableBookingsByCustomerId(currentUser.getUserId()));
            showCustomAlertDialog("Refreshed", "Your booking list has been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, bookingsTable, refreshBookingsButton);
        VBox.setVgrow(bookingsTable, Priority.ALWAYS);
        return layout;
    }

    /**
     * Creates the "Feedback & Ratings" tab content for customers.
     * Allows customers to submit general feedback and rate individual dishes.
     */
    private VBox createFeedbackAndRatingsTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Share Your Feedback & Rate Dishes");
        titleLabel.getStyleClass().add("h1-label");

        TabPane subTabPane = new TabPane();
        subTabPane.getStyleClass().add("custom-sub-tab-pane");
        subTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        subTabPane.getTabs().addAll(
                new Tab("Submit Feedback", createSubmitFeedbackSubTab()),
                new Tab("Rate a Dish", createRateDishSubTab())
        );

        layout.getChildren().addAll(titleLabel, subTabPane);
        VBox.setVgrow(subTabPane, Priority.ALWAYS);
        return layout;
    }

    /**
     * Creates the "Submit Feedback" sub-tab content.
     */
    private VBox createSubmitFeedbackSubTab() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.TOP_CENTER);

        Label header = new Label("General Restaurant Feedback");
        header.getStyleClass().add("h2-label");

        TextArea commentsField = new TextArea();
        commentsField.setPromptText("Enter your comments here...");
        commentsField.setWrapText(true);
        commentsField.getStyleClass().add("text-area-custom");
        commentsField.setPrefHeight(100);

        Label ratingLabel = new Label("Overall Rating (1-5 stars):");
        ratingLabel.getStyleClass().add("label");
        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5); // 1 to 5 stars, default 5
        ratingSpinner.getStyleClass().add("spinner-custom");
        ratingSpinner.setPrefWidth(100);

        Button submitButton = new Button("Submit Feedback");
        submitButton.getStyleClass().add("button-primary");
        submitButton.setOnAction(e -> {
            String comments = commentsField.getText();
            int rating = ratingSpinner.getValue();

            if (comments.isEmpty()) {
                showCustomAlertDialog("Missing Input", "Please enter your comments.", Alert.AlertType.WARNING);
                return;
            }

            Feedback newFeedback = new Feedback(0, currentUser.getUsername(), rating, comments, LocalDateTime.now());
            if (DatabaseManager.addFeedback(newFeedback)) {
                showCustomAlertDialog("Success", "Thank you for your feedback!", Alert.AlertType.INFORMATION);
                commentsField.clear(); // Clear the form
                allFeedback.setAll(DatabaseManager.getAllFeedback()); // Refresh admin view of feedback
            } else {
                showCustomAlertDialog("Error", "Failed to submit feedback. Please try again.", Alert.AlertType.ERROR);
            }
        });

        layout.getChildren().addAll(header, commentsField, ratingLabel, ratingSpinner, submitButton);
        return layout;
    }

    /**
     * Creates the "Rate a Dish" sub-tab content.
     */
    private VBox createRateDishSubTab() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.TOP_CENTER);

        Label header = new Label("Rate a Specific Dish");
        header.getStyleClass().add("h2-label");

        Label dishLabel = new Label("Select Dish:");
        dishLabel.getStyleClass().add("label");
        // Use allMenuItems to populate dish selection
        ComboBox<MenuItem> dishComboBox = new ComboBox<>(allMenuItems);
        dishComboBox.setPromptText("Select a Dish");
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


        Label ratingLabel = new Label("Rating (1-5 stars):");
        ratingLabel.getStyleClass().add("label");
        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5); // 1 to 5 stars, default 5
        ratingSpinner.getStyleClass().add("spinner-custom");
        ratingSpinner.setPrefWidth(100);

        Button submitButton = new Button("Submit Dish Rating");
        submitButton.getStyleClass().add("button-primary");
        submitButton.setOnAction(e -> {
            MenuItem selectedDish = dishComboBox.getSelectionModel().getSelectedItem();
            int rating = ratingSpinner.getValue();

            if (selectedDish == null) {
                showCustomAlertDialog("Missing Input", "Please select a dish to rate.", Alert.AlertType.WARNING);
                return;
            }

            DishRating newRating = new DishRating(0, selectedDish.getId(), currentUser.getUsername(), rating, LocalDateTime.now());
            if (DatabaseManager.addDishRating(newRating)) {
                showCustomAlertDialog("Success", "Rating for " + selectedDish.getName() + " submitted!", Alert.AlertType.INFORMATION);
                dishComboBox.getSelectionModel().clearSelection(); // Clear the form
                allDishRatings.setAll(DatabaseManager.getAllDishRatings()); // Refresh all dish ratings for display
            } else {
                showCustomAlertDialog("Error", "Failed to submit dish rating. Please try again.", Alert.AlertType.ERROR);
            }
        });

        layout.getChildren().addAll(header, dishLabel, dishComboBox, ratingLabel, ratingSpinner, submitButton);
        return layout;
    }


    /**
     * Creates the "Manage Menu" tab content for admin users.
     * Allows admins to add, edit, and delete menu items.
     * Removed image URL column and handling as images are no longer used.
     */
    private VBox createAdminMenuManagementTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Manage Menu Items");
        titleLabel.getStyleClass().add("h1-label");

        TableView<MenuItem> menuTable = new TableView<>();
        menuTable.getStyleClass().add("table-view-custom");
        menuTable.setPlaceholder(new Label("No menu items available."));
        menuTable.setEditable(true);

        // Load all menu items initially
        allMenuItems.setAll(DatabaseManager.getAllMenuItems());
        menuTable.setItems(allMenuItems);

        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250); // Increased width as image URL column is removed
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            MenuItem item = event.getRowValue();
            item.setName(event.getNewValue());
            DatabaseManager.updateMenuItem(item);
            showCustomAlertDialog("Updated", "Menu item name updated.", Alert.AlertType.INFORMATION);
        });

        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(120); // Adjusted width
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        priceCol.setOnEditCommit(event -> {
            MenuItem item = event.getRowValue();
            item.setPrice(event.getNewValue());
            DatabaseManager.updateMenuItem(item);
            showCustomAlertDialog("Updated", "Menu item price updated.", Alert.AlertType.INFORMATION);
        });

        // Removed imageUrlCol


        TableColumn<MenuItem, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(100); // Adjusted width
        stockCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        stockCol.setOnEditCommit(event -> {
            MenuItem item = event.getRowValue();
            item.setStock(event.getNewValue());
            // Using updateMenuItem for direct stock updates
            if (DatabaseManager.updateMenuItem(item)) { // This now updates stock directly
                showCustomAlertDialog("Updated", "Menu item stock updated.", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update menu item stock.", Alert.AlertType.ERROR);
            }
        });


        TableColumn<MenuItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(param -> new TableCell<MenuItem, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-danger-small");
                deleteButton.setOnAction(event -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    if (showConfirmationDialog("Confirm Deletion", "Are you sure you want to delete " + item.getName() + "?")) {
                        if (DatabaseManager.deleteMenuItem(item.getId())) {
                            allMenuItems.remove(item);
                            showCustomAlertDialog("Success", item.getName() + " deleted.", Alert.AlertType.INFORMATION);
                        } else {
                            showCustomAlertDialog("Error", "Failed to delete menu item.", Alert.AlertType.ERROR);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        menuTable.getColumns().addAll(idCol, nameCol, priceCol, stockCol, actionsCol); // Removed imageUrlCol
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        // Add New Menu Item Section
        VBox addMenuItemBox = new VBox(10);
        addMenuItemBox.setPadding(new Insets(15));
        addMenuItemBox.getStyleClass().add("section-box");

        Label addHeader = new Label("Add New Menu Item");
        addHeader.getStyleClass().add("h2-label");

        TextField newNameField = new TextField();
        newNameField.setPromptText("Item Name");
        newNameField.getStyleClass().add("text-field-custom");

        TextField newPriceField = new TextField();
        newPriceField.setPromptText("Price");
        newPriceField.getStyleClass().add("text-field-custom");
        newPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Allows empty string, then matches 0 or more digits, optionally followed by a dot and 0-2 digits
            if (!newVal.matches("|\\d*([\\.]\\d{0,2})?")) {
                newPriceField.setText(oldVal);
            }
        });


        // Removed newImageUrlField as images are no longer used
        // TextField newImageUrlField = new TextField();
        // newImageUrlField.setPromptText("Image Filename (e.g., burger.png) or full path (e.g., D:/img.jpg)");
        // newImageUrlField.getStyleClass().add("text-field-custom");

        Spinner<Integer> newStockSpinner = new Spinner<>(0, 1000, 50); // Default initial stock 50
        newStockSpinner.setPromptText("Initial Stock");
        newStockSpinner.getStyleClass().add("spinner-custom");
        newStockSpinner.setPrefWidth(100);
        newStockSpinner.setEditable(true);


        Button addItemButton = new Button("Add Item");
        addItemButton.getStyleClass().add("button-primary");
        addItemButton.setOnAction(e -> {
            String name = newNameField.getText();
            String priceText = newPriceField.getText();
            String imageUrl = ""; // Set to empty string as images are removed
            int initialStock = newStockSpinner.getValue();


            if (name.isEmpty() || priceText.isEmpty()) {
                showCustomAlertDialog("Missing Information", "Name and Price are required for a new menu item.", Alert.AlertType.WARNING);
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                MenuItem newItem = new MenuItem(0, name, price, imageUrl, initialStock);
                // Call addMenuItem, stock is already part of the MenuItem object
                if (DatabaseManager.addMenuItem(newItem)) {
                    allMenuItems.add(newItem); // Add to observable list
                    showCustomAlertDialog("Success", name + " added to menu.", Alert.AlertType.INFORMATION);
                    newNameField.clear();
                    newPriceField.clear();
                    // newImageUrlField.clear(); // Removed
                    newStockSpinner.getValueFactory().setValue(50); // Reset spinner
                } else {
                    showCustomAlertDialog("Error", "Failed to add menu item. Name might already exist.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showCustomAlertDialog("Invalid Price", "Please enter a valid number for price.", Alert.AlertType.ERROR);
            }
        });

        addMenuItemBox.getChildren().addAll(addHeader, newNameField, newPriceField, new Label("Initial Stock:"), newStockSpinner, addItemButton); // Removed newImageUrlField


        Button refreshMenuButton = new Button("Refresh Menu");
        refreshMenuButton.getStyleClass().add("button-secondary");
        refreshMenuButton.setOnAction(e -> {
            allMenuItems.setAll(DatabaseManager.getAllMenuItems());
            showCustomAlertDialog("Refreshed", "Menu list has been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, menuTable, addMenuItemBox, refreshMenuButton);
        VBox.setVgrow(menuTable, Priority.ALWAYS);
        return layout;
    }


    /**
     * Creates the "Manage Orders" tab content for admin users.
     * Allows admins to view and update the status of all orders.
     */
    private VBox createAdminOrderManagementTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Manage Customer Orders");
        titleLabel.getStyleClass().add("h1-label");

        TableView<Order> ordersTable = new TableView<>();
        ordersTable.getStyleClass().add("table-view-custom");
        ordersTable.setPlaceholder(new Label("No orders available."));
        ordersTable.setEditable(true);

        allOrders.setAll(DatabaseManager.getAllOrders()); // Load all orders
        ordersTable.setItems(allOrders);

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(80);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer Username");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerCol.setPrefWidth(150);

        TableColumn<Order, LocalDateTime> orderTimeCol = new TableColumn<>("Order Time");
        orderTimeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        orderTimeCol.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        orderTimeCol.setPrefWidth(150);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> {
            String itemsSummary = cellData.getValue().getItemsWithQuantities().entrySet().stream()
                                    .map(entry -> entry.getKey().getName() + " x" + entry.getValue())
                                    .collect(Collectors.joining(", "));
            return new ReadOnlyStringWrapper(itemsSummary);
        });
        itemsCol.setCellFactory(tc -> {
            TableCell<Order, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(itemsCol.widthProperty().subtract(10)); // Adjust wrapping width
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });
        itemsCol.setPrefWidth(250);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total (Incl. GST)");
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalWithGST()).asObject());
        totalCol.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Rs.%.2f", item));
                }
            }
        });
        totalCol.setPrefWidth(120);


        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(OrderStatus.values()));
        statusCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            order.setStatus(event.getNewValue());
            if (DatabaseManager.updateOrder(order)) {
                showCustomAlertDialog("Updated", "Order " + order.getOrderId() + " status updated to " + order.getStatus().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update order status.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);
        paymentStatusCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentStatus.values()));
        paymentStatusCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            order.setPaymentStatus(event.getNewValue());
            if (DatabaseManager.updateOrder(order)) {
                showCustomAlertDialog("Updated", "Order " + order.getOrderId() + " payment status updated to " + order.getPaymentStatus().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update order payment status.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<Order, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);
        paymentMethodCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentMethod.values()));
        paymentMethodCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            order.setPaymentMethod(event.getNewValue());
            if (DatabaseManager.updateOrder(order)) {
                showCustomAlertDialog("Updated", "Order " + order.getOrderId() + " payment method updated to " + order.getPaymentMethod().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update order payment method.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<Order, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(80);
        actionsCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-danger-small");
                deleteButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if (showConfirmationDialog("Confirm Deletion", "Are you sure you want to delete Order ID " + order.getOrderId() + "? This will return items to stock.")) {
                        if (DatabaseManager.deleteOrder(order.getOrderId())) {
                            allOrders.remove(order);
                            showCustomAlertDialog("Success", "Order " + order.getOrderId() + " deleted and stock returned.", Alert.AlertType.INFORMATION);
                            // Refresh menu items for customer view to reflect stock changes
                            allMenuItems.setAll(DatabaseManager.getAllMenuItems());
                        } else {
                            showCustomAlertDialog("Error", "Failed to delete order.", Alert.AlertType.ERROR);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });


        ordersTable.getColumns().addAll(orderIdCol, customerCol, orderTimeCol, itemsCol, totalCol, statusCol, paymentStatusCol, paymentMethodCol, actionsCol);
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshOrdersButton = new Button("Refresh Orders");
        refreshOrdersButton.getStyleClass().add("button-secondary");
        refreshOrdersButton.setOnAction(e -> {
            allOrders.setAll(DatabaseManager.getAllOrders());
            showCustomAlertDialog("Refreshed", "Order list has been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, ordersTable, refreshOrdersButton);
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        return layout;
    }


    /**
     * Creates the "Manage Users" tab content for admin users.
     * Allows admins to view, edit roles, and delete users.
     */
    private VBox createAdminUserManagementTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Manage Users");
        titleLabel.getStyleClass().add("h1-label");

        TableView<User> usersTable = new TableView<>();
        usersTable.getStyleClass().add("table-view-custom");
        usersTable.setPlaceholder(new Label("No users available."));
        usersTable.setEditable(true);

        ObservableList<User> allUsers = FXCollections.observableArrayList(DatabaseManager.getAllUsers()); // Load all users
        usersTable.setItems(allUsers);

        TableColumn<User, String> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userIdCol.setPrefWidth(150);

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
        roleCol.setCellFactory(ComboBoxTableCell.forTableColumn(UserRole.values()));
        roleCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            UserRole newRole = event.getNewValue();
            // Prevent admin from changing their own role to non-admin
            if (user.getUserId().equals(currentUser.getUserId()) && newRole != UserRole.ADMIN) {
                showCustomAlertDialog("Cannot Change Own Role", "You cannot demote yourself from Admin role.", Alert.AlertType.WARNING);
                usersTable.refresh(); // Revert the UI change
                return;
            }
            if (DatabaseManager.updateUserRole(user.getUserId(), newRole)) {
                user.setRole(newRole); // Update the user object in the observable list
                showCustomAlertDialog("Updated", "User " + user.getUsername() + " role updated to " + newRole.getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update user role.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(80);
        actionsCol.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-danger-small");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    // Prevent admin from deleting their own account
                    if (user.getUserId().equals(currentUser.getUserId())) {
                        showCustomAlertDialog("Cannot Delete Self", "You cannot delete your own user account.", Alert.AlertType.WARNING);
                        return;
                    }
                    if (showConfirmationDialog("Confirm Deletion", "Are you sure you want to delete user " + user.getUsername() + "? This action cannot be undone.")) {
                        if (DatabaseManager.deleteUser(user.getUserId())) {
                            allUsers.remove(user); // Remove from observable list
                            showCustomAlertDialog("Success", "User " + user.getUsername() + " deleted.", Alert.AlertType.INFORMATION);
                        } else {
                            showCustomAlertDialog("Error", "Failed to delete user.", Alert.AlertType.ERROR);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });


        usersTable.getColumns().addAll(userIdCol, usernameCol, fullNameCol, emailCol, phoneCol, roleCol, actionsCol);
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        Button refreshUsersButton = new Button("Refresh Users");
        refreshUsersButton.getStyleClass().add("button-secondary");
        refreshUsersButton.setOnAction(e -> {
            allUsers.setAll(DatabaseManager.getAllUsers());
            showCustomAlertDialog("Refreshed", "User list has been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, usersTable, refreshUsersButton);
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        return layout;
    }

    /**
     * Creates the "View Feedback" tab content for admin users.
     * Displays all customer feedback entries.
     */
    private VBox createAdminFeedbackTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Customer Feedback");
        titleLabel.getStyleClass().add("h1-label");

        TableView<Feedback> feedbackTable = new TableView<>();
        feedbackTable.getStyleClass().add("table-view-custom");
        feedbackTable.setPlaceholder(new Label("No feedback available."));

        allFeedback.setAll(DatabaseManager.getAllFeedback()); // Load all feedback
        feedbackTable.setItems(allFeedback);

        TableColumn<Feedback, Integer> feedbackIdCol = new TableColumn<>("ID");
        feedbackIdCol.setCellValueFactory(new PropertyValueFactory<>("feedbackId"));
        feedbackIdCol.setPrefWidth(50);

        TableColumn<Feedback, String> customerUsernameCol = new TableColumn<>("Customer");
        customerUsernameCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerUsernameCol.setPrefWidth(150);

        TableColumn<Feedback, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(80);

        TableColumn<Feedback, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        commentsCol.setCellFactory(tc -> {
            TableCell<Feedback, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(commentsCol.widthProperty().subtract(10)); // Adjust wrapping width
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });
        commentsCol.setPrefWidth(300);

        TableColumn<Feedback, LocalDateTime> feedbackDateCol = new TableColumn<>("Date");
        feedbackDateCol.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));
        feedbackDateCol.setCellFactory(column -> new TableCell<Feedback, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        feedbackDateCol.setPrefWidth(150);

        feedbackTable.getColumns().addAll(feedbackIdCol, customerUsernameCol, ratingCol, commentsCol, feedbackDateCol);
        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshFeedbackButton = new Button("Refresh Feedback");
        refreshFeedbackButton.getStyleClass().add("button-secondary");
        refreshFeedbackButton.setOnAction(e -> {
            allFeedback.setAll(DatabaseManager.getAllFeedback());
            showCustomAlertDialog("Refreshed", "Feedback list has been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, feedbackTable, refreshFeedbackButton);
        VBox.setVgrow(feedbackTable, Priority.ALWAYS);
        return layout;
    }

    /**
     * Creates the "View Dish Ratings" tab content for admin users.
     * Displays all dish ratings and average ratings for each dish.
     */
    private VBox createAdminDishRatingsTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Dish Ratings & Averages");
        titleLabel.getStyleClass().add("h1-label");

        // Table for individual ratings
        Label individualRatingsLabel = new Label("Individual Dish Ratings");
        individualRatingsLabel.getStyleClass().add("h2-label");
        VBox.setMargin(individualRatingsLabel, new Insets(10, 0, 0, 0)); // Add top margin

        TableView<DishRating> dishRatingsTable = new TableView<>();
        dishRatingsTable.getStyleClass().add("table-view-custom");
        dishRatingsTable.setPlaceholder(new Label("No dish ratings available."));

        allDishRatings.setAll(DatabaseManager.getAllDishRatings()); // Load all dish ratings
        dishRatingsTable.setItems(allDishRatings);

        TableColumn<DishRating, Integer> ratingIdCol = new TableColumn<>("ID");
        ratingIdCol.setCellValueFactory(new PropertyValueFactory<>("ratingId"));
        ratingIdCol.setPrefWidth(50);

        TableColumn<DishRating, String> menuItemNameCol = new TableColumn<>("Dish Name");
        menuItemNameCol.setCellValueFactory(cellData -> {
            MenuItem item = DatabaseManager.getMenuItemById(cellData.getValue().getMenuItemId());
            return new ReadOnlyStringWrapper(item != null ? item.getName() : "Unknown Dish");
        });
        menuItemNameCol.setPrefWidth(200);

        TableColumn<DishRating, String> customerUsernameCol = new TableColumn<>("Customer");
        customerUsernameCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));
        customerUsernameCol.setPrefWidth(150);

        TableColumn<DishRating, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(80);

        TableColumn<DishRating, LocalDateTime> ratingDateCol = new TableColumn<>("Date");
        ratingDateCol.setCellValueFactory(new PropertyValueFactory<>("ratingDate"));
        ratingDateCol.setCellFactory(column -> new TableCell<DishRating, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        ratingDateCol.setPrefWidth(150);

        dishRatingsTable.getColumns().addAll(ratingIdCol, menuItemNameCol, customerUsernameCol, ratingCol, ratingDateCol);
        dishRatingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Table for average ratings
        Label averageRatingsLabel = new Label("Average Dish Ratings");
        averageRatingsLabel.getStyleClass().add("h2-label");
        VBox.setMargin(averageRatingsLabel, new Insets(20, 0, 0, 0)); // Add top margin

        TableView<Map.Entry<String, Double>> avgRatingsTable = new TableView<>();
        avgRatingsTable.getStyleClass().add("table-view-custom");
        avgRatingsTable.setPlaceholder(new Label("No average ratings available."));

        TableColumn<Map.Entry<String, Double>, String> avgDishNameCol = new TableColumn<>("Dish Name");
        avgDishNameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getKey()));
        avgDishNameCol.setPrefWidth(250);

        TableColumn<Map.Entry<String, Double>, Double> avgRatingValueCol = new TableColumn<>("Average Rating");
        avgRatingValueCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getValue()));
        avgRatingValueCol.setCellFactory(column -> new TableCell<Map.Entry<String, Double>, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f / 5.00", item));
                }
            }
        });
        avgRatingValueCol.setPrefWidth(150);

        avgRatingsTable.getColumns().addAll(avgDishNameCol, avgRatingValueCol);
        avgRatingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Populate average ratings
        Map<String, Double> averageRatingsMap = new HashMap<>();
        for (MenuItem item : DatabaseManager.getAllMenuItems()) {
            double avgRating = DatabaseManager.getAverageRatingForMenuItem(item.getId());
            averageRatingsMap.put(item.getName(), avgRating);
        }
        avgRatingsTable.setItems(FXCollections.observableArrayList(averageRatingsMap.entrySet()));


        Button refreshRatingsButton = new Button("Refresh Ratings");
        refreshRatingsButton.getStyleClass().add("button-secondary");
        refreshRatingsButton.setOnAction(e -> {
            allDishRatings.setAll(DatabaseManager.getAllDishRatings());
            // Recalculate and update average ratings
            Map<String, Double> refreshedAverageRatingsMap = new HashMap<>();
            for (MenuItem item : DatabaseManager.getAllMenuItems()) {
                double avgRating = DatabaseManager.getAverageRatingForMenuItem(item.getId());
                refreshedAverageRatingsMap.put(item.getName(), avgRating);
            }
            avgRatingsTable.setItems(FXCollections.observableArrayList(refreshedAverageRatingsMap.entrySet()));
            showCustomAlertDialog("Refreshed", "Dish ratings have been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, individualRatingsLabel, dishRatingsTable, averageRatingsLabel, avgRatingsTable, refreshRatingsButton);
        VBox.setVgrow(dishRatingsTable, Priority.ALWAYS);
        VBox.setVgrow(avgRatingsTable, Priority.ALWAYS);
        return layout;
    }


    /**
     * Creates the user profile view where users can manage their personal details and saved payment methods.
     * This tab is available to both customers and admins for their own profiles.
     */
    private VBox createProfileView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("User Profile");
        titleLabel.getStyleClass().add("h1-label");

        // --- User Details Section ---
        VBox userDetailsSection = new VBox(15);
        userDetailsSection.setPadding(new Insets(15));
        userDetailsSection.getStyleClass().add("section-box");
        Label detailsLabel = new Label("My Personal Details");
        detailsLabel.getStyleClass().add("h2-label");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(10));
        detailsGrid.getStyleClass().add("form-grid");

        // Display fields
        Label usernameDisplayLabel = new Label("Username:");
        TextField usernameDisplayField = new TextField(currentUser.getUsername());
        usernameDisplayField.setEditable(false);
        usernameDisplayField.getStyleClass().add("text-field-custom");

        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField(currentUser.getFullName());
        fullNameField.setEditable(false); // Initially not editable
        fullNameField.getStyleClass().add("text-field-custom");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setEditable(false); // Initially not editable
        emailField.getStyleClass().add("text-field-custom");

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField(currentUser.getPhoneNumber());
        phoneField.setEditable(false); // Initially not editable
        phoneField.getStyleClass().add("text-field-custom");

        detailsGrid.add(usernameDisplayLabel, 0, 0);
        detailsGrid.add(usernameDisplayField, 1, 0);
        detailsGrid.add(fullNameLabel, 0, 1);
        detailsGrid.add(fullNameField, 1, 1);
        detailsGrid.add(emailLabel, 0, 2);
        detailsGrid.add(emailField, 1, 2);
        detailsGrid.add(phoneLabel, 0, 3);
        detailsGrid.add(phoneField, 1, 3);

        Button editDetailsButton = new Button("Edit Details");
        editDetailsButton.getStyleClass().add("button-secondary");
        Button saveDetailsButton = new Button("Save Changes");
        saveDetailsButton.getStyleClass().add("button-primary");
        Button cancelEditButton = new Button("Cancel");
        cancelEditButton.getStyleClass().add("button-secondary");

        HBox detailButtons = new HBox(10, editDetailsButton, saveDetailsButton, cancelEditButton);
        detailButtons.setAlignment(Pos.CENTER);
        saveDetailsButton.setVisible(false); // Hidden by default
        cancelEditButton.setVisible(false); // Hidden by default

        // Edit/Save/Cancel logic for User Details
        editDetailsButton.setOnAction(e -> {
            fullNameField.setEditable(true);
            emailField.setEditable(true);
            phoneField.setEditable(true);
            editDetailsButton.setVisible(false);
            saveDetailsButton.setVisible(true);
            cancelEditButton.setVisible(true);
            showCustomAlertDialog("Edit Mode", "You can now edit your details. Click Save when done.", Alert.AlertType.INFORMATION);
        });

        cancelEditButton.setOnAction(e -> {
            // Revert to original values from currentUser object (which reflects DB state)
            currentUser = DatabaseManager.getUserByUserId(currentUser.getUserId()); // Re-fetch to ensure freshest data
            if (currentUser != null) {
                fullNameField.setText(currentUser.getFullName());
                emailField.setText(currentUser.getEmail());
                phoneField.setText(currentUser.getPhoneNumber());
            }

            fullNameField.setEditable(false);
            emailField.setEditable(false);
            phoneField.setEditable(false);
            editDetailsButton.setVisible(true);
            saveDetailsButton.setVisible(false);
            cancelEditButton.setVisible(false);
            showCustomAlertDialog("Canceled", "Changes discarded.", Alert.AlertType.INFORMATION);
        });

        saveDetailsButton.setOnAction(e -> {
            String newFullName = fullNameField.getText();
            String newEmail = emailField.getText();
            String newPhoneNumber = phoneField.getText();

            if (newFullName.isEmpty() || newEmail.isEmpty() || newPhoneNumber.isEmpty()) {
                showCustomAlertDialog("Input Error", "All fields must be filled.", Alert.AlertType.ERROR);
                return;
            }
            // Basic email validation
            if (!newEmail.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
                showCustomAlertDialog("Input Error", "Please enter a valid email address.", Alert.AlertType.ERROR);
                return;
            }
            // Basic phone number validation (digits only)
            if (!newPhoneNumber.matches("\\d+")) {
                showCustomAlertDialog("Input Error", "Phone number must contain only digits.", Alert.AlertType.ERROR);
                return;
            }

            // Update the currentUser object in memory
            currentUser.setFullName(newFullName);
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhoneNumber);

            if (DatabaseManager.updateUserDetails(currentUser)) {
                showCustomAlertDialog("Success", "Your profile details have been updated.", Alert.AlertType.INFORMATION);
                // Update welcome label in main scene if visible
                // This is a direct manipulation, might be brittle if layout changes significantly
                Platform.runLater(() -> {
                    // This relies on the specific hierarchy: root (BorderPane) -> top (HBox) -> welcomeLabel (index 2)
                    Label welcomeLbl = (Label) primaryStage.getScene().lookup(".h3-label");
                    if (welcomeLbl != null) {
                        welcomeLbl.setText("Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole().getDisplayValue() + ")");
                    }
                });
            } else {
                showCustomAlertDialog("Error", "Failed to update profile details.", Alert.AlertType.ERROR);
                // Revert UI fields on failure by re-fetching from DB
                currentUser = DatabaseManager.getUserByUserId(currentUser.getUserId()); // Re-fetch to revert to actual DB state
                if (currentUser != null) {
                    fullNameField.setText(currentUser.getFullName());
                    emailField.setText(currentUser.getEmail());
                    phoneField.setText(currentUser.getPhoneNumber());
                }
            }

            fullNameField.setEditable(false);
            emailField.setEditable(false);
            phoneField.setEditable(false);
            editDetailsButton.setVisible(true);
            saveDetailsButton.setVisible(false);
            cancelEditButton.setVisible(false);
        });

        userDetailsSection.getChildren().addAll(detailsLabel, detailsGrid, detailButtons);

        // --- Saved Payment Methods Section ---
        VBox paymentMethodsSection = new VBox(15);
        paymentMethodsSection.setPadding(new Insets(15));
        paymentMethodsSection.getStyleClass().add("section-box");
        Label paymentsLabel = new Label("My Saved Payment Methods");
        paymentsLabel.getStyleClass().add("h2-label");

        // The list of saved cards for the ListView
        ObservableList<CreditCard> savedCards = FXCollections.observableArrayList(DatabaseManager.getSavedCreditCards(currentUser.getUserId()));
        ListView<CreditCard> savedCardsListView = new ListView<>(savedCards);
        savedCardsListView.setPrefHeight(150);
        savedCardsListView.getStyleClass().add("list-view-custom");
        savedCardsListView.setPlaceholder(new Label("No saved cards."));

        savedCardsListView.setCellFactory(lv -> new ListCell<CreditCard>() {
            @Override
            protected void updateItem(CreditCard item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellLayout = new HBox(10);
                    cellLayout.setAlignment(Pos.CENTER_LEFT);
                    Label cardInfo = new Label(item.getCardType() + " ending in " + item.getLastFourDigits() + " (Exp: " + item.getExpiryMonth() + "/" + item.getExpiryYear() + ")");
                    cardInfo.getStyleClass().add("body-text");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button removeButton = new Button("Remove");
                    removeButton.getStyleClass().add("button-danger-small");
                    removeButton.setOnAction(e -> {
                        if (showConfirmationDialog("Remove Card", "Are you sure you want to remove this card?")) {
                            if (DatabaseManager.deleteCreditCard(item.getCardId())) {
                                savedCards.remove(item);
                                currentUser.removeSavedCard(item); // Update user object in memory
                                showCustomAlertDialog("Removed", "Credit card removed.", Alert.AlertType.INFORMATION);
                            } else {
                                showCustomAlertDialog("Error", "Failed to remove card.", Alert.AlertType.ERROR);
                            }
                        }
                    });
                    cellLayout.getChildren().addAll(cardInfo, spacer, removeButton);
                    setGraphic(cellLayout);
                }
            }
        });


        Button addCardButton = new Button("Add New Card");
        addCardButton.getStyleClass().add("button-primary");
        // Pass the ObservableList to the dialog so it can update it directly
        addCardButton.setOnAction(e -> showAddCreditCardDialog(savedCards));

        paymentMethodsSection.getChildren().addAll(paymentsLabel, savedCardsListView, addCardButton);

        layout.getChildren().addAll(titleLabel, userDetailsSection, paymentMethodsSection);
        return layout;
    }


    /**
     * Shows a confirmation dialog to the user.
     * @param title The title of the confirmation dialog.
     * @param message The message to display.
     * @return true if the user confirms, false otherwise.
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("alert");
        dialogPane.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

        ButtonType buttonTypeOk = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeCancel);

        // Styling for buttons in dialog (optional, but consistent with app)
        alert.setOnShowing(dialogEvent -> { // Changed parameter name from 'dialog' to 'dialogEvent' to avoid conflict
            // Cast dialogEvent.getSource() to Dialog and then get its DialogPane
            Dialog<?> sourceDialog = (Dialog<?>) dialogEvent.getSource();
            ((DialogPane)sourceDialog.getDialogPane()).lookupButton(buttonTypeOk).getStyleClass().add("button-danger"); // Example: Red for dangerous confirm
            ((DialogPane)sourceDialog.getDialogPane()).lookupButton(buttonTypeCancel).getStyleClass().add("button-secondary");
        });


        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOk;
    }

    /**
     * Shows an OTP verification dialog.
     * @param onOtpSuccess Runnable to execute if OTP verification is successful.
     */
    private void showOtpVerificationDialog(Runnable onOtpSuccess) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("OTP Verification");
        dialog.setHeaderText("A 6-digit OTP has been generated.");
        dialog.getDialogPane().getStyleClass().add("alert");
        if (cssStylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(cssStylesheet);
        }

        VBox otpLayout = new VBox(10);
        otpLayout.setPadding(new Insets(15));
        otpLayout.setAlignment(Pos.CENTER);
        otpLayout.getStyleClass().add("otp-box"); // Apply OTP box styling

        Label otpInstructionLabel = new Label("Please enter the OTP to confirm:");
        otpInstructionLabel.getStyleClass().add("body-text");

        // Generate a dummy OTP (for a real app, this would be sent via SMS/email)
        String generatedOtp = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        
        // Display OTP (for demo purposes)
        Label otpDisplayLabel = new Label("Your OTP is: " + generatedOtp);
        otpDisplayLabel.getStyleClass().add("otp-display"); // Apply a distinct style if needed

        TextField otpField = new TextField();
        otpField.setPromptText("Enter OTP");
        otpField.setMaxWidth(150);
        otpField.setAlignment(Pos.CENTER);
        otpField.getStyleClass().add("text-field-custom");
        otpField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 6) otpField.setText(oldVal);
            if (!newVal.matches("\\d*")) otpField.setText(newVal.replaceAll("[^\\d]", ""));
        });

        otpLayout.getChildren().addAll(otpInstructionLabel, otpDisplayLabel, otpField);
        dialog.getDialogPane().setContent(otpLayout);

        ButtonType confirmButtonType = new ButtonType("Verify OTP", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmButtonType) {
            if (otpField.getText().equals(generatedOtp)) {
                showCustomAlertDialog("OTP Verified", "OTP verification successful!", Alert.AlertType.INFORMATION);
                if (onOtpSuccess != null) {
                    onOtpSuccess.run(); // Execute the success callback
                }
            } else {
                showCustomAlertDialog("Verification Failed", "Invalid OTP. Please try again.", Alert.AlertType.ERROR);
            }
        } else {
            showCustomAlertDialog("OTP Cancelled", "OTP verification cancelled.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Generates the content for a bill based on an Order object.
     * @param order The Order object for which to generate the bill.
     * @return A formatted string representing the bill.
     */
    private String generateBillContent(Order order) {
        StringBuilder bill = new StringBuilder();
        bill.append("=========================================\n");
        bill.append("          SHIELD RESTAURANT BILL       \n");
        bill.append("=========================================\n");
        bill.append("Order ID: ").append(order.getOrderId()).append("\n");
        bill.append("Customer: ").append(order.getCustomerUsername() != null ? order.getCustomerUsername() : "N/A").append("\n");
        bill.append("Order Time: ").append(order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        bill.append("-----------------------------------------\n");
        bill.append("Items:\n");

        Map<MenuItem, Integer> itemCounts = order.getItemsWithQuantities();
        if (itemCounts != null && !itemCounts.isEmpty()) {
            itemCounts.forEach((item, count) -> {
                bill.append(String.format("  %-25s x %-3d Rs.%.2f\n", item.getName(), count, item.getPrice() * count));
            });
        } else {
            bill.append("  No items in this order.\n");
        }
        bill.append("-----------------------------------------\n");
        bill.append(String.format("Subtotal:            Rs.%.2f\n", order.getSubtotal()));
        bill.append(String.format("Discount Applied:    Rs.%.2f\n", order.getDiscountApplied()));
        bill.append(String.format("Net Amount:          Rs.%.2f\n", order.getFinalPriceBeforeGST()));
        bill.append(String.format("GST (%.0f%%):         Rs.%.2f\n", GST_RATE * 100, order.getGSTAmount()));
        bill.append("-----------------------------------------\n");
        bill.append(String.format("Total Payable:       Rs.%.2f\n", order.getTotalWithGST()));
        bill.append("Payment Status: ").append(order.getPaymentStatus().getDisplayValue()).append("\n");
        bill.append("Payment Method: ").append(order.getPaymentMethod().getDisplayValue()).append("\n");
        bill.append("=========================================\n");
        bill.append("  Thank you for your business!          \n");
        bill.append("=========================================\n");
        return bill.toString();
    }

    /**
     * Shows a dialog to allow the user to save the generated bill content to a file.
     * @param billContent The string content of the bill.
     * @param defaultFileName The default filename suggested to the user.
     */
    private void showBillDownloadDialog(String billContent, String defaultFileName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bill Download");
        alert.setHeaderText("Your order has been successfully placed!");
        alert.setContentText("Would you like to download your bill?");
        alert.getDialogPane().getStyleClass().add("alert"); // Apply custom alert style
        if (cssStylesheet != null) { // Apply CSS to this dialog as well
            alert.getDialogPane().getStylesheets().add(cssStylesheet);
        }
        alert.initOwner(primaryStage.getScene().getWindow());

        ButtonType downloadButton = new ButtonType("Download Bill", ButtonBar.ButtonData.OK_DONE);
        ButtonType noThanksButton = new ButtonType("No Thanks", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(downloadButton, noThanksButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == downloadButton) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Bill");
            fileChooser.setInitialFileName(defaultFileName);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(billContent);
                    showCustomAlertDialog("Download Complete", "Bill saved successfully to " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
                } catch (IOException ex) {
                    showCustomAlertDialog("Download Error", "Failed to save bill: " + ex.getMessage(), Alert.AlertType.ERROR);
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Helper method to print the order bill. In a real application, this might involve
     * generating a PDF or sending to a physical printer. For this demo, it calls
     * showBillDownloadDialog.
     * @param order The Order object for which to print the bill.
     */
    private void printOrderBill(Order order) {
        String billContent = order.generateBillContent(); // Corrected: calling existing method
        showBillDownloadDialog(billContent, "order_" + order.getOrderId() + "_bill.txt");
    }

    /**
     * Shows a dialog for adding a new credit card.
     * @param targetList The ObservableList<CreditCard> to which the new card should be added upon successful save.
     */
    private void showAddCreditCardDialog(ObservableList<CreditCard> targetList) {
        Dialog<CreditCard> dialog = new Dialog<>();
        dialog.setTitle("Add New Credit Card");
        dialog.setHeaderText("Enter Credit Card Details");
        dialog.getDialogPane().getStyleClass().add("alert");
        if (cssStylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(cssStylesheet);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField lastFourDigitsField = new TextField();
        lastFourDigitsField.setPromptText("Last 4 Digits");
        lastFourDigitsField.setMaxWidth(100);
        lastFourDigitsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 4) lastFourDigitsField.setText(oldVal);
            if (!newVal.matches("\\d*")) lastFourDigitsField.setText(newVal.replaceAll("[^\\d]", ""));
        });

        ComboBox<String> cardTypeComboBox = new ComboBox<>(FXCollections.observableArrayList("Visa", "MasterCard", "Amex"));
        cardTypeComboBox.setPromptText("Card Type");

        ComboBox<String> expiryMonthComboBox = new ComboBox<>(FXCollections.observableArrayList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"));
        expiryMonthComboBox.setPromptText("MM");

        List<String> years = new ArrayList<>();
        int currentYear = LocalDateTime.now().getYear();
        for (int i = 0; i < 10; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        ComboBox<String> expiryYearComboBox = new ComboBox<>(FXCollections.observableArrayList(years));
        expiryYearComboBox.setPromptText("YYYY");

        grid.addRow(0, new Label("Last 4 Digits:"), lastFourDigitsField);
        grid.addRow(1, new Label("Card Type:"), cardTypeComboBox);
        grid.addRow(2, new Label("Expiry Date:"), new HBox(5, expiryMonthComboBox, expiryYearComboBox));

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save Card", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Enable/disable Save button based on input validity
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(
                lastFourDigitsField.textProperty().length().lessThan(4)
                .or(cardTypeComboBox.valueProperty().isNull())
                .or(expiryMonthComboBox.valueProperty().isNull())
                .or(expiryYearComboBox.valueProperty().isNull())
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String lastFour = lastFourDigitsField.getText();
                String type = cardTypeComboBox.getValue();
                String expMonth = expiryMonthComboBox.getValue();
                String expYear = expiryYearComboBox.getValue();

                CreditCard newCard = new CreditCard(lastFour, type, expMonth, expYear);
                return newCard;
            }
            return null;
        });

        Optional<CreditCard> result = dialog.showAndWait();
        result.ifPresent(card -> {
            if (currentUser != null && DatabaseManager.saveCreditCard(currentUser.getUserId(), card)) {
                currentUser.addSavedCard(card); // Update the in-memory user object
                targetList.add(card); // Update the ObservableList passed to the dialog
                showCustomAlertDialog("Success", "Credit card saved.", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to save credit card.", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Creates the "Manage Bookings" tab content for admin users.
     * Allows admins to view and update table bookings.
     */
    @SuppressWarnings("unchecked")
	private VBox createAdminBookingManagementTab() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("content-pane");

        Label titleLabel = new Label("Manage Table Bookings");
        titleLabel.getStyleClass().add("h1-label");

        TableView<TableBooking> bookingsTable = new TableView<>();
        bookingsTable.getStyleClass().add("table-view-custom");
        bookingsTable.setPlaceholder(new Label("No table bookings available."));
        bookingsTable.setEditable(true);

        allTableBookings.setAll(DatabaseManager.getAllTableBookings()); // Load all bookings
        bookingsTable.setItems(allTableBookings);

        // --- Column Definitions for Admin Booking Management Tab ---
        TableColumn<TableBooking, Integer> bookingIdCol = new TableColumn<>("Booking ID");
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookingIdCol.setPrefWidth(80);

        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setPrefWidth(150);

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<TableBooking, String> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTableType().getDisplayValue()));
        tableTypeCol.setPrefWidth(120);

        TableColumn<TableBooking, Integer> tableNumberCol = new TableColumn<>("Table No.");
        tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        tableNumberCol.setPrefWidth(100);

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(80);

        TableColumn<TableBooking, LocalDateTime> bookingTimeCol = new TableColumn<>("Booking Time");
        bookingTimeCol.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        bookingTimeCol.setCellFactory(column -> new TableCell<TableBooking, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        bookingTimeCol.setPrefWidth(150);

        TableColumn<TableBooking, Integer> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        durationCol.setPrefWidth(100);

        TableColumn<TableBooking, Double> feeCol = new TableColumn<>("Fee");
        feeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));
        feeCol.setCellFactory(column -> new TableCell<TableBooking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Rs.%.2f", item));
                }
            }
        });
        feeCol.setPrefWidth(80);

        TableColumn<TableBooking, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setPrefWidth(120);
        paymentStatusCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentStatus.values()));
        paymentStatusCol.setOnEditCommit(event -> {
            TableBooking booking = event.getRowValue();
            booking.setPaymentStatus(event.getNewValue());
            if (DatabaseManager.updateTableBooking(booking)) {
                showCustomAlertDialog("Updated", "Booking " + booking.getBookingId() + " payment status updated to " + booking.getPaymentStatus().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update booking payment status.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<TableBooking, PaymentMethod> paymentMethodCol = new TableColumn<>("Payment Method");
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentMethodCol.setPrefWidth(120);
        paymentMethodCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentMethod.values()));
        paymentMethodCol.setOnEditCommit(event -> {
            TableBooking booking = event.getRowValue();
            booking.setPaymentMethod(event.getNewValue());
            if (DatabaseManager.updateTableBooking(booking)) {
                showCustomAlertDialog("Updated", "Booking " + booking.getBookingId() + " payment method updated to " + booking.getPaymentMethod().getDisplayValue() + ".", Alert.AlertType.INFORMATION);
            } else {
                showCustomAlertDialog("Error", "Failed to update booking payment method.", Alert.AlertType.ERROR);
            }
        });

        TableColumn<TableBooking, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(80);
        actionsCol.setCellFactory(param -> new TableCell<TableBooking, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-danger-small");
                deleteButton.setOnAction(event -> {
                    TableBooking booking = getTableView().getItems().get(getIndex());
                    if (showConfirmationDialog("Confirm Deletion", "Are you sure you want to delete Booking ID " + booking.getBookingId() + "?")) {
                        if (DatabaseManager.deleteTableBooking(booking.getBookingId())) {
                            allTableBookings.remove(booking);
                            showCustomAlertDialog("Success", "Booking " + booking.getBookingId() + " deleted.", Alert.AlertType.INFORMATION);
                        } else {
                            showCustomAlertDialog("Error", "Failed to delete booking.", Alert.AlertType.ERROR);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        bookingsTable.getColumns().addAll(bookingIdCol, customerNameCol, phoneCol, tableTypeCol, tableNumberCol,
                seatsCol, bookingTimeCol, durationCol, feeCol, paymentStatusCol, paymentMethodCol, actionsCol);
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshBookingsButton = new Button("Refresh Bookings");
        refreshBookingsButton.getStyleClass().add("button-secondary");
        refreshBookingsButton.setOnAction(e -> {
            allTableBookings.setAll(DatabaseManager.getAllTableBookings());
            showCustomAlertDialog("Refreshed", "Booking list has been updated.", Alert.AlertType.INFORMATION);
        });

        layout.getChildren().addAll(titleLabel, bookingsTable, refreshBookingsButton);
        VBox.setVgrow(bookingsTable, Priority.ALWAYS);
        return layout;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
