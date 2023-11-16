package org.lu.sarisaristorepos;

public class Product {
    private String id, name, imageURL, category;
    private String price;
    private String stocks;
    private String quantity;
    private boolean selected;  // Add selected property

    public Product(String id, String name, String price, String imageURL, String stocks, String category, String quantity) {
        this.id = id; // Initialize the Firestore document ID
        this.name = name;
        this.price = price;
        this.imageURL = imageURL;
        this.stocks = stocks;
        this.category = category;
        this.quantity = quantity;
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

    public String getStocks() {
        return stocks;
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
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
