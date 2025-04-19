package com.example.AppBanTrangSucQuachVietAnh;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
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

/**
 * Activity hiển thị và quản lý giỏ hàng của người dùng
 * Cho phép xem, cập nhật số lượng và thanh toán các sản phẩm trong giỏ hàng
 */
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

    /**
     * Khởi tạo Activity và các thành phần giao diện
     * @param savedInstanceState Trạng thái đã lưu của Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Thiết lập toolbar với nút back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Giỏ hàng");

        // Khởi tạo các thành phần
        initializeComponents();
        
        // Lấy thông tin user từ SharedPreferences
        loadUserInfo();
        
        // Thiết lập RecyclerView và Adapter
        setupRecyclerView();
        
        // Tải danh sách sản phẩm trong giỏ hàng
        loadCartItems();
    }

    /**
     * Khởi tạo các thành phần giao diện và đối tượng cần thiết
     */
    private void initializeComponents() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        textTotalAmount = findViewById(R.id.textTotalAmount);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        databaseManager = DatabaseManager.getInstance();
        cartItems = new ArrayList<>();
    }

    /**
     * Lấy thông tin user từ SharedPreferences
     */
    private void loadUserInfo() {
        userId = getSharedPreferences("login_prefs", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Thiết lập RecyclerView và Adapter
     */
    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Tải danh sách sản phẩm trong giỏ hàng từ database
     */
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

    /**
     * Cập nhật tổng tiền của giỏ hàng
     */
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

    /**
     * Xử lý sự kiện khi nhấn nút thanh toán
     * @param view View được nhấn
     */
    public void onCheckoutClick(View view) {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thanh toán");
        builder.setMessage("Bạn có chắc chắn muốn thanh toán đơn hàng này?");
        
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            checkout();
        });
        
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Thực hiện thanh toán đơn hàng
     */
    private void checkout() {
        new Thread(() -> {
            boolean success = databaseManager.createOrder(userId, cartItems);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Cập nhật số lượng thất bại", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 