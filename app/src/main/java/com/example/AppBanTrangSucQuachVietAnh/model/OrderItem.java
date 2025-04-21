package com.example.AppBanTrangSucQuachVietAnh.model;

public class OrderItem {
    private int id;
    private int orderId;
    private Jewelry product;
    private int quantity;
    private double price;
    private String createdAt;

    public OrderItem() {
    }

    public OrderItem(int id, int orderId, Jewelry product, int quantity, double price, String createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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
} 