package com.example.appbanbanhnguyenhaidang;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbanbanhnguyenhaidang.adapter.OrderAdapter;
import com.example.appbanbanhnguyenhaidang.database.DatabaseHelper;
import com.example.appbanbanhnguyenhaidang.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {
    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<Order> orders;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Khởi tạo toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đơn hàng của tôi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo database helper
        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo RecyclerView
        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orders = new ArrayList<>();
        adapter = new OrderAdapter(orders, this);
        rvOrders.setAdapter(adapter);

        // Tải danh sách đơn hàng
        loadOrders();
    }

    private void loadOrders() {
        databaseHelper.getOrders(new DatabaseHelper.OnOrdersLoadedListener() {
            @Override
            public void onOrdersLoaded(List<Order> loadedOrders) {
                orders.clear();
                orders.addAll(loadedOrders);
                adapter.updateOrders(orders);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(OrdersActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        // Chuyển đến màn hình chi tiết đơn hàng
        startActivity(OrderDetailActivity.newIntent(this, order.getId()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 