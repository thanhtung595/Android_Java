package com.example.AppBanTrangSucQuachVietAnh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.adapter.JewelryAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.Locale;

/**
 * Activity chính của ứng dụng hiển thị danh sách sản phẩm trang sức.
 * Xử lý hiển thị sản phẩm, tìm kiếm, lọc và các chức năng dành cho admin.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private JewelryAdapter adapter;
    private List<Jewelry> jewelryList;
    private DatabaseManager databaseManager;
    private TextView textEmpty;
    private SharedPreferences preferences;
    private int userId;
    private String userRole;
    private boolean isAdmin = false;
    private ProgressDialog progressDialog;
    private View progressBar;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Thiết lập ngôn ngữ tiếng Việt
        Locale locale = new Locale("vi");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate started");

        // Khởi tạo đối tượng quản lý cơ sở dữ liệu
        databaseManager = DatabaseManager.getInstance();

        // Kiểm tra đăng nhập
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        String role = prefs.getString("user_role", null);

        if (username == null || password == null || role == null) {
            Log.d(TAG, "Chưa đăng nhập, chuyển đến LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Kiểm tra quyền admin
        boolean isAdmin = "admin".equals(role);
        Log.d(TAG, "Quyền người dùng: " + role + ", là admin: " + isAdmin);

        // Đã đăng nhập, tiếp tục khởi tạo MainActivity
        initToolbar();
        initRecyclerView(isAdmin);
        initFAB(isAdmin);
        initEmptyView();

        // Tải danh sách sản phẩm trong thread riêng
        loadJewelryData();
    }

    /**
     * Khởi tạo thanh công cụ với các tùy chọn tìm kiếm và lọc
     */
    private void initToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Log.d(TAG, "Đã khởi tạo thanh công cụ");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo thanh công cụ: " + e.getMessage(), e);
        }
    }

    /**
     * Khởi tạo RecyclerView để hiển thị danh sách sản phẩm
     * @param isAdmin boolean để kiểm soát các tính năng dành cho admin
     */
    private void initRecyclerView(boolean isAdmin) {
        try {
            Log.d(TAG, "Khởi tạo RecyclerView");
            recyclerView = findViewById(R.id.recyclerView);
            
            // Thiết lập LayoutManager với 3 cột
            GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
            recyclerView.setLayoutManager(layoutManager);
            
            // Khởi tạo adapter với xử lý click
            adapter = new JewelryAdapter(
                this, 
                isAdmin,
                // Xử lý sự kiện xóa
                jewelry -> {
                    if (isAdmin) {
                        new AlertDialog.Builder(this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                            .setPositiveButton("Xóa", (dialog, which) -> deleteJewelry(jewelry))
                            .setNegativeButton("Hủy", null)
                            .show();
                    } else {
                        // Thêm vào giỏ hàng cho user
                        addToCart(jewelry);
                    }
                },
                // Xử lý sự kiện click vào item
                jewelry -> {
                    Log.d(TAG, "Click vào sản phẩm: " + jewelry.getId());
                    if (isAdmin) {
                        Intent intent = new Intent(this, EditJewelryActivity.class);
                        intent.putExtra("jewelry_id", jewelry.getId());
                        startActivity(intent);
                    } else {
                        // Thêm vào giỏ hàng cho user
                        addToCart(jewelry);
                    }
                }
            );
            
            // Thiết lập adapter cho RecyclerView
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "Đã khởi tạo RecyclerView và Adapter");
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo RecyclerView: " + e.getMessage(), e);
        }
    }

    /**
     * Khởi tạo nút thêm mới (FAB)
     * Chỉ hiển thị với người dùng admin
     * @param isAdmin boolean để kiểm soát hiển thị FAB
     */
    private void initFAB(boolean isAdmin) {
        try {
            Log.d(TAG, "Đang khởi tạo FAB, là admin: " + isAdmin);
            FloatingActionButton fab = findViewById(R.id.fabAdd);
            
            // Chỉ hiển thị FAB nếu là admin
            if (isAdmin) {
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(v -> {
                    Log.d(TAG, "Đã nhấn FAB");
                    Intent intent = new Intent(MainActivity.this, AddJewelryActivity.class);
                    startActivity(intent);
                });
                Log.d(TAG, "Đã khởi tạo và hiển thị FAB cho admin");
            } else {
                fab.setVisibility(View.GONE);
                Log.d(TAG, "Đã ẩn FAB cho người dùng không phải admin");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo FAB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo view hiển thị khi không có sản phẩm
     */
    private void initEmptyView() {
        try {
            textEmpty = findViewById(R.id.textEmpty);
            progressBar = findViewById(R.id.progressBar);
            emptyView = findViewById(R.id.emptyView);
            Log.d(TAG, "Đã khởi tạo view trống");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo view trống: " + e.getMessage(), e);
        }
    }

    /**
     * Tải danh sách sản phẩm từ cơ sở dữ liệu
     * Cập nhật giao diện với danh sách sản phẩm hoặc thông báo trống
     */
    private void loadJewelryData() {
        runOnUiThread(() -> showLoading());

        new Thread(() -> {
            try {
                // Lấy danh sách sản phẩm
                List<Jewelry> jewelryList = databaseManager.getAllJewelry();

                // Cập nhật UI trên main thread
                runOnUiThread(() -> {
                    hideLoading();
                    if (jewelryList != null && !jewelryList.isEmpty()) {
                        adapter.setJewelryList(jewelryList);
                        hideEmptyView();
                    } else {
                        showEmptyView();
                    }
                    databaseManager.close();
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi tải danh sách sản phẩm: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    hideLoading();
                    showError("Không thể tải danh sách sản phẩm");
                    showEmptyView();
                });
            }
        }).start();
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume bắt đầu");
        // Tải lại dữ liệu khi activity được khôi phục
        new Thread(this::loadJewelryData).start();
    }

    /**
     * Xử lý các lựa chọn menu (tìm kiếm, lọc, đăng xuất)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Xóa thông tin đăng nhập
            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
            editor.remove("username");
            editor.remove("user_role");
            editor.apply();
            
            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            showSearchDialog();
            return true;
        } else if (item.getItemId() == R.id.action_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Tạo menu tùy chọn với các chức năng tìm kiếm, lọc và đăng xuất
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Xóa sản phẩm khỏi cơ sở dữ liệu
     */
    private void deleteJewelry(Jewelry jewelry) {
        Log.d(TAG, "Bắt đầu xóa sản phẩm: " + jewelry.getId());
        new Thread(() -> {
            try {
                if (databaseManager.deleteJewelry(jewelry.getId())) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        // Đóng kết nối hiện tại
                        databaseManager.close();
                        // Tạo kết nối mới
                        databaseManager = DatabaseManager.getInstance();
                        // Tải lại dữ liệu
                        loadJewelryData();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xóa sản phẩm: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tìm kiếm sản phẩm");

        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Tạo EditText để nhập tên sản phẩm
        EditText input = new EditText(this);
        input.setHint("Nhập tên sản phẩm cần tìm (để trống để xem tất cả)");
        layout.addView(input);

        builder.setView(layout);

        // Thêm nút tìm kiếm
        builder.setPositiveButton("Tìm kiếm", (dialog, which) -> {
            String searchText = input.getText().toString().trim();
            searchJewelry(searchText);
        });

        // Thêm nút hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void searchJewelry(String searchText) {
        showLoading();
        new Thread(() -> {
            try {
                List<Jewelry> searchResults = databaseManager.searchJewelry(searchText);
                runOnUiThread(() -> {
                    hideLoading();
                    if (searchResults != null && !searchResults.isEmpty()) {
                        adapter.setJewelryList(searchResults);
                        hideEmptyView();
                    } else {
                        showEmptyView();
                        Toast.makeText(this, "Không tìm thấy sản phẩm phù hợp", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi tìm kiếm sản phẩm: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    hideLoading();
                    showError("Lỗi khi tìm kiếm sản phẩm");
                    showEmptyView();
                });
            }
        }).start();
    }

    private void addToCart(Jewelry jewelry) {
        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        builder.setView(dialogView);

        // Lấy các view từ dialog
        TextView tvProductName = dialogView.findViewById(R.id.tvProductName);
        TextView tvProductPrice = dialogView.findViewById(R.id.tvProductPrice);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        Button btnDecrease = dialogView.findViewById(R.id.btnDecrease);
        Button btnIncrease = dialogView.findViewById(R.id.btnIncrease);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Hiển thị thông tin sản phẩm
        tvProductName.setText(jewelry.getName());
        tvProductPrice.setText(String.format("$%.2f", jewelry.getPrice()));

        // Xử lý tăng/giảm số lượng
        btnIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(tvQuantity.getText().toString());
            tvQuantity.setText(String.valueOf(quantity + 1));
        });

        btnDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(tvQuantity.getText().toString());
            if (quantity > 1) {
                tvQuantity.setText(String.valueOf(quantity - 1));
            }
        });

        // Tạo và hiển thị dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý nút hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            int quantity = Integer.parseInt(tvQuantity.getText().toString());
            
            // Tạo CartItem mới
            CartItem cartItem = new CartItem();
            cartItem.setProduct(jewelry);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(jewelry.getPrice());

            // Lưu vào database
            new Thread(() -> {
                try {
                    // Lấy accountId từ SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                    int accountId = prefs.getInt("account_id", -1);

                    if (accountId != -1) {
                        // Lưu vào database
                        DatabaseManager dbManager = DatabaseManager.getInstance();
                        dbManager.addToCart(accountId, cartItem);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }
}