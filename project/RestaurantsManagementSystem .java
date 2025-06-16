import java.util.*;
import java.time.LocalDate; // For potential daily sales report timestamp

public class RestaurantsManagementSystem {

    // --- Constants ---
    private static final int ADMIN_PIN = 2004;
    private static final String DISCOUNT_COUPON_CODE = "SHEILD2004";
    private static final double DISCOUNT_AMOUNT = 100.00;
    private static final double DISCOUNT_MIN_ORDER_PRICE = 500.00;

    // --- Nested Classes (already well-defined, minor tweaks) ---
    static class MenuItem {
        private int id;
        private String name;
        private double price;

        public MenuItem(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "ID: " + id + " | " + name + " - Rs." + String.format("%.2f", price);
        }
    }

    static class Order {
        int orderId;
        List<MenuItem> items = new ArrayList<>();
        String status = "placed"; // e.g., "placed", "confirmed and paid", "cancelled"
        String paymentStatus = "pending"; // e.g., "pending", "paid", "failed"
        double discountApplied = 0;

        public Order(int orderId) {
            this.orderId = orderId;
        }

        void addItem(MenuItem item) {
            items.add(item);
        }

        double getPrice() {
            double total = 0;
            for (MenuItem item : items) {
                total += item.getPrice();
            }
            return total;
        }

        double getFinalPrice() {
            return getPrice() - discountApplied;
        }

        public void display() {
            System.out.println("Order ID: " + orderId);
            System.out.println("--- Items ---");
            if (items.isEmpty()) {
                System.out.println("No items in this order.");
            } else {
                for (MenuItem item : items) {
                    System.out.println("  - " + item.getName() + " | Price: Rs." + String.format("%.2f", item.getPrice()));
                }
            }
            System.out.println("-----------------");
            System.out.println("Subtotal: Rs." + String.format("%.2f", getPrice()));
            if (discountApplied > 0) {
                System.out.println("Discount Applied: Rs." + String.format("%.2f", discountApplied));
                System.out.println("Final Price: Rs." + String.format("%.2f", getFinalPrice()));
            }
            System.out.println("Status: " + status);
            System.out.println("Payment Status: " + paymentStatus);
        }
    }

    static class TableBooking {
        String tableType;
        int tableNumber;
        String customerName;
        String phone;
        int seats;
        String customerId;
        String paymentStatus = "pending";
        double bookingFee;
        LocalDate bookingDate; // Added for potential future enhancements (e.g., date-specific reports)

        public TableBooking(String tableType, int tableNumber, String customerName, String phone, int seats, String customerId, double bookingFee) {
            this.tableType = tableType;
            this.tableNumber = tableNumber;
            this.customerName = customerName;
            this.phone = phone;
            this.seats = seats;
            this.customerId = customerId;
            this.bookingFee = bookingFee;
            this.bookingDate = LocalDate.now(); // Set current date
        }

        public void display() {
            System.out.println("Customer ID: " + customerId +
                    " | Table: " + tableType + " #" + tableNumber +
                    " | Seats: " + seats +
                    " | Name: " + customerName +
                    " | Phone: " + phone +
                    " | Booking Fee: Rs." + String.format("%.2f", bookingFee) +
                    " | Payment Status: " + paymentStatus +
                    " | Date: " + bookingDate); // Display date
        }
    }

    // --- Global Data Stores (still in-memory for now, addressed later) ---
    static List<MenuItem> menuList = new ArrayList<>();
    static Map<Integer, MenuItem> menuMap = new HashMap<>(); // For faster lookup by ID
    static List<Order> allOrders = new ArrayList<>();
    static List<TableBooking> allBookings = new ArrayList<>(); // Renamed 'bookings' to 'allBookings' for clarity
    // No longer need 'paidBookings' separately, can filter from 'allBookings'

