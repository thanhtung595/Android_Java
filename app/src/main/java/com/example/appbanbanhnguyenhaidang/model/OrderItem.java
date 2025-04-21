package com.example.appbanbanhnguyenhaidang.model;

public class OrderItem {
    private int id;
    private int orderId;
    private Product product;
    private int quantity;
    private double price;

    public OrderItem(int id, int orderId, Product product, int quantity, double price) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
} 