package com.example.AppBanTrangSucQuachVietAnh;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.adapter.OrderAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    private static final String TAG = "OrderHistoryActivity";
    
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private LinearLayout emptyView;
    private DatabaseManager databaseManager;
    private List<Order> orders;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Khởi tạo DatabaseManager
        databaseManager = DatabaseManager.getInstance();

        // Lấy user ID từ Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrders();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        orders = new ArrayList<>();
        adapter = new OrderAdapter(orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        new Thread(() -> {
            List<Order> userOrders = databaseManager.getUserOrders(userId);
            runOnUiThread(() -> {
                orders.clear();
                orders.addAll(userOrders);
                adapter.notifyDataSetChanged();
                updateEmptyView();
            });
        }).start();
    }

    private void updateEmptyView() {
        if (orders.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
} 