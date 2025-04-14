package com.example.AppBanTrangSucQuachVietAnh.data;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Lớp hỗ trợ quản lý cấu trúc và phiên bản cơ sở dữ liệu
 * Chứa các câu lệnh tạo bảng và nâng cấp schema
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "jewelry_sales_db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    /**
     * Câu lệnh tạo bảng accounts
     */
    public static final String CREATE_TABLE_ACCOUNTS =
        "CREATE TABLE IF NOT EXISTS accounts (" +
        "id INT PRIMARY KEY AUTO_INCREMENT, " +
        "username VARCHAR(50) UNIQUE NOT NULL, " +
        "password VARCHAR(100) NOT NULL, " +
        "role VARCHAR(20) NOT NULL DEFAULT 'user', " +
        "full_name VARCHAR(100), " +
        "email VARCHAR(100), " +
        "phone VARCHAR(20), " +
        "address TEXT, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
        ")";

    /**
     * Câu lệnh tạo bảng jewelry
     */
    public static final String CREATE_TABLE_JEWELRY = 
        "CREATE TABLE IF NOT EXISTS jewelry (" +
        "id INT PRIMARY KEY AUTO_INCREMENT, " +
        "name VARCHAR(100) NOT NULL, " +
        "description TEXT, " +
        "price DECIMAL(10,2) NOT NULL, " +
        "stock INT NOT NULL DEFAULT 0, " +
        "category VARCHAR(50), " +
        "image MEDIUMBLOB, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
        ")";

    /**
     * Câu lệnh tạo tài khoản admin mặc định
     */
    public static final String INSERT_DEFAULT_ADMIN = 
        "INSERT INTO accounts (username, password, role, full_name) " +
        "SELECT 'admin', 'admin123', 'admin', 'Administrator' " +
        "WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE username = 'admin')";

    /**
     * Câu lệnh tạo chỉ mục cho các trường thường xuyên tìm kiếm
     */
    public static final String[] CREATE_INDEXES = {
        "CREATE INDEX IF NOT EXISTS idx_jewelry_category ON jewelry(category)",
        "CREATE INDEX IF NOT EXISTS idx_jewelry_name ON jewelry(name)",
        "CREATE INDEX IF NOT EXISTS idx_accounts_username ON accounts(username)",
        "CREATE INDEX IF NOT EXISTS idx_accounts_role ON accounts(role)"
    };

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Tạo bảng accounts
            db.execSQL(CREATE_TABLE_ACCOUNTS);

            // Tạo bảng products
            db.execSQL("CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "price REAL NOT NULL," +
                "stock INTEGER NOT NULL DEFAULT 0," +
                "category TEXT," +
                "image BLOB," +
                "created_by INTEGER," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (created_by) REFERENCES accounts(id)" +
            ")");

            // Thêm tài khoản admin mặc định
            ContentValues values = new ContentValues();
            values.put("username", "admin");
            values.put("password", "admin");
            values.put("full_name", "Administrator");
            values.put("role", "admin");
            db.insert("accounts", null, values);

            // Thêm tài khoản user mặc định
            values.clear();
            values.put("username", "user");
            values.put("password", "user");
            values.put("full_name", "Regular User");
            values.put("role", "user");
            db.insert("accounts", null, values);

            // Tạo các chỉ mục
            for (String createIndex : CREATE_INDEXES) {
                db.execSQL(createIndex);
            }

            Log.d(TAG, "Created database tables and added default users");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Không cần nâng cấp database
        Log.d(TAG, "Database upgrade not needed");
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        try {
            // Kiểm tra xem database có tồn tại không
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                Log.e(TAG, "Database file does not exist: " + dbFile.getAbsolutePath());
                return null;
            }
            return super.getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Error getting writable database", e);
            return null;
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        try {
            // Kiểm tra xem database có tồn tại không
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                Log.e(TAG, "Database file does not exist: " + dbFile.getAbsolutePath());
                return null;
            }
            return super.getReadableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Error getting readable database", e);
            return null;
        }
    }

    /**
     * Khởi tạo cấu trúc cơ sở dữ liệu
     * @param connection Kết nối đến cơ sở dữ liệu
     */
    public static void initializeDatabase(Connection connection) {
        try {
            Log.d(TAG, "Bắt đầu khởi tạo cơ sở dữ liệu...");
            
            // Tạo các bảng
            executeUpdate(connection, CREATE_TABLE_ACCOUNTS);
            executeUpdate(connection, CREATE_TABLE_JEWELRY);
            
            // Tạo tài khoản admin mặc định
            executeUpdate(connection, INSERT_DEFAULT_ADMIN);
            
            // Tạo các chỉ mục
            for (String createIndex : CREATE_INDEXES) {
                executeUpdate(connection, createIndex);
            }
            
            Log.d(TAG, "Khởi tạo cơ sở dữ liệu thành công");
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage(), e);
            throw new RuntimeException("Không thể khởi tạo cơ sở dữ liệu", e);
        }
    }

    /**
     * Thực thi câu lệnh SQL cập nhật
     * @param connection Kết nối đến cơ sở dữ liệu
     * @param sql Câu lệnh SQL cần thực thi
     */
    private static void executeUpdate(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
} 