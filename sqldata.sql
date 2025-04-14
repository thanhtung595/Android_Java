-- Tạo cơ sở dữ liệu
CREATE DATABASE IF NOT EXISTS bakery_db;
USE bakery_db;

-- Tạo bảng Account
CREATE TABLE IF NOT EXISTS Account (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('admin', 'user') NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng Products
CREATE TABLE IF NOT EXISTS Products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image LONGBLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Thêm tài khoản admin mặc định
INSERT INTO Account (username, password, full_name, role) 
VALUES ('admin', 'admin123', 'Administrator', 'admin');

-- Thêm một số sản phẩm mẫu
INSERT INTO Products (name, description, price, image) 
VALUES 
('Bánh mì', 'Bánh mì tươi ngon', 15000.00, ''),
('Bánh ngọt', 'Bánh ngọt thơm ngon', 25000.00, ''),
('Bánh kem', 'Bánh kem tươi', 35000.00, ''); 