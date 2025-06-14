import java.util.*;

public class RestaurantsManagementSystem 
{

    static class MenuItem 
    {
        private int id;
        private String name;
        private double price;

        public MenuItem(int id, String name, double price) 
        {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() 
        {
            return id;
        }

        public String getName() 
        {
            return name;
        }

        public double getPrice() 
        {
            return price;
        }

        @Override
        public String toString() 
        {
            return "ID: " + id + " | " + name + " - Rs." + price;
        }
    }

    static class Order 
    {
        int orderId;
        List<MenuItem> items = new ArrayList<>();
        String status = "placed";
        String paymentStatus = "pending";
        double discountApplied = 0; 

        public Order(int orderId) 
        {
            this.orderId = orderId;
        }

        void addItem(MenuItem item) 
        {
            items.add(item);
        }

        double getPrice() 
        { 
            double total = 0;
            for (MenuItem item : items) 
            {
                total += item.getPrice();
            }
            return total;
        }

        double getFinalPrice() 
        { 
            return getPrice() - discountApplied;
        }

        public void display() 
        {
            System.out.println("Order ID: " + orderId);
            for (MenuItem item : items) 
            {
                System.out.println("Item: " + item.getName() + " | Price: Rs." + item.getPrice());
            }
            System.out.println("Total Price: Rs." + String.format("%.2f", getPrice()));
            if (discountApplied > 0) 
            {
                System.out.println("Discount Applied: Rs." + String.format("%.2f", discountApplied));
                System.out.println("Final Price: Rs." + String.format("%.2f", getFinalPrice()));
            }
            System.out.println("Status: " + status);
            System.out.println("Payment Status: " + paymentStatus);
        }
    }

    static class TableBooking 
    {
        String tableType;
        int tableNumber;
        String customerName;
        String phone;
        int seats;
        String customerId;
        String paymentStatus = "pending";
        double bookingFee; // Changed to double

        public TableBooking(String tableType, int tableNumber, String customerName, String phone, int seats, String customerId, double bookingFee) 
        {
            this.tableType = tableType;
            this.tableNumber = tableNumber;
            this.customerName = customerName;
            this.phone = phone;
            this.seats = seats;
            this.customerId = customerId;
            this.bookingFee = bookingFee;
        }

        public void display() {
            System.out.println("Customer ID: " + customerId +
                    " | Table: " + tableType + " #" + tableNumber +
                    " | Seats: " + seats +
                    " | Name: " + customerName +
                    " | Phone: " + phone +
                    " | Booking Fee: Rs." + String.format("%.2f", bookingFee) +
                    " | Payment Status: " + paymentStatus);
        }
    }

    static List<MenuItem> menuList = new ArrayList<>();
    static List<MenuItem> cart = new ArrayList<>(); // This cart is only for current unplaced order
    static List<TableBooking> bookings = new ArrayList<>();
    static List<TableBooking> paidBookings = new ArrayList<>(); // To track paid table bookings separately
    static List<Order> allOrders = new ArrayList<>(); // To store all placed orders (paid/unpaid)
    static int orderCounter = 1; // Renamed from orderId to avoid confusion with Order.orderId
    static int customerIdCounter = 1001; // Renamed from customerId

    // Table availability arrays
    static boolean[] tables10 = new boolean[10];
    static boolean[] tables8 = new boolean[10];
    static boolean[] tables6 = new boolean[10];
    static boolean[] tables4 = new boolean[10];
    static boolean[] tables2 = new boolean[10];
    static Map<Integer, MenuItem> menuMap = new HashMap<>();

    // The current order being built by the customer
    static Order currentOrder = null;

    public static void main(String[] args)
    {
        Scanner s = new Scanner(System.in);
        initializeMenu();
        System.out.println("Welcome to the Shield Restaurant");
        boolean valided = true;
        System.out.println("1. Admin access");
        System.out.println("2. Customer access");
        System.out.print("Please select your access level (1 or 2): ");
        int accessLevel = s.nextInt();
        while (valided)
        {
            if (accessLevel == 2)
            {
                customerAccess(s);
            }
            accessLevel = 1;
            if (accessLevel == 1)
            {
                System.out.print("Enter 4-digit admin PIN: ");
                int pin = s.nextInt();
                if (pin == 2004)
                {
                    adminAccess(s);
                }
                else
                {
                    System.out.println("Invalid PIN. Access denied.");
                }
            }
            System.out.print("Next access level (1 for Exit, 2 for Customer): ");
            accessLevel = s.nextInt();
            if (accessLevel == 2)
                valided = true;
            else
            {
                valided = false;
                System.out.println("Exiting the system. Thank you!");
            }
        }
    }

