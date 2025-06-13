import java.util.*;

public class RestaurantsManagementSystem 
{
    static class MenuItem 
    {
        private int id;
        private String name;
        private String price;
        public MenuItem(int id, String name, String price)
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
        public String getPrice() 
        { 
            return price; 
        }
    }

    static class Order 
    {
        int orderId;
        List<MenuItem> items = new ArrayList<>();
        String status = "placed";
        String paymentStatus = "pending";
        public Order(int orderId) 
        {
            this.orderId = orderId;
        }
        void addItem(MenuItem item) 
        {
            items.add(item);
        }
        int getPrice() 
        {
            int total = 0;
            for (MenuItem item : items) 
            {
                String priceStr = item.getPrice().replaceAll("[^0-9]", "");
                total += Integer.parseInt(priceStr);
            }
            return total;
        }
        public void display() 
        {
            System.out.println("Order ID: " + orderId);
            for (MenuItem item : items) 
            {
                System.out.println("Item: " + item.getName() + " | Price: Rs." + item.getPrice());
            }
            System.out.println("Total Price: Rs." + getPrice());
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
        int bookingFee;

        public TableBooking(String tableType, int tableNumber, String customerName, String phone, int seats, String customerId, int bookingFee) 
        {
            this.tableType = tableType;
            this.tableNumber = tableNumber;
            this.customerName = customerName;
            this.phone = phone;
            this.seats = seats;
            this.customerId = customerId;
            this.bookingFee = bookingFee;
        }
        public void display() 
        {
            System.out.println("Customer ID: " + customerId +
                    " | Table: " + tableType + " #" + tableNumber +
                    " | Seats: " + seats +
                    " | Name: " + customerName +
                    " | Phone: " + phone +
                    " | Booking Fee: Rs." + bookingFee +
                    " | Payment Status: " + paymentStatus);
        }
    }

    static List<MenuItem> menuList = new ArrayList<>();
    static List<MenuItem> cart = new ArrayList<>();
    static List<TableBooking> bookings = new ArrayList<>();
    static List<TableBooking> paidBookings = new ArrayList<>();
    static Queue<Order> orderQueue = new LinkedList<>();
    static List<Order> paidOrders = new ArrayList<>();
    static List<Order> unpaidOrders = new ArrayList<>();
    static int orderId = 1;
    static int customerId = 1001;
    static boolean[] tables10 = new boolean[10];
    static boolean[] tables8 = new boolean[10];
    static boolean[] tables6 = new boolean[10];
    static boolean[] tables4 = new boolean[10];
    static boolean[] tables2 = new boolean[10];
    static Map<Integer, MenuItem> menuMap = new HashMap<>();
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

    public static void initializeMenu() 
    {
        int id = 101;
        String[] names = {"chicken biryani", "mutton biryani", "veg biryani", "chicken curry", "mutton curry", "veg curry", "chicken tikka", "mutton tikka", "veg tikka", "chicken kebab", "mutton kebab", "veg kebab", "soft drink", "water", "salad", "dessert"};
        int[] prices = {150, 200, 100, 180, 220, 120, 160, 210, 130, 170, 230, 140, 50, 20, 30, 80};
        for (int i = 0; i < names.length; i++) 
        {
            MenuItem item = new MenuItem(id, names[i], "RS. " + prices[i]);
            menuList.add(item);
            menuMap.put(id++, item);
        }
    }

