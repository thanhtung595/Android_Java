package com.example.AppBanTrangSucQuachVietAnh.model;

public class CartItem {
    private int id;
    private int cartId;
    private Jewelry product;
    private int quantity;
    private double price;
    private String createdAt;
    private String updatedAt;

    public CartItem() {
    }

    public CartItem(int id, int cartId, Jewelry product, int quantity, double price, String createdAt, String updatedAt) {
        this.id = id;
        this.cartId = cartId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public Jewelry getProduct() {
        return product;
    }

    public void setProduct(Jewelry product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
} 