package com.example.AppBanTrangSucQuachVietAnh;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.adapter.CartAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private static final String TAG = "CartActivity";
    
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private LinearLayout emptyView;
    private TextView textTotalAmount;
    private Button buttonCheckout;
    private DatabaseManager databaseManager;
    private List<CartItem> cartItems;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

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
        loadCartItems();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        textTotalAmount = findViewById(R.id.textTotalAmount);
        buttonCheckout = findViewById(R.id.buttonCheckout);

        buttonCheckout.setOnClickListener(v -> checkout());
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
        cartItems = new ArrayList<>();
        adapter = new CartAdapter(cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCartItems() {
        new Thread(() -> {
            List<CartItem> items = databaseManager.getCartItems(userId);
            runOnUiThread(() -> {
                cartItems.clear();
                cartItems.addAll(items);
                adapter.updateData(cartItems);
                updateTotalAmount();
                updateEmptyView();
            });
        }).start();
    }

    private void updateTotalAmount() {
        double total = cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textTotalAmount.setText(currencyFormat.format(total));
    }

    private void updateEmptyView() {
        if (cartItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            buttonCheckout.setEnabled(false);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            buttonCheckout.setEnabled(true);
        }
    }

    private void checkout() {
        new Thread(() -> {
            boolean success = databaseManager.createOrder(userId, cartItems);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi khi đặt hàng", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        new Thread(() -> {
            boolean success = databaseManager.updateCartItemQuantity(item.getId(), newQuantity);
            runOnUiThread(() -> {
                if (success) {
                    item.setQuantity(newQuantity);
                    adapter.updateData(cartItems);
                    updateTotalAmount();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onDeleteClick(CartItem item) {
        new Thread(() -> {
            boolean success = databaseManager.removeFromCart(item.getId());
            runOnUiThread(() -> {
                if (success) {
                    cartItems.remove(item);
                    adapter.updateData(cartItems);
                    updateTotalAmount();
                    updateEmptyView();
                } else {
                    Toast.makeText(this, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
} 