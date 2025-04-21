package com.example.AppBanTrangSucQuachVietAnh.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.example.AppBanTrangSucQuachVietAnh.model.Account;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;
import com.example.AppBanTrangSucQuachVietAnh.model.OrderItem;
import com.example.AppBanTrangSucQuachVietAnh.model.Product;

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
    private final Object lock = new Object(); // Thêm lock object để đồng bộ hóa

    /**
     * Constructor riêng cho Singleton pattern
     */
    private DatabaseManager() {
        // Không gọi checkConnection và createProductsTable trong constructor
        // Chúng sẽ được gọi khi cần thiết
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
        synchronized (lock) {
            try {
                if (connection == null || connection.isClosed()) {
                    Log.d(TAG, "Kết nối chưa tồn tại hoặc đã đóng, đang tạo kết nối mới...");
                    connection = MySQLConnection.getConnection();
                    return connection != null && !connection.isClosed();
                }
                return true;
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi kiểm tra kết nối: " + e.getMessage(), e);
                return false;
            }
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
        
        synchronized (lock) {
            // Kiểm tra và khôi phục kết nối nếu cần
            if (!checkConnection()) {
                Log.d(TAG, "Kết nối đã đóng, đang thử kết nối lại...");
                tryReconnect();
                if (!checkConnection()) {
                    Log.e(TAG, "Không thể kết nối lại database");
                    return jewelryList;
                }
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    try {
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
                    } catch (SQLException e) {
                        Log.e(TAG, "Lỗi khi đọc dữ liệu sản phẩm: " + e.getMessage(), e);
                        continue;
                    }
                }
                
                Log.d(TAG, "Lấy danh sách sản phẩm thành công: " + jewelryList.size() + " sản phẩm");
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi lấy danh sách sản phẩm: " + e.getMessage(), e);
            }
        }
        
        return jewelryList;
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
        String sql;
        PreparedStatement stmt;
        
        synchronized (lock) {
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kết nối đến database");
                return jewelryList;
            }

            try {
                if (keyword == null || keyword.trim().isEmpty()) {
                    // Nếu không có từ khóa, lấy tất cả sản phẩm
                    sql = "SELECT * FROM products ORDER BY created_at DESC";
                    stmt = connection.prepareStatement(sql);
                } else {
                    // Nếu có từ khóa, tìm kiếm theo tên
                    sql = "SELECT * FROM products WHERE name LIKE ? ORDER BY created_at DESC";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, "%" + keyword + "%");
                }
                
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
                Log.d(TAG, "Tìm thấy " + jewelryList.size() + " sản phẩm");
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi tìm kiếm sản phẩm: " + e.getMessage(), e);
            }
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

    /**
     * Kiểm tra cấu trúc bảng products
     */
    public void checkProductsTable() {
        synchronized (lock) {
            if (!checkConnection()) {
                Log.e(TAG, "Không thể kiểm tra bảng products do không có kết nối");
                return;
            }

            try (Statement stmt = connection.createStatement()) {
                // Kiểm tra xem bảng products có tồn tại không
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'products'");
                if (!rs.next()) {
                    Log.d(TAG, "Bảng products không tồn tại, đang tạo bảng mới...");
                    createProductsTable();
                } else {
                    Log.d(TAG, "Bảng products đã tồn tại");
                    
                    // Kiểm tra cấu trúc bảng
                    rs = stmt.executeQuery("DESCRIBE products");
                    while (rs.next()) {
                        Log.d(TAG, "Cột: " + rs.getString("Field") + 
                              ", Kiểu: " + rs.getString("Type") + 
                              ", Null: " + rs.getString("Null") + 
                              ", Key: " + rs.getString("Key"));
                    }
                    
                    // Đếm số bản ghi
                    rs = stmt.executeQuery("SELECT COUNT(*) as count FROM products");
                    if (rs.next()) {
                        Log.d(TAG, "Số bản ghi trong bảng products: " + rs.getInt("count"));
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi kiểm tra bảng products: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Tạo bảng products nếu chưa tồn tại
     */
    private void createProductsTable() {
        synchronized (lock) {
            try (Statement stmt = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS products (" +
                           "id INT AUTO_INCREMENT PRIMARY KEY, " +
                           "name VARCHAR(255) NOT NULL, " +
                           "description TEXT, " +
                           "price DOUBLE NOT NULL, " +
                           "stock INT NOT NULL, " +
                           "category VARCHAR(100), " +
                           "image LONGBLOB, " +
                           "created_by INT, " +
                           "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                           "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                           ")";
                stmt.execute(sql);
                Log.d(TAG, "Đã tạo bảng products thành công");
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi tạo bảng products: " + e.getMessage(), e);
            }
        }
    }

    private int getOrCreateCart(int accountId) {
        synchronized (lock) {
            try {
                if (!checkConnection()) {
                    Log.e(TAG, "Không thể kết nối đến database");
                    return -1;
                }

                // Kiểm tra xem đã có cart chưa
                String checkSql = "SELECT id FROM carts WHERE account_id = ? AND status = 'active'";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, accountId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("id");
                        }
                    }
                }

                // Nếu chưa có, tạo cart mới
                String insertSql = "INSERT INTO carts (account_id, status) VALUES (?, 'active')";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, accountId);
                    insertStmt.executeUpdate();
                    try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Lỗi khi tạo/lấy cart: " + e.getMessage(), e);
            }
            return -1;
        }
    }

    public boolean addToCart(int accountId, CartItem cartItem) {
        try {
            // Kiểm tra và khôi phục kết nối nếu cần
            if (!checkConnection()) {
                System.out.println("Không thể kết nối đến database khi thêm vào giỏ hàng");
                return false;
            }

            // Lấy cart_id từ bảng carts
            int cartId = getOrCreateCart(accountId);
            if (cartId == -1) {
                System.out.println("Không thể tạo hoặc lấy cart_id");
                return false;
            }

            String sql = "INSERT INTO cart_items (cart_id, account_id, product_id, quantity, price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, cartId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, cartItem.getProduct().getId());
            stmt.setInt(4, cartItem.getQuantity());
            stmt.setDouble(5, cartItem.getPrice());
            
            System.out.println("Đang thêm sản phẩm vào giỏ hàng:");
            System.out.println("Cart ID: " + cartId);
            System.out.println("Account ID: " + accountId);
            System.out.println("Product ID: " + cartItem.getProduct().getId());
            System.out.println("Quantity: " + cartItem.getQuantity());
            System.out.println("Price: " + cartItem.getPrice());
            
            int result = stmt.executeUpdate();
            System.out.println("Kết quả thêm vào giỏ hàng: " + (result > 0 ? "Thành công" : "Thất bại"));
            return result > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi khi thêm vào giỏ hàng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<CartItem> getCartItems(int accountId) {
        List<CartItem> cartItems = new ArrayList<>();
        try {
            // Kiểm tra và khôi phục kết nối nếu cần
            if (!checkConnection()) {
                System.out.println("Không thể kết nối đến database khi lấy giỏ hàng");
                return cartItems;
            }

            // Lấy cart_id từ bảng carts
            int cartId = getOrCreateCart(accountId);
            if (cartId == -1) {
                System.out.println("Không thể tạo hoặc lấy cart_id");
                return cartItems;
            }

            String sql = "SELECT ci.*, p.* FROM cart_items ci JOIN products p ON ci.product_id = p.id WHERE ci.cart_id = ?";
            System.out.println("SQL Query: " + sql);
            System.out.println("Cart ID: " + cartId);
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, cartId);
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("Đang lấy giỏ hàng cho cart_id: " + cartId);
            int count = 0;
            while (rs.next()) {
                count++;
                Jewelry product = new Jewelry();
                product.setId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setImage(rs.getBytes("image"));
                product.setCategory(rs.getString("category"));

                CartItem cartItem = new CartItem();
                cartItem.setId(rs.getInt("id"));
                cartItem.setProduct(product);
                cartItem.setQuantity(rs.getInt("quantity"));
                cartItem.setPrice(rs.getDouble("price"));
                cartItems.add(cartItem);
                
                System.out.println("Đã thêm sản phẩm vào giỏ hàng: " + product.getName());
                System.out.println("Chi tiết sản phẩm:");
                System.out.println("- ID: " + product.getId());
                System.out.println("- Tên: " + product.getName());
                System.out.println("- Số lượng: " + cartItem.getQuantity());
                System.out.println("- Giá: " + cartItem.getPrice());
            }
            System.out.println("Tổng số sản phẩm trong giỏ hàng: " + count);
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy giỏ hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return cartItems;
    }

    public boolean removeFromCart(int cartItemId) {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            String sql = "DELETE FROM cart_items WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createOrder(int accountId, List<CartItem> cartItems) {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            connection.setAutoCommit(false);

            // Tạo đơn hàng
            String orderSql = "INSERT INTO orders (account_id, total_amount, status) VALUES (?, ?, 'pending')";
            PreparedStatement orderStmt = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            double totalAmount = calculateTotalAmount(cartItems);
            orderStmt.setInt(1, accountId);
            orderStmt.setDouble(2, totalAmount);
            orderStmt.executeUpdate();

            // Lấy ID của đơn hàng vừa tạo
            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // Thêm chi tiết đơn hàng
            String orderItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement orderItemStmt = connection.prepareStatement(orderItemSql);
            for (CartItem item : cartItems) {
                orderItemStmt.setInt(1, orderId);
                orderItemStmt.setInt(2, item.getProduct().getId());
                orderItemStmt.setInt(3, item.getQuantity());
                orderItemStmt.setDouble(4, item.getPrice());
                orderItemStmt.addBatch();
            }
            orderItemStmt.executeBatch();

            // Xóa giỏ hàng
            String deleteCartSql = "DELETE FROM cart_items WHERE account_id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteCartSql);
            deleteStmt.setInt(1, accountId);
            deleteStmt.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private double calculateTotalAmount(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // Lấy danh sách đơn hàng của tài khoản
    public List<Order> getOrders(int accountId) {
        List<Order> orders = new ArrayList<>();
        
        if (!checkConnection()) {
            Log.e(TAG, "Không thể kết nối đến database khi lấy danh sách đơn hàng");
            return orders;
        }

        try {
            Log.d(TAG, "Đang lấy danh sách đơn hàng cho account_id: " + accountId);
            String sql = "SELECT * FROM orders WHERE account_id = ? ORDER BY created_at DESC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setAccountId(rs.getInt("account_id")); 
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setCreatedAt(rs.getString("created_at"));
                order.setUpdatedAt(rs.getString("updated_at"));
                orders.add(order);
                
                Log.d(TAG, "Đã thêm đơn hàng: ID=" + order.getId() + 
                          ", TotalAmount=" + order.getTotalAmount() + 
                          ", Status=" + order.getStatus());
            }
            
            Log.d(TAG, "Tổng số đơn hàng đã lấy: " + orders.size());
            
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    // Lấy chi tiết đơn hàng
    public Order getOrderDetails(int orderId) {
        Order order = new Order();

        try {
            // Lấy thông tin đơn hàng
            String orderSql = "SELECT * FROM orders WHERE id = ?";
            PreparedStatement orderStmt = connection.prepareStatement(orderSql);
            orderStmt.setInt(1, orderId);
            ResultSet orderRs = orderStmt.executeQuery();

            if (!orderRs.next()) {
                return null;
            }

            order.setId(orderRs.getInt("id"));
            order.setAccountId(orderRs.getInt("account_id"));
            order.setTotalAmount(orderRs.getDouble("total_amount"));
            order.setStatus(orderRs.getString("status"));
            order.setCreatedAt(orderRs.getString("created_at"));
            order.setUpdatedAt(orderRs.getString("updated_at"));

            // Lấy chi tiết đơn hàng
            String orderItemsSql = "SELECT oi.*, p.* FROM order_items oi " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "WHERE oi.order_id = ?";
            PreparedStatement orderItemsStmt = connection.prepareStatement(orderItemsSql);
            orderItemsStmt.setInt(1, orderId);
            ResultSet orderItemsRs = orderItemsStmt.executeQuery();

            List<OrderItem> orderItems = new ArrayList<>();
            while (orderItemsRs.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(orderItemsRs.getInt("id"));
                orderItem.setOrderId(orderItemsRs.getInt("order_id"));
                orderItem.setQuantity(orderItemsRs.getInt("quantity"));
                orderItem.setPrice(orderItemsRs.getDouble("price"));
                orderItem.setCreatedAt(orderItemsRs.getString("created_at"));

                Jewelry product = new Jewelry();
                product.setId(orderItemsRs.getInt("product_id"));
                product.setName(orderItemsRs.getString("name"));
                product.setDescription(orderItemsRs.getString("description"));
                product.setPrice(orderItemsRs.getDouble("price"));
                product.setImage(orderItemsRs.getBytes("image"));
                product.setCategory(orderItemsRs.getString("category"));
                orderItem.setProduct(product);

                orderItems.add(orderItem);
            }
            order.setItems(orderItems);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    public void initialize() {
        new Thread(() -> {
            try {
                checkConnection();
                System.out.println("Đã kết nối đến database thành công");
            } catch (Exception e) {
                System.out.println("Lỗi khởi tạo database: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
} 