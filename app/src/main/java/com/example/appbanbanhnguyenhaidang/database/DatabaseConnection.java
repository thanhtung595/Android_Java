package com.example.appbanbanhnguyenhaidang.database;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection";
    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/bakery_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC Driver not found.", e);
            throw new SQLException("MySQL JDBC Driver not found.");
        } catch (SQLException e) {
            Log.e(TAG, "Connection failed! Check output console", e);
            throw e;
        }
    }
} 