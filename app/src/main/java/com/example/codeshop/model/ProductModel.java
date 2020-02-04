package com.example.codeshop.model;

public class ProductModel {
    private String prodName;
    private String prodPrice;
    private String prodImageLink;
    private String prodOfferPrice;
    private String prodDiscountRate;
    private String prodBarcode;
    private String prodBrand;
    private String prodDescritpion;
    private int quantity;

    public ProductModel(){
        this.quantity = 1;
    }

    public int getQuantity(){
        return this.quantity;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public void setProdPrice(String prodPrice) {
        this.prodPrice = prodPrice;
    }

    public void setProdImageLink(String prodImageLink) {
        this.prodImageLink = prodImageLink;
    }

    public void setProdOfferPrice(String prodOfferPrice) {
        this.prodOfferPrice = prodOfferPrice;
    }

    public void setProdDiscountRate(String prodDiscountRate) {
        this.prodDiscountRate = prodDiscountRate;
    }

    public int increaseQuantity(){
        this.quantity ++;
        return this.quantity;
    }

    public int decreaseQuantity(){
        if(this.quantity !=1){
            this.quantity--;
        }
        return this.quantity;
    }

    public void setProdBarcode(String prodBarcode) {
        this.prodBarcode = prodBarcode;
    }

    public void setProdBrand(String prodBrand) {
        this.prodBrand = prodBrand;
    }

    public void setProdDescritpion(String prodDescritpion) {
        this.prodDescritpion = prodDescritpion;
    }

    public String getProdBrand() {
        return prodBrand;
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
