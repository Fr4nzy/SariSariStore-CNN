package org.lu.sarisaristorepos;
public class Product {
    private String name;
    private String price;
    private String imageURL;
    private String stocks;

    public Product(String name, String price, String imageURL, String stocks) {
        this.name = name;
        this.price = price;
        this.imageURL = imageURL;
        this.stocks = stocks;
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
}