    // Table availability arrays
    static boolean[] tables10 = new boolean[10]; // 10 tables for 10 people
    static boolean[] tables8 = new boolean[10];  // 10 tables for 8 people
    static boolean[] tables6 = new boolean[10];  // 10 tables for 6 people
    static boolean[] tables4 = new boolean[10];  // 10 tables for 4 people
    static boolean[] tables2 = new boolean[10];  // 10 tables for 2 people

    // --- Counters ---
    static int orderCounter = 1;
    static int customerIdCounter = 1001;
    static int menuItemIdCounter = 101;

    // Current active order for a customer session.
    // This implies only one customer can place an order at a time or
    // a customer finishes one order before starting another.
    // In a multi-user system, this would be tied to a specific session/user.
    static Order currentCustomerOrder = null; // Renamed 'currentOrder' for clarity
    static List<MenuItem> currentCart = new ArrayList<>(); // To represent the items being added to the currentCustomerOrder

    // --- Main Method ---
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        initializeMenu();
        System.out.println("Welcome to the Shield Restaurant");

        boolean runningSystem = true;
        while (runningSystem) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Admin access");
            System.out.println("2. Customer access");
            System.out.println("0. Exit System");
            System.out.print("Please select your access level (0, 1 or 2): ");

            int accessLevel = -1;
            try {
                accessLevel = Integer.parseInt(s.nextLine()); // Use nextLine to consume the whole line
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue; // Restart the loop
            }

