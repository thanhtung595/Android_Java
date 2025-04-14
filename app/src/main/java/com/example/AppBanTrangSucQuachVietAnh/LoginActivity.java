package com.example.AppBanTrangSucQuachVietAnh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Account;

/**
 * Activity xử lý đăng nhập người dùng
 * Kiểm tra thông tin đăng nhập và phân quyền người dùng (admin/user)
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editUsername;
    private EditText editPassword;
    private Button buttonLogin;
    private DatabaseManager databaseManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "Khởi tạo LoginActivity");

        // Khởi tạo DatabaseManager
        databaseManager = DatabaseManager.getInstance();

        // Khởi tạo các view
        initViews();
        Log.d(TAG, "Đã khởi tạo các view thành công");
    }

    /**
     * Khởi tạo các thành phần giao diện
     */
    private void initViews() {
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);

        buttonLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            handleLogin(username, password);
        });
    }

    /**
     * Xử lý quá trình đăng nhập
     * Kiểm tra thông tin đăng nhập và phân quyền
     */
    private void handleLogin(String username, String password) {
        showProgressDialog();
        new Thread(() -> {
            try {
                Account account = databaseManager.checkLogin(username, password);
                runOnUiThread(() -> {
                    hideProgressDialog();
                    if (account != null) {
                        // Lưu thông tin đăng nhập
                        saveLoginInfo(username, password, account.getRole());
                        startMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideProgressDialog();
                    Toast.makeText(LoginActivity.this, 
                        "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Lưu thông tin đăng nhập vào SharedPreferences
     */
    private void saveLoginInfo(String username, String password, String role) {
        SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("user_role", role);
        editor.apply();
        Log.d(TAG, "Đã lưu thông tin đăng nhập: " + username + ", quyền: " + role);
    }

    /**
     * Chuyển đến MainActivity
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị dialog tiến trình
     */
    private void showProgressDialog() {
        progressDialog.show();
    }

    /**
     * Ẩn dialog tiến trình
     */
    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
} 