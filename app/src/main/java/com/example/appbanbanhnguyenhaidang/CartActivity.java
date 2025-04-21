package com.example.appbanbanhnguyenhaidang;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanbanhnguyenhaidang.adapter.CartAdapter;
import com.example.appbanbanhnguyenhaidang.database.DatabaseHelper;
import com.example.appbanbanhnguyenhaidang.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private RecyclerView rvCartItems;
    private TextView tvEmptyCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Giỏ hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo views
        rvCartItems = findViewById(R.id.rvCartItems);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        // Khởi tạo database helper
        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo RecyclerView
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        // Load giỏ hàng
        loadCartItems();

        // Xử lý sự kiện thanh toán
        btnCheckout.setOnClickListener(v -> {
            // TODO: Implement checkout functionality
            Toast.makeText(this, "Tính năng thanh toán sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCartItems() {
        databaseHelper.getCartItems(new DatabaseHelper.OnCartItemsLoadedListener() {
            @Override
            public void onCartItemsLoaded(List<CartItem> items) {
                runOnUiThread(() -> {
                    cartItems.clear();
                    cartItems.addAll(items);
                    cartAdapter.notifyDataSetChanged();
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            btnCheckout.setEnabled(false);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
        }

        // Tính tổng tiền
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        tvTotal.setText(String.format("Tổng tiền: %,.0f VNĐ", total));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQuantityChanged(CartItem item, int quantity) {
        databaseHelper.updateCartItemQuantity(item.getId(), quantity, new DatabaseHelper.OnOperationResultListener() {
            @Override
            public void onOperationSuccess(String message) {
                runOnUiThread(() -> {
                    item.setQuantity(quantity);
                    cartAdapter.notifyDataSetChanged();
                    updateUI();
                });
            }

            @Override
            public void onOperationFailed(String error) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onRemoveItem(CartItem item) {
        databaseHelper.removeFromCart(item.getId(), new DatabaseHelper.OnOperationResultListener() {
            @Override
            public void onOperationSuccess(String message) {
                runOnUiThread(() -> {
                    cartItems.remove(item);
                    cartAdapter.notifyDataSetChanged();
                    updateUI();
                    Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onOperationFailed(String error) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }
} 