    public static void adminAccess(Scanner s) 
    {
        boolean isValid = true;
        while (isValid) 
        {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View menu items");
            System.out.println("2. Add new menu item");
            System.out.println("3. Remove menu item by ID");
            System.out.println("4. View Table Bookings");
            System.out.println("5. View Order");
            System.out.println("6. Place Order");
            System.out.println("7. Confirm Order and Pay");
            System.out.println("8. Check Payment Status by Order ID");
            System.out.println("9. View All Orders");
            System.out.println("10. Exit");
            System.out.print("Please select an option (1-9): ");
            int choice = s.nextInt();
            s.nextLine();

            switch (choice) 
            {
                case 1: displayMenu(); break;
                case 2:
                    System.out.print("Enter item name: ");
                    String name = s.nextLine();
                    System.out.print("Enter price: ");
                    int price = s.nextInt(); s.nextLine();
                    int nextId = menuList.get(menuList.size() - 1).getId() + 1;
                    MenuItem newItem = new MenuItem(nextId, name.toLowerCase(), "RS. " + price);
                    menuList.add(newItem);
                    menuMap.put(nextId, newItem);
                    System.out.println("Item added with ID: " + nextId);
                    break;
                case 3:
                    System.out.print("Enter item ID to remove: ");
                    int id = s.nextInt();
                    if (menuMap.containsKey(id)) 
                    {
                        menuList.removeIf(item -> item.getId() == id);
                        menuMap.remove(id);
                        System.out.println("Item removed.");
                    } 
                    else 
                    {
                        System.out.println("Item not found.");
                    }
                    break;
                case 4:
                    if (bookings.isEmpty()) System.out.println("No bookings found.");
                    else for (TableBooking b : bookings) b.display();
                    break;
                case 5: viewCart(); break;
                case 6: 
                    System.out.println("Placing order is handled in customer access."); 
                    break;
                case 7: confirmOrder(s); break;
                case 8:
                    System.out.print("Enter Order ID to check payment status: ");
                    int checkId = s.nextInt();
                    checkPaymentStatus(checkId);
                    break;
                case 9:
                    viewAllOrders();
                    break;
                case 10:
                    if(!cart.isEmpty())
                    {
                        System.out.println("The customer didn't confirm the order.");
                        System.out.println("Clearing the cart.");
                        viewCart();
                        confirmOrder(s);
                    }
                    isValid = false;
                    break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    public static void customerAccess(Scanner s) 
    {
        while (true) 
        {
            System.out.println("\nCustomer Menu:");
            System.out.println("1. View Menu");
            System.out.println("2. Add Items to Cart (comma-separated IDs)");
            System.out.println("3. View Cart");
            System.out.println("4. Place Order");
            System.out.println("5. Confirm Order and Pay");
            System.out.println("6. Reserve Table");
            System.out.println("7. View Booked Tables");
            System.out.println("8. Check Available Tables");
            System.out.println("9. Search My Order by ID");
            System.out.println("10. Exit");
            System.out.print("Enter your choice: ");
            int ch = s.nextInt();
            switch (ch) 
            {
                case 1: displayMenu(); break;
                case 2:
                    System.out.print("Enter item IDs: ");
                    s.nextLine();
                    addMultipleItemsToCart(s.nextLine());
                    break;
                case 3: viewCart(); break;
                case 4:
                case 5: confirmOrder(s); break;
                case 6:
                    s.nextLine();
                    System.out.print("Enter your name: ");
                    String name = s.nextLine();
                    System.out.print("Enter phone: ");
                    String phone = s.nextLine();
                    System.out.print("Enter number of people: ");
                    int people = s.nextInt();
                    reserveTable(people, name, phone, s);
                    break;
                case 7:
                    s.nextLine();
                    System.out.print("Enter Customer ID to view booking (e.g., CUST1001): ");
                    String custId = s.nextLine().trim();
                    boolean found = false;
                    for (TableBooking b : bookings) {
                        if (b.customerId.equalsIgnoreCase(custId)) {
                            b.display();
                            found = true;
                            break;
                        }
                    }
                    if (!found) System.out.println("No booking found for ID: " + custId);
                    break;
                case 8: printAvailableTables(); break;
                case 9:
                    System.out.print("Enter your Order ID: ");
                    int orderIdSearch = s.nextInt();
                    searchOrderById(orderIdSearch);
                    break;
                case 10: return;
                default: System.out.println("Invalid option.");
            }
        }
    }
    public static void viewAllOrders() {
        System.out.println("\n--- Paid Orders ---");
        if (paidOrders.isEmpty()) {
            System.out.println("No paid orders found.");
        } else {
            for (Order o : paidOrders) {
                o.display();
                System.out.println("---------------------");
            }
        }

        System.out.println("\n--- Unpaid Orders ---");
        if (unpaidOrders.isEmpty()) {
            System.out.println("No unpaid orders found.");
        } else {
            for (Order o : unpaidOrders) {
                o.display();
                System.out.println("---------------------");
            }
        }
    }

    public static void displayMenu() 
    {
        System.out.println("\nMenu:");
        Collections.sort(menuList, Comparator.comparingInt(MenuItem::getId));
        for (MenuItem item : menuList) 
        {
            System.out.println("ID: " + item.getId() + " | " + item.getName() + " - Rs." + item.getPrice());
        }
    }

    public static void addMultipleItemsToCart(String input) 
    {
        if (currentOrder == null) 
        {
            currentOrder = new Order(orderId);
            System.out.println("Order ID generated: " + currentOrder.orderId);
        }
        String[] ids = input.split(",");
        for (String idStr : ids) 
        {
            try 
            {
                int id = Integer.parseInt(idStr.trim());
                MenuItem item = menuMap.get(id);
                if (item != null) 
                {
                    cart.add(item);
                    currentOrder.addItem(item);
                    System.out.println("Added: " + item.getName());
                } 
                else 
                {
                    System.out.println("Item ID " + id + " not found.");
                }
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid ID: " + idStr);
            }
        }
        System.out.println("Current Order ID: " + currentOrder.orderId);
    }

    public static void viewCart() 
    {
        if (cart.isEmpty()) System.out.println("Cart is empty.");
        else 
        {
            System.out.println("Current Order ID: " + currentOrder.orderId);
            int total = 0;
            for (MenuItem item : cart) 
            {
                int price = Integer.parseInt(item.getPrice().replaceAll("[^0-9]", ""));
                total += price;
                System.out.println("Item: " + item.getName() + " | Price: Rs." + item.getPrice());
            }
            System.out.println("Total: Rs." + total);
        }
    }

    // Improved: Prevent duplicate payments, ask for correct OTP/amount
    public static void confirmOrder(Scanner s) 
    {
        if (cart.isEmpty()) 
        {
            System.out.println("Cart is empty.");
            return;
        }
        currentOrder.display();

        // OTP logic
        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000);
        System.out.println("To confirm payment, your OTP is: " + otp);

        while (true) {
            System.out.print("Enter the OTP: ");
            int enteredOtp = s.nextInt();
            if (enteredOtp == otp) break;
            System.out.println("Incorrect OTP. Please try again.");
        }

        // Amount logic
        int expectedAmount = currentOrder.getPrice();
        while (true) {
            System.out.print("Enter payment amount (Rs." + expectedAmount + "): ");
            int enteredAmount = s.nextInt();
            if (enteredAmount == expectedAmount) break;
            System.out.println("Incorrect amount. Please enter the exact amount.");
        }

        boolean alreadyPaid = false;
        for (Order o : paidOrders) 
        {
            if (o.orderId == currentOrder.orderId) 
            {
                alreadyPaid = true;
                break;
            }
        }
        if (alreadyPaid) 
        {
            System.out.println("This order has already been paid. Duplicate payment not allowed.");
            cart.clear();
            currentOrder = null;
            return;
        }

        currentOrder.paymentStatus = "paid";
        paidOrders.add(currentOrder);
        orderQueue.add(currentOrder);
        System.out.println("Payment successful. Your Order ID is: " + currentOrder.orderId);
        currentOrder.display();
        orderId++;
        cart.clear();
        currentOrder = null;
    }
    public static void reserveTable(int people, String name, String phone, Scanner s) 
    {
        String tableType = "";
        int tableNumber = -1;
        boolean[] tableArr = null;
        int bookingFee = 0;

        if (people > 10) 
        {
            System.out.println("We do not have tables for more than 10 people.");
            return;
        }
        if (people > 8) 
        {
            tableType = "10-seater";
            tableArr = tables10;
            bookingFee = 500;
        }
        else if (people > 6) 
        {
            tableType = "8-seater";
            tableArr = tables8;
            bookingFee = 400;
        }
        else if (people > 4) 
        {
            tableType = "6-seater";
            tableArr = tables6;
            bookingFee = 350;
        }
        else if (people > 2) 
        {
            tableType = "4-seater";
            tableArr = tables4;
            bookingFee = 300;
        }
        else if (people > 0) 
        {
            tableType = "2-seater";
            tableArr = tables2;
            bookingFee = 200;
        }
        else 
        {
            System.out.println("Invalid number of people.");
            return;
        }

        // Find first available table
        for (int i = 0; i < tableArr.length; i++) 
        {
            if (!tableArr[i]) 
            {
                tableNumber = i + 1;
                break;
            }
        }
        if (tableNumber == -1) 
        {
            System.out.println("No available tables of this size.");
            return;
        }

        String customerIdStr = "CUST" + (customerId++);
        TableBooking booking = new TableBooking(tableType, tableNumber, name, phone, people, customerIdStr, bookingFee);

        // Process payment and booking
        if (processTableBookingPayment(booking, s)) 
        {
            tableArr[tableNumber - 1] = true; // Mark as booked
            bookings.add(booking);
        }
    }

    // Payment logic for table booking
    public static boolean processTableBookingPayment(TableBooking booking, Scanner s) {
        // Check for duplicate: same customerName, phone, tableType, and tableNumber
        for (TableBooking b : paidBookings) {
            if (
                b.customerName.equalsIgnoreCase(booking.customerName)
                && b.phone.equals(booking.phone)
                && b.tableType.equals(booking.tableType)
                && b.tableNumber == booking.tableNumber
            ) {
                System.out.println("This table booking has already been paid for. Duplicate payment not allowed.");
                return false;
            }
        }

        // OTP logic
        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000);
        System.out.println("To confirm table booking, your OTP is: " + otp);

        while (true) {
            System.out.print("Enter the OTP: ");
            int enteredOtp = s.nextInt();
            if (enteredOtp == otp) break;
            System.out.println("Incorrect OTP. Please try again.");
        }

        // Amount logic
        while (true) {
            System.out.print("Enter booking fee amount (Rs." + booking.bookingFee + "): ");
            int enteredAmount = s.nextInt();
            if (enteredAmount == booking.bookingFee) break;
            System.out.println("Incorrect amount. Please enter the exact booking fee.");
        }

        booking.paymentStatus = "paid";
        paidBookings.add(booking);
        System.out.println("Payment successful. Table booked!");
        booking.display();
        return true;
    }

    public static void printAvailableTables() 
    {
        System.out.println("Available 2-seater tables: " + countAvailable(tables2));
        System.out.println("Available 4-seater tables: " + countAvailable(tables4));
        System.out.println("Available 6-seater tables: " + countAvailable(tables6));
        System.out.println("Available 8-seater tables: " + countAvailable(tables8));
        System.out.println("Available 10-seater tables: " + countAvailable(tables10));
    }

    public static int countAvailable(boolean[] arr) 
    {
        int count = 0;
        for (boolean b : arr) if (!b) count++;
        return count;
    }

    public static void searchOrderById(int id) 
    {
        boolean found = false;
        for (Order o : paidOrders) 
        {
            if (o.orderId == id) 
            {
                o.display();
                found = true;
                break;
            }
        }
        if (!found) System.out.println("Order not found or not paid.");
    }

    public static void checkPaymentStatus(int id) 
    {
        boolean found = false;
        for (Order o : paidOrders) 
        {
            if (o.orderId == id) 
            {
                System.out.println("Order ID: " + id + " | Payment Status: " + o.paymentStatus);
                found = true;
                break;
            }
        }
        if (!found) System.out.println("Order not found or not paid.");
    }
}
