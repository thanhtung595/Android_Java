package com.example.appbanbanhnguyenhaidang;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbanbanhnguyenhaidang.adapter.OrderItemAdapter;
import com.example.appbanbanhnguyenhaidang.database.DatabaseHelper;
import com.example.appbanbanhnguyenhaidang.model.OrderItem;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {
    private static final String EXTRA_ORDER_ID = "order_id";

    private RecyclerView rvOrderItems;
    private OrderItemAdapter adapter;
    private List<OrderItem> items;
    private DatabaseHelper databaseHelper;

    public static Intent newIntent(Context context, int orderId) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Lấy order_id từ intent
        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            finish();
            return;
        }

        // Khởi tạo toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết đơn hàng #" + orderId);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo database helper
        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo RecyclerView
        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        items = new ArrayList<>();
        adapter = new OrderItemAdapter(items);
        rvOrderItems.setAdapter(adapter);

        // Tải chi tiết đơn hàng
        loadOrderItems(orderId);
    }

    private void loadOrderItems(int orderId) {
        databaseHelper.getOrderItems(orderId, new DatabaseHelper.OnOrderItemsLoadedListener() {
            @Override
            public void onOrderItemsLoaded(List<OrderItem> loadedItems) {
                items.clear();
                items.addAll(loadedItems);
                adapter.updateItems(items);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(OrderDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 