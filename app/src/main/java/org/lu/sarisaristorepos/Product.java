package org.lu.sarisaristorepos;

public class Product {
    private String id, name, imageURL,brand, category;
    private String price;
    private boolean selected;  // Add selected property

    public Product(String id, String name, String price, String imageURL, String brand, String category) {
        this.id = id; // Initialize the Firestore document ID
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.imageURL = imageURL;
        this.category = category;
        this.selected = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getBrand() {
        return brand;
    }
    public String getCategory() {
        return category;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