    public static void initializeMenu() {
        int id = 101;
        String[] names = {"chicken biryani", "mutton biryani", "veg biryani", "chicken curry", "mutton curry", "veg curry", "chicken tikka", "mutton tikka", "veg tikka", "chicken kebab", "mutton kebab", "veg kebab", "soft drink", "water", "salad", "dessert"};
        double[] prices = {150.00, 200.00, 100.00, 180.00, 220.00, 120.00, 160.00, 210.00, 130.00, 170.00, 230.00, 140.00, 50.00, 20.00, 30.00, 80.00};
        for (int i = 0; i < names.length; i++) {
            MenuItem item = new MenuItem(id, names[i], prices[i]);
            menuList.add(item);
            menuMap.put(id++, item);
        }
    }

    public static void displayMenu() {
        System.out.println("\n====== VEG MENU ======");
        for (MenuItem item : menuList) {
            String n = item.getName().toLowerCase();
            if (n.contains("veg") || n.equals("salad") || n.equals("dessert")) {
                System.out.println(item);
            }
        }
        System.out.println("\n==== NON-VEG MENU ====");
        for (MenuItem item : menuList) {
            String n = item.getName().toLowerCase();
            if ((n.contains("chicken") || n.contains("mutton")) && !n.contains("veg")) {
                System.out.println(item);
            }
        }
        System.out.println("\n== BEVERAGES & EXTRAS ==");
        for (MenuItem item : menuList) {
            String n = item.getName().toLowerCase();
            if (n.equals("soft drink") || n.equals("water")) {
                System.out.println(item);
            }
        }
        System.out.println("\n** Use coupon SHEILD2004 for Rs.100 off on orders above Rs.500! **");
    }

