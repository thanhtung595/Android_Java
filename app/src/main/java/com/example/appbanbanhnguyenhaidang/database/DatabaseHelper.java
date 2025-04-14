package com.example.appbanbanhnguyenhaidang.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.example.appbanbanhnguyenhaidang.model.Account;
import com.example.appbanbanhnguyenhaidang.model.Product;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DatabaseHelper - Quản lý kết nối và thao tác với cơ sở dữ liệu MySQL
 */
public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/bakery_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private final Context context;
    private final ExecutorService executorService;

    public interface OnLoginResultListener {
        void onLoginSuccess(Account account);
        void onLoginFailed(String error);
    }

    public interface OnProductsResultListener {
        void onProductsLoaded(List<Product> products);
        void onProductsError(String error);
    }

    public interface OnProductResultListener {
        void onProductLoaded(Product product);
        void onProductError(String error);
    }

    public interface OnOperationResultListener {
        void onOperationSuccess(String message);
        void onOperationFailed(String error);
    }

    public interface LoginCallback {
        void onSuccess(String role);
        void onError(String error);
    }

    public interface DatabaseCallback {
        void onSuccess();
        void onError(String error);
    }

    public DatabaseHelper(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * Kết nối đến database
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC Driver not found", e);
            throw new SQLException("MySQL JDBC Driver not found");
        }
    }

    /**
     * Kiểm tra đăng nhập
     */
    public void checkLogin(String username, String password, LoginCallback callback) {
        Log.d(TAG, "Attempting login for user: " + username);
        executorService.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = getConnection();
                Log.d(TAG, "Database connection established");

                String query = "SELECT * FROM account WHERE username = ? AND password = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, password);
                Log.d(TAG, "Executing query: " + query);

                rs = stmt.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    Log.d(TAG, "Login successful. Role: " + role);
                    callback.onSuccess(role);
                } else {
                    Log.d(TAG, "Login failed: Invalid credentials");
                    callback.onError("Invalid username or password");
                }
            } catch (SQLException e) {
                Log.e(TAG, "Database error during login", e);
                callback.onError("Database error: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    Log.e(TAG, "Error closing database resources", e);
                }
            }
        });
    }

    /**
     * Lấy tất cả sản phẩm
     */
    public void getAllProducts(OnProductsResultListener listener) {
        executorService.execute(() -> {
            try {
                List<Product> products = new ArrayList<>();
                String query = "SELECT * FROM products";
                
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    while (rs.next()) {
                        Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getBytes("image")
                        );
                        products.add(product);
                    }
                    listener.onProductsLoaded(products);
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error getting all products", e);
                listener.onProductsError("Error loading products: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public void getProductById(int id, OnProductResultListener listener) {
        executorService.execute(() -> {
            try {
                String query = "SELECT * FROM Products WHERE id = ?";
                
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getBytes("image")
                        );
                        listener.onProductLoaded(product);
                    } else {
                        listener.onProductError("Không tìm thấy sản phẩm");
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error getting product by id", e);
                listener.onProductError("Lỗi kết nối database: " + e.getMessage());
            }
        });
    }

    /**
     * Thêm sản phẩm mới
     */
    public void addProduct(Product product, DatabaseCallback callback) {
        executorService.execute(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO products (name, description, price, image) VALUES (?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {
                
                Log.d("DatabaseHelper", "Adding product: " + product.getName());
                Log.d("DatabaseHelper", "Image size: " + (product.getImage() != null ? product.getImage().length : 0) + " bytes");
                
                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getDescription());
                pstmt.setDouble(3, product.getPrice());
                pstmt.setBytes(4, product.getImage());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            product.setId(rs.getInt(1));
                            Log.d("DatabaseHelper", "Product added successfully with ID: " + product.getId());
                            callback.onSuccess();
                        }
                    }
                } else {
                    Log.e("DatabaseHelper", "Failed to add product: No rows affected");
                    callback.onError("Failed to add product");
                }
            } catch (SQLException e) {
                Log.e("DatabaseHelper", "Error adding product: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Cập nhật sản phẩm
     */
    public void updateProduct(Product product, DatabaseCallback callback) {
        executorService.execute(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE products SET name = ?, description = ?, price = ?, image = ? WHERE id = ?")) {
                
                Log.d("DatabaseHelper", "Updating product: " + product.getName());
                Log.d("DatabaseHelper", "Image size: " + (product.getImage() != null ? product.getImage().length : 0) + " bytes");
                
                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getDescription());
                pstmt.setDouble(3, product.getPrice());
                pstmt.setBytes(4, product.getImage());
                pstmt.setInt(5, product.getId());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    Log.d("DatabaseHelper", "Product updated successfully");
                    callback.onSuccess();
                } else {
                    Log.e("DatabaseHelper", "Failed to update product: No rows affected");
                    callback.onError("Failed to update product");
                }
            } catch (SQLException e) {
                Log.e("DatabaseHelper", "Error updating product: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Xóa sản phẩm
     */
    public void deleteProduct(int id, OnOperationResultListener listener) {
        executorService.execute(() -> {
            try {
                String query = "DELETE FROM Products WHERE id = ?";
                
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setInt(1, id);
                    if (stmt.executeUpdate() > 0) {
                        listener.onOperationSuccess("Xóa sản phẩm thành công");
                    } else {
                        listener.onOperationFailed("Xóa sản phẩm thất bại");
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error deleting product", e);
                listener.onOperationFailed("Lỗi kết nối database: " + e.getMessage());
            }
        });
    }
} 