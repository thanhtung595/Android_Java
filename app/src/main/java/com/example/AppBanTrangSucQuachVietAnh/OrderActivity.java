package com.example.AppBanTrangSucQuachVietAnh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.adapter.OrderAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private OrderAdapter orderAdapter;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvOrders = findViewById(R.id.rvOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        databaseManager = DatabaseManager.getInstance();

        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, new ArrayList<>(), this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            int accountId = prefs.getInt("account_id", -1);

            if (accountId != -1) {
                if (databaseManager.checkConnection()) {
                    List<Order> orders = databaseManager.getOrders(accountId);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (orders != null && orders.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else if (orders != null) {
                            orderAdapter.updateOrders(orders);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setText("Không thể kết nối đến cơ sở dữ liệu");
                        tvEmpty.setVisibility(View.VISIBLE);
                    });
                }
            }
        }).start();
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 