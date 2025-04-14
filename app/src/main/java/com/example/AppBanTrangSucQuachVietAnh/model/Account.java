package com.example.AppBanTrangSucQuachVietAnh.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Model lưu trữ thông tin tài khoản người dùng
 * Bao gồm thông tin cơ bản và phân quyền (admin/user)
 */
public class Account implements Serializable {
    private int id;              // ID tài khoản
    private String username;     // Tên đăng nhập
    private String password;     // Mật khẩu
    private String role;         // Quyền hạn (admin/user)
    private String fullName;     // Họ tên đầy đủ
    private String email;        // Địa chỉ email
    private String phone;        // Số điện thoại
    private String address;      // Địa chỉ
    private Date createdAt;      // Ngày tạo tài khoản
    private Date updatedAt;      // Ngày cập nhật gần nhất

    /**
     * Constructor mặc định
     */
    public Account() {
    }

    /**
     * Constructor với các thông tin cơ bản
     */
    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Constructor đầy đủ thông tin
     */
    public Account(int id, String username, String password, String role,
                  String fullName, String email, String phone, String address,
                  Date createdAt, Date updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
} 