    public static void addMultipleItemsToCart(String input) {
        if (currentOrder == null) {
            currentOrder = new Order(orderCounter++);
            System.out.println("New Order ID generated: " + currentOrder.orderId);
        }
        String[] ids = input.split(",");
        for (String idStr : ids) {
            try {
                int id = Integer.parseInt(idStr.trim());
                MenuItem item = menuMap.get(id);
                if (item != null) {
                    cart.add(item);
                    currentOrder.addItem(item);
                    System.out.println("Added: " + item.getName());
                } else {
                    System.out.println("Item ID " + id + " not found.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID: " + idStr);
            }
        }
        System.out.println("Items added to current order (ID: " + currentOrder.orderId + ").");
    }

    public static void viewCart() {
        if (currentOrder == null || cart.isEmpty()) {
            System.out.println("Cart is empty or no current order initiated.");
            return;
        }
        System.out.println("Current Order ID: " + currentOrder.orderId);
        double total = 0;
        for (MenuItem item : cart) {
            total += item.getPrice();
            System.out.println("Item: " + item.getName() + " | Price: Rs." + String.format("%.2f", item.getPrice()));
        }
        System.out.println("Subtotal: Rs." + String.format("%.2f", total));
    }

    public static void placeOrder() {
        if (currentOrder == null || cart.isEmpty()) {
            System.out.println("No items in cart to place an order.");
            return;
        }
        currentOrder.status = "placed";
        allOrders.add(currentOrder); // Add to allOrders list
        System.out.println("Order " + currentOrder.orderId + " has been placed.");
        // We keep currentOrder active until confirmed/paid or explicitly cleared
    }

    public static void confirmOrder(Scanner s) {
        if (currentOrder == null || cart.isEmpty()) {
            System.out.println("No active order to confirm or cart is empty.");
            return;
        }

        System.out.println("\n--- Confirming Order " + currentOrder.orderId + " ---");
        currentOrder.display();

        double discount = 0;
        System.out.print("Do you have a discount coupon? (yes/no): ");
        String hasCoupon = s.next().trim().toLowerCase();
        if (hasCoupon.equals("yes")) {
            System.out.print("Enter coupon code: ");
            String code = s.next().trim();
            if (code.equalsIgnoreCase("SHEILD2004") && currentOrder.getPrice() >= 500) {
                discount = 100.00;
                currentOrder.discountApplied = discount;
                System.out.println("Coupon applied! Rs.100 off.");
            } else if (!code.equalsIgnoreCase("SHEILD2004")) {
                System.out.println("Invalid coupon code.");
            } else {
                System.out.println("Order must be at least Rs.500 to use this coupon.");
            }
        }

        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000);
        System.out.println("To confirm payment, your OTP is: " + otp);
        while (true) {
            System.out.print("Enter the OTP: ");
            int enteredOtp = s.nextInt();
            if (enteredOtp == otp) break;
            System.out.println("Incorrect OTP. Please try again.");
        }

        double expectedAmount = currentOrder.getFinalPrice();
        while (true) {
            System.out.print("Enter payment amount (Rs." + String.format("%.2f", expectedAmount) + "): ");
            double enteredAmount = s.nextDouble();
            if (enteredAmount == expectedAmount) break;
            System.out.println("Incorrect amount. Please enter the correct amount.");
        }

        currentOrder.paymentStatus = "paid";
        currentOrder.status = "confirmed and paid";
        // If the order was in unpaidOrders, remove it and add to paidOrders
        allOrders.removeIf(o -> o.orderId == currentOrder.orderId); // Remove the existing entry to update
        allOrders.add(currentOrder); // Add the updated order
        
        cart.clear(); // Clear the cart after successful payment
        System.out.println("Order " + currentOrder.orderId + " confirmed and paid. Thank you!");
        currentOrder = null; // Reset current order for the next customer interaction
    }

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
            int choice = s.nextInt();
            s.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    displayMenu();
                    break;
                case 2:
                    System.out.print("Enter item name: ");
                    String name = s.nextLine();
                    System.out.print("Enter price: ");
                    double price = s.nextDouble();
                    s.nextLine(); // Consume newline
                    int nextId = menuList.isEmpty() ? 101 : menuList.get(menuList.size() - 1).getId() + 1;
                    MenuItem newItem = new MenuItem(nextId, name.toLowerCase(), price);
                    menuList.add(newItem);
                    menuMap.put(nextId, newItem);
                    System.out.println("Item added with ID: " + nextId);
                    break;
                case 3:
                    System.out.print("Enter item ID to remove: ");
                    int idToRemove = s.nextInt();
                    s.nextLine(); // Consume newline
                    MenuItem removedItem = menuMap.remove(idToRemove);
                    if (removedItem != null) {
                        menuList.removeIf(item -> item.getId() == idToRemove);
                        System.out.println("Item '" + removedItem.getName() + "' removed.");
                    } else {
                        System.out.println("Item not found with ID: " + idToRemove);
                    }
                    break;
                case 4:
                    if (bookings.isEmpty()) {
                        System.out.println("No table bookings found.");
                    } else {
                        System.out.println("\n--- All Table Bookings ---");
                        for (TableBooking b : bookings) {
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
                    int checkOrderId = s.nextInt();
                    checkPaymentStatus(checkOrderId);
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
            System.out.println("4. Place Order"); // Added explicit place order
            System.out.println("5. Confirm Order and Pay");
            System.out.println("6. Reserve Table");
            System.out.println("7. View My Table Bookings (by Customer ID)");
            System.out.println("8. Check Available Tables");
            System.out.println("9. Search My Order by ID");
            System.out.println("10. Search Menu Items");
            System.out.println("11. Exit Customer Access");
            System.out.print("Enter your choice: ");
            int ch = s.nextInt();
            s.nextLine(); // Consume newline

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
                    placeOrder(); // New option to place order without immediate payment
                    break;
                case 5:
                    confirmOrder(s);
                    break;
                case 6:
                    System.out.print("Enter your name: ");
                    String name = s.nextLine();
                    System.out.print("Enter phone: ");
                    String phone = s.nextLine();
                    System.out.print("Enter number of people: ");
                    int people = s.nextInt();
                    reserveTable(people, name, phone, s);
                    break;
                case 7:
                    System.out.print("Enter your Customer ID (e.g., CUST1001): ");
                    String custId = s.nextLine().trim();
                    boolean foundBooking = false;
                    for (TableBooking b : bookings) {
                        if (b.customerId.equalsIgnoreCase(custId)) {
                            b.display();
                            foundBooking = true;
                        }
                    }
                    if (!foundBooking) System.out.println("No booking found for ID: " + custId);
                    break;
                case 8:
                    printAvailableTables();
                    break;
                case 9:
                    System.out.print("Enter your Order ID: ");
                    int orderIdSearch = s.nextInt();
                    searchOrderById(orderIdSearch);
                    break;
                case 10:
                    searchMenuItemsByName(s);
                    break;
                case 11:
                    if (currentOrder != null && !cart.isEmpty()) {
                        System.out.println("You have an unconfirmed order in your cart (Order ID: " + currentOrder.orderId + ").");
                        System.out.print("Do you want to abandon this order? (yes/no): ");
                        String abandon = s.nextLine().trim().toLowerCase();
                        if (abandon.equals("yes")) {
                            System.out.println("Order abandoned. Clearing cart.");
                            // Optionally, if the order was "placed" but not paid, you might want to mark it as cancelled or unpaid
                            // For simplicity, we just clear the cart and current order reference here
                            cart.clear();
                            currentOrder = null;
                        } else {
                            System.out.println("You can confirm and pay for your order.");
                            confirmOrder(s);
                        }
                    }
                    return; // Exit customer access
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
            System.out.println("We don't have tables for more than 10 people.");
            return;
        }

        for (int i = 0; i < tableArr.length; i++) {
            if (!tableArr[i]) {
                tableArr[i] = true; // Mark as booked
                tableNumber = i + 1;
                break;
            }
        }

        if (tableNumber == -1) {
            System.out.println("Sorry, no available tables for " + seats + " people.");
            return;
        }

        String custId = "CUST" + customerIdCounter++;
        TableBooking booking = new TableBooking(tableType, tableNumber, name, phone, seats, custId, bookingFee);
        bookings.add(booking);
        System.out.println("Table booked! Your Customer ID: " + custId + " | Booking Fee: Rs." + String.format("%.2f", bookingFee));

        System.out.print("Confirm payment for booking fee (Rs." + String.format("%.2f", bookingFee) + ")? (yes/no): ");
        String confirmPayment = s.next().trim().toLowerCase();
        if (confirmPayment.equals("yes")) {
            System.out.print("Enter payment amount: ");
            double paid = s.nextDouble();
            if (paid == bookingFee) {
                booking.paymentStatus = "paid";
                paidBookings.add(booking);
                System.out.println("Booking confirmed and paid!");
            } else {
                System.out.println("Incorrect amount. Booking not paid. Please pay at the counter.");
            }
        } else {
            System.out.println("Booking not paid. Please pay at the counter upon arrival.");
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
        System.out.print(name + ": ");
        boolean found = false;
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i]) {
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
        boolean found = false;
        for (Order o : allOrders) {
            if (o.orderId == checkId) {
                System.out.println("Order ID: " + checkId + " | Payment Status: " + o.paymentStatus + " | Order Status: " + o.status);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Order ID " + checkId + " not found.");
        }
    }

    public static void searchMenuItemsByName(Scanner s) {
        System.out.print("Enter keyword to search menu: ");
        String keyword = s.nextLine().toLowerCase();
        boolean found = false;
        System.out.println("\n--- Search Results ---");
        for (MenuItem item : menuList) {
            if (item.getName().toLowerCase().contains(keyword)) {
                System.out.println(item);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No menu items found with keyword: " + keyword);
        }
    }

    public static void searchOrderById(int orderIdSearch) {
        boolean found = false;
        for (Order o : allOrders) {
            if (o.orderId == orderIdSearch) {
                o.display();
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Order ID " + orderIdSearch + " not found in your order history.");
        }
    }

    public static void generateDailySalesReport() {
        double totalSales = 0;
        int paidOrdersCount = 0;
        for (Order order : allOrders) {
            if (order.paymentStatus.equals("paid")) {
                totalSales += order.getFinalPrice();
                paidOrdersCount++;
            }
        }
        System.out.println("\n--- Daily Sales Report ---");
        System.out.println("Total Paid Orders: " + paidOrdersCount);
        System.out.println("Total Sales: Rs." + String.format("%.2f", totalSales));
        System.out.println("--------------------------");
    }
}
