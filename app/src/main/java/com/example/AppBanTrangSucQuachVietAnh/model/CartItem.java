package com.example.AppBanTrangSucQuachVietAnh.model;

import java.io.Serializable;

/**
 * Model lưu trữ thông tin sản phẩm trong giỏ hàng
 * Bao gồm thông tin sản phẩm và số lượng đặt mua
 */
public class CartItem implements Serializable {
    private int id;                 // ID item trong giỏ hàng
    private int userId;             // ID người dùng
    private int productId;          // ID sản phẩm
    private int quantity;           // Số lượng đặt mua
    private String productName;     // Tên sản phẩm
    private double productPrice;    // Giá sản phẩm
    private byte[] productImage;    // Hình ảnh sản phẩm

    /**
     * Constructor mặc định
     */
    public CartItem() {
    }

    /**
     * Constructor với các thông tin cơ bản
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param quantity Số lượng đặt mua
     */
    public CartItem(int userId, int productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Các getter và setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public byte[] getProductImage() {
        return productImage;
    }

    public void setProductImage(byte[] productImage) {
        this.productImage = productImage;
    }

    /**
     * Tính tổng tiền cho sản phẩm trong giỏ hàng
     * @return Tổng tiền = số lượng * đơn giá
     */
    public double getSubtotal() {
        return quantity * productPrice;
    }
} 