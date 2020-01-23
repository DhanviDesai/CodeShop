package com.example.codeshop.model;

public class ProductModel {
    private String prodName;
    private String prodPrice;
    private String prodImageLink;
    private String prodOfferPrice;
    private String prodDiscountRate;
    private String prodBarcode;

    public ProductModel(String prodName, String prodPrice, String prodImageLink, String prodOfferPrice, String prodDiscountRate, String prodBarcode) {
        this.prodName = prodName;
        this.prodPrice = prodPrice;
        this.prodImageLink = prodImageLink;
        this.prodOfferPrice = prodOfferPrice;
        this.prodDiscountRate = prodDiscountRate;
        this.prodBarcode = prodBarcode;
    }

    public String getProdBarcode() {
        return prodBarcode;
    }

    public String getProdName() {
        return prodName;
    }

    public String getProdPrice() {
        return prodPrice;
    }

    public String getProdImageLink() {
        return prodImageLink;
    }

    public String getProdOfferPrice() {
        return prodOfferPrice;
    }

    public String getProdDiscountRate() {
        return prodDiscountRate;
    }
}
