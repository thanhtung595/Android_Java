package com.example.AppBanTrangSucQuachVietAnh;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.adapter.CartItemAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.util.Log;

public class CartActivity extends AppCompatActivity implements CartItemAdapter.OnCartItemClickListener {
    private static final String TAG = "CartActivity";
    private RecyclerView recyclerView;
    private CartItemAdapter adapter;
    private List<CartItem> cartItems;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Giỏ hàng");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
        databaseManager = DatabaseManager.getInstance();

        // Setup RecyclerView
        cartItems = new ArrayList<>();
        adapter = new CartItemAdapter(this, cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load cart items
        loadCartItems();

        // Setup checkout button
        btnCheckout.setOnClickListener(v -> {
            // TODO: Implement checkout process
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadCartItems() {
        new Thread(() -> {
            try {
                // Lấy accountId từ SharedPreferences
                SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                int accountId = prefs.getInt("account_id", -1);

                if (accountId != -1) {
                    // Lấy danh sách sản phẩm từ database
                    List<CartItem> items = databaseManager.getCartItems(accountId);
                    runOnUiThread(() -> {
                        cartItems.clear();
                        cartItems.addAll(items);
                        adapter.notifyDataSetChanged();
                        updateTotalPrice();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading cart items", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format("$%.2f", total));
    }

    @Override
    public void onQuantityChange(CartItem cartItem, int newQuantity) {
        // TODO: Update quantity in database or API
        cartItem.setQuantity(newQuantity);
        adapter.updateCartItems(cartItems);
        updateTotalPrice();
    }

    @Override
    public void onRemoveItem(CartItem cartItem) {
        Log.d(TAG, "Bắt đầu xóa cart item với ID: " + cartItem.getId());
        new Thread(() -> {
            boolean success = databaseManager.removeCartItem(cartItem.getId());
            runOnUiThread(() -> {
                if (success) {
                    Log.d(TAG, "Xóa cart item thành công");
                    cartItems.remove(cartItem);
                    adapter.updateCartItems(cartItems);
                    updateTotalPrice();
                    Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Xóa cart item thất bại");
                    Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
} 