-- Tạo bảng cart để lưu thông tin giỏ hàng
CREATE TABLE IF NOT EXISTS cart (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES account(id)
);

-- Tạo bảng cart_items để lưu chi tiết các sản phẩm trong giỏ hàng
CREATE TABLE IF NOT EXISTS cart_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Thêm cột username vào bảng account nếu chưa có
ALTER TABLE account ADD COLUMN IF NOT EXISTS id INT PRIMARY KEY AUTO_INCREMENT FIRST; 