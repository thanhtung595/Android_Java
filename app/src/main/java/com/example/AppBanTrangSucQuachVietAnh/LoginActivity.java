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
 * Activity xử lý đăng nhập của ứng dụng
 * Cho phép người dùng đăng nhập vào hệ thống với tài khoản đã đăng ký
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editUsername;
    private EditText editPassword;
    private Button btnLogin;
    private DatabaseManager dbManager;
    private SharedPreferences prefs;

    /**
     * Khởi tạo Activity và các thành phần giao diện
     * @param savedInstanceState Trạng thái đã lưu của Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các thành phần
        initializeComponents();
        
        // Thiết lập sự kiện cho nút đăng nhập
        setupLoginButton();
    }

    /**
     * Khởi tạo các thành phần giao diện và đối tượng cần thiết
     */
    private void initializeComponents() {
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        dbManager = DatabaseManager.getInstance();
        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
    }

    /**
     * Thiết lập sự kiện cho nút đăng nhập
     */
    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị ProgressDialog trong quá trình đăng nhập
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang đăng nhập...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Thực hiện đăng nhập trong thread riêng
            new Thread(() -> {
                try {
                    Account account = dbManager.checkLogin(username, password);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (account != null) {
                            // Lưu thông tin đăng nhập
                            saveLoginInfo(account);
                            // Chuyển đến MainActivity
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }

    /**
     * Lưu thông tin đăng nhập vào SharedPreferences
     * @param account Đối tượng Account chứa thông tin người dùng
     */
    private void saveLoginInfo(Account account) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", account.getUsername());
        editor.putString("password", account.getPassword());
        editor.putString("user_role", account.getRole());
        editor.putInt("user_id", account.getId());
        editor.apply();
        Log.d(TAG, "Đã lưu thông tin đăng nhập cho user: " + account.getUsername() + ", ID: " + account.getId());
    }

    /**
     * Xử lý khi Activity bị hủy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
} 