            switch (accessLevel) {
                case 1:
                    System.out.print("Enter 4-digit admin PIN: ");
                    String pinInput = s.nextLine();
                    try {
                        int pin = Integer.parseInt(pinInput);
                        if (pin == ADMIN_PIN) {
                            adminAccess(s);
                        } else {
                            System.out.println("Invalid PIN. Access denied.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PIN format. Please enter a number.");
                    }
                    break;
                case 2:
                    customerAccess(s);
                    break;
                case 0:
                    runningSystem = false;
                    System.out.println("Exiting the system. Thank you for visiting Shield Restaurant!");
                    break;
                default:
                    System.out.println("Invalid access level. Please enter 0, 1 or 2.");
            }
        }
        s.close(); // Close the scanner when done
    }

    // --- Initialization ---
    public static void initializeMenu() {
        // Clear existing menu to prevent duplicates if called multiple times (though not intended here)
        menuList.clear();
        menuMap.clear();

        String[] names = {"chicken biryani", "mutton biryani", "veg biryani", "chicken curry", "mutton curry", "veg curry", "chicken tikka", "mutton tikka", "veg tikka", "chicken kebab", "mutton kebab", "veg kebab", "soft drink", "water", "salad", "dessert"};
        double[] prices = {150.00, 200.00, 100.00, 180.00, 220.00, 120.00, 160.00, 210.00, 130.00, 170.00, 230.00, 140.00, 50.00, 20.00, 30.00, 80.00};
        for (int i = 0; i < names.length; i++) {
            MenuItem item = new MenuItem(menuItemIdCounter, names[i], prices[i]);
            menuList.add(item);
            menuMap.put(menuItemIdCounter, item);
            menuItemIdCounter++;
        }
        // Initialize tables to all be false (available)
        Arrays.fill(tables2, false);
        Arrays.fill(tables4, false);
        Arrays.fill(tables6, false);
        Arrays.fill(tables8, false);
        Arrays.fill(tables10, false);
    }

    // --- Customer-facing methods ---
    public static void displayMenu() {
        System.out.println("\n====== VEG MENU ======");
        menuList.stream()
                .filter(item -> {
                    String n = item.getName().toLowerCase();
                    return n.contains("veg") || n.equals("salad") || n.equals("dessert");
                })
                .forEach(System.out::println);

        System.out.println("\n==== NON-VEG MENU ====");
        menuList.stream()
                .filter(item -> {
                    String n = item.getName().toLowerCase();
                    return (n.contains("chicken") || n.contains("mutton")) && !n.contains("veg");
                })
                .forEach(System.out::println);

        System.out.println("\n== BEVERAGES & EXTRAS ==");
        menuList.stream()
                .filter(item -> {
                    String n = item.getName().toLowerCase();
                    return n.equals("soft drink") || n.equals("water");
                })
                .forEach(System.out::println);

        System.out.println("\n** Use coupon " + DISCOUNT_COUPON_CODE + " for Rs." + String.format("%.2f", DISCOUNT_AMOUNT) + " off on orders above Rs." + String.format("%.2f", DISCOUNT_MIN_ORDER_PRICE) + "! **");
    }

    public static void addMultipleItemsToCart(String input) {
        // Initialize current order and cart if not already started
        if (currentCustomerOrder == null) {
            currentCustomerOrder = new Order(orderCounter++);
            currentCart.clear(); // Ensure cart is clean for a new order
            System.out.println("New Order ID generated: " + currentCustomerOrder.orderId);
        }

        String[] ids = input.split(",");
        boolean anyItemAdded = false;
        for (String idStr : ids) {
            try {
                int id = Integer.parseInt(idStr.trim());
                MenuItem item = menuMap.get(id);
                if (item != null) {
                    currentCart.add(item); // Add to the temporary cart for current session
                    currentCustomerOrder.addItem(item); // Also add to the current order object
                    System.out.println("Added: " + item.getName());
                    anyItemAdded = true;
                } else {
                    System.out.println("Item ID " + id + " not found.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format: '" + idStr + "'. Please enter numbers only.");
            }
        }
        if (anyItemAdded) {
            System.out.println("Items added to current order (ID: " + currentCustomerOrder.orderId + ").");
        } else {
            System.out.println("No valid items were added to the cart.");
        }
    }

    public static void viewCart() {
        if (currentCustomerOrder == null || currentCart.isEmpty()) { // Check currentCart specifically
            System.out.println("Cart is empty or no current order initiated.");
            return;
        }
        System.out.println("\n--- Your Current Cart (Order ID: " + currentCustomerOrder.orderId + ") ---");
        double total = 0;
        for (MenuItem item : currentCart) { // Iterate through currentCart
            total += item.getPrice();
            System.out.println("Item: " + item.getName() + " | Price: Rs." + String.format("%.2f", item.getPrice()));
        }
        System.out.println("------------------------------------");
        System.out.println("Subtotal: Rs." + String.format("%.2f", total));
        System.out.println("Note: Final price with discount will be shown at payment.");
    }

    public static void placeOrder() {
        if (currentCustomerOrder == null || currentCart.isEmpty()) {
            System.out.println("No items in cart to place an order.");
            return;
        }

        // Only add to allOrders if it's a new order (not just updating existing)
        boolean orderExistsInAllOrders = false;
        for (Order o : allOrders) {
            if (o.orderId == currentCustomerOrder.orderId) {
                orderExistsInAllOrders = true;
                break;
            }
        }
        if (!orderExistsInAllOrders) {
            allOrders.add(currentCustomerOrder);
        }

        currentCustomerOrder.status = "placed"; // Update status
        System.out.println("Order " + currentCustomerOrder.orderId + " has been placed. Please proceed to confirm and pay.");
    }

    public static void confirmOrder(Scanner s) {
        if (currentCustomerOrder == null || currentCart.isEmpty() || !currentCustomerOrder.status.equals("placed")) {
            System.out.println("No active 'placed' order to confirm or cart is empty.");
            if (currentCustomerOrder != null && currentCustomerOrder.paymentStatus.equals("paid")) {
                System.out.println("Order " + currentCustomerOrder.orderId + " is already paid.");
            }
            return;
        }

        System.out.println("\n--- Confirming Order " + currentCustomerOrder.orderId + " ---");
        currentCustomerOrder.display(); // Display the order details before confirmation

        double discount = 0;
        System.out.print("Do you have a discount coupon? (yes/no): ");
        String hasCoupon = s.nextLine().trim().toLowerCase(); // Use nextLine()
        if (hasCoupon.equals("yes")) {
            System.out.print("Enter coupon code: ");
            String code = s.nextLine().trim(); // Use nextLine()
            if (code.equalsIgnoreCase(DISCOUNT_COUPON_CODE) && currentCustomerOrder.getPrice() >= DISCOUNT_MIN_ORDER_PRICE) {
                discount = DISCOUNT_AMOUNT;
                currentCustomerOrder.discountApplied = discount;
                System.out.println("Coupon applied! Rs." + String.format("%.2f", DISCOUNT_AMOUNT) + " off. New final price: Rs." + String.format("%.2f", currentCustomerOrder.getFinalPrice()));
            } else if (!code.equalsIgnoreCase(DISCOUNT_COUPON_CODE)) {
                System.out.println("Invalid coupon code.");
            } else {
                System.out.println("Order must be at least Rs." + String.format("%.2f", DISCOUNT_MIN_ORDER_PRICE) + " to use this coupon.");
            }
        }

        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000); // Generates a 4-digit OTP
        System.out.println("To confirm payment, your OTP is: " + otp);
        int enteredOtp;
        while (true) {
            System.out.print("Enter the OTP: ");
            try {
                enteredOtp = Integer.parseInt(s.nextLine());
                if (enteredOtp == otp) {
                    System.out.println("OTP confirmed.");
                    break;
                } else {
                    System.out.println("Incorrect OTP. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid OTP format. Please enter a number.");
            }
        }

        double expectedAmount = currentCustomerOrder.getFinalPrice();
        double enteredAmount;
        while (true) {
            System.out.print("Enter payment amount (Rs." + String.format("%.2f", expectedAmount) + "): ");
            try {
                enteredAmount = Double.parseDouble(s.nextLine());
                if (enteredAmount == expectedAmount) {
                    System.out.println("Payment amount correct.");
                    break;
                } else if (enteredAmount > expectedAmount) {
                    System.out.println("Payment successful. Change due: Rs." + String.format("%.2f", (enteredAmount - expectedAmount)));
                    break;
                } else {
                    System.out.println("Incorrect amount. Please enter at least the correct amount.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount format. Please enter a number.");
            }
        }

        currentCustomerOrder.paymentStatus = "paid";
        currentCustomerOrder.status = "confirmed and paid";

        currentCart.clear(); // Clear the temporary cart after order is confirmed
        System.out.println("Order " + currentCustomerOrder.orderId + " confirmed and paid. Thank you for your order!");
        currentCustomerOrder = null; // Clear the current order reference for the next customer
    }

    // --- Admin-facing methods ---
    public static void adminAccess(Scanner s) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View Menu Items");
            System.out.println("2. Add New Menu Item");
            System.out.println("3. Remove Menu Item by ID");
            System.out.println("4. View All Table Bookings");
            System.out.println("5. View All Orders");
            System.out.println("6. Check Order Payment Status by ID");
            System.out.println("7. Search Menu Items by Name");
            System.out.println("8. Generate Daily Sales Report");
            System.out.println("9. Exit Admin Access");
            System.out.print("Please select an option (1-9): ");
            
            int choice = -1;
            try {
                choice = Integer.parseInt(s.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    displayMenu();
                    break;
                case 2:
                    System.out.print("Enter item name: ");
                    String name = s.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("Item name cannot be empty.");
                        break;
                    }
                    System.out.print("Enter price: ");
                    double price;
                    try {
                        price = Double.parseDouble(s.nextLine());
                        if (price <= 0) {
                            System.out.println("Price must be positive.");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid price format. Please enter a number.");
                        break;
                    }

                    MenuItem newItem = new MenuItem(menuItemIdCounter, name.toLowerCase(), price);
                    menuList.add(newItem);
                    menuMap.put(menuItemIdCounter, newItem);
                    System.out.println("Item added successfully with ID: " + menuItemIdCounter + " (" + newItem.getName() + " - Rs." + String.format("%.2f", newItem.getPrice()) + ")");
                    menuItemIdCounter++;
                    break;
                case 3:
                    System.out.print("Enter item ID to remove: ");
                    int idToRemove;
                    try {
                        idToRemove = Integer.parseInt(s.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format. Please enter a number.");
                        break;
                    }

                    MenuItem removedItem = menuMap.remove(idToRemove);
                    if (removedItem != null) {
                        menuList.removeIf(item -> item.getId() == idToRemove); // Remove from list too
                        System.out.println("Item '" + removedItem.getName() + "' removed successfully.");
                    } else {
                        System.out.println("Item not found with ID: " + idToRemove);
                    }
                    break;
                case 4:
                    if (allBookings.isEmpty()) {
                        System.out.println("No table bookings found.");
                    } else {
                        System.out.println("\n--- All Table Bookings ---");
                        for (TableBooking b : allBookings) {
                            b.display();
                            System.out.println("---------------------");
                        }
                    }
                    break;
                case 5:
                    viewAllOrders();
                    break;
                case 6:
                    System.out.print("Enter Order ID to check payment status: ");
                    int checkOrderId;
                    try {
                        checkOrderId = Integer.parseInt(s.nextLine());
                        checkPaymentStatus(checkOrderId);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Order ID format. Please enter a number.");
                    }
                    break;
                case 7:
                    searchMenuItemsByName(s);
                    break;
                case 8:
                    generateDailySalesReport();
                    break;
                case 9:
                    running = false;
                    System.out.println("Exiting Admin Access.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public static void customerAccess(Scanner s) {
        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Menu");
            System.out.println("2. Add Items to Cart (comma-separated IDs)");
            System.out.println("3. View Cart");
            System.out.println("4. Place Order");
            System.out.println("5. Confirm Order and Pay");
            System.out.println("6. Reserve Table");
            System.out.println("7. View My Table Bookings (by Customer ID)");
            System.out.println("8. Check Available Tables");
            System.out.println("9. Search My Order by ID");
            System.out.println("10. Search Menu Items");
            System.out.println("11. Exit Customer Access");
            System.out.print("Enter your choice: ");
            
            int ch = -1;
            try {
                ch = Integer.parseInt(s.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (ch) {
                case 1:
                    displayMenu();
                    break;
                case 2:
                    System.out.print("Enter item IDs (e.g., 101,103,105): ");
                    addMultipleItemsToCart(s.nextLine());
                    break;
                case 3:
                    viewCart();
                    break;
                case 4:
                    placeOrder();
                    break;
                case 5:
                    confirmOrder(s);
                    break;
                case 6:
                    System.out.print("Enter your name: ");
                    String name = s.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("Name cannot be empty.");
                        break;
                    }
                    System.out.print("Enter phone: ");
                    String phone = s.nextLine().trim();
                    // Basic phone number validation (e.g., all digits)
                    if (!phone.matches("\\d{10}")) { // Simple check for 10 digits
                        System.out.println("Invalid phone number. Please enter 10 digits.");
                        break;
                    }

                    System.out.print("Enter number of people: ");
                    int people;
                    try {
                        people = Integer.parseInt(s.nextLine());
                        if (people <= 0) {
                            System.out.println("Number of people must be positive.");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input for number of people. Please enter a number.");
                        break;
                    }
                    reserveTable(people, name, phone, s);
                    break;
                case 7:
                    System.out.print("Enter your Customer ID (e.g., CUST1001): ");
                    String custId = s.nextLine().trim();
                    boolean foundBooking = false;
                    for (TableBooking b : allBookings) { // Iterate allBookings
                        if (b.customerId.equalsIgnoreCase(custId)) {
                            b.display();
                            foundBooking = true;
                        }
                    }
                    if (!foundBooking) {
                        System.out.println("No booking found for ID: " + custId);
                    }
                    break;
                case 8:
                    printAvailableTables();
                    break;
                case 9:
                    System.out.print("Enter your Order ID: ");
                    int orderIdSearch;
                    try {
                        orderIdSearch = Integer.parseInt(s.nextLine());
                        searchOrderById(orderIdSearch);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Order ID format. Please enter a number.");
                    }
                    break;
                case 10:
                    searchMenuItemsByName(s);
                    break;
                case 11:
                    // Check for unconfirmed order before exiting
                    if (currentCustomerOrder != null && !currentCart.isEmpty() && currentCustomerOrder.paymentStatus.equals("pending")) {
                        System.out.println("You have an unconfirmed order in your cart (Order ID: " + currentCustomerOrder.orderId + ").");
                        System.out.print("Do you want to abandon this order? (yes/no): ");
                        String abandon = s.nextLine().trim().toLowerCase();
                        if (abandon.equals("yes")) {
                            System.out.println("Order abandoned. Clearing cart.");
                            currentCart.clear();
                            // Optionally, remove the order from allOrders if not yet paid
                            // For simplicity, we just clear currentCustomerOrder and cart
                            // If it was already in allOrders, it remains there with "placed" status.
                            currentCustomerOrder = null;
                        } else {
                            System.out.println("Please complete your order before exiting.");
                            // Stay in the loop, prompt for action again
                            break; // break from switch, not while loop
                        }
                    }
                    return; // Exit customer access loop
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public static void viewAllOrders() {
        if (allOrders.isEmpty()) {
            System.out.println("No orders found in the system.");
            return;
        }

        System.out.println("\n--- All Orders ---");
        // Sort orders by ID for better viewing
        allOrders.sort(Comparator.comparingInt(o -> o.orderId));
        for (Order o : allOrders) {
            o.display();
            System.out.println("---------------------");
        }
    }

    public static void reserveTable(int people, String name, String phone, Scanner s) {
        String tableType = "";
        int tableNumber = -1;
        int seats = 0;
        double bookingFee = 0;
        boolean[] tableArr = null;

        // Determine appropriate table size and fee
        if (people <= 2) {
            tableType = "Table2";
            seats = 2;
            tableArr = tables2;
            bookingFee = 100.00;
        } else if (people <= 4) {
            tableType = "Table4";
            seats = 4;
            tableArr = tables4;
            bookingFee = 200.00;
        } else if (people <= 6) {
            tableType = "Table6";
            seats = 6;
            tableArr = tables6;
            bookingFee = 300.00;
        } else if (people <= 8) {
            tableType = "Table8";
            seats = 8;
            tableArr = tables8;
            bookingFee = 400.00;
        } else if (people <= 10) {
            tableType = "Table10";
            seats = 10;
            tableArr = tables10;
            bookingFee = 500.00;
        } else {
            System.out.println("We do not have tables for more than 10 people. Please consider multiple bookings.");
            return;
        }

        // Find an available table
        for (int i = 0; i < tableArr.length; i++) {
            if (!tableArr[i]) { // If table is NOT booked
                tableArr[i] = true; // Book it
                tableNumber = i + 1;
                break;
            }
        }

        if (tableNumber == -1) {
            System.out.println("Sorry, no available tables for " + seats + " people at the moment.");
            return;
        }

        String custId = "CUST" + customerIdCounter++;
        TableBooking booking = new TableBooking(tableType, tableNumber, name, phone, seats, custId, bookingFee);
        allBookings.add(booking); // Add to the master list of all bookings
        System.out.println("Table booked successfully!");
        booking.display(); // Show booking details

        System.out.print("Confirm payment for booking fee (Rs." + String.format("%.2f", bookingFee) + ")? (yes/no): ");
        String confirmPayment = s.nextLine().trim().toLowerCase(); // Use nextLine()
        if (confirmPayment.equals("yes")) {
            System.out.print("Enter payment amount: ");
            double paid;
            try {
                paid = Double.parseDouble(s.nextLine());
                if (paid >= bookingFee) { // Allow overpayment with change
                    booking.paymentStatus = "paid";
                    System.out.println("Booking fee confirmed and paid!");
                    if (paid > bookingFee) {
                        System.out.println("Change due: Rs." + String.format("%.2f", (paid - bookingFee)));
                    }
                } else {
                    System.out.println("Incorrect amount. Booking not paid. Please pay the full fee.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount format. Booking not paid. Please pay at the counter.");
            }
        } else {
            System.out.println("Booking fee not paid upfront. Please pay at the counter upon arrival. Note: Unpaid bookings may be cancelled if not claimed.");
        }
    }

    public static void printAvailableTables() {
        System.out.println("\n--- Available Tables ---");
        printTableStatus("Table for 2", tables2);
        printTableStatus("Table for 4", tables4);
        printTableStatus("Table for 6", tables6);
        printTableStatus("Table for 8", tables8);
        printTableStatus("Table for 10", tables10);
    }

    public static void printTableStatus(String name, boolean[] arr) {
        System.out.print(name + " available: ");
        boolean found = false;
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i]) { // If table is not booked (false)
                System.out.print((i + 1) + " ");
                found = true;
            }
        }
        if (!found) {
            System.out.print("None available");
        }
        System.out.println();
    }

    public static void checkPaymentStatus(int checkId) {
        // This method can check both order and table booking payment status
        boolean found = false;

        // Check orders
        for (Order o : allOrders) {
            if (o.orderId == checkId) {
                System.out.println("--- Order " + checkId + " Status ---");
                System.out.println("Payment Status: " + o.paymentStatus);
                System.out.println("Order Status: " + o.status);
                found = true;
                break;
            }
        }

        // Check table bookings (assuming booking IDs could overlap with order IDs if prefixes aren't unique,
        // but here they are unique with CUST prefix, so separate logic is fine)
        // For actual TableBooking ID search, you might need a different search method or a booking ID system.
        // As currently implemented, checkId is likely intended for Order IDs.
        // Let's adapt it to search by Order ID only for now, and rely on Customer ID for booking searches.
        if (!found) {
            System.out.println("Order ID " + checkId + " not found.");
        }
    }

    public static void searchMenuItemsByName(Scanner s) {
        System.out.print("Enter keyword to search menu: ");
        String keyword = s.nextLine().trim().toLowerCase();
        boolean found = false;
        System.out.println("\n--- Search Results for '" + keyword + "' ---");
        for (MenuItem item : menuList) {
            if (item.getName().toLowerCase().contains(keyword)) {
                System.out.println(item);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No menu items found containing: '" + keyword + "'");
        }
    }

    public static void searchOrderById(int orderIdSearch) {
        boolean found = false;
        for (Order o : allOrders) { // Iterates through allOrders
            if (o.orderId == orderIdSearch) { // Checks if the orderId matches
                System.out.println("\n--- Order " + orderIdSearch + " Details ---");
                o.display();
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Order ID " + orderIdSearch + " not found in the system.");
        }
    }

    public static void generateDailySalesReport() {
        double totalOrderSales = 0;
        int paidOrdersCount = 0;
        for (Order order : allOrders) {
            if (order.paymentStatus.equals("paid")) {
                totalOrderSales += order.getFinalPrice();
                paidOrdersCount++;
            }
        }

        double totalBookingFees = 0;
        int paidBookingsCount = 0;
        for (TableBooking booking : allBookings) {
            if (booking.paymentStatus.equals("paid")) {
                totalBookingFees += booking.bookingFee;
                paidBookingsCount++;
            }
        }

        System.out.println("\n--- Daily Sales Report (" + LocalDate.now() + ") ---");
        System.out.println("Orders:");
        System.out.println("  Total Paid Orders: " + paidOrdersCount);
        System.out.println("  Total Order Revenue: Rs." + String.format("%.2f", totalOrderSales));
        System.out.println("\nTable Bookings:");
        System.out.println("  Total Paid Bookings: " + paidBookingsCount);
        System.out.println("  Total Booking Fee Revenue: Rs." + String.format("%.2f", totalBookingFees));
        System.out.println("\nOverall Total Revenue: Rs." + String.format("%.2f", (totalOrderSales + totalBookingFees)));
        System.out.println("------------------------------------------");
    }
}
