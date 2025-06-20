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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.text.Text; // Import Text for word wrapping in TableView
import javafx.geometry.HPos; // Import HPos

import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleDoubleProperty;


public class Main extends Application {

    private Stage primaryStage;
    private User currentUser;
    private ObservableList<MenuItem> currentOrderItems = FXCollections.observableArrayList();
    private int nextOrderId = 1;

    private static final String APP_NAME = "DineEase Restaurant Management";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(APP_NAME);

        DatabaseManager.initializeDatabase();

        showLoginScene();
    }

    private void showLoginScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("login-container");

        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(30));
        loginBox.getStyleClass().add("login-register-box");

        Label titleLabel = new Label("Welcome to DineEase");
        titleLabel.getStyleClass().add("title-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field-custom");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordField.setMaxWidth(300);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(200);
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            User user = DatabaseManager.validateUser(username, password);
            if (user != null) {
                currentUser = user;
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + currentUser.getFullName() + "!");
                if (currentUser.getRole() == UserRole.ADMIN) {
                    showAdminPortal();
                } else {
                    showCustomerDashboard();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        });

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("button-secondary");
        registerButton.setMaxWidth(200);
        registerButton.setOnAction(e -> showRegisterScene());

        loginBox.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, registerButton);

        VBox container = new VBox(loginBox);
        container.setAlignment(Pos.CENTER);
        root.setCenter(container);

        Scene scene = new Scene(root, 1000, 700);
        applyCss(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showRegisterScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("register-container");

        VBox registerBox = new VBox(15);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(30));
        registerBox.getStyleClass().add("login-register-box");

        Label titleLabel = new Label("Register New Account");
        titleLabel.getStyleClass().add("title-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (unique)");
        usernameField.getStyleClass().add("text-field-custom");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field-custom");
        passwordField.setMaxWidth(300);

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

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("button-primary");
        registerButton.setMaxWidth(200);
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }

            if (DatabaseManager.registerUser(username, password, fullName, email, phone, UserRole.CUSTOMER)) {
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Account created for " + username + ". Please login.");
                showLoginScene();
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists or an error occurred.");
            }
        });

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("button-secondary");
        backButton.setMaxWidth(200);
        backButton.setOnAction(e -> showLoginScene());

        registerBox.getChildren().addAll(titleLabel, usernameField, passwordField, fullNameField, emailField, phoneField, registerButton, backButton);

        VBox container = new VBox(registerBox);
        container.setAlignment(Pos.CENTER);
        root.setCenter(container);

        Scene scene = new Scene(root, 1000, 700);
        applyCss(scene);
        primaryStage.setScene(scene);
    }

    private void showCustomerDashboard() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("content-pane");

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.getStyleClass().add("top-bar");

        Label welcomeLabel = new Label("Welcome, " + currentUser.getFullName() + "!");
        welcomeLabel.getStyleClass().add("h2-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button-danger");
        logoutButton.setOnAction(e -> {
            currentUser = null;
            showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been logged out.");
            showLoginScene();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        root.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("custom-tab-pane");

        Tab menuTab = new Tab("Order Food");
        menuTab.setClosable(false);
        menuTab.setContent(createMenuOrderTab());

        Tab bookingsTab = new Tab("Book Table");
        bookingsTab.setClosable(false);
        bookingsTab.setContent(createCustomerBookingsTab());

        Tab feedbackTab = new Tab("Submit Feedback");
        feedbackTab.setClosable(false);
        feedbackTab.setContent(createFeedbackTab());

        tabPane.getTabs().addAll(menuTab, bookingsTab, feedbackTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 800);
        applyCss(scene);
        primaryStage.setScene(scene);
    }

    private Node createMenuOrderTab() {
        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        VBox menuSection = new VBox(15);
        menuSection.getStyleClass().add("section-box");
        menuSection.setPrefWidth(700);
        Label menuLabel = new Label("Our Menu");
        menuLabel.getStyleClass().add("h2-label");

        TextField searchField = new TextField();
        searchField.setPromptText("Search menu items...");
        searchField.getStyleClass().add("text-field-custom");

        TilePane menuGrid = new TilePane();
        menuGrid.setPadding(new Insets(10));
        menuGrid.setHgap(15);
        menuGrid.setVgap(15);
        menuGrid.setPrefColumns(3);

        List<MenuItem> allMenuItems = DatabaseManager.getAllMenuItems();
        if (allMenuItems.isEmpty()) {
            menuGrid.getChildren().add(new Label("No menu items available."));
        } else {
            populateMenuGrid(menuGrid, allMenuItems);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            menuGrid.getChildren().clear();
            String searchText = newVal.toLowerCase();
            List<MenuItem> filteredItems = allMenuItems.stream()
                    .filter(item -> item.getName().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
            populateMenuGrid(menuGrid, filteredItems);
        });

        ScrollPane menuScrollPane = new ScrollPane(menuGrid);
        menuScrollPane.setFitToWidth(true);
        menuScrollPane.getStyleClass().add("scroll-pane-custom");

        menuSection.getChildren().addAll(menuLabel, searchField, menuScrollPane);
        HBox.setHgrow(menuSection, Priority.ALWAYS);

        VBox orderSummarySection = new VBox(15);
        orderSummarySection.getStyleClass().add("section-box");
        orderSummarySection.setPrefWidth(400);
        orderSummarySection.setAlignment(Pos.TOP_CENTER);
        Label orderLabel = new Label("Your Order");
        orderLabel.getStyleClass().add("h2-label");

        TableView<MenuItem> orderTableView = new TableView<>();
        orderTableView.setEditable(true);
        orderTableView.getStyleClass().add("table-view-custom");

        TableColumn<MenuItem, String> orderItemNameCol = new TableColumn<>("Item");
        orderItemNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        orderItemNameCol.setPrefWidth(150);

        TableColumn<MenuItem, Double> orderItemPriceCol = new TableColumn<>("Price");
        orderItemPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        orderItemPriceCol.setPrefWidth(80);

        TableColumn<MenuItem, Integer> orderQuantityCol = new TableColumn<>("Qty");
        orderQuantityCol.setCellValueFactory(item -> new SimpleIntegerProperty(Collections.frequency(currentOrderItems, item.getValue())).asObject());
        orderQuantityCol.setOnEditCommit(event -> {
            MenuItem editedItem = event.getRowValue();
            int newQuantity = event.getNewValue();
            if (newQuantity < 0) newQuantity = 0;

            int currentQuantity = Collections.frequency(currentOrderItems, editedItem);
            if (newQuantity > currentQuantity) {
                for (int i = 0; i < (newQuantity - currentQuantity); i++) {
                    currentOrderItems.add(editedItem);
                }
            } else if (newQuantity < currentQuantity) {
                for (int i = 0; i < (currentQuantity - newQuantity); i++) {
                    currentOrderItems.remove(editedItem);
                }
            }
            // Refresh the table to reflect changes in quantity
            orderTableView.setItems(FXCollections.observableArrayList(new HashSet<>(currentOrderItems)));
            updateOrderSummary(orderSummarySection, orderTableView);
        });
        orderQuantityCol.setPrefWidth(60);

        TableColumn<MenuItem, Void> orderRemoveCol = new TableColumn<>("Remove");
        orderRemoveCol.setCellFactory(param -> new TableCell<MenuItem, Void>() {
            private final Button removeButton = new Button("X");
            {
                removeButton.getStyleClass().add("button-danger-small");
                removeButton.setOnAction(event -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    currentOrderItems.removeIf(i -> i.getId() == item.getId());
                    orderTableView.setItems(FXCollections.observableArrayList(new HashSet<>(currentOrderItems)));
                    updateOrderSummary(orderSummarySection, orderTableView);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        orderRemoveCol.setPrefWidth(70);

        orderTableView.getColumns().addAll(orderItemNameCol, orderItemPriceCol, orderQuantityCol, orderRemoveCol);
        orderTableView.setItems(FXCollections.observableArrayList(new HashSet<>(currentOrderItems)));

        Label subtotalLabel = new Label("Subtotal: Rs. 0.00");
        subtotalLabel.getStyleClass().add("h3-label");
        Label gstLabel = new Label("GST (5%): Rs. 0.00");
        gstLabel.getStyleClass().add("h4-label");
        Label totalLabel = new Label("Total: Rs. 0.00");
        totalLabel.getStyleClass().add("h2-label");

        Button checkoutButton = new Button("Proceed to Checkout");
        checkoutButton.getStyleClass().add("button-success");
        checkoutButton.setMaxWidth(Double.MAX_VALUE);
        checkoutButton.setOnAction(e -> {
            if (currentOrderItems.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Order", "Please add items to your order before checking out.");
                return;
            }
            showPaymentScene();
        });

        orderSummarySection.getChildren().addAll(orderLabel, orderTableView, subtotalLabel, gstLabel, totalLabel, checkoutButton);
        updateOrderSummary(orderSummarySection, orderTableView);

        content.getChildren().addAll(menuSection, orderSummarySection);
        return content;
    }

    private void populateMenuGrid(TilePane menuGrid, List<MenuItem> menuItems) {
        menuGrid.getChildren().clear();
        for (MenuItem item : menuItems) {
            VBox itemCard = new VBox(5);
            itemCard.getStyleClass().add("menu-item-card");
            itemCard.setAlignment(Pos.CENTER);
            itemCard.setPadding(new Insets(10));

            ImageView itemImage = new ImageView(new Image(item.getImageUrl(), true));
            itemImage.setFitWidth(100);
            itemImage.setFitHeight(100);
            itemImage.setPreserveRatio(false);

            Label nameLabel = new Label(item.getName());
            nameLabel.getStyleClass().add("menu-item-name");
            nameLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("Rs.%.2f", item.getPrice()));
            priceLabel.getStyleClass().add("menu-item-price");

            Button addButton = new Button("Add to Cart");
            addButton.getStyleClass().add("button-add-to-cart");
            addButton.setOnAction(e -> {
                currentOrderItems.add(item);
                showAlert(Alert.AlertType.INFORMATION, "Added to Cart", item.getName() + " added to your order.");
                // Refresh the order summary table
                Node orderTabContent = ((TabPane) primaryStage.getScene().getRoot().lookup(".custom-tab-pane")).getSelectionModel().getSelectedItem().getContent();
                TableView<MenuItem> orderTableView = (TableView<MenuItem>) ((VBox) ((HBox) orderTabContent).getChildren().get(1)).getChildren().get(1);
                orderTableView.setItems(FXCollections.observableArrayList(new HashSet<>(currentOrderItems)));
                updateOrderSummary((VBox) ((HBox) orderTabContent).getChildren().get(1), orderTableView);
            });

            itemCard.getChildren().addAll(itemImage, nameLabel, priceLabel, addButton);
            menuGrid.getChildren().add(itemCard);
        }
    }

    private void updateOrderSummary(VBox orderSummarySection, TableView<MenuItem> orderTableView) {
        Order tempOrder = new Order(0);
        tempOrder.setItems(new ArrayList<>(currentOrderItems));

        double subtotal = tempOrder.getSubtotal();
        double gstAmount = tempOrder.getGSTAmount();
        double total = tempOrder.getTotalWithGST();

        ((Label) orderSummarySection.getChildren().get(2)).setText(String.format("Subtotal: Rs.%.2f", subtotal));
        ((Label) orderSummarySection.getChildren().get(3)).setText(String.format("GST (5%%): Rs.%.2f", gstAmount));
        ((Label) orderSummarySection.getChildren().get(4)).setText(String.format("Total: Rs.%.2f", total));
    }

    private void showPaymentScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("content-pane");

        VBox paymentBox = new VBox(20);
        paymentBox.setAlignment(Pos.CENTER);
        paymentBox.setPadding(new Insets(30));
        paymentBox.getStyleClass().add("card-pane");
        paymentBox.setMaxWidth(600);

        Label titleLabel = new Label("Complete Your Payment");
        titleLabel.getStyleClass().add("h1-label");

        Order finalOrder = new Order(DatabaseManager.getNextOrderId());
        finalOrder.setItems(new ArrayList<>(currentOrderItems));
        finalOrder.setCustomerUsername(currentUser.getUsername());
        finalOrder.setOrderTime(LocalDateTime.now());

        Label orderSummaryLabel = new Label(finalOrder.getReceiptDetails(DatabaseManager.getAllMenuItems()));
        orderSummaryLabel.getStyleClass().add("body-text");

        ComboBox<PaymentMethod> paymentMethodComboBox = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.setPromptText("Select Payment Method");
        paymentMethodComboBox.getStyleClass().add("combo-box-custom");
        paymentMethodComboBox.setValue(PaymentMethod.ONLINE_PAYMENT);

        Button payButton = new Button("Pay Now");
        payButton.getStyleClass().add("button-success");
        payButton.setMaxWidth(Double.MAX_VALUE);

        VBox otpVerificationBox = new VBox(10);
        otpVerificationBox.setAlignment(Pos.CENTER);
        otpVerificationBox.setPadding(new Insets(10));
        otpVerificationBox.getStyleClass().add("otp-box");
        otpVerificationBox.setVisible(false);
        otpVerificationBox.setManaged(false);

        Label otpMessageLabel = new Label("An OTP has been sent to your registered phone number/email.");
        otpMessageLabel.getStyleClass().add("body-text");

        TextField otpDisplayField = new TextField();
        otpDisplayField.setPromptText("OTP (for simulation)");
        otpDisplayField.setEditable(false);
        otpDisplayField.getStyleClass().add("text-field-custom");
        otpDisplayField.setMaxWidth(200);

        TextField otpInputField = new TextField();
        otpInputField.setPromptText("Enter OTP");
        otpInputField.getStyleClass().add("text-field-custom");
        otpInputField.setMaxWidth(200);

        Button verifyOtpButton = new Button("Verify OTP");
        verifyOtpButton.getStyleClass().add("button-primary");
        verifyOtpButton.setMaxWidth(200);

        otpVerificationBox.getChildren().addAll(otpMessageLabel, otpDisplayField, otpInputField, verifyOtpButton);

        payButton.setOnAction(e -> {
            PaymentMethod selectedMethod = paymentMethodComboBox.getValue();
            if (selectedMethod == null) {
                showAlert(Alert.AlertType.WARNING, "Payment Method Required", "Please select a payment method.");
                return;
            }
            finalOrder.setPaymentMethod(selectedMethod);

            if (selectedMethod == PaymentMethod.ONLINE_PAYMENT) {
                int generatedOtp = generateOtp();
                otpDisplayField.setText(String.valueOf(generatedOtp));
                otpVerificationBox.setVisible(true);
                otpVerificationBox.setManaged(true);
                payButton.setDisable(true);
            } else {
                processOrder(finalOrder);
            }
        });

        verifyOtpButton.setOnAction(e -> {
            try {
                int enteredOtp = Integer.parseInt(otpInputField.getText());
                int displayedOtp = Integer.parseInt(otpDisplayField.getText());

                if (enteredOtp == displayedOtp) {
                    showAlert(Alert.AlertType.INFORMATION, "OTP Verified", "OTP successfully verified. Payment complete!");
                    processOrder(finalOrder);
                } else {
                    showAlert(Alert.AlertType.ERROR, "OTP Mismatch", "Incorrect OTP. Please try again.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid OTP", "Please enter a valid numeric OTP.");
            }
        });

        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("button-secondary");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> showCustomerDashboard());

        paymentBox.getChildren().addAll(titleLabel, orderSummaryLabel, paymentMethodComboBox, payButton, otpVerificationBox, backButton);

        VBox container = new VBox(paymentBox);
        container.setAlignment(Pos.CENTER);
        root.setCenter(container);

        Scene scene = new Scene(root, 1000, 700);
        applyCss(scene);
        primaryStage.setScene(scene);
    }

    private int generateOtp() {
        Random random = new Random();
        return 100000 + random.nextInt(900000);
    }

    private void processOrder(Order order) {
        order.paymentStatus = PaymentStatus.PAID;
        order.status = OrderStatus.PENDING;

        if (DatabaseManager.addOrder(order)) {
            showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Your order has been placed successfully!");
            currentOrderItems.clear(); // Clear cart after successful order
            showCustomerDashboard(); // Go back to dashboard
        } else {
            showAlert(Alert.AlertType.ERROR, "Order Failed", "Failed to place your order. Please try again.");
        }
    }

    private Node createCustomerBookingsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        Label titleLabel = new Label("Your Table Bookings");
        titleLabel.getStyleClass().add("h1-label");

        TableView<TableBooking> bookingsTable = new TableView<>();
        bookingsTable.getStyleClass().add("table-view-custom");
        bookingsTable.setPlaceholder(new Label("No bookings found."));

        TableColumn<TableBooking, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime bookingTime = cellData.getValue().getBookingTime();
            return new SimpleStringProperty(bookingTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        TableColumn<TableBooking, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> {
            LocalDateTime bookingTime = cellData.getValue().getBookingTime();
            return new SimpleStringProperty(bookingTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        });

        TableColumn<TableBooking, String> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTableType().getDisplayValue()));

        TableColumn<TableBooking, Integer> tableNumCol = new TableColumn<>("Table No.");
        tableNumCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));

        TableColumn<TableBooking, String> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus().getDisplayValue()));

        TableColumn<TableBooking, Double> feeCol = new TableColumn<>("Booking Fee");
        feeCol.setCellValueFactory(new PropertyValueFactory<>("bookingFee"));

        bookingsTable.getColumns().addAll(dateCol, timeCol, tableTypeCol, tableNumCol, seatsCol, paymentStatusCol, feeCol);
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ComboBox<TableType> tableTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(TableType.values()));
        tableTypeComboBox.setPromptText("Select Table Type");
        tableTypeComboBox.getStyleClass().add("combo-box-custom");

        Spinner<Integer> seatsSpinner = new Spinner<>(1, 20, 4);
        seatsSpinner.getStyleClass().add("spinner-custom");
        seatsSpinner.setEditable(true);

        DatePicker bookingDatePicker = new DatePicker();
        bookingDatePicker.setPromptText("Select Date");
        bookingDatePicker.getStyleClass().add("date-picker-custom");

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 19);
        hourSpinner.getStyleClass().add("spinner-custom");
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.getStyleClass().add("spinner-custom");

        Label timeLabel = new Label("Time (HH:MM):");
        HBox timeInput = new HBox(5, timeLabel, hourSpinner, new Label(":"), minuteSpinner);
        timeInput.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> durationSpinner = new Spinner<>(30, 240, 60, 15);
        durationSpinner.getStyleClass().add("spinner-custom");
        Label durationLabel = new Label("Duration (minutes):");
        HBox durationInput = new HBox(5, durationLabel, durationSpinner);
        durationInput.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Your Name");
        nameField.getStyleClass().add("text-field-custom");

        TextField phoneField = new TextField(currentUser.getPhoneNumber());
        phoneField.setPromptText("Your Phone");
        phoneField.getStyleClass().add("text-field-custom");

        Button findTablesButton = new Button("Find Available Tables");
        findTablesButton.getStyleClass().add("button-primary");

        TableView<Map<String, Object>> availableTablesView = new TableView<>();
        availableTablesView.getStyleClass().add("table-view-custom");
        availableTablesView.setPlaceholder(new Label("No tables available for selected criteria."));

        TableColumn<Map<String, Object>, Integer> availTableNumCol = new TableColumn<>("Table No.");
        availTableNumCol.setCellValueFactory(cellData -> new SimpleIntegerProperty((Integer) cellData.getValue().get("tableNumber")).asObject());

        TableColumn<Map<String, Object>, String> availTableTypeCol = new TableColumn<>("Type");
        availTableTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue().get("tableType")));

        TableColumn<Map<String, Object>, Integer> availSeatsCol = new TableColumn<>("Seats");
        availSeatsCol.setCellValueFactory(cellData -> new SimpleIntegerProperty((Integer) cellData.getValue().get("seats")).asObject());

        availableTablesView.getColumns().addAll(availTableNumCol, availTableTypeCol, availSeatsCol);
        availableTablesView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button bookSelectedTableButton = new Button("Book Selected Table");
        bookSelectedTableButton.getStyleClass().add("button-success");
        bookSelectedTableButton.setDisable(true);

        availableTablesView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            bookSelectedTableButton.setDisable(newVal == null);
        });

        findTablesButton.setOnAction(e -> {
            if (bookingDatePicker.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Date", "Please select a booking date.");
                return;
            }
            LocalDateTime proposedBookingTime = LocalDateTime.of(bookingDatePicker.getValue(), LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
            int requiredSeats = seatsSpinner.getValue();
            TableType preferredTableType = tableTypeComboBox.getValue();
            int duration = durationSpinner.getValue();

            List<Map<String, Object>> availableTables = DatabaseManager.checkTableAvailability(proposedBookingTime, duration, requiredSeats, preferredTableType);
            availableTablesView.setItems(FXCollections.observableArrayList(availableTables));
        });

        bookSelectedTableButton.setOnAction(e -> {
            Map<String, Object> selectedTable = availableTablesView.getSelectionModel().getSelectedItem();
            if (selectedTable == null) {
                showAlert(Alert.AlertType.WARNING, "No Table Selected", "Please select a table to book.");
                return;
            }

            LocalDateTime bookingDateTime = LocalDateTime.of(bookingDatePicker.getValue(), LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
            int tableNumber = (Integer) selectedTable.get("tableNumber");
            TableType type = TableType.fromString((String) selectedTable.get("tableType"));
            int duration = durationSpinner.getValue();
            double bookingFee = 50.0;

            TableBooking newBooking = new TableBooking(
                    currentUser.getUsername(),
                    nameField.getText(),
                    phoneField.getText(),
                    type,
                    tableNumber,
                    bookingDateTime,
                    duration,
                    bookingFee
            );

            VBox bookingOtpBox = new VBox(10);
            bookingOtpBox.setAlignment(Pos.CENTER);
            bookingOtpBox.setPadding(new Insets(10));
            bookingOtpBox.getStyleClass().add("otp-box");

            Label otpBookingMessageLabel = new Label("An OTP has been sent for your booking confirmation.");
            TextField otpBookingDisplayField = new TextField(String.valueOf(generateOtp()));
            otpBookingDisplayField.setEditable(false);
            otpBookingDisplayField.getStyleClass().add("text-field-custom");
            otpBookingDisplayField.setMaxWidth(200);

            TextField otpBookingInputField = new TextField();
            otpBookingInputField.setPromptText("Enter OTP");
            otpBookingInputField.getStyleClass().add("text-field-custom");
            otpBookingInputField.setMaxWidth(200);

            Button verifyBookingOtpButton = new Button("Verify Booking OTP");
            verifyBookingOtpButton.getStyleClass().add("button-primary");
            verifyBookingOtpButton.setMaxWidth(200);

            bookingOtpBox.getChildren().addAll(otpBookingMessageLabel, otpBookingDisplayField, otpBookingInputField, verifyBookingOtpButton);

            Alert otpAlert = new Alert(Alert.AlertType.CONFIRMATION);
            otpAlert.setTitle("Table Booking OTP Verification");
            otpAlert.setHeaderText("Please verify your booking with OTP");
            otpAlert.getDialogPane().setContent(bookingOtpBox);
            styleAlertDialog(otpAlert, primaryStage.getScene().getWindow());

            verifyBookingOtpButton.setOnAction(verifyE -> {
                try {
                    int enteredOtp = Integer.parseInt(otpBookingInputField.getText());
                    int displayedOtp = Integer.parseInt(otpBookingDisplayField.getText());

                    if (enteredOtp == displayedOtp) {
                        if (DatabaseManager.addTableBooking(newBooking)) {
                            showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed", "Table " + tableNumber + " booked successfully!");
                            loadCustomerBookings(bookingsTable);
                            otpAlert.close();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Booking Failed", "Failed to book table. Please try again.");
                            otpAlert.close();
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "OTP Mismatch", "Incorrect OTP. Please try again.");
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid OTP", "Please enter a valid numeric OTP.");
                }
            });

            otpAlert.showAndWait();
        });


        GridPane bookingInputGrid = new GridPane();
        bookingInputGrid.setHgap(10);
        bookingInputGrid.setVgap(10);
        bookingInputGrid.setPadding(new Insets(10));
        bookingInputGrid.getStyleClass().add("form-grid");

        bookingInputGrid.add(new Label("Your Name:"), 0, 0);
        bookingInputGrid.add(nameField, 1, 0);
        bookingInputGrid.add(new Label("Phone Number:"), 0, 1);
        bookingInputGrid.add(phoneField, 1, 1);
        bookingInputGrid.add(new Label("Preferred Table Type:"), 0, 2);
        bookingInputGrid.add(tableTypeComboBox, 1, 2);
        bookingInputGrid.add(new Label("Required Seats:"), 0, 3);
        bookingInputGrid.add(seatsSpinner, 1, 3);
        bookingInputGrid.add(new Label("Booking Date:"), 0, 4);
        bookingInputGrid.add(bookingDatePicker, 1, 4);
        bookingInputGrid.add(timeInput, 0, 5, 2, 1);
        bookingInputGrid.add(durationInput, 0, 6, 2, 1);
        bookingInputGrid.add(findTablesButton, 0, 7, 2, 1);
        GridPane.setHalignment(findTablesButton, HPos.CENTER); // Corrected: Changed Pos.CENTER to HPos.CENTER


        HBox tablesActionBox = new HBox(10, availableTablesView, bookSelectedTableButton);
        tablesActionBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(availableTablesView, Priority.ALWAYS);
        VBox.setVgrow(tablesActionBox, Priority.ALWAYS);

        content.getChildren().addAll(titleLabel, bookingsTable, new Separator(), new Label("Make a New Booking"), bookingInputGrid, tablesActionBox);

        loadCustomerBookings(bookingsTable);
        return content;
    }

    private void loadCustomerBookings(TableView<TableBooking> bookingsTable) {
        List<TableBooking> customerBookings = DatabaseManager.getCustomerTableBookings(currentUser.getUsername());
        bookingsTable.setItems(FXCollections.observableArrayList(customerBookings));
    }

    private Node createFeedbackTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        Label titleLabel = new Label("Submit Your Feedback");
        titleLabel.getStyleClass().add("h1-label");

        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Enter your comments here...");
        commentsArea.setPrefRowCount(5);
        commentsArea.getStyleClass().add("text-area-custom");

        Label ratingLabel = new Label("Overall Rating (1-5 Stars):");
        ratingLabel.getStyleClass().add("body-text");

        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
        ratingSpinner.getStyleClass().add("spinner-custom");
        ratingSpinner.setEditable(true);

        Button submitFeedbackButton = new Button("Submit Feedback");
        submitFeedbackButton.getStyleClass().add("button-primary");
        submitFeedbackButton.setMaxWidth(200);
        submitFeedbackButton.setOnAction(e -> {
            String comments = commentsArea.getText();
            int rating = ratingSpinner.getValue();

            if (comments.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Comments", "Please provide your comments.");
                return;
            }

            if (DatabaseManager.addFeedback(currentUser.getUsername(), rating, comments)) {
                showAlert(Alert.AlertType.INFORMATION, "Feedback Submitted", "Thank you for your feedback!");
                commentsArea.clear();
                ratingSpinner.getValueFactory().setValue(5);
            } else {
                showAlert(Alert.AlertType.ERROR, "Submission Failed", "Failed to submit feedback. Please try again.");
            }
        });

        Separator separator = new Separator();

        Label dishRatingLabel = new Label("Rate a Specific Dish");
        dishRatingLabel.getStyleClass().add("h2-label");

        ComboBox<MenuItem> dishComboBox = new ComboBox<>();
        dishComboBox.setPromptText("Select a Dish");
        dishComboBox.setItems(FXCollections.observableArrayList(DatabaseManager.getAllMenuItems()));
        dishComboBox.setConverter(new javafx.util.StringConverter<MenuItem>() {
            @Override
            public String toString(MenuItem object) {
                return object != null ? object.getName() : "";
            }
            @Override
            public MenuItem fromString(String string) {
                return DatabaseManager.getAllMenuItems().stream()
                        .filter(item -> item.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
        dishComboBox.getStyleClass().add("combo-box-custom");

        Label dishRatingValueLabel = new Label("Dish Rating (1-5 Stars):");
        Spinner<Integer> dishRatingSpinner = new Spinner<>(1, 5, 5);
        dishRatingSpinner.getStyleClass().add("spinner-custom");
        dishRatingSpinner.setEditable(true);

        Button submitDishRatingButton = new Button("Submit Dish Rating");
        submitDishRatingButton.getStyleClass().add("button-primary");
        submitDishRatingButton.setMaxWidth(200);
        submitDishRatingButton.setOnAction(e -> {
            MenuItem selectedDish = dishComboBox.getValue();
            int dishRating = dishRatingSpinner.getValue();

            if (selectedDish == null) {
                showAlert(Alert.AlertType.WARNING, "No Dish Selected", "Please select a dish to rate.");
                return;
            }

            if (DatabaseManager.addDishRating(selectedDish.getId(), currentUser.getUsername(), dishRating)) {
                showAlert(Alert.AlertType.INFORMATION, "Dish Rated", selectedDish.getName() + " rated successfully!");
                dishComboBox.getSelectionModel().clearSelection();
                dishRatingSpinner.getValueFactory().setValue(5);
            } else {
                showAlert(Alert.AlertType.ERROR, "Rating Failed", "Failed to submit dish rating. Please try again.");
            }
        });


        VBox overallFeedbackSection = new VBox(10, titleLabel, ratingLabel, ratingSpinner, commentsArea, submitFeedbackButton);
        overallFeedbackSection.getStyleClass().add("section-box");
        overallFeedbackSection.setPadding(new Insets(15));

        VBox dishFeedbackSection = new VBox(10, dishRatingLabel, dishComboBox, dishRatingSpinner, submitDishRatingButton);
        dishFeedbackSection.getStyleClass().add("section-box");
        dishFeedbackSection.setPadding(new Insets(15));

        content.getChildren().addAll(overallFeedbackSection, separator, dishFeedbackSection);
        return content;
    }


    private void showAdminPortal() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("content-pane");

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.getStyleClass().add("top-bar");

        Label welcomeLabel = new Label("Admin Portal - " + currentUser.getFullName());
        welcomeLabel.getStyleClass().add("h2-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button-danger");
        logoutButton.setOnAction(e -> {
            currentUser = null;
            showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been logged out.");
            showLoginScene();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        root.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("custom-tab-pane");

        Tab menuMgmtTab = new Tab("Menu Management");
        menuMgmtTab.setClosable(false);
        menuMgmtTab.setContent(createMenuManagementTab());

        Tab orderMgmtTab = new Tab("Order Management");
        orderMgmtTab.setClosable(false);
        orderMgmtTab.setContent(createOrderManagementTab());

        Tab bookingMgmtTab = new Tab("Booking Management");
        bookingMgmtTab.setClosable(false);
        bookingMgmtTab.setContent(createBookingManagementTab());

        Tab userMgmtTab = new Tab("User Management");
        userMgmtTab.setClosable(false);
        userMgmtTab.setContent(createUserManagementTab());

        Tab feedbackReportsTab = new Tab("Feedback & Reports");
        feedbackReportsTab.setClosable(false);
        feedbackReportsTab.setContent(createFeedbackReportsTab());

        tabPane.getTabs().addAll(menuMgmtTab, orderMgmtTab, bookingMgmtTab, userMgmtTab, feedbackReportsTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 800);
        applyCss(scene);
        primaryStage.setScene(scene);
    }

    private Node createMenuManagementTab() {
        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        VBox addEditForm = new VBox(15);
        addEditForm.getStyleClass().add("section-box");
        addEditForm.setPrefWidth(350);
        addEditForm.setAlignment(Pos.TOP_CENTER);
        addEditForm.setPadding(new Insets(20));

        Label formTitle = new Label("Add/Edit Menu Item");
        formTitle.getStyleClass().add("h2-label");

        TextField itemIdField = new TextField();
        itemIdField.setPromptText("Item ID (Auto-generated)");
        itemIdField.setEditable(false);
        itemIdField.getStyleClass().add("text-field-custom");

        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Item Name");
        itemNameField.getStyleClass().add("text-field-custom");

        TextField itemPriceField = new TextField();
        itemPriceField.setPromptText("Item Price");
        itemPriceField.getStyleClass().add("text-field-custom");

        TextField itemImageUrlField = new TextField();
        itemImageUrlField.setPromptText("Image URL (e.g., https://placehold.co/100x100)");
        itemImageUrlField.getStyleClass().add("text-field-custom");

        ImageView previewImageView = new ImageView();
        previewImageView.setFitWidth(100);
        previewImageView.setFitHeight(100);
        previewImageView.setPreserveRatio(false);
        previewImageView.getStyleClass().add("image-preview");

        itemImageUrlField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.trim().isEmpty()) {
                    previewImageView.setImage(new Image(newVal, true));
                } else {
                    previewImageView.setImage(null);
                }
            } catch (Exception ex) {
                previewImageView.setImage(null);
            }
        });

        Button saveButton = new Button("Add Item");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        Button clearButton = new Button("Clear Form");
        clearButton.getStyleClass().add("button-secondary");
        clearButton.setMaxWidth(Double.MAX_VALUE);

        addEditForm.getChildren().addAll(formTitle, itemIdField, itemNameField, itemPriceField, itemImageUrlField, previewImageView, saveButton, clearButton);

        VBox menuListSection = new VBox(15);
        menuListSection.getStyleClass().add("section-box");
        menuListSection.setPadding(new Insets(20));
        menuListSection.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(menuListSection, Priority.ALWAYS);

        Label listTitle = new Label("Current Menu Items");
        listTitle.getStyleClass().add("h2-label");

        TableView<MenuItem> menuTableView = new TableView<>();
        menuTableView.getStyleClass().add("table-view-custom");
        menuTableView.setPlaceholder(new Label("No menu items available."));

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

        TableColumn<MenuItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(param -> new TableCell<MenuItem, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("button-info-small");
                deleteButton.getStyleClass().add("button-danger-small");

                editButton.setOnAction(event -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    itemIdField.setText(String.valueOf(item.getId()));
                    itemNameField.setText(item.getName());
                    itemPriceField.setText(String.valueOf(item.getPrice()));
                    itemImageUrlField.setText(item.getImageUrl());
                    saveButton.setText("Update Item");
                });

                deleteButton.setOnAction(event -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    Optional<ButtonType> result = showAlert(Alert.AlertType.CONFIRMATION, "Confirm Deletion", "Are you sure you want to delete '" + item.getName() + "'?").showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        if (DatabaseManager.deleteMenuItem(item.getId())) {
                            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Menu item deleted successfully.");
                            loadMenuItems(menuTableView);
                            clearForm(itemIdField, itemNameField, itemPriceField, itemImageUrlField, previewImageView, saveButton);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete menu item.");
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
                    setGraphic(pane);
                }
            }
        });

        menuTableView.getColumns().addAll(idCol, nameCol, priceCol, imageUrlCol, actionsCol);
        menuTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadMenuItems(menuTableView);

        saveButton.setOnAction(e -> {
            String idText = itemIdField.getText();
            String name = itemNameField.getText();
            String priceText = itemPriceField.getText();
            String imageUrl = itemImageUrlField.getText();

            if (name.isEmpty() || priceText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in item name and price.");
                return;
            }

            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Price", "Price must be positive.");
                    return;
                }

                if (idText.isEmpty() || saveButton.getText().equals("Add Item")) {
                    // Add new item
                    if (DatabaseManager.addMenuItem(name, price, imageUrl)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Menu item added successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add menu item. Name might already exist.");
                    }
                } else {
                    // Update existing item
                    int id = Integer.parseInt(idText);
                    if (DatabaseManager.updateMenuItem(id, name, price, imageUrl)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Menu item updated successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update menu item.");
                    }
                }
                loadMenuItems(menuTableView);
                clearForm(itemIdField, itemNameField, itemPriceField, itemImageUrlField, previewImageView, saveButton);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price must be a valid number.");
            }
        });

        clearButton.setOnAction(e -> clearForm(itemIdField, itemNameField, itemPriceField, itemImageUrlField, previewImageView, saveButton));

        menuListSection.getChildren().addAll(listTitle, menuTableView);
        content.getChildren().addAll(addEditForm, menuListSection);

        return content;
    }

    private void loadMenuItems(TableView<MenuItem> menuTableView) {
        ObservableList<MenuItem> menuItems = FXCollections.observableArrayList(DatabaseManager.getAllMenuItems());
        menuTableView.setItems(menuItems);
    }

    private void clearForm(TextField itemIdField, TextField itemNameField, TextField itemPriceField, TextField itemImageUrlField, ImageView previewImageView, Button saveButton) {
        itemIdField.clear();
        itemNameField.clear();
        itemPriceField.clear();
        itemImageUrlField.clear();
        previewImageView.setImage(null);
        saveButton.setText("Add Item");
    }

    private Node createOrderManagementTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        Label titleLabel = new Label("Order Management");
        titleLabel.getStyleClass().add("h1-label");

        TableView<Order> ordersTable = new TableView<>();
        ordersTable.getStyleClass().add("table-view-custom");
        ordersTable.setPlaceholder(new Label("No orders found."));

        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));

        TableColumn<Order, String> orderTimeCol = new TableColumn<>("Order Time");
        orderTimeCol.setCellValueFactory(cellData -> {
            LocalDateTime orderTime = cellData.getValue().getOrderTime();
            return new SimpleStringProperty(orderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                cellData.getValue().getItems().stream()
                        .map(MenuItem::getName)
                        .collect(Collectors.joining(", "))
        ));
        itemsCol.setPrefWidth(250);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total (Rs.)");
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalWithGST()).asObject());

        TableColumn<Order, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(OrderStatus.values()));
        statusCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            OrderStatus newStatus = event.getNewValue();
            if (DatabaseManager.updateOrderStatus(order.orderId, newStatus)) {
                order.setStatus(newStatus); // Corrected: Use setter
                showAlert(Alert.AlertType.INFORMATION, "Order Status Updated", "Order " + order.orderId + " status changed to " + newStatus.getDisplayValue());
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update order status.");
            }
        });

        TableColumn<Order, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentStatus.values()));
        paymentStatusCol.setOnEditCommit(event -> {
            Order order = event.getRowValue();
            PaymentStatus newPaymentStatus = event.getNewValue(); // Corrected: Directly get enum value
            if (DatabaseManager.updateOrderPaymentStatus(order.orderId, newPaymentStatus)) {
                order.setPaymentStatus(newPaymentStatus); // Corrected: Use setter
                showAlert(Alert.AlertType.INFORMATION, "Payment Status Updated", "Order " + order.orderId + " payment status changed to " + newPaymentStatus.getDisplayValue());
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update payment status.");
            }
        });

        ordersTable.getColumns().addAll(orderIdCol, customerCol, orderTimeCol, itemsCol, totalCol, statusCol, paymentStatusCol);
        ordersTable.setEditable(true);
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Refresh Orders");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadOrders(ordersTable));

        VBox controls = new VBox(10, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(titleLabel, controls, ordersTable);
        loadOrders(ordersTable);
        return content;
    }

    private void loadOrders(TableView<Order> ordersTable) {
        ObservableList<Order> orders = FXCollections.observableArrayList(DatabaseManager.getAllOrders());
        ordersTable.setItems(orders);
    }

    private Node createBookingManagementTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        Label titleLabel = new Label("Table Booking Management");
        titleLabel.getStyleClass().add("h1-label");

        TableView<TableBooking> bookingsTable = new TableView<>();
        bookingsTable.getStyleClass().add("table-view-custom");
        bookingsTable.setPlaceholder(new Label("No bookings found."));

        TableColumn<TableBooking, String> customerIdCol = new TableColumn<>("Customer ID");
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<TableBooking, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<TableBooking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<TableBooking, String> tableTypeCol = new TableColumn<>("Table Type");
        tableTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTableType().getDisplayValue()));

        TableColumn<TableBooking, Integer> tableNumCol = new TableColumn<>("Table No.");
        tableNumCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));

        TableColumn<TableBooking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));

        TableColumn<TableBooking, String> bookingTimeCol = new TableColumn<>("Booking Time");
        bookingTimeCol.setCellValueFactory(cellData -> {
            LocalDateTime bookingTime = cellData.getValue().getBookingTime();
            return new SimpleStringProperty(bookingTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        TableColumn<TableBooking, Integer> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        TableColumn<TableBooking, PaymentStatus> paymentStatusCol = new TableColumn<>("Payment Status");
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        paymentStatusCol.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentStatus.values()));
        paymentStatusCol.setOnEditCommit(event -> {
            TableBooking booking = event.getRowValue();
            PaymentStatus newStatus = event.getNewValue(); // Corrected: Directly get enum value
            if (DatabaseManager.updateBookingPaymentStatus(booking.tableNumber, booking.getBookingTime(), newStatus)) {
                booking.setPaymentStatus(newStatus);
                showAlert(Alert.AlertType.INFORMATION, "Booking Payment Updated", "Booking for Table " + booking.tableNumber + " payment status changed to " + newStatus.getDisplayValue());
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update booking payment status.");
            }
        });


        bookingsTable.getColumns().addAll(customerIdCol, customerNameCol, phoneCol, tableTypeCol, tableNumCol, seatsCol, bookingTimeCol, durationCol, paymentStatusCol);
        bookingsTable.setEditable(true);
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Refresh Bookings");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadAllBookings(bookingsTable));

        VBox controls = new VBox(10, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(titleLabel, controls, bookingsTable);
        loadAllBookings(bookingsTable);
        return content;
    }

    private void loadAllBookings(TableView<TableBooking> bookingsTable) {
        ObservableList<TableBooking> bookings = FXCollections.observableArrayList(DatabaseManager.getAllTableBookings());
        bookingsTable.setItems(bookings);
    }

    private Node createUserManagementTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        Label titleLabel = new Label("User Management");
        titleLabel.getStyleClass().add("h1-label");

        TableView<User> userTable = new TableView<>();
        userTable.getStyleClass().add("table-view-custom");
        userTable.setPlaceholder(new Label("No users found."));

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<User, UserRole> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(ComboBoxTableCell.forTableColumn(UserRole.values()));
        roleCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            UserRole newRole = event.getNewValue();
            if (DatabaseManager.updateUserRole(user.getUsername(), newRole)) {
                user.setRole(newRole);
                showAlert(Alert.AlertType.INFORMATION, "Role Updated", user.getUsername() + "'s role changed to " + newRole.getDisplayValue());
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update user role.");
            }
        });

        userTable.getColumns().addAll(usernameCol, fullNameCol, emailCol, phoneCol, roleCol);
        userTable.setEditable(true);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Refresh Users");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadUsers(userTable));

        VBox controls = new VBox(10, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(titleLabel, controls, userTable);
        loadUsers(userTable);
        return content;
    }

    private void loadUsers(TableView<User> userTable) {
        ObservableList<User> users = FXCollections.observableArrayList(DatabaseManager.getAllUsers());
        userTable.setItems(users);
    }

    private Node createFeedbackReportsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card-pane");

        Label titleLabel = new Label("Feedback and Reports");
        titleLabel.getStyleClass().add("h1-label");

        TabPane reportsTabPane = new TabPane();
        reportsTabPane.getStyleClass().add("custom-sub-tab-pane");

        Tab allFeedbackTab = new Tab("All Customer Feedback");
        allFeedbackTab.setClosable(false);
        allFeedbackTab.setContent(createAllFeedbackView());

        Tab dishRatingsTab = new Tab("Dish Ratings");
        dishRatingsTab.setClosable(false);
        dishRatingsTab.setContent(createDishRatingsView());

        Tab topDishesTab = new Tab("Top Dishes");
        topDishesTab.setClosable(false);
        topDishesTab.setContent(createTopDishesView());

        reportsTabPane.getTabs().addAll(allFeedbackTab, dishRatingsTab, topDishesTab);

        content.getChildren().addAll(titleLabel, reportsTabPane);
        return content;
    }

    private Node createAllFeedbackView() {
        VBox feedbackView = new VBox(15);
        feedbackView.setPadding(new Insets(15));

        TableView<Feedback> feedbackTable = new TableView<>();
        feedbackTable.getStyleClass().add("table-view-custom");
        feedbackTable.setPlaceholder(new Label("No general feedback found."));

        TableColumn<Feedback, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));

        TableColumn<Feedback, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        TableColumn<Feedback, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        commentsCol.setPrefWidth(300);
        commentsCol.setCellFactory(tc -> {
            TableCell<Feedback, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.textProperty().bind(cell.itemProperty());
            text.wrappingWidthProperty().bind(commentsCol.widthProperty());
            return cell ;
        });


        TableColumn<Feedback, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFeedbackDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        feedbackTable.getColumns().addAll(customerCol, ratingCol, commentsCol, dateCol);
        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Refresh Feedback");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadFeedback(feedbackTable));

        VBox controls = new VBox(10, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        feedbackView.getChildren().addAll(controls, feedbackTable);
        loadFeedback(feedbackTable);
        return feedbackView;
    }

    private void loadFeedback(TableView<Feedback> feedbackTable) {
        ObservableList<Feedback> feedback = FXCollections.observableArrayList(DatabaseManager.getAllFeedback());
        feedbackTable.setItems(feedback);
    }

    private Node createDishRatingsView() {
        VBox dishRatingsView = new VBox(15);
        dishRatingsView.setPadding(new Insets(15));

        TableView<DishRating> dishRatingTable = new TableView<>();
        dishRatingTable.getStyleClass().add("table-view-custom");
        dishRatingTable.setPlaceholder(new Label("No dish ratings found."));

        TableColumn<DishRating, String> dishNameCol = new TableColumn<>("Dish Name");
        dishNameCol.setCellValueFactory(cellData -> {
            MenuItem item = DatabaseManager.getMenuItemById(cellData.getValue().getMenuItemId());
            return new SimpleStringProperty(item != null ? item.getName() : "Unknown Dish");
        });

        TableColumn<DishRating, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerUsername"));

        TableColumn<DishRating, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        TableColumn<DishRating, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRatingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        dishRatingTable.getColumns().addAll(dishNameCol, customerCol, ratingCol, dateCol);
        dishRatingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Refresh Dish Ratings");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadDishRatings(dishRatingTable));

        VBox controls = new VBox(10, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        dishRatingsView.getChildren().addAll(controls, dishRatingTable);
        loadDishRatings(dishRatingTable);
        return dishRatingsView;
    }

    private void loadDishRatings(TableView<DishRating> dishRatingTable) {
        ObservableList<DishRating> dishRatings = FXCollections.observableArrayList(DatabaseManager.getAllDishRatings());
        dishRatingTable.setItems(dishRatings);
    }

    private Node createTopDishesView() {
        VBox topDishesView = new VBox(15);
        topDishesView.setPadding(new Insets(15));

        Label topDishesLabel = new Label("Top 5 Dishes by Average Rating");
        topDishesLabel.getStyleClass().add("h2-label");

        TableView<Map.Entry<String, Double>> topDishesTable = new TableView<>();
        topDishesTable.getStyleClass().add("table-view-custom");
        topDishesTable.setPlaceholder(new Label("No dish ratings yet to calculate top dishes."));

        TableColumn<Map.Entry<String, Double>, String> dishNameCol = new TableColumn<>("Dish Name");
        dishNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Double>, Double> avgRatingCol = new TableColumn<>("Average Rating");
        avgRatingCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValue()).asObject());

        topDishesTable.getColumns().addAll(dishNameCol, avgRatingCol);
        topDishesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshButton = new Button("Refresh Top Dishes");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadTopDishes(topDishesTable));

        VBox controls = new VBox(10, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        topDishesView.getChildren().addAll(topDishesLabel, controls, topDishesTable);
        loadTopDishes(topDishesTable);
        return topDishesView;
    }

    private void loadTopDishes(TableView<Map.Entry<String, Double>> topDishesTable) {
        List<DishRating> allRatings = DatabaseManager.getAllDishRatings();
        Map<Integer, List<Integer>> ratingsByMenuItem = new HashMap<>();

        for (DishRating rating : allRatings) {
            ratingsByMenuItem.computeIfAbsent(rating.getMenuItemId(), k -> new ArrayList<>()).add(rating.getRating());
        }

        Map<String, Double> averageRatings = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : ratingsByMenuItem.entrySet()) {
            MenuItem item = DatabaseManager.getMenuItemById(entry.getKey());
            if (item != null) {
                double avg = entry.getValue().stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
                averageRatings.put(item.getName(), avg);
            }
        }

        List<Map.Entry<String, Double>> sortedTopDishes = averageRatings.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        topDishesTable.setItems(FXCollections.observableArrayList(sortedTopDishes));
    }


    private void applyCss(Scene scene) {
        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: style.css not found. UI may not be styled correctly.");
        }
    }

    private Alert showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlertDialog(alert, primaryStage.getScene().getWindow());
        alert.show();
        return alert;
    }

    private void styleAlertDialog(Alert alert, Window owner) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog-custom");

        ImageView icon = null;
        if (alert.getAlertType() == Alert.AlertType.INFORMATION) {
            icon = new ImageView(new Image("https://placehold.co/32x32/8BC34A/FFFFFF?text=i"));
        } else if (alert.getAlertType() == Alert.AlertType.WARNING) {
            icon = new ImageView(new Image("https://placehold.co/32x32/FFC107/FFFFFF?text=!_"));
        } else if (alert.getAlertType() == Alert.AlertType.ERROR) {
            icon = new ImageView(new Image("https://placehold.co/32x32/F44336/FFFFFF?text=X"));
        } else if (alert.getAlertType() == Alert.AlertType.CONFIRMATION) {
            icon = new ImageView(new Image("https://placehold.co/32x32/3F51B5/FFFFFF?text=?"));
        }
        if (icon != null) {
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            dialogPane.setGraphic(icon);
        }
        alert.initOwner(owner);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
