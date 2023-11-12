package org.lu.sarisaristorepos;
public class Product {
    private String name;
    private String price;
    private String imageURL;
    private String stocks;
    private boolean selected;

    public Product(String name, String price, String imageURL, String stocks) {
        this.name = name;
        this.price = price;
        this.imageURL = imageURL;
        this.stocks = stocks;
        this.selected = false; // Initially not selected
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
