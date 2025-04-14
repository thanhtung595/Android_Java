package com.example.AppBanTrangSucQuachVietAnh.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.example.AppBanTrangSucQuachVietAnh.model.Account;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp quản lý tương tác với cơ sở dữ liệu
 * Cung cấp các phương thức CRUD cho các đối tượng Account và Jewelry
 */
public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private Connection connection;

    /**
     * Constructor riêng cho Singleton pattern
     */
    private DatabaseManager() {
    }

    /**
     * Lấy instance của DatabaseManager (Singleton pattern)
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Kiểm tra kết nối đến cơ sở dữ liệu
     * @return true nếu kết nối thành công
     */
    public boolean checkConnection() {
        Log.d(TAG, "Đang kiểm tra kết nối database...");
        try {
            if (connection == null || connection.isClosed()) {
                Log.d(TAG, "Kết nối null hoặc đã đóng, thử kết nối lại");
                connection = MySQLConnection.getConnection();
            }
            
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SELECT 1");
                    Log.d(TAG, "Kiểm tra kết nối thành công");
                    return true;
                }
            }
            Log.e(TAG, "Không thể tạo kết nối");
            return false;
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kiểm tra kết nối: " + e.getMessage(), e);
            tryReconnect();
            return false;
        }
    }

    /**
     * Đóng kết nối đến cơ sở dữ liệu
     */
    public void close() {
        MySQLConnection.closeConnection();
    }

    /**
     * Kiểm tra thông tin đăng nhập
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Account nếu đăng nhập thành công, null nếu thất bại
     */
    public Account checkLogin(String username, String password) {
        try {
            // Đảm bảo có kết nối trước khi thực hiện truy vấn
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kết nối đến database");
                return null;
            }

            String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account();
                        account.setId(rs.getInt("id"));
                        account.setUsername(rs.getString("username"));
                        account.setRole(rs.getString("role"));
                        account.setFullName(rs.getString("full_name"));
                        account.setCreatedAt(rs.getTimestamp("created_at"));
                        account.setUpdatedAt(rs.getTimestamp("updated_at"));
                        return account;
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kiểm tra đăng nhập: " + e.getMessage(), e);
            tryReconnect();
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả sản phẩm
     * @return List<Jewelry> danh sách sản phẩm
     */
    public List<Jewelry> getAllJewelry() {
        List<Jewelry> jewelryList = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY created_at DESC";
        Statement stmt = null;
        ResultSet rs = null;
        
        // Kiểm tra và khôi phục kết nối nếu cần
        if (!checkConnection()) {
            Log.d(TAG, "Kết nối đã đóng, đang thử kết nối lại...");
            tryReconnect();
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kết nối lại database");
                return jewelryList;
            }
        }

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Jewelry jewelry = new Jewelry();
                jewelry.setId(rs.getInt("id"));
                jewelry.setName(rs.getString("name"));
                jewelry.setDescription(rs.getString("description"));
                jewelry.setPrice(rs.getDouble("price"));
                jewelry.setStock(rs.getInt("stock"));
                jewelry.setCategory(rs.getString("category"));
                jewelry.setImage(rs.getBytes("image"));
                jewelry.setCreatedBy(rs.getInt("created_by"));
                jewelry.setCreatedAt(rs.getTimestamp("created_at"));
                jewelry.setUpdatedAt(rs.getTimestamp("updated_at"));
                jewelryList.add(jewelry);
            }
            
            Log.d(TAG, "Lấy danh sách sản phẩm thành công: " + jewelryList.size() + " sản phẩm");
            return jewelryList;
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi lấy danh sách sản phẩm: " + e.getMessage(), e);
            return jewelryList;
        } finally {
            // Đóng ResultSet và Statement trong khối finally
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi đóng ResultSet/Statement: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Thêm sản phẩm mới
     * @param jewelry Đối tượng Jewelry cần thêm
     * @return true nếu thêm thành công
     */
    public boolean addJewelry(Jewelry jewelry) {
        Log.d(TAG, "Bắt đầu thêm sản phẩm mới");
        String sql = "INSERT INTO products (name, description, price, stock, category, image, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        // Kiểm tra và khôi phục kết nối nếu cần
        if (!checkConnection()) {
            Log.d(TAG, "Kết nối đã đóng, đang thử kết nối lại...");
            tryReconnect();
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kết nối lại database");
                return false;
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jewelry.getName());
            stmt.setString(2, jewelry.getDescription());
            stmt.setDouble(3, jewelry.getPrice());
            stmt.setInt(4, jewelry.getStock());
            stmt.setString(5, jewelry.getCategory());
            stmt.setBytes(6, jewelry.getImage());
            stmt.setInt(7, jewelry.getCreatedBy());
            
            Log.d(TAG, "Đang thực hiện thêm sản phẩm vào database");
            int rowsAffected = stmt.executeUpdate();
            Log.d(TAG, "Thêm sản phẩm thành công, số dòng ảnh hưởng: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi thêm sản phẩm: " + e.getMessage(), e);
            // Thử kết nối lại nếu lỗi là do mất kết nối
            if (e.getMessage().contains("connection closed") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Communications link failure")) {
                Log.d(TAG, "Lỗi kết nối, đang thử kết nối lại...");
                tryReconnect();
                // Thử thêm sản phẩm lại một lần nữa
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, jewelry.getName());
                    stmt.setString(2, jewelry.getDescription());
                    stmt.setDouble(3, jewelry.getPrice());
                    stmt.setInt(4, jewelry.getStock());
                    stmt.setString(5, jewelry.getCategory());
                    stmt.setBytes(6, jewelry.getImage());
                    stmt.setInt(7, jewelry.getCreatedBy());
                    
                    Log.d(TAG, "Đang thử thêm sản phẩm lại sau khi kết nối lại");
                    int rowsAffected = stmt.executeUpdate();
                    Log.d(TAG, "Thêm sản phẩm thành công sau khi thử lại, số dòng ảnh hưởng: " + rowsAffected);
                    return rowsAffected > 0;
                } catch (SQLException e2) {
                    Log.e(TAG, "Lỗi thêm sản phẩm sau khi thử kết nối lại: " + e2.getMessage(), e2);
                }
            }
            return false;
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     * @param jewelry Đối tượng Jewelry cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean updateJewelry(Jewelry jewelry) {
        Log.d(TAG, "Bắt đầu cập nhật sản phẩm ID: " + jewelry.getId());
        String sql = "UPDATE products SET name=?, description=?, price=?, stock=?, category=?, image=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        
        // Kiểm tra và khôi phục kết nối nếu cần
        if (!checkConnection()) {
            Log.d(TAG, "Kết nối đã đóng, đang thử kết nối lại...");
            tryReconnect();
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kết nối lại database");
                return false;
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jewelry.getName());
            stmt.setString(2, jewelry.getDescription());
            stmt.setDouble(3, jewelry.getPrice());
            stmt.setInt(4, jewelry.getStock());
            stmt.setString(5, jewelry.getCategory());
            stmt.setBytes(6, jewelry.getImage());
            stmt.setInt(7, jewelry.getId());
            
            Log.d(TAG, "Đang thực hiện cập nhật sản phẩm trong database");
            int rowsAffected = stmt.executeUpdate();
            Log.d(TAG, "Cập nhật sản phẩm thành công, số dòng ảnh hưởng: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi cập nhật sản phẩm: " + e.getMessage(), e);
            // Thử kết nối lại nếu lỗi là do mất kết nối
            if (e.getMessage().contains("connection closed") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Communications link failure")) {
                Log.d(TAG, "Lỗi kết nối, đang thử kết nối lại...");
                tryReconnect();
                // Thử cập nhật sản phẩm lại một lần nữa
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, jewelry.getName());
                    stmt.setString(2, jewelry.getDescription());
                    stmt.setDouble(3, jewelry.getPrice());
                    stmt.setInt(4, jewelry.getStock());
                    stmt.setString(5, jewelry.getCategory());
                    stmt.setBytes(6, jewelry.getImage());
                    stmt.setInt(7, jewelry.getId());
                    
                    Log.d(TAG, "Đang thử cập nhật sản phẩm lại sau khi kết nối lại");
                    int rowsAffected = stmt.executeUpdate();
                    Log.d(TAG, "Cập nhật sản phẩm thành công sau khi thử lại, số dòng ảnh hưởng: " + rowsAffected);
                    return rowsAffected > 0;
                } catch (SQLException e2) {
                    Log.e(TAG, "Lỗi cập nhật sản phẩm sau khi thử kết nối lại: " + e2.getMessage(), e2);
                }
            }
            return false;
        }
    }

    /**
     * Xóa sản phẩm
     * @param id ID của sản phẩm cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteJewelry(int id) {
        Log.d(TAG, "Bắt đầu xóa sản phẩm ID: " + id);
        String sql = "DELETE FROM products WHERE id=?";

        // Kiểm tra và khôi phục kết nối nếu cần
        if (!checkConnection()) {
            Log.d(TAG, "Kết nối đã đóng, đang thử kết nối lại...");
            tryReconnect();
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kết nối lại database");
                return false;
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            Log.d(TAG, "Đang thực hiện xóa sản phẩm");
            int rowsAffected = stmt.executeUpdate();
            Log.d(TAG, "Xóa sản phẩm thành công, số dòng ảnh hưởng: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi xóa sản phẩm: " + e.getMessage(), e);
            // Thử kết nối lại nếu lỗi là do mất kết nối
            if (e.getMessage().contains("connection closed") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("Communications link failure")) {
                Log.d(TAG, "Lỗi kết nối, đang thử kết nối lại...");
                tryReconnect();
                // Thử xóa sản phẩm lại một lần nữa
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    Log.d(TAG, "Đang thử xóa sản phẩm lại sau khi kết nối lại");
                    int rowsAffected = stmt.executeUpdate();
                    Log.d(TAG, "Xóa sản phẩm thành công sau khi thử lại, số dòng ảnh hưởng: " + rowsAffected);
                    return rowsAffected > 0;
                } catch (SQLException e2) {
                    Log.e(TAG, "Lỗi xóa sản phẩm sau khi thử kết nối lại: " + e2.getMessage(), e2);
                }
            }
            return false;
        }
    }

    /**
     * Tìm kiếm sản phẩm theo tên hoặc danh mục
     * @param keyword Từ khóa tìm kiếm
     * @return List<Jewelry> danh sách sản phẩm tìm thấy
     */
    public List<Jewelry> searchJewelry(String keyword) {
        List<Jewelry> jewelryList = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR category LIKE ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Jewelry jewelry = new Jewelry();
                    jewelry.setId(rs.getInt("id"));
                    jewelry.setName(rs.getString("name"));
                    jewelry.setDescription(rs.getString("description"));
                    jewelry.setPrice(rs.getDouble("price"));
                    jewelry.setStock(rs.getInt("stock"));
                    jewelry.setCategory(rs.getString("category"));
                    jewelry.setImage(rs.getBytes("image"));
                    jewelry.setCreatedBy(rs.getInt("created_by"));
                    jewelry.setCreatedAt(rs.getTimestamp("created_at"));
                    jewelry.setUpdatedAt(rs.getTimestamp("updated_at"));
                    jewelryList.add(jewelry);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
        return jewelryList;
    }

    /**
     * Lọc sản phẩm theo danh mục
     * @param category Danh mục cần lọc
     * @return List<Jewelry> danh sách sản phẩm thuộc danh mục
     */
    public List<Jewelry> filterByCategory(String category) {
        List<Jewelry> jewelryList = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Jewelry jewelry = new Jewelry();
                    jewelry.setId(rs.getInt("id"));
                    jewelry.setName(rs.getString("name"));
                    jewelry.setDescription(rs.getString("description"));
                    jewelry.setPrice(rs.getDouble("price"));
                    jewelry.setStock(rs.getInt("stock"));
                    jewelry.setCategory(rs.getString("category"));
                    jewelry.setImage(rs.getBytes("image"));
                    jewelry.setCreatedBy(rs.getInt("created_by"));
                    jewelry.setCreatedAt(rs.getTimestamp("created_at"));
                    jewelry.setUpdatedAt(rs.getTimestamp("updated_at"));
                    jewelryList.add(jewelry);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi lọc sản phẩm theo danh mục: " + e.getMessage(), e);
        }
        return jewelryList;
    }

    private synchronized Connection getConnection() throws SQLException {
        Log.d(TAG, "Getting database connection...");
        try {
            if (connection == null || connection.isClosed()) {
                Log.d(TAG, "Connection is null or closed, creating new connection...");
                connection = MySQLConnection.getConnection();
                if (connection == null) {
                    Log.e(TAG, "Failed to create new connection");
                    throw new SQLException("Could not create connection to database server");
                }
            }

            // Test the connection with a timeout
            if (!connection.isValid(5)) {
                Log.e(TAG, "Connection is invalid, attempting to reconnect...");
                connection = MySQLConnection.getConnection();
                if (!connection.isValid(5)) {
                    Log.e(TAG, "Reconnection failed");
                    throw new SQLException("Could not establish a valid database connection");
                }
            }

            Log.d(TAG, "Database connection successful");
            return connection;
        } catch (SQLException e) {
            Log.e(TAG, "Error getting database connection: " + e.getMessage());
            Log.e(TAG, "SQL State: " + e.getSQLState());
            Log.e(TAG, "Error Code: " + e.getErrorCode());
            throw e;
        }
    }

    private void checkTable(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, null, tableName, null)) {
            if (!tables.next()) {
                Log.e(TAG, "Table '" + tableName + "' does not exist!");
            } else {
                Log.i(TAG, "Table '" + tableName + "' exists");
                try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                    Log.d(TAG, "Columns in " + tableName + " table:");
                    while (columns.next()) {
                        Log.d(TAG, columns.getString("COLUMN_NAME") + " - " + columns.getString("TYPE_NAME"));
                    }
                }
            }
        }
    }

    public Jewelry getJewelryById(int id) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT * FROM products WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Jewelry jewelry = new Jewelry();
                        jewelry.setId(rs.getInt("id"));
                        jewelry.setName(rs.getString("name"));
                        jewelry.setDescription(rs.getString("description"));
                        jewelry.setPrice(rs.getDouble("price"));
                        jewelry.setStock(rs.getInt("stock"));
                        jewelry.setCategory(rs.getString("category"));
                        jewelry.setImage(rs.getBytes("image"));
                        jewelry.setCreatedBy(rs.getInt("created_by"));
                        jewelry.setCreatedAt(rs.getTimestamp("created_at"));
                        jewelry.setUpdatedAt(rs.getTimestamp("updated_at"));
                        return jewelry;
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi lấy sản phẩm theo ID: " + e.getMessage(), e);
            tryReconnect();
        }
        return null;
    }

    /**
     * Thử kết nối lại khi gặp lỗi
     */
    private void tryReconnect() {
        Log.d(TAG, "Đang thử kết nối lại...");
        try {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    Log.e(TAG, "Lỗi đóng kết nối cũ: " + e.getMessage(), e);
                }
            }
            connection = MySQLConnection.getConnection();
            Log.d(TAG, "Kết nối lại thành công");
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi kết nối lại: " + e.getMessage(), e);
        }
    }

    // Utility methods
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return null;
        }
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            if (!success) {
                Log.e(TAG, "Failed to compress bitmap");
                return null;
            }
            byte[] byteArray = stream.toByteArray();
            Log.d(TAG, "Converted bitmap to byte array, size: " + byteArray.length + " bytes");
            return byteArray;
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to byte array: " + e.getMessage(), e);
            return null;
        }
    }

    public static Bitmap byteArrayToBitmap(byte[] data) {
        if (data == null) {
            Log.e(TAG, "Byte array is null");
            return null;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode byte array to bitmap");
                return null;
            }
            Log.d(TAG, "Converted byte array to bitmap, size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error converting byte array to bitmap: " + e.getMessage(), e);
            return null;
        }
    }

    private void closeResources(PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error closing resources: " + e.getMessage(), e);
        }
    }
} 