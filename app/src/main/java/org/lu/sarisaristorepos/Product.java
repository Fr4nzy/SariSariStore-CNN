package org.lu.sarisaristorepos;

public class Product {
    private String id; // Firestore document ID
    private String name;
    private String price;
    private String imageURL;
    private String stocks;
    private boolean selected;

    public Product(String id, String name, String price, String imageURL, String stocks) {
        this.id = id; // Initialize the Firestore document ID
        this.name = name;
        this.price = price;
        this.imageURL = imageURL;
        this.stocks = stocks;
        this.selected = false; // Initially not selected
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}