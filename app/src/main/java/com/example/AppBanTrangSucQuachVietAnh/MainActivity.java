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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;

import com.example.AppBanTrangSucQuachVietAnh.adapter.JewelryAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

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

        // Lấy thông tin đăng nhập từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");
        String userRole = prefs.getString("user_role", "");
        userId = prefs.getInt("user_id", -1);

        if (username.isEmpty() || password.isEmpty()) {
            Log.d(TAG, "Chưa đăng nhập, chuyển đến LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Kiểm tra quyền admin
        boolean isAdmin = "admin".equals(userRole);
        Log.d(TAG, "Quyền người dùng: " + userRole + ", là admin: " + isAdmin);

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
                    new AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteJewelry(jewelry))
                        .setNegativeButton("Hủy", null)
                        .show();
                },
                // Xử lý sự kiện click vào item
                jewelry -> {
                    Log.d(TAG, "Click vào sản phẩm: " + jewelry.getId());
                    if (isAdmin) {
                        Intent intent = new Intent(this, EditJewelryActivity.class);
                        intent.putExtra("jewelry", jewelry);
                        startActivity(intent);
                    } else {
                        showAddToCartDialog(jewelry);
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
        Log.d(TAG, "Bắt đầu tải danh sách sản phẩm");
        
        // Hiển thị loading trên main thread
        runOnUiThread(() -> showLoading());

        new Thread(() -> {
            try {
                // Kiểm tra kết nối database
                if (databaseManager == null) {
                    Log.e(TAG, "DatabaseManager chưa được khởi tạo");
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Lỗi kết nối cơ sở dữ liệu");
                    });
                    return;
                }

                // Kiểm tra kết nối
                if (!databaseManager.checkConnection()) {
                    Log.e(TAG, "Không thể kết nối đến cơ sở dữ liệu");
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Không thể kết nối đến cơ sở dữ liệu");
                    });
                    return;
                }

                Log.d(TAG, "Đang lấy danh sách sản phẩm từ database");
                List<Jewelry> jewelryList = databaseManager.getAllJewelry();
                Log.d(TAG, "Số lượng sản phẩm lấy được: " + (jewelryList != null ? jewelryList.size() : 0));

                // Cập nhật UI trên main thread
                runOnUiThread(() -> {
                    hideLoading();
                    if (jewelryList == null || jewelryList.isEmpty()) {
                        Log.d(TAG, "Không có sản phẩm nào");
                        showEmptyView();
                    } else {
                        Log.d(TAG, "Hiển thị " + jewelryList.size() + " sản phẩm");
                        hideEmptyView();
                        adapter.setJewelryList(jewelryList);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi tải danh sách sản phẩm: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    hideLoading();
                    showError("Lỗi tải danh sách sản phẩm: " + e.getMessage());
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
        }
        if (item.getItemId() == R.id.menu_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("user_id", userId);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Thêm menu giỏ hàng cho user
        if (!isAdmin) {
            menu.add(Menu.NONE, R.id.menu_cart, Menu.NONE, "Giỏ hàng")
                .setIcon(android.R.drawable.ic_menu_more)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        // Thiết lập SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        searchJewelry(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.isEmpty()) {
                            loadJewelryData();
                        }
                        return true;
                    }
                });
            }
        }

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

    private void showAddToCartDialog(Jewelry jewelry) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView textProductName = dialogView.findViewById(R.id.textProductName);
        TextView textProductPrice = dialogView.findViewById(R.id.textProductPrice);
        TextView textStock = dialogView.findViewById(R.id.textStock);
        EditText editQuantity = dialogView.findViewById(R.id.editQuantity);
        ImageButton buttonDecrease = dialogView.findViewById(R.id.buttonDecrease);
        ImageButton buttonIncrease = dialogView.findViewById(R.id.buttonIncrease);
        Button buttonAddToCart = dialogView.findViewById(R.id.buttonAddToCart);

        // Hiển thị thông tin sản phẩm
        textProductName.setText(jewelry.getName());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textProductPrice.setText(currencyFormat.format(jewelry.getPrice()));
        textStock.setText("Còn lại: " + jewelry.getStock());

        // Xử lý tăng giảm số lượng
        buttonDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(editQuantity.getText().toString());
            if (quantity > 1) {
                editQuantity.setText(String.valueOf(quantity - 1));
            }
        });

        buttonIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(editQuantity.getText().toString());
            if (quantity < jewelry.getStock()) {
                editQuantity.setText(String.valueOf(quantity + 1));
            }
        });

        // Xử lý thêm vào giỏ hàng
        buttonAddToCart.setOnClickListener(v -> {
            int quantity = Integer.parseInt(editQuantity.getText().toString());
            if (quantity > jewelry.getStock()) {
                Toast.makeText(this, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem cartItem = new CartItem(userId, jewelry.getId(), quantity);
            new Thread(() -> {
                boolean success = databaseManager.addToCart(cartItem);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        dialog.show();
    }

    private void searchJewelry(String keyword) {
        new Thread(() -> {
            List<Jewelry> results = databaseManager.searchJewelry(keyword);
            runOnUiThread(() -> {
                if (results.isEmpty()) {
                    showEmptyView();
                } else {
                    hideEmptyView();
                    adapter.updateData(results);
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