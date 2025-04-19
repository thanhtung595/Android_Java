package com.example.AppBanTrangSucQuachVietAnh.model;

import java.io.Serializable;

public class OrderDetail implements Serializable {
    // Các hằng số trạng thái đơn hàng
    public static final String STATUS_PENDING = "pending";    // Đơn hàng đang chờ xử lý
    public static final String STATUS_COMPLETED = "completed"; // Đơn hàng đã hoàn thành
    public static final String STATUS_CANCELLED = "cancelled"; // Đơn hàng đã hủy

    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private double price;
    private String productName;
    private byte[] productImage;

    public OrderDetail() {
    }

    // Getters and Setters
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

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public byte[] getProductImage() {
        return productImage;
    }

    public void setProductImage(byte[] productImage) {
        this.productImage = productImage;
    }

    public double getSubtotal() {
        return quantity * price;
    }
} 