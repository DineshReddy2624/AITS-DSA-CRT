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
        public int getId() { return id; }
        public String getName() { return name; }
        public String getPrice() { return price; }
    }

    static class Order
    {
        int orderId;
        List<MenuItem> items = new ArrayList<>();
        String status = "placed";
        String paymentStatus = "pending";
        int discountApplied = 0;
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
        int getFinalPrice()
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
            System.out.println("Total Price: Rs." + getPrice());
            if (discountApplied > 0)
            {
                System.out.println("Discount Applied: Rs." + discountApplied);
                System.out.println("Final Price: Rs." + getFinalPrice());
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

    public static void displayMenu()
    {
        System.out.println("\n====== VEG MENU ======");
        for (MenuItem item : menuList)
        {
            String n = item.getName().toLowerCase();
            if (n.contains("veg") || n.equals("salad") || n.equals("dessert"))
            {
                System.out.println("ID: " + item.getId() + " | " + item.getName() + " - Rs." + item.getPrice());
            }
        }
        System.out.println("\n==== NON-VEG MENU ====");
        for (MenuItem item : menuList)
        {
            String n = item.getName().toLowerCase();
            if ((n.contains("chicken") || n.contains("mutton")) && !n.contains("veg"))
            {
                System.out.println("ID: " + item.getId() + " | " + item.getName() + " - Rs." + item.getPrice());
            }
        }
        System.out.println("\n== BEVERAGES & EXTRAS ==");
        for (MenuItem item : menuList)
        {
            String n = item.getName().toLowerCase();
            if (n.equals("soft drink") || n.equals("water"))
            {
                System.out.println("ID: " + item.getId() + " | " + item.getName() + " - Rs." + item.getPrice());
            }
        }
        System.out.println("\n** Use coupon SHEILD2004 for Rs.100 off on orders above Rs.500! **");
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
        if (cart.isEmpty())
        {
            System.out.println("Cart is empty.");
        }
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

    public static void confirmOrder(Scanner s)
    {
        if (cart.isEmpty())
        {
            System.out.println("Cart is empty.");
            return;
        }
        int discount = 0;
        currentOrder.display();
        System.out.print("Do you have a discount coupon? (yes/no): ");
        String hasCoupon = s.next().trim().toLowerCase();
        if (hasCoupon.equals("yes"))
        {
            System.out.print("Enter coupon code: ");
            String code = s.next().trim();
            if (code.equalsIgnoreCase("SHEILD2004") && currentOrder.getPrice() >= 500)
            {
                discount = 100;
                currentOrder.discountApplied = discount;
                System.out.println("Coupon applied! Rs.100 off.");
            }
            else if (!code.equalsIgnoreCase("SHEILD2004"))
            {
                System.out.println("Invalid coupon code.");
            }
            else
            {
                System.out.println("Order must be at least Rs.500 to use this coupon.");
            }
        }
        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000);
        System.out.println("To confirm payment, your OTP is: " + otp);
        while (true)
        {
            System.out.print("Enter the OTP: ");
            int enteredOtp = s.nextInt();
            if (enteredOtp == otp) break;
            System.out.println("Incorrect OTP. Please try again.");
        }
        int expectedAmount = currentOrder.getPrice() - discount;
        while (true)
        {
            System.out.print("Enter payment amount (Rs." + expectedAmount + "): ");
            int enteredAmount = s.nextInt();
            if (enteredAmount == expectedAmount) break;
            System.out.println("Incorrect amount. Please enter the correct amount.");
        }
        currentOrder.paymentStatus = "paid";
        currentOrder.status = "confirmed";
        paidOrders.add(currentOrder);
        cart.clear();
        System.out.println("Order confirmed and paid. Thank you!");
        orderId++;
        currentOrder = null;
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
                    for (TableBooking b : bookings)
                    {
                        if (b.customerId.equalsIgnoreCase(custId))
                        {
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

    public static void viewAllOrders()
    {
        System.out.println("\n--- Paid Orders ---");
        if (paidOrders.isEmpty())
        {
            System.out.println("No paid orders found.");
        }
        else
        {
            for (Order o : paidOrders)
            {
                o.display();
                System.out.println("---------------------");
            }
        }

        System.out.println("\n--- Unpaid Orders ---");
        if (unpaidOrders.isEmpty())
        {
            System.out.println("No unpaid orders found.");
        }
        else
        {
            for (Order o : unpaidOrders)
            {
                o.display();
                System.out.println("---------------------");
            }
        }
    }

    public static void reserveTable(int people, String name, String phone, Scanner s)
    {
        String tableType = "";
        int tableNumber = -1;
        int seats = 0;
        int bookingFee = 0;
        boolean[] tableArr = null;

        if (people <= 2)
        {
            tableType = "Table2";
            seats = 2;
            tableArr = tables2;
            bookingFee = 100;
        }
        else if (people <= 4)
        {
            tableType = "Table4";
            seats = 4;
            tableArr = tables4;
            bookingFee = 200;
        }
        else if (people <= 6)
        {
            tableType = "Table6";
            seats = 6;
            tableArr = tables6;
            bookingFee = 300;
        }
        else if (people <= 8)
        {
            tableType = "Table8";
            seats = 8;
            tableArr = tables8;
            bookingFee = 400;
        }
        else if (people <= 10)
        {
            tableType = "Table10";
            seats = 10;
            tableArr = tables10;
            bookingFee = 500;
        }
        else
        {
            System.out.println("We don't have tables for more than 10 people.");
            return;
        }

        for (int i = 0; i < tableArr.length; i++)
        {
            if (!tableArr[i])
            {
                tableArr[i] = true;
                tableNumber = i + 1;
                break;
            }
        }

        if (tableNumber == -1)
        {
            System.out.println("Sorry, no available tables for " + seats + " people.");
            return;
        }

        String custId = "CUST" + customerId++;
        TableBooking booking = new TableBooking(tableType, tableNumber, name, phone, seats, custId, bookingFee);
        bookings.add(booking);
        System.out.println("Table booked! Your Customer ID: " + custId + " | Booking Fee: Rs." + bookingFee);

        System.out.print("Pay booking fee (Rs." + bookingFee + "): ");
        int paid = s.nextInt();
        if (paid == bookingFee)
        {
            booking.paymentStatus = "paid";
            paidBookings.add(booking);
            System.out.println("Booking confirmed and paid!");
        }
        else
        {
            System.out.println("Incorrect amount. Booking not paid.");
        }
    }

    public static void printAvailableTables()
    {
        System.out.println("Available tables:");
        printTableStatus("Table2", tables2);
        printTableStatus("Table4", tables4);
        printTableStatus("Table6", tables6);
        printTableStatus("Table8", tables8);
        printTableStatus("Table10", tables10);
    }

    public static void printTableStatus(String name, boolean[] arr)
    {
        System.out.print(name + ": ");
        boolean found = false;
        for (int i = 0; i < arr.length; i++)
        {
            if (!arr[i])
            {
                System.out.print((i + 1) + " ");
                found = true;
            }
        }
        if (!found)
        {
            System.out.print("None");
        }
        System.out.println();
    }

    public static void checkPaymentStatus(int checkId)
    {
        boolean found = false;
        for (Order o : paidOrders)
        {
            if (o.orderId == checkId)
            {
                System.out.println("Order ID: " + checkId + " | Payment Status: " + o.paymentStatus);
                found = true;
                break;
            }
        }
        if (!found)
        {
            for (Order o : unpaidOrders)
            {
                if (o.orderId == checkId)
                {
                    System.out.println("Order ID: " + checkId + " | Payment Status: " + o.paymentStatus);
                    found = true;
                    break;
                }
            }
        }
        if (!found)
        {
            System.out.println("Order ID not found.");
        }
    }

    public static void searchOrderById(int orderIdSearch)
    {
        for (Order o : paidOrders)
        {
            if (o.orderId == orderIdSearch)
            {
                o.display();
                return;
            }
        }
        for (Order o : unpaidOrders)
        {
            if (o.orderId == orderIdSearch)
            {
                o.display();
                return;
            }
        }
        System.out.println("Order ID not found.");
    }
}
