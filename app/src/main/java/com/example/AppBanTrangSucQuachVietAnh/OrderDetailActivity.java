package com.example.AppBanTrangSucQuachVietAnh;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.adapter.OrderProductAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId;
    private TextView tvOrderDate;
    private TextView tvOrderStatus;
    private TextView tvTotalAmount;
    private RecyclerView rvOrderItems;
    private ProgressBar progressBar;
    private OrderProductAdapter orderProductAdapter;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        progressBar = findViewById(R.id.progressBar);

        databaseManager = DatabaseManager.getInstance();

        setupRecyclerView();
        loadOrderDetails();
    }

    private void setupRecyclerView() {
        orderProductAdapter = new OrderProductAdapter(this, null);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderProductAdapter);
    }

    private void loadOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);

        int orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            finish();
            return;
        }

        new Thread(() -> {
            Order order = databaseManager.getOrderDetails(orderId);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (order != null) {
                    displayOrderDetails(order);
                }
            });
        }).start();
    }

    private void displayOrderDetails(Order order) {
        tvOrderId.setText(String.format("Đơn hàng #%d", order.getId()));
        
        // Hiển thị trực tiếp chuỗi ngày tháng từ database
        tvOrderDate.setText(order.getCreatedAt());
        
        tvOrderStatus.setText(order.getStatus());
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(format.format(order.getTotalAmount()));

        orderProductAdapter.updateOrderItems(order.getItems());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 