package com.example.AppBanTrangSucQuachVietAnh;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.AppBanTrangSucQuachVietAnh.adapter.OrderHistoryAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

/**
 * Activity hiển thị lịch sử đơn hàng của người dùng
 * Cho phép xem chi tiết các đơn hàng đã mua
 */
public class OrderHistoryActivity extends AppCompatActivity {
    private static final String TAG = "OrderHistoryActivity";
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private DatabaseManager databaseManager;
    private int userId;

    /**
     * Khởi tạo Activity và các thành phần giao diện
     * @param savedInstanceState Trạng thái đã lưu của Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Khởi tạo các thành phần
        initializeComponents();
        
        // Lấy thông tin user từ SharedPreferences
        loadUserInfo();
        
        // Thiết lập RecyclerView và Adapter
        setupRecyclerView();
        
        // Tải danh sách đơn hàng
        loadOrders();
    }

    /**
     * Khởi tạo các thành phần giao diện và đối tượng cần thiết
     */
    private void initializeComponents() {
        recyclerView = findViewById(R.id.recyclerView);
        databaseManager = DatabaseManager.getInstance();
    }

    /**
     * Lấy thông tin user từ SharedPreferences
     */
    private void loadUserInfo() {
        userId = getSharedPreferences("login_prefs", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {
            Log.e(TAG, "Không tìm thấy user ID");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    /**
     * Thiết lập RecyclerView và Adapter
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Tải danh sách đơn hàng
     */
    private void loadOrders() {
        new Thread(() -> {
            try {
                // Kiểm tra kết nối
                if (!databaseManager.checkConnection()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi: Không thể kết nối cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                List<Order> orders = databaseManager.getUserOrders(userId);
                runOnUiThread(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        adapter.setOrders(orders);
                    } else {
                        TextView textEmpty = findViewById(R.id.textEmpty);
                        textEmpty.setText("Bạn chưa có đơn hàng nào");
                        textEmpty.setVisibility(TextView.VISIBLE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi tải lịch sử mua hàng: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Xử lý khi Activity bị hủy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
} 