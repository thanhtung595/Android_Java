package com.example.AppBanTrangSucQuachVietAnh.model;

public class Cart {
    private int id;
    private int accountId;
    private String status;
    private String createdAt;
    private String updatedAt;

    public Cart() {
    }

    public Cart(int id, int accountId, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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