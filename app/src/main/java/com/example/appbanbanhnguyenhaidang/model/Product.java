package com.example.appbanbanhnguyenhaidang.model;

/**
 * Model Product - Lưu trữ thông tin sản phẩm
 * Bao gồm: id, name, description, price, image
 */
public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private byte[] image;

    public Product() {
    }

    public Product(int id, String name, String description, double price, byte[] image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    // Getter và Setter
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
} 