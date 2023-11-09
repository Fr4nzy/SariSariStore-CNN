package org.lu.sarisaristorepos;

public class ProductClass {
    private String ProductName, ProductCategory, ProductDescription, ProductImage, DocId, UserId;

    public ProductClass() {
    }

    public ProductClass(String productName, String productCategory, String productDescription,
                        String productImage, String docId, String userId) {
        ProductName = productName;
        ProductCategory = productCategory;
        ProductDescription = productDescription;
        ProductImage = productImage;
        DocId = docId;
        UserId = userId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductCategory() {
        return ProductCategory;
    }

    public void setProductCategory(String productCategory) {
        ProductCategory = productCategory;
    }

    public String getProductDescription() {
        return ProductDescription;
    }

    public void setProductDescription(String productDescription) {
        ProductDescription = productDescription;
    }

    public String getProductImage() {
        return ProductImage;
    }

    public void setProductImage(String productImage) {
        ProductImage = productImage;
    }

    public String getDocId() {
        return DocId;
    }

    public void setDocId(String docId) {
        DocId = docId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
