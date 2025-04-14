package com.example.AppBanTrangSucQuachVietAnh.data;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối đến cơ sở dữ liệu MySQL
 * Cung cấp các phương thức để tạo và quản lý kết nối
 */
public class MySQLConnection {
    private static final String TAG = "MySQLConnection";
    
    // Thông tin kết nối
    private static final String HOST = "10.0.2.2";
    private static final int PORT = 3306;
    private static final String DATABASE = "jewelry_sales_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG, "MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Error loading MySQL JDBC Driver", e);
        }
    }

    /**
     * Tạo và trả về kết nối đến cơ sở dữ liệu
     * @return Connection đối tượng kết nối
     * @throws SQLException nếu không thể kết nối
     */
    public static Connection getConnection() throws SQLException {
        Log.d(TAG, "Đang tạo kết nối mới đến MySQL...");
        
        try {
            // Đăng ký driver MySQL
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG, "Đã đăng ký MySQL driver");
            
            // Tạo URL kết nối
            String url = String.format("jdbc:mysql://%s:%d/%s", HOST, PORT, DATABASE);
            Log.d(TAG, "URL kết nối: " + url);
            
            // Tạo kết nối mới
            connection = DriverManager.getConnection(url, USERNAME, PASSWORD);
            Log.d(TAG, "Đã kết nối thành công đến MySQL");
            
            return connection;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Không tìm thấy MySQL driver: " + e.getMessage(), e);
            throw new SQLException("Không tìm thấy MySQL driver", e);
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kết nối MySQL: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Kiểm tra kết nối hiện tại
     * @return true nếu kết nối vẫn hoạt động
     */
    public static boolean checkConnection() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kiểm tra kết nối: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Đóng kết nối hiện tại nếu còn mở
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Log.d(TAG, "Đã đóng kết nối MySQL");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi đóng kết nối: " + e.getMessage(), e);
        }
    }

    /**
     * Thử kết nối lại nếu kết nối hiện tại không khả dụng
     * @return true nếu kết nối lại thành công
     */
    public static boolean tryReconnect() {
        try {
            Log.d(TAG, "Đang thử kết nối lại...");
            if (!checkConnection()) {
                closeConnection();
                connection = getConnection();
                return checkConnection();
            }
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kết nối lại: " + e.getMessage(), e);
            return false;
        }
    }
} 