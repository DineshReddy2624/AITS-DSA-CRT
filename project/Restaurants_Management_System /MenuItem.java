package application;

/**
 * Represents a single menu item in the restaurant.
 * Includes basic details like ID, name, and price.
 */
public class MenuItem {
    private int id;
    private String name;
    private double price;

    /**
     * Constructor for MenuItem.
     * @param id The unique identifier for the menu item.
     * @param name The name of the menu item.
     * @param price The price of the menu item.
     */
    public MenuItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
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

    // --- Setters (optional, but useful if items can be updated) ---
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Provides a string representation of the menu item for display.
     */
    @Override
    public String toString() {
        return id + ": " + name + " - Rs." + String.format("%.2f", price);
    }
}

