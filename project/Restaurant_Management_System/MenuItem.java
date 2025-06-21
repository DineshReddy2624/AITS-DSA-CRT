package application;

import java.util.Objects;

/**
 * Represents a single menu item available in the restaurant.
 * Includes properties like ID, name, price, image URL, and stock quantity.
 */
public class MenuItem {
    private int id; // Unique identifier for the menu item
    private String name;
    private double price;
    private String imageUrl; // URL to the image of the menu item
    private int stock; // New: Added stock quantity for inventory management

    /**
     * Constructor for a new MenuItem with default image and zero stock.
     * @param id The unique ID of the menu item.
     * @param name The name of the menu item.
     * @param price The price of the menu item.
     */
    public MenuItem(int id, String name, double price) {
        // Calls the more comprehensive constructor with default values for imageUrl and stock
        this(id, name, price, "", 0);
    }

    /**
     * Constructor for a new MenuItem with a specified image URL and zero stock.
     * @param id The unique ID of the menu item.
     * @param name The name of the menu item.
     * @param price The price of the menu item.
     * @param imageUrl The URL of the image for the menu item.
     */
    public MenuItem(int id, String name, double price, String imageUrl) {
        // Calls the full constructor with a default stock of 0
        this(id, name, price, imageUrl, 0);
    }

    /**
     * Full constructor for MenuItem, including stock.
     * @param id The unique ID of the menu item.
     * @param name The name of the menu item.
     * @param price The price of the menu item.
     * @param imageUrl The URL of the image for the menu item.
     * @param stock The current stock quantity of the menu item.
     */
    public MenuItem(int id, String name, double price, String imageUrl, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        // Set a default placeholder image if the provided URL is null or empty
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
        } else {
            this.imageUrl = imageUrl;
        }
        this.stock = stock;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStock() { // New: Getter for stock
        return stock;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        // Ensure that a valid URL is set, otherwise use a placeholder
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
        } else {
            this.imageUrl = imageUrl;
        }
    }

    public void setStock(int stock) { // New: Setter for stock
        this.stock = stock;
    }

    /**
     * Provides a string representation of the MenuItem object.
     * @return A formatted string displaying item details including stock.
     */
    @Override
    public String toString() {
        return "MenuItem{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", price=" + price +
               ", imageUrl='" + imageUrl + '\'' +
               ", stock=" + stock + // Include stock in toString
               '}';
    }

    /**
     * Checks if two MenuItem objects are equal.
     * Equality is based on ID, name, price, image URL, and stock.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id &&
               Double.compare(menuItem.price, price) == 0 &&
               stock == menuItem.stock && // Include stock in equals
               Objects.equals(name, menuItem.name) &&
               Objects.equals(imageUrl, menuItem.imageUrl);
    }

    /**
     * Generates a hash code for the MenuItem object.
     * The hash code is based on ID, name, price, image URL, and stock.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, imageUrl, stock);
    }
}
