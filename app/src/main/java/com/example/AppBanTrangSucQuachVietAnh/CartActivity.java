package com.example.AppBanTrangSucQuachVietAnh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartItemAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private CartItemAdapter adapter;
    private List<CartItem> cartItems;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private ProgressBar progressBar;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo các view
        initViews();

        // Khởi tạo RecyclerView
        setupRecyclerView();

        // Load dữ liệu giỏ hàng
        loadCartItems();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
        databaseManager = DatabaseManager.getInstance();

        // Thiết lập sự kiện cho nút thanh toán
        btnCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                showCheckoutConfirmation();
            } else {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        adapter = new CartItemAdapter(this, cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private int getAccountId() {
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        return prefs.getInt("account_id", 1); // Trả về 1 nếu không tìm thấy account_id
    }

    private void loadCartItems() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        new Thread(() -> {
            List<CartItem> loadedItems = databaseManager.getCartItems(getAccountId());
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                // Thêm log để kiểm tra
                if (loadedItems != null) {
                    System.out.println("Số lượng sản phẩm trong giỏ hàng: " + loadedItems.size());
                    for (CartItem item : loadedItems) {
                        System.out.println("Sản phẩm: " + item.getProduct().getName() + 
                                         ", Số lượng: " + item.getQuantity() + 
                                         ", Giá: " + item.getPrice());
                    }
                    
                    // Cập nhật dữ liệu cho adapter
                    cartItems.clear();
                    cartItems.addAll(loadedItems);
                    adapter.notifyDataSetChanged();
                    updateTotalAmount();
                }
            });
        }).start();
    }

    private void updateTotalAmount() {
        if (tvTotalPrice == null || cartItems == null) return;
        
        double total = 0;
        for (CartItem item : cartItems) {
            if (item != null) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(format.format(total));
        
        // Log để kiểm tra
        System.out.println("Tổng số sản phẩm: " + cartItems.size());
        System.out.println("Tổng tiền: " + total);
    }

    private void showCheckoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc chắn muốn thanh toán đơn hàng này?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    processCheckout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void processCheckout() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnCheckout != null) {
            btnCheckout.setEnabled(false);
        }

        new Thread(() -> {
            boolean success = databaseManager.createOrder(getAccountId(), cartItems);
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (btnCheckout != null) {
                    btnCheckout.setEnabled(true);
                }

                if (success) {
                    Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Thanh toán thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onItemClick(CartItem item) {
        // Xử lý khi click vào item
    }

    @Override
    public void onRemoveClick(CartItem item) {
        new Thread(() -> {
            boolean success = databaseManager.removeFromCart(item.getId());
            runOnUiThread(() -> {
                if (success) {
                    cartItems.remove(item);
                    adapter.notifyDataSetChanged();
                    updateTotalAmount();
                    Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 