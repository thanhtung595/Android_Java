package com.example.AppBanTrangSucQuachVietAnh.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Model lưu trữ thông tin sản phẩm trang sức
 * Bao gồm thông tin cơ bản và hình ảnh của sản phẩm
 */
public class Jewelry implements Serializable {
    private int id;              // ID sản phẩm
    private String name;         // Tên sản phẩm
    private String description;  // Mô tả sản phẩm
    private double price;        // Giá bán
    private int stock;          // Số lượng tồn kho
    private String category;     // Danh mục sản phẩm
    private byte[] image;        // Hình ảnh sản phẩm
    private int createdBy;      // Người tạo
    private Date createdAt;      // Ngày tạo
    private Date updatedAt;      // Ngày cập nhật

    /**
     * Constructor mặc định
     */
    public Jewelry() {
    }

    /**
     * Constructor với các thông tin cơ bản
     */
    public Jewelry(String name, String description, double price, int stock, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    /**
     * Constructor đầy đủ thông tin
     */
    public Jewelry(int id, String name, String description, double price, 
                  int stock, String category, byte[] image, 
                  Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Các getter và setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Jewelry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", createdBy=" + createdBy +
                '}';
    }
} 