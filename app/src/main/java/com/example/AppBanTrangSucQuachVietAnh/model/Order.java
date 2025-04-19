package com.example.AppBanTrangSucQuachVietAnh.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Model lưu trữ thông tin đơn hàng
 * Bao gồm thông tin cơ bản và chi tiết các sản phẩm trong đơn hàng
 */
public class Order implements Serializable {
    // Các hằng số trạng thái đơn hàng
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    private int id;                 // ID đơn hàng
    private int userId;             // ID người dùng đặt hàng
    private double totalAmount;     // Tổng tiền đơn hàng
    private String status;          // Trạng thái đơn hàng
    private Timestamp createdAt;    // Ngày tạo đơn hàng
    private Timestamp updatedAt;    // Ngày cập nhật gần nhất
    private List<OrderDetail> orderDetails; // Chi tiết các sản phẩm trong đơn hàng

    /**
     * Constructor mặc định
     */
    public Order() {
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
} 