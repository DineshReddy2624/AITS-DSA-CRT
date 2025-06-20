package application;

import java.util.Objects;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String imageUrl;

    public MenuItem(int id, String name, double price) {
        this(id, name, price, "");
    }

    public MenuItem(int id, String name, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
        } else {
            this.imageUrl = imageUrl;
        }
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

    public String getImageUrl() {
        return imageUrl;
    }

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
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            this.imageUrl = "https://placehold.co/100x100/CCCCCC/FFFFFF?text=Dish";
        } else {
            this.imageUrl = imageUrl;
        }
    }

    @Override
    public String toString() {
        return "MenuItem{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", price=" + price +
               ", imageUrl='" + imageUrl + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
