package com.example.appbanbanhnguyenhaidang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appbanbanhnguyenhaidang.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

/**
 * LoginActivity - Xử lý đăng nhập và phân quyền người dùng
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        databaseHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.msg_enter_credentials, Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Attempting login with username: " + username);
            databaseHelper.checkLogin(username, password, new DatabaseHelper.LoginCallback() {
                @Override
                public void onSuccess(String role) {
                    Log.d(TAG, "Login successful with role: " + role);
                    runOnUiThread(() -> {
                        startMainActivity(role);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Login error: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void startMainActivity(String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }
} 