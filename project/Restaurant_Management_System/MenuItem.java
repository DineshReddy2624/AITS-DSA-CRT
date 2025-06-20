package application;

/**
 * Represents a single item on the restaurant's menu.
 * Updated to include an image URL.
 */
public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String imageUrl; // New: URL for the menu item's image

    public MenuItem(int id, String name, double price) {
        this(id, name, price, ""); // Default to empty string for image URL
    }

    public MenuItem(int id, String name, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        // Ensure a default placeholder if imageUrl is null or empty
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
        } else {
            this.imageUrl = imageUrl;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() { // New Getter for image URL
        return imageUrl;
    }

    // Setters (if allowed to be modified after creation)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) { // New Setter for image URL
        // Ensure a default placeholder if setting to null or empty
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
        } else {
            this.imageUrl = imageUrl;
        }
    }

    @Override
    public String toString() {
        return name + " (Rs." + String.format("%.2f", price) + ")";
    }
}
