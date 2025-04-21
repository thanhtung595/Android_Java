package com.example.appbanbanhnguyenhaidang.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.appbanbanhnguyenhaidang.model.Account;
import com.example.appbanbanhnguyenhaidang.model.CartItem;
import com.example.appbanbanhnguyenhaidang.model.Order;
import com.example.appbanbanhnguyenhaidang.model.OrderItem;
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

    public interface OnCartItemsLoadedListener {
        void onCartItemsLoaded(List<CartItem> items);
        void onError(String error);
    }

    public interface OnOrdersLoadedListener {
        void onOrdersLoaded(List<Order> orders);
        void onError(String error);
    }

    public interface OnOrderItemsLoadedListener {
        void onOrderItemsLoaded(List<OrderItem> items);
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
                    int userId = rs.getInt("id");
                    Log.d(TAG, "Login successful. Role: " + role + ", UserId: " + userId);
                    
                    // Lưu thông tin đăng nhập
                    SharedPreferences.Editor editor = context.getSharedPreferences("login_pref", Context.MODE_PRIVATE).edit();
                    editor.putString("username", username);
                    editor.putString("role", role);
                    editor.putInt("user_id", userId);
                    editor.apply();
                    
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
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    listener.onOperationSuccess("Xóa sản phẩm thành công");
                } else {
                    listener.onOperationFailed("Không tìm thấy sản phẩm để xóa");
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error deleting product", e);
                listener.onOperationFailed("Lỗi kết nối database: " + e.getMessage());
            }
        });
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public void searchProductsByName(String query, OnProductsResultListener listener) {
        executorService.execute(() -> {
            try {
                List<Product> products = new ArrayList<>();
                String searchQuery = "SELECT * FROM products WHERE name LIKE ?";
                
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(searchQuery)) {
                    
                    stmt.setString(1, "%" + query + "%");
                    ResultSet rs = stmt.executeQuery();
                    
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
                Log.e(TAG, "Error searching products", e);
                listener.onProductsError("Lỗi tìm kiếm sản phẩm: " + e.getMessage());
            }
        });
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public void addToCart(int productId, OnOperationResultListener listener) {
        executorService.execute(() -> {
            try {
                // Lấy user_id từ SharedPreferences
                SharedPreferences preferences = context.getSharedPreferences("login_pref", Context.MODE_PRIVATE);
                int userId = preferences.getInt("user_id", -1);
                
                if (userId == -1) {
                    throw new SQLException("Người dùng chưa đăng nhập");
                }

                // Kiểm tra xem giỏ hàng đã tồn tại chưa
                String checkCartQuery = "SELECT id FROM cart WHERE user_id = ?";
                PreparedStatement checkCartStmt = getConnection().prepareStatement(checkCartQuery);
                checkCartStmt.setInt(1, userId);
                ResultSet cartResult = checkCartStmt.executeQuery();

                int cartId;
                if (cartResult.next()) {
                    // Nếu giỏ hàng đã tồn tại, lấy cart_id
                    cartId = cartResult.getInt("id");
                } else {
                    // Nếu giỏ hàng chưa tồn tại, tạo mới
                    String createCartQuery = "INSERT INTO cart (user_id) VALUES (?)";
                    PreparedStatement createCartStmt = getConnection().prepareStatement(createCartQuery, Statement.RETURN_GENERATED_KEYS);
                    createCartStmt.setInt(1, userId);
                    createCartStmt.executeUpdate();
                    
                    ResultSet generatedKeys = createCartStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        cartId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Không thể tạo giỏ hàng mới");
                    }
                }

                // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
                String checkItemQuery = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
                PreparedStatement checkItemStmt = getConnection().prepareStatement(checkItemQuery);
                checkItemStmt.setInt(1, cartId);
                checkItemStmt.setInt(2, productId);
                ResultSet itemResult = checkItemStmt.executeQuery();

                if (itemResult.next()) {
                    // Nếu sản phẩm đã có trong giỏ hàng, cập nhật số lượng
                    int itemId = itemResult.getInt("id");
                    int currentQuantity = itemResult.getInt("quantity");
                    String updateQuery = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                    PreparedStatement updateStmt = getConnection().prepareStatement(updateQuery);
                    updateStmt.setInt(1, currentQuantity + 1);
                    updateStmt.setInt(2, itemId);
                    updateStmt.executeUpdate();
                } else {
                    // Nếu sản phẩm chưa có trong giỏ hàng, thêm mới
                    String insertQuery = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, 1)";
                    PreparedStatement insertStmt = getConnection().prepareStatement(insertQuery);
                    insertStmt.setInt(1, cartId);
                    insertStmt.setInt(2, productId);
                    insertStmt.executeUpdate();
                }

                listener.onOperationSuccess("Đã thêm vào giỏ hàng");
            } catch (SQLException e) {
                Log.e(TAG, "Error adding to cart", e);
                listener.onOperationFailed("Lỗi khi thêm vào giỏ hàng: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng
     */
    public void getCartItems(OnCartItemsLoadedListener listener) {
        executorService.execute(() -> {
            try (Connection conn = getConnection()) {
                String query = "SELECT ci.id as cart_item_id, ci.quantity, p.* " +
                             "FROM cart_items ci " +
                             "JOIN products p ON ci.product_id = p.id " +
                             "JOIN cart c ON ci.cart_id = c.id " +
                             "JOIN account a ON c.user_id = a.id " +
                             "WHERE a.username = ?";

                List<CartItem> items = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, getCurrentUsername());
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getBytes("image")
                        );
                        CartItem item = new CartItem(
                            rs.getInt("cart_item_id"),
                            product,
                            rs.getInt("quantity")
                        );
                        items.add(item);
                    }
                    listener.onCartItemsLoaded(items);
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error getting cart items", e);
                listener.onError("Lỗi lấy giỏ hàng: " + e.getMessage());
            }
        });
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    public void updateCartItemQuantity(int cartItemId, int quantity, OnOperationResultListener listener) {
        executorService.execute(() -> {
            try (Connection conn = getConnection()) {
                String query = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, cartItemId);
                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        listener.onOperationSuccess("Cập nhật số lượng thành công");
                    } else {
                        listener.onOperationFailed("Không tìm thấy sản phẩm trong giỏ hàng");
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error updating cart item quantity", e);
                listener.onOperationFailed("Lỗi cập nhật số lượng: " + e.getMessage());
            }
        });
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    public void removeFromCart(int cartItemId, OnOperationResultListener listener) {
        executorService.execute(() -> {
            try (Connection conn = getConnection()) {
                String query = "DELETE FROM cart_items WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, cartItemId);
                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        listener.onOperationSuccess("Đã xóa sản phẩm khỏi giỏ hàng");
                    } else {
                        listener.onOperationFailed("Không tìm thấy sản phẩm trong giỏ hàng");
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error removing from cart", e);
                listener.onOperationFailed("Lỗi xóa sản phẩm: " + e.getMessage());
            }
        });
    }

    /**
     * Tạo đơn hàng mới từ giỏ hàng
     */
    public void createOrder(OnOperationResultListener listener) {
        executorService.execute(() -> {
            try {
                // Lấy thông tin giỏ hàng
                String getCartQuery = "SELECT c.id as cart_id, a.id as user_id FROM cart c " +
                                    "JOIN account a ON c.user_id = a.id " +
                                    "WHERE a.username = ?";
                PreparedStatement getCartStmt = getConnection().prepareStatement(getCartQuery);
                getCartStmt.setString(1, getCurrentUsername());
                ResultSet cartRs = getCartStmt.executeQuery();

                if (!cartRs.next()) {
                    listener.onOperationFailed("Không tìm thấy giỏ hàng");
                    return;
                }

                int cartId = cartRs.getInt("cart_id");
                int userId = cartRs.getInt("user_id");

                // Lấy danh sách sản phẩm trong giỏ hàng
                String getCartItemsQuery = "SELECT ci.id as cart_item_id, ci.quantity, p.* " +
                                         "FROM cart_items ci " +
                                         "JOIN products p ON ci.product_id = p.id " +
                                         "WHERE ci.cart_id = ?";
                PreparedStatement getCartItemsStmt = getConnection().prepareStatement(getCartItemsQuery);
                getCartItemsStmt.setInt(1, cartId);
                ResultSet itemsRs = getCartItemsStmt.executeQuery();

                List<CartItem> cartItems = new ArrayList<>();
                double totalAmount = 0;

                while (itemsRs.next()) {
                    Product product = new Product(
                        itemsRs.getInt("id"),
                        itemsRs.getString("name"),
                        itemsRs.getString("description"),
                        itemsRs.getDouble("price"),
                        itemsRs.getBytes("image")
                    );
                    CartItem item = new CartItem(
                        itemsRs.getInt("cart_item_id"),
                        product,
                        itemsRs.getInt("quantity")
                    );
                    cartItems.add(item);
                    totalAmount += product.getPrice() * item.getQuantity();
                }

                if (cartItems.isEmpty()) {
                    listener.onOperationFailed("Giỏ hàng trống");
                    return;
                }

                // Tạo đơn hàng mới
                String createOrderQuery = "INSERT INTO orders (user_id, total_amount) VALUES (?, ?)";
                PreparedStatement createOrderStmt = getConnection().prepareStatement(createOrderQuery, Statement.RETURN_GENERATED_KEYS);
                createOrderStmt.setInt(1, userId);
                createOrderStmt.setDouble(2, totalAmount);
                createOrderStmt.executeUpdate();

                ResultSet generatedKeys = createOrderStmt.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new SQLException("Không thể tạo đơn hàng");
                }
                int orderId = generatedKeys.getInt(1);

                // Thêm chi tiết đơn hàng
                String insertOrderItemQuery = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
                PreparedStatement insertOrderItemStmt = getConnection().prepareStatement(insertOrderItemQuery);

                for (CartItem item : cartItems) {
                    if (item.getProduct() == null) {
                        throw new SQLException("Sản phẩm không tồn tại");
                    }
                    insertOrderItemStmt.setInt(1, orderId);
                    insertOrderItemStmt.setInt(2, item.getProduct().getId());
                    insertOrderItemStmt.setInt(3, item.getQuantity());
                    insertOrderItemStmt.setDouble(4, item.getProduct().getPrice());
                    insertOrderItemStmt.addBatch();
                }
                insertOrderItemStmt.executeBatch();

                // Xóa giỏ hàng
                String deleteCartItemsQuery = "DELETE FROM cart_items WHERE cart_id = ?";
                PreparedStatement deleteCartItemsStmt = getConnection().prepareStatement(deleteCartItemsQuery);
                deleteCartItemsStmt.setInt(1, cartId);
                deleteCartItemsStmt.executeUpdate();

                listener.onOperationSuccess("Đặt hàng thành công");
            } catch (SQLException e) {
                Log.e(TAG, "Error creating order", e);
                listener.onOperationFailed("Lỗi khi đặt hàng: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     */
    public void getOrders(OnOrdersLoadedListener listener) {
        executorService.execute(() -> {
            try {
                // Lấy user_id từ SharedPreferences
                SharedPreferences preferences = context.getSharedPreferences("login_pref", Context.MODE_PRIVATE);
                int userId = preferences.getInt("user_id", -1);
                
                if (userId == -1) {
                    throw new SQLException("Người dùng chưa đăng nhập");
                }

                String query = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
                PreparedStatement stmt = getConnection().prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                    );
                    orders.add(order);
                }
                
                // Chuyển callback về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onOrdersLoaded(orders);
                });
            } catch (SQLException e) {
                Log.e(TAG, "Error getting orders", e);
                // Chuyển callback về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onError("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    public void getOrderItems(int orderId, OnOrderItemsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                String query = "SELECT oi.*, p.* FROM order_items oi " +
                             "JOIN products p ON oi.product_id = p.id " +
                             "WHERE oi.order_id = ?";
                PreparedStatement stmt = getConnection().prepareStatement(query);
                stmt.setInt(1, orderId);
                ResultSet rs = stmt.executeQuery();

                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getBytes("image")
                    );
                    OrderItem item = new OrderItem(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        product,
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    );
                    items.add(item);
                }
                
                // Chuyển callback về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onOrderItemsLoaded(items);
                });
            } catch (SQLException e) {
                Log.e(TAG, "Error getting order items", e);
                // Chuyển callback về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onError("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage());
                });
            }
        });
    }

    private String getCurrentUsername() {
        SharedPreferences preferences = context.getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        return preferences.getString("username", "");
    }